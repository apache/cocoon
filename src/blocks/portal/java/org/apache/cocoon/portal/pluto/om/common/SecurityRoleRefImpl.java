/*
 * Copyright 2004,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.SecurityRoleRef;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: SecurityRoleRefImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class SecurityRoleRefImpl implements SecurityRoleRef, java.io.Serializable
{

    private String roleName;
    private String roleLink;
    private DescriptionSet descriptions;

    public SecurityRoleRefImpl()
    {
        descriptions = new DescriptionSetImpl();
    }

    // SecurityRoleRef implementation.

    public String getRoleName()
    {
        return roleName;
    }

    public String getRoleLink()
    {
        return roleLink;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.SecurityRoleRef#getDescription(Locale)
     */
    public Description getDescription(Locale locale)
    {
        return descriptions.get(locale);
    }


    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    // additional methods.

    public void setRoleLink(String roleLink)
    {
        this.roleLink = roleLink;
    }

    public DescriptionSet getDescriptionSet()
    {
        return descriptions;
    }

    public void setDescriptionSet(DescriptionSet descriptions)
    {
        this.descriptions = descriptions;
    }

    public Collection getCastorDescriptions()
    {
        return(DescriptionSetImpl)descriptions;
    }

    public void setCastorDescriptions(DescriptionSet castorDescriptions)
    {
        this.descriptions = castorDescriptions;
    }


}
