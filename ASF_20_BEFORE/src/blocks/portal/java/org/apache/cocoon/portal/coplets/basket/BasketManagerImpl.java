/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.coplets.basket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.io.IOUtil;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.servlet.multipart.PartOnDisk;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * This is the implementation of the basket manager
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: BasketManagerImpl.java,v 1.2 2004/02/24 10:34:54 joerg Exp $
 */
public class BasketManagerImpl
extends AbstractLogEnabled
implements BasketManager, Serviceable, Subscriber, Contextualizable, Initializable, Parameterizable, ThreadSafe, Component  {
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The component context */
    protected Context context;
    
    /** The configuration for storing baskets */
    protected String directory;
    
    /** The class name of the basket */
    protected String basketClassName = Basket.class.getName();
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.directory = parameters.getParameter("directory", this.directory);
        this.basketClassName = parameters.getParameter("basket-class", this.basketClassName);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        this.directory = ((File)context.get(Constants.CONTEXT_WORK_DIR)).getAbsolutePath();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        EventManager eventManager = null;
        try {
            eventManager = (EventManager) this.manager.lookup(EventManager.ROLE);
            eventManager.getRegister().subscribe(this);
        } finally {
            this.manager.release(eventManager);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return BasketEvent.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event event) {
        // dispatch
        Session session = ContextHelper.getRequest(this.context).getSession();
        Basket basket = this.getBasket();
        if ( event instanceof AddItemEvent ) {
            
            this.processAddItemEvent((AddItemEvent)event, basket);
            
        } else if (event instanceof RemoveItemEvent ){
            
            this.processRemoveItemEvent((RemoveItemEvent)event, basket);
            
        } else if (event instanceof SaveBasketEvent) {
            
            this.saveBasket(basket);
            
        } else if (event instanceof RefreshBasketEvent) {
            
            session.removeAttribute(ALL_BASKETS_KEY);          
            
        } else if (event instanceof CleanBasketEvent) {
            
            this.processCleanBasketEvent((CleanBasketEvent)event, session);
            
        } else if ( event instanceof UploadItemEvent ) {
            
            this.processUploadItemEvent((UploadItemEvent)event, basket);
        } else if ( event instanceof ShowItemEvent ) {
            
            this.processShowItemEvent((ShowItemEvent)event, basket);
        } else if ( event instanceof ShowBasketEvent ) {
            
            this.processShowBasketEvent((ShowBasketEvent)event, session);
        }
    }

    /**
     * Process an upload and add the item to the basket
     * @param event The event triggering the action
     * @param basket The basket
     */
    protected void processUploadItemEvent(UploadItemEvent event, Basket basket) {
        Request req = ContextHelper.getRequest(this.context);
        List paramNames = event.getItemNames();
        Iterator i = paramNames.iterator();
        while ( i.hasNext() ) {
            String name = (String)i.next();
            Object o = req.get(name);
            if ( o != null && o instanceof Part) {
                Part file = (Part)o;
                try {
                    byte[] c = IOUtil.toByteArray(file.getInputStream());
                    ContentItem ci = new ContentItem(file.getFileName(), true);
                    ci.setContent(c);
                    basket.addItem(ci);
                } catch (Exception ignore) {
                }
                if ( file instanceof PartOnDisk) {
                    ((PartOnDisk)file).getFile().delete();
                }
            }
        }
    }

    /**
     * Show one item of the basket
     * @param event  The event triggering the action
     * @param basket The basket
     */
    protected void processShowItemEvent(ShowItemEvent event, Basket basket) {
        if ( event.getItem() instanceof ContentItem ) {
            PortalService service = null;
            try {
                service = (PortalService) this.manager.lookup(PortalService.ROLE);
                
                ContentItem ci = (ContentItem)event.getItem();
                CopletLayout layout = (CopletLayout) event.getLayout();
                CopletInstanceData cid = null;
                if ( ci.isContent() ) {
                    CopletData copletData = service.getComponentManager().getProfileManager().getCopletData(event.getCopletDataId());
                    cid = service.getComponentManager().getCopletFactory().newInstance(copletData);
                    cid.setAttribute("item-content", ci.getContent());                
                } else {
                    if ( ci.getURL() != null ) {
                        SourceResolver resolver = null;
                        Source source = null;
                        String url = null;
                        try {
                            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
                            url = ci.getURL();
                            source = resolver.resolveURI(url);
                            CopletData copletData = service.getComponentManager().getProfileManager().getCopletData(event.getCopletDataId());
                            cid = service.getComponentManager().getCopletFactory().newInstance(copletData);
                            cid.setAttribute("item-content", IOUtil.toByteArray(source.getInputStream()));
                        } catch (IOException se) {
                            this.getLogger().warn("Unable to get content for " + url, se);
                        } catch (ServiceException se) {
                            this.getLogger().warn("Unable to get source resolver.", se);
                        } finally {
                            if ( source != null ) {
                                resolver.release(source);
                            }
                            this.manager.release(resolver);
                        }
                        
                    } else {
                        CopletData copletData = service.getComponentManager().getProfileManager().getCopletInstanceData(ci.getCopletId()).getCopletData();
                        cid = service.getComponentManager().getCopletFactory().newInstance(copletData);
                        Map attributes = (Map) ci.getAttribute("coplet-attributes");
                        Iterator i = attributes.entrySet().iterator();
                        while ( i.hasNext() ) {
                            Map.Entry entry = (Map.Entry)i.next();
                            cid.setAttribute(entry.getKey().toString(), entry.getValue());
                        }
                    }
                }
                layout.setCopletInstanceData(cid);
            } catch (ProcessingException pe) {
                this.getLogger().warn("Unable to create new instance.", pe);
            } catch (ServiceException se) {
                this.getLogger().warn("Unable to lookup portal service.", se);
            } finally {
                this.manager.release(service);
            }
        }
    }

    /**
     * Show the selected basket
     */
    protected void processShowBasketEvent(ShowBasketEvent event, Session session) {
        Basket basket = this.loadBasket( event.getBasketId() );
        session.setAttribute(BASKET_KEY, basket);
    }
    /**
     * Cleaning a basket or all
     * @param event   The triggering event
     * @param session The session
     */
    protected void processCleanBasketEvent(CleanBasketEvent event, Session session) {
        String basketId = event.getBasketId();
        List baskets = (List)session.getAttribute(ALL_BASKETS_KEY);
        if ( basketId == null) {
            // remove all baskets
            if ( baskets != null ) {
                Iterator i = baskets.iterator();
                while (i.hasNext()) {
                    BasketDescription entry = (BasketDescription)i.next();
                    this.deleteBasket(entry.id);
                }
                session.removeAttribute(ALL_BASKETS_KEY);
            }
        } else {
            // remove one basket
            this.deleteBasket(basketId);
            if ( baskets != null ) {
                Iterator i = baskets.iterator();
                boolean found = false;
                while (i.hasNext() && !found) {
                    BasketDescription entry = (BasketDescription)i.next();
                    if ( entry.id.equals(basketId)) {
                        found = true;
                        i.remove();
                    }
                }                    
            }
        }
    }

    /**
     * This method processes removing one item from the basket
     * @param event The event triggering the action
     * @param basket The basket
     */
    protected void processRemoveItemEvent(RemoveItemEvent event, Basket basket) {
        Object item = event.getItem();
        basket.removeItem(item);
    }

    /**
     * This method processes adding one item to the basket
     * @param event The event triggering the action
     * @param basket The basket
     */
    protected void processAddItemEvent(AddItemEvent event, Basket basket) {
        Object item = event.getItem();
        if ( item instanceof ContentItem ) {
            ContentItem ci = (ContentItem)item;
            boolean found = false;
            //Iterator i = basket.getIterator();
            // while ( i.hasNext() && ! found ) {
            //    Object next = i.next();
            //    if ( next instanceof ContentItem ) {
            //        found = ((ContentItem)next).equalsItem(ci);
            //    }
            //}
            if (!found) {
                basket.addItem(ci);
                if ( ci.isContent() ) {
                    SourceResolver resolver = null;
                    Source source = null;
                    String url = null;
                    try {
                        resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
                        url = ci.getURL();
                        if ( url == null ) {
                            url = "coplet://" + ci.getCopletId();
                        }
                        source = resolver.resolveURI(url);
                        ci.setContent(IOUtil.toByteArray(source.getInputStream()));
                    } catch (IOException se) {
                        this.getLogger().warn("Unable to get content for " + url, se);
                    } catch (ServiceException se) {
                        this.getLogger().warn("Unable to get source resolver.", se);
                    } finally {
                        if ( source != null ) {
                            resolver.release(source);
                        }
                        this.manager.release(resolver);
                    }
                } else if ( ci.getURL() == null ) {
                    // copy coplet attributes
                    PortalService service = null;
                    try {
                        service = (PortalService) this.manager.lookup(PortalService.ROLE);
                        CopletInstanceData cid = service.getComponentManager().getProfileManager().getCopletInstanceData(ci.getCopletId());
                        Map attributes = new HashMap();
                        Iterator i = cid.getAttributes().entrySet().iterator();
                        while ( i.hasNext() ) {
                            Map.Entry entry = (Map.Entry)i.next();
                            attributes.put(entry.getKey(), entry.getValue());
                        }
                        ci.setAttribute("coplet-attributes", attributes);
                    } catch (ServiceException se) {
                        this.getLogger().warn("Unable to lookup portal service.", se);
                    } finally {
                        this.manager.release(service);
                    }
                }
            }
        } else { 
            basket.addItem(item);
        }
    }

    /** 
     * Load the basket for a single user
     * @return The basket or null
     */
    protected Basket loadBasket(String userId) {
        if ( this.directory != null ) {
            File file = new File(this.directory, userId+".basket");
            if ( file.exists() ) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    Basket basket = (Basket)ois.readObject();
                    ois.close();
                    return basket;
                } catch (Exception ignore) {
                    // ignore this
                }
            }
        }
        return null;
    }
    
    /** 
     * Load the basket 
     */
    protected Basket loadBasket() {
        Basket basket = null;
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            RequestState rs = authManager.getState();
            final String user = (String)rs.getHandler().getContext().getContextInfo().get("ID");
            basket = this.loadBasket(user);
        } catch (ProcessingException ignore) {
            // ignore this
        } catch (ServiceException ignore) {
            // ignore this
        }
        if ( basket == null ) {
            try {
                basket = (Basket)ClassUtils.newInstance(this.basketClassName);
            } catch (Exception ignore) {
                basket = new Basket();
            }
        }
        return basket;
    }
    
    /** 
     * Delete the basket for a u ser
     */
    protected void deleteBasket(String userId) {
        if ( this.directory != null ) {
            File file = new File(this.directory, userId+".basket");
            if ( file.exists() ) {
                file.delete();
            }
        }
    }

    /** 
     * Save the basket for a single user
     */
    protected void saveBasket(Basket basket, String userId) {
        if ( this.directory != null ) {
            File file = new File(this.directory, userId+".basket");
            try {
                if ( !file.exists() ) {
                    file.createNewFile();
                    file = new File(this.directory, userId+".basket");
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(basket);
                oos.close();
            } catch (Exception ignore) {
                // ignore this
            }
        }
    }
    
    /**
     * Get baskets of all users
     */
    protected List loadBaskets() {
        if ( this.directory != null ) {
            File directory = new File(this.directory);
            if ( directory.exists()) {
                List baskets = new ArrayList();
                File[] files = directory.listFiles();
                for(int i=0; i<files.length;i++) {
                    String user = files[i].getName();
                    int pos = user.indexOf(".basket");
                    if ( pos != -1 ) {
                        user = user.substring(0, pos);
                        Basket basket = this.loadBasket(user);
                        if ( basket != null ) {
                            BasketDescription bd = new BasketDescription();
                            bd.id = user;
                            bd.size = basket.contentSize();
                            baskets.add(bd);
                        }
                    }
                }
                return baskets;
            }
        }
        return null;
    }
    
    /** 
     * Save the basket 
     */
    protected void saveBasket(Basket basket) {
        if ( basket != null ) {
            AuthenticationManager authManager = null;
            try {
                authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
                RequestState rs = authManager.getState();
                final String user = (String)rs.getHandler().getContext().getContextInfo().get("ID");
                this.saveBasket(basket, user);
            } catch (ProcessingException ignore) {
                // ignore this
            } catch (ServiceException ignore) {
                // ignore this
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBasket()
     */
    public Basket getBasket() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        Basket basket = (Basket) session.getAttribute(BASKET_KEY);
        if ( basket == null ) {
            basket = this.loadBasket();
            session.setAttribute(BASKET_KEY, basket);
        }
        return basket;
    }
   
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBaskets()
     */
    public List getBaskets() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        List baskets = (List)session.getAttribute(ALL_BASKETS_KEY);
        if ( baskets == null ) {
            baskets = this.loadBaskets();
            if (baskets == null) {
                baskets = new ArrayList();
            }
            session.setAttribute(ALL_BASKETS_KEY, baskets);
        }
        return baskets;
    }    
    
}
