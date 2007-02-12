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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A read-only implementation of <code>Map</code> that combines several other maps.
 * 
 * @version $Id$
 */
public class CombiningMap extends AbstractMap {

    protected List maps = new ArrayList();
    private boolean locked = false;

    /**
     * Adds a <code>Map</code> in the combined map, with the lowest lookup priority.
     * <p>
     * New maps cannot be added if this object was already iterated.
     * 
     * @param map the new map
     * @return this object, as a convenience to write <code>combiner.add(map1).add(map2).add(map3)</code>
     * @throw IllegalStateException if this object was already iterated.
     */
    public CombiningMap add(Map map) {
        if (locked) {
            throw new IllegalStateException("Cannot add new Maps to a CombiningMap once it has been iterated");
        }
        maps.add(map);
        
        return this;
    }
    
    public Object get(Object key) {
        // Faster implemetation than the default in AbstractMap
        for (int i = 0; i < maps.size(); i++) {
            Map map = (Map)maps.get(i);
            Object result = map.get(key);

            if (result != null) {
                return result;
            }

            if (map.containsKey(key)) {
                return null;
            }
        }
        
        return null;
    }
    
    public boolean containsKey(Object key) {
        // Faster implemetation than the default in AbstractMap
        for (int i = 0; i < maps.size(); i++) {
            Map map = (Map)maps.get(i);
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public Set entrySet() {
        locked = true;
        return new CombiningEntrySet();
    }

    private class CombiningEntrySet extends AbstractSet {

        public Iterator iterator() {
            return new CombiningIterator();
        }

        /**
         * Super inefficient way, but this implementation is meant to be super-lightweight
         * and efficient at iterations.
         */
        public int size() {
           
            int size = 0;
            Iterator iter = iterator();
            while (iter.hasNext()) {
                size++;
                iter.next();
            }
            return size;
        }
    }
    
    private class CombiningIterator implements Iterator {
        
        private int index;
        private Iterator delegate;
        private Map.Entry next;

        public CombiningIterator() {
            // Initialize the first result
            if (!maps.isEmpty()) {
                delegate = ((Map)maps.get(0)).entrySet().iterator();
                if (delegate.hasNext()) {
                    next = (Map.Entry)delegate.next();
                }
            }
            
        }
        public boolean hasNext() {
            return next != null;
        }

        public Object next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Object result = next;
            fetchNext();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        private void fetchNext() {
            boolean skip;
            do {
                // Get an iterator that has more values
                while (delegate != null && !delegate.hasNext()) {
                    // Ended iteration on the previous map
                    index++;
                    if (index < maps.size()) {
                        delegate = ((Map)maps.get(index)).entrySet().iterator();
                    } else {
                        // Iteration finished
                        next = null;
                        delegate = null;
                        return;
                    }
                }
                
                // Get the next entry
                next = (Map.Entry)delegate.next();
                
                // Skip it if its key doesn't exist in the previous Maps
                Object key = next.getKey();
                skip = false;
                for (int i = 0; i < index-1; i++) {
                    if (((Map)maps.get(i)).containsKey(key)) {
                        skip = true;
                        continue;
                    }
                }
            } while(skip);
        }
    }
}
