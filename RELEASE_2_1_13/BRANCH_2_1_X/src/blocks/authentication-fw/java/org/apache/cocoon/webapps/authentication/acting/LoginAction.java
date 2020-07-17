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
package org.apache.cocoon.webapps.authentication.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.excalibur.source.SourceParameters;

/**
 *  This action logs the current user into a given handler. If the
 *  authentication is successful, a map is returned with the authentication
 *  information and a session is created (if it not already exists).
 *  If the authentication is not successful, the error information is stored
 *  into the temporary context.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version CVS $Id$
*/
public final class LoginAction extends ServiceableAction
                               implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("BEGIN act resolver=" + resolver +
                              ", objectModel=" + objectModel +
                              ", source=" + source +
                              ", par=" + par);
        }

        final String handlerName = par.getParameter("handler", null);
        if (handlerName == null) {
            throw new ProcessingException("LoginAction requires at least the handler parameter.");
        }

        // Build authentication parameters
        SourceParameters authenticationParameters = new SourceParameters();
        String[] keys = par.getNames();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                final String key = keys[i];
                if (key.startsWith("parameter_")) {
                    authenticationParameters.setParameter(key.substring("parameter_".length()),
                                                          par.getParameter(key));
                }
            }
        }

        Map map = null;

        // authenticate
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager) this.manager.lookup(AuthenticationManager.ROLE);
            UserHandler handler = authManager.login(handlerName,
                                                    par.getParameter("application", null),
                                                    authenticationParameters);
            if (handler != null) {
                // success
                map = handler.getContext().getContextInfo();
            }
        } finally {
            this.manager.release(authManager);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("END act map=" + map);
        }

        return map;
    }
}
