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

//import org.apache.avalon.ConfigurationException;
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
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-07-25 18:48:44 $
 */
public class ResourcePipeline {
    private Generator generator = null;
    private Parameters generatorParam = null;
    private String generatorSource = null;
    private Reader reader = null;
    private Parameters readerParam = null;
    private String readerSource = null;
    private Vector transformers = new Vector();
    private Vector transformerParams = new Vector();
    private Vector transformerSources = new Vector();
    private Serializer serializer = null;
    private Parameters serializerParam = null;
    private String serializerSource = null;
    private boolean isReader = false;

    public ResourcePipeline () {
    }

    public void setGenerator (Generator generator, String source, Parameters param) {
        this.generator = generator;
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public void setReader (Reader reader, String source, Parameters param) {
        this.reader = reader;
        this.readerSource = source;
        this.readerParam = param;
        this.isReader = true;
    }

    public void setSerializer (Serializer serializer, String source, Parameters param) {
        this.serializer = serializer;
        this.serializerSource = source;
        this.serializerParam = param;
    }

    public void addTransformer (Transformer transformer, String source, Parameters param) {
        this.transformers.add (transformer);
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment, OutputStream out)
                            throws ProcessingException, IOException, SAXException {
        if (isReader) {
            reader.setup (environment, serializerSource, generatorParam);
            reader.setOutputStream (out);
            reader.generate();
        } else {
            if (generator == null) {
                throw new ProcessingException ("Generator not specified");
            }

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

            serializer.setup (environment, serializerSource, serializerParam);
            serializer.setOutputStream (out);
            producer.setConsumer (serializer);
            generator.generate();
        }
        return true;
    }
} 
