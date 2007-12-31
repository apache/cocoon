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
package org.apache.cocoon.portal.event.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.Register;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.Deprecation;

/**
 * This is the default implementation of the event manager.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 *
 * @version CVS $Id$
 */
public class DefaultEventManager
    extends AbstractLogEnabled
    implements EventManager,
                Serviceable,
                Initializable,
                ThreadSafe,
                Configurable,
                Disposable,
                Contextualizable,
                Publisher, Register {

    private final String rootEventType = Event.class.getName();
    private Class eventClass;
    /** The list of all subscribers. */
    private List subscribers = new ArrayList();
    /** The list of all receivers */
    private Map receivers = new HashMap();

    private ServiceManager manager;
    private Configuration configuration;

    protected EventAspectChain chain;

    protected ServiceSelector aspectSelector;

    protected Context context;

    /** The portal service */
    protected PortalService service;

    /** Introspected receiver classes */
    protected Map receiverClasses = new HashMap();

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.service = (PortalService)manager.lookup(PortalService.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventManager#getPublisher()
     */
    public Publisher getPublisher() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventManager#getRegister()
     */
    public Register getRegister() {
        return this;
    }

    /**
     * Helper method to get the current object model
     */
    protected Map getObjectModel() {
        return ContextHelper.getObjectModel( this.context );
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        this.configuration = conf;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
            this.manager.release(this.service);
            this.service = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        this.eventClass = Class.forName( this.rootEventType );
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Initialising eventClass " + this.eventClass);
        }

        // FIXME - the following configuration is not portal specific, it's global!
        // subscribe all configured roles
        Configuration roles = this.configuration.getChild("subscriber-roles", false);
        if ( roles != null ) {
            Configuration[] rolesConf = roles.getChildren("role");
            for(int i=0; i<rolesConf.length;i++) {
                final Configuration current = rolesConf[i];
                final String name = current.getAttribute("name");

                Subscriber subscriber = null;
                try {
                    subscriber = (Subscriber) this.manager.lookup(name);
                    Deprecation.logger.warn("Subscriber is deprecated. Please convert the following component to a Receiver: " + subscriber.getClass().getName());
                    this.subscribe(subscriber);
                } finally {
                    this.manager.release(subscriber);
                }
            }
        }
        // subscribe all configured classes
        Configuration classes = this.configuration.getChild("subscriber-classes", false);
        if ( classes != null ) {
            Configuration[] classesConf = classes.getChildren("class");
            for(int i=0; i<classesConf.length;i++) {
                final Configuration current = classesConf[i];
                final String name = current.getAttribute("name");

                Deprecation.logger.warn("Subscriber is deprecated. Please convert the following component to a Receiver: " + name);
                Subscriber subscriber = (Subscriber) ClassUtils.newInstance(name);
                ContainerUtil.enableLogging(subscriber, this.getLogger());
                ContainerUtil.contextualize(subscriber, this.context);
                ContainerUtil.service(subscriber, this.manager );
                ContainerUtil.initialize(subscriber);
                this.subscribe(subscriber);
            }
        }
        // subscribe all configured receiver roles
        roles = this.configuration.getChild("receiver-roles", false);
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
        classes = this.configuration.getChild("receiver-classes", false);
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

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Publisher#publish(org.apache.cocoon.portal.event.Event)
     */
    public void publish( final Event event ) {
        this.send(event);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Register#subscribe(org.apache.cocoon.portal.event.Subscriber)
     */
    public void subscribe( final Subscriber subscriber ) {
        if ( !this.eventClass.isAssignableFrom( subscriber.getEventType() ) ) {
            throw new RuntimeException("Invalid event type " + subscriber.getEventType()
                                      +" for subscriber " + subscriber);
        }

        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Subscribing event " + subscriber.getEventType().getName() );
        }

        // Add to list but prevent duplicate subscriptions
        if ( !this.subscribers.contains( subscriber ) ) {
            this.subscribers.add( subscriber );
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Subscribed Event " + subscriber.getEventType().getName() );
                this.getLogger().debug( "Subscribers now active: " + this.subscribers.size() );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Register#unsubscribe(org.apache.cocoon.portal.event.Subscriber)
     */
    public void unsubscribe( Subscriber subscriber ) {

        if ( !this.eventClass.isAssignableFrom( subscriber.getEventType() ) ) {
            throw new RuntimeException("Invalid event type " + subscriber.getEventType()
                    +" for unsubscribing " + subscriber);
        }
        if ( this.subscribers.contains( subscriber ) ) {
            this.subscribers.remove( subscriber );
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Unsubscribed Event " + subscriber.getEventType().getName() );
                this.getLogger().debug( "Subscribers now active: " + this.subscribers.size() );
            }
        } else {
            this.getLogger().warn( "Subscriber " + subscriber + " not found" );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.EventManager#processEvents()
     */
    public void processEvents()
    throws ProcessingException {
        if ( this.configuration != null ) {
            synchronized ( this ) {
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
            }
        }
        DefaultEventAspectContext context = new DefaultEventAspectContext(this.chain);
        EventConverter converter = null;
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            converter = (EventConverter) this.manager.lookup(EventConverter.ROLE);
            Publisher publisher = this.getPublisher();

            converter.start();

            // Invoke aspects
            context.setEventPublisher( publisher );
            context.setObjectModel(this.getObjectModel());
            context.setEventConverter(converter);
            context.invokeNext( service );

            converter.finish();

        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup component.", ce);
        } finally {
            this.manager.release(converter);
            this.manager.release(service);
        }

    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context)
    throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#send(org.apache.cocoon.portal.event.Event)
     */
    public void send(Event event) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Publishing event " + event.getClass());
        }
        for ( Iterator e = this.subscribers.iterator(); e.hasNext(); ){
            Subscriber subscriber = (Subscriber)e.next();
            if (subscriber.getEventType().isAssignableFrom(event.getClass())
            && (subscriber.getFilter() == null || subscriber.getFilter().filter(event))) {
                if ( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().info("Informing subscriber "+subscriber+" of event "+event.getClass());
                }
                subscriber.inform(event);
            }
        }
        for (Iterator re = this.receivers.entrySet().iterator(); re.hasNext(); ) {
            final Map.Entry current = (Map.Entry)re.next();
            final Receiver receiver = (Receiver)current.getKey();
            final List methodInfos = (List)current.getValue();
            boolean found = false;
            final Iterator ci = methodInfos.iterator();
            while ( !found && ci.hasNext() ) {
                final MethodInfo info = (MethodInfo)ci.next();
                if ( info.eventClass.isAssignableFrom(event.getClass()) ) {
                    if ( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().info("Informing receiver "+receiver+" of event "+event.getClass());
                    }
                    try {
                        info.method.invoke(receiver, new Object[] {event, this.service});
                    } catch (Exception ignore) {
                        this.getLogger().warn("Exception during event dispatching on receiver " + receiver
                                             +" and event " + event, ignore);
                    }
                    found = true;
                }
            }
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
                        if ( this.eventClass.isAssignableFrom( params[0] ) ) {
                            MethodInfo info = new MethodInfo();
                            info.eventClass = params[0];
                            info.method = current;
                            result.add(info);
                        }
                    }
                }
            }
            if ( result.size() == 0 ) {
                result = null;
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
        List eventClassesForReceiver = (List)this.receivers.get(receiver);
        if ( eventClassesForReceiver == null ) {
            this.receivers.put(receiver, infos);
        }
        if ( this.getLogger().isDebugEnabled() ) {
            for(int i=0; i<infos.size();i++) {
                this.getLogger().debug( "Receiver " + receiver + " subscribed for event: " + ((MethodInfo)infos.get(i)).eventClass.getName() );
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.EventManager#unsubscribe(org.apache.cocoon.portal.event.Receiver)
     */
    public void unsubscribe(Receiver receiver) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Receiver " + receiver + " unsubscribed.");
        }
        this.receivers.remove(receiver);
    }

}
