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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.URIAbsolutizer;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A factory for the context protocol using the context of the servlet api. 
 * It builds the source by asking the environment context for the real URL
 * (see {@link org.apache.cocoon.environment.Context#getResource(String)}) 
 * and then resolving this real URL.
 *
 * @version $Id$
 */
public class ContextSourceFactory extends AbstractLogEnabled
                                  implements SourceFactory, Serviceable, Contextualizable,
                                             ThreadSafe, URIAbsolutizer {

    /** The ServiceManager */
    protected ServiceManager manager;

    /** Http servlet context - if available */
    protected ServletContext servletContext;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(org.apache.avalon.framework.context.Context context)
    throws ContextException {
        this.servletContext = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating source object for " + location);
        }

        // Lookup resolver 
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);

            // Remove the protocol and the first '/'
            final int pos = location.indexOf(":/");
            final String scheme = location.substring(0, pos);
            final String path = location.substring(pos + 2);

            // fix for #24093, we don't give access to files outside the context:
            if (path.indexOf("../") != -1) {
                throw new MalformedURLException("Invalid path ('../' is not allowed) : " + path);
            }

            URL u;

            // Try to get a file first and fall back to a resource URL
            String actualPath = this.servletContext.getRealPath(path);
            if (actualPath != null) {
                u = new File(actualPath).toURL();
            } else {
                u = this.servletContext.getResource(path);
            }

            if (u != null) {
                Source source = resolver.resolveURI(u.toExternalForm());
                if ( parameters != null 
                     && BooleanUtils.toBoolean("force-traversable")
                     && this.servletContext != null 
                     && !(source instanceof TraversableSource) ) {
                    final Set children = this.servletContext.getResourcePaths(path + '/');
                    if ( children != null ) {
                        source = new TraversableContextSource(source, children, this, path, scheme);
                    }
                }
                return source;                
            }

            final String message = location + " could not be found. (possible context problem)";
            getLogger().info(message);
            throw new MalformedURLException(message);
        } catch (ServiceException se) {
            throw new SourceException("Unable to lookup source resolver.", se);
        } finally {
            this.manager.release(resolver);
        }
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // In fact, this method should never be called as this factory
        // returns a source object from a different factory. So that
        // factory should release the source
        if (source != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Releasing source " + source.getURI());
            }

            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                if (source instanceof TraversableContextSource) {
                    resolver.release(((TraversableContextSource) source).wrappedSource);
                } else {
                    resolver.release(source);
                }
            } catch (ServiceException ingore) {
                // we ignore this
            } finally {
                this.manager.release(resolver);
            }
        }
    }

    /**
     * @see org.apache.excalibur.source.URIAbsolutizer#absolutize(java.lang.String, java.lang.String)
     */
    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }
}
