/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: UpdateTarget.java,v 1.2 2003/09/24 22:34:52 cziegeler Exp $
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
