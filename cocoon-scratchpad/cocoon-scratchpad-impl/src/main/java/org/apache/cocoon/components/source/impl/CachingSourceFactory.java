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
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * This class implements a proxy like source caches the contents of the source
 * it wraps. This implementation can cache the content either
 * for a given period of time or until an external event invalidates
 * the cached response.
 * <p>
 * When using the timeout approach you have a choice between two separate
 * revalidation strategies.
 * </p>
 * 1) Synchronously. This means that the cached contents are checked for validity
 * and thrown out on the current thread.<br>
 * 2) Asynchronously. A cronjob is scheduled to invalidate and update the cached response
 * in the backgound.<br><br>
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
 *  <td><code>{@link org.apache.cocoon.components.source.impl.Refresher#ROLE}</code></td>
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
implements SourceFactory, URIAbsolutizer, Serviceable, Configurable, Disposable, ThreadSafe
{

    // ---------------------------------------------------- Constants
    
    public static final String ASYNC_PARAM = "async";
    public static final String EVENT_AWARE_PARAM = "event-aware";
    public static final String FAILSAFE_PARAM = "failsafe";
    public static final String CACHE_ROLE_PARAM = "cache-role";
    public static final String REFRESHER_ROLE_PARAM = "refresher-role";
    public static final String DEFAULT_EXPIRES_PARAM = "default-expires";

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
    private boolean isInitialized;

    /** The <code>ServiceManager</code> */
    protected ServiceManager manager;

    /** The {@link SourceResolver} */
    protected SourceResolver resolver;

    /** The refresher */
    protected Refresher refresher;

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
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Using cache " + this.cacheRole);
        }

        // 'refresher-role' parameter
        if (this.async) {
            this.refresherRole = parameters.getParameter(REFRESHER_ROLE_PARAM, Refresher.ROLE);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Using refresher " + this.refresherRole);
            }
        }

        this.defaultExpires = parameters.getParameterAsInteger(DEFAULT_EXPIRES_PARAM, -1);
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
                this.refresher = (Refresher) this.manager.lookup(this.refresherRole);
            } catch (ServiceException se) {
                // clean up
                if (this.resolver != null){
                    this.manager.release(this.resolver);
                    this.resolver = null;
                }
                throw new SourceException("Missing service dependency: " + this.refresherRole, se);
            }
        }
        this.isInitialized = true;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.manager.release(this.refresher);
            this.refresher = null;
            this.manager = null;
            this.resolver = null;
        }
    }

    // ---------------------------------------------------- SourceFactory implementation

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(final String location, final Map parameters)
    throws MalformedURLException, IOException {

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Creating source object for " + location);
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
        String uri = location.substring(index+1);

        // parse the query string
        SourceParameters sp = null;
        String queryString = null;
        index = uri.indexOf('?');
        if (index != -1) {
            queryString = uri.substring(index+1);
            uri = uri.substring(0,index);
            sp = new SourceParameters(queryString);
        }

        // put caching source specific query string parameters
        // into a Parameters object
        final Parameters params = new Parameters();
        SourceParameters remainingParameters = (SourceParameters) sp.clone();
        if (sp != null) {
            final Iterator names = sp.getParameterNames();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (name.startsWith("cocoon:cache")) {
                    params.setParameter(name.substring("cocoon:".length()), sp.getParameter(name));
                    remainingParameters.removeParameter(name);
                }
            }
            queryString = remainingParameters.getEncodedQueryString();
            if (queryString != null) {
                uri += "?" + queryString;
            }
        }

        int expires = params.getParameterAsInteger(CachingSource.CACHE_EXPIRES_PARAM, this.defaultExpires);
        String cacheName = params.getParameter(CachingSource.CACHE_NAME_PARAM, null);

        final CachingSource source = createCachingSource(this.resolver.resolveURI(uri),
                                                      this.scheme,
                                                      location,
                                                      expires,
                                                      cacheName,
                                                      this.async,
                                                      this.cache,
                                                      getLogger(),
                                                      manager);
        
        if (this.async && expires > 0) {

            params.setParameter(CachingSource.CACHE_EXPIRES_PARAM, String.valueOf(expires));
            params.setParameter(CachingSource.CACHE_NAME_PARAM, cacheName);
            params.setParameter(CACHE_ROLE_PARAM, this.cacheRole);

            // schedule it with the refresher
            this.refresher.refresh(source.getCacheKey(),
                                   source.getSourceURI(),
                                   this.cacheRole,
                                   params);
        }

        return source;
    }

    /**
     * Factory method for creating a new CachingSource. Delegates to createCachingSource()
     */
    public static CachingSource newCachingSource(Source wrappedSource, 
                                                 String scheme,
                                                 String uri,
                                                 int expires,
                                                 String cacheName,
                                                 boolean async,
                                                 Cache cache,
                                                 Logger logger,
                                                 ServiceManager manager)
                                                 throws SourceException {
        return new CachingSourceFactory().createCachingSource(wrappedSource, scheme, uri, expires, cacheName, async, cache, logger, manager);
    }
    
    /**
     * Actually creates a new CachingSource. Can be overriden in subclasses
     */
    protected CachingSource createCachingSource(Source wrappedSource, 
                                                 String scheme,
                                                 String uri,
                                                 int expires,
                                                 String cacheName,
                                                 boolean async,
                                                 Cache cache,
                                                 Logger logger,
                                                 ServiceManager manager)
    throws SourceException {
        
        CachingSource source;
        if (wrappedSource instanceof TraversableSource) {
            if (wrappedSource instanceof InspectableSource) {
                source = new InspectableTraversableCachingSource(scheme,
                                                                 uri,
                                                                 (InspectableSource) wrappedSource,
                                                                 expires,
                                                                 cacheName,
                                                                 async,
                                                                 eventAware);
            } else {
                source = new TraversableCachingSource(scheme,
                                                      uri,
                                                      (TraversableSource) wrappedSource,
                                                      expires,
                                                      cacheName,
                                                      async,
                                                      eventAware);
            }
        } else {
            source = new CachingSource(scheme,
                                       uri,
                                       wrappedSource,
                                       expires,
                                       cacheName,
                                       async,
                                       eventAware);
        }

        // set the required components directly for speed
        source.cache = cache;

        ContainerUtil.enableLogging(source, logger);
        try {
            // call selected avalon lifecycle interfaces. Mmmh.
            ContainerUtil.service(source, manager);
            ContainerUtil.initialize(source);
        } catch (ServiceException se) {
            throw new SourceException("Unable to initialize source.", se);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }
        return source;
    }

    /**
     * Release a {@link Source} object.
     */
    public void release(Source source) {
        if (source instanceof CachingSource) {
            if (this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Releasing source " + source.getURI());
            }
            resolver.release(((CachingSource) source).source);
            ((CachingSource) source).dispose();
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
