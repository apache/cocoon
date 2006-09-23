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
package org.apache.cocoon.components.accessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * An accessor that handles a <code>Map</code> of accessors.
 * @version $Id$
 */
public class MapAccessor implements Accessor, Configurable, Serviceable, ThreadSafe  {

    /** Reference to the accessors */
    private Map accessors;

    /** The service manager instance */
    private ServiceManager manager;

    /**
     * The accessors that should be part of the map are configured
     * through <code>&lt;element name="the key that will be used in
     * the map" accessor="the name of the accessor"/&gt;</code>.
     *
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        Map accessors = new HashMap();
        Configuration[] children = config.getChildren("element");
        for (int i = 0; i < children.length; i++) {
            String accessor = children[i].getAttribute("accessor");
            // use the accessor name as name if nothing else is given
            String name = children[i].getAttribute("name", accessor);
            accessors.put(name, accessor);
        }
        this.accessors = new AccessorMap(accessors, this.manager);
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get the map of accessors
     * @see org.apache.cocoon.components.accessor.Accessor#getObject()
     */
    public Object getObject() {
        return this.accessors;
    }

    /**
     * Map that finds the accessor at access time.
     */
    public static class AccessorMap extends HashMap {

        private ServiceManager manager;

        public AccessorMap(Map map, ServiceManager manager) {
            super(map);
            this.manager = manager;
        }

        public Object get(Object key) {
            String accessorName = (String)super.get(key);
            if (accessorName == null)
                return null;
            ServiceSelector accessorSelector = null;
            Accessor accessor = null;
            try {
                // Thread safe accessors could be looked up once and be cached
                accessorSelector =
                    (ServiceSelector)this.manager.lookup(Accessor.ROLE + "Selector");
                accessor = (Accessor)accessorSelector.select(accessorName);
                return accessor.getObject();
            } catch (ServiceException e) {
                // FIXME: Don't know if this is the appropriate action
                throw new CascadingRuntimeException("Trying to access non existing acessor: " +
                                           accessorName, e);
            } finally {
                accessorSelector.release(accessor);
                this.manager.release(accessorSelector);
            }
        }

        // The MapAccessor is thread safe so the map should be read only
        public void clear() {
            throw new UnsupportedOperationException("AccessorMap is read only");
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException("AccessorMap is read only");
        }

        public void putAll(Map m) {
            throw new UnsupportedOperationException("AccessorMap is read only");
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException("AccessorMap is read only");
        }
    }
}
