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

import java.util.List;

/**
 * This is the manager for baskets.
 * You can retrieve the current basket for the user from this manager.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: BasketManager.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public interface BasketManager {
    
    /** The component role */
    String ROLE = BasketManager.class.getName();
    
    /** This key is used to store the current basket in the session */
    String BASKET_KEY = BasketManager.class.getName();

    /** This key is used to store all baskets in the session */
    String ALL_BASKETS_KEY = BasketManager.class.getName() + "/All";

    /**
     * Return the basket of the current user
     */
    Basket getBasket();
   
    /**
     * Return all baskets. 
     * This is a list of BasketDescription objects
     */
    List getBaskets();

    /**
     * This class describes a basket
     */
    public class BasketDescription {
        /** The id */
        public String id;
        /** The size of the basket */
        public int    size;
    }
}
