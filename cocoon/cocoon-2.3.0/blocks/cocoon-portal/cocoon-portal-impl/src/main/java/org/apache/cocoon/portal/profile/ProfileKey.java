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
package org.apache.cocoon.portal.profile;

import java.util.Collection;
import java.util.Properties;

/**
*
* @version $Id$
*/
public class ProfileKey extends Properties {

    protected String portalName;

    protected String profileName;

    protected String profileCategory;

    protected Collection userGroups;

    protected String userName;

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileCategory() {
        return profileCategory;
    }

    public void setProfileCategory(String profileCategory) {
        this.profileCategory = profileCategory;
    }

    public Collection getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Collection userGroups) {
        this.userGroups = userGroups;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    public String getProperty(String key) {
        if ("profile-name".equals(key)) {
            return (this.profileName == null ? "" : this.profileName);
        }
        if ("profile-name".equals(key)) {
            return (this.profileCategory == null ? "" : this.profileCategory);
        }
        if ("portal-name".equals(key)) {
            return (this.portalName == null ? "" : this.portalName);
        }
        if ("user-name".equals(key)) {
            return (this.userName == null ? "" : this.userName);
        }
        return super.getProperty(key);
    }

}
