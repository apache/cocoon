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
 * Clean all baskets or one single basket
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: CleanBasketEvent.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class CleanBasketEvent extends BasketEvent {
    
    /** The basket id that will be cleaned */
    protected String basketId;
    
    /**
     * Constructor
     * If this constructor is used all baskets will be cleaned
     */
    public CleanBasketEvent() {
    }
    
    /**
     * Constructor
     * One basket will be cleaned
     * @param basketId The id of the basket
     */
    public CleanBasketEvent(String basketId) {
        this.basketId = basketId;
    }
    
    /**
     * Return the basket id or null for all baskets
     */
    public String getBasketId() {
        return this.basketId;
    }
}
