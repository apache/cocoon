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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.pluto.om.common.SecurityRole;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.util.StringUtils;

/**
 *
 * @version $Id$
 */
public class SecurityRoleSetImpl extends HashSet implements SecurityRoleSet, java.io.Serializable {

    public SecurityRoleSetImpl() {
        // nothing to do 
    }

    // SecurityRoleSet implementation.

    public SecurityRole get(String roleName) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRole securityRole = (SecurityRole)iterator.next();
            if (securityRole.getRoleName().equals(roleName)) {
                return securityRole;
            }
        }
        return null;
    }

    // additional methods.

    public SecurityRole add(SecurityRole securityRole) {
        SecurityRoleImpl newSecurityRole = new SecurityRoleImpl();
        newSecurityRole.setRoleName(securityRole.getRoleName());
        newSecurityRole.setDescription(securityRole.getDescription());

        super.add(newSecurityRole);

        return newSecurityRole;
    }

    public SecurityRole add(String roleName, String description) {
        SecurityRoleImpl securityRole = new SecurityRoleImpl();
        securityRole.setRoleName(roleName);
        securityRole.setDescription(description);

        super.add(securityRole);

        return securityRole;
    }

    public void remove(SecurityRole securityRole) {
        super.remove(securityRole);
    }

    public SecurityRole remove(String roleName) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRole securityRole = (SecurityRole)iterator.next();
            if (securityRole.getRoleName().equals(roleName)) {
                super.remove(securityRole);
                return securityRole;
            }
        }
        return null;
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": ");
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buffer.append(((SecurityRoleImpl)iterator.next()).toString(indent+2));
        }
        return buffer.toString();
    }
}
