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
package org.apache.cocoon.portal.tools.helper;

import org.apache.cocoon.portal.profile.PortalUser;

/**
 * A role matcher matching against a single role.
 * 
 * @version CVS $Id$
 */
public class SingleRoleMatcher
implements RoleMatcher {
    
    /**
     * The role.
     */
    private final String role;

    /**
     * Creates a new SingleRoleMatcher.
     */
    public SingleRoleMatcher(String role) {
        this.role = role;
    }

    /** 
     * Overridden from superclass.
     * 
     * @see RoleMatcher#matches(PortalUser)
     */
    public boolean matches(PortalUser user) {
        return user.isUserInRole(this.role);
    }
}