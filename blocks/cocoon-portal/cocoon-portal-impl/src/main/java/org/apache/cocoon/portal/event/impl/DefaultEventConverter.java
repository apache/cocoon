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
import org.apache.cocoon.portal.impl.AbstractBean;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.HashUtil;

/**
 * This implementation stores the events that can't be converted to strings (which don't
 * support the {@link org.apache.cocoon.portal.event.ConvertableEvent} interface) in
 * the current user session. Each event is then converted to a string just containing
 * the index of the event in this list. This list is cleared when the session is closed.
 *
 * In addition, this component can be configured with additional event converters
 * that convert a specific event.
 *
 * TODO - What happens if two event classes have the same hash?
 *
 * @version $Id$
 */
public class DefaultEventConverter
    extends AbstractBean
    implements EventConverter {

    protected static final String EVENT_LIST = DefaultEventConverter.class.getName();

    /** All factories mapped by a key.
     * The key is either the hash if the class name or a configured mapping.
     * If there is no configured mapping for an event class, the value
     * is the constructor to construct the event.
     * If there is a configured mapping, then this map contains two entries:
     * The first entry has the hash of the class name as the key and the value
     * is the configured mapping name. The second entry has the configured mapping
     * name as the key and the Constructor as the value.
     */
    protected Map factories = new HashMap();

    /** All configured event converters. */
    protected Map converters = new HashMap();

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
                final long hash = HashUtil.hash(current.getValue().toString());
                final String hashKey = Long.toString(hash);
                this.factories.put(hashKey, current.getKey().toString());
                this.factories.put(current.getKey().toString(), c);
            }
        }
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
    public void configure(Configuration config) throws ConfigurationException {
        Configuration[] mappings = config.getChild("mappings").getChildren("mapping");
        for( int i=0; i<mappings.length; i++) {
            final Configuration current = mappings[i];
            final String key = current.getAttribute("name");
            final String eventClass = current.getAttribute("event-class");
            final Constructor c = this.getConstructor(eventClass);
            final long hash = HashUtil.hash(eventClass);
            final String hashKey = Long.toString(hash);
            this.factories.put(hashKey, key);
            this.factories.put(key, c);
        }
        Configuration[] helpers = config.getChild("converters").getChildren("converter");
        for( int i=0; i<helpers.length; i++) {
            final Configuration current = helpers[i];
            final String role = current.getAttribute("role");
            final String eventClass = current.getAttribute("event-class");
            final String name = current.getAttribute("name", null);
            try {
                final EventConverter converter = (EventConverter)this.manager.lookup(role);
                final long hash = HashUtil.hash(eventClass);
                final String hashKey = Long.toString(hash);
                if ( name != null ) {
                    this.factories.put(name, converter);
                    this.factories.put(hashKey, name);                    
                } else {
                    this.factories.put(hashKey, converter);                    
                }
                this.converters.put(eventClass, converter);
            } catch (ServiceException e) {
                throw new ConfigurationException("Unable to lookup event converter: " + role, e);
            }
        }
    }
    */

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#dispose()
    public void dispose() {
        if ( this.manager != null ) {
            final Iterator i = this.converters.values().iterator();
            while ( i.hasNext() ) {
                this.manager.release(i.next());
            }
        }
        super.dispose();
    }
     */

    protected Constructor getConstructor(String factory) {
        try {
            final Class factoryClass = ClassUtils.loadClass(factory);
            return factoryClass.getConstructor(new Class[] {PortalService.class, String.class});
        } catch (NoSuchMethodException nsme) {
            throw new PortalRuntimeException("Factory class does not provide required constructor: " + factory, nsme);
        } catch (ClassNotFoundException cnfe) {
            throw new PortalRuntimeException("Factory class can't be loaded: " + factory, cnfe);
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.EventConverter#encode(org.apache.cocoon.portal.event.Event)
     */
    public String encode(Event event) {
        final String eventClassName = event.getClass().getName();
        String data = null;
        // first check if we have a converter registered for this event
        EventConverter registeredConverter = (EventConverter)this.converters.get(eventClassName);
        if ( registeredConverter != null ) {
            data = registeredConverter.encode(event);
        }
        // if this is a convertable event just return
        // the used factory and the data.
        if ( data == null && event instanceof ConvertableEvent ) {
            data = ((ConvertableEvent)event).asString();
        }
        // check if *this* event is convertable
        if ( data != null ) {
            final long hash = HashUtil.hash(eventClassName);
            final String hashKey = Long.toString(hash);
            Object o = this.factories.get(hashKey);
            if ( o == null ) {
                final Constructor c = this.getConstructor(eventClassName);
                this.factories.put(hashKey, c);
                o = c;
            }
            if ( o instanceof Constructor ) {
                return hashKey + ':' + data;
            } else if ( o instanceof EventConverter ) {
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
                // if the value is a constructor or an event converter we simply use it
                // if the value is a string we simply use this string
                // to lookup the constructor/event converter (another lookup in the map).
                if ( o instanceof String ) {
                    o = this.factories.get(o);
                }
                if ( o instanceof Constructor) {
                    Constructor c = (Constructor)o;
                    try {
                        return (Event)c.newInstance(new Object[] {this.portalService, data});
                    } catch (InstantiationException ie) {
                        ie.printStackTrace();
                        // we ignore this
                    } catch (InvocationTargetException ite) {
                        ite.printStackTrace();
                        // we ignore this                    
                    } catch (IllegalAccessException iae) {
                        iae.printStackTrace();
                        // we ignore this                    
                    }
                } else if ( o instanceof EventConverter ) {
                    EventConverter converter = (EventConverter)o;
                    return converter.decode(data);
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
