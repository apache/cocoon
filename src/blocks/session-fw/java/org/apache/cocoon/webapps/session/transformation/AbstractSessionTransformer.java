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
package org.apache.cocoon.webapps.session.transformation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.FormManager;
import org.apache.cocoon.webapps.session.SessionManager;

/**
 *  This class is the basis for all session transformers.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractSessionTransformer.java,v 1.3 2003/10/21 12:39:17 cziegeler Exp $
*/
public abstract class AbstractSessionTransformer
extends AbstractSAXTransformer {

    private SessionManager     sessionManager;
    private FormManager        formManager;
    private ContextManager     contextManager;
    
    /**
     * Get the SessionManager component
     */
    protected SessionManager getSessionManager()
    throws ProcessingException {
        if (this.sessionManager == null) {
            try {
                this.sessionManager = (SessionManager)this.manager.lookup(SessionManager.ROLE);
            } catch (ServiceException ce) {
                throw new ProcessingException("Error during lookup of SessionManager component.", ce);
            }
        }
        return this.sessionManager;
    }

    /**
     * Get the ContextManager component
     */
    protected ContextManager getContextManager()
    throws ProcessingException {
        if (this.contextManager == null) {
            try {
                this.contextManager = (ContextManager)this.manager.lookup(ContextManager.ROLE);
            } catch (ServiceException ce) {
                throw new ProcessingException("Error during lookup of ContextManager component.", ce);
            }
        }
        return this.contextManager;
    }

    /**
     * Get the FormManager component
     */
    protected FormManager getFormManager()
    throws ProcessingException {
        if (this.formManager == null) {
            try {
                this.formManager = (FormManager)this.manager.lookup(FormManager.ROLE);
            } catch (ServiceException ce) {
                throw new ProcessingException("Error during lookup of FormManager component.", ce);
            }
        }
        return this.formManager;
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        super.recycle();
        this.manager.release( this.sessionManager);
        this.manager.release( this.formManager);
        this.manager.release( this.contextManager);
        this.sessionManager = null;
        this.formManager = null;
        this.contextManager = null;
    }

    /**
     * Get the current session if available or return <code>null</code>.
     * @return The Session object or null.
     */
    public Session getSession()
    throws ProcessingException {
        return this.getSessionManager().getSession(false);
    }

}
