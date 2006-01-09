/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.apache.cocoon.portal.profile.PortalUser;

/**
 * Information about the current user.
 * This data object is used for loading the profile. It decouples the
 * portal from the used authentication method.
 *
 * @version $Id: MapProfileLS.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class AbstractPortalUser
    implements PortalUser, Serializable {

    /** The unique name of the user. */
    protected String userName;

    protected String group;

    protected Map userInfo = Collections.EMPTY_MAP;

    /** Is this an anonymous user. */
    protected boolean anonymous;

    public AbstractPortalUser() {
        // nothing to do
    }

    /**
     * @see org.apache.cocoon.portal.profile.PortalUser#isAnonymous()
     */
    public boolean isAnonymous() {
        return this.anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
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
     * @see org.apache.cocoon.portal.profile.PortalUser#getUserInfo(java.lang.String)
     */
    public Object getUserInfo(String key) {
        return this.userInfo.get(key);
    }

    /**
     * @see org.apache.cocoon.portal.profile.PortalUser#getUserInfos()
     */
    public Map getUserInfos() {
        return this.userInfo;
    }

    public void setUserInfos(Map infos) {
        if ( infos == null ) {
            this.userInfo = Collections.EMPTY_MAP;
        } else {
            this.userInfo = Collections.unmodifiableMap(infos);
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.PortalUser#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.profile.PortalUser#getAuthType()
     */
    public String getAuthType() {
        return null;
    }
}
