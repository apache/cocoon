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
package org.apache.cocoon.forms.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.forms.formmodel.AbstractContainerWidget;
import org.apache.cocoon.forms.formmodel.ContainerWidget;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.collections.iterators.AbstractIteratorDecorator;

/**
 * A <code>Map</code> view of a container widget, keys being children names and values either
 * maps (for container children), objects (for terminal children) or lists (for repeaters).
 * <p>
 * The returned map is non-modifiable, except using the <code>put()</code> method, which much
 * refer to an existing child widget, and <code>putAll(Map)</code> that will silently ignore keys
 * that don't refer to existing child widgets.
 * <p>
 * Also, this map accepts getting and setting values for keys that correspond to value-less widgets
 * such as {@link org.apache.cocoon.forms.formmodel.Action}. The result in that case is always
 * <code>null</code>. This is to allow global retrieving or filling of the map values.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class ContainerWidgetAsMap extends AbstractMap {
    protected AbstractContainerWidget container;
    private boolean lowerCase;

    /**
     * Wraps a container widget in a <code>Map</code>.
     * <p>
     * The <code>keysToLowerCase</code> argument specifies if input keys given in <code>get()</code>,
     * <code>put()</code> and <code>putAll()</code> should be converted to lower case before searching for
     * the corresponding widget. This feature allows to directly feed widgets with <code>Map</code>s coming
     * from JDBC resultset rows where keys are uppercase (see <a href="http://jdbi.codehaus.org">JDBI</a>).
     * 
     * @param container the container to wrap
     * @param keysToLowerCase should we convert keys to lower case?
     */
    public ContainerWidgetAsMap(AbstractContainerWidget container, boolean keysToLowerCase) {
        this.container = container;
        this.lowerCase = keysToLowerCase;
    }

    /**
     * Same as <code>ContainerWidgetAsMap(container, false)</code>
     */
    public ContainerWidgetAsMap(AbstractContainerWidget container) {
        this(container, false);
    }

    /**
     * Get the container widget that is wrapped by this <code>Map</code>.
     * 
     * @return the wrapped {@link ContainerWidget}
     */
    public ContainerWidget getWidget() {
        return this.container;
    }
    
    /**
     * Get a widget relative to the container wrapped by this <code>Map</code>
     * 
     * @param path a widget lookup path
     * @return the widget pointed to by <code>path</code> or <code>null</code> if it doesn't exist.
     * @see Widget#lookupWidget(String)
     */
    public Widget getWidget(String path) {
        return this.container.lookupWidget(path);
    }

    /**
     * Put a value in a child widget. The value must be compatible with the datatype
     * expected by the child widget. In the case of repeaters and containers, this
     * datatype is <code>Collection</code> and <code>Map</code> respectively, which
     * will be used to fill the rows and child widgets.
     * <p>
     * Note also that the contract of <code>put</code> requires the previous value
     * to be returned. In the case of repeaters and containers, the value is a live
     * wrapper around the actual widget, meaning that it's not different from the
     * current value.
     */
    public Object put(Object key, Object value) {
        String name = (String)key;
        if (lowerCase) name = name.toLowerCase();
        
        Widget w = container.getChild(name);
        if (w != null) {
            return setValue(w, value);
        } else {
            throw new UnsupportedOperationException(container + " has no child named '" + key + "'");
        }
    }

    public void putAll(Map map) {
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String name = (String)entry.getKey();
            if (lowerCase) name = name.toLowerCase();
            Widget w = container.getChild(name);
            if (w != null) {
                setValue(w, entry.getValue());
            }
        }
    }

    public Object get(Object key) {
        String name = (String)key;
        if (lowerCase) name = name.toLowerCase();
        Widget w = container.getChild(name);
        return w == null ? null : asValueOrMap(w);
    }

    public Set entrySet() {
        return new ContainerEntrySet();
    }

    private Object asValueOrMap(Widget w) {
        if (w instanceof Repeater) {
            return new RepeaterAsList((Repeater)w, lowerCase);
        } else if (w instanceof AbstractContainerWidget) {
            return new ContainerWidgetAsMap((AbstractContainerWidget)w, lowerCase);
        } else {
            try {
                return w.getValue();
            } catch (UnsupportedOperationException uoe) {
                // This widget doesn't hold a value
                return null;
            }
        }
    }

    /**
     * Set a widget's value and returns the previous value as required by put().
     */
    private Object setValue(Widget w, Object value) {
        if (w instanceof Repeater) {
            // Must be a collection
            if (!(value instanceof Collection)) {
                throw new IllegalArgumentException("A repeater cannot be filled with " + value);
            }
            List result = new RepeaterAsList((Repeater)w, lowerCase);
            result.addAll((Collection)value);
            return result;

        } else if (w instanceof AbstractContainerWidget) {
            // Must be a map
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException("A container cannot be filled with " + value);
            }
            Map result = new ContainerWidgetAsMap((AbstractContainerWidget)w);
            result.putAll((Map)value);
            return result;
        } else {
            try {
                Object result = w.getValue();
                w.setValue(value);
                return result;
            } catch (UnsupportedOperationException uoe) {
                // This widget doesn't hold a value
                return null;
            }
        }
    }

    private class ContainerEntrySet extends AbstractSet {
        public Iterator iterator() {
            return new ContainerEntryIterator();
        }

        public int size() {
            return container.getSize();
        }
    }

    private class ContainerEntryIterator extends AbstractIteratorDecorator {
        public ContainerEntryIterator() {
            super(container.getChildren());
        }

        public Object next() {
            return new ContainerEntry((Widget)super.next());
        }
    }

    private class ContainerEntry implements Map.Entry {
        Widget widget;
        public ContainerEntry(Widget w) {
            widget = w;
        }
        public Object getKey() {
            return widget.getName();
        }
        public Object getValue() {
            return asValueOrMap(widget);
        }
        public Object setValue(Object value) {
            Object result = asValueOrMap(widget);
            ContainerWidgetAsMap.this.setValue(widget, value);
            return result;
        }
    }
}