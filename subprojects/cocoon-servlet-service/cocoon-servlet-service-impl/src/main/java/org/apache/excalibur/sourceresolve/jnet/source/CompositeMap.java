/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet.source;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Decorates a map of other maps to provide a single unified view.
 * <p>
 * This map is not modifiable and is not synchronized and is not thread-safe.
 */
public class CompositeMap implements Map {

    /** An empty array. */
    protected static Map[] EMPTY = new Map[0];

    /** Array of all maps in the composite */
    private Map[] composite = EMPTY;

    /**
     * Add an additional Map to the composite.
     *
     * @param map  the Map to be added to the composite
     */
    public void pushMap(Map map) {
		if ( this.composite.length == 0 ) {
		    this.composite = new Map[] {map};
	    } else {
            Map[] temp = new Map[this.composite.length + 1];
		    System.arraycopy(this.composite, 0, temp, 0, this.composite.length);
            temp[temp.length - 1] = map;
            this.composite = temp;
	    }
    }

    /**
     * Remove the last map from the composite.
     */
	public void popMap() {
	    if ( this.composite.length == 1 ) {
		    this.composite = EMPTY;
		} else {
		    Map[] temp = new Map[this.composite.length - 1];
			System.arraycopy(this.composite, 0, temp, 0, this.composite.length - 1);
			this.composite = temp;
		}
	}

	/**
	 * Return the number of contained maps.
	 */
	public int getMapCount() {
	    return this.composite.length;
	}

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
	    throw new UnsupportedOperationException("Clear is not supported.");
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        for (int i = this.composite.length - 1; i >= 0; --i) {
            if (this.composite[i].containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        for (int i = this.composite.length - 1; i >= 0; --i) {
            if (this.composite[i].containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        Set entries = new HashSet();
        for (int i = 0; i < this.composite.length; i++) {
            entries.addAll(this.composite[i].entrySet());
        }
        return entries;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        for (int i = this.composite.length - 1; i >= 0; --i) {
            if (this.composite[i].containsKey(key)) {
                return this.composite[i].get(key);
            }
        }
        return null;
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        for (int i = this.composite.length - 1; i >= 0; --i) {
            if (!this.composite[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        Set keys = new HashSet();
        for (int i = this.composite.length - 1; i >= 0; --i) {
            keys.addAll(this.composite[i].keySet());
        }
        return keys;
    }

    /**
     * @see java.util.Map#put(K, V)
     */
    public Object put(Object key, Object value) {
	    throw new UnsupportedOperationException("Put is not supported");
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map map) {
	    throw new UnsupportedOperationException("Put is not supported");
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
	    throw new UnsupportedOperationException("Remove is not supported");
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        int size = 0;
        for (int i = this.composite.length - 1; i >= 0; --i) {
            size += this.composite[i].size();
        }
        return size;
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        Set values = new HashSet();
        for (int i = 0; i < this.composite.length; i++) {
            values.addAll(this.composite[i].values());
        }
        return values;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Map) {
            Map map = (Map) obj;
            return (this.entrySet().equals(map.entrySet()));
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int code = 0;
        for (Iterator i = this.entrySet().iterator(); i.hasNext();) {
            code += i.next().hashCode();
        }
        return code;
    }
}
