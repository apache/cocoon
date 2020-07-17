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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.cocoon.components.cron.CronJob;
import org.apache.cocoon.components.cron.JobScheduler;
import org.apache.cocoon.components.cron.ServiceableCronJob;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplets.basket.events.AddItemEvent;
import org.apache.cocoon.portal.coplets.basket.events.ContentStoreEvent;
import org.apache.cocoon.portal.coplets.basket.events.CleanBriefcaseEvent;
import org.apache.cocoon.portal.coplets.basket.events.MoveItemEvent;
import org.apache.cocoon.portal.coplets.basket.events.RefreshBasketEvent;
import org.apache.cocoon.portal.coplets.basket.events.RemoveItemEvent;
import org.apache.cocoon.portal.coplets.basket.events.ShowBasketEvent;
import org.apache.cocoon.portal.coplets.basket.events.ShowItemEvent;
import org.apache.cocoon.portal.coplets.basket.events.UploadItemEvent;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.servlet.multipart.PartOnDisk;
import org.apache.cocoon.util.ClassUtils;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * This is the implementation of the basket manager
 *
 * @version CVS $Id$
 */
public class BasketManagerImpl
extends AbstractLogEnabled
implements BasketManager, Serviceable, Receiver, Contextualizable, Initializable, Disposable, Parameterizable, ThreadSafe, Component  {
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The component context */
    protected Context context;
    
    /** The configuration for storing baskets */
    protected String directory;
    
    /** The class name of the basket */
    protected String basketClassName = Basket.class.getName();
    
    /** The class name of the briefcase */
    protected String briefcaseClassName = Briefcase.class.getName();

    /** The class name of the folder */
    protected String folderClassName = Folder.class.getName();

    /** All actions for a basket */
    protected List basketActions = new ArrayList();
    
    /** All actions for a briefcase */
    protected List briefcaseActions = new ArrayList();
    
    /** All batches */
    protected List batches = new ArrayList();
    
    /** Scheduler */
    protected JobScheduler scheduler;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.directory = parameters.getParameter("directory", this.directory);
        this.basketClassName = parameters.getParameter("basket-class", this.basketClassName);
        this.briefcaseClassName = parameters.getParameter("briefcase-class", this.briefcaseClassName);
        this.folderClassName = parameters.getParameter("folder-class", this.folderClassName);
        String[] names = parameters.getNames();
        if ( names != null ) {
            for(int i=0; i<names.length; i++) {
                final String current = names[i];
                if ( current.startsWith("basket:action:") ) {
                    final String value = parameters.getParameter(current);
                    final String key = current.substring(14);
                    this.basketActions.add(new ActionInfo(key, value));
                } else if ( current.startsWith("briefcase:action:") ) {
                    final String value = parameters.getParameter(current);
                    final String key = current.substring(17);
                    this.briefcaseActions.add(new ActionInfo(key, value));
                }
            }
        }
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
        this.scheduler = (JobScheduler)this.manager.lookup(JobScheduler.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.scheduler);
            this.scheduler = null;
            this.manager = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        EventManager eventManager = null;
        try {
            eventManager = (EventManager) this.manager.lookup(EventManager.ROLE);
            eventManager.subscribe(this);
        } finally {
            this.manager.release(eventManager);
        }
    }

    /**
     * @see Receiver
     */
    public void inform(ContentStoreEvent event, PortalService service) {
        // dispatch
        final Session session = ContextHelper.getRequest(this.context).getSession();
        if ( event instanceof AddItemEvent ) {
            
            this.processAddItemEvent((AddItemEvent)event);
            
        } else if (event instanceof RemoveItemEvent ){
            
            this.processRemoveItemEvent((RemoveItemEvent)event);
            
        } else if (event instanceof RefreshBasketEvent) {
            
            session.removeAttribute(ALL_BRIEFCASES_KEY);          
            
        } else if (event instanceof CleanBriefcaseEvent) {
            
            this.processCleanBriefcaseEvent((CleanBriefcaseEvent)event, session);
            
        } else if ( event instanceof UploadItemEvent ) {
            
            this.processUploadItemEvent((UploadItemEvent)event);
        } else if ( event instanceof ShowItemEvent ) {
            
            this.processShowItemEvent((ShowItemEvent)event);
        } else if ( event instanceof ShowBasketEvent ) {
            
            this.processShowBasketEvent((ShowBasketEvent)event, session);
        } else if ( event instanceof MoveItemEvent ) {
            ContentStore source = ((MoveItemEvent)event).getContentStore();
            ContentStore target = ((MoveItemEvent)event).getTarget();
            Object item = ((MoveItemEvent)event).getItem();
            source.removeItem(item);
            target.addItem(item);
            this.saveContentStore(source);
            this.saveContentStore(target);
        }
    }

    /**
     * Process an upload and add the item to the content store
     * @param event The event triggering the action
     */
    protected void processUploadItemEvent(UploadItemEvent event) {
        final ContentStore store = event.getContentStore();
        final Request req = ContextHelper.getRequest(this.context);
        final List paramNames = event.getItemNames();
        final Iterator i = paramNames.iterator();
        while ( i.hasNext() ) {
            final String name = (String)i.next();
            final Object o = req.get(name);
            if ( o != null && o instanceof Part) {
                final Part file = (Part)o;
                try {
                    byte[] c = IOUtils.toByteArray(file.getInputStream());
                    ContentItem ci = new ContentItem(file.getFileName(), true);
                    ci.setContent(c);
                    store.addItem(ci);
                } catch (Exception ignore) {
                    // ignore the exception
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
     */
    protected void processShowItemEvent(ShowItemEvent event) {
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
                            cid.setAttribute("item-content", IOUtils.toByteArray(source.getInputStream()));
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
                        final CopletInstanceData original = service.getComponentManager().getProfileManager().getCopletInstanceData(ci.getCopletId());
                        final CopletData copletData = original.getCopletData();
                        cid = service.getComponentManager().getCopletFactory().newInstance(copletData);
                        Map attributes = (Map) ci.getAttribute("coplet-attributes");
                        Iterator i = attributes.entrySet().iterator();
                        while ( i.hasNext() ) {
                            Map.Entry entry = (Map.Entry)i.next();
                            cid.setAttribute(entry.getKey().toString(), entry.getValue());
                        }
                        // now copy the original attributes
                        attributes = original.getAttributes();
                        i = attributes.entrySet().iterator();
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
        Briefcase briefcase = (Briefcase)this.loadContentStore( BRIEFCASE_KEY, event.getBasketId() );
        session.setAttribute(BRIEFCASE_KEY, briefcase);
    }
    
    /**
     * Cleaning a briefcase or all
     * @param event   The triggering event
     * @param session The session
     */
    protected void processCleanBriefcaseEvent(CleanBriefcaseEvent event, Session session) {
        final Briefcase briefcase = (Briefcase)event.getContentStore();
        final List baskets = (List)session.getAttribute(ALL_BRIEFCASES_KEY);
        if ( briefcase == null) {
            // remove all briefcases
            if ( baskets != null ) {
                Iterator i = baskets.iterator();
                while (i.hasNext()) {
                    ContentStoreDescription entry = (ContentStoreDescription)i.next();
                    this.deleteContentStore(BRIEFCASE_KEY, entry.id);
                }
                session.removeAttribute(ALL_BRIEFCASES_KEY);
            }
        } else {
            // remove one briefcase
            this.deleteContentStore(BRIEFCASE_KEY, briefcase.getId());
            if ( baskets != null ) {
                Iterator i = baskets.iterator();
                boolean found = false;
                while (i.hasNext() && !found) {
                    ContentStoreDescription entry = (ContentStoreDescription)i.next();
                    if ( entry.id.equals(briefcase.getId())) {
                        found = true;
                        i.remove();
                    }
                }                    
            }
        }
    }

    /**
     * This method processes removing one item from a content store
     * @param event The event triggering the action
     */
    protected void processRemoveItemEvent(RemoveItemEvent event) {
        final Object item = event.getItem();
        final ContentStore store = event.getContentStore();
        
        store.removeItem(item);
        
        this.saveContentStore(store);
    }

    /**
     * This method processes adding one item to a content store
     * @param event The event triggering the action
     */
    protected void processAddItemEvent(AddItemEvent event) {
        final ContentStore store = event.getContentStore();
        final Object item = event.getItem();
        if ( item instanceof ContentItem ) {
            ContentItem ci = (ContentItem)item;

                if ( ci.isContent() ) {
                    SourceResolver resolver = null;
                    Source source = null;
                    String url = null;
                    try {
                        resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
                        url = ci.getURL();
                        if ( url == null ) {
                        // copy coplet attributes
                        PortalService service = null;
                        try {
                            service = (PortalService) this.manager.lookup(PortalService.ROLE);
                            CopletInstanceData cid = service.getComponentManager().getProfileManager().getCopletInstanceData(ci.getCopletId());
                            url = "coplet://" + ci.getCopletId();
                            Map attributes = new HashMap();
                            Iterator i = cid.getAttributes().entrySet().iterator();
                            while ( i.hasNext() ) {
                                Map.Entry entry = (Map.Entry)i.next();
                                attributes.put(entry.getKey(), entry.getValue());
                            }
                            i = cid.getCopletData().getAttributes().entrySet().iterator();
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
                        source = resolver.resolveURI(url);
                        ci.setContent(IOUtils.toByteArray(source.getInputStream()));
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
                        i = cid.getCopletData().getAttributes().entrySet().iterator();
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
            store.addItem(ci);
        } else { 
            store.addItem(item);
        }
        this.saveContentStore(store);
    }

    /**
     * Save the content store if it is a briefcase or a folder
     */
    protected void saveContentStore(ContentStore store) {
        if ( store instanceof Briefcase ) {
            this.saveContentStore(BRIEFCASE_KEY, store);
        } else if ( store instanceof Folder ) {
            this.saveContentStore(FOLDER_KEY, store);
        }
    }

    /** 
     * Load the content store for a single user
     * @param type The type of the content store (briefcase or folder)
     * @return The content store or null
     */
    protected ContentStore loadContentStore(String type, String userId) {
        if ( this.directory != null ) {
            final String suffix;
            if ( FOLDER_KEY.equals(type) ) {
                suffix = ".folder";
            } else {
                suffix = ".briefcase";
            }
            File file = new File(this.directory, userId + suffix);
            if ( file.exists() ) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    ContentStore store = (ContentStore)ois.readObject();
                    ois.close();
                    return store;
                } catch (Exception ignore) {
                    // ignore this
                }
            }
        }
        return null;
    }
    
    /** 
     * Load a content store
     * @param type The type of the content store (briefcase or folder)
     */
    protected ContentStore loadContentStore(String type) {
        ContentStore store = null;
        String user = this.getUser();
        if ( user != null ) {
            store = this.loadContentStore(type, user);
        }
        if ( store == null && user != null ) {
            try {
                final String clazzName;
                if ( BRIEFCASE_KEY.equals(type) ) {
                    clazzName = this.briefcaseClassName;
                } else {
                    clazzName = this.folderClassName;
                }
                
                final Class clazz = ClassUtils.loadClass(clazzName);
                final Constructor constructor = clazz.getConstructor(new Class[] {String.class});
                
                store = (ContentStore)constructor.newInstance(new Object[] {user});
            } catch (Exception ignore) {
                if ( BRIEFCASE_KEY.equals(type) ) {
                    store = new Briefcase(user);
                } else {
                    store = new Folder(user);
                }
            }
        }
        return store;
    }
    
    /** 
     * Delete the content store for a user
     */
    protected void deleteContentStore(String type, String userId) {
        final String suffix;
        if ( FOLDER_KEY.equals(type) ) {
            suffix = ".folder";
        } else {
            suffix = ".briefcase";
        }
        if ( this.directory != null ) {
            File file = new File(this.directory, userId+suffix);
            if ( file.exists() ) {
                file.delete();
            }
        }
    }

    /** 
     * Save the content store for a single user
     */
    protected void saveContentStore(String type, ContentStore store) {
        final String userId = store.getId();
        final String suffix;
        if ( FOLDER_KEY.equals(type) ) {
            suffix = ".folder";
        } else {
            suffix = ".briefcase";
        }
        
        if ( this.directory != null ) {
            File file = new File(this.directory, userId+suffix);
            try {
                if ( !file.exists() ) {
                    file.createNewFile();
                    file = new File(this.directory, userId+suffix);
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(store);
                oos.close();
            } catch (Exception ignore) {
                // ignore this
            }
        }
    }
    
    /**
     * Get briefcases of all users
     */
    protected List loadBriefcases() {
        if ( this.directory != null ) {
            File directory = new File(this.directory);
            if ( directory.exists()) {
                List briefcases = new ArrayList();
                File[] files = directory.listFiles();
                for(int i=0; i<files.length;i++) {
                    String user = files[i].getName();
                    int pos = user.indexOf(".briefcase");
                    if ( pos != -1 ) {
                        user = user.substring(0, pos);
                        ContentStore store = this.loadContentStore(BRIEFCASE_KEY, user);
                        if ( store != null ) {
                            ContentStoreDescription bd = new ContentStoreDescription();
                            bd.id = user;
                            bd.size = store.contentSize();
                            briefcases.add(bd);
                        }
                    }
                }
                return briefcases;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBasket()
     */
    public Basket getBasket() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        Basket basket = (Basket) session.getAttribute(BASKET_KEY);
        if ( basket == null ) {
            final String user = this.getUser();
            try {
                final Class clazz = ClassUtils.loadClass(this.basketClassName);
                final Constructor constructor = clazz.getConstructor(new Class[] {String.class});
                
                basket = (Basket)constructor.newInstance(new Object[] {user});
            } catch (Exception ignore) {
                basket = new Basket(user);
            }
            session.setAttribute(BASKET_KEY, basket);
        }
        return basket;
    }
   
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBriefcase()
     */
    public Briefcase getBriefcase() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        Briefcase briefcase = (Briefcase) session.getAttribute(BRIEFCASE_KEY);
        if ( briefcase == null ) {
            briefcase = (Briefcase)this.loadContentStore(BRIEFCASE_KEY);
            session.setAttribute(BRIEFCASE_KEY, briefcase);
        }
        return briefcase;
    }
    
    public Folder getFolder() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        Folder folder = (Folder) session.getAttribute(FOLDER_KEY);
        if ( folder == null ) {
            folder = (Folder)this.loadContentStore(FOLDER_KEY);
            session.setAttribute(FOLDER_KEY, folder);
        }
        return folder;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBriefcaseDescriptions()
     */
    public List getBriefcaseDescriptions() {
        Session session = ContextHelper.getRequest(this.context).getSession();
        List briefcases = (List)session.getAttribute(ALL_BRIEFCASES_KEY);
        if ( briefcases == null ) {
            briefcases = this.loadBriefcases();
            if (briefcases == null) {
                briefcases = new ArrayList();
            }
            session.setAttribute(ALL_BRIEFCASES_KEY, briefcases);
        }
        return briefcases;
    }    
    
    /** 
     * Get the current user
     */
    protected String getUser() {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            return service.getComponentManager().getProfileManager().getUser().getUserName();
        } catch (ServiceException ignore) {
            // ignore this
        } finally {
            this.manager.release(service);
        }
        return null;
        
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBasketActions()
     */
    public List getBasketActions() {
        return this.basketActions;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBriefcaseActions()
     */
    public List getBriefcaseActions() {
        return this.briefcaseActions;
        }
        
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#addBatch(org.apache.cocoon.portal.coplets.basket.ContentItem, int, org.apache.cocoon.portal.coplets.basket.BasketManager.ActionInfo)
     */
    public void addBatch(ContentItem item, 
                         int frequencyInDays,
                         ActionInfo action) {
        final String name = action.name + "_" + item;
        
        final BatchInfo info = new BatchInfo();
        info.item = item;
        info.frequencyInSeconds = frequencyInDays * 60 * 60 * 24;
        info.action = action;
        if ( frequencyInDays > 0 ) {
            synchronized (this.batches) {
                BatchInfo old = this.searchBatchInfo(item, action);
                if ( old != null ) {
                    this.batches.remove(old);
                    this.scheduler.removeJob(name);
                }
                this.batches.add(info);
            }
        }
        final Job job = new Job(action.url, item);
        
        try {
            if ( frequencyInDays > 0) {
                this.scheduler.addPeriodicJob(name, job, info.frequencyInSeconds, false, null, null);
            } else {
                this.scheduler.fireJob(job);
            }
                
        } catch (Exception ignore) {
            this.getLogger().warn("Exception during adding of new batch.", ignore);
        }
    }
   
    protected BatchInfo searchBatchInfo(ContentItem item, ActionInfo info) {
        final Iterator i = this.batches.iterator();
        while (i.hasNext()) {
            final BatchInfo current = (BatchInfo)i.next();
            if ( current.item.equals(item) ) {
                if ( current.action.name.equals(info.name) ) {
                    return current;
                }
            }
        }
        return null;
    }
    
    protected static final class BatchInfo {
        public ContentItem item;
        public int frequencyInSeconds;
        public ActionInfo action;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBasketAction(java.lang.String)
     */
    public ActionInfo getBasketAction(String name) {
        final Iterator i = this.basketActions.iterator();
        while ( i.hasNext() ) {
            final ActionInfo current = (ActionInfo)i.next();
            if ( current.name.equals(name) ) {
                return current;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#getBriefcaseAction(java.lang.String)
     */
    public ActionInfo getBriefcaseAction(String name) {
        final Iterator i = this.briefcaseActions.iterator();
        while ( i.hasNext() ) {
            final ActionInfo current = (ActionInfo)i.next();
            if ( current.name.equals(name) ) {
                return current;
            }
        }
        return null;
    }
    
    public static final class Job extends ServiceableCronJob implements CronJob {
        
        protected final String url;
        
        public Job(String url, ContentItem item) {
            final StringBuffer buffer = new StringBuffer(url);
            boolean hasParams = url.indexOf('?') != -1;
            Iterator i = item.attributes.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                final String key = current.getKey().toString();
                if ( !"coplet-attributes".equals(key) ) {
                    if ( hasParams ) {
                        buffer.append('&');
                    } else {
                        buffer.append('?');
                        hasParams = true;
                    }
                    buffer.append(key);
                    buffer.append('=');
                    buffer.append(current.getValue().toString());
                }
            }
            // now add coplet attributes
            Map copletAttributes = (Map)item.attributes.get("coplet-attributes");
            if ( copletAttributes != null ) {
                i = copletAttributes.entrySet().iterator();
                while ( i.hasNext() ) {
                    final Map.Entry current = (Map.Entry)i.next();
                    final String key = current.getKey().toString();
                    if ( hasParams ) {
                        buffer.append('&');
                    } else {
                        buffer.append('?');
                        hasParams = true;
                    }
                    buffer.append(key);
                    buffer.append('=');
                    buffer.append(current.getValue().toString());
                }
            }
            this.url = buffer.toString();
        }    
    
        /* (non-Javadoc)
         * @see org.apache.cocoon.components.cron.CronJob#execute(java.lang.String)
         */
        public void execute(String jobname) {
            SourceResolver resolver = null;
            Source source = null;
            try {
                resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                source = resolver.resolveURI(this.url);

                InputStreamReader r = new InputStreamReader(source.getInputStream());
                try {
                    char[] b = new char[8192];

                    while( r.read(b) > 0) {
                        // nothing to do
                    }

                } finally {
                    r.close();
                }
            } catch (Exception ignore) {
                // we ignore all
                this.getLogger().warn("Exception during execution of job " + jobname, ignore);
            } finally {
                if ( resolver != null ) {
                    resolver.release(source);
                    this.manager.release(resolver);
                }
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplets.basket.BasketManager#update(org.apache.cocoon.portal.coplets.basket.ContentStore)
     */
    public void update(ContentStore store) {
        this.saveContentStore(store);
    }
}
