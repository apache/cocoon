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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.ExtendedCachedResponse;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.cron.ConfigurableCronJob;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.InputSource;

/**
 * A target updating a cache entry.
 *
 * This target requires several parameters:
 * - uri (String): The uri to cache, every valid protocol can be used, except the Cocoon protocol!
 * - cacheRole (String): The role of the cache component to store the content
 * - expires (long): The time in seconds the cached content is valid
 * - cacheKey (SimpleCacheKey) : The key used to cache the content
 *  
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: UpdateTarget.java,v 1.3 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class UpdateTarget 
    extends AbstractLogEnabled
    implements Recyclable, Serviceable, ConfigurableCronJob {
    
    protected String uri;
    
    protected String cacheRole;
    
    protected long expires;
    
    protected ServiceManager manager;
    
    protected SourceResolver resolver;
    
    protected SimpleCacheKey cacheKey;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.cornerstone.services.scheduler.Target#targetTriggered(java.lang.String)
     */
    public void execute(String name) {
        if ( this.uri != null ) {
            if ( this.getLogger().isInfoEnabled()) {
                this.getLogger().info("Refreshing " + this.uri);
            }

            Source source = null;
            Cache cache = null;
            try {
                cache = (Cache)this.manager.lookup(this.cacheRole);
                // the content expires, so remove it                
                cache.remove(cacheKey);
                
                source = this.resolver.resolveURI(this.uri);

                XMLSerializer serializer = null;
                SAXParser parser = null;
                byte[] cachedResponse;
                byte[] content = null;
                
                try {
                    serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                    if ( source instanceof XMLizable ) {
                        ((XMLizable)source).toSAX(serializer);
                    } else {
                        // resd the content
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        final byte[] buffer = new byte[2048];
                        final InputStream inputStream = source.getInputStream();
                        int length;
        
                        while ((length = inputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, length);
                        }
                        baos.flush();
                        inputStream.close();
                        
                        content = baos.toByteArray();
                        
                        parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
                    
                        final InputSource inputSource = new InputSource(new ByteArrayInputStream(content));
                        inputSource.setSystemId(source.getURI());
                    
                        parser.parse( inputSource, serializer );
                    }
                    cachedResponse = (byte[])serializer.getSAXFragment();
                } finally {
                    this.manager.release(parser);
                    this.manager.release(serializer);
                }
                
                SourceValidity val = new ExpiresValidity(this.expires);
                ExtendedCachedResponse response = new ExtendedCachedResponse(val, content);
                response.setAlternativeResponse(cachedResponse);
                cache.store(cacheKey, response);
            
            } catch (Exception ignore) {
                this.getLogger().error("Exception during updating " +this.uri, ignore);
            } finally {
                this.resolver.release(source);
                this.manager.release( cache );
            }

        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.scheduler.ConfigurableTarget#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters pars, Map objects) {
        this.uri = pars.getParameter("uri", null);
        this.cacheRole = pars.getParameter("cache-role", Cache.ROLE);
        this.expires = pars.getParameterAsLong("cache-expires", 1800);
        this.cacheKey = (SimpleCacheKey) objects.get("cache-key");
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.uri = null;
        this.cacheKey = null;
        this.expires = 0;
        this.cacheRole = null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

}
