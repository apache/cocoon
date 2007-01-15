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
package org.apache.cocoon.auth.impl;

import java.security.Principal;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.auth.AuthenticationException;
import org.apache.cocoon.auth.User;

/**
 * Verify if a user can be authenticated.
 * This is a very simple authenticator that checks if the user is authenticated
 * using the servlet authentication mechanisms.
 *
 * @version $Id$
*/
public class ServletSecurityHandler
    extends AbstractSecurityHandler {

    /** The process info provider. */
    protected ProcessInfoProvider processInfoProvider;

    public void setProcessInfoProvider(ProcessInfoProvider p) {
        this.processInfoProvider = p;
    }

    /**
     * Create a new user.
     * @param req The current request.
     * @return A new user object.
     */
    protected User createUser(final Request req) {
        final User user = new ServletUser(req);
        return user;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#login(java.util.Map)
     */
    public User login(final Map loginContext)
    throws AuthenticationException {
        final Request req = ObjectModelHelper.getRequest(this.processInfoProvider.getObjectModel());
        User user = null;
        if ( req.getRemoteUser() != null ) {
            user = this.createUser( req );
        }
        return user;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#logout(java.util.Map, org.apache.cocoon.auth.User)
     */
    public void logout(final Map logoutContext, final User user) {
        // TODO what can we do here?
    }

    /**
     * Inner class for the current user. This class provides access to some
     * servlet specific information.
     */
    public static class ServletUser extends StandardUser {

        /** The principal belonging to the user. */
        protected final Principal principal;

        /**
         * Instantiate a new user.
         * @param req      The current request.
         */
        public ServletUser(final Request req) {
            super(req.getRemoteUser());
            this.principal = req.getUserPrincipal();
            this.setAttribute(User.ATTRIBUTE_PRINCIPAL, this.principal);
        }

        /**
         * Return the current principal.
         * @return The principal.
         */
        public Principal getPrincipal() {
            return this.principal;
        }
    }
}
