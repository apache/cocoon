/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.Parameters;
import org.apache.avalon.Loggable;
import org.apache.log.Logger;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.saxconnector.SAXConnector;

import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.26 $ $Date: 2001-03-12 04:39:07 $
 */
public class ResourcePipeline implements Composer {
    private Generator generator;
    private Parameters generatorParam;
    private String generatorSource;
    private Exception generatorException;
    private Reader reader;
    private Parameters readerParam;
    private String readerSource;
    private String readerMimeType;
    private String sitemapReaderMimeType;
    private ArrayList transformers = new ArrayList();
    private ArrayList transformerParams = new ArrayList();
    private ArrayList transformerSources = new ArrayList();
    private ArrayList connectors = new ArrayList();
    private Serializer serializer;
    private Parameters serializerParam;
    private String serializerSource;
    private String serializerMimeType;
    private String sitemapSerializerMimeType;
    
    private Logger log;

    /** the component manager */
    private ComponentManager manager;

    public void compose (ComponentManager manager) {
        this.manager = manager;
    }
    
    public void setLogger(Logger log) {
        this.log = log;
    }

    public void setGenerator (String role, String source, Parameters param, Exception e)
    throws Exception {
        this.generatorException = e;
        this.setGenerator (role, source, param);
    }

    public void setGenerator (String role, String source, Parameters param)
    throws Exception {
        if (this.generator != null) {
            throw new ProcessingException ("Generator already set. You can only select one Generator (" + role + ")");
        }
        ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.GENERATORS);
        this.generator = (Generator) selector.select(role);
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public void setReader (String role, String source, Parameters param)
    throws Exception {
        this.setReader (role, source, param, null);
    }

