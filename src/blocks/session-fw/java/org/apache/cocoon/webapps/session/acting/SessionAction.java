/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.session.acting;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ComposerAction;
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
 * @version CVS $Id: SessionAction.java,v 1.3 2003/07/18 08:37:15 cziegeler Exp $
*/
public final class SessionAction
extends ComposerAction
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
        } catch (ComponentException ce) {
            throw new ProcessingException("Error during lookup of sessionManager component.", ce);
        } finally {
            this.manager.release( (Component)sessionManager );
        }

        return EMPTY_MAP;
    }

}
