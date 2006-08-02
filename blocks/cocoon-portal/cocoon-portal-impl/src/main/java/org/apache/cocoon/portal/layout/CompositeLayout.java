/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.layout.impl.CompositeLayoutImpl;

/**
 * A composite layout is a layout that contains other layouts.
 *
 * @version $Id$
 */
public abstract class CompositeLayout 
    extends Layout {

    /** The children of this layout object. */
    protected final List items = new ArrayList();

    /**
     * Create a new composite layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param name The name of the layout.
     */
    public CompositeLayout(String id, String name) {
        super(id, name);
    }

    /**
     * Add indexed item to the itemList.
     * @param index index for the position inside the list
     * @param item item to add
     */
    public final void addItem(int index, Item item) {
        this.items.add(index, item);
        item.setParent(this);
    }

    /**
     * Add Item to the ItemList.
     * @param item item to add
     */
    public final void addItem(Item item) {
        this.items.add(item);
        item.setParent(this);
    }

    /**
     * Get Item from the ItemList.
     * @return Item
     */
    public final Item getItem(int index) {
        return (Item) this.items.get(index);
    }

    /**
     * Get the ItemList.
     * @return items
     */
    public final List getItems() {
        return this.items;
    }

    /**
     * Get size of ItemList.
     * @return size
     */
    public final int getSize() {
        return this.items.size();
    }

    /**
     * Remove an item from the item list.
     */
    public final void removeItem(Item item) {
        this.items.remove(item);
        item.setParent(null);
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        CompositeLayoutImpl clone = (CompositeLayoutImpl)super.clone();

        // we are not cloning the items
        clone.items.clear();

        return clone;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#copy()
     */
    public Layout copy() {
        CompositeLayout clone = (CompositeLayout)super.copy();
        final Iterator i = this.items.iterator();
        while ( i.hasNext() ) {
            final Item current = (Item)i.next();
            final Item clonedItem = current.copy(clone);
            clone.addItem(clonedItem);
        }
        return clone;
    }
}
