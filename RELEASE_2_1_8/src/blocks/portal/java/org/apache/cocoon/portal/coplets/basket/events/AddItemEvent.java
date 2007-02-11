/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplets.basket.events;

import org.apache.cocoon.portal.coplets.basket.ContentStore;

/**
 * This event is used to add an item to the content store
 *
 * @version CVS $Id: AddItemEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class AddItemEvent extends ContentStoreEvent {
    
    /** The item to add */
    protected Object item;
    
    /**
     * Constructor
     * @param store   The content store
     * @param item    The item that will be added to the content store
     */
    public AddItemEvent(ContentStore store, Object item) {
        super(store);
        this.item = item;
    }
    
    /**
     * The item
     */
    public Object getItem() {
        return this.item;
    }
}
