/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.commons.collections.FastHashMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;

import java.io.IOException;
import java.util.Map;

/**
 * Component implementing the {@link CacheManager} role.
 *
 * @version $Id$
 */
public class DefaultCacheManager
        extends AbstractLogEnabled
        implements CacheManager, ThreadSafe, Serviceable, Disposable,
                   Configurable, Component {
// FIXME: Component is there to allow this block to also run in the 2.1 branch

    protected ServiceManager manager;
    protected Configuration configuration;
    protected Map cache;

    public DefaultCacheManager() {
        this.cache = new FastHashMap();
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

    /**
     * Configurable
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public Object get(Source source, String prefix) {
        // Create a cache key
        final String key = prefix + source.getURI();

        // If object is not in the cache then return null
        Object[] objectAndValidity = (Object[]) this.cache.get(key);
        if (objectAndValidity == null) {
            return null;
        }

        // If object is in the cache, check stored object validity
        final SourceValidity validity = (SourceValidity) objectAndValidity[1];
        int valid = validity.isValid();
        if (valid == SourceValidity.UNKNOWN) {
            // Compare against current source validity
            valid = validity.isValid(source.getValidity());
        }

        // If stored object is not valid then remove object from cache and return null
        if (valid != SourceValidity.VALID) {
            this.cache.remove(key);
            return null;
        }

        // If valid then return cached object
        return objectAndValidity[0];
    }

    public void set(Object object, Source source, String prefix) throws IOException {
        final String key = prefix + source.getURI();
        final SourceValidity validity = source.getValidity();
        if (validity != null) {
            Object[] objectAndValidity = {object,  validity};
            this.cache.put(key, objectAndValidity);
        }
    }

    /**
     * Disposable
     */
    public void dispose() {
        this.manager = null;
        this.cache = null;
    }
}
