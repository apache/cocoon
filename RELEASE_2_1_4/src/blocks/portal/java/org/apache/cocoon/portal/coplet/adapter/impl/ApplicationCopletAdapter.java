/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.coplet.adapter.impl;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.application.PortalApplicationConfigFactory;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.transformation.ProxyTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This coplet adapter is used to connect to external applications that are plugged into the portal
 *
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * 
 * @version CVS $Id: ApplicationCopletAdapter.java,v 1.3 2004/02/09 13:39:42 cziegeler Exp $
 */
public class ApplicationCopletAdapter extends CachingURICopletAdapter {

    public void streamContent(
        final CopletInstanceData coplet,
        final String uri,
        final ContentHandler contentHandler)
        throws SAXException {
        try {
            super.streamContent(coplet, uri, contentHandler);
        }
        catch (SAXException se) {
            getLogger().error(
                "ApplicationCopletAdapter: Exception while getting coplet resource",
                se);

            renderErrorContent(coplet, contentHandler);
        }
    }

    /**
     * This adapter listens for CopletLinkEvents. If it catches one the link uri is saved in
     * the coplet instance data for further handling in the ProxyTransformer.
     * There is a special CopletLinkEvent with the uri "createNewCopletInstance", which is the
     * trigger to create a new instance of the one that is the target of the event.
     */
    public void handleCopletInstanceEvent(Event e) {
        super.handleCopletInstanceEvent(e);
        
        if ( e instanceof CopletLinkEvent ) {
            CopletLinkEvent event = (CopletLinkEvent) e;
            CopletInstanceData coplet = (CopletInstanceData) event.getTarget();
            String link = event.getLink();
    
            if ("createNewCopletInstance".equals(link)) {
                try {
                    createNewInstance(coplet);
                }
                catch (ProcessingException ex) {
                    getLogger().error("Could not create new coplet instance", ex);
                }
            }
            else {
                // this is a normal link event, so save the url in the instance data
                // for ProxyTransformer
                coplet.setAttribute(ProxyTransformer.LINK, event.getLink());
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
        }
        catch (Exception e) {
            throw new ProcessingException(e);
        }
        finally {
            this.manager.release(profileManager);
        }
    }

    /**
     * Returns the name of the new named item in the tab layout
     * @return String the name of the new item
     */
    private String getNewInstanceTabName(CompositeLayout layout) {
        Integer data = (Integer) layout.getAspectData("tab");
        Item selectedItem = (NamedItem) layout.getItem(data.intValue());

        if (selectedItem instanceof NamedItem) {
            return ((NamedItem) selectedItem).getName();
        }
        else {
            return ("New");
        }
    }

    /**
     * Sets the application configuration in the coplet instance data.
     * @param	coplet the coplet instance data
     */
    private void setApplicationConfig(CopletInstanceData coplet) {
        try {
            PortalApplicationConfigFactory factory =
                PortalApplicationConfigFactory.getInstance(resolver);

            coplet.setAttribute(
                ProxyTransformer.CONFIG,
                factory.getConfig(coplet.getCopletData().getId()));
        }
        catch (ProcessingException pe) {
            getLogger().error(
                "Error while getting portal application configuration for coplet "
                    + coplet.getId(),
                pe);
        }
    }

    /**
     * Called when user logs in to the portal.
     */
    public void login(CopletInstanceData coplet) {
        getLogger().info("ApplicationCopletAdapter:login");
        setApplicationConfig(coplet);
    }

    /**
     * Called when user logs out from the portal.
     */
    public void logout(CopletInstanceData coplet) {
        getLogger().info("ApplicationCopletAdapter:logout");
    }

    /**
     * Render the error content for a coplet
     * @param coplet
     * @param handler
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(
        CopletInstanceData coplet,
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
