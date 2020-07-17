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
package org.apache.cocoon.auth.impl;

import java.security.Principal;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.auth.AbstractSecurityHandler;
import org.apache.cocoon.auth.StandardUser;
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

    /** The component context. */
    protected Context context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(final Context aContext) throws ContextException {
        super.contextualize(aContext);
        this.context = aContext;
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
    public User login(final Map loginContext) throws Exception {
        final Request req = ContextHelper.getRequest(this.context);
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