    public void setReader (String role, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.reader != null) {
            throw new ProcessingException ("Reader already set. You can only select one Reader (" + role + ")");
        }
        SitemapComponentSelector selector = (SitemapComponentSelector) this.manager.lookup(Roles.READERS);
        this.reader = (Reader)selector.select(role);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
        this.sitemapReaderMimeType = selector.getMimeTypeForRole(role);
    }

    public void setSerializer (String role, String source, Parameters param)
    throws Exception {
        this.setSerializer (role, source, param, null);
    }

    public void setSerializer (String role, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.serializer != null) {
            throw new ProcessingException ("Serializer already set. You can only select one Serializer (" + role + ")");
        }
        SitemapComponentSelector selector = (SitemapComponentSelector) this.manager.lookup(Roles.SERIALIZERS);
        this.serializer = (Serializer)selector.select(role);
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.sitemapSerializerMimeType = selector.getMimeTypeForRole(role);
    }

    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.TRANSFORMERS);
        this.transformers.add ((Transformer)selector.select(role));
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }
    
    public boolean process(Environment environment)
    throws ProcessingException {
        if ( this.reader != null ) {
            return processReader(environment);
        } else {
            if ( !checkPipeline() ) {
                throw new ProcessingException("Attempted to process incomplete pipeline.");
            }
            
            setupPipeline(environment);
            connectPipeline();
            
            // execute the pipeline:
            try {
                this.generator.generate();
            } catch ( Exception e ) {
                throw new ProcessingException(
                    "Failed to execute pipeline.",
                    e
                );
            }
            
            return true;
        }
    }
    
    /** Process the pipeline using a reader.
     * @throws ProcessingException if 
     */
    private boolean processReader(Environment environment)
    throws ProcessingException {
        String mimeType;
        try {
            this.reader.setup((EntityResolver) environment,environment.getObjectModel(),readerSource,readerParam);
            mimeType = this.reader.getMimeType();
            if ( mimeType != null ) {
                environment.setContentType(mimeType);
            } else if ( readerMimeType != null ) {
                environment.setContentType(this.readerMimeType);
            } else {
                environment.setContentType(this.sitemapReaderMimeType);
            }
            this.reader.setOutputStream(environment.getOutputStream());
            this.reader.generate();
        } catch ( Exception e ) {
            throw new ProcessingException("Error reading resource",e);
        }
        return true;
    }
    
    /** Sanity check the non-reader pipeline.
     * @return true if the pipeline is 'sane', false otherwise.
     */
    private boolean checkPipeline() {
        if ( this.generator == null ) {
            return false;
        }
        
        if ( this.serializer == null ) {
            return false;
        }
        
        Iterator itt = this.transformers.iterator();
        while ( itt.hasNext() ) {
            if ( itt.next() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    /** Setup pipeline components.
     */
    private void setupPipeline(Environment environment)
    throws ProcessingException {
        try {
            // setup the generator
            this.generator.setup(
                (EntityResolver)environment,
                environment.getObjectModel(),
                generatorSource,
                generatorParam
            );

            Iterator transformerItt = this.transformers.iterator();
            Iterator transformerSourceItt = this.transformerSources.iterator();
            Iterator transformerParamItt = this.transformerParams.iterator();

            while ( transformerItt.hasNext() ) {
                Transformer trans = (Transformer)transformerItt.next();
                trans.setup(
                    (EntityResolver)environment,
                    environment.getObjectModel(),
                    (String)transformerSourceItt.next(),
                    (Parameters)transformerParamItt.next()
                );
            }
            
            this.serializer.setOutputStream(environment.getOutputStream());
            String mimeType = this.serializer.getMimeType();
            if (mimeType != null) {
                // we have a mimeType freom the component itself
                environment.setContentType (mimeType);
            } else if (serializerMimeType != null) {
                // there was a mimeType specified in the sitemap pipeline
                environment.setContentType (serializerMimeType);
            } else {
                // use the mimeType specified in the sitemap component declaration
                environment.setContentType (this.sitemapSerializerMimeType);
            }
        } catch (SAXException e) {
            throw new ProcessingException(
                "Could not setup pipeline.",
                e
            );
        } catch (IOException e) {
            throw new ProcessingException(
                "Could not setup pipeline.",
                e
            );
        }
        
        
    }
    
    /** Connect the pipeline.
     */
    private void connectPipeline() throws ProcessingException {
        XMLProducer prev = (XMLProducer) this.generator;
        XMLConsumer next;
        
        try {
            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                // connect SAXConnector
                SAXConnector connect = (SAXConnector)
                    this.manager.lookup(Roles.SAX_CONNECTOR);
                this.connectors.add(connect);
                next = (XMLConsumer) connect;
                prev.setConsumer(next);
                prev = (XMLProducer) connect;

                // Connect next component.
                Transformer trans = (Transformer) itt.next();
                next = (XMLConsumer) trans;
                prev.setConsumer(next);
                prev = (XMLProducer) trans;
            }

            // insert SAXConnector
            SAXConnector connect = (SAXConnector)
                this.manager.lookup(Roles.SAX_CONNECTOR);
            this.connectors.add(connect);
            next = (XMLConsumer) connect;
            prev.setConsumer(next);
            prev = (XMLProducer) connect;

            // connect serializer.
            prev.setConsumer(this.serializer);
        } catch ( ComponentManagerException e ) {
            throw new ProcessingException(
                "Could not connect pipeline.",
                e
            );
        }
        
    }
    
    public void dispose() {
        this.log.debug("Disposing of ResourcePipeline");
        
        try {
            // release reader.
            if ( this.reader != null ) {
                ((ComponentSelector) this.manager.lookup(Roles.READERS))
                    .release(this.reader);
            }

            // release generator
            if ( this.generator != null ) {
                ((ComponentSelector) this.manager.lookup(Roles.GENERATORS))
                    .release(this.generator);
            }

            // release serializer
            if ( this.serializer != null ) {
                ((ComponentSelector) this.manager.lookup(Roles.SERIALIZERS))
                    .release(this.serializer);
            }

            // Release transformers
            ComponentSelector transformerSelector;
            transformerSelector = (ComponentSelector)this.manager.lookup(Roles.TRANSFORMERS);
            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                transformerSelector.release((Component)itt.next());
            }
            this.transformers.clear();
        } catch ( ComponentManagerException e ) {
            this.log.warn(
                "Failed to release components from resource pipeline.",
                e
            );
        } finally {
            this.reader = null;
            this.generator = null;
            this.serializer = null;
            this.transformers.clear();
        }
        
        // Release connectors
        Iterator itt = this.connectors.iterator();
        while ( itt.hasNext() ) {
            this.manager.release((Component) itt.next());
        }
        this.connectors.clear();
    }
}
