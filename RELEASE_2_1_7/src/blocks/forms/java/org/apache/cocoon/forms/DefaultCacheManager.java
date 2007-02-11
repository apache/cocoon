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

import java.io.IOException;

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

/**
 * Component implementing the {@link CacheManager} role.
 *
 * @version $Id$
 */
public class DefaultCacheManager
  extends AbstractLogEnabled
  implements CacheManager, ThreadSafe, Serviceable, Disposable, Configurable, Component {

    protected ServiceManager manager;
    protected Configuration configuration;
    protected FastHashMap cache = new FastHashMap();

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
        String key = prefix + source.getURI();
        SourceValidity newValidity = source.getValidity();

        // If source is not valid then remove object from cache and return null
        if (newValidity == null) {
            this.cache.remove(key);
            return null;
        }

        // If object is not in cache then return null
        Object[] objectAndValidity = (Object[]) this.cache.get(key);
        if (objectAndValidity == null) {
            return null;
        }

        // Check stored validity against current source validity
        SourceValidity storedValidity = (SourceValidity) objectAndValidity[1];
        int valid = storedValidity.isValid();
        boolean isValid;
        if (valid == SourceValidity.UNKNOWN) {
            valid = storedValidity.isValid(newValidity);
            isValid = (valid == SourceValidity.VALID);
        } else {
            isValid = (valid == SourceValidity.VALID);
        }

        // If stored object is not valid then remove object from cache and return null
        if (!isValid) {
            this.cache.remove(key);
            return null;
        }

        // If valid then return cached object
        return objectAndValidity[0];
    }

    public void set(Object object, Source source, String prefix) throws IOException {
        String key = prefix + source.getURI();
        SourceValidity validity = source.getValidity();
        if (validity != null) {
            Object[] objectAndValidity = {object,  validity};
            cache.put(key, objectAndValidity);
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
