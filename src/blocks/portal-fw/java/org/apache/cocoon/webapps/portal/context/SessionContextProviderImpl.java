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
package org.apache.cocoon.webapps.portal.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
import org.xml.sax.SAXException;

/**
 *  Context provider for the portal context
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContextProviderImpl.java,v 1.6 2004/01/09 11:20:23 cziegeler Exp $
*/
public final class SessionContextProviderImpl
extends AbstractLogEnabled
implements SessionContextProvider, ThreadSafe, Component, Composable, Contextualizable, Disposable {

    private ComponentManager manager;
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
                    portal = (PortalManager)manager.lookup(PortalManager.ROLE);
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
                } catch (SAXException se) {
                    throw new ProcessingException("SAXException", se);
                } catch (IOException ioe) {
                    throw new ProcessingException("IOException", ioe);
                } catch (ComponentException ce) {
                    throw new ProcessingException("Unable to lookup portal.", ce);
                } finally {
                    manager.release( (Component)portal);
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
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
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
            this.manager.release( (Component)this.xpathProcessor );
            this.xpathProcessor = null;
            this.manager = null;
        }
    }
}
