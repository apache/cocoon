/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * This is the manager for content-stores: baskets, briefcases and folders
 * You can retrieve the current basket, briefcase or folder for the user 
 * from this manager.
 *
 * @version CVS $Id$
 */
public interface BasketManager {
    
    /** The component role */
    String ROLE = BasketManager.class.getName();
    
    /** This key is used to store the current basket in the session */
    String BASKET_KEY = BasketManager.class.getName() + "/Basket";

    /** This key is used to store the current briefcase in the session */
    String BRIEFCASE_KEY = BasketManager.class.getName() + "/Briefcase";

    /** This key is used to store the current folder in the session */
    String FOLDER_KEY = BasketManager.class.getName() + "/Folder";

    /** This key is used to store all briefcases in the session (of the admin) */
    String ALL_BRIEFCASES_KEY = BasketManager.class.getName() + "/All";

    /**
     * Return the basket of the current user
     */
    Basket getBasket();
   
    /**
     * Return the briefcase of the current user
     */
    Briefcase getBriefcase();

    /**
     * Return the folder of the current user
     */
    Folder getFolder();

    /**
     * Return all briefcases. 
     * This is a list of {@link ContentStoreDescription} objects.
     */
    List getBriefcaseDescriptions();

    /** 
     * An action info consists of a name and a url
     */
    public static class ActionInfo {
        public final String name;
        public final String url;
        
        public ActionInfo(String n, String u) {
            this.name = n;
            this.url = u;
        }
    }

    /**
     * Return all configured actions for a basket - this is a list of 
     * {@link ActionInfo}s.
     */
    List getBasketActions();

    /**
     * Get the info
     */
    ActionInfo getBasketAction(String name);
    
    /**
     * Return all configured actions for a briefcase - this is a list of 
     * {@link ActionInfo}s.
     */
    List getBriefcaseActions();

    /**
     * Get the info
     */
    ActionInfo getBriefcaseAction(String name);

    void addBatch(ContentItem item,
                  int         frequencyInDays,
                  ActionInfo  action);
    
    /**
     * Update/save the content store
     */
    void update(ContentStore store);
}
