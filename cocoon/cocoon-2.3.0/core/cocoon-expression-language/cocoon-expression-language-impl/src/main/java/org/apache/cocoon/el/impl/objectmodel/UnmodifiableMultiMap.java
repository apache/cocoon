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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.el.util.MultiMap;
import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Unmodifiable;
import org.apache.commons.collections.collection.UnmodifiableCollection;
import org.apache.commons.collections.iterators.EntrySetMapIterator;
import org.apache.commons.collections.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections.map.AbstractMapDecorator;
import org.apache.commons.collections.map.UnmodifiableEntrySet;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.collections.set.UnmodifiableSet;

/**
 * <p>This class brings equally the same functionality as {@link UnmodifiableMap} but also implements {@link MultiMap} interface.</p>
 * 
 * <p>Use this class to wrap {@link MultiMap MultiMaps} only.</p> 
 */
public class UnmodifiableMultiMap extends AbstractMapDecorator implements MultiMap, IterableMap, Unmodifiable {
    
    public static MultiMap decorate(MultiMap map) {
        if (map instanceof UnmodifiableMultiMap)
            return map;
        return new UnmodifiableMultiMap(map);
    }

    private UnmodifiableMultiMap(MultiMap map) {
        super(map);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.collections.MultiMap#values()
     */
    public Collection values() {
        Collection coll = super.values();
        return UnmodifiableCollection.decorate(coll);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        Set set = super.entrySet();
        return UnmodifiableEntrySet.decorate(set);
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        Set set = super.keySet();
        return UnmodifiableSet.decorate(set);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.collections.IterableMap#mapIterator()
     */
    public MapIterator mapIterator() {
        if (map instanceof IterableMap) {
            MapIterator it = ((IterableMap) map).mapIterator();
            return UnmodifiableMapIterator.decorate(it);
        } else {
            MapIterator it = new EntrySetMapIterator(map);
            return UnmodifiableMapIterator.decorate(it);
        }
    }
    
    //-----------------------------------------------------------------------
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map mapToCopy) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    public boolean remove(Object key, Object item) {
        throw new UnsupportedOperationException();
    }

}
