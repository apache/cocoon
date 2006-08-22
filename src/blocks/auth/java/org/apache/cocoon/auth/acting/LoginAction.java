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
package org.apache.cocoon.auth.acting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.User;

/**
 * This action logs the current user into a given application. If the
 * authentication is successful, a map is returned with the authentication
 * information and a session is created (if it not already exists).
 *
 * @version $Id$
*/
public final class LoginAction
extends AbstractAuthAction {

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(final Redirector redirector,
                   final SourceResolver resolver,
                   final Map objectModel,
                   final String source,
                   final Parameters par)
    throws Exception {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN act resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }

        final String applicationName = par.getParameter("application");

        Map map = null;

        final Map loginContext = new HashMap();
        loginContext.put(ApplicationManager.LOGIN_CONTEXT_PARAMETERS_KEY, par);
        final User user =
                  this.applicationManager.login( applicationName, loginContext );

        if ( user != null) {
            // success
            map = new HashMap();
            map.put("ID", user.getId());
            Iterator i = user.getAttributeNames();
            while ( i.hasNext() ) {
                final String key = (String)i.next();
                map.put(key, user.getAttribute(key));
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map="+map);
        }

        return map;
    }

}
