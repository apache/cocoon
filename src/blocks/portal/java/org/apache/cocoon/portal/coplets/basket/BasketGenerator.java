/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplets.basket;

import java.io.IOException;
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
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * This is a portlet that displays the contents of a basket
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: BasketGenerator.java,v 1.3 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class BasketGenerator
extends ServiceableGenerator {
    
    /** This is the coplet ID that is used to display the content */
    protected String showCopletId;
    
    /** This is the layout ID that is used to display the content */
    protected String showLayoutId;

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
            Basket basket = this.basketManager.getBasket();
    
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                LinkService linkService = service.getComponentManager().getLinkService();
                
                XMLUtils.startElement(this.xmlConsumer, "basket-content");
    
                XMLUtils.startElement(this.xmlConsumer, "item-count");
                XMLUtils.data(this.xmlConsumer, String.valueOf(basket.size()));
                XMLUtils.endElement(this.xmlConsumer, "item-count");
                XMLUtils.startElement(this.xmlConsumer, "persist-url");
                final Event saveEvent = new SaveBasketEvent();
                XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(saveEvent));
                XMLUtils.endElement(this.xmlConsumer, "persist-url");
                
                if ( basket.size() > 0 ) {
                    ProfileManager profileManager = service.getComponentManager().getProfileManager();
                    
                    XMLUtils.startElement(this.xmlConsumer, "items");
                    for(int i=0; i<basket.size();i++) {
                        Object item = basket.getItem(i);
                        XMLUtils.startElement(this.xmlConsumer, "item");
                        XMLUtils.startElement(this.xmlConsumer, "id");
                        XMLUtils.data(this.xmlConsumer, item.toString());
                        XMLUtils.endElement(this.xmlConsumer, "id");
                        if ( item instanceof ContentItem ) {
                            ContentItem ci = (ContentItem)item;
                            Event e = new ShowItemEvent(item, profileManager.getPortalLayout(null, this.showLayoutId), this.showCopletId);
                            XMLUtils.startElement(this.xmlConsumer, "show-url");
                            XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(e));
                            XMLUtils.endElement(this.xmlConsumer, "show-url");
                            if (ci.size() != -1 ) {
                                XMLUtils.startElement(this.xmlConsumer, "size");
                                XMLUtils.data(this.xmlConsumer, String.valueOf(ci.size()));
                                XMLUtils.endElement(this.xmlConsumer, "size");
                            }                        
                        }
                        Event removeEvent = new RemoveItemEvent(item);
                        XMLUtils.startElement(this.xmlConsumer, "remove-url");
                        XMLUtils.data(this.xmlConsumer, linkService.getLinkURI(removeEvent));
                        XMLUtils.endElement(this.xmlConsumer, "remove-url");
                        XMLUtils.endElement(this.xmlConsumer, "item");
                    }
                    XMLUtils.endElement(this.xmlConsumer, "items");
                }
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
    throws IOException, SAXException, ProcessingException {
        List baskets = this.basketManager.getBaskets();

        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            LinkService linkService = service.getComponentManager().getLinkService();
            XMLUtils.startElement(this.xmlConsumer, "basket-admin");
            if ( baskets.size() > 0 ) {
                XMLUtils.startElement(this.xmlConsumer, "baskets");
                for(int i=0; i<baskets.size();i++) {
                    BasketManager.BasketDescription item = (BasketManager.BasketDescription)baskets.get(i);
                    XMLUtils.startElement(this.xmlConsumer, "basket");

                    XMLUtils.startElement(this.xmlConsumer, "id");
                    XMLUtils.data(this.xmlConsumer, item.id);
                    XMLUtils.endElement(this.xmlConsumer, "id");
                    
                    XMLUtils.startElement(this.xmlConsumer, "size");
                    XMLUtils.data(this.xmlConsumer, String.valueOf(item.size));
                    XMLUtils.endElement(this.xmlConsumer, "size");
                    
                    Event event = new CleanBasketEvent(item.id);
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
            
            e = new CleanBasketEvent();
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
    
}
