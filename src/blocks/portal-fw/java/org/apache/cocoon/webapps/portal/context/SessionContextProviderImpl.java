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
package org.apache.cocoon.webapps.portal.context;

import java.util.HashMap;
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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.portal.PortalConstants;
import org.apache.cocoon.webapps.portal.components.PortalManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.xpath.XPathProcessor;

/**
 *  Context provider for the portal context
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContextProviderImpl.java,v 1.9 2004/05/26 08:39:49 cziegeler Exp $
*/
public final class SessionContextProviderImpl extends AbstractLogEnabled
        implements SessionContextProvider, ThreadSafe, Component, Serviceable, Contextualizable, Disposable {

    private ServiceManager manager;
    private Context context;
    
    /** The XPath Processor */
    protected XPathProcessor xpathProcessor;
    
    /**
     * Get the context
     * @param name The name of the context
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    public SessionContext getSessionContext(String name)
    throws ProcessingException {
        Map objectModel = ContextHelper.getObjectModel(this.context);

        SessionContext context = this.getContext( objectModel, name );
        if (name.equals(PortalConstants.SESSION_CONTEXT_NAME) && context == null) {
            Request req = ObjectModelHelper.getRequest(objectModel);
            Session session = req.getSession(false);
            if (session != null) {

                PortalManager portal = null;
                try {
                    portal = (PortalManager)this.manager.lookup(PortalManager.ROLE);
                    // is this an external resource which wants access to a coplet?
                    String value = req.getParameter("portalcontext");
                    if (value != null) {
                        int sepOne, sepTwo;
                        sepOne = value.indexOf('_');
                        if (sepOne != -1) {
                            sepTwo = value.indexOf('_', sepOne+1);
                            if (sepTwo != -1) {
                                String copletIdentifier = value.substring(0, sepOne);
                                String copletID = value.substring(sepOne+1, sepTwo);
                                String copletNumber = value.substring(sepTwo+1);

                                if (copletIdentifier.equals("coplet")) {
                                    Map info = new HashMap(3);
                                    SessionContextImpl.copletInfo.set(info);

                                    SourceParameters pars = new SourceParameters();
                                    info.put(PortalConstants.COPLETINFO_PARAMETERS, pars);
                                    pars.setSingleParameterValue(PortalConstants.PARAMETER_ID, copletID);
                                    pars.setSingleParameterValue(PortalConstants.PARAMETER_NUMBER, copletNumber);
                                    pars.setSingleParameterValue(PortalConstants.PARAMETER_MEDIA, portal.getMediaType());

                                    info.put(PortalConstants.COPLETINFO_STATUSPROFILE, portal.getStatusProfile());
                                    info.put(PortalConstants.COPLETINFO_PORTALURI, req.getRequestURI());
                                }
                            }
                        }
                    } else {
                        if (SessionContextImpl.copletInfo.get() == null) {
                            throw new ProcessingException("Portal context not available outside a coplet.");
                        }
                    }
                    context = new SessionContextImpl(name, objectModel, portal, this.xpathProcessor);
                    objectModel.put(this.getClass().getName()+name, context);
                } catch (ServiceException se) {
                    throw new ProcessingException("Unable to lookup portal.", se);
                } finally {
                    this.manager.release(portal);
                }
            }
        }
        return context;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.session.context.SessionContextProvider#existsSessionContext(java.lang.String)
     */
    public boolean existsSessionContext(String name)
    throws ProcessingException {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        return (this.getContext( objectModel, name) != null);
    }

    private SessionContext getContext(Map objectModel, String name) {
        SessionContext context = (SessionContext) objectModel.get(this.getClass().getName()+name);
        if ( context != null ) {
            SessionContextImpl r = (SessionContextImpl)context;
            if (!(r.getRequest() == ObjectModelHelper.getRequest( objectModel))) {
                context = null;
                objectModel.remove(this.getClass().getName()+name);
            }
        }
        return context; 
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#Service(org.apache.avalon.framework.Service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.xpathProcessor);
            this.xpathProcessor = null;
            this.manager = null;
        }
    }
}
