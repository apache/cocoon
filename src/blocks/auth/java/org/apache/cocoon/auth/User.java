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

import java.util.Iterator;

/**
 * This object represents the current user. Each user must have a unique
 * identifier (per {@link org.apache.cocoon.auth.SecurityHandler}).
 * For session replication, the implementation should be {@link java.io.Serializable}.
 *
 * @version $Id$
*/
public interface User {

    /**
     * Return the unique id of this user.
     * @return The identifier.
     */
    String getId();

    /**
     * Set an information about the user.
     * For session replication the value of the attribute should
     * be {@link java.io.Serializable}.
     * @param key   The key identifying the information.
     * @param value The value of the information.
     */
    void setAttribute(String key, Object value);

    /**
     * Remove an information about the user.
     * @param key The key identifying the information.
     */
    void removeAttribute(String key);

    /**
     * Get information about the user.
     * @param key The key identifying the information.
     * @return The value or null.
     */
    Object getAttribute(String key);

    /**
     * Return all available names.
     * @return An Iterator for the names (Strings).
     */
    Iterator getAttributeNames();

    /**
     * Check if the user is in a given role.
     * This method can't check for a role handled by the servlet engine,
     * it only handles indendently specified roles.
     * Therefore, it is advisable to not call this method directly, but
     * use the provided methods from the {@link ApplicationUtil} instead.
     *
     * @param role The role to test.
     * @return Returns true if the user has the role, otherwise false.
     */
    boolean isUserInRole(String role);
}
