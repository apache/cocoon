/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.portal.wsrp.consumer;

import oasis.names.tc.wsrp.v1.types.UserContext;

/**
 * Extends the <tt>UserContext</tt>-class with the supportedLocales.
 * Without these extension the supportedLocales can only be set global for
 * the consumerEnvironment. In that case all users have the same locale.
 * Now the supportedLocales can be set per user.<br/>
 * 
 * The order of the locales is important. If the first entry is not offered 
 * by the portlet the second will be tested and so on. The first match delivers
 * the used locale.<br/> 
 *
 * @version $Id$
 */
public class UserContextExtension extends UserContext {

    /** The locales for the user. */
    protected String[] supportedLocales;

    /** User Authentication. */
    protected String userAuthentication;

    /**
     * Default constructor
     */
    public UserContextExtension() {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param extensions
     * @param profile
     * @param userCategories
     * @param userContextKey
     */
    public UserContextExtension(
            oasis.names.tc.wsrp.v1.types.Extension[] extensions,
            oasis.names.tc.wsrp.v1.types.UserProfile profile,
            java.lang.String[] userCategories,
            java.lang.String userContextKey) {
        super(extensions, profile, userCategories, userContextKey);
    }

    /**
     * Set the supportedLocales for the current user
     * 
     * @param supportedLocales
     */
    public void setSupportedLocales(String[] supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
    
    /**
     * @return all locales the user wants to support
     */
    public String[] getSupportedLocales() {
        return this.supportedLocales;
    }

    public String getUserAuthentication() {
        return userAuthentication;
    }

    public void setUserAuthentication(String userAuthentication) {
        this.userAuthentication = userAuthentication;
    }
}
