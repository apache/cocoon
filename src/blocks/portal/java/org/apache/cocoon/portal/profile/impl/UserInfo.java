/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.cocoon.portal.profile.PortalUser;

/**
 * Information about the current user.
 * This data object is used for loading the profile. It decouples the
 * portal from the used authentication method.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: MapProfileLS.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class UserInfo implements PortalUser, Serializable {

    protected String userName;

    protected String group;

    protected String portalName;

    protected String layoutKey;

    protected Map    configurations;

    public UserInfo(String portalName, String layoutKey) {
        this.portalName = portalName;
        this.layoutKey = layoutKey;
    }

    /**
     * @return Returns the group.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group The group to set.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Returns the configurations.
     */
    public Map getConfigurations() {
        return configurations;
    }

    /**
     * @param configurations The configurations to set.
     */
    public void setConfigurations(Map configurations) {
        this.configurations = configurations;
    }

    /**
     * @return Returns the layoutKey.
     */
    public String getLayoutKey() {
        return layoutKey;
    }

    /**
     * @return Returns the portalName.
     */
    public String getPortalName() {
        return portalName;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.PortalUser#isUserInRole(java.lang.String)
     */
    public abstract boolean isUserInRole(String role);
}
