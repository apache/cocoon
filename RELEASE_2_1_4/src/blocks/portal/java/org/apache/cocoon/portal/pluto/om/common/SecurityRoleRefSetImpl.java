/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: SecurityRoleRefSetImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
 */
public class SecurityRoleRefSetImpl extends HashSet implements SecurityRoleRefSet, SecurityRoleRefSetCtrl, java.io.Serializable {

    public SecurityRoleRefSetImpl()
    {
    }

    // SecurityRoleRefSet implementation.

    public SecurityRoleRef get(String roleName)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRoleRef securityRoleRef = (SecurityRoleRef)iterator.next();
            if (securityRoleRef.getRoleName().equals(roleName)) {
                return securityRoleRef;
            }
        }
        return null;
    }

    // SecurityRoleRefSetCtrl implementation.

    public SecurityRoleRef add(SecurityRoleRef securityRoleRef)
    {
        SecurityRoleRefImpl newSecurityRoleRef = new SecurityRoleRefImpl();
        newSecurityRoleRef.setRoleName(securityRoleRef.getRoleName());
        newSecurityRoleRef.setRoleLink(securityRoleRef.getRoleLink());
        newSecurityRoleRef.setDescriptionSet(((SecurityRoleRefImpl)securityRoleRef).getDescriptionSet());

        super.add(newSecurityRoleRef);

        return newSecurityRoleRef;
    }

    public SecurityRoleRef remove(String roleName)
    {
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

    public void remove(SecurityRoleRef securityRoleRef)
    {
        super.remove(securityRoleRef);
    }

    // additional methods.
    
    public SecurityRoleRef add(String roleName, String roleLink, DescriptionSet descriptions)
    {
        SecurityRoleRefImpl securityRoleRef = new SecurityRoleRefImpl();
        securityRoleRef.setRoleName(roleName);
        securityRoleRef.setRoleLink(roleLink);
        securityRoleRef.setDescriptionSet(descriptions);

        super.add(securityRoleRef);

        return securityRoleRef;
    }

    // unmodifiable part

    public static class Unmodifiable extends UnmodifiableSet
            implements SecurityRoleRefSet {

        public Unmodifiable(SecurityRoleRefSet c)
        {
            super(c);
        }

        // additional methods.

        public SecurityRoleRef get(String roleName)
        {
            return((SecurityRoleRefSet)c).get(roleName);
        }

    }

}
