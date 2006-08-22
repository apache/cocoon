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
package org.apache.cocoon.portal.coplets.basket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This is a per user store that can contain any object. 
 * Make a subclass to add your specific functionality.
 *
 * @version CVS $Id: Basket.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class ContentStore implements Serializable {
    
    /** The ordered list of items */
    protected final List items = new ArrayList();
    
    /** The id */
    protected final String id;
    
    /**
     * The constructor
     */
    public ContentStore(String id) {
        this.id = id;
    }
    
    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Get an item at the index
     */
    public Object getItem(int index) {
        return this.items.get(index);
    }
    
    /**
     * Add an item
     */
    public void addItem(Object item) {
        this.items.add(item);
    }
    
    /**
     * Get the iterator
     */
    public Iterator getIterator() {
        return this.items.iterator();
    }
    
    /**
     * Remove an item
     */
    public void removeItem(Object item) {
        this.items.remove(item);
    }
    
    /**
     * Number of items in the basket
     */
    public int size() {
        return this.items.size();
    }

    /**
     * Calculate the size of a basket
     */
    public int contentSize() {
        int size = 0;
        Iterator i = this.items.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            if ( item instanceof ContentItem ) {
                int v = ((ContentItem)item).size();
                if ( v != -1 ) {
                    size += v;
                }
            }
        }
        return size;
    }
    
    /**
     * Get an item with the id
     */
    public Object getItem(long id) {
        Iterator i = this.items.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            if ( item instanceof AbstractItem ) {
                if (((AbstractItem)item).getId() == id ) {
                    return item;
                }
            }
        }
        return null;        
    }
}

