/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.webapps.authentication;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.excalibur.source.SourceParameters;



/**
 * This is the authentication manager.
 * It is used to authenticate (login, logout) a user. Usually, this
 * component should not be used from custom code. The provided
 * actions perform all required tasks.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AuthenticationManager.java,v 1.7 2004/03/05 13:01:40 bdelacretaz Exp $
*/
public interface AuthenticationManager {

    /** The Avalon Role */
    String ROLE = AuthenticationManager.class.getName();

    /**
     * Is the current user authenticated for the given handler?
     * @return Returns the corresponding handler if the user is authenticated.
     */
    UserHandler isAuthenticated(String handlerName)
    throws ProcessingException;

    /**
     * Is the current user authenticated for the given handler?
     * If the user is already authenticated, the {@link RequestState}
     * is updated to the provided information (handler and application).
     */
    boolean checkAuthentication(Redirector redirector,
                                 String     handlerName,
                                 String     applicationName)
    throws ProcessingException, IOException;

    /**
     * Try to login the user.
     * If the authentication is successful, the user handler is returned.
     * If not, <code>null</code> is returned.
     */
    UserHandler login(String              handlerName,
                      String              applicationName,
                      SourceParameters    parameters)
    throws ProcessingException;

    /**
     * Perform a logout of the user.
     */
    void logout(String handlerName,
                 int mode)
    throws ProcessingException;
    
    /**
     * Get the current state of authentication
     */
    RequestState getState();

    /**
     * Create Application Context.
     * This context is destroyed when the user logs out of the handler
     */
    SessionContext createApplicationContext(String name,
                                            String loadURI,
                                            String saveURI)
    throws ProcessingException;
}
