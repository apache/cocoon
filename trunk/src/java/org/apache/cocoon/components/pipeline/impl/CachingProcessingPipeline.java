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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * The CachingProcessingPipeline
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachingProcessingPipeline.java,v 1.5 2004/01/28 17:23:22 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingPipeline
 * @x-avalon.lifestyle type=pooled
 */
public class CachingProcessingPipeline
    extends AbstractCachingProcessingPipeline implements ProcessingPipeline {

    /**
    * Cache longest cacheable key
    */
    protected void cacheResults(Environment environment, OutputStream os)  throws Exception {
        if (this.toCacheKey != null) {
            // See if there is an expires object for this resource.                
            Long expiresObj = (Long) environment.getObjectModel().get(ObjectModelHelper.EXPIRES_OBJECT);
            if ( this.cacheCompleteResponse ) {
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          ((CachingOutputStream)os).getContent(),
                                          expiresObj);
                this.cache.store(this.toCacheKey,
                                 response);
            } else {
                CachedResponse response = new CachedResponse(this.toCacheSourceValidities,
                                          (byte[])this.xmlSerializer.getSAXFragment(),
                                          expiresObj);
                this.cache.store(this.toCacheKey,
                                 response);
            }
        }
    }

    /**
     * create a new cache key
     */
    protected ComponentCacheKey newComponentCacheKey(int type, String role,Serializable key) {
        return new ComponentCacheKey(type, role, key);
    }


    /**
     * Connect the pipeline.
     */
    protected void connectCachingPipeline(Environment   environment)
    throws ProcessingException {
            try {
                XMLSerializer localXMLSerializer = null;
                if (!this.cacheCompleteResponse) {
                    this.xmlSerializer = (XMLSerializer)this.manager.lookup( XMLSerializer.ROLE );
                    localXMLSerializer = this.xmlSerializer;
                }
                if ( this.cachedResponse == null ) {
                    XMLProducer prev = super.generator;
                    XMLConsumer next;

                    int cacheableTransformerCount = this.firstNotCacheableTransformerIndex;

                    Iterator itt = this.transformers.iterator();
                    while ( itt.hasNext() ) {
                        next = (XMLConsumer) itt.next();
                        if (localXMLSerializer != null) {
                            if (cacheableTransformerCount == 0) {
                                next = new XMLTeePipe(next, localXMLSerializer);
                                localXMLSerializer = null;
                            } else {
                                cacheableTransformerCount--;
                            }
                        }
                        this.connect(environment, prev, next);
                        prev = (XMLProducer) next;
                    }
                    next = super.lastConsumer;
                    if (localXMLSerializer != null) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    this.connect(environment, prev, next);
                } else {
                    this.xmlDeserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                    // connect the pipeline:
                    XMLProducer prev = xmlDeserializer;
                    XMLConsumer next;
                    int cacheableTransformerCount = 0;
                    Iterator itt = this.transformers.iterator();
                    while ( itt.hasNext() ) {
                        next = (XMLConsumer) itt.next();
                        if (cacheableTransformerCount >= this.firstProcessedTransformerIndex) {
                            if (localXMLSerializer != null
                                    && cacheableTransformerCount == this.firstNotCacheableTransformerIndex) {
                                next = new XMLTeePipe(next, localXMLSerializer);
                                localXMLSerializer = null;
                            }
                            this.connect(environment, prev, next);
                            prev = (XMLProducer)next;
                        }
                        cacheableTransformerCount++;
                    }
                    next = super.lastConsumer;
                    if (localXMLSerializer != null) {
                        next = new XMLTeePipe(next, localXMLSerializer);
                        localXMLSerializer = null;
                    }
                    this.connect(environment, prev, next);
                }

            } catch ( ServiceException e ) {
                throw new ProcessingException("Could not connect pipeline.", e);
            }
    }

}
