/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.portal.layout.AbstractLayout;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;


/**
 * A composite layout is a layout that contains other layouts.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: CompositeLayoutImpl.java,v 1.4 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public class CompositeLayoutImpl 
    extends AbstractLayout
    implements CompositeLayout {

	protected List items = new ArrayList();

    /**
     * Constructor
     */
    public CompositeLayoutImpl() {}
    
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
    
    public final void removeItem(Item item) {
        this.items.remove(item);
        item.setParent(null);
    }
    
}
