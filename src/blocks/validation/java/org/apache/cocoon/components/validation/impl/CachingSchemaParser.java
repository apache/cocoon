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
package org.apache.cocoon.components.validation.impl;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.EntityResolver;
import org.xml.sax.SAXException;

/**
 * <p>A {@link SchemaParser} caching {@link Schema} instance for multiple use.</p>
 *
 * <p>A {@link Schema} will be cached until its {@link SourceValidity} expires.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public abstract class CachingSchemaParser
implements Serviceable, Initializable, Disposable, SchemaParser, Configurable {

    /** <p>The {@link ServiceManager} configured for this instance.</p> */
    protected ServiceManager serviceManager = null;
    /** <p>The {@link SourceResolver} to resolve URIs into {@link Source}s.</p> */
    protected SourceResolver sourceResolver = null;
    /** <p>The {@link EntityResolver} resolving against catalogs of public IDs.</p> */
    protected EntityResolver entityResolver = null;
    /** <p>The {@link Store} used for caching {@link Schema}s (if enabled).</p> */
    protected Store transientStore = null;
    /** <p>A flag indicating whether schemas can be cached or not.</p> */
    private boolean enableCache = true;

    /**
     * <p>Contextualize this component specifying a {@link ServiceManager} instance.</p>
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
    }

    /**
     * <p>Configure this instance.</p>
     * 
     * <p>The only configuration sub-element allowed by this instance is
     * <code>&lt;cache-schemas&gt;<i>true|false</i>&lt;/cache-schemas&gt;</code>
     * indicating where parsed schema should be cached. The default value for this
     * is <code>true</code>.</p>
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        Configuration subconfiguration = configuration.getChild("cache-schemas");
        this.enableCache = subconfiguration.getValueAsBoolean(true);
    }

    /**
     * <p>Initialize this component instance.</p>
     * 
     * <p>A this point component resolution will happen.</p>
     */
    public void initialize()
    throws Exception {
        this.entityResolver = (EntityResolver) this.serviceManager.lookup(EntityResolver.ROLE);
        this.sourceResolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
        this.transientStore = (Store) this.serviceManager.lookup(Store.TRANSIENT_STORE);
    }
    
    /**
     * <p>Dispose this component instance.</p>
     */
    public void dispose() {
        if (this.entityResolver != null) this.serviceManager.release(this.entityResolver);
        if (this.sourceResolver != null) this.serviceManager.release(this.sourceResolver);
        if (this.transientStore != null) this.serviceManager.release(this.transientStore);
    }

    /**
     * <p>Return a cached or freshly parsed {@link Schema} instance.</p>
     */
    public final Schema getSchema(String uri)
    throws IOException, SAXException {
        
        /* First of all resolve the source, and use the resolved URI */
        Source source = null;
        try {
            source = this.sourceResolver.resolveURI(uri); 
            uri = source.getURI();
        } finally {
            if (source != null) this.sourceResolver.release(source);
        }

        /* Prepare a key, and try to get the cached copy of the schema */
        String key = this.getClass().getName() + ":" + uri;
        Schema schema = (Schema) this.transientStore.get(key);
        SourceValidity validity = null;

        /* If the schema was found verify its validity and optionally clear */
        if (schema != null) {
            validity = schema.getValidity();
            if (validity == null) {
                this.transientStore.remove(key);
                schema = null;
            } else if (validity.isValid() != SourceValidity.VALID) {
                this.transientStore.remove(key);
                schema = null;
            }
        }

        /* If the schema was not cached or was cleared, parse and cache it */
        if (schema == null) {
            schema = this.parseSchema(uri);
            validity = schema.getValidity();
            if ((validity != null) && (validity.isValid() == SourceValidity.VALID)) {
                this.transientStore.store(key, schema);
            }
        }
        
        /* Return the parsed or cached schema */
        return schema;
    }
    
    /**
     * <p>Freshly parsed a brand new {@link Schema} instance.</p>
     *
     * <p>Caching of the parsed {@link Schema} instance will be performed in the
     * {@link #getSchema(String)} method.</p>
     */
    protected abstract Schema parseSchema(String uri)
    throws IOException, SAXException;
}
