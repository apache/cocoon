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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.excalibur.source.SourceValidity;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * The CachingPointProcessingPipeline
 *
 * @since 2.1
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @version CVS $Id: CachingPointProcessingPipeline.java,v 1.7 2004/01/28 17:23:22 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingPipeline
 * @x-avalon.lifestyle type=pooled
 */
public class CachingPointProcessingPipeline
    extends AbstractCachingProcessingPipeline implements Configurable, ProcessingPipeline {

    protected ArrayList isCachePoint = new ArrayList();
    protected ArrayList xmlSerializerArray = new ArrayList();
    protected boolean nextIsCachePoint = false;
    protected String autoCachingPointSwitch;
    protected  boolean autoCachingPoint = true;


   /**
    * The <code>CachingPointProcessingPipeline</code> is configurable.
    * The autoCachingPoint algorithm can be switced on/off
    * in the sitemap.xmap
    */
    public void configure(Configuration config) throws ConfigurationException {
        this.autoCachingPointSwitch = config.getChild("autoCachingPoint").getValue(null);

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
        }
        else {
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
        }
        catch (Exception ex)
        {
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
        }
        catch (Exception ex)
        {
            if (this.getLogger().isWarnEnabled()) {
                getLogger().warn("caching-point hint Exception, pipeline-hint ignored: " + ex);
            }
        }
	
        // add caching point flag
        // default value is false
        this.isCachePoint.add(new Boolean(this.nextIsCachePoint));
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

        if (this.generator == null)
        return;

    if (!this.autoCachingPoint)
        return;

        this.nextIsCachePoint = true;
         if (this.getLogger().isDebugEnabled()) {
           this.getLogger().debug("Informed Pipeline of branch point");
         }
    }

    /**
     * Cache longest cacheable path plus cache points.
     */
    protected void cacheResults(Environment environment, OutputStream os)  throws Exception {

        if (this.toCacheKey != null) {
            if ( this.cacheCompleteResponse ) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Cached: caching complete response; pSisze"
                                           + this.toCacheKey.size() + " Key " + this.toCacheKey);
                }
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          ((CachingOutputStream)os).getContent());
                this.cache.store(this.toCacheKey.copy(),
                                 response);
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
                    XMLSerializer serializer = (XMLSerializer) itt.previous();
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
            try {
                XMLSerializer localXMLSerializer = null;
                XMLSerializer cachePointXMLSerializer = null;
                if (!this.cacheCompleteResponse) {
                    this.xmlSerializer = (XMLSerializer)this.manager.lookup( XMLSerializer.ROLE );
                    localXMLSerializer = this.xmlSerializer;
                }
                if ( this.cachedResponse == null ) {

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

                                cachePointXMLSerializer = ((XMLSerializer)
                                this.manager.lookup( XMLSerializer.ROLE ));
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
                        cachePointXMLSerializer = ((XMLSerializer)this.manager.lookup( XMLSerializer.ROLE ));
                        next = new XMLTeePipe(next, cachePointXMLSerializer);
                        this.xmlSerializerArray.add(cachePointXMLSerializer);
                    }
                    this.connect(environment, prev, next);


                } else {
                    // Here the first part of the pipeline has been retrived from cache
                    // we now check if any part of the rest of the pipeline can be cached
                    this.xmlDeserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
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
                                if ( !(prev instanceof XMLDeserializer) &&
                                        (this.isCachePoint.get(cacheableTransformerCount) != null)  &&
                                        ((Boolean)this.isCachePoint.get(cacheableTransformerCount)).booleanValue()) {
                                    cachePointXMLSerializer = ((XMLSerializer)this.manager.lookup( XMLSerializer.ROLE ));
                                    next = new XMLTeePipe(next, cachePointXMLSerializer);
                                    this.xmlSerializerArray.add(cachePointXMLSerializer);
                                }
                            }

                            // Serializer is not cacheable,
                            // but we  have the longest cacheable key. Do default longest key caching
                            if (localXMLSerializer != null && !(prev instanceof XMLDeserializer)
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
                    if (localXMLSerializer != null && !(prev instanceof XMLDeserializer)) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        this.xmlSerializerArray.add(localXMLSerializer);
                        localXMLSerializer = null;

            }
            //	else the serializer is cacheable but has views
            else if (this.nextIsCachePoint && !(prev instanceof XMLDeserializer) &&
                            cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                        cachePointXMLSerializer = ((XMLSerializer)this.manager.lookup( XMLSerializer.ROLE ));
                        next = new XMLTeePipe(next,  cachePointXMLSerializer);
                        this.xmlSerializerArray.add(cachePointXMLSerializer);
                    }
                    this.connect(environment, prev, next);
                }

            } catch ( ServiceException e ) {
                throw new ProcessingException("Could not connect pipeline.", e);
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
