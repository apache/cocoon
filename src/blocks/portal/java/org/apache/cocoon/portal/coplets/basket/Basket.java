/*
 * Copyright 2004,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * This is a per user basket
 * Make a subclass to add your specific functionality
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: Basket.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class Basket implements Serializable {
    
    /** The ordered list of items */
    protected List items = new ArrayList();
    
    /**
     * The constructor
     */
    public Basket() {
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
}
