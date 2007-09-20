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
package org.apache.cocoon.portal.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.components.serializers.util.EncodingSerializer;
import org.apache.cocoon.components.serializers.util.XHTMLSerializer;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.services.PortalManager;
import org.springframework.web.HttpRequestHandler;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class PortalServlet implements HttpRequestHandler {

    /** The portal service. */
    protected PortalService portalService;

    /** Our configuration. */
    protected Properties properties;

    /**
     * @see org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        // This is a fix: if we don't use the link service here, we get
        // in some rare cases a wrong uri!
        // TODO - Find the real reason
        this.portalService.getLinkService().getRefreshLinkURI();

        // start the portal rendering
        // 1. event processing
        // 2. rendering
        final PortalManager pm = this.portalService.getPortalManager();
        try {
            pm.process();
            // TODO - what do we here? We don't have an xml consumer
            // create serializer
            EncodingSerializer serializer = new XHTMLSerializer();
            pm.render(serializer, this.properties);
        } catch (SAXException se) {
            throw new ServletException(se);
        } catch (PortalException pe) {
            throw new ServletException(pe);
        }
    }
}
