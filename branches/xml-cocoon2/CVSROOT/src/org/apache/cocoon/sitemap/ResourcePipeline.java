/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.util.Vector;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.avalon.Configuration;
import org.apache.avalon.Configurable;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.XMLProducer;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.10 $ $Date: 2000-07-31 17:05:56 $
 */
public class ResourcePipeline implements Composer {
    private Generator generator = null;
    private Parameters generatorParam = null;
    private String generatorSource = null;
    private Reader reader = null;
    private Parameters readerParam = null;
    private String readerSource = null;
    private String readerMimeType = null;
    private Vector transformers = new Vector();
    private Vector transformerParams = new Vector();
    private Vector transformerSources = new Vector();
    private Serializer serializer = null;
    private Parameters serializerParam = null;
    private String serializerSource = null;
    private String serializerMimeType = null;

    /** the component manager */
    private ComponentManager manager = null;

    public ResourcePipeline () {
    }

    public void setComponentManager (ComponentManager manager) {
        this.manager = manager;
    }
    public void setGenerator (Generator generator, String source, 
                              Configuration conf, Parameters param) 
    throws InstantiationException, IllegalAccessException {
        this.generator = (Generator)generator.getClass().newInstance();
        if (this.generator instanceof Composer) 
            ((Composer)this.generator).setComponentManager (manager);
        if (this.generator instanceof Configurable) 
            ((Configurable)this.generator).setConfiguration (conf);
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public void setReader (Reader reader, String source, 
                           Configuration conf, Parameters param) 
    throws InstantiationException, IllegalAccessException {
        this.setReader (reader, source, conf, param, null);
    }

    public void setReader (Reader reader, String source, 
                           Configuration conf, Parameters param, String mimeType) 
    throws InstantiationException, IllegalAccessException {
        this.reader = (Reader)reader.getClass().newInstance();
        if (this.reader instanceof Composer) 
            ((Composer)this.reader).setComponentManager (manager);
        if (this.reader instanceof Configurable) 
            ((Configurable)this.reader).setConfiguration (conf);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
    }

    public void setSerializer (Serializer serializer, String source, 
                               Configuration conf, Parameters param) 
    throws InstantiationException, IllegalAccessException {
        this.setSerializer (serializer, source, conf, param, null);
    }

    public void setSerializer (Serializer serializer, String source, 
                               Configuration conf, Parameters param, String mimeType) 
    throws InstantiationException, IllegalAccessException {
        this.serializer = (Serializer)serializer.getClass().newInstance();
        if (this.serializer instanceof Composer) 
            ((Composer)this.serializer).setComponentManager (manager);
        if (this.serializer instanceof Configurable) 
            ((Configurable)this.serializer).setConfiguration (conf);
        this.serializerSource = source;
        this.serializerParam = param;
    }

    public void addTransformer (Transformer transformer, String source, 
                               Configuration conf, Parameters param) 
    throws InstantiationException, IllegalAccessException {
        Transformer transfmr = (Transformer)transformer.getClass().newInstance();
        if (transfmr instanceof Composer) 
            ((Composer)transfmr).setComponentManager (manager);
        if (transfmr instanceof Configurable) 
            ((Configurable)transfmr).setConfiguration (conf);
        this.transformers.add (transfmr);
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment, OutputStream out)
                            throws ProcessingException, IOException, SAXException {
        if (generator == null) {
            if (reader != null) {
                if (readerMimeType != null)
                    environment.setContentType (readerMimeType);
                reader.setup (environment, readerSource, readerParam);
                reader.setOutputStream (out);
                reader.generate();
            } else {
                throw new ProcessingException ("Generator/Reader not specified");
            }
        } else {
            if (serializer == null) {
                throw new ProcessingException ("Serializer not specified");
            }

            generator.setup (environment, generatorSource, generatorParam);
            Transformer transformer = null;
            XMLProducer producer = generator;
            int i = transformers.size();
            for (int j=0; j < i; j++) {
                transformer = (Transformer) transformers.elementAt (j);
                transformer.setup (environment, (String)transformerSources.elementAt (j),
                               (Parameters)transformerParams.elementAt (j));
                producer.setConsumer (transformer);
                producer = transformer;
            }

            if (serializerMimeType != null)
                    environment.setContentType (readerMimeType); 
            serializer.setup (environment, serializerSource, serializerParam);
            serializer.setOutputStream (out);
            producer.setConsumer (serializer);
            generator.generate();
        }
        return true;
    }
} 
