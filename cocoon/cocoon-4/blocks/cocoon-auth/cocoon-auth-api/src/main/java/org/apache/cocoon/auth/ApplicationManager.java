/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth;

import java.util.Map;

/**
 * This is the central component of Cocoon Authentication. It controls all applications
 * defined in the current Cocoon instance and provides ways to authenticate
 * a user.
 *
 * @version $Id$
*/
public interface ApplicationManager {

    /** The string used to store the user in the object model and the prefix
     * for the session. */
    String USER = "cauth-user";

    /** The string used to store the application in the object model. */
    String APPLICATION = "cauth-application";

    /** The string used to store the application data in the object model .*/
    String APPLICATION_DATA = "cauth-application-data";

    /** The key for the user name in the login context. */
    String LOGIN_CONTEXT_USERNAME_KEY = "name";

    /** The key for the user password in the login context. */
    String LOGIN_CONTEXT_PASSWORD_KEY = "password";

    /** The key for the logout method in the logout context. */
    String LOGOUT_CONTEXT_MODE_KEY = "mode";

    /** logout mode: terminate session if the user is not logged into
     * any application anymore (default). */
    String LOGOUT_MODE_TERMINATE_SESSION_IF_UNUSED = "0";

    /** logout mode: don't terminate the session. */
    String LOGOUT_MODE_KEEP_SESSION = "1";

    /**
     * Test, if the current user is already logged into the application.
     * @param appName The name of the application.
     * @return Returns true if the user is already logged in, false otherwise.
     */
    boolean isLoggedIn(String appName);

    /**
     * Log the user in to the application. If the user is already logged in
     * then the corresponding user object is returned.
     * If the login process is started, the login context is passed to
     * {@link SecurityHandler#login(Map)} and on successful login to
     * {@link Application#userDidLogin(User, Map)}.
     * @param appName The name of the application.
     * @param loginContext The context for the login operation.
     * @return The user object on a successful login, null otherwise.
     * @throws Exception If anything goes wrong.
     */
    User login(String appName, Map loginContext) throws Exception;

    /**
     * Logout the user from an application.
     * During the logout process, the logout context is passed to
     * {@link Application#userWillLogout(User, Map)},
     * and eventually to {@link SecurityHandler#logout(Map, User)}.
     * @param appName The name of the application.
     * @param logoutContext The context for the logout operation.
     */
    void logout(String appName, Map logoutContext);
}
