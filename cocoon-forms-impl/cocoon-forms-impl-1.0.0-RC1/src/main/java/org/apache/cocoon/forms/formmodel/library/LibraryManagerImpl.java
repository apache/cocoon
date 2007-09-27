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
package org.apache.cocoon.forms.formmodel.library;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationImpl;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @version $Id$
 *
 */
public class LibraryManagerImpl extends AbstractLogEnabled
                                implements LibraryManager, Serviceable, Configurable,
                                           Disposable, ThreadSafe, Component {

    private static final String PREFIX = "CocoonFormsLibrary:";

    private ServiceManager manager;
    private CacheManager cacheManager;

    private ServiceSelector widgetDefinitionBuilderSelector;

    //
    // Lifecycle
    //

    public void configure(Configuration configuration) throws ConfigurationException {
        // TODO Read config to "preload" libraries
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.cacheManager = (CacheManager)serviceManager.lookup(CacheManager.ROLE);
        this.widgetDefinitionBuilderSelector = (ServiceSelector) manager.lookup(WidgetDefinitionBuilder.class.getName() + "Selector");
    }

    public void dispose() {
        if (this.cacheManager != null) {
            this.manager.release(this.cacheManager);
            this.cacheManager = null;
        }
        this.manager = null;
    }

    //
    // Business methods
    //

    public Library get(String sourceURI) throws LibraryException {
        return get(sourceURI, null);
    }

    public Library get(String sourceURI, String baseURI) throws LibraryException {
        SourceResolver sourceResolver = null;
        Source source = null;
        try {
            try {
                sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                source = sourceResolver.resolveURI(sourceURI, baseURI, null);
            } catch (Exception e) {
                throw new LibraryException("Unable to resolve library.",
                                           e, new LocationImpl("[LibraryManager]", sourceURI));
            }

            Library lib = (Library) this.cacheManager.get(source, PREFIX);
            if (lib != null && lib.dependenciesHaveChanged()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Library IS REMOVED from cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
                this.cacheManager.remove(source, PREFIX); // evict?
                return null;
            }

            if (getLogger().isDebugEnabled()) {
                if (lib != null) {
                    getLogger().debug("Library IS in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                } else {
                    getLogger().debug("Library IS NOT in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
            }

            return lib;
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
            if (sourceResolver != null) {
                manager.release(sourceResolver);
            }
        }
    }

    public Library load(String sourceURI) throws LibraryException {
        return load(sourceURI, null);
    }

    public Library load(String sourceURI, String baseURI) throws LibraryException {
        SourceResolver sourceResolver = null;
        Source source = null;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Loading library: '" + sourceURI + "' relative to '" + baseURI + "'");
        }

        try {
            try {
                sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                source = sourceResolver.resolveURI(sourceURI, baseURI, null);
            } catch (Exception e) {
                throw new LibraryException("Unable to resolve library.",
                                           e, new LocationImpl("[LibraryManager]", sourceURI));
            }

            Library lib = (Library) this.cacheManager.get(source, PREFIX);
            if (lib != null && lib.dependenciesHaveChanged()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Library IS EXPIRED in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
                lib = null;
            }

            if (lib == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Library IS NOT in cache, loading: '" + sourceURI + "' relative to '" + baseURI + "'");
                }

                try {
                    InputSource inputSource = new InputSource(source.getInputStream());
                    inputSource.setSystemId(source.getURI());

                    Document doc = DomHelper.parse(inputSource, this.manager);
                    lib = newLibrary();
                    lib.buildLibrary(doc.getDocumentElement());

                    this.cacheManager.set(lib, source, PREFIX);
                } catch (Exception e) {
                    throw new LibraryException("Unable to load library.",
                                               e, new LocationImpl("[LibraryManager]", source.getURI()));
                }
            }

            return lib;
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
            if (sourceResolver != null) {
                manager.release(sourceResolver);
            }
        }
    }

    public Library newLibrary() {
        Library lib = new Library(this, widgetDefinitionBuilderSelector);
        lib.enableLogging(getLogger());
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Created a new library: " + lib);
        }

        return lib;
    }
}
