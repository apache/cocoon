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
import java.io.Serializable;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.*;
import org.apache.cocoon.components.pipeline.AbstractProcessingPipeline;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.transformation.Transformer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.source.impl.validity.DeferredValidity;

/**
 * This is the base class for all caching pipeline implementations.
 *
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @version CVS $Id: AbstractCachingProcessingPipeline.java,v 1.2 2003/03/19 15:42:15 cziegeler Exp $
 */
public abstract class AbstractCachingProcessingPipeline
            extends AbstractProcessingPipeline
    implements Disposable {

    /** This is the Cache holding cached responses */
    protected Cache  cache;

    /** The role name of the generator */
    protected String generatorRole;

    /** The role names of the transfomrers */
    protected ArrayList transformerRoles = new ArrayList();

    /** The role name of the serializer */
    protected String serializerRole;

    /** The role name of the reader */
    protected String readerRole;

    /** The deserializer */
    protected XMLDeserializer xmlDeserializer;
    /** The serializer */
    protected XMLSerializer xmlSerializer;

    /** The cached byte stream */
    protected byte[]           cachedResponse;
    /** The index indicating the first transformer getting input from the cache */
    protected int firstProcessedTransformerIndex;
    /** Complete response is cached */
    protected boolean completeResponseIsCached;


    /** This key indicates the response that is fetched from the cache */
    protected PipelineCacheKey fromCacheKey;
    /** This key indicates the response that will get into the cache */
    protected PipelineCacheKey toCacheKey;
    /** The source validities used for caching */
    protected SourceValidity[] toCacheSourceValidities;
    
    /** The index indicating to the first transformer which is not cacheable */
    protected int firstNotCacheableTransformerIndex;
    /** Cache complete response */
    protected boolean cacheCompleteResponse;

    protected boolean   generatorIsCacheableProcessingComponent;
    protected boolean   serializerIsCacheableProcessingComponent;
    protected boolean[] transformerIsCacheableProcessingComponent;

    /** Smart caching ? */
    protected boolean doSmartCaching;
    /** Default setting for smart caching */
    protected boolean configuredDoSmartCaching;
    
    /**
     * Abstract methods defined in subclasses
     */
    protected abstract void cacheResults(Environment environment, OutputStream os)  throws Exception;
    protected abstract ComponentCacheKey newComponentCacheKey(int type, String role,Serializable key);
    protected abstract void connectCachingPipeline(Environment   environment) throws ProcessingException;

    /**
     * Composable Interface
     */
    public void compose (ComponentManager manager)
    throws ComponentException {
        super.compose(manager);
        this.cache = (Cache)this.manager.lookup(Cache.ROLE);
    }

    /**
     * Parameterizable Interface - Configuration
     */
    public void parameterize(Parameters params) {
        super.parameterize(params);
        this.configuredDoSmartCaching = params.getParameterAsBoolean("smart-caching", true);
    }
    
    /**
     * Setup this component
     */
    public void setup(Parameters params) {
        super.setup(params);
        this.doSmartCaching = params.getParameterAsBoolean("smart-caching",
                                                           this.configuredDoSmartCaching);
    }

    /**
     * Set the generator.
     */
    public void setGenerator (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        super.setGenerator(role, source, param, hintParam);
        this.generatorRole = role;
    }

    /**
     * Add a transformer.
     */
    public void addTransformer (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        super.addTransformer(role, source, param, hintParam);
        this.transformerRoles.add(role);
    }


    /**
     * Set the serializer.
     */
    public void setSerializer (String role, String source, Parameters param, Parameters hintParam, String mimeType)
    throws ProcessingException {
        super.setSerializer(role, source, param, hintParam, mimeType);
        this.serializerRole = role;
    }

    /**
     * Set the Reader.
     */
    public void setReader (String role, String source, Parameters param, String mimeType)
    throws ProcessingException {
        super.setReader(role, source, param, mimeType);
        this.readerRole = role;
    }

    /**
     * Process the given <code>Environment</code>, producing the output.
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {
        if (this.toCacheKey == null && this.cachedResponse == null) {
            return super.processXMLPipeline( environment );
        } else if (this.cachedResponse != null && this.completeResponseIsCached) {
            try {
                final OutputStream outputStream = environment.getOutputStream(0);
                if (this.cachedResponse.length > 0) {
                    environment.setContentLength(this.cachedResponse.length);
                    outputStream.write(this.cachedResponse);
                }
            } catch ( SocketException se ) {
                if (se.getMessage().indexOf("reset") > 0
                        || se.getMessage().indexOf("aborted") > 0) {
                    throw new ConnectionResetException("Connection reset by peer", se);
                } else {
                    throw new ProcessingException("Failed to execute reader pipeline.", se);
                }
            } catch ( Exception e ) {
                if (e instanceof ProcessingException)
                    throw (ProcessingException)e;
                throw new ProcessingException("Error executing reader pipeline.",e);
            }
        } else {

            if (this.getLogger().isDebugEnabled() && this.toCacheKey != null) {
                this.getLogger().debug("Caching content for further requests of '" + environment.getURI() + "' using key " + this.toCacheKey);
            }
            try {
                OutputStream os = null;
                
                if ( this.cacheCompleteResponse && this.toCacheKey != null) {
                    os = new CachingOutputStream( environment.getOutputStream(this.outputBufferSize) );
                }
                if ( super.serializer != super.lastConsumer ) {
                    if (os == null) {
                        os = environment.getOutputStream(this.outputBufferSize);
                    }
                    // internal processing
                    if ( this.xmlDeserializer != null ) {
                        this.xmlDeserializer.deserialize(this.cachedResponse);
                    } else {
                        this.generator.generate();
                    }
                } else {
                    if (this.serializer.shouldSetContentLength()) {
                        if (os == null) {
                            os = environment.getOutputStream(0);
                        }
                        // set the output stream
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        this.serializer.setOutputStream(baos);
    
                        // execute the pipeline:
                        if ( this.xmlDeserializer != null ) {
                            this.xmlDeserializer.deserialize(this.cachedResponse);
                        } else {
                            this.generator.generate();
                        }
                        byte[] data = baos.toByteArray();
                        environment.setContentLength(data.length);
                        os.write(data);
                    } else {
                        if (os == null) {
                            os = environment.getOutputStream(this.outputBufferSize);
                        }
                        // set the output stream
                        this.serializer.setOutputStream( os );
                        // execute the pipeline:
                        if ( this.xmlDeserializer != null ) {
                            this.xmlDeserializer.deserialize(this.cachedResponse);
                        } else {
                            this.generator.generate();
                        }
                    }
                }
                //
                // Now that we have processed the pipeline,
                // we do the actual caching
                //
                this.cacheResults(environment,os);

            } catch ( SocketException se ) {
                if (se.getMessage().indexOf("reset") > 0
                        || se.getMessage().indexOf("aborted") > 0) {
                    throw new ConnectionResetException("Connection reset by peer", se);
                } else {
                    throw new ProcessingException("Failed to execute reader pipeline.", se);
                }
            } catch ( ProcessingException e ) {
                throw e;
            } catch ( Exception e ) {
                throw new ProcessingException("Failed to execute pipeline.", e);
            }
            return true;
        }
        return true;
    }

    /**
     * The components of the pipeline are checked if they are Cacheable.
     */
    protected void generateCachingKey(Environment environment)
    throws ProcessingException {

        this.toCacheKey = null;

        Serializable key = null;
        this.generatorIsCacheableProcessingComponent = false;
        this.serializerIsCacheableProcessingComponent = false;
        this.transformerIsCacheableProcessingComponent = new boolean[this.transformers.size()];

        this.firstNotCacheableTransformerIndex = 0;
        this.cacheCompleteResponse = false;

        // first step is to generate the key:
        // All pipeline components starting with the generator
        // are tested if they are either a CacheableProcessingComponent
        // or Cacheable (deprecated). The returned keys are chained together
        // to build a unique key of the request

        // is the generator cacheable?
        if (super.generator instanceof CacheableProcessingComponent) {
            key = ((CacheableProcessingComponent)super.generator).getKey();
            this.generatorIsCacheableProcessingComponent = true;
        } else if (super.generator instanceof Cacheable) {
            key = new Long(((Cacheable)super.generator).generateKey());
        }

        if (key != null) {
            this.toCacheKey = new PipelineCacheKey();
            this.toCacheKey.addKey(this.newComponentCacheKey(ComponentCacheKey.ComponentType_Generator,
                                       this.generatorRole,
                                       key)
                              );

            // now testing transformers
            final int transformerSize = super.transformers.size();
            boolean continueTest = true;

            while (this.firstNotCacheableTransformerIndex < transformerSize
                    && continueTest) {
                final Transformer trans =
                    (Transformer)super.transformers.get(this.firstNotCacheableTransformerIndex);
                key = null;
                if (trans instanceof CacheableProcessingComponent) {
                    key = ((CacheableProcessingComponent)trans).getKey();
                    this.transformerIsCacheableProcessingComponent[this.firstNotCacheableTransformerIndex] = true;
                } else if (trans instanceof Cacheable) {
                    key = new Long(((Cacheable)trans).generateKey());
                }
                if (key != null) {
                    this.toCacheKey.addKey(this.newComponentCacheKey(ComponentCacheKey.ComponentType_Transformer,
                                                 (String)this.transformerRoles.get(this.firstNotCacheableTransformerIndex),
                                                 key));

                    this.firstNotCacheableTransformerIndex++;
                } else {
                    continueTest = false;
                }
            }
            // all transformers are cacheable => pipeline is cacheable 
            // test serializer if this is not an internal request
            if (this.firstNotCacheableTransformerIndex == transformerSize
                && super.serializer == this.lastConsumer) {

                key = null;
                if (super.serializer instanceof CacheableProcessingComponent) {
                    key = ((CacheableProcessingComponent)this.serializer).getKey();
                    this.serializerIsCacheableProcessingComponent = true;
                } else if (this.serializer instanceof Cacheable) {
                    key = new Long(((Cacheable)this.serializer).generateKey());
                }
                if (key != null) {
                    this.toCacheKey.addKey(this.newComponentCacheKey(ComponentCacheKey.ComponentType_Serializer,
                                                 (String)this.serializerRole,
                                                 key)
                                                );
                    this.cacheCompleteResponse = true;
                }
            }
        }
    }

    /**
     * Calculate the key that can be used to get something from the cache, and 
     * handle expires properly.
     * 
     */
    protected void validatePipeline(Environment environment)
    throws ProcessingException {
        this.completeResponseIsCached = this.cacheCompleteResponse;
        this.fromCacheKey = this.toCacheKey.copy();        
        this.firstProcessedTransformerIndex = this.firstNotCacheableTransformerIndex;

        boolean finished = false;
        
        while (this.fromCacheKey != null && !finished) {
            
            finished = true;
            final CachedResponse response = this.cache.get( this.fromCacheKey );

            // now test validity
            if (response != null) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug(
                        "Found cached response for '" + environment.getURI() + 
                        "' using key: " + this.fromCacheKey
                    );
                }

                boolean responseIsValid = true;
                boolean responseIsUsable = true;

                // See if we have an explicit "expires" setting. If so,
                // and if it's still fresh, we're done.
                Long responseExpires = (Long) response.getExpires();

                if (responseExpires != null) {
                    if (this.getLogger().isDebugEnabled()) {
                       this.getLogger().debug(
                       "Expires time found for " +
                       environment.getURI());
                    }
                    if ( responseExpires.longValue() > System.currentTimeMillis()) {
                        if (this.getLogger().isDebugEnabled()) {
                            this.getLogger().debug(
                                "Expires time still fresh for " +
                                environment.getURI() +
                                ", ignoring all other cache settings. This entry expires on "+
                                new Date(responseExpires.longValue()));
                        }
                        this.cachedResponse = response.getResponse();
                        return;
                    } else {
                        if (this.getLogger().isDebugEnabled()) {
                            this.getLogger().debug(
                                "Expires time has expired for " +
                                environment.getURI() +
                                " regenerating content.");
                        }
                        
                        // If an expires parameter was provided, use it. If this parameter is not available
                        // it means that the sitemap was modified, and the old expires value is not valid
                        // anymore.
                        if (expires != 0) {
                     
                            if (this.getLogger().isDebugEnabled())
                                this.getLogger().debug("Refreshing expires informations");
                     
                            response.setExpires(new Long(expires + System.currentTimeMillis()));    
                     
                        } else {
                     
                            if (this.getLogger().isDebugEnabled())
                                this.getLogger().debug("No expires defined anymore for this object, setting it to no expires");
                     
                            response.setExpires(null);
                        }                                   
                    }
                } else {
                    // The response had no expires informations. See if it needs to be set (i.e. because the configuration has changed)
                    if (expires != 0) {
                        if (this.getLogger().isDebugEnabled())
                            this.getLogger().debug("Setting a new expires object for this resource");
                        response.setExpires(new Long(expires + System.currentTimeMillis()));                                
                    }        
                }
                
                SourceValidity[] fromCacheValidityObjects = response.getValidityObjects();

                int i = 0;
                while (responseIsValid && i < fromCacheValidityObjects.length) {
                    boolean isValid = false;
                    // BH check if validities[i] is null, may happen
                    // if exception was thrown due to malformed content
                    SourceValidity validity = fromCacheValidityObjects[i];
                    int valid = validity != null ? validity.isValid() : -1;
                    if ( valid == 0) { // don't know if valid, make second test
                       
                        validity = this.getValidityForInternalPipeline(i);

                        if (validity != null) {
                            valid = fromCacheValidityObjects[i].isValid( validity );
                            if (valid == 0) {
                                validity = null;
                            } else {
                                isValid = (valid == 1);
                            }
                        }
                    } else {
                        isValid = (valid == 1);
                    }
                    if ( !isValid ) {
                        responseIsValid = false;
                        // update validity
                        if (validity == null)
                            responseIsUsable = false;
                    } else {                        
                        i++;
                    }
                }
                if ( responseIsValid ) {
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Using valid cached content for '" + environment.getURI() + "'.");
                    }
                    // we are valid, ok that's it
                    this.cachedResponse = response.getResponse();
                } else {
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Cached content is invalid for '" + environment.getURI() + "'.");
                    }
                    // we are not valid!

                    if (!responseIsUsable) {
                        // we could not compare, because we got no
                        // validity object, so shorten pipeline key
                        if (i > 0) {
                            int deleteCount = fromCacheValidityObjects.length - i;
                            if (i > 0 && i <= firstNotCacheableTransformerIndex + 1) {
                                this.firstNotCacheableTransformerIndex = i-1;
                            }
                            for(int x=0; x < deleteCount; x++) {
                                this.toCacheKey.removeLastKey();
                            }
                            finished = false;
                        } else {
                            this.toCacheKey = null;
                        }
                        this.cacheCompleteResponse = false;
                    } else {
                        // the entry is invalid, remove it
                        this.cache.remove( this.fromCacheKey );
                    }

                    // try a shorter key
                    if (i > 0) {
                        this.fromCacheKey.removeLastKey();
                        if (!this.completeResponseIsCached) {
                            this.firstProcessedTransformerIndex--;
                        }
                    } else {
                        this.fromCacheKey = null;
                    }
                    finished = false;
                    this.completeResponseIsCached = false;
                }
            } else {
                
                // no cached response found
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug(
                        "Cached response not found for '" + environment.getURI() + 
                        "' using key: " +  this.fromCacheKey
                    );
                }
               
                if (!this.doSmartCaching) {
                    // try a shorter key
                    if (this.fromCacheKey.size() > 1) {
                        this.fromCacheKey.removeLastKey();
                        if (!this.completeResponseIsCached) {
                            this.firstProcessedTransformerIndex--;
                        }
                        finished = false;
                    } else {
                        this.fromCacheKey = null;
                    }
                } else {
                    // stop on longest key for smart caching
                    this.fromCacheKey = null;
                }
                this.completeResponseIsCached = false;
            }
        }

    }

    /**
     * Setup the evenet pipeline.
     * The components of the pipeline are checked if they are
     * Cacheable.
     */
    protected void setupPipeline(Environment environment)
    throws ProcessingException {
        super.setupPipeline( environment );

        // generate the key to fill the cache
        this.generateCachingKey(environment);

        // test the cache for a valid response
        if (this.toCacheKey != null) {
            this.validatePipeline(environment);
        }
        
        // now generate validity objects for the new response
        if (this.toCacheKey != null) {
            // only update validity objects if we cannot use
            // a cached response or when the cached response does
            // cache less than now is cacheable
            if (this.fromCacheKey == null 
                || this.fromCacheKey.size() < this.toCacheKey.size()) {
                
                this.toCacheSourceValidities = new SourceValidity[this.toCacheKey.size()];
                int len = this.toCacheSourceValidities.length;
                int i = 0;
                while (i < len) {
                    final SourceValidity validity = this.getValidityForInternalPipeline(i);

                    if (validity == null) {
                        if (i > 0 
                            && (this.fromCacheKey == null || i > this.fromCacheKey.size())) {
                            // shorten key
                            for(int m=i; m < this.toCacheSourceValidities.length; m++) {
                                this.toCacheKey.removeLastKey();
                                if (!this.cacheCompleteResponse) {
                                    this.firstNotCacheableTransformerIndex--;
                                }
                                this.cacheCompleteResponse = false;
                            }
                            SourceValidity[] copy = new SourceValidity[i];
                            System.arraycopy(this.toCacheSourceValidities, 0,
                                             copy, 0, copy.length);
                            this.toCacheSourceValidities = copy;
                            len = this.toCacheSourceValidities.length;
                        } else {
                            // caching is not possible!
                            this.toCacheKey = null;
                            this.toCacheSourceValidities = null;
                            this.cacheCompleteResponse = false;
                            len = 0;
                        }
                    } else {
                        this.toCacheSourceValidities[i] = validity;
                    }
                    i++;
                }
            } else {
                // we don't have to cache
                this.toCacheKey = null;
                this.cacheCompleteResponse = false;
            }
        }
    }

    /**
     * Connect the pipeline.
     */
    protected void connectPipeline(Environment   environment)
    throws ProcessingException {
        if ( this.toCacheKey == null && this.cachedResponse == null) {
            super.connectPipeline( environment );
            return;
        } else if (this.completeResponseIsCached) {
            // do nothing
            return;
        } else {
            this.connectCachingPipeline(environment);
        }
    }

    /** Process the pipeline using a reader.
     * @throws ProcessingException if an error occurs
     */
    protected boolean processReader(Environment  environment)
    throws ProcessingException {
        try {
            boolean usedCache = false;
            OutputStream outputStream = null;
            SourceValidity readerValidity = null;
            PipelineCacheKey pcKey = null;

            // test if reader is cacheable
            Serializable readerKey = null;
            boolean isCacheableProcessingComponent = false;
            if (super.reader instanceof CacheableProcessingComponent) {
                readerKey = ((CacheableProcessingComponent)super.reader).getKey();
                isCacheableProcessingComponent = true;
            } else if (super.reader instanceof Cacheable) {
                readerKey = new Long(((Cacheable)super.reader).generateKey());
            }

            if ( readerKey != null) {
                // response is cacheable, build the key
                pcKey = new PipelineCacheKey();
                pcKey.addKey(new ComponentCacheKey(ComponentCacheKey.ComponentType_Reader,
                                                   this.readerRole,
                                                   readerKey)
                            );

                // now we have the key to get the cached object
                CachedResponse cachedObject = (CachedResponse)this.cache.get( pcKey );

                if (cachedObject != null) {
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug(
                            "Found cached response for '" + environment.getURI() +
                            "' using key: " + pcKey
                        );
                    }
                    SourceValidity[] validities = cachedObject.getValidityObjects();
                    if (validities == null || validities.length != 1) {
                        throw new ProcessingException("Cached response is not correct.");
                    }
                    SourceValidity cachedValidity = validities[0];
                    int result = cachedValidity.isValid();
                    boolean valid = false;
                    if ( result == 0 ) {
                        // get reader validity and compare
                        if (isCacheableProcessingComponent) {
                            readerValidity = ((CacheableProcessingComponent)super.reader).getValidity();
                        } else {
                            CacheValidity cv = ((Cacheable)super.reader).generateValidity();
                            if ( cv != null ) {
                                readerValidity = CacheValidityToSourceValidity.createValidity( cv );
                            }
                        }
                        if (readerValidity != null) {
                            result = cachedValidity.isValid(readerValidity);
                            if ( result == 0 ) {
                                readerValidity = null;
                            } else {
                                valid = (result == 1);
                            }
                        }
                    } else {
                        valid = (result > 0);
                    }

                    if (valid) {
                        if (this.getLogger().isDebugEnabled()) {
                            this.getLogger().debug("Using valid cached content for '" + environment.getURI() + "'.");
                        }
                        byte[] response = cachedObject.getResponse();
                        if (response.length > 0) {
                            usedCache = true;
                            outputStream = environment.getOutputStream(0);
                            environment.setContentLength(response.length);
                            outputStream.write(response);
                        }
                    } else {
                        if (this.getLogger().isDebugEnabled()) {
                            this.getLogger().debug("Cached content is invalid for '" + environment.getURI() + "'.");
                        }
                        // remove invalid cached object
                        this.cache.remove(pcKey);
                    }
                }
            }

            if (!usedCache) {

                if ( pcKey != null ) {
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Caching content for further requests of '" + environment.getURI() + "'.");
                    }
                    if (readerValidity == null) {
                        if (isCacheableProcessingComponent) {
                            readerValidity = ((CacheableProcessingComponent)super.reader).getValidity();
                        } else {
                            CacheValidity cv = ((Cacheable)super.reader).generateValidity();
                            if ( cv != null ) {
                                readerValidity = CacheValidityToSourceValidity.createValidity( cv );
                            }
                        }
                    }
                    if (readerValidity != null) {
                        outputStream = environment.getOutputStream(this.outputBufferSize);
                        outputStream = new CachingOutputStream(outputStream);
                    } else {
                        pcKey = null;
                    }
                }


                if (this.reader.shouldSetContentLength()) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    this.reader.setOutputStream(os);
                    this.reader.generate();
                    byte[] data = os.toByteArray();
                    environment.setContentLength(data.length);
                    if (outputStream == null) {
                        outputStream = environment.getOutputStream(0);
                    }
                    environment.getOutputStream(0).write(data);
                } else {
                    if (outputStream == null) {
                        outputStream = environment.getOutputStream(this.outputBufferSize);
                    }
                    this.reader.setOutputStream(outputStream);
                    this.reader.generate();
                }

                // store the response
                if (pcKey != null) {
                    this.cache.store(
                        environment.getObjectModel(),
                        pcKey,
                        new CachedResponse( new SourceValidity[] {readerValidity},
                                            ((CachingOutputStream)outputStream).getContent())
                    );
                }
            }
        } catch ( SocketException se ) {
            if (se.getMessage().indexOf("reset") > 0
                    || se.getMessage().indexOf("aborted") > 0) {
                throw new ConnectionResetException("Connection reset by peer", se);
            } else {
                throw new ProcessingException("Failed to execute pipeline.", se);
            }
        } catch ( ProcessingException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new ProcessingException("Failed to execute pipeline.", e);
        }

        return true;
    }


    /**
     * Return valid validity objects for the event pipeline
     * If the "event pipeline" (= the complete pipeline without the
     * serializer) is cacheable and valid, return all validity objects.
     * Otherwise return <code>null</code>
     */
    public SourceValidity getValidityForEventPipeline() {
        if ( null != this.toCacheKey 
             && !this.completeResponseIsCached
             && this.firstNotCacheableTransformerIndex == super.transformers.size()) {
            AggregatedValidity validity = new AggregatedValidity();
            for(int i=0; i < this.toCacheKey.size(); i++) {
                validity.add(this.getValidityForInternalPipeline(i));
                //validity.add(new DeferredPipelineValidity(this, i));
            }
            return validity;
        }
        return null;
    }

    /**
     * 
     * @see org.apache.cocoon.components.pipeline.ProcessingPipeline#getValidityForInternalPipeline(int)
     */
    SourceValidity getValidityForInternalPipeline(int index) {
        final SourceValidity validity;
        if (index == 0) {
            // test generator
            if (this.generatorIsCacheableProcessingComponent) {
                validity = ((CacheableProcessingComponent)super.generator).getValidity();
            } else {
                validity = CacheValidityToSourceValidity.createValidity(((Cacheable)super.generator).generateValidity());
            }
        } else if (index <= firstNotCacheableTransformerIndex) {
            // test transformer
            final Transformer trans = (Transformer)super.transformers.get(index-1);
            if (this.transformerIsCacheableProcessingComponent[index-1]) {
                validity = ((CacheableProcessingComponent)trans).getValidity();
            } else {
                validity = CacheValidityToSourceValidity.createValidity(((Cacheable)trans).generateValidity());
            }
        } else {
            // test serializer
            if (this.serializerIsCacheableProcessingComponent) {
                validity = ((CacheableProcessingComponent)super.serializer).getValidity();
            } else {
                validity = CacheValidityToSourceValidity.createValidity(((Cacheable)super.serializer).generateValidity());
            }
        }
        return validity;
    }
    
    /**
     * Recyclable Interface
     */
    public void recycle() {
        super.recycle();

        this.manager.release( this.xmlDeserializer );
        this.xmlDeserializer = null;

        this.manager.release( this.xmlSerializer );
        this.xmlSerializer = null;

        this.generatorRole = null;
        this.transformerRoles.clear();
        this.serializerRole = null;
        this.readerRole = null;

        this.fromCacheKey = null;
        this.cachedResponse = null;
        
        this.transformerIsCacheableProcessingComponent = null;
        this.toCacheKey = null;
        this.toCacheSourceValidities = null;
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        if ( null != this.manager ) {
            this.manager.release(this.cache);
        }
        this.cache = null;
        this.manager = null;
    }
}

final class DeferredPipelineValidity implements DeferredValidity {

    private final AbstractCachingProcessingPipeline pipeline;
    private final int index;
    
    public DeferredPipelineValidity(AbstractCachingProcessingPipeline pipeline, int index) {
        this.pipeline = pipeline;
        this.index = index;
    }
    
    /**
     * @see org.apache.excalibur.source.impl.validity.DeferredValidity#getValidity()
     */
    public SourceValidity getValidity() {
        return pipeline.getValidityForInternalPipeline(this.index);
    }
}
