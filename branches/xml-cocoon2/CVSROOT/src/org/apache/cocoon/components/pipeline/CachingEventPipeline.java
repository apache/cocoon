/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.Disposable;
import org.apache.avalon.configuration.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.cocoon.xml.XMLPipe;
import org.apache.cocoon.Processor;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.saxconnector.SAXConnector;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.caching.EventCache;
import org.apache.cocoon.components.sax.XMLTeePipe;

import org.apache.cocoon.caching.ComponentCacheKey;
import org.apache.cocoon.caching.PipelineCacheKey;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CachedEventObject;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * The CachingEventPipeline
 *
 * If all components of this event pipeline are cacheable then the whole
 * pipeline is also cacheable. If in this case the CacheableEventPipeline interface
 * is invoked (e.g. by the CachingStreamPipeline) the CachingEventPipeline
 * does not cache! (If it would cache, the response would be cached twice!)
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-19 11:30:38 $
 */
public final class CachingEventPipeline
extends AbstractEventPipeline
implements Disposable, CacheableEventPipeline {

    /** The store for the cached sax events */
    private EventCache eventCache;

    private XMLProducer producer;
    private String generatorRole;
    private ArrayList transformerRoles = new ArrayList();
    private ArrayList notCacheableTransformers = new ArrayList();
    private Map validityObjects;
    private PipelineCacheKey pipelineCacheKey;
    private boolean setupFinished = false;
    /** Indicates if the whole pipeline is cacheable */
    private boolean cacheable = false;
    private int firstNotCacheableTransformerIndex;

    public void compose (ComponentManager manager)
    throws ComponentManagerException {
        super.compose(manager);
        this.eventCache = (EventCache)this.manager.lookup(Roles.EVENT_CACHE);
    }

    /**
     * Set the generator.
     */
    public void setGenerator (String role, String source, Parameters param)
    throws Exception {
        super.setGenerator(role, source, param);
        this.generatorRole = role;
    }

    /**
     * Add a transformer.
     */
    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        super.addTransformer(role, source, param);
        this.transformerRoles.add(role);
    }

    /**
     * Generate the unique key.
     * This key is the <code>PipelineCacheKey</code> for the whole
     * EventPipeline.
     *
     * @param environment The current environment of the request.
     * @return The generated key or <code>null</code> if the pipeline
     *              is currently not cacheable as a whole.
     */
    public PipelineCacheKey generateKey(Environment environment)
    throws Exception {
        this.setup(environment);
        return (this.cacheable == true ? this.pipelineCacheKey : null);
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @param environment The current environment of the request.
     * @return The generated validity objects for the whole pipeline
     *         or <code>null</code> if the pipeline is currently
     *         not cacheable.
     */
    public Map generateValidity(Environment environment)
    throws Exception {
        // if we are cacheable and this method is called
        // reset the pipeline cache key to avoid duplicate caching
        if (this.cacheable == true) {
            this.pipelineCacheKey = null;
            return this.validityObjects;
        }
        return null;
    }

    public boolean process(Environment environment) throws Exception {
        this.setup(environment);

        // we cache if the pipelinecachekey is available
        XMLSerializer xmlSerializer = null;

        try {
            if (this.pipelineCacheKey != null) {
                // now we have the key to get the cached object
                CachedEventObject cachedObject = (CachedEventObject)this.eventCache.get(this.pipelineCacheKey);

                if (cachedObject != null) {
                    getLogger().debug("Found cached content.");
                    Iterator validityIterator = validityObjects.keySet().iterator();
                    ComponentCacheKey validityKey;
                    boolean valid = true;
                    while (validityIterator.hasNext() == true && valid == true) {
                        validityKey = (ComponentCacheKey)validityIterator.next();
                        valid = cachedObject.isValid(validityKey, (CacheValidity)validityObjects.get(validityKey));
                    }
                    if (valid == true) {

                        getLogger().debug("Using valid cached content.");
                        // get all transformers which are not cacheable
                        int transformerSize = this.transformers.size();
                        while (this.firstNotCacheableTransformerIndex < transformerSize) {
                            this.notCacheableTransformers.add(this.transformers.get(this.firstNotCacheableTransformerIndex));
                            this.firstNotCacheableTransformerIndex++;
                        }

                        XMLDeserializer deserializer = null;
                        try {
                            deserializer = (XMLDeserializer)this.manager.lookup(Roles.XML_DESERIALIZER);
                            // connect the pipeline:
                            this.producer = deserializer;
                            this.connectPipeline(environment,
                                                 notCacheableTransformers,
                                                 null);
                            // execute the pipeline:
                            deserializer.deserialize(cachedObject.getSAXFragment());
                        } catch ( Exception e ) {
                            throw new ProcessingException(
                                 "Failed to execute pipeline.",
                                  e
                            );
                        } finally {
                            if (deserializer != null)
                                this.manager.release((Component)deserializer);
                        }
                    } else {
                        getLogger().debug("Cached content is invalid.");
                        // remove invalid cached object
                        this.eventCache.remove(this.pipelineCacheKey);
                        cachedObject = null;
                    }
                }
                if (cachedObject == null) {
                    getLogger().debug("Caching content for further requests.");
                    xmlSerializer = (XMLSerializer)this.manager.lookup(Roles.XML_SERIALIZER);
                }
            }

            if (this.producer == null) {
                // the content was not cached/or is invalid
                this.producer = this.generator;
                this.connectPipeline(environment,
                                     this.transformers,
                                     xmlSerializer);
                // execute the pipeline:
                try {
                    this.generator.generate();
                    // did we have cacheable components?
                    if (xmlSerializer != null) {
                        this.eventCache.store(this.pipelineCacheKey,
                            new CachedEventObject(this.validityObjects,
                            xmlSerializer.getSAXFragment()));
                    }
                } catch ( Exception e ) {
                    throw new ProcessingException(
                        "Failed to execute pipeline.",
                        e
                    );
                }
            }
        } finally {
            if (xmlSerializer != null)
                this.manager.release((Component)xmlSerializer);
        }
        return true;
    }

    /**
     * Setup the evenet pipeline.
     * The components of the pipeline are checked if they are
     * Cacheable.
     */
    private void setup(Environment environment) throws Exception {
        if (this.setupFinished == true) {
            return;
        }
        if (this.checkPipeline() == false) {
            throw new ProcessingException("Attempted to process incomplete pipeline.");
        }

        // set up all sitemap components
        this.setupPipeline(environment);

        this.firstNotCacheableTransformerIndex = 0;

        // is the generator cacheable?
        if (this.generator instanceof Cacheable) {

            long key = ((Cacheable)this.generator).generateKey();
            CacheValidity validity = ((Cacheable)this.generator).generateValidity();

            // final check, the current generator might not be cacheable
            if (key != 0 && validity != null) {
                ComponentCacheKey cck = new ComponentCacheKey(
                           ComponentCacheKey.ComponentType_Generator,
                           this.generatorRole,
                           key);
                this.validityObjects = new HashMap();
                this.validityObjects.put(cck, validity);
                this.pipelineCacheKey = new PipelineCacheKey();
                this.pipelineCacheKey.addKey(cck);

                // now testing transformers
                Transformer trans;
                ComponentCacheKey transCacheKey;
                int transformerSize = this.transformers.size();
                long transKey;
                CacheValidity transValidity;
                boolean testTrans = true;

                while (this.firstNotCacheableTransformerIndex < transformerSize
                           && testTrans == true) {
                    trans = (Transformer)this.transformers.get(this.firstNotCacheableTransformerIndex);
                    if (trans instanceof Cacheable) {
                        transKey = ((Cacheable)trans).generateKey();
                        transValidity = ((Cacheable)trans).generateValidity();
                        if (transKey != 0 && transValidity != null) {
                            transCacheKey = new ComponentCacheKey(
                                 ComponentCacheKey.ComponentType_Transformer,
                                 (String)this.transformerRoles.get(this.firstNotCacheableTransformerIndex),
                                 transKey);
                            this.pipelineCacheKey.addKey(transCacheKey);
                            this.validityObjects.put(transCacheKey, transValidity);
                        } else {
                            testTrans = false;
                        }
                    } else {
                        testTrans = false;
                    }
                    if (testTrans == true)
                        this.firstNotCacheableTransformerIndex++;
                }
                // all transformers are cacheable => pipeline is cacheable
                if (this.firstNotCacheableTransformerIndex == transformerSize)
                    this.cacheable = true;
            }
        }
        this.setupFinished = true;
    }

    /** Connect the pipeline.
     */
    private void connectPipeline(Environment   environment,
                                 ArrayList     usedTransformers,
                                 XMLSerializer xmlSerializer)
    throws ProcessingException {
        XMLProducer prev = this.producer;
        XMLConsumer next;

        try {
            Iterator itt = usedTransformers.iterator();
            while ( itt.hasNext() ) {
                // connect SAXConnector
                SAXConnector connect = (SAXConnector) this.manager.lookup(Roles.SAX_CONNECTOR);
                connect.setup((EntityResolver)environment,environment.getObjectModel(),null,null);
                this.connectors.add(connect);
                next = (XMLConsumer) connect;
                prev.setConsumer(next);
                prev = (XMLProducer) connect;

                // Connect next component.
                next = (XMLConsumer) itt.next();
                if (xmlSerializer != null
                    && next instanceof Cacheable == false) {
                    next = new XMLTeePipe(next,
                                (XMLConsumer)xmlSerializer);
                    xmlSerializer = null;
                }
                prev.setConsumer(next);
                prev = (XMLProducer) next;
            }

            // insert SAXConnector
            SAXConnector connect = (SAXConnector) this.manager.lookup(Roles.SAX_CONNECTOR);
            this.connectors.add(connect);
            next = (XMLConsumer) connect;
            prev.setConsumer(next);
            prev = (XMLProducer) connect;

            // insert this consumer
            next = super.xmlConsumer;
            if (xmlSerializer != null) {
                next = new XMLTeePipe(next,
                                (XMLConsumer)xmlSerializer);
                xmlSerializer = null;
            }
            prev.setConsumer(next);
        } catch ( IOException e ) {
            throw new ProcessingException(
                "Could not connect pipeline.",
                e
            );
        } catch ( SAXException e ) {
            throw new ProcessingException(
                "Could not connect pipeline.",
                e
            );
        } catch ( ComponentManagerException e ) {
            throw new ProcessingException(
                "Could not connect pipeline.",
                e
            );
        }

    }

    public void recycle() {
        getLogger().debug("Recycling of CachingEventPipeline");

        super.recycle();

        this.producer = null;
        this.generatorRole = null;
        this.transformerRoles.clear();
        this.notCacheableTransformers.clear();
        this.validityObjects = null;
        this.pipelineCacheKey = null;
        this.setupFinished = false;
        this.cacheable = false;
    }

    public void dispose() {
        super.dispose();
        if(this.eventCache != null)
            this.manager.release((Component)this.eventCache);
    }
}
