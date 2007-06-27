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
package org.apache.cocoon.auth.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cocoon.auth.User;

/**
 * This object represents the current user. Each user must have a unique
 * identifier (per {@link org.apache.cocoon.auth.SecurityHandler}).
 *
 * @version $Id$
*/
public class StandardUser
implements User, Serializable {

    /** The unique id of the user. */
    protected String id;

    /** The user attributes. */
    protected final Map attributes = new HashMap();

    /** Cache the roles info. */
    protected List roles;

    /**
     * Create a new user object.
     * @param userId The unique identifier for this user.
     */
    public StandardUser(final String userId) {
        this.id = userId;
    }

    /**
     * Create a new user object.
     * If you use this constructor, you have to ensure that the id of the user
     * is set accordingly before the user object is used
     */
    public StandardUser() {
        // nothing to do here, we have to ensure that the id is set!
    }

    /**
     * @see org.apache.cocoon.auth.User#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see org.apache.cocoon.auth.User#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(final String key, final Object value) {
        this.attributes.put(key, value);
    }

    /**
     * @see org.apache.cocoon.auth.User#removeAttribute(java.lang.String)
     */
    public void removeAttribute(final String key) {
        this.attributes.remove(key);
    }

    /**
     * @see org.apache.cocoon.auth.User#getAttribute(java.lang.String)
     */
    public Object getAttribute(final String key) {
        return this.attributes.get(key);
    }

    /**
     * @see org.apache.cocoon.auth.User#getAttributeNames()
     */
    public Iterator getAttributeNames() {
        return this.attributes.keySet().iterator();
    }

    /**
     * Check if the user is in a given role. This default implementation
     * checks the two attributes "roles" and "role". If the incomming role
     * is found in one of the two attributes, true is returned.
     * Subclasses should override this method.
     *
     * @param role The role to test.
     * @return Returns true if the user has the role, otherwise false.
     * @see org.apache.cocoon.auth.User#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(final String role) {
        if ( this.roles == null ) {
            this.roles = new ArrayList();
            final Object allRoles = this.getAttribute("roles");
            if ( allRoles != null && allRoles instanceof String ) {
                final StringTokenizer st = new StringTokenizer( (String)allRoles, ",");
                while ( st.hasMoreElements() ) {
                    this.roles.add(st.nextElement());
                }
            }
            final Object singleRole = this.getAttribute("role");
            if ( singleRole != null && singleRole instanceof String ) {
                this.roles.add(singleRole);
            }
        }
        return this.roles.contains( role );
    }
}
