/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.el.impl.objectmodel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.apache.cocoon.el.util.MultiMap;
import org.apache.cocoon.el.util.MultiValueMap;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.objectmodel.ObjectModelProvider;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.collections.map.AbstractMapDecorator;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;

/**
 * Prototype implementation of {@link ObjectModel} interface. It <b>must</b> be initialized manually for now.
 *
 */
public class ObjectModelImpl extends AbstractMapDecorator implements ObjectModel {
    //FIXME: It seems that there is no easy way to reuse MuliValueMap

    private static final String SEGMENT_SEPARATOR = "/";

    private ArrayStack localContexts;
    private Map singleValueMap;
    private MultiMap multiValueMap;
    private MultiMap multiValueMapForLocated;
    private Map initialEntries;

    //FIXME: This is a temporary solution
    private boolean modified;


    public ObjectModelImpl() {
        singleValueMap = new HashMap();
        //FIXME: Not sure if this makes sense
        //super.map = UnmodifiableMap.decorate(singleValueMap);
        super.map = singleValueMap;
        localContexts = new ArrayStack();
        multiValueMap = MultiValueMap.decorate(new HashMap(), StackReversedIteration.class);
        multiValueMapForLocated = MultiValueMap.decorate(new HashMap(), StackReversedIteration.class);
    }

    public static class StackReversedIteration extends ArrayStack {

        public Iterator iterator() {
            return new ReverseListIterator(this);
        }

        public ListIterator listIterator() {
            throw new UnsupportedOperationException();
        }
    }

    public Object get(Object key) {
        //FIXME: This should be done more elegantly
        if ("this".equals(key)) {
            return this;
        }

        return super.get(key);
    }

    public MultiMap getAll() {
        return UnmodifiableMultiMap.decorate(multiValueMap);
    }

    public Object put(Object key, Object value) {
        modified = true;
        if (!localContexts.empty()) {
            ((ArrayStack) localContexts.peek()).push(new DefaultKeyValue(key, value));
        }

        singleValueMap.put(key, value);
        multiValueMap.put(key, value);

        return value;
    }

    public void putAll(Map mapToCopy) {
        modified = true;
        if (!localContexts.empty()) {
            ArrayStack entries = (ArrayStack)localContexts.peek();
            for (Iterator keysIterator = mapToCopy.keySet().iterator(); keysIterator.hasNext();) {
                Object key = keysIterator.next();
                entries.push(new DefaultKeyValue(key, mapToCopy.get(key)));
            }
        }

        singleValueMap.putAll(mapToCopy);
        multiValueMap.putAll(mapToCopy);
    }

    /**
     * Locates map at given path
     * @param path where Map can be found
     * @param createIfNeeded indicates if map(s) should be created if no corresponding found
     * @return located Map or null if <code>createIfNeeded</code> is false and Map cannot be found
     */
    private Map locateMapAt(String path, boolean createIfNeeded) {
        if (path.lastIndexOf(SEGMENT_SEPARATOR) == -1) {
            return this;
        }

        Map map = this;
        int segmentBegin = 0;
        int segmentEnd = path.indexOf(SEGMENT_SEPARATOR);
        while (segmentEnd != -1) {
            String key = path.substring(segmentBegin, segmentEnd);
            if (map.containsKey(key)) {
                Object obj = map.get(key);
                if (!(obj instanceof Map)) {
                    throw new ClassCastException("Object at path " + path.substring(0, segmentEnd) + "is not a Map");
                }

                map = (Map)obj;
            } else {
                if (!createIfNeeded) {
                    return null;
                }

                Map newMap = new HashMap();
                map.put(key, newMap);
                map = newMap;
            }
            segmentBegin = segmentEnd + 1;
            segmentEnd = path.indexOf(SEGMENT_SEPARATOR, segmentBegin);
        }

        return map;
    }

    public void putAt(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Path cannot be null.");
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        Map map = locateMapAt(path, true);
        String key = path.substring(path.lastIndexOf(SEGMENT_SEPARATOR) + 1, path.length());
        if (!localContexts.empty()) {
            ((ArrayStack) localContexts.peek()).push(new PathValue(path, value));
        }
        map.put(key, value);
    }

    private void removeAt(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Path cannot be null.");
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        Map map = locateMapAt(path, false);
        String key = path.substring(path.lastIndexOf(SEGMENT_SEPARATOR) + 1, path.length());
        if (map == null) {
            return;
        }

        multiValueMapForLocated.remove(key, value);
        if (multiValueMap.containsKey(key)) {
            map.put(key, ((StackReversedIteration) multiValueMap.get(key)).peek());
        } else {
            map.remove(key);
        }
    }

    public void cleanupLocalContext() {
        if (localContexts.empty()) {
            throw new IllegalStateException("Local contexts stack is empty");
        }

        ArrayStack removeEntries = (ArrayStack)localContexts.pop();
        while (!removeEntries.isEmpty()) {
            if (removeEntries.peek() instanceof PathValue) {
                PathValue entry = (PathValue)removeEntries.pop();
                removeAt(entry.getPath(), entry.getValue());
            } else {
                KeyValue entry = (KeyValue)removeEntries.pop();
                Object key = entry.getKey();
                Object value = entry.getValue();

                multiValueMap.remove(key, value);
                if (multiValueMap.containsKey(key)) {
                    singleValueMap.put(key, ((StackReversedIteration) multiValueMap.get(key)).peek());
                } else {
                    singleValueMap.remove(key);
                }
            }
        }
    }

    public void markLocalContext() {
        localContexts.push(new ArrayStack());
    }

    public Map getInitialEntries() {
        return initialEntries;
    }

    public void setInitialEntries(Map initialEntries) {
        if (this.initialEntries != null) {
            throw new IllegalStateException("Object Model has initial entries set already.");
        }

        this.initialEntries = initialEntries;
        for (Iterator keysIterator = initialEntries.keySet().iterator(); keysIterator.hasNext(); ) {
            Object key = keysIterator.next();
            put(key, ((ObjectModelProvider)initialEntries.get(key)).getObject());
        }

        this.modified = false;
    }

    public void fillContext() {
        // Hack: I use jxpath to populate the context object's properties
        // in the jexl context
        Object contextObject = get(CONTEXTBEAN);
        if (contextObject == null) {
            //nothing to do
            return;
        }

        // FIXME Exception Handling
        final JXPathBeanInfo bi =
            JXPathIntrospector.getBeanInfo(contextObject.getClass());
        if (bi.isDynamic()) {
            Class cl = bi.getDynamicPropertyHandlerClass();
            try {
                DynamicPropertyHandler h =
                    (DynamicPropertyHandler) cl.newInstance();
                String[] result = h.getPropertyNames(contextObject);
                int len = result.length;
                for (int i = 0; i < len; i++) {
                    try {
                        put(result[i], h.getProperty(contextObject, result[i]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            PropertyDescriptor[] props =  bi.getPropertyDescriptors();
            int len = props.length;
            for (int i = 0; i < len; i++) {
                try {
                    Method read = props[i].getReadMethod();
                    if (read != null) {
                        put(props[i].getName(),
                            read.invoke(contextObject, null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final class PathValue {
        private String path;
        private Object value;

        public PathValue(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public String getPath() {
            return this.path;
        }

        public Object getValue() {
            return this.value;
        }

    }

    /* (non-Javadoc)
     * @see ObjectModel#setParent(ObjectModel)
     */
    public void setParent(ObjectModel parentObjectModel) {
        if (this.modified) {
            throw new IllegalStateException("Setting parent may occur only if Object Model is empty.");
        }

        singleValueMap.putAll(parentObjectModel);
        multiValueMap.putAll(parentObjectModel.getAll());
    }
}
