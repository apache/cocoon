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
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.URIAbsolutizer;

import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceRefresher;

/**
 * This class implements a proxy like source caches the contents of the source
 * it wraps. This implementation can cache the content either for a given period
 * of time or until an external event invalidates the cached response.
 *
 * <p>When using the timeout approach you have a choice between two separate
 * revalidation strategies:</p>
 *
 * <ul>
 * <li>Synchronously. This means that the cached contents are checked for validity
 * and thrown out on the current thread.
 * <li>Asynchronously. A runnable task is created to invalidate and update the
 * cached response in the backgound.
 * </ul>
 *
 * <h2>Protocol syntax</h2>
 * <p>
 * The URL needs to contain the URL of the cached source, an expiration
 * period in seconds, and optionally a cache key:
 * <code>cached:http://www.apache.org/[?cocoon:cache-expires=60][&cocoon:cache-name=main]</code>.
 * </p>
 * <p>
 * The above examples shows how the real source <code>http://www.apache.org/</code>
 * is wrapped and the cached contents is used for <code>60</code> seconds.
 * The second querystring parameter instructs that the cache key be extended with the string
 * <code>main</code>. This allows the use of multiple cache entries for the same source.
 * </p>
 * <p>
 * This factory creates either instances of {@link org.apache.cocoon.components.source.impl.CachingSource}
 * or {@link org.apache.cocoon.components.source.impl.TraversableCachingSource}
 * depending on the whether the wrapped Source is an instance of TraversableSource.
 * </p>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr>
 *  <th>cache-role (String)</th>
 *  <td>Role of component used as cache.</td>
 *  <td>opt</td>
 *  <td>String</td>
 *  <td><code>{@link Cache#ROLE}</code></td>
 * </tr>
 * <tr>
 *  <th>refresher-role (String)</th>
 *  <td>Role of component used for refreshing sources.</td>
 *  <td>opt</td>
 *  <td>String</td>
 *  <td><code>{@link org.apache.cocoon.components.source.helpers.SourceRefresher#ROLE}</code></td>
 * </tr>
 * <tr>
 *  <th>async (boolean)</th>
 *  <td>Indicated if the cached source should be refreshed asynchronously.</td>
 *  <td>opt</td>
 *  <td>String</td>
 *  <td><code>false</code></td>
 * </tr>
 * <tr>
 *  <th>event-aware (boolean)</th>
 *  <td>Whether to use event-based cache invalidation.</td>
 *  <td>opt</td>
 *  <td>String</td>
 *  <td><code>false</code></td>
 * </tr>
 * <tr>
 *  <th>default-expires (int)</th>
 *  <td>Default expiration value for if it is not specified on the Source itself.</td>
 *  <td>opt</td>
 *  <td>String</td>
 *  <td><code>-1</code></td>
 * </tr>
 * </tbody></table>
 *
 * @version $Id$
 * @since 2.1.1
 */
