/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.portal;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.user.UserEventUtil;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.profile.impl.AbstractPortalUser;
import org.apache.cocoon.auth.User;
import org.apache.cocoon.auth.impl.StandardApplication;

/**
 * This is a default implementation for a portal application.
 *
 * @version $Id$
*/
public class StandardPortalApplication
    extends StandardApplication {

    /** Attribute storing the portal user. */
    public static final String PORTAL_USER = PortalUser.class.getName();

    /** The portal service. */
    protected PortalService portalService;

    public void setPortalService(PortalService s) {
        this.portalService = s;
    }

    /**
     * @see org.apache.cocoon.auth.Application#userDidLogin(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userDidLogin(final User user, final Map context) {
        super.userDidLogin(user, context);
        final PortalUser pu = new PortalUserInfo(user);
        user.setAttribute(PORTAL_USER, pu);
        UserEventUtil.sendUserDidLoginEvent(this.portalService, pu);
    }

    /**
     * @see org.apache.cocoon.auth.Application#userWillLogout(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userWillLogout(final User user, final Map context) {
        UserEventUtil.sendUserWillLogoutEvent(this.portalService, (PortalUser)user.getAttribute(PORTAL_USER));
        super.userWillLogout(user, context);
    }

    /**
     * @see org.apache.cocoon.auth.Application#userIsAccessing(org.apache.cocoon.auth.User)
     */
    public void userIsAccessing(User user) {
        UserEventUtil.sendUserIsAccessingEvent(this.portalService, (PortalUser)user.getAttribute(PORTAL_USER));
        super.userIsAccessing(user);
    }

    /**
     * The user info for the portal engine.
     */
    public static final class PortalUserInfo extends AbstractPortalUser {

        /** The cauth user object. */
        protected final User user;

        /**
         * Create a new user info object.
         * @param aUser      The cauth user object.
         */
        public PortalUserInfo(final User aUser) {
            this.user = aUser;
            this.setAnonymous(this.user.getId().equals("anonymous"));
            // build a map for the user info
            final Map userInfos = new HashMap();
            final Iterator i = this.user.getAttributeNames();
            while ( i.hasNext() ) {
                final String key = (String)i.next();
                final Object value = this.user.getAttribute(key);
                userInfos.put(key, value);
            }
            if ( userInfos.size() > 0 ) {
                this.setUserInfos(userInfos);
            }
        }

        /**
         * @see org.apache.cocoon.portal.om.PortalUser#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(final String role) {
            return user.isUserInRole(role);
        }

        /**
         * @see org.apache.cocoon.portal.om.PortalUser#getGroups()
         */
        public Collection getGroups() {
            final Collection c = (Collection) this.user.getAttribute("groups");
            if ( c == null ) {
                return Collections.EMPTY_LIST;
            }
            return c;
        }

        /**
         * @see org.apache.cocoon.portal.om.PortalUser#getUserName()
         */
        public String getUserName() {
            return this.user.getId();
        }

        /**
         * @see org.apache.cocoon.portal.profile.impl.AbstractPortalUser#getAuthType()
         */
        public String getAuthType() {
            return "cauth";
        }

        /**
         * @see org.apache.cocoon.portal.profile.impl.AbstractPortalUser#getUserPrincipal()
         */
        public Principal getUserPrincipal() {
            return (Principal) this.user.getAttribute(User.ATTRIBUTE_PRINCIPAL);
        }
    }
}
