/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.util.ClassUtils;

/**
 * This is the default implementation of the event manager.
 * For each event class (interface and impl.) a {@link DefaultEventManager.HierarchyInfo} is created.
 * This helper object contains all subscribed receivers for the event and *all*
 * parents being it interfaces or classes. This makes adding new receivers
 * very easy: we just get the hierarchy info for the event in question and then
 * add the receiver.
 * Sending events is also very easy, we lookup the hierarchy info for the event,
 * get all receivers for this event and notify them. Finally we iterate over
 * all parents and notify the receivers of the all the parents.
 * The simplicity in subscribing and sending of events comes with the drawback that
 * unsubscribing is more costly.
 *
 * @version $Id$
 */
public class DefaultEventManager 
    extends AbstractComponent
    implements EventManager, 
               Configurable {

    /** Our configuration. */
    protected Configuration configuration;

    protected EventAspectChain chain;

    protected ServiceSelector aspectSelector;

    /** Introspected receiver classes. */
    protected Map receiverClasses = new HashMap();

    /** Map of all event classes and their corresponding receivers. */
    protected Map eventHierarchy = new HashMap();

    /**
     * Helper method to get the current object model 
     */
    protected Map getObjectModel() {
        return ContextHelper.getObjectModel( this.context );
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) 
    throws ConfigurationException {
        this.configuration = conf;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        super.initialize();
        // we create a tree of all events - we initialize this with the root class
        this.getHierarchyInfo(Event.class);

        // subscribe all configured receiver roles
        Configuration roles = this.configuration.getChild("receiver-roles", false);
        if ( roles != null ) {
            Configuration[] rolesConf = roles.getChildren("role");
            for(int i=0; i<rolesConf.length;i++) {
                final Configuration current = rolesConf[i];
                final String name = current.getAttribute("name");
                
                Receiver receiver = null;
                try {
                    receiver = (Receiver) this.manager.lookup(name);
                    this.subscribe(receiver);
                } finally {
                    this.manager.release(receiver);
                }
            }
        }
        // subscribe all configured receiver classes
        Configuration classes = this.configuration.getChild("receiver-classes", false);
        if ( classes != null ) {
            Configuration[] classesConf = classes.getChildren("class");
            for(int i=0; i<classesConf.length;i++) {
                final Configuration current = classesConf[i];
                final String name = current.getAttribute("name");
                
                Receiver receiver = (Receiver)ClassUtils.newInstance(name);
                ContainerUtil.enableLogging(receiver, this.getLogger());
                ContainerUtil.contextualize(receiver, this.context);
                ContainerUtil.service(receiver, this.manager );
                ContainerUtil.initialize(receiver);
                this.subscribe(receiver);
            }
        }

        // subscribe all receivers that are necessary for the portal to work
        this.subscribe(new InternalEventReceiver());
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#processEvents()
     */
    public void processEvents()
    throws ProcessingException {
        if ( this.configuration != null ) {
            try {
                this.aspectSelector = (ServiceSelector) this.manager.lookup( EventAspect.ROLE+"Selector");
                this.chain = new EventAspectChain();
                this.chain.configure(this.aspectSelector, this.configuration.getChild("event-aspects"));
            } catch (ConfigurationException ce) {
                throw new ProcessingException("Unable configure component.", ce);
            } catch (ServiceException ce) {
                throw new ProcessingException("Unable to lookup component.", ce);
            }
            this.configuration = null;
        }
        DefaultEventAspectContext context = new DefaultEventAspectContext(this.chain);
        EventConverter converter = null;
        try {
            converter = (EventConverter) this.manager.lookup(EventConverter.ROLE);

            converter.start();

            // Invoke aspects
            context.setObjectModel(this.getObjectModel());
            context.setEventConverter(converter);
            context.invokeNext( this.portalService );

            converter.finish();

        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup component.", ce);
        } finally {
            this.manager.release(converter);
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#send(org.apache.cocoon.portal.event.Event)
     */
    public void send(Event event) {
        if ( getLogger().isDebugEnabled() ) {
            getLogger().debug("Publishing event " + event);
        }
        final HierarchyInfo info = this.getHierarchyInfo(event.getClass());
        this.send(event, info, true);
    }

    protected void send(Event event, HierarchyInfo hierarchy, boolean recursive) {
        // first call listeners for parents
        if ( recursive ) {
            final Iterator parentIterator = hierarchy.getParents().iterator();
            while ( parentIterator.hasNext() ) {
                final HierarchyInfo current = (HierarchyInfo)parentIterator.next();
                this.send(event, current, false);
            }
        }
        final List receiverInfos = hierarchy.getReceiverInfos();
        int index = 0;
        // we don't use an iterator to avoid problems if a receiver is added while we're sending
        // events.
        while ( index < receiverInfos.size() ) {
            final ReceiverInfo receiverInfo = (ReceiverInfo)receiverInfos.get(index);
            if ( getLogger().isDebugEnabled() ) {
                getLogger().info("Informing receiver "+receiverInfo.receiver+" of event "+event.getClass());
            }
            try {
                receiverInfo.method.invoke(receiverInfo.receiver, new Object[] {event, this.portalService});
            } catch (Exception ignore) {
                this.getLogger().warn("Exception during event dispatching on receiver " + receiverInfo.receiver
                                     +" and event " + event, ignore);
            }
            index++;
        }
    }

    protected static final class MethodInfo {
        public Class eventClass;
        public Method method;
    }

    protected synchronized List introspect(Class receiverClass) {
        List result = (List)this.receiverClasses.get(receiverClass.getName());
        if ( result == null ) {
            result = new ArrayList();
            Method[] methods = receiverClass.getMethods();
            for(int i=0; i<methods.length; i++ ) {
                final Method current = methods[i];
                if ( current.getName().equals("inform") ) {
                    final Class[] params = current.getParameterTypes();
                    if ( params.length == 2 
                         && params[1].getName().equals(PortalService.class.getName())) {
                        if ( Event.class.isAssignableFrom( params[0] ) ) {
                            MethodInfo info = new MethodInfo();
                            info.eventClass = params[0];
                            info.method = current;
                            result.add(info);
                            // create the hierarchy info for the event class
                            this.getHierarchyInfo(info.eventClass);
                        }
                    }
                }
            }
            if ( result.size() == 0 ) {
                result = null;
            } else {
                this.receiverClasses.put(receiverClass.getName(), result);
            }
        }
        return result;
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#subscribe(org.apache.cocoon.portal.event.Receiver)
     */
    public void subscribe(Receiver receiver) {
        List infos = this.introspect(receiver.getClass());
        if ( infos == null ) {
            throw new RuntimeException("Invalid event receiver type: " + receiver);
        }

        // Add to list but prevent duplicate subscriptions
        final Iterator i = infos.iterator();
        while ( i.hasNext() ) {
            final MethodInfo info = (MethodInfo)i.next();
            final HierarchyInfo hierarchy = this.getHierarchyInfo(info.eventClass);
            hierarchy.addToReceivers(new ReceiverInfo(receiver, info.method));
            if ( getLogger().isDebugEnabled() ) {
                getLogger().debug( "Receiver " + receiver + " subscribed for event: " + info.eventClass.getName() );
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#unsubscribe(org.apache.cocoon.portal.event.Receiver)
     */
    public void unsubscribe(Receiver receiver) {
        // unsubscribing is a costly operation
        List infos = this.introspect(receiver.getClass());
        if ( infos != null ) {
            final Iterator i = infos.iterator();
            while ( i.hasNext() ) {
                final MethodInfo info = (MethodInfo)i.next();
                final HierarchyInfo hierarchy = this.getHierarchyInfo(info.eventClass);
                hierarchy.removeFromReceivers(receiver);
                if ( getLogger().isDebugEnabled() ) {
                    getLogger().debug( "Receiver " + receiver + " unsubscribed from event: " + info.eventClass.getName() );
                }
            }
        }
    }

    /**
     * Create a hierarchy information.
     */
    protected HierarchyInfo getHierarchyInfo(Class c) {
        final String className = c.getName();
        HierarchyInfo info = (HierarchyInfo) this.eventHierarchy.get(className);
        if ( info == null ) {
            info = new HierarchyInfo(className);

            final Class parent = c.getSuperclass();
            this.addToHierarchy(info, parent);

            Class[] interfaces = c.getInterfaces();
            for (int i=0; i<interfaces.length; i++) {
                this.addToHierarchy(info, interfaces[i]);
            }
            this.eventHierarchy.put(className, info);
        }
        return info;
    }

    protected void addToHierarchy(HierarchyInfo info, Class c) {
        if ( c != null && Event.class.isAssignableFrom(c) ) {
            final HierarchyInfo parent = this.getHierarchyInfo(c);
            info.addToParents(parent);
            // now add all parents from the parent :)
            this.addToHierarchy(info, parent.getParents());
        }
    }

    protected void addToHierarchy(HierarchyInfo info, List parents) {
        final Iterator i = parents.iterator();
        while ( i.hasNext() ) {
            final HierarchyInfo current = (HierarchyInfo)i.next();
            info.addToParents(current);
            this.addToHierarchy(info, current.getParents());
        }        
    }

    protected static final class HierarchyInfo {

        protected final String className;
        protected List receivers;
        protected List parents;

        public HierarchyInfo(String className) {
            this.className = className;
        }

        public void removeFromReceivers(Receiver receiver) {
            if ( this.receivers != null ) {
                final Iterator i = this.receivers.iterator();
                while ( i.hasNext() ) {
                    final ReceiverInfo current = (ReceiverInfo)i.next();
                    if ( current.receiver.equals(receiver) ) {
                        i.remove();
                        return;
                    }
                }
                this.receivers.remove(receiver);
            }
        }

        public synchronized void addToReceivers(ReceiverInfo r) {
            if ( this.receivers == null ) {
                this.receivers = new ArrayList();
            }
            if ( !this.receivers.contains(r)) {
                this.receivers.add(r);
            }
        }

        public void addToParents(HierarchyInfo i) {
            if ( this.parents == null ) {
                this.parents = new ArrayList();
            }
            if ( !this.parents.contains(i) ) {
                this.parents.add(i);
            }
        }

        public List getReceiverInfos() {
            if ( this.receivers == null ) {
                return Collections.EMPTY_LIST;
            }
            return this.receivers;
        }

        public List getParents() {
            if ( this.parents == null ) {
                return Collections.EMPTY_LIST;
            }
            return this.parents;
        }
    }

    protected static final class ReceiverInfo {
        final public Receiver receiver;
        final public Method   method;

        public ReceiverInfo(Receiver r, Method m) {
            this.receiver = r;
            this.method = m;
        }
    }
}
