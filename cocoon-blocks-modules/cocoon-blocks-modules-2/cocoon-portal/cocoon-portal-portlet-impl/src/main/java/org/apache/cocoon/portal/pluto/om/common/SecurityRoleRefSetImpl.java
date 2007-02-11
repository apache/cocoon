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

import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.common.SecurityRoleRefSetCtrl;

/**
 *
 * @version $Id$
 */
public class SecurityRoleRefSetImpl
    extends HashSet
    implements SecurityRoleRefSet, SecurityRoleRefSetCtrl, java.io.Serializable {

    public SecurityRoleRefSetImpl() {
        // nothing to do 
    }

    /**
     * @see org.apache.pluto.om.common.SecurityRoleRefSet#get(java.lang.String)
     */
    public SecurityRoleRef get(String roleName) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRoleRef securityRoleRef = (SecurityRoleRef)iterator.next();
            if (securityRoleRef.getRoleName().equals(roleName)) {
                return securityRoleRef;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.common.SecurityRoleRefSetCtrl#add(org.apache.pluto.om.common.SecurityRoleRef)
     */
    public SecurityRoleRef add(SecurityRoleRef securityRoleRef) {
        SecurityRoleRefImpl newSecurityRoleRef = new SecurityRoleRefImpl();
        newSecurityRoleRef.setRoleName(securityRoleRef.getRoleName());
        newSecurityRoleRef.setRoleLink(securityRoleRef.getRoleLink());
        newSecurityRoleRef.setDescriptionSet(((SecurityRoleRefImpl)securityRoleRef).getDescriptionSet());

        super.add(newSecurityRoleRef);

        return newSecurityRoleRef;
    }

    /**
     * @see org.apache.pluto.om.common.SecurityRoleRefSetCtrl#remove(java.lang.String)
     */
    public SecurityRoleRef remove(String roleName) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRoleRef securityRoleRef = (SecurityRoleRef)iterator.next();
            if (securityRoleRef.getRoleName().equals(roleName)) {
                super.remove(securityRoleRef);
                return securityRoleRef;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.common.SecurityRoleRefSetCtrl#remove(org.apache.pluto.om.common.SecurityRoleRef)
     */
    public void remove(SecurityRoleRef securityRoleRef) {
        super.remove(securityRoleRef);
    }

    // additional methods.

    public SecurityRoleRef add(String roleName, String roleLink, DescriptionSet descriptions) {
        SecurityRoleRefImpl securityRoleRef = new SecurityRoleRefImpl();
        securityRoleRef.setRoleName(roleName);
        securityRoleRef.setRoleLink(roleLink);
        securityRoleRef.setDescriptionSet(descriptions);

        super.add(securityRoleRef);

        return securityRoleRef;
    }
}
