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
package org.apache.cocoon.auth.portal;

import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.profile.impl.UserInfo;
import org.apache.cocoon.portal.profile.impl.UserInfoProvider;
import org.apache.cocoon.auth.ApplicationUtil;
import org.apache.cocoon.auth.User;

/**
 * Get the information about the current user.
 * This implementation uses CAuth.
 * Note: This class belongs to cauth but has to be defined in the portal block for now.
 *       This will be cleaned up with Cocoon 2.2.
 * @version $Id$
 */
public class UserInfoProviderImpl
implements UserInfoProvider, Contextualizable {

    /** The component context. */
    protected Context context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(final Context aContext) throws ContextException {
        this.context = aContext;
    }

    /**
     * @see org.apache.cocoon.portal.profile.impl.UserInfoProvider#getUserInfo(java.lang.String, java.lang.String)
     */
    public UserInfo getUserInfo(final String portalName, final String layoutKey)
    throws Exception {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        final User user = ApplicationUtil.getUser(objectModel);

        final UserInfo info = new PortalUserInfo(portalName, layoutKey, user);

        info.setUserName(user.getId());
        info.setGroup((String)user.getAttribute("group"));
        final PortalApplication app =
                            (PortalApplication)ApplicationUtil.getApplication(objectModel);
        info.setConfigurations(app.getPortalConfiguration());

        return info;
    }

    /**
     * The user info for the portal engine.
     */
    public static final class PortalUserInfo extends UserInfo {

        /** The CAuth user object. */
        protected final User user;

        /**
         * Create a new user info object.
         * @param portalName The current portal name.
         * @param layoutKey  The layout information.
         * @param aUser      The CAuth user object.
         */
        public PortalUserInfo(final String portalName, final String layoutKey, final User aUser) {
            super(portalName, layoutKey);
            this.user = aUser;
        }

        /**
         * @see org.apache.cocoon.portal.profile.PortalUser#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(final String role) {
            return user.isUserInRole(role);
        }
    }
}
