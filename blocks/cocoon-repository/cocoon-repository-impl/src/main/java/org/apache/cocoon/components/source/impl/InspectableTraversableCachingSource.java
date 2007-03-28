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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceProperty;

/**
 * TraversableCachingSource that adds support for SourceProperty caching.
 */
public class InspectableTraversableCachingSource extends TraversableCachingSource
                                                 implements InspectableSource {

    public InspectableTraversableCachingSource(final CachingSourceFactory factory,
                                               final String protocol,
                                               final String uri,
                                               final String sourceUri,
                                               InspectableSource source,
                                               int expires,
                                               String cacheName,
                                               boolean async,
                                               CachingSourceValidityStrategy validityStrategy, 
                                               boolean fail) {
        super(factory, protocol, uri, sourceUri, (TraversableSource) source, expires, cacheName, async, validityStrategy, fail);
    }

    private InspectableSource getInspectableSource() {
        return (InspectableSource) super.source;
    }

    private InspectableSourceMeta getInspectableResponseMeta() throws IOException {
        return (InspectableSourceMeta) getResponseMeta();
    }


    public SourceProperty getSourceProperty(String namespace, String name) throws SourceException {
        final InspectableSourceMeta meta;
        try {
            meta = getInspectableResponseMeta();
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
            return null;
        }

        SourceProperty property = meta.getSourceProperty(namespace, name);
        if (property == null) {
            // In the case of webdav the source cannot
            // determine all available properties beforehand.
            // Therefore, although we initialized the cached
            // response by calling getSourceProperties(),
            // this does not mean this particular property
            // was returned and cached. Hence we try to
            // get it here still and remember if it was null.
            property = getInspectableSource().getSourceProperty(namespace, name);
            if (property == null) {
                // remember that this property is null
                property = InspectableSourceMeta.NULL_PROPERTY;
            }
            meta.setSourceProperty(property);
        }

        if (InspectableSourceMeta.NULL_PROPERTY.equals(property)) {
            return null;
        }

        return property;
    }

    public void setSourceProperty(SourceProperty property) throws SourceException {
        getInspectableSource().setSourceProperty(property);
        try {
            getInspectableResponseMeta().setSourceProperty(property);
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
        }
    }

    public SourceProperty[] getSourceProperties() throws SourceException {
        try {
            return getInspectableResponseMeta().getSourceProperties();
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
            return null;
        }
    }

    public void removeSourceProperty(String namespace, String name) throws SourceException {
        getInspectableSource().removeSourceProperty(namespace, name);
        try {
            getInspectableResponseMeta().removeSourceProperty(namespace, name);
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
        }
    }


    protected SourceMeta readMeta(Source source) throws SourceException {
        return new InspectableSourceMeta(source);
    }


    protected static class InspectableSourceMeta extends TraversableSourceMeta {
        protected static final SourceProperty NULL_PROPERTY = new SourceProperty("cocoon", "isnull");

        private Map properties;

        public InspectableSourceMeta() {
        }

        public InspectableSourceMeta(Source source) throws SourceException {
            super(source);
            setSourceProperties(((InspectableSource) source).getSourceProperties());
        }

        protected SourceProperty getSourceProperty(String namespace, String name) {
            if (properties == null) {
                return null;
            }
            final String key = namespace + "#" + name;
            return (SourceProperty) properties.get(key);
        }

        protected void setSourceProperty(SourceProperty property) {
            if (this.properties == null) {
                this.properties = Collections.synchronizedMap(new HashMap(11));
            }
            final String key = property.getNamespace() + "#" + property.getName();
            properties.put(key, property);
        }

        protected SourceProperty[] getSourceProperties() {
            if (this.properties == null) {
                return null;
            }
            final Collection values = this.properties.values();
            return (SourceProperty[]) values.toArray(new SourceProperty[values.size()]);
        }

        protected void setSourceProperties(SourceProperty[] props) {
            if (this.properties == null) {
                this.properties = Collections.synchronizedMap(new HashMap(props.length));
            }
            for (int i = 0; i < props.length; i++) {
                setSourceProperty(props[i]);
            }
        }

        protected void removeSourceProperty(String namespace, String name) {
            if (this.properties != null) {
                final String key = namespace + "#" + name;
                properties.remove(key);
            }
        }
    }
}
