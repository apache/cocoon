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
package org.apache.cocoon.webapps.session.components;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.FormManager;
import org.apache.cocoon.webapps.session.SessionManager;
import org.apache.cocoon.webapps.session.TransactionManager;
import org.xml.sax.SAXException;

/**
 * The base class for all components
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractSessionComponent.java,v 1.2 2003/05/04 20:19:41 cziegeler Exp $
*/
public abstract class AbstractSessionComponent extends AbstractLogEnabled
    implements Component, Composable, Recomposable, Recyclable, RequestLifecycleComponent {

    private SessionManager     sessionManager;
    private FormManager        formManager;
    private ContextManager     contextManager;
    private TransactionManager transactionManager;
    
    protected ComponentManager manager;

    /** The current object model */
    protected Map     objectModel;

    /** The current source resolver */
    protected SourceResolver resolver;

    protected Request          request;
    protected Response         response;


    /**
     * Composer interface. Get the Avalon ComponentManager.
     */
    public void compose(ComponentManager manager) 
    throws ComponentException {
        this.manager = manager;
    }

    /**
     * Recomposable
     */
    public void recompose( ComponentManager componentManager )
    throws ComponentException {
        this.recycle();
        this.manager = componentManager;
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * used to process the request.
     *  Set up the SessionManager component.
     *  This method is automatically called for each request. Do not invoke
     *  this method by hand.
     */
    public void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException {
        this.objectModel = objectModel;
        this.resolver    = resolver;
        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
    }

    /**
     * Get the SessionManager component
     */
    protected SessionManager getSessionManager()
    throws ProcessingException {
        if (this.sessionManager == null) {
            try {
                this.sessionManager = (SessionManager)this.manager.lookup(SessionManager.ROLE);
            } catch (ComponentException ce) {
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
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of ContextManager component.", ce);
            }
        }
        return this.contextManager;
    }

    /**
     * Get the ContextManager component
     */
    protected TransactionManager getTransactionManager()
    throws ProcessingException {
        if (this.transactionManager == null) {
            try {
                this.transactionManager = (TransactionManager)this.manager.lookup(TransactionManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of TransactionManager component.", ce);
            }
        }
        return this.transactionManager;
    }

    /**
     * Get the FormManager component
     */
    protected FormManager getFormManager()
    throws ProcessingException {
        if (this.formManager == null) {
            try {
                this.formManager = (FormManager)this.manager.lookup(FormManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of FormManager component.", ce);
            }
        }
        return this.formManager;
    }

    /**
     * Recycle
     */
    public void recycle() {
        if (this.manager != null) {
            this.manager.release( (Component)this.sessionManager);
            this.manager.release( (Component)this.formManager);
            this.manager.release( (Component)this.contextManager);
            this.manager.release( (Component)this.transactionManager);
        }
        this.transactionManager = null;
        this.sessionManager = null;
        this.formManager = null;
        this.contextManager = null;
        this.objectModel = null;
        this.resolver = null;
        this.request = null;
        this.response = null;
    }

}
