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
package org.apache.cocoon.portal.profile.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;

/**
 * Get the information about the current user.
 * This implementation uses the authentication-fw block
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: MapProfileLS.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class AuthenticationFWUserInfoProvider 
implements UserInfoProvider, Serviceable {
    
    protected ServiceManager manager;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.impl.UserInfoProvider#getUserInfo(java.lang.String, java.lang.String)
     */
    public UserInfo getUserInfo(String portalName, String layoutKey) 
    throws Exception {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            final RequestState state = authManager.getState();
            final UserHandler handler = state.getHandler();

            final UserInfo info = new AFWUserInfo(portalName, layoutKey, handler);


            info.setUserName(handler.getUserId());
            try {
                info.setGroup((String)handler.getContext().getContextInfo().get("group"));
            } catch (ProcessingException pe) {
                // ignore this
            }

            final ApplicationConfiguration ac = state.getApplicationConfiguration();        
            if ( ac == null ) {
                throw new ProcessingException("Configuration for portal not found in application configuration.");
            }
            final Configuration appConf = ac.getConfiguration("portal");
            if ( appConf == null ) {
                throw new ProcessingException("Configuration for portal not found in application configuration.");
            }
            final Configuration config = appConf.getChild("profiles");
            final Configuration[] children = config.getChildren();
            final Map configs = new HashMap();
            if ( children != null ) {
                for(int i=0; i < children.length; i++) {
                    configs.put(children[i].getName(), children[i].getAttribute("uri"));
                }
            }
            info.setConfigurations(configs);
            return info;    
        } finally {
            this.manager.release( authManager );
        }
    }
    
    public static final class AFWUserInfo extends UserInfo {
        
        protected final UserHandler handler;
        /**
         * @param portalName
         * @param layoutKey
         */
        public AFWUserInfo(String portalName, String layoutKey, UserHandler handler) {
            super(portalName, layoutKey);
            this.handler = handler;
        }
    
        /* (non-Javadoc)
         * @see org.apache.cocoon.portal.profile.PortalUser#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(String role) {
            return this.isUserInRole(role);
        }
    }
}
