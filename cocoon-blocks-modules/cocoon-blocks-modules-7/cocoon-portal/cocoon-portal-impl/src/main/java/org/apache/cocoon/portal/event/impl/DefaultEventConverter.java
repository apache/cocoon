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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This implementation stores the events that can't be converted to strings (which don't
 * support the {@link org.apache.cocoon.portal.event.ConvertableEvent} interface) in
 * the current user session. Each event is then converted to a string just containing
 * the index of the event in this list. This list is cleared when the session is closed.
 *
 * Each event consists of its class name and its data. This event converter concatenates both
 * information to one string. As the class name is usual rather long, this component
 * can be configured with a mapping from a unique (short) key to the class name.
 * If no mapping is configured for an event, a hash of the class name is used as the
 * hash is usually shorter than the class name. We use a hash as we need a reproducable way
 * of mapping class names for session sharing.
 * It is advisable to map all event class to custom keys instead of using a hash.
 *
 * @version $Id$
 */
public class DefaultEventConverter
    extends AbstractBean
    implements EventConverter {

    protected static final String EVENT_LIST = DefaultEventConverter.class.getName();

    /** All factories mapped by a key.
     * The key is either the hash of the class name or a configured mapping.
     * If there is no configured mapping for an event class, the value
     * is the constructor of the event class to construct the event.
     * If there is a configured mapping, then this map contains two entries:
     * The first entry has the hash of the class name as the key and the value
     * is the configured mapping name. The second entry has the configured mapping
     * name as the key and the Constructor as the value.
     */
    protected Map factories = new HashMap();

    /** The configured mappings. */
    protected Map mappings;

    /**
     * Set the mappings.
     * A mapping is a key value pair consisting of a name as the key and a class
     * name as a value.
     */
    public void setMappings(Map m) {
        this.mappings = m;
    }

    /**
     * Initialize this component.
     */
    public void init() {
        if ( this.mappings != null ) {
            final Iterator i = this.mappings.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                final Constructor c = this.getConstructor(current.getValue().toString());
                final long hash = current.getValue().hashCode();
                final String hashKey = Long.toString(hash);
                // check for duplicate hash code
                if ( this.factories.containsKey(hashKey) ) {
                    throw new PortalRuntimeException("Duplicate hash code (" + hashKey + ") for classes '" + current.getValue() + "' and '" +
                                                     this.factories.get(this.factories.get(hashKey)) + "'.");
                }
                // check for duplicate mapping
                if ( this.factories.containsKey(current.getKey().toString()) ) {
                    throw new PortalRuntimeException("Duplicate mapping for key '" + current.getKey() + "'.");
                }
                this.factories.put(hashKey, current.getKey().toString());
                this.factories.put(current.getKey().toString(), c);
            }
        }
    }

    /**
     * Try to get the constructor for a given event class.
     * The constructor should be a two argument constructor gettint the portal service and the data string.
     */
    protected Constructor getConstructor(String eventClass) {
        try {
            final Class factoryClass = Thread.currentThread().getContextClassLoader().loadClass(eventClass);
            return factoryClass.getConstructor(new Class[] {PortalService.class, String.class});
        } catch (NoSuchMethodException nsme) {
            throw new PortalRuntimeException("Factory class does not provide required constructor: " + eventClass, nsme);
        } catch (ClassNotFoundException cnfe) {
            throw new PortalRuntimeException("Factory class can't be loaded: " + eventClass, cnfe);
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.EventConverter#encode(org.apache.cocoon.portal.event.Event)
     */
    public String encode(Event event) {
        String data = null;
        // if this is a convertable event just return
        // the used factory and the data.
        if ( event instanceof ConvertableEvent ) {
            data = ((ConvertableEvent)event).asString();
        }
        // check if *this* event is convertable
        if ( data != null ) {
            final String eventClassName = event.getClass().getName();
            final long hash = eventClassName.hashCode();
            final String hashKey = Long.toString(hash);
            Object o = this.factories.get(hashKey);
            if ( o == null ) {
                final Constructor c = this.getConstructor(eventClassName);
                if ( this.getLogger().isWarnEnabled() ) {
                    this.getLogger().warn("No mapping defined for event class: '" + eventClassName + "'.");
                }
                this.factories.put(hashKey, c);
                o = c;
            }
            if ( o instanceof Constructor ) {
                if ( !((Constructor)o).getDeclaringClass().getName().equals(eventClassName) ) {
                    throw new PortalRuntimeException("Duplicate hash code (" + hashKey + ") for classes '" + eventClassName + "' and '" +
                            ((Constructor)o).getDeclaringClass().getName() + "'.");
                }
                return hashKey + ':' + data;
            }
            return o.toString() + ':' + data;
        }
        List list = (List)this.portalService.getUserService().getAttribute(EVENT_LIST);
        if ( null == list ) {
            list = new ArrayList();
        }
        int index = list.indexOf(event);
        if ( index == -1 ) {
            list.add(event);
            index = list.size() - 1;
            this.portalService.getUserService().setAttribute(EVENT_LIST, list);
        }
        return String.valueOf(index);
    }

    /**
     * @see org.apache.cocoon.portal.event.EventConverter#decode(java.lang.String)
     */
    public Event decode(String value) {
        if (value != null) {
            // Is this a convertable event?
            int pos = value.indexOf(':');
            if ( pos > 1 ) {
                final String hashKey = value.substring(0, pos);
                final String data = value.substring(pos+1);
                Object o = this.factories.get(hashKey);
                // if the value is a constructor we simply use it
                // if the value is a string we simply use this string
                // to lookup the constructor (another lookup in the map).
                if ( o instanceof String ) {
                    o = this.factories.get(o);
                }
                if ( o instanceof Constructor) {
                    Constructor c = (Constructor)o;
                    try {
                        return (Event)c.newInstance(new Object[] {this.portalService, data});
                    } catch (InstantiationException ie) {
                        // we ignore this
                    } catch (InvocationTargetException ite) {
                        // we ignore this
                    } catch (IllegalAccessException iae) {
                        // we ignore this
                    }
                }
            }
            List list = (List)this.portalService.getUserService().getAttribute(EVENT_LIST);
            if ( null != list ) {
                int index = new Integer(value).intValue();
                if (index < list.size()) {
                    return (Event)list.get(index);
                }
            }
        }
        return null;
    }
}
