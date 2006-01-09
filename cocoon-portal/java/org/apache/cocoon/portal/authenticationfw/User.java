/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.authenticationfw;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.impl.AbstractPortalUser;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;

/**
 * The User object used by the authentication-fw implementation.
 *
 * @version $Id$
 */
public class User extends AbstractPortalUser { 

    protected final UserHandler handler;

    /**
     * @param handler
     */
    public User(UserHandler handler) {
        this.handler = handler;
    }

    /**
     * @see org.apache.cocoon.portal.profile.PortalUser#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        return this.handler.isUserInRole(role);
    }

    public static PortalUser getPortalUser(ServiceManager manager, String portalName) {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)manager.lookup(AuthenticationManager.ROLE);
            final RequestState state = authManager.getState();
            final UserHandler handler = state.getHandler();

            final AbstractPortalUser info = new User(handler);

            info.setUserName(handler.getUserId());
            try {
                info.setGroup((String)handler.getContext().getContextInfo().get("group"));
            } catch (ProcessingException pe) {
                // ignore this
            }

            return info;
        } catch (ServiceException se) {
            throw new PortalRuntimeException("Unable to lookup authentication manager.", se);
        } finally {
            manager.release( authManager );
        }
    }
}
