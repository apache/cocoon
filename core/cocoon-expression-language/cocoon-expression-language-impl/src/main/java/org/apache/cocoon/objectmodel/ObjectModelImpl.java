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
package org.apache.cocoon.objectmodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.collections.map.AbstractMapDecorator;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.collections.map.UnmodifiableMap;

/**
 * Prototype implementation of {@link ObjectModel} interface. It <b>must</b> be initialized manually for now.
 *
 */
public class ObjectModelImpl extends AbstractMapDecorator implements ObjectModel {
    //FIXME: It seems that there is no easy way to reuse MuliValueMap
    
    private ArrayStack localContexts;
    private Map singleValueMap;
    private MultiMap multiValueMap;
    
    public ObjectModelImpl() {
        singleValueMap = new HashMap();
        super.map = UnmodifiableMap.decorate(singleValueMap);
        localContexts = new ArrayStack();
        multiValueMap = MultiValueMap.decorate(new HashMap(), StackReversedIteration.class);
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
        if ("this".equals(key))
            return this;
        return super.get(key);
    }
    
    public Map getAll() {
        return UnmodifiableMap.decorate(multiValueMap);
    }
    
    public Object put(Object key, Object value) {
        if (!localContexts.empty())
            ((ArrayStack) localContexts.peek()).push(new DefaultKeyValue(key, value));
        
        singleValueMap.put(key, value);
        multiValueMap.put(key, value);
        
        return value;
    }
    
    public void putAll(Map mapToCopy) {
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

    public void cleanupLocalContext() {
        if (localContexts.empty())
            throw new IllegalStateException("Local contexts stack is empty");
        ArrayStack removeEntries = (ArrayStack)localContexts.pop();
        while (!removeEntries.isEmpty()) {
            KeyValue entry = (KeyValue)removeEntries.pop();
            Object key = entry.getKey();
            Object value = entry.getValue();
            
            multiValueMap.remove(key, value);
            if (multiValueMap.containsKey(key))
                singleValueMap.put(key, ((StackReversedIteration)multiValueMap.get(key)).peek());
            else
                singleValueMap.remove(key);
        }
    }

    public void markLocalContext() {
        localContexts.push(new ArrayStack());
    }
    
}