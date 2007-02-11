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


/**
 * This event is used to add an item to the basket
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AddItemEvent.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class AddItemEvent extends BasketEvent {
    
    /** The item to add */
    protected Object item;
    
    /**
     * Constructor
     * @param item The item that will be added to the basket
     */
    public AddItemEvent(Object item) {
        this.item = item;
    }
    
    /**
     * The item
     */
    public Object getItem() {
        return this.item;
    }
}
