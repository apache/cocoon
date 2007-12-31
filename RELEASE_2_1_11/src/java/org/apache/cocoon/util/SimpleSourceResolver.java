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
package org.apache.cocoon.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.impl.ContextSourceFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.URLSourceFactory;

/**
 * A minimalist <code>SourceResolver</code> that handles a fixed restricted number of protocols. It is
 * used as a bootstrap resolver to load roles and imported files in a service manager.
 * <p>
 * The supported protocols schemes are:
 * <ul>
 * <li><code>resource</code> to load resources in the classpath,</li>
 * <li><code>context</code> to load resources from the context, defined by the <code>context-root</code>
 *     entry in the Avalon {@link Context} (either a {@link File} or an {@link URL}), or if not
 *     present, from the <code>user.dir</code> system property,</li>
 * <li>all standard JDK schemes (http, file, etc).
 * </ul>
 * Relative URIs are resolved relatively to the context root, i.e. similarily to "<code>context:</code>".
 *
 * @version $Id$
 */
public final class SimpleSourceResolver extends AbstractLogEnabled
    implements ThreadSafe, Contextualizable, SourceResolver {

    // The base URI, initialized in contextualize()
    private String contextBase;

    // The three factories we use (no need for a selector nor a Map)
    private ResourceSourceFactory resourceFactory = new ResourceSourceFactory();
    private URLSourceFactory urlFactory = new URLSourceFactory();
    private ContextSourceFactory contextFactory = new ContextSourceFactory();

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.resourceFactory.enableLogging(logger);
        this.urlFactory.enableLogging(logger);
        this.contextFactory.enableLogging(logger);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.contextFactory.contextualize(context);
        try {
            this.contextFactory.service(new SimpleServiceManager(this));
        } catch (ServiceException se) {
            throw new ContextException("Unable to service context factory.", se);
        }

        try {
            // Similar to Excalibur's SourceResolverImpl, and consistent with ContextHelper.CONTEXT_ROOT_URL
            if( context.get("context-root") instanceof URL) {
                contextBase = ((URL)context.get("context-root")).toExternalForm();
            } else {
                contextBase = ((File)context.get("context-root")).toURL().toExternalForm();
            }
        } catch(ContextException ce) {
            // set the base URL to the current directory
            try {
                contextBase = new File(System.getProperty("user.dir")).toURL().toExternalForm();
            } catch( MalformedURLException mue) {
                throw new ContextException( "Malformed URL for user.dir, and no context-root exists", mue);
            }
        } catch( MalformedURLException mue) {
            throw new ContextException("Malformed URL for context-root", mue);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Base URL set to " + this.contextBase);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public Source resolveURI(String uri) throws MalformedURLException, IOException {
        return resolveURI(uri, contextBase, null);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public Source resolveURI(String uri, String base, Map params) throws MalformedURLException, IOException {
        if (uri.startsWith("resource://")) {
            return resourceFactory.getSource(uri, null);
        } else if (uri.startsWith("context://")) {
            return this.contextFactory.getSource(uri, params);
        } else {
            // special handling for windows and unix file paths
            if( uri.length() > 1 && uri.charAt( 1 ) == ':' ) {
                uri = "file:/" + uri;
                base = null;
            } else if( uri.length() > 2 && uri.charAt(0) == '/' && uri.charAt(2) == ':' ) {
                uri = "file:" + uri;
                base = null;
            }
            URL url;
            if ( base == null ) {
                url = new URL(uri);
            } else {
                URL baseURL = new URL(base);
                url = new URL(baseURL, uri);
            }
            return this.urlFactory.getSource(url.toExternalForm(), params);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        if ( source != null ) {
            if ( "context".equals(source.getScheme()) ) {
                this.contextFactory.release(source);
            } else if ( "resource".equals(source.getScheme()) ) {
                this.resourceFactory.release(source);
            } else {
                this.urlFactory.release(source);
            }
        }
    }

    public static final class SimpleServiceManager implements ServiceManager {

        private final SourceResolver resolver;

        public SimpleServiceManager(SourceResolver resolver) {
            this.resolver = resolver;
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
         */
        public boolean hasService(String role) {
            return SourceResolver.ROLE.equals(role);
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
         */
        public Object lookup(String role) throws ServiceException {
            if ( !SourceResolver.ROLE.equals(role) ) {
                throw new ServiceException("SimpleServiceManager", "Unable to lookup component with role: " + role);
            }
            return this.resolver;
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
         */
        public void release(Object component) {
            // nothing to do
        }
    }
}

