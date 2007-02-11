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
 * Move an item from one store to another
 *
 * @version CVS $Id: RemoveItemEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class MoveItemEvent extends ContentStoreEvent {
    
    /** The item to move */
    protected Object item;
    
    /** The target store */
    protected final ContentStore target;
    
    /**
     * Constructor
     * @param store The content store
     * @param item  The item to move
     * @param target The target store
     */
    public MoveItemEvent(ContentStore store, Object item, ContentStore target) {
        super(store);
        this.item = item;
        this.target = target;
    }
    
    /**
     * Return the item to remove
     */
    public Object getItem() {
        return this.item;
    }
    
    /** 
     * Return the target
     */
    public ContentStore getTarget() {
        return this.target;
    }
}
