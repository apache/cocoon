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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.FormManager;
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.cocoon.webapps.session.SessionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.w3c.dom.DocumentFragment;

/**
 * Form handling
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultFormManager.java,v 1.7 2004/04/15 08:26:53 cziegeler Exp $
*/
public final class DefaultFormManager
extends AbstractLogEnabled
implements Serviceable, Component, FormManager, ThreadSafe, Contextualizable {

    /** This session attribute is used to store the information for the inputxml tags */
    private static final String ATTRIBUTE_INPUTXML_STORAGE = "org.apache.cocoon.webapps.session.InputXMLStorage";

    /** The <code>ServiceManager</code> */
    private ServiceManager manager;

    /** The context */
    private Context context;
    
    /**
     * Get the context
     */
    private SessionContext getContext(String name) 
    throws ProcessingException {
        ContextManager contextManager = null;
        try {
            contextManager = (ContextManager) this.manager.lookup(ContextManager.ROLE);
            return contextManager.getContext( name );
        } catch (ServiceException ce ) {
            throw new ProcessingException("Unable to lookup context manager.", ce);
        } finally {
            this.manager.release(contextManager);
        }
    }
    
    private DocumentFragment getContextFragment(String context, String path) 
    throws ProcessingException {
        SessionManager sessionManager = null;
        try {
            sessionManager = (SessionManager) this.manager.lookup(SessionManager.ROLE);
            return sessionManager.getContextFragment( context, path );
        } catch (ServiceException ce ) {
            throw new ProcessingException("Unable to lookup session manager.", ce);
        } finally {
            this.manager.release(sessionManager);
        }        
    }
    
    /**
     * @see FormManager#registerInputField(String, String, String, String)
     */
    public DocumentFragment registerInputField(String contextName,
                                               String path,
                                               String name,
                                               String formName)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN registerInputField context="+contextName+", path="+path+", name="+name+", formName="+formName);
        }

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.registerInputField: Context Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.registerInputField: Path is required");
        }
        if (name == null) {
            throw new ProcessingException("SessionManager.registerInputField: Name is required");
        }
        if (formName == null) {
            throw new ProcessingException("SessionManager.registerInputField: Form is required");
        }

        DocumentFragment value = null;
        SessionContext context = this.getContext(contextName);
        if (context == null) {
            throw new ProcessingException("SessionManager.registerInputField: Context not found " + contextName);
        }
        final Request request = ContextHelper.getRequest(this.context);
        Session session = request.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.registerInputField: Session is required for context " + contextName);
        }

        synchronized(session) {
            Map inputFields = (Map)session.getAttribute(ATTRIBUTE_INPUTXML_STORAGE);
            if (inputFields == null) {
                inputFields = new HashMap(10);
                session.setAttribute(ATTRIBUTE_INPUTXML_STORAGE, inputFields);
            }
            inputFields.put(name, new Object[] {context, path, formName});
            value = context.getXML(path);
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END registerInputField value="+value);
        }
        return value;
    }

    /**
     * Process all input fields.
     * The fields are removed even if the request did not contain
     * any values.
     * This is a private method and should not be invoked directly.
     */
    private void processInputFields(Map objectModel) {
        // we only want to invoke the testing once per request
        if (objectModel.containsKey(this.getClass().getName())) {
            return;
        }
        objectModel.put(this.getClass().getName(), "done");

        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN processInputFields");
        }

        final Request request = ObjectModelHelper.getRequest( objectModel );
        final String formName = request.getParameter(SessionConstants.SESSION_FORM_PARAMETER);
        if ( null != formName ) {
            final Session session = request.getSession(false);
            if (session != null) {
                synchronized(session) {
                    final Map inputFields = (Map)session.getAttribute(ATTRIBUTE_INPUTXML_STORAGE);
                    if (inputFields != null) {
                        final Enumeration keys = request.getParameterNames();
                        String   currentKey;
                        Object[] contextAndPath;

                        while (keys.hasMoreElements()) {
                            currentKey = (String)keys.nextElement();
                            if (inputFields.containsKey(currentKey)) {
                                contextAndPath = (Object[])inputFields.get(currentKey);
                                inputFields.remove(currentKey);

                                SessionContext context = (SessionContext)contextAndPath[0];
                                String path            = (String)contextAndPath[1];

                                if (formName.equals(contextAndPath[2])) {
                                    try {
                                        context.setXML(path,
                                                     this.getContextFragment(SessionConstants.REQUEST_CONTEXT, "/parameter/"+currentKey));
                                    } catch (ProcessingException ignore) {
                                        this.getLogger().warn("Exception during processing of input fields.", ignore);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END processInputFields");
        }
    }

    public void processInputFields() {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        this.processInputFields( objectModel ) ;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

}
