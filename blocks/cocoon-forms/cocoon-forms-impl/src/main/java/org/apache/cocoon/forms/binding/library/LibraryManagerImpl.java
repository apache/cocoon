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
package org.apache.cocoon.forms.binding.library;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.forms.binding.JXPathBindingManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @version $Id$
 */
public class LibraryManagerImpl implements LibraryManager {

    private static Log LOG = LogFactory.getLog( LibraryManagerImpl.class );
    
	protected static final String PREFIX = "CocoonFormBindingLibrary:";

    private CacheManager cacheManager;

    private JXPathBindingManager bindingManager;

    private SourceResolver sourceResolver;

    private SAXParser parser;

    public void setBindingManager(JXPathBindingManager bindingManager) {
    	this.bindingManager = bindingManager;
    }

    public Library get(String sourceURI) throws LibraryException {
        return get(sourceURI, null);
    }

    public Library get(String sourceURI, String baseURI) throws LibraryException {
        Source source = null;
        try {
            try {
                source = sourceResolver.resolveURI(sourceURI, baseURI, null);
            } catch (Exception e) {
                throw new LibraryException("Unable to resolve library.",
                                           e, new LocationImpl("[LibraryManager]", sourceURI));
            }

            Library lib = (Library) this.cacheManager.get(source, PREFIX);
            if (lib != null && lib.dependenciesHaveChanged()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Library IS REMOVED from cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
                this.cacheManager.remove(source, PREFIX); // evict?
                return null;
            }

            if (LOG.isDebugEnabled()) {
                if (lib != null) {
                    LOG.debug("Library IS in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                } else {
                    LOG.debug("Library IS NOT in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
            }

            return lib;
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
        }
    }

    public Library load(String sourceURI) throws LibraryException {
        return load(sourceURI, null);
    }

	public Library load(String sourceURI, String baseURI) throws LibraryException {
        Source source = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading library: '" + sourceURI + "' relative to '" + baseURI + "'");
        }

        try {
            try {
                source = sourceResolver.resolveURI(sourceURI, baseURI, null);
            } catch (Exception e) {
                throw new LibraryException("Unable to resolve library.",
                                           e, new LocationImpl("[LibraryManager]", sourceURI));
            }

            Library lib = (Library) this.cacheManager.get(source, PREFIX);
            if (lib != null && lib.dependenciesHaveChanged()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Library IS EXPIRED in cache: '" + sourceURI + "' relative to '" + baseURI + "'");
                }
                lib = null;
            }

            if (lib == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Library IS NOT in cache, loading: '" + sourceURI + "' relative to '" + baseURI + "'");
                }

            	try {
                    InputSource inputSource = new InputSource(source.getInputStream());
                    inputSource.setSystemId(source.getURI());

                    Document doc = DomHelper.parse(inputSource, parser);
                    lib = newLibrary();
                    lib.buildLibrary(doc.getDocumentElement());

                    this.cacheManager.set(lib,source,PREFIX);
                } catch (RuntimeException re) {
                    throw re;
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
        }
	}

	public Library newLibrary() {
		Library lib = new Library(this, bindingManager.getBuilderAssistant());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created new library! " + lib);
        }

        return lib;
	}

	public void debug(String msg) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(msg);
		}
	}

    public void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    public void setSourceResolver( SourceResolver sourceResolver )
    {
        this.sourceResolver = sourceResolver;
    }

    public void setParser( SAXParser parser )
    {
        this.parser = parser;
    }
}
