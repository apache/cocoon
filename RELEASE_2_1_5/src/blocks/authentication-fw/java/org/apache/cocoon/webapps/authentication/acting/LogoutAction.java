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
package org.apache.cocoon.webapps.authentication.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;

/**
 *  This action logs the current user out of a given handler
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: LogoutAction.java,v 1.7 2004/03/05 13:01:40 bdelacretaz Exp $
*/
public final class LogoutAction
extends ServiceableAction
implements ThreadSafe {

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN act resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }

        int mode;
        final String modeString = par.getParameter("mode", "if-not-authenticated");
        if ( modeString.equals("if-not-authenticated") ) {
            mode = AuthenticationConstants.LOGOUT_MODE_IF_NOT_AUTHENTICATED;
        } else if ( modeString.equalsIgnoreCase("if-unused") ) {
            mode = AuthenticationConstants.LOGOUT_MODE_IF_UNUSED;
        } else if ( modeString.equalsIgnoreCase("immediately") ) {
            mode = AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY;
        } else {
           throw new ProcessingException("Unknown mode " + modeString);
        }

        // logout
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager) this.manager.lookup(AuthenticationManager.ROLE);
            RequestState state = authManager.getState();
            
            final String handlerName = par.getParameter("handler",
                                                         (state == null ? null : state.getHandlerName()));
            if ( null == handlerName ) {
                throw new ProcessingException("LogoutAction requires at least the handler parameter.");
            }
            authManager.logout( handlerName , mode );
        } finally {
            this.manager.release( authManager );
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END act map={}");
        }

        return EMPTY_MAP;
    }

}
