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
 * @version CVS $Id: AbstractSessionTransformer.java,v 1.4 2004/03/05 13:02:23 bdelacretaz Exp $
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
