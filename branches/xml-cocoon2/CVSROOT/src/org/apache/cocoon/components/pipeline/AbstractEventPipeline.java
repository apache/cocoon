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
import org.apache.cocoon.Processor;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.saxconnector.SAXConnector;

import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@Carsten Ziegeler">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-12 12:30:34 $
 */
public abstract class AbstractEventPipeline
extends AbstractXMLProducer
implements EventPipeline {

    protected Generator generator;
    protected Parameters generatorParam;
    protected String generatorSource;
    protected ArrayList transformers = new ArrayList();
    protected ArrayList transformerParams = new ArrayList();
    protected ArrayList transformerSources = new ArrayList();
    protected ArrayList connectors = new ArrayList();

    /** the component manager */
    protected ComponentManager manager;

    public void compose (ComponentManager manager)
    throws ComponentManagerException {
        this.manager = manager;
    }

    public void setGenerator (String role, String source, Parameters param, Exception e)
    throws Exception {
        this.setGenerator (role, source, param);
        // FIXME(CZ) What can be done if this is not an ErrorNotifier?
        // (The sitemap uses this setGenerator() method only from inside
        // the error pipeline, when a ErrorNotifier is explicitly generated.)
        if (this.generator instanceof ErrorNotifier) {
            ((ErrorNotifier)this.generator).setException(e);
        }
    }

    public void setGenerator (String role, String source, Parameters param)
    throws Exception {
        if (this.generator != null) {
            throw new ProcessingException ("Generator already set. You can only select one Generator (" + role + ")");
        }
        ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.GENERATORS);
        this.generator = (Generator) selector.select(role);
        this.manager.release((Component)selector);
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.TRANSFORMERS);
        this.transformers.add ((Transformer)selector.select(role));
        this.manager.release((Component)selector);
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process(Environment environment) throws Exception {
        if ( !checkPipeline() ) {
            throw new ProcessingException("Attempted to process incomplete pipeline.");
        }

        setupPipeline(environment);
        connectPipeline(environment);

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

    /** Sanity check the non-reader pipeline.
     * @return true if the pipeline is 'sane', false otherwise.
     */
    protected boolean checkPipeline() {
        if ( this.generator == null ) {
            return false;
        }

        Iterator itt = this.transformers.iterator();
        while ( itt.hasNext() ) {
            if ( itt.next() == null) {
                return false;
            }
        }

        if (super.xmlConsumer == null) {
            return false;
        }
        return true;
    }

    /** Setup pipeline components.
     */
    protected void setupPipeline(Environment environment)
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
    protected void connectPipeline(Environment environment) throws ProcessingException {
        XMLProducer prev = (XMLProducer) this.generator;
        XMLConsumer next;

        try {
            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                // connect SAXConnector
                SAXConnector connect = (SAXConnector) this.manager.lookup(Roles.SAX_CONNECTOR);
                connect.setup((EntityResolver)environment,environment.getObjectModel(),null,null);
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
            SAXConnector connect = (SAXConnector) this.manager.lookup(Roles.SAX_CONNECTOR);
            this.connectors.add(connect);
            next = (XMLConsumer) connect;
            prev.setConsumer(next);
            prev = (XMLProducer) connect;

            // insert this consumer
            prev.setConsumer(super.xmlConsumer);
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
        super.recycle();
        try {
            // release generator
            if ( this.generator != null ) {
                ((ComponentSelector) this.manager.lookup(Roles.GENERATORS))
                    .release(this.generator);
            }
            this.generator = null;

            // Release transformers
            ComponentSelector transformerSelector;
            transformerSelector = (ComponentSelector)this.manager.lookup(Roles.TRANSFORMERS);
            Iterator itt = this.transformers.iterator();
            while ( itt.hasNext() ) {
                transformerSelector.release((Component)itt.next());
            }
            this.manager.release((Component)transformerSelector);

            this.transformers.clear();
            this.transformerParams.clear();
            this.transformerSources.clear();

            // Release connectors
            Iterator itc = this.connectors.iterator();
            while ( itc.hasNext() ) {
                this.manager.release((Component) itc.next());
            }
            this.connectors.clear();
        } catch ( ComponentManagerException e ) {
            getLogger().warn(
                "Failed to release components from event pipeline.",
                e
            );
        } finally {
            this.generator = null;
            this.transformers.clear();
        }
    }
}
