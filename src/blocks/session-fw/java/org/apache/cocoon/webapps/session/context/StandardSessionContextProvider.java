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
package org.apache.cocoon.webapps.session.context;

import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.excalibur.xml.xpath.XPathProcessor;

/**
 *  Context provider for the temporarily context, the request and the
 *  response context.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: StandardSessionContextProvider.java,v 1.8 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public final class StandardSessionContextProvider
extends AbstractLogEnabled
implements SessionContextProvider, ThreadSafe, Contextualizable, Serviceable, Component, Disposable {

    protected Context context;
    
    protected ServiceManager manager;
    
    /** The xpath processor */
    protected XPathProcessor xpathProcessor;

    /**
     * Get the context
     * @param name The name of the context
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    public SessionContext getSessionContext(String name)
    throws ProcessingException {
        final Map objectModel = ContextHelper.getObjectModel( this.context );
        
        // get the context from the object model
        SessionContext context = this.getContext( objectModel, name );
        if ( context == null ) {
            if ( name.equals(SessionConstants.TEMPORARY_CONTEXT) ) {
                context = new SimpleSessionContext(this.xpathProcessor);
                context.setup(name, null, null);
            } else if ( name.equals(SessionConstants.REQUEST_CONTEXT) ) {
                context = new RequestSessionContext();
                context.setup(name, null, null);
                ((RequestSessionContext)context).setup( objectModel, this.manager, this.xpathProcessor );
            }
            objectModel.put(this.getClass().getName()+name, context);
        }
        return context;
    }

    /**
     * Does the context exist?
     */
    public boolean existsSessionContext(String name)
    throws ProcessingException {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        return (this.getContext( objectModel, name) != null);
    }

    private SessionContext getContext(Map objectModel, String name) {
        SessionContext context = (SessionContext) objectModel.get(this.getClass().getName()+name);
        if ( context != null && !name.equals(SessionConstants.TEMPORARY_CONTEXT)) {
            if ( name.equals(SessionConstants.REQUEST_CONTEXT)) {
                RequestSessionContext r = (RequestSessionContext)context;
                if (!(r.getRequest() == ObjectModelHelper.getRequest( objectModel))) {
                    context = null;
                    objectModel.remove(this.getClass().getName()+name);
                }
            }
        }
        return context; 
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
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null) {
            this.manager.release( this.xpathProcessor );
            this.xpathProcessor = null;
            this.manager = null;            
        }
    }

}
