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

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.URIAbsolutizer;
import org.springframework.beans.factory.BeanNameAware;

import org.apache.cocoon.caching.Cache;
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
 * <code>cached:http://www.apache.org/[?cocoon:cache-expires=60][&cocoon:cache-name=main][&cocoon:cache-fail=true]</code>.
 * </p>
 * <p>
 * The above examples shows how the real source <code>http://www.apache.org/</code>
 * is wrapped and the cached contents is used for <code>60</code> seconds.
 * The second querystring parameter instructs that the cache key be extended with the string
 * <code>main</code>. This allows the use of multiple cache entries for the same source. The <code>cache-fail</code>
 * argument lets subsequent syncronous requests, that have to be refreshed, fail, in the case
 * that the wrapped source can't be reached. The default value for <code>cache-fail</code> is <code>true</code>.
 * </p>
 * <p>
 * This factory creates either instances of {@link org.apache.cocoon.components.source.impl.CachingSource}
 * or {@link org.apache.cocoon.components.source.impl.TraversableCachingSource}
 * depending on the whether the wrapped Source is an instance of TraversableSource.
 * </p>
 * <p>
 * If other types of sources should be supported, subclass this class and override the method 
 * {@link #instantiateSource(String, String, Source, int, String, boolean)}.
 * </p>
 * <p>
 * In order to use the CachingSourceFactory in Cocoon, you have to set it up as Spring bean. For this purpose
 * you can use the abstract bean definition with the name 
 * <code>org.apache.excalibur.source.SourceFactory/caching::abstract</code>:
 * <br>
 * <code>
 *   &lt;bean name=&quot;org.apache.excalibur.source.SourceFactory/cached::abstract&quot;
 *     class=&quot;org.apache.cocoon.components.source.impl.CachingSourceFactory&quot;
 *     abstract=&quot;true&quot;&gt;
 *     &lt;property name=&quot;cache&quot; ref=&quot;org.apache.cocoon.caching.Cache&quot;/&gt;
 *     &lt;property name=&quot;sourceResolver&quot; ref=&quot;org.apache.excalibur.source.SourceResolver&quot;/&gt;
 *     &lt;property name=&quot;serviceManager&quot; ref=&quot;org.apache.avalon.framework.service.ServiceManager&quot;/&gt;
 *     &lt;property name=&quot;validityStrategy&quot;&gt;
 *       &lt;bean class=&quot;org.apache.cocoon.components.source.impl.ExpiresCachingSourceValidityStrategy&quot;/&gt;        
 *     &lt;/property&gt;
 *   &lt;/bean&gt;      
 * </p>
 * <p>
 * It is also possible to override dependencies partially there.
 * </p>
 * 
 * @version $Id$
 * @since 2.1.1
 */
public class CachingSourceFactory implements URIAbsolutizer, SourceFactory, BeanNameAware {

    private Log logger = LogFactory.getLog(getClass());        
    
    private static final boolean DEFAULT_ASYNC_VALUE = false;
    
    private static final int DEFAULT_EXPIRES_VALUE = -1;

    /** Protocol prefix / factory name */
    protected String scheme;

    /** Asynchronous ? */
    protected boolean async = DEFAULT_ASYNC_VALUE;
    
    protected int defaultExpires = DEFAULT_EXPIRES_VALUE;

    /** Validity strategy implementation*/
    protected CachingSourceValidityStrategy validityStrategy;
    
    /** The cache */
    protected Cache cache;

    /** The refresher */
    protected SourceRefresher refresher;
    
    /** The Cocoon service manager */
    protected ServiceManager serviceManager;
    
    /** The {@link SourceResolver} */
    protected SourceResolver resolver;
    
    
    public CachingSourceFactory() {
    }
    

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(final String location, final Map parameters)
    throws IOException {

        if (logger.isDebugEnabled() ) {
            logger.debug("Creating source " + location);
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

        int expires = params.getParameterAsInteger(CachingSource.CACHE_EXPIRES_PARAM, this.defaultExpires);
        String cacheName = params.getParameter(CachingSource.CACHE_NAME_PARAM, null);        
        boolean fail = params.getParameterAsBoolean(CachingSource.CACHE_FAIL_PARAM, false);

        return createCachingSource(location, uri, this.resolver.resolveURI(uri), expires, cacheName, fail);
    }

    /**
     * Actually creates a new CachingSource. Can be overriden in subclasses
     */
    protected CachingSource createCachingSource(String uri,
                                                String wrappedUri,
                                                Source wrappedSource,
                                                int expires,
                                                String cacheName,
                                                boolean fail)
    throws SourceException {

        CachingSource source = instantiateSource(uri,
                                                 wrappedUri,
                                                 wrappedSource,
                                                 expires,
                                                 cacheName,
                                                 fail);

        // set the required components directly for speed
        source.cache = this.cache;
        try {
            // call selected avalon lifecycle interfaces. Mmmh.
            ContainerUtil.service(source, this.serviceManager);
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
            if(this.refresher == null) {
                String msg = "Make sure that the SourceRefresher is injected correctly when " + 
                "you want to use the source asyncronous and with an expires param > 0.";
                logger.error(msg);
                throw new SourceException(msg);
            }
            this.refresher.refresh(source.getCacheKey(), source.getURI(), params);
        }

        return source;
    }
    
    protected CachingSource instantiateSource(String uri, String wrappedUri, Source wrappedSource, int expires,
                    String cacheName, boolean fail) {

        if (wrappedSource instanceof TraversableSource) {
            return new TraversableCachingSource(
                            this, 
                            this.scheme, 
                            uri, 
                            wrappedUri,
                            (TraversableSource) wrappedSource, 
                            expires, 
                            cacheName, 
                            this.async,
                            this.validityStrategy, 
                            fail);
        } else {
            return new CachingSource(
                            this.scheme, 
                            uri, 
                            wrappedUri, 
                            wrappedSource, 
                            expires, 
                            cacheName, 
                            this.async,
                            this.validityStrategy, 
                            fail);
        }
    }

    /**
     * Release a {@link Source} object.
     */
    public void release(Source source) {
        if (source instanceof CachingSource) {
            if (logger.isDebugEnabled() ) {
                logger.debug("Releasing source " + source.getURI());
            }
            CachingSource caching = (CachingSource) source;
            resolver.release(caching.source);
            caching.dispose();
        }
    }

    // ---------------------------------------------------- URIAbsolutizer

    /**
     * @see org.apache.excalibur.source.URIAbsolutizer#absolutize(java.lang.String, java.lang.String)
     */
    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }

    // ---------------------------------------------------- Set dependencies
    
    /**
     * Mandatory dependency on an implementation of {@link Cache}.
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    /**
     * Mandatory dependency on an implementation of {@link CachingSourceValidityStrategy}.
     */
    public void setValidityStrategy(CachingSourceValidityStrategy validityStrategy) {
        this.validityStrategy = validityStrategy;
    }   
    
    /**
     * Mandatory dependency on an implementation of {@link ServiceManager}.
     */    
    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    /**
     * Mandatory dependency on an implementation of {@link SourceResolver}.
     */    
    public void setSourceResolver(SourceResolver resolver) {
        this.resolver = resolver;
    }
 
    /**
     * Optional dependency on an implementation of {@link SourceRefresher}.
     */      
    public void setSourceRefresher(SourceRefresher refresher) {
        this.refresher = refresher;
    }
    
    /**
     * Optional property whether the source is asyncronous. The default value is <code>false</code>.
     */    
    public void setAsync(boolean async) {
        this.async = async;
    }
    
    /**
     * Optional property the default expiry time in seconds. Default is <code>-1</code> which means that is valid forever.
     */       
    public void setDefaultExpires(int expires) {
        this.defaultExpires = expires;
    }

    // ---------------------------------------------------- BeanNameAware
    
    public void setBeanName(String beanName) {
        int pos = beanName.lastIndexOf('/');
        this.scheme = beanName.substring(pos + 1);
    }    
    
}