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
import org.apache.cocoon.caching.CachedObject;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * The CachingEventPipeline
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-11 10:52:33 $
 */
public class CachingEventPipeline extends AbstractEventPipeline {

    /** The store for the cached sax events */
    private EventCache saxStore;

    private XMLProducer producer;
    private String generatorRole;
    private ArrayList transformerRoles = new ArrayList();
    private ArrayList notCacheableTransformers = new ArrayList();

    public void compose (ComponentManager manager)
    throws ComponentManagerException {
        super.compose(manager);
        this.saxStore = (EventCache)this.manager.lookup(Roles.EVENT_CACHE);
    }

    public void setGenerator (String role, String source, Parameters param)
    throws Exception {
        super.setGenerator(role, source, param);
        this.generatorRole = role;
    }

    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        super.addTransformer(role, source, param);
        this.transformerRoles.add(role);
    }

    public boolean process(Environment environment) throws Exception {
        if (this.checkPipeline() == false) {
            throw new ProcessingException("Attempted to process incomplete pipeline.");
        }

        Map validityObjects = null;
        PipelineCacheKey pipelineCacheKey = null;
        XMLSerializer xmlSerializer = null;

        try {
            // set up all sitemap components
            this.setupPipeline(environment);

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
                    validityObjects = new HashMap();
                    validityObjects.put(cck, validity);
                    pipelineCacheKey = new PipelineCacheKey();
                    pipelineCacheKey.addKey(cck);

                    // now testing transformers
                    Iterator itt = this.transformers.iterator();
                    Transformer trans;
                    ComponentCacheKey transCacheKey;
                    int index = 0;
                    long transKey;
                    CacheValidity transValidity;
                    boolean testTrans = true;

                    while (itt.hasNext() == true && testTrans == true) {
                        trans = (Transformer)itt.next();
                        if (trans instanceof Cacheable) {
                            transKey = ((Cacheable)trans).generateKey();
                            transValidity = ((Cacheable)trans).generateValidity();
                            if (transKey != 0 && transValidity != null) {
                                transCacheKey = new ComponentCacheKey(
                                     ComponentCacheKey.ComponentType_Transformer,
                                     (String)this.transformerRoles.get(index),
                                     transKey);
                                pipelineCacheKey.addKey(transCacheKey);
                                validityObjects.put(transCacheKey, transValidity);
                            } else {
                                testTrans = false;
                            }
                        } else {
                            testTrans = false;
                        }
                        index++;
                    }

                    // now we have the key to get the cached object
                    CachedObject cachedObject = (CachedObject)saxStore.get(pipelineCacheKey);
                    if (cachedObject != null) {
                        Iterator validityIterator = validityObjects.keySet().iterator();
                        ComponentCacheKey validityKey;
                        boolean valid = true;
                        while (validityIterator.hasNext() == true && valid == true) {
                            validityKey = (ComponentCacheKey)validityIterator.next();
                            valid = cachedObject.isValid(validityKey, (CacheValidity)validityObjects.get(validityKey));
                        }
                        if (valid == true) {

                            // get all transformers which are not cacheable
                            itt = this.transformers.iterator();
                            while (itt.hasNext() == true) {
                                trans = (Transformer)itt.next();
                                if (trans instanceof Cacheable == false) {
                                    this.notCacheableTransformers.add(trans);
                                }
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
                            // remove invalid cached object
                            saxStore.remove(pipelineCacheKey);
                            cachedObject = null;
                        }
                    }
                    if (cachedObject == null) {
                        xmlSerializer = (XMLSerializer)this.manager.lookup(Roles.XML_SERIALIZER);
                    }
                }
            }

            if (this.producer == null) {
                // the content was not cached/or invalid
                this.producer = this.generator;
                this.connectPipeline(environment,
                                     this.transformers,
                                     xmlSerializer);
                // execute the pipeline:
                try {
                    this.generator.generate();
                    // did we have cacheable components?
                    if (xmlSerializer != null) {
                        this.saxStore.store(pipelineCacheKey,
                            new CachedObject(validityObjects,
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
    }
}
