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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.portal.PortalConstants;
import org.apache.cocoon.webapps.portal.components.PortalManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

/**
 *  Context provider for the portal context
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionContextProviderImpl.java,v 1.1 2003/03/09 00:05:19 pier Exp $
*/
public final class SessionContextProviderImpl
implements SessionContextProvider {

    /**
     * Get the context
     * @param name The name of the context
     * @param objectModel The objectModel of the current request.
     * @return The context
     * @throws ProcessingException If the context is not available.
     */
    public SessionContext getSessionContext(String name,
                                            Map objectModel,
                                            SourceResolver   resolver,
                                            ComponentManager manager)
    throws ProcessingException {
        SessionContext context = null;
        if (name.equals(PortalConstants.SESSION_CONTEXT_NAME) == true) {
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

                                if (copletIdentifier.equals("coplet") == true) {
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

                    context = new SessionContextImpl(name, objectModel, portal);
                } catch (SAXException se) {
                    throw new ProcessingException("SAXException", se);
                } catch (IOException ioe) {
                    throw new ProcessingException("IOException", ioe);
                } catch (ComponentException ce) {
                    throw new ProcessingException("Unable to lookup portal.", ce);
                } finally {
                    manager.release(portal);
                }
            }
        }
        return context;
    }
}
