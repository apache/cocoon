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
package org.apache.cocoon.portal.services;

import java.util.Collection;

import org.apache.cocoon.portal.om.PortalUser;


/**
 * The user service provides access to the portal user object (which is a wrapper around the
 * portal application specific user object).
 * The user service can store user specific attributes which have the lifetime of the user
 * session or the current request (temporary attributes).
 *
 * @version $Id$
 */
public interface UserService {

    /**
     * Get current user object.
     */
    PortalUser getUser();

    /**
     * Return the value of an attribute.
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getAttribute(String key);

    /**
     * Set an attribute.
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setAttribute(String key, Object value);

    /**
     * Remove an attribute.
     * @param key The key of the attribute
     */
    Object removeAttribute(String key);

    /**
     * Return the names of all attributes.
     */
    Collection getAttributeNames();

    /**
     * Return the value of a temporary attribute.
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getTemporaryAttribute(String key);

    /**
     * Set a temporary attribute.
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setTemporaryAttribute(String key, Object value);

    /**
     * Remove a temporary attribute.
     * @param key The key of the attribute
     */
    Object removeTemporaryAttribute(String key);

    /**
     * Return the names of all temporary attributes.
     */
    Collection getTemporaryAttributeNames();

    /**
     * Return the default profile name for the current user.
     */
    String getDefaultProfileName();
}