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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.transformation.ProxyTransformer;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This coplet adapter is used to connect to external applications that are
 * plugged into the portal.
 *
 * TODO: Decide if we still need this adapter.
 * @version $Id$
 */
public class ApplicationCopletAdapter extends CocoonCopletAdapter {

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.CocoonCopletAdapter#streamContent(org.apache.cocoon.portal.om.CopletInstance, java.lang.String, org.xml.sax.ContentHandler)
     */
    protected void streamContent(final CopletInstance coplet,
                                 final String uri,
                                 final ContentHandler contentHandler)
    throws SAXException {
        try {
            super.streamContent(coplet, uri, contentHandler);
        } catch (SAXException se) {
            this.getLogger().error(
                "ApplicationCopletAdapter: Exception while getting coplet resource",
                se);

            this.renderErrorContent(coplet, contentHandler);
        }
    }

    /**
     * This adapter listens for CopletLinkEvents. If it catches one the link uri is saved in
     * the coplet instance data for further handling in the ProxyTransformer.
     * There is a special CopletLinkEvent with the uri "createNewCopletInstance", which is the
     * trigger to create a new instance of the one that is the target of the event.
     */
    public void inform(CopletInstanceEvent e) {
        super.inform(e);

        if ( e instanceof CopletLinkEvent ) {
            CopletLinkEvent event = (CopletLinkEvent) e;
            CopletInstance coplet = event.getTarget();

            // this is a normal link event, so save the url in the instance data
            // for ProxyTransformer
            String linkValue = event.getLink();
            Boolean addParams = (Boolean)this.getConfiguration(coplet, "appendParameters", Boolean.FALSE);
            if ( addParams.booleanValue() ) {
                final StringBuffer uri = new StringBuffer(event.getLink());
                boolean hasParams = (uri.toString().indexOf("?") != -1);

                // append parameters - if any
                LinkService linkService = this.portalService.getLinkService();
                final javax.servlet.http.HttpServletRequest r = this.portalService.getRequestContext().getRequest();
                final Enumeration params = r.getParameterNames();
                while (params.hasMoreElements()) {
                    final String name = (String)params.nextElement();
                    if (!linkService.isInternalParameterName(name)) {
                        final String[] values = r.getParameterValues(name);
                        for(int i = 0; i < values.length; i++) {
                            if ( hasParams ) {
                                uri.append('&');
                            } else {
                                uri.append('?');
                                hasParams = true;
                            }
                            uri.append(name);
                            uri.append('=');
                            try {
                                uri.append(NetUtils.decode(values[i], "utf-8"));
                            } catch (UnsupportedEncodingException uee) {
                                // ignore this
                            }
                        }
                    }
                }
                linkValue = uri.toString();
            }
            coplet.setTemporaryAttribute(ProxyTransformer.LINK, linkValue);
        }
    }

    /**
     * Render the error content for a coplet
     * @param coplet
     * @param handler
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstance coplet,
                                         ContentHandler handler)
    throws SAXException {
        handler.startDocument();
        XMLUtils.startElement(handler, "p");
        XMLUtils.data(
            handler,
            "ApplicationCopletAdapter: Can't get content for coplet "
                + coplet.getId()
                + ". Look up the logs.");
        XMLUtils.endElement(handler, "p");
        handler.endDocument();

        return true;
    }
}
