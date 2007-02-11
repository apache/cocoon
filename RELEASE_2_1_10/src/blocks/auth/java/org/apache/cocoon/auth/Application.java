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
package org.apache.cocoon.auth;

import java.util.Map;


/**
 * This class describes the current application. Inside Cocoon, you can have
 * different applications running at the same time (a portal, a shop, a
 * registration wizard etc.) Each of these applications might require its
 * own login or might have different settings. The application object helps in
 * managing these things.
 *
 * An application provides access to the corresponding {@link SecurityHandler}
 * and {@link ApplicationStore}. It can also store configuration values as
 * attributes.
 *
 * In addition, the application is notified about user actions (login, logout
 * and usage).
 *
 * @version $Id$
*/
public interface Application {

    /**
     * Return the security handler for this application.
     * @return The security handler
     */
    SecurityHandler getSecurityHandler();

    /**
     * Return the application store for loading/saving user specific data.
     * @return Return the application store or null.
     */
    ApplicationStore getApplicationStore();

    /**
     * Notify the application about a successful login of a user.
     * @param user The current user.
     * @param context The context for the login operation.
     */
    void userDidLogin(User user, Map context);

    /**
     * Notify the application about a logout of a user.
     * @param user The current user.
     * @param context The context for the logout operation.
     */
    void userWillLogout(User user, Map context);

    /**
     * Notify the application about a user using the application
     * in the current request. This method might be called more than
     * once during one request, so the application should check this.
     * This hook can for example be used by the application to
     * prepare the current object model of the request.
     * @param user The current user.
     */
    void userIsAccessing(User user);

    /**
     * Set an application attribute.
     * @param key   The key of the attribute.
     * @param value The value of the attribute.
     */
    void setAttribute(String key, Object value);

    /**
     * Remove an application attribute.
     * @param key The key of the attribute.
     */
    void removeAttribute(String key);

    /**
     * Get the value of an application attribute.
     * @param key The key of the attribute.
     * @return The value of the attribute or null.
     */
    Object getAttribute(String key);
}
