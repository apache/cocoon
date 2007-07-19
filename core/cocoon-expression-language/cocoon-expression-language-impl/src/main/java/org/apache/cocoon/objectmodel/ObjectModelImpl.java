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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.collections.map.AbstractMapDecorator;

/**
 * Prototype implementation of {@link ObjectModel} interface. It <b>must</b> be initialized manually for now.
 *
 */
public class ObjectModelImpl extends AbstractMapDecorator implements ObjectModel {
    //FIXME: It seems that there is no easy way to reuse MuliValueMap
    
    private ArrayStack localContexts;
    
    public ObjectModelImpl() {
        super(new HashMap());
        localContexts = new ArrayStack();
    }

    private static class StackReversedIteration extends ArrayStack {
        
        public Iterator iterator() {
            return new ReverseListIterator(this);
        }
        
        public ListIterator listIterator() {
            throw new UnsupportedOperationException();
        }
    }
    
    public Object put(Object key, Object value) {
        if (!localContexts.empty())
            ((Collection) localContexts.peek()).add(new DefaultKeyValue(key, value));
        
        Object valuesForKey = getMap().get(key);
        if (valuesForKey == null) {
            super.put(key, value);
            return value;
        }
        else if (valuesForKey instanceof StackReversedIteration)
            return ((StackReversedIteration) valuesForKey).add(value) ? value : null;
        else {
            StackReversedIteration stack = new StackReversedIteration();
            stack.add(valuesForKey);
            stack.add(value);
            super.put(key, stack);
            return value;
        }
    }
    
    public void putAll(Map mapToCopy) {
        if (!localContexts.empty()) {
            Collection entries = (Collection)localContexts.peek();
            for (Iterator keysIterator = mapToCopy.keySet().iterator(); keysIterator.hasNext();) {
                Object key = keysIterator.next();
                entries.add(new DefaultKeyValue(key, mapToCopy.get(key)));
            }
        }
        
        super.putAll(mapToCopy);
    }

    public Object remove(Object key, Object item) {
        Object valuesForKey = getMap().get(key);
        if (valuesForKey == null)
            return null;
        else if (valuesForKey instanceof StackReversedIteration) {
            StackReversedIteration stack = (StackReversedIteration)valuesForKey;
            boolean changed = stack.remove(item);
            if (stack.size() == 1) {
                super.put(key, stack.pop());
                changed = true;
            }
            return changed ? item : null;
        } 
        else
            return super.remove(key);
    }
    
    /**
     * Gets the total size of the map by counting all the values.
     *
     * @return the total size of the map counting all values
     */
    public int totalSize() {
        int total = 0;
        Collection values = getMap().values();
        for (Iterator it = values.iterator(); it.hasNext();) {
            Object object = it.next();
            if (object instanceof StackReversedIteration)
                total += ((Collection) object).size();
            else
                total++;
        }
        return total;
    }
    
    public Collection values() {
        return new Values();
    }
    
    private class Values extends AbstractCollection {
        public Iterator iterator() {
            final IteratorChain chain = new IteratorChain();
            for (Iterator it = keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object object = get(key);
                if (object instanceof StackReversedIteration)
                    chain.addIterator(new ValuesIterator(key));
                else
                    //TODO: Implement removing
                    chain.addIterator(new SingletonIterator(object, false));
            }
            return chain;
        }

        public int size() {
            return totalSize();
        }

        public void clear() {
            ObjectModelImpl.this.clear();
        }
    }

    /**
     * Inner class that provides the values iterator.
     */
    private class ValuesIterator implements Iterator {
        private final Object key;
        private final Collection values;
        private final Iterator iterator;

        public ValuesIterator(Object key) {
            this.key = key;
            this.values = (Collection)getMap().get(key);
            this.iterator = values.iterator();
        }

        public void remove() {
            iterator.remove();
            if (values.isEmpty()) {
                ObjectModelImpl.this.remove(key);
            }
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            return iterator.next();
        }
    }

    public void cleanupLocalContext() {
        if (localContexts.empty())
            throw new IllegalStateException("Local contexts stack is empty");
        Collection removeEntries = (Collection)localContexts.pop();
        for (Iterator entriesIterator = removeEntries.iterator(); entriesIterator.hasNext();) {
            KeyValue entry = (KeyValue) entriesIterator.next();
            remove(entry.getKey(), entry.getValue());
        }
    }

    public void markLocalContext() {
        localContexts.push(new LinkedList());
    }
    
}