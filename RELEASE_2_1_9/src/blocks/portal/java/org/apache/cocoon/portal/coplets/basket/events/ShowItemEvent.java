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
import org.apache.cocoon.portal.layout.Layout;

/**
 * Show one item of a content store
 *
 * @version CVS $Id: ShowItemEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class ShowItemEvent extends ContentStoreEvent {
    
    /** The item to show */
    protected final Object item;
    
    /** The layout object to use */
    protected final Layout layout;
    
    /** The id of the coplet data used to display the content */
    protected final String copletData;
    
    /**
     * Constructor
     * @param store      The content store
     * @param item       The item to show
     * @param layout     The layout object where the item is displayed
     * @param copletData The coplet data id of a content coplet
     */
    public ShowItemEvent(ContentStore store, Object item, Layout layout, String copletData) {
        super(store);
        this.item = item;
        this.layout = layout;
        this.copletData = copletData;
    }
    
    /**
     * Return item
     */
    public Object getItem() {
        return this.item;
    }
    
    /**
     * Return the layout
     */
    public Layout getLayout() {
        return this.layout;
    }
    
    /**
     * Return the coplet data id
     */
    public String getCopletDataId() {
        return this.copletData;
    }
}
