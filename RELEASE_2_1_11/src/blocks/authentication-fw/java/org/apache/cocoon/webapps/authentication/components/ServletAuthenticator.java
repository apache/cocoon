/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.webapps.authentication.components;

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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Verify if a user can be authenticated.
 * This is a very simple authenticator that checks if the user is authenticated
 * using the servlet authentication mechanisms.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version CVS $Id: PipelineAuthenticator.java 30932 2004-07-29 17:35:38Z vgritsenko $
*/
public class ServletAuthenticator
    extends AbstractLogEnabled
    implements Contextualizable, ThreadSafe, Serviceable, Authenticator {

    protected Context context;
    protected ServiceManager manager;

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

    /**
     * Fill the authentication context.
     * This method can be overwritten to add any application specific data
     * to the user.
     * (Don't forget to call this implementation via super as well as it
     * adds the ID).
     *
     * @param contextDoc The context. This document has already the authentication
     *                   root node.
     */
    protected void fillContext(Document contextDoc) {
        final Request req = ContextHelper.getRequest(this.context);
        final Element root = contextDoc.getDocumentElement();

        // append the ID
        final Element id = contextDoc.createElement("ID");
        id.appendChild(contextDoc.createTextNode(req.getRemoteUser()));
        root.appendChild(id);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.authentication.components.Authenticator#authenticate(org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration, org.apache.excalibur.source.SourceParameters)
     */
    public AuthenticationResult authenticate(HandlerConfiguration configuration,
                                             SourceParameters parameters)
    throws ProcessingException {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("start authenticator using handler " + configuration.getName());
        }

        final Request req = ContextHelper.getRequest(this.context);
        AuthenticationResult result = null;
        if ( req.getRemoteUser() != null ) {
            DOMParser parser = null;
            try {
                parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
                final Document doc = parser.createDocument();
                final Element root = doc.createElement("authentication");
                doc.appendChild(root);
                this.fillContext(doc);

                result = new AuthenticationResult(true, doc);
            } catch (SAXException se) {
                throw new ProcessingException("Unable to create document.", se);
            } catch (ServiceException se) {
                throw new ProcessingException("Unable to lookup dom parser.", se);
            } finally {
                this.manager.release(parser);
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("end authenticator: " + result);
        }

        return result;
    }


    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.authentication.components.Authenticator#logout(UserHandler)
     */
    public void logout(UserHandler handler) {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("logout using handler " + handler.getHandlerName());
        }
        // TODO what can we do here?
    }
}
