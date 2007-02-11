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

import org.apache.cocoon.portal.layout.Layout;


/**
 * Show one item of the basket
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ShowItemEvent.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class ShowItemEvent extends BasketEvent {
    
    /** The item to show */
    protected Object item;
    
    /** The layout object to use */
    protected Layout layout;
    
    /** The id of the coplet data used to display the content */
    protected String copletData;
    
    /**
     * Constructor
     * @param item       The item to show
     * @param layout     The layout object where the item is displayed
     * @param copletData The coplet data id of a content coplet
     */
    public ShowItemEvent(Object item, Layout layout, String copletData) {        
        this.item = item;
        this.layout = layout;
        this.copletData = copletData;
    }
    
    /**
     * Return the basket id
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
