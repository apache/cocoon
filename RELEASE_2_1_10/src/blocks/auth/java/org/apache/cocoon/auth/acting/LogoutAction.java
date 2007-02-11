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
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.auth.ApplicationManager;

/**
 * This action logs the current user out of a given application.
 *
 * @version $Id$
*/
public final class LogoutAction
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

        final String modeString = par.getParameter("mode", "terminate");
        final String mode;
        if ( modeString.equals("terminate") ) {
            mode = ApplicationManager.LOGOUT_MODE_TERMINATE_SESSION_IF_UNUSED;
        } else if ( modeString.equalsIgnoreCase("keep") ) {
            mode = ApplicationManager.LOGOUT_MODE_KEEP_SESSION;
        } else {
           throw new ProcessingException("Unknown mode " + modeString);
        }

        final Map logoutContext = new HashMap();
        logoutContext.put(ApplicationManager.LOGOUT_CONTEXT_PARAMETERS_KEY, par);
        logoutContext.put(ApplicationManager.LOGOUT_CONTEXT_MODE_KEY, mode);

        this.applicationManager.logout(applicationName, logoutContext);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map={}");
        }

        return EMPTY_MAP;
    }

}
