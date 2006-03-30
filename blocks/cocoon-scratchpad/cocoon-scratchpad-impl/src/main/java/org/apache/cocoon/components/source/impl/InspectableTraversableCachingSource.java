/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

/**
 * TraversableCachingSource that adds support for SourceProperty caching.
 */
public class InspectableTraversableCachingSource extends TraversableCachingSource
implements InspectableSource {

    private InspectableSource isource;

    public InspectableTraversableCachingSource(String protocol,
                                               String uri,
                                               InspectableSource source, 
                                               int expires,
                                               String cacheName,
                                               boolean async,
                                               boolean eventAware) {
        super(protocol, uri, (TraversableSource) source, expires, cacheName, async, eventAware);
        this.isource = source;
    }

    public SourceProperty getSourceProperty(String namespace, String name) throws SourceException {
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
            return null;
        }
        final InspectableSourceMeta imeta = ((InspectableSourceMeta) super.response.getExtra());
        SourceProperty property = imeta.getSourceProperty(namespace, name);
        if (property == null) {
            // In the case of webdav the source cannot
            // determine all available properties beforehand.
            // Therefore, although we initialized the cached
            // response by calling getSourceProperties(),
            // this does not mean this particular property
            // was returned and cached. Hence we try to 
            // get it here still and remember if it was null.
            property = isource.getSourceProperty(namespace, name);
            if (property == null) {
                // remember that this property is null
                property = InspectableSourceMeta.NULL_PROPERTY;
            }
            imeta.setSourceProperty(property);
        }
        if (InspectableSourceMeta.NULL_PROPERTY.equals(property)) {
            return null;
        }
        return property;
    }

    public void setSourceProperty(SourceProperty property) throws SourceException {
        isource.setSourceProperty(property);
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
        }
        final InspectableSourceMeta imeta = ((InspectableSourceMeta) super.response.getExtra());
        imeta.setSourceProperty(property);
    }

    public SourceProperty[] getSourceProperties() throws SourceException {
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
            return null;
        }
        final InspectableSourceMeta imeta = ((InspectableSourceMeta) super.response.getExtra());
        return imeta.getSourceProperties();
    }

    public void removeSourceProperty(String namespace, String name) throws SourceException {
        isource.removeSourceProperty(namespace, name);
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing inspectable response", e);
            }
        }
        final InspectableSourceMeta imeta = ((InspectableSourceMeta) super.response.getExtra());
        imeta.removeSourceProperty(namespace, name);
    }
    
    protected SourceMeta createMeta() {
        return new InspectableSourceMeta();
    }

    protected void initMeta(SourceMeta meta, Source source) throws IOException {
        super.initMeta(meta, source);
        final InspectableSourceMeta imeta = ((InspectableSourceMeta) meta);
        imeta.setSourceProperties(isource.getSourceProperties());
    }

    protected TraversableCachingSource newSource(String uri, Source wrapped) {
        return  new InspectableTraversableCachingSource(super.protocol,
                                                        uri,
                                                        (InspectableSource) wrapped,
                                                        super.expires,
                                                        super.cacheName,
                                                        super.async,
                                                        super.eventAware);
    }
    
    protected static class InspectableSourceMeta extends TraversableSourceMeta {
        
        protected static final SourceProperty NULL_PROPERTY = new SourceProperty("cocoon", "isnull");
        
        private Map properties;
        
        protected SourceProperty getSourceProperty(String namespace, String name) {
            if (properties == null) return null;
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
            if (this.properties == null) return null;
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
