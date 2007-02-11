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
package org.apache.cocoon.portal.layout;

import java.util.List;


/**
 * A composite layout is a layout that contains other layouts.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: CompositeLayout.java,v 1.9 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public interface CompositeLayout 
    extends Layout {

	/**
	 * Add indexed item to the itemList.
	 * @param index index for the position inside the list
	 * @param item item to add
	 */
    void addItem(int index, Item item);

	/**
	 * Add Item to the ItemList.
	 * @param item item to add
	 */
	void addItem(Item item);

    /**
     * Get the item at the index
     * @param index
     * @return The item or null
     */
	Item getItem(int index);

	/**
	 * Get the ItemList.
	 * @return items
	 */
	List getItems();

	/**
	 * Get size of ItemList.
	 * @return size
	 */
	int getSize();
    
    /**
     * Remove an item
     * @param item
     */
    void removeItem(Item item);
    
}
