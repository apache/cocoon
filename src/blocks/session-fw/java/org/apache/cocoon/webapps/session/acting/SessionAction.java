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
package org.apache.cocoon.webapps.session.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.SessionManager;

/**
 * This action creates and terminates a session.
 * The action is controlled via parameters. The action parameter defines
 * the action (creating or terminating).
 * The value "create" creates a new session (if not already available)
 * The value "terminate" terminates the session. The termination can be controlled
 * with a second parameter "mode": The default value "immediately" terminates
 * the session, the value "if-unused" terminates the session only if no
 * session context is available anymore. This means the user must not have
 * any own session context and must not be authenticated anymore using
 * the uthentication framework.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionAction.java,v 1.5 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public final class SessionAction
extends ServiceableAction
implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws ProcessingException {
        SessionManager sessionManager = null;
        try {
            sessionManager = (SessionManager)this.manager.lookup(SessionManager.ROLE);
            final String action = par.getParameter("action", "create");
          
            if ( action.equals("create") ) {
                
                // create a session
                sessionManager.createSession();
                
            } else if ( action.equals("terminate") ) {
                
                // terminate a session
                final String mode = par.getParameter("mode", "immediately");
                if ( mode.equals("immediately") ) {
                    sessionManager.terminateSession(true);
                } else if ( mode.equals("if-unused")  ) {
                    sessionManager.terminateSession(false);
                } else {
                    throw new ProcessingException("Unknown mode " + mode + " for action " + action);
                }
                
            } else {
                throw new ProcessingException("Unknown action: " + action);
            }
        } catch (ServiceException ce) {
            throw new ProcessingException("Error during lookup of sessionManager component.", ce);
        } finally {
            this.manager.release( sessionManager );
        }

        return EMPTY_MAP;
    }

}