public class CachingSourceFactory extends AbstractLogEnabled
                                  implements Serviceable, Configurable, Disposable,
                                             ThreadSafe, URIAbsolutizer, SourceFactory {

    // ---------------------------------------------------- Constants

    private static final String ASYNC_PARAM = "async";
    private static final String EVENT_AWARE_PARAM = "event-aware";
    private static final String CACHE_ROLE_PARAM = "cache-role";
    private static final String REFRESHER_ROLE_PARAM = "refresher-role";
    private static final String DEFAULT_EXPIRES_PARAM = "default-expires";

    // ---------------------------------------------------- Instance variables

    /** Protocol prefix / factory name */
    private String scheme;

    /** Asynchronous ? */
    private boolean async;

    /** Event aware ? */
    private boolean eventAware;

    /** The role of the cache */
    private String cacheRole;

    /** The role of the refresher */
    private String refresherRole;

    /** Default expires value */
    private int defaultExpires;

    /** Has the lazy initialization been done? */
    private volatile boolean isInitialized;

    /** The <code>ServiceManager</code> */
    protected ServiceManager manager;

    /** The {@link SourceResolver} */
    protected SourceResolver resolver;

    /** The refresher */
    protected SourceRefresher refresher;

    /** The cache */
    protected Cache cache;

    // ---------------------------------------------------- Lifecycle

    public CachingSourceFactory() {
    }

    public void service(ServiceManager manager) {
        this.manager = manager;
        // Due to cyclic dependencies we can't lookup the resolver,
        // the refresher or the cache until after the factory is
        // initialized.
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.scheme = configuration.getAttribute("name");
        Parameters parameters = Parameters.fromConfiguration(configuration);

        // 'async' parameter
        this.async = parameters.getParameterAsBoolean(ASYNC_PARAM, false);

        // 'event-aware' parameter
        this.eventAware = parameters.getParameterAsBoolean(EVENT_AWARE_PARAM, false);

        // 'cache-role' parameter
        this.cacheRole = parameters.getParameter(CACHE_ROLE_PARAM, Cache.ROLE);

        // 'refresher-role' parameter
        if (this.async) {
            this.refresherRole = parameters.getParameter(REFRESHER_ROLE_PARAM, SourceRefresher.ROLE);
        }

        this.defaultExpires = parameters.getParameterAsInteger(DEFAULT_EXPIRES_PARAM, -1);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using cache " + this.cacheRole);
            if (this.async) {
                getLogger().debug("Using refresher " + this.refresherRole);
            }
        }
    }

    /**
     * Lazy initialization of resolver and refresher because of
     * cyclic dependencies.
     *
     * @throws SourceException
     */
    private synchronized void lazyInitialize() throws SourceException {
        if (this.isInitialized) {
            // another thread finished initialization for us while
            // we were waiting
            return;
        }

        try {
            this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
        } catch (ServiceException se) {
            throw new SourceException("Missing service dependency: " + SourceResolver.ROLE, se);
        }

        try {
            this.cache = (Cache) this.manager.lookup(this.cacheRole);
        } catch (ServiceException se) {
            throw new SourceException("Missing service dependency: " + this.cacheRole, se);
        }

        if (this.async) {
            try {
                this.refresher = (SourceRefresher) this.manager.lookup(this.refresherRole);
            } catch (ServiceException se) {
                throw new SourceException("Missing service dependency: " + this.refresherRole, se);
            }
        }

        this.isInitialized = true;
    }

    /* (non-Javadoc)
     * @see Disposable#dispose()
     */
    public void dispose() {
        if (this.refresher != null) {
            this.manager.release(this.refresher);
            this.refresher = null;
        }
        if (this.cache != null) {
            this.manager.release(this.cache);
            this.cache = null;
        }
        if (this.resolver != null) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        this.manager = null;
    }

    // ---------------------------------------------------- SourceFactory implementation

    protected String getScheme() {
        return this.scheme;
    }

    protected boolean isAsync() {
        return this.async;
    }

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(final String location, final Map parameters)
    throws MalformedURLException, IOException {

        if (getLogger().isDebugEnabled() ) {
            getLogger().debug("Creating source " + location);
        }

        // we must do lazy initialization because of cyclic dependencies
        if (!this.isInitialized) {
            lazyInitialize();
        }

        // snip the cache protocol
        int index = location.indexOf(':');
        if (index == -1) {
            throw new MalformedURLException("This Source requires a subprotocol to be specified.");
        }

        String uri = location.substring(index + 1);

        // parse the query string
        SourceParameters sp = null;
        index = uri.indexOf('?');
        if (index != -1) {
            sp = new SourceParameters(uri.substring(index + 1));
            uri = uri.substring(0, index);
        }

        // put caching source specific query string parameters
        // into a Parameters object
        final Parameters params = new Parameters();
        if (sp != null) {
            SourceParameters remainingParameters = (SourceParameters) sp.clone();
            final Iterator names = sp.getParameterNames();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (name.startsWith("cocoon:cache")) {
                    params.setParameter(name.substring("cocoon:".length()), sp.getParameter(name));
                    remainingParameters.removeParameter(name);
                }
            }
            String queryString = remainingParameters.getEncodedQueryString();
            if (queryString != null) {
                uri += "?" + queryString;
            }
        }

        int expires = params.getParameterAsInteger(CachingSource.CACHE_EXPIRES_PARAM, defaultExpires);
        String cacheName = params.getParameter(CachingSource.CACHE_NAME_PARAM, null);

        Source source = this.resolver.resolveURI(uri);
        return createCachingSource(location, uri, source, expires, cacheName);
    }

    /**
     * Actually creates a new CachingSource. Can be overriden in subclasses
     */
    protected CachingSource createCachingSource(String uri,
                                                String wrappedUri,
                                                Source wrappedSource,
                                                int expires,
                                                String cacheName)
    throws SourceException {

        CachingSource source;

        if (wrappedSource instanceof TraversableSource) {
            if (wrappedSource instanceof InspectableSource) {
                source = new InspectableTraversableCachingSource(this,
                                                                 getScheme(),
                                                                 uri,
                                                                 wrappedUri,
                                                                 (InspectableSource) wrappedSource,
                                                                 expires,
                                                                 cacheName,
                                                                 isAsync(),
                                                                 eventAware);
            } else {
                source = new TraversableCachingSource(this,
                                                      getScheme(),
                                                      uri,
                                                      wrappedUri,
                                                      (TraversableSource) wrappedSource,
                                                      expires,
                                                      cacheName,
                                                      isAsync(),
                                                      eventAware);
            }
        } else {
            source = new CachingSource(getScheme(),
                                       uri,
                                       wrappedUri,
                                       wrappedSource,
                                       expires,
                                       cacheName,
                                       isAsync(),
                                       eventAware);
        }

        // set the required components directly for speed
        source.cache = this.cache;

        ContainerUtil.enableLogging(source, getLogger());
        try {
            // call selected avalon lifecycle interfaces. Mmmh.
            ContainerUtil.service(source, this.manager);
            ContainerUtil.initialize(source);
        } catch (ServiceException e) {
            throw new SourceException("Unable to initialize source.", e);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }

        if (this.async && expires > 0) {
            // schedule it with the refresher
            final Parameters params = new Parameters();
            params.setParameter(SourceRefresher.PARAM_CACHE_INTERVAL,
                                String.valueOf(source.getExpiration()));
            this.refresher.refresh(source.getCacheKey(), source.getURI(), params);
        }

        return source;
    }

    /**
     * Release a {@link Source} object.
     */
    public void release(Source source) {
        if (source instanceof CachingSource) {
            if (getLogger().isDebugEnabled() ) {
                getLogger().debug("Releasing source " + source.getURI());
            }
            CachingSource caching = (CachingSource) source;
            resolver.release(caching.source);
            caching.dispose();
        }
    }

    // ---------------------------------------------------- URIAbsolutizer implementation

    /*
     *  (non-Javadoc)
     * @see org.apache.excalibur.source.URIAbsolutizer#absolutize(java.lang.String, java.lang.String)
     */
    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }

}
