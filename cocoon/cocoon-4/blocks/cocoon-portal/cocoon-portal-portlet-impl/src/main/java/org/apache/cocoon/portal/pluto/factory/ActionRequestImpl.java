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
package org.apache.cocoon.portal.pluto.factory;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.om.PortalUser;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Implementation for the action request object.
 *
 * @version $Id$
 */
public class ActionRequestImpl
    extends org.apache.pluto.core.impl.ActionRequestImpl {

    /** The portal user. */
    protected final PortalUser user;

    public ActionRequestImpl(PortletWindow window, HttpServletRequest req, PortalUser user) {
        super(window, req);
        this.user = user;
    }

    /**
     * @see org.apache.pluto.core.impl.PortletRequestImpl#getAuthType()
     */
    public String getAuthType() {
        if ( this.user.getAuthType() != null ) {
            return this.user.getAuthType();
        }
        return super.getAuthType();
    }

    /**
     * @see org.apache.pluto.core.impl.PortletRequestImpl#getRemoteUser()
     */
    public String getRemoteUser() {
        return this.user.getUserName();
    }

    /**
     * @see org.apache.pluto.core.impl.PortletRequestImpl#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        if ( this.user.getUserPrincipal() != null ) {
            return this.user.getUserPrincipal();
        }
        return super.getUserPrincipal();
    }

    /**
     * @see org.apache.pluto.core.impl.PortletRequestImpl#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        if ( this.user.isUserInRole(role) ) {
            return true;
        }
        return super.isUserInRole(role);
    }
}
