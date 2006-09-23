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
package org.apache.cocoon.components.source.helpers;

/**
 * This class represents a source permission for users
 *
 * @version $Id$
 */
public class PrincipalSourcePermission extends AbstractSourcePermission {

    public final static String PRINCIPAL_SELF              = "SELF";
    public final static String PRINCIPAL_ALL               = "ALL";
    public final static String PRINCIPAL_GUEST             = "GUEST";

    private String principal;

    /**
     * Creates a new permission
     *
     * @param principal Principal of the permission
     * @param privilege Privilege of the permission
     * @param inheritable If the permission is inheritable
     * @param negative If the permission is negative
     */
    public PrincipalSourcePermission(String principal, String privilege, 
                                     boolean inheritable, boolean negative) {

        this.principal   = principal;
        setPrivilege(privilege);
        setInheritable(inheritable);
        setNegative(negative);
    }

    /**
     * Sets the principal of the permission
     *
     * @param principal Principal of the permission
     */
    public void setPrincipal(String principal) {
        this.principal   = principal;
    }

    /**
     * Returns the principal of the permission
     * 
     * @return Principal of the permission
     */
    public String getPrincipal() {
        return this.principal;
    }
}
