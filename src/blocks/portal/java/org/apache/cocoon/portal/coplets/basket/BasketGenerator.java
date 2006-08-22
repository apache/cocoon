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
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplets.basket.events.CleanBriefcaseEvent;
import org.apache.cocoon.portal.coplets.basket.events.RefreshBasketEvent;
import org.apache.cocoon.portal.coplets.basket.events.RemoveItemEvent;
import org.apache.cocoon.portal.coplets.basket.events.ShowBasketEvent;
import org.apache.cocoon.portal.coplets.basket.events.ShowItemEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * This is a portlet that displays the contents of a basket
 *
 * @version CVS $Id$
 */
public class BasketGenerator
extends ServiceableGenerator {
    
    /** This is the coplet ID that is used to display the content */
    protected String showCopletId;
    
    /** This is the layout ID that is used to display the content */
    protected String showLayoutId;

    /** The type of items to display */
    protected String type;
    
    /** The location of the type information */
    protected String typeLocation;
    
    /** admin mode? */
    protected boolean adminMode;
    
    /** The basket manager */
    protected BasketManager basketManager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.basketManager = (BasketManager)this.manager.lookup(BasketManager.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.basketManager);
            this.basketManager = null;
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.showCopletId = par.getParameter("show-coplet", null);
        this.showLayoutId = par.getParameter("show-layout", null);
        this.adminMode = par.getParameterAsBoolean("admin-mode", false);
        this.type = par.getParameter("type", null);
        this.typeLocation = par.getParameter("type-location", null);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        this.xmlConsumer.startDocument();
        if ( this.adminMode ) {
            this.generateAdminMode();            
        } else {
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);

                final UserConfiguration uc = UserConfiguration.get(this.objectModel, service);
                Basket    basket = null;
                Briefcase briefcase = null;
                Folder    folder = null;

                if ( uc.isBasketEnabled() ) {
                    basket = this.basketManager.getBasket();
                }
                if ( uc.isBriefcaseEnabled() ) {
                    briefcase = this.basketManager.getBriefcase();
                }
                if ( uc.isFolderEnabled() ) {
                    folder = this.basketManager.getFolder();
                }
    
                final LinkService linkService = service.getComponentManager().getLinkService();
                
                XMLUtils.startElement(this.xmlConsumer, "basket-content");
    
                this.toSAX(uc);
                
                final ProfileManager profileManager = service.getComponentManager().getProfileManager();
                    
                    XMLUtils.startElement(this.xmlConsumer, "items");
                
                int itemCount = 0;
                long itemSize = 0;

                StoreInfo info;
                
                info = this.toSAX(basket, linkService, profileManager);
                itemCount += info.count;
                itemSize += info.maxSize;
                info = this.toSAX(briefcase, linkService, profileManager);
                itemCount += info.count;
                itemSize += info.maxSize;
                info = this.toSAX(folder, linkService, profileManager);
                itemCount += info.count;
                itemSize += info.maxSize;

                XMLUtils.endElement(this.xmlConsumer, "items");

                XMLUtils.startElement(this.xmlConsumer, "item-count");
                XMLUtils.data(this.xmlConsumer, String.valueOf(itemCount));
                XMLUtils.endElement(this.xmlConsumer, "item-count");

                XMLUtils.startElement(this.xmlConsumer, "item-size");
                double f = itemSize / 10.24;
                f = Math.floor(f);
                if ( f < 10.0 && f > 0.1) {
                    f = 10.0;
                } else if ( f < 0.1 ) {
                    f = 0.0;
                }
                f = f / 100.0;
                XMLUtils.data(this.xmlConsumer, String.valueOf(f));
                XMLUtils.endElement(this.xmlConsumer, "item-size");

                XMLUtils.endElement(this.xmlConsumer, "basket-content");
            } catch (ServiceException se) {
                throw new SAXException("Unable to lookup portal service.", se);
            } finally {
                this.manager.release(service);
            }
        }
        this.xmlConsumer.endDocument();
    }

    /**
     * Render admin mode
     */
    protected void generateAdminMode() 
    throws SAXException {
        List baskets = this.basketManager.getBriefcaseDescriptions();

        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            LinkService linkService = service.getComponentManager().getLinkService();
            XMLUtils.startElement(this.xmlConsumer, "basket-admin");
            if ( baskets.size() > 0 ) {
                XMLUtils.startElement(this.xmlConsumer, "baskets");
                for(int i=0; i<baskets.size();i++) {
                    ContentStoreDescription item = (ContentStoreDescription)baskets.get(i);
                    XMLUtils.startElement(this.xmlConsumer, "basket");

                    XMLUtils.startElement(this.xmlConsumer, "id");
                    XMLUtils.data(this.xmlConsumer, item.id);
                    XMLUtils.endElement(this.xmlConsumer, "id");
                    
                    XMLUtils.startElement(this.xmlConsumer, "size");
                    XMLUtils.data(this.xmlConsumer, String.valueOf(item.size));
                    XMLUtils.endElement(this.xmlConsumer, "size");
                    
                    Event event = new CleanBriefcaseEvent((Briefcase)null);
                    XMLUtils.startElement(this.xmlConsumer, "remove-url");
                    XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(event));
                    XMLUtils.endElement(this.xmlConsumer, "remove-url");
                    
                    event = new ShowBasketEvent(item.id);
                    XMLUtils.startElement(this.xmlConsumer, "show-url");
                    XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(event));
                    XMLUtils.endElement(this.xmlConsumer, "show-url");

                    XMLUtils.endElement(this.xmlConsumer, "basket");
                }
                XMLUtils.endElement(this.xmlConsumer, "baskets");
            }
            Event e;
            e = new RefreshBasketEvent();
            XMLUtils.startElement(this.xmlConsumer, "refresh-url");
            XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(e));
            XMLUtils.endElement(this.xmlConsumer, "refresh-url");
            
            e = new CleanBriefcaseEvent();
            XMLUtils.startElement(this.xmlConsumer, "clean-url");
            XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(e));
            XMLUtils.endElement(this.xmlConsumer, "clean-url");
            
            XMLUtils.endElement(this.xmlConsumer, "basket-admin");
        } catch (ServiceException se) {
            throw new SAXException("Unable to lookup portal service.", se);
        } finally {
            this.manager.release(service);
        }
    }
    
    protected StoreInfo toSAX(ContentStore store, LinkService linkService, ProfileManager profileManager)
    throws SAXException {
        StoreInfo info = new StoreInfo();
        if ( store != null ) {
            for(int i=0; i<store.size();i++) {
                Object item = store.getItem(i);
                if ( item instanceof ContentItem ) {
                    ContentItem ci = (ContentItem)item;
                    
                    boolean process = true;
                    if ( this.type != null && this.type.length() > 0 && this.typeLocation != null ) {
                        Map attributes = (Map)ci.getAttribute("coplet-attributes");
                        if ( attributes != null ) {
                            if ( !this.type.equals(attributes.get(this.typeLocation)) ) {
                                process = false;
                            }
                        }
                    }
                    if ( process ) {
                        info.count++;
                        info.maxSize += ci.size();
                        XMLUtils.startElement(this.xmlConsumer, "item");
                        
                        XMLUtils.createElement(this.xmlConsumer, "title", item.toString());
        
                        XMLUtils.startElement(this.xmlConsumer, "store");
                        if ( store instanceof Briefcase ) {
                            XMLUtils.data(this.xmlConsumer, "briefcase");
                        } else if ( store instanceof Folder ) {
                            XMLUtils.data(this.xmlConsumer, "folder");                    
                        } else {
                            XMLUtils.data(this.xmlConsumer, "basket");                    
                        }
                        XMLUtils.endElement(this.xmlConsumer, "store");
                    
                        XMLUtils.createElement(this.xmlConsumer, "id", String.valueOf(ci.getId()));
                        Event e = new ShowItemEvent(store, item, profileManager.getPortalLayout(null, this.showLayoutId), this.showCopletId);
                        XMLUtils.createElement(this.xmlConsumer, "show-url", linkService.getLinkURI(e));
                        if (ci.size() != -1 ) {
                            XMLUtils.createElement(this.xmlConsumer, "size", String.valueOf(ci.size()));
                        }                        
                        XMLUtils.startElement(this.xmlConsumer, "attributes");
                        this.toSAX(ci.attributes);
                        XMLUtils.endElement(this.xmlConsumer, "attributes");
                        Event removeEvent = new RemoveItemEvent(store, item);
                        XMLUtils.createElement(this.xmlConsumer, "remove-url", linkService.getLinkURI(removeEvent));
                        
                        XMLUtils.endElement(this.xmlConsumer, "item");
                    }
                }
            }
        }    
        return info;
    }
    
    protected void toSAX(Map attributes)
    throws SAXException {
        if ( attributes != null ) {
            AttributesImpl a = new AttributesImpl();
            final Iterator i = attributes.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                final String key = current.getKey().toString();
                if ( "coplet-attributes".equals(key) ) {
                    this.toSAX((Map)current.getValue());
                } else {
                    final Object value = current.getValue();
                    final String valueText;
                    if ( value != null ) {
                        valueText = value.toString();
                    } else {
                        valueText ="";
                    }
                    a.addCDATAAttribute("name", key);
                    a.addCDATAAttribute("value", valueText);
                    XMLUtils.createElement(this.xmlConsumer, "attribute", a);
                    a.clear();
                }
            }
        }
    }
    
    protected void toSAX(UserConfiguration uc)
    throws SAXException {
        XMLUtils.startElement(this.xmlConsumer, "configuration");
        AttributesImpl attr = new AttributesImpl();
        
        if ( uc.isBasketEnabled() ) {
            XMLUtils.createElement(this.xmlConsumer, "basket", attr, "enabled");
            attr.clear();
        }
        if ( uc.isBriefcaseEnabled() ) {
            XMLUtils.createElement(this.xmlConsumer, "briefcase", "enabled");
            attr.clear();
        }
        if ( uc.isFolderEnabled() ) {
            XMLUtils.createElement(this.xmlConsumer, "folder", "enabled");
            attr.clear();
        }
        
        XMLUtils.endElement(this.xmlConsumer, "configuration");
    }
    
    public static final class StoreInfo {
        int count;
        long maxSize;
    }
}
