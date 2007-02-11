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
package org.apache.cocoon.webapps.authentication.context;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.authentication.AuthenticationConstants;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;


/**
 *  Context provider for the authentication context
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AuthenticationContextProvider.java,v 1.7 2003/11/24 04:07:22 antonio Exp $
*/
public final class AuthenticationContextProvider
extends AbstractLogEnabled
implements SessionContextProvider, ThreadSafe, Component, Serviceable {

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
