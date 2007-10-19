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
package org.apache.cocoon.webapps.authentication.context;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;


/**
 *  Context provider for the authentication context
 *
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
 */
public final class AuthenticationContextProvider extends AbstractLogEnabled
                                                 implements SessionContextProvider, ThreadSafe, Serviceable {

    protected ServiceManager manager;
    
    /**
     * Get the context
     * @param name The name of the context
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    public SessionContext getSessionContext(String name)
    throws ProcessingException {
        AuthenticationContext context = null;
        if (name.equals(AuthenticationConstants.SESSION_CONTEXT_NAME) ) {
            
            AuthenticationManager authManager = null;
            RequestState state = null;
            try {
                authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
                state = authManager.getState();
            } catch (ServiceException ignore) {
            } finally {
                this.manager.release( authManager );
            }
            
            if ( null != state ) {
                UserHandler handler = state.getHandler();
                if ( handler != null ) {
                    context = handler.getContext();
                }
            }
        }
        return context;
    }

    /**
     * Does the context exist?
     */
    public boolean existsSessionContext(String name)
    throws ProcessingException {
        return (this.getSessionContext( name ) != null);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

}
