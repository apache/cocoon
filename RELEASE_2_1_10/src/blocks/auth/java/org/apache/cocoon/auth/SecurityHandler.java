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
 * The Security Handler
 * A security handler is the connection between the web application and the
 * backend system managing the users.
 * A handler offers two main methods: one for login and one for logout. The
 * login method tries to authenticate the current user and returns a
 * {@link User} object on success.
 *
 * A {@link SecurityHandler} must be implemented in a thread safe manner.
 *
 * @version $Id$
*/
public interface SecurityHandler {

    /**
     * Try to authenticate the user.
     * @param context The context for the login operation.
     * @return The user if the authentication is successful, null otherwise.
     * @throws Exception If something goes wrong.
     */
    User login(Map context)
    throws Exception;

    /**
     * This notifies the security-handler that a user logs out.
     * @param context The context for the login operation.
     * @param user    The user object.
     */
    void logout(Map context, User user);

    /**
     * Return a unique identifier for this security handler.
     * For session replication to work, a security handler must deliver
     * the same identifier across systems!
     * @return A unique identifier.
     */
    String getId();
}
