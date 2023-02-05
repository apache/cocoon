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
package org.apache.cocoon.core.xml.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * A component that uses catalogs for resolving entities.
 * This implementation uses the XML Entity and URI Resolvers from
 * http://xml.apache.org/commons/
 * published by Norman Walsh. More information on the catalogs can be
 * found at
 * http://xml.apache.org/cocoon/userdocs/concepts/catalog.html
 *
 * @version $Id$
 * @since 2.2
 */
public class DefaultEntityResolver
    implements EntityResolver, ResourceLoaderAware {

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * Correct resource uris.
     */
    protected String correctUri(String uri) throws IOException {
        // if it is a file we have to recreate the url,
        // otherwise we get problems under windows with some file
        // references starting with "/DRIVELETTER" and some
        // just with "DRIVELETTER"
        if (uri.startsWith("file:")) {
            final File f = new File(uri.substring(5));
            return f.toURL().toExternalForm();
        }
        return uri;
    }

    /** The catalog manager */
    protected CatalogManager catalogManager = new CatalogManager();

    /** The catalog resolver */
    protected CatalogResolver catalogResolver = new CatalogResolver(catalogManager);

    /** Verbosity level. */ 
    protected Integer verbosity;
 
    protected String catalog = "WEB-INF/cocoon/entities/catalog";
 
    protected String localCatalog;

    protected ResourceLoader resourceLoader;

    public Integer getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(Integer verbosity) {
        this.verbosity = verbosity;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getLocalCatalog() {
        return localCatalog;
    }

    public void setLocalCatalog(String localCatalog) {
        this.localCatalog = localCatalog;
    }

    public void setResourceLoader(ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Set the configuration. Load the system catalog and apply any
     * parameters that may have been set using the public setter methods.
     */
    public void init()
    throws Exception { 
        // Over-ride debug level that is set by CatalogManager.properties
        if ( this.verbosity != null ) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Setting Catalog resolver verbosity level to " + this.verbosity);
            }
            this.catalogManager.setVerbosity(this.verbosity.intValue());
        }

        // Load the built-in catalog
        if ( this.catalog == null ) {
            this.getLogger().warn("No default catalog defined.");
        } else {
            this.parseCatalog(this.catalog);
        }

        // Load a single additional local catalog 
        if ( this.localCatalog != null ) {
            this.parseCatalog( this.localCatalog );
        }
    }

    /**
     * Parse a catalog
     */
    protected void parseCatalog(String uri) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Additional Catalog is " + uri);
        }

        final Resource resource = this.resourceLoader.getResource(uri);
        try {
            this.catalogResolver.getCatalog().parseCatalog(this.correctUri(resource.getURL().toExternalForm()));
        } catch (Exception e) {   
            this.getLogger().warn("Could not get Catalog file. Trying again: " + uri, e);
                        
            // try it again
            try {
                this.catalogResolver.getCatalog().parseCatalog("text/plain", resource.getInputStream());
            } catch (Exception ex) {
                this.getLogger().warn("Could not get Catalog file: " + uri, ex);
            }
        }
    }
    
    /**
     * Allow the application to resolve external entities.
     *
     * <p>The Parser will call this method before opening any external
     * entity except the top-level document entity (including the
     * external DTD subset, external entities referenced within the
     * DTD, and external entities referenced within the document
     * element): the application may request that the parser resolve
     * the entity itself, that it use an alternative URI, or that it
     * use an entirely different input source.</p>
     *
     * <p>Application writers can use this method to redirect external
     * system identifiers to secure and/or local URIs, to look up
     * public identifiers in a catalogue, or to read an entity from a
     * database or other input source (including, for example, a dialog
     * box).</p>
     *
     * <p>If the system identifier is a URL, the SAX parser must
     * resolve it fully before reporting it to the application.</p>
     *
     * @param publicId The public identifier of the external entity
     *        being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *        being referenced.
     * @return An InputSource object describing the new input source,
     *         or null to request that the parser open a regular
     *         URI connection to the system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException A Java-specific IO exception,
     *            possibly the result of creating a new InputStream
     *            or Reader for the InputSource.
     * @see org.xml.sax.InputSource
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException {
        InputSource altInputSource = this.catalogResolver.resolveEntity(publicId, systemId);
        if (altInputSource != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Resolved catalog entity: "
                    + publicId + " " + altInputSource.getSystemId());
            }
        }

        return altInputSource;
    }

}
