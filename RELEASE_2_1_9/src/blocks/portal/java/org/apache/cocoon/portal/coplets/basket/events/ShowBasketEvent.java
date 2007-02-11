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



/**
 * Show the contents of one basket
 *
 * @version CVS $Id: ShowBasketEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class ShowBasketEvent extends ContentStoreEvent {
    
    /** The basket to show */
    protected String basketId;
    
    /**
     * Constructor
     * @param basketId The id of the basket
     */
    public ShowBasketEvent(String basketId) {
        this.basketId = basketId;
    }
    
    /**
     * Return the basket id
     */
    public String getBasketId() {
        return this.basketId;
    }
}
