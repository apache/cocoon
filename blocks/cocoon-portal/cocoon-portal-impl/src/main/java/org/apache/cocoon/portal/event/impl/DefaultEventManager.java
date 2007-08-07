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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.util.AbstractBean;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;

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
 * Receivers can implement the {@link Ordered} interface to specify their priority when it
 * comes to event processing. For example some components need to receive the event first,
 * as they might be used by other components during event processing.
 *
 * @version $Id$
 */
public class DefaultEventManager
    extends AbstractBean
    implements EventManager {

    /** Introspected receiver classes. */
    protected Map receiverClasses = new HashMap();

    /** Map of all event classes and their corresponding receivers. */
    protected Map eventHierarchy = new HashMap();

    /**
     * Initialize this component.
     */
    public void init() {
        // we create a tree of all events - we initialize this with the root class
        this.getHierarchyInfo(Event.class);

        // TODO - Add this as a default bean!
        // subscribe all receivers that are necessary for the portal to work
        this.subscribe(new InternalEventReceiver());
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
                if ( receiverInfo.simpleVersion ) {
                    receiverInfo.method.invoke(receiverInfo.receiver, new Object[] {event});
                } else {
                    receiverInfo.method.invoke(receiverInfo.receiver, new Object[] {event, this.portalService});
                }
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
        public boolean simpleVersion;
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
                    if ( (params.length == 1)
                         || (params.length == 2
                             && params[1].getName().equals(PortalService.class.getName()))) {
                        if ( Event.class.isAssignableFrom( params[0] ) ) {
                            final MethodInfo info = new MethodInfo();
                            info.eventClass = params[0];
                            info.method = current;
                            info.simpleVersion = params.length == 1;
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
            hierarchy.addToReceivers(new ReceiverInfo(receiver, info.method, info.simpleVersion));
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
                Collections.sort(this.receivers, ReceiverInfoComparator.instance);
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

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if ( obj instanceof HierarchyInfo ) {
                return ((HierarchyInfo)obj).className.equals(this.className);
            }
            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.className.hashCode();
        }
    }

    protected static final class ReceiverInfo {
        final public Receiver receiver;
        final public Method   method;
        final public boolean  simpleVersion;

        public ReceiverInfo(Receiver r, Method m, boolean simple) {
            this.receiver = r;
            this.method = m;
            this.simpleVersion = simple;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if ( obj instanceof ReceiverInfo ) {
                return  ((ReceiverInfo)obj).receiver.equals(this.receiver)
                     && ((ReceiverInfo)obj).method.equals(this.method)
                     && ((ReceiverInfo)obj).simpleVersion == this.simpleVersion;
            }
            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.receiver.hashCode() + this.method.hashCode();
        }
    }

    protected static final class ReceiverInfoComparator implements Comparator {

        protected static ReceiverInfoComparator instance = new ReceiverInfoComparator();
        protected OrderComparator orderComparator = new OrderComparator();

        public int compare(Object o1, Object o2) {
            if ( o1 instanceof ReceiverInfo && o2 instanceof ReceiverInfo ) {
                return this.orderComparator.compare(((ReceiverInfo)o1).receiver, ((ReceiverInfo)o2).receiver);
            }
            return 0;
        }
    }
}
