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
package org.apache.cocoon.components.pipeline.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

/**
 * This pipeline implementation caches the complete content for a defined
 * period of time (expires). 
 * 
 * <map:pipe name="expires" src="org.apache.cocoon.components.pipeline.impl.ExpiresCachingProcessingPipeline"/>
 * 
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ExpiresCachingProcessingPipeline.java,v 1.2 2004/03/01 03:50:57 antonio Exp $
 */
public class ExpiresCachingProcessingPipeline
    extends BaseCachingProcessingPipeline {

    /** This key can be used to put a key in the object model */
    public static final String CACHE_KEY_KEY = ExpiresCachingProcessingPipeline.class.getName() + "/CacheKey";
    
    /** This key can be used to put an expires information in the object model */
    public static final String CACHE_EXPIRES_KEY = ExpiresCachingProcessingPipeline.class.getName() + "/Expires";
    
    /** The source validity */
    protected SourceValidity cacheValidity;

    /** The key used for caching */
    protected SimpleCacheKey cacheKey;
    
    /** The expires information */
    protected long           cacheExpires;
    
    /** The cached response */
    protected CachedResponse cachedResponse;
    
    /**
     * Process the given <code>Environment</code>, producing the output.
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {
        try {
            if ( this.cachedResponse != null ) {
                byte[] content = cachedResponse.getResponse();

                if ( this.serializer == this.lastConsumer ) {
                    final OutputStream outputStream = environment.getOutputStream(0);
                    if (content.length > 0) {
                        environment.setContentLength(content.length);
                        outputStream.write(content);
                    }
                } else {
                    this.xmlDeserializer.setConsumer( this.lastConsumer );
                    this.xmlDeserializer.deserialize( content );
                }

            } else {

                // generate new response

                if ( this.cacheExpires == 0 ) {
                    return super.processXMLPipeline( environment );
                }
                    
                byte[] cachedData;
                if ( this.serializer == this.lastConsumer ) {
                    
                    if (this.serializer.shouldSetContentLength()) {
                        OutputStream os = environment.getOutputStream(this.outputBufferSize);
    
                        // set the output stream
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        this.serializer.setOutputStream(baos);
        
                        this.generator.generate();
    
                        cachedData = baos.toByteArray();
                        environment.setContentLength(cachedData.length);
                        os.write(cachedData);
                    } else {
                        CachingOutputStream os = new CachingOutputStream( environment.getOutputStream(this.outputBufferSize) );
                        // set the output stream
                        this.serializer.setOutputStream( os );
                        this.generator.generate();
                        
                        cachedData = os.getContent();
                    }
    
                } else {
                    this.generator.generate();
                    cachedData = (byte[])this.xmlSerializer.getSAXFragment();
                }
                //
                // Now that we have processed the pipeline,
                // we do the actual caching
                //
                if ( this.cacheValidity != null ) {
                    cachedResponse = new CachedResponse(this.cacheValidity,
                                                        cachedData);
                    this.cache.store(this.cacheKey,
                                     cachedResponse);
                }
            }
        } catch ( SocketException se ) {
            if (se.getMessage().indexOf("reset") > 0
                    || se.getMessage().indexOf("aborted") > 0
                    || se.getMessage().indexOf("connection abort") > 0) {
                throw new ConnectionResetException("Connection reset by peer", se);
            } else {
                throw new ProcessingException("Failed to execute pipeline.", se);
            }
        } catch ( ProcessingException pe ) {
            throw pe;
        } catch ( Exception e ) {
            throw new ProcessingException("Error executing pipeline.", e);
        }
        return true;
    }

    /**
     * Connect the XML pipeline.
     */
    protected void connectPipeline(Environment environment)
    throws ProcessingException {
        if ( this.lastConsumer != this.serializer ) {
            // internal
            if ( this.cachedResponse == null) {
                // if we cache, we need an xml serializer
                if ( this.cacheExpires > 0) {
                    try {
                        final XMLConsumer old = this.lastConsumer;
                        this.xmlSerializer = (XMLSerializer)this.manager.lookup( XMLSerializer.ROLE );
                        this.lastConsumer = new XMLTeePipe(this.lastConsumer, this.xmlSerializer);
        
                        super.connectPipeline( environment );
        
                        this.lastConsumer = old;
                    } catch ( ComponentException e ) {
                        throw new ProcessingException("Could not connect pipeline.", e);
                    }
                } else {
                    super.connectPipeline( environment );
                }
            } else {
                // we use the cache, so we need an xml deserializer
                try {
                    this.xmlDeserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                } catch ( ComponentException e ) {
                    throw new ProcessingException("Could not connect pipeline.", e);
                }
            }
        } else {
            // external: we only need to connect if we don't use a cached response
            if ( this.cachedResponse == null) {
                super.connectPipeline( environment );
            }
        }
    }

    /**
     * Prepare the pipeline 
     */
    protected void preparePipeline(Environment environment)
    throws ProcessingException {
        // get the key and the expires info
        // we must do this before we call super.preparePipeline,
        // otherwise internal pipelines are instantiated and
        // get a copy of the object model with our info!
        final Map objectModel = environment.getObjectModel();
        String key = (String)objectModel.get(CACHE_KEY_KEY);
        if ( key == null ) {
            key = this.parameters.getParameter("cache-key", null);
            if ( key == null ) {
                key = environment.getURIPrefix()+environment.getURI();
            }
        } else {
            objectModel.remove(CACHE_KEY_KEY);
        }
        String expiresValue = (String)objectModel.get(CACHE_EXPIRES_KEY);
        if ( expiresValue == null ) {
            this.cacheExpires = this.parameters.getParameterAsLong("cache-expires", 0);
        } else {
            this.cacheExpires = Long.valueOf(expiresValue).longValue();
            objectModel.remove(CACHE_EXPIRES_KEY);            
        }
        
        // prepare the pipeline
        super.preparePipeline( environment );

        // and now prepare the caching information
        this.cacheKey = new SimpleCacheKey(key, 
                                           this.serializer == this.lastConsumer);
        if ( this.cacheExpires > 0) {
            this.cacheValidity = new ExpiresValidity(this.cacheExpires*1000);                                               
        }
        final boolean purge = this.parameters.getParameterAsBoolean("purge-cache", false);
         
        this.cachedResponse = this.cache.get(this.cacheKey);
        if ( this.cachedResponse != null ) {
            final SourceValidity sv = cachedResponse.getValidityObjects()[0];
            if ( purge 
                 || (this.cacheExpires != -1 && sv.isValid() != SourceValidity.VALID) ) {
                this.cache.remove( this.cacheKey );
                this.cachedResponse = null;
            }
        }
        if ( this.cacheExpires > 0 
             && (this.reader != null || this.lastConsumer == this.serializer )) {
            Response res = ObjectModelHelper.getResponse(environment.getObjectModel());
            res.setDateHeader("Expires", System.currentTimeMillis() + (this.cacheExpires*1000));
            res.setHeader("Cache-Control", "max-age=" + this.cacheExpires + ", public");
        }
    }
    
    /**
     * Return valid validity objects for the event pipeline
     * If the "event pipeline" (= the complete pipeline without the
     * serializer) is cacheable and valid, return all validity objects.
     * Otherwise return <code>null</code>
     */
    public SourceValidity getValidityForEventPipeline() {
        return this.cacheValidity;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.pipeline.ProcessingPipeline#getKeyForEventPipeline()
     */
    public String getKeyForEventPipeline() {
        if ( this.cacheKey != null && this.cacheValidity != null) {
            return this.cacheKey.toString();
        }
        return null;
    }
    
    /**
     * Recyclable Interface
     */
    public void recycle() {
        this.cacheKey = null;
        this.cacheExpires = 0;
        this.cachedResponse = null;
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.pipeline.AbstractProcessingPipeline#processReader(org.apache.cocoon.environment.Environment)
     */
    protected boolean processReader(Environment environment)
    throws ProcessingException {
        try {
            if ( this.cachedResponse != null ) {
                
                final byte[] content = cachedResponse.getResponse();
                environment.setContentLength(content.length);
                
                final OutputStream os = environment.getOutputStream(0);
                os.write(content);
                
            } else {
                // generate new response

                if ( this.cacheExpires == 0 ) {
                    return super.processReader( environment );
                }
                
                byte[] cachedData;
                
                if (this.reader.shouldSetContentLength()) {
                    final OutputStream os = environment.getOutputStream(this.outputBufferSize);

                    // set the output stream
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    this.reader.setOutputStream(baos);
    
                    this.reader.generate();

                    cachedData = baos.toByteArray();
                    environment.setContentLength(cachedData.length);
                    os.write(cachedData);
                } else {
                    final CachingOutputStream os = new CachingOutputStream( environment.getOutputStream(this.outputBufferSize) );
                    // set the output stream
                    this.reader.setOutputStream( os );
                    this.reader.generate();
                    
                    cachedData = os.getContent();
                }

                //
                // Now that we have processed the pipeline,
                // we do the actual caching
                //
                if ( this.cacheValidity != null ) {
                    cachedResponse = new CachedResponse(this.cacheValidity,
                                                        cachedData);
                    this.cache.store(this.cacheKey,
                                     cachedResponse);
                }
            }
        } catch ( SocketException se ) {
            if (se.getMessage().indexOf("reset") > 0
                    || se.getMessage().indexOf("aborted") > 0
                    || se.getMessage().indexOf("connection abort") > 0) {
                throw new ConnectionResetException("Connection reset by peer", se);
            } else {
                throw new ProcessingException("Failed to execute pipeline.", se);
            }
        } catch ( ProcessingException pe ) {
            throw pe;
        } catch ( Exception e ) {
            throw new ProcessingException("Error executing pipeline.", e);
        }
        return true;
    }

}
