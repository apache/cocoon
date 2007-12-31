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
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.transformation.ProxyTransformer;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This coplet adapter is used to connect to external applications that are 
 * plugged into the portal.
 *
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 *
 * @version CVS $Id$
 */
public class ApplicationCopletAdapter extends CachingURICopletAdapter {

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.URICopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, java.lang.String, org.xml.sax.ContentHandler)
     */
    public void streamContent(final CopletInstanceData coplet,
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
    public void handleCopletInstanceEvent(CopletInstanceEvent e) {
        super.handleCopletInstanceEvent(e);
        
        if ( e instanceof CopletLinkEvent ) {
            CopletLinkEvent event = (CopletLinkEvent) e;
            CopletInstanceData coplet = (CopletInstanceData) event.getTarget();
            String link = event.getLink();
    
            if ("createNewCopletInstance".equals(link)) {
                try {
                    createNewInstance(coplet);
                } catch (ProcessingException ex) {
                    getLogger().error("Could not create new coplet instance", ex);
                }
            } else {
                // this is a normal link event, so save the url in the instance data
                // for ProxyTransformer
                String linkValue = event.getLink();
                Boolean addParams = (Boolean)this.getConfiguration(coplet, "appendParameters", Boolean.FALSE);
                if ( addParams.booleanValue() ) {
                    final StringBuffer uri = new StringBuffer(event.getLink());
                    boolean hasParams = (uri.toString().indexOf("?") != -1);
                    
                    // append parameters - if any
                    final Map objectModel = ContextHelper.getObjectModel(this.context);
                    final Request r = ObjectModelHelper.getRequest(objectModel);
                    final Enumeration params = r.getParameterNames();
                    while (params.hasMoreElements()) {
                        final String name = (String)params.nextElement();
                        if (!name.startsWith("cocoon-portal-")) {
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
    }

    /**
     * Creates a new instance of the given coplet. Also a new named item in the tab layout is
     * created to show the data of the new coplet instance in the portal.
     * @param 	coplet	the coplet instance data
     * @trows	ProcessingException if something fails in the creation process
     */
    private void createNewInstance(CopletInstanceData coplet)
    throws ProcessingException {
        ProfileManager profileManager = null;
        try {
            profileManager =
                (ProfileManager) this.manager.lookup(ProfileManager.ROLE);

            CopletData copletData = coplet.getCopletData();

            LayoutFactory lfac =
                (LayoutFactory) this.manager.lookup(LayoutFactory.ROLE);

            CopletLayout copletLayout =
                (CopletLayout) lfac.newInstance("coplet");

            CopletFactory cfac =
                (CopletFactory) manager.lookup(CopletFactory.ROLE);

            CopletInstanceData newCoplet = cfac.newInstance(copletData);

            copletLayout.setCopletInstanceData(newCoplet);
            profileManager.register(copletLayout);

            NamedItem newItem = new NamedItem();
            newItem.setLayout(copletLayout);

            CompositeLayout tabLayout =
                (CompositeLayout) profileManager.getPortalLayout(
                    "portalApplications", null);

            newItem.setName(getNewInstanceTabName(tabLayout));
            tabLayout.addItem(newItem);
        } catch (ServiceException ce) {
            throw new ProcessingException(
                "Unable to lookup profile manager.",
                ce);
        } catch (Exception e) {
            throw new ProcessingException(e);
        } finally {
            this.manager.release(profileManager);
        }
    }

    /**
     * Returns the name of the new named item in the tab layout
     * @return String the name of the new item
     */
    private String getNewInstanceTabName(CompositeLayout layout) {
        Integer data = (Integer) layout.getAspectData("tab");
        Item selectedItem = layout.getItem(data.intValue());

        if (selectedItem instanceof NamedItem) {
            return ((NamedItem) selectedItem).getName();
        }
        return ("New");
    }

    /**
     * Render the error content for a coplet
     * @param coplet
     * @param handler
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstanceData coplet,
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
