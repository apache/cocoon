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
 * The base class for own components
 * This is only here for compatibility
 * 
 * @deprecated Lookup the components yourself and use contextualizable to get the
 *             current object model
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractSessionComponent.java,v 1.4 2004/03/05 13:02:22 bdelacretaz Exp $
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
