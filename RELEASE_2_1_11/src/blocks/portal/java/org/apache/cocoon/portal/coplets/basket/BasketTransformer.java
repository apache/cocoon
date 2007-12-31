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
package org.apache.cocoon.portal.coplets.basket;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplets.basket.BasketManager.ActionInfo;
import org.apache.cocoon.portal.coplets.basket.events.AddItemEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer supports the basket and briefcase feature. It can generate links to
 * add content into a content store.
 *
 * @version CVS $Id$
 */
public class BasketTransformer
    extends AbstractBasketTransformer {

    /** Element to add a link */
    protected static final String ADD_ITEM_ELEMENT = "add-item";

    /** Element to show all actions */
    protected static final String SHOW_ACTIONS_ELEMENT = "show-actions";

    /** The default store: briefcase or basket */
    protected String defaultStoreName = "basket";

    /** The default link element name */
    protected String defaultLinkElement = "a";

    /** The default namespace for the link element */
    protected String defaultLinkElementNS = "";

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        super.configure(configuration);
        this.defaultStoreName = configuration.getChild("default-store").getValue(this.defaultStoreName);
        this.defaultLinkElement = configuration.getChild("default-link-element").getValue(this.defaultLinkElement);
        this.defaultLinkElementNS = configuration.getChild("default-link-element-ns").getValue(this.defaultLinkElementNS);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws ProcessingException, IOException, SAXException {
        if ( ADD_ITEM_ELEMENT.equals(name) ) {
            final String linkElementName = this.parameters.getParameter("link-element", this.defaultLinkElement);
            final String linkElementNS = this.parameters.getParameter("link-element-ns", this.defaultLinkElementNS);
            XMLUtils.endElement(this.contentHandler, linkElementNS, linkElementName);
        } else if ( SHOW_ACTIONS_ELEMENT.equals(name) ) {
            // nothing to do here
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startTransformingElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startTransformingElement(String uri, String name,
                                         String raw, Attributes attr)
    throws ProcessingException, IOException, SAXException {
        if ( ADD_ITEM_ELEMENT.equals(name) ) {
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);

                // do we want to add content or a link?
            boolean addContent = false;
                final String value = attr.getValue("content");
            if ( value != null ) {
                addContent = new Boolean(value).booleanValue();
            }
                
                // do we want to add a url or a coplet?
                final ContentItem ci;
                final String href = attr.getValue("href");            
                if ( href != null ) {
                    ci = new ContentItem(href, addContent);
                } else {
                    final String copletId = attr.getValue("coplet");
                    final CopletInstanceData cid = service.getComponentManager().getProfileManager().getCopletInstanceData(copletId);                    
                    ci = new ContentItem(cid, addContent);
                }

                // if a title is present set the title
                final String title = attr.getValue("title");
                if(title!=null) {
                    ci.setTitle(title);
                }
                
                // do we want to add the content to the basket or to the briefcase
                final ContentStore store;
                final String storeName = (attr.getValue("store") == null ? this.defaultStoreName : attr.getValue("store"));
                if ("basket".equalsIgnoreCase(storeName) )     {
                    store = this.basketManager.getBasket();
                } else {
                    store = this.basketManager.getBriefcase();
                }

                final Event e = new AddItemEvent(store, ci);
                final AttributesImpl ai = new AttributesImpl();
                String newLink = service.getComponentManager().getLinkService().getLinkURI(e);
                // check for bockmark
                final String bookmark = attr.getValue("bookmark");
                if ( bookmark != null && bookmark.length() > 0) {
                    int pos = newLink.indexOf('?') + 1;
                    final char separator;
                    if ( bookmark.indexOf('?') == -1 ) {
                        separator = '?';
                    } else {
                        separator = '&';
                    }
                    newLink = bookmark + separator + newLink.substring(pos);
                }
                ai.addCDATAAttribute("href", newLink);

                final String linkElementName = this.parameters.getParameter("link-element", this.defaultLinkElement);
                final String linkElementNS = this.parameters.getParameter("link-element-ns", this.defaultLinkElementNS);
                XMLUtils.startElement(this.contentHandler, linkElementNS, linkElementName, ai);
            } catch (ServiceException se) {
                throw new SAXException("Unable to lookup portal service.", se);
            } finally {
                this.manager.release(service);
            }
        } else if ( SHOW_ACTIONS_ELEMENT.equals(name) ) {
            // basket or briefcase
            final List actions;
            final String storeName = (attr.getValue("store") == null ? this.defaultStoreName : attr.getValue("store"));
            if ("basket".equalsIgnoreCase(storeName) )     {
                actions = this.basketManager.getBasketActions();
            } else {
                actions = this.basketManager.getBriefcaseActions();
                }
            final String checkedAction = attr.getValue("checked");
            final Iterator i = actions.iterator();
            AttributesImpl a = new AttributesImpl();
            while ( i.hasNext() ) {
                final BasketManager.ActionInfo current = (ActionInfo) i.next();
                a.addCDATAAttribute("name", current.name);
                if ( current.name.equals(checkedAction) ) {
                    a.addCDATAAttribute("checked", "true");
                }
                XMLUtils.createElement(this.xmlConsumer, "action", a);
                a.clear();
            }
        }
    }

}
