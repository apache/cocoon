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

import java.util.StringTokenizer;

import org.apache.cocoon.portal.profile.PortalUser;

/**
 * A role matcher matching against several role.
 * 
 * @version CVS $Id$
 */
public class MultipleRoleMatcher
implements RoleMatcher {
    
    /**
     * The character used to seperate multiple roles.
     */
    public static final String ROLE_SEPARATOR = "+";

    /**
     * The role.
     */
    private String[] roles;

    /**
     * Creates a new MultipleRoleMatcher.
     */
    public MultipleRoleMatcher(String roles) {
        StringTokenizer tokenizer = new StringTokenizer(
            roles,
            ROLE_SEPARATOR,
            false);

        this.roles = new String[tokenizer.countTokens()];

        String token;
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            this.roles[i] = token;
            i++;
        }
    }

    /** 
     * Overridden from superclass.
     * 
     * @see RoleMatcher#matches(PortalUser)
     */
    public boolean matches(PortalUser user) {
        // The user must have all roles
        int length = this.roles.length;
        for (int i = 0; i < length; i++) {
            if (!user.isUserInRole(this.roles[i])) {
                return false;
            }
        }
        return true;
    }
}