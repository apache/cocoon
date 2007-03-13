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
package org.apache.cocoon.components.pipeline.impl;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.excalibur.source.SourceValidity;

/**
 * The CachingPointProcessingPipeline
 *
 * @since 2.1
 * @version $Id$
 */
public class CachingPointProcessingPipeline
    extends AbstractCachingProcessingPipeline {

    protected ArrayList isCachePoint = new ArrayList();
    protected ArrayList xmlSerializerArray = new ArrayList();
    protected boolean nextIsCachePoint = false;
    protected String autoCachingPointSwitch;
    protected boolean autoCachingPoint = true;


   /**
    * The <code>CachingPointProcessingPipeline</code> is configurable.
    * The autoCachingPoint algorithm can be switced on/off
    * in the sitemap.xmap
    */
    public void parameterize(Parameters config) throws ParameterException {
        super.parameterize(config);
        this.autoCachingPointSwitch = config.getParameter("autoCachingPoint", null);

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("Auto caching-point is set to = '" + this.autoCachingPointSwitch + "'");
        }

        // Default is that auto caching-point is on
        if (this.autoCachingPointSwitch == null){
            this.autoCachingPoint=true;
            return;
        }

        if (this.autoCachingPointSwitch.toLowerCase().equals("on")) {
            this.autoCachingPoint=true;
        } else {
            this.autoCachingPoint=false;
        }
    }

    /**
     * Set the generator.
     */
    public void setGenerator (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        super.setGenerator(role, source, param, hintParam);

        // check the hint param for a "caching-point" hint
        String pipelinehint = null;
        try {
            pipelinehint = hintParam.getParameter("caching-point", null);

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("generator caching-point pipeline-hint is set to: " + pipelinehint);
            }
        } catch (Exception ex) {
            if (this.getLogger().isWarnEnabled()) {
                getLogger().warn("caching-point hint Exception, pipeline-hint ignored: " + ex);
            }
        }

        // if this generator is manually set to "caching-point" (via pipeline-hint)
        // then ensure the next component is caching.
        if ( "true".equals(pipelinehint)) {
            this.nextIsCachePoint=true;
        }
    }


    /**
     * Add a transformer.
     */
    public void addTransformer (String role, String source, Parameters param,  Parameters hintParam)
    throws ProcessingException {
        super.addTransformer(role, source, param, hintParam);

        // check the hint param for a "caching-point" hint
        String pipelinehint = null;
        try {
            pipelinehint = hintParam.getParameter("caching-point", null);

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("transformer caching-point pipeline-hint is set to: " + pipelinehint);
            }
        } catch (Exception ex) {
            if (this.getLogger().isWarnEnabled()) {
                getLogger().warn("caching-point hint Exception, pipeline-hint ignored: " + ex);
            }
        }

        // add caching point flag
        // default value is false
        this.isCachePoint.add(Boolean.valueOf(this.nextIsCachePoint));
        this.nextIsCachePoint = false;

        // if this transformer is manually set to "caching-point" (via pipeline-hint)
        // then ensure the next component is caching.
        if ( "true".equals(pipelinehint)) {
            this.nextIsCachePoint=true;
        }
    }


    /**
     * Determine if the given branch-point
     * is a caching-point
     *
     * Please Note: this method is used by auto caching-point
     * and is of no consequence when auto caching-point is switched off
     */
    public void informBranchPoint() {

        if (this.generator == null) {
            return;
        }
        if (!this.autoCachingPoint) {
            return;
        }

        this.nextIsCachePoint = true;
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Informed Pipeline of branch point");
        }
    }

    /**
     * Cache longest cacheable path plus cache points.
     */
    protected CachedResponse cacheResults(Environment environment, OutputStream os)  throws Exception {
    	CachedResponse completeCachedResponse = null;

        if (this.toCacheKey != null) {
            if ( this.cacheCompleteResponse ) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Cached: caching complete response; pSisze"
                                           + this.toCacheKey.size() + " Key " + this.toCacheKey);
                }
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          ((CachingOutputStream)os).getContent());
                response.setContentType(environment.getContentType());
                this.cache.store(this.toCacheKey.copy(),
                                 response);
                completeCachedResponse = response;
                //
                // Scan back along the pipelineCacheKey for
                // for any cachepoint(s)
                //
                this.toCacheKey.removeUntilCachePoint();

                //
                // adjust the validities object
                // to reflect the new length of the pipeline cache key.
                //
                // REVISIT: Is it enough to simply reduce the length of the validities array?
                //
                if (this.toCacheKey.size()>0) {
                    SourceValidity[] copy = new SourceValidity[this.toCacheKey.size()];
                    System.arraycopy(this.toCacheSourceValidities, 0,
                                     copy, 0, copy.length);
                    this.toCacheSourceValidities = copy;
                }
            }

            if (this.toCacheKey.size()>0) {
                ListIterator itt = this.xmlSerializerArray.listIterator(this.xmlSerializerArray.size());
                while (itt.hasPrevious()) {
                    XMLByteStreamCompiler serializer = (XMLByteStreamCompiler) itt.previous();
                    CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                              (byte[])serializer.getSAXFragment());
                    this.cache.store(this.toCacheKey.copy(),
                                     response);

                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Caching results for the following key: "
                            + this.toCacheKey);
                    }

                    //
                    // Check for further cachepoints
                    //
                    toCacheKey.removeUntilCachePoint();
                    if (this.toCacheKey.size()==0)
                        // no cachePoint found in key
                        break;

                    //
                    // re-calculate validities array
                    //
                    SourceValidity[] copy = new SourceValidity[this.toCacheKey.size()];
                    System.arraycopy(this.toCacheSourceValidities, 0,
                                     copy, 0, copy.length);
                    this.toCacheSourceValidities = copy;
                } //end serializer loop

            }
        }
        return completeCachedResponse;
    }

    /**
     * Create a new ComponentCachekey
     * ComponentCacheKeys can be flagged as cachepoints
     */
    protected ComponentCacheKey newComponentCacheKey(int type, String role,Serializable key) {
        boolean cachePoint = false;

        if (type == ComponentCacheKey.ComponentType_Transformer) {
            cachePoint =
                ((Boolean)this.isCachePoint.get(this.firstNotCacheableTransformerIndex)).booleanValue();
        } else if (type == ComponentCacheKey.ComponentType_Serializer) {
            cachePoint = this.nextIsCachePoint;
        }

        return new ComponentCacheKey(type, role, key, cachePoint);
    }


    /**
     * Connect the caching point pipeline.
     */
    protected void connectCachingPipeline(Environment   environment)
    throws ProcessingException {
        XMLByteStreamCompiler localXMLSerializer = null;
        XMLByteStreamCompiler cachePointXMLSerializer = null;
        if (!this.cacheCompleteResponse) {
            this.xmlSerializer = new XMLByteStreamCompiler();
            localXMLSerializer = this.xmlSerializer;
        }

        if (this.cachedResponse == null) {
            XMLProducer prev = super.generator;
            XMLConsumer next;

            int cacheableTransformerCount = this.firstNotCacheableTransformerIndex;
            int currentTransformerIndex = 0; //start with the first transformer

            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                next = (XMLConsumer) itt.next();

                // if we have cacheable transformers,
                // check the tranformers for cachepoints
                if (cacheableTransformerCount > 0) {
                    if ( (this.isCachePoint.get(currentTransformerIndex) != null)  &&
                            ((Boolean)this.isCachePoint.get(currentTransformerIndex)).booleanValue()) {

                        cachePointXMLSerializer = new XMLByteStreamCompiler();
                        next = new XMLTeePipe(next, cachePointXMLSerializer);
                        this.xmlSerializerArray.add(cachePointXMLSerializer);
                    }
                }


                // Serializer is not cacheable,
                // but we  have the longest cacheable key. Do default longest key caching
                if (localXMLSerializer != null) {
                    if (cacheableTransformerCount == 0) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        this.xmlSerializerArray.add(localXMLSerializer);
                        localXMLSerializer = null;
                    } else {
                        cacheableTransformerCount--;
                    }
                }
                this.connect(environment, prev, next);
                prev = (XMLProducer) next;

                currentTransformerIndex++;
            }
            next = super.lastConsumer;


            // if the serializer is not cacheable, but all the transformers are:
            // (this is default longest key caching)
            if (localXMLSerializer != null) {
                next = new XMLTeePipe(next, localXMLSerializer);
                this.xmlSerializerArray.add(localXMLSerializer);
                localXMLSerializer = null;
            }

            // else if the serializer is cacheable and has cocoon views
            else if ((currentTransformerIndex == this.firstNotCacheableTransformerIndex) &&
                    this.nextIsCachePoint) {
                cachePointXMLSerializer = new XMLByteStreamCompiler();
                next = new XMLTeePipe(next, cachePointXMLSerializer);
                this.xmlSerializerArray.add(cachePointXMLSerializer);
            }
            this.connect(environment, prev, next);

        } else {
            // Here the first part of the pipeline has been retrived from cache
            // we now check if any part of the rest of the pipeline can be cached
            this.xmlDeserializer = new XMLByteStreamInterpreter();
            // connect the pipeline:
            XMLProducer prev = xmlDeserializer;
            XMLConsumer next;
            int cacheableTransformerCount = 0;
            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                next = (XMLConsumer) itt.next();

                if (cacheableTransformerCount >= this.firstProcessedTransformerIndex) {

                    // if we have cacheable transformers left,
                    // then check the tranformers for cachepoints
                    if (cacheableTransformerCount < this.firstNotCacheableTransformerIndex) {
                        if ( !(prev instanceof XMLByteStreamInterpreter) &&
                                (this.isCachePoint.get(cacheableTransformerCount) != null)  &&
                                ((Boolean)this.isCachePoint.get(cacheableTransformerCount)).booleanValue()) {
                            cachePointXMLSerializer = new XMLByteStreamCompiler();
                            next = new XMLTeePipe(next, cachePointXMLSerializer);
                            this.xmlSerializerArray.add(cachePointXMLSerializer);
                        }
                    }

                    // Serializer is not cacheable,
                    // but we  have the longest cacheable key. Do default longest key caching
                    if (localXMLSerializer != null && !(prev instanceof XMLByteStreamInterpreter)
                            && cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        this.xmlSerializerArray.add(localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    this.connect(environment, prev, next);
                    prev = (XMLProducer)next;
                }
                cacheableTransformerCount++;
            }
            next = super.lastConsumer;

            //*all* the transformers are cacheable, but the serializer is not!! this is longest key
            if (localXMLSerializer != null && !(prev instanceof XMLByteStreamInterpreter)) {
                next = new XMLTeePipe(next, localXMLSerializer);
                this.xmlSerializerArray.add(localXMLSerializer);
                localXMLSerializer = null;
            } else if (this.nextIsCachePoint && !(prev instanceof XMLByteStreamInterpreter) &&
                    cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                // else the serializer is cacheable but has views
                cachePointXMLSerializer = new XMLByteStreamCompiler();
                next = new XMLTeePipe(next,  cachePointXMLSerializer);
                this.xmlSerializerArray.add(cachePointXMLSerializer);
            }
            this.connect(environment, prev, next);
        }
    }


    /**
     * Recyclable Interface
     */
    public void recycle() {
        super.recycle();

        Iterator itt = this.xmlSerializerArray.iterator();
        while (itt.hasNext()) {
            this.manager.release(itt.next());
        }

        this.isCachePoint.clear();
        this.xmlSerializerArray.clear();
        this.nextIsCachePoint = false;
        this.autoCachingPointSwitch=null;
    }
}
