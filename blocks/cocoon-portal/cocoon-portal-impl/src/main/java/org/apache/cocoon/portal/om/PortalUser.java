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
package org.apache.cocoon.portal.om;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

/**
 * Information about the current portal user.
 * This object decouples the portal from the used authentication method.
 *
 * @version $Id$
 */
public interface PortalUser {

    /**
     * Is this user an anonymous user?
     */
    boolean isAnonymous();

    /**
     * @return Return all groups.
     */
    Collection getGroups();

    /**
     * @return Returns the userName.
     */
    String getUserName();

    /**
     * Tests if the user has a given role.
     * @param role The role to test.
     * @return True if the user has the role.
     */
    boolean isUserInRole(String role);

    /**
     * Get a user information for an application specific key.
     * @param key The name of the information.
     * @return The value or null.
     */
    Object getUserInfo(String key);

    /**
     * Get all application specific information about an user.
     * This method never returns null but the map might be empty.
     * @return A map with key-value-pairs.
     */
    Map getUserInfos();

    /**
     * Return the user prinicpal.
     * If the used authentication mechanism provides a user prinicpal
     * it should be returned - if not, null is returned.
     * @return The user principal or null.
     */
    Principal getUserPrincipal();

    /**
     * Return information about the authentication mechanism
     * @return A string specifying the mechanism or null.
     */
    String getAuthType();

    /**
     * Return the default profile name for this user.
     * @return The default profile name or null if the portal wide default should be used.
     */
    String getDefaultProfileName();
}
