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
import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.xml.XMLProducer;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2000-09-10 19:57:45 $
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
    throws Exception {
        this.generator = (Generator)ClassUtils.newInstance(generator.getClass().getName());
        this.initComponent (this.generator, conf);
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public Generator getGenerator () {
        return this.generator;
    }

    public void setReader (Reader reader, String source,
                           Configuration conf, Parameters param)
    throws Exception {
        this.setReader (reader, source, conf, param, null);
    }

    public void setReader (Reader reader, String source,
                           Configuration conf, Parameters param, String mimeType)
    throws Exception {
        this.reader = (Reader)ClassUtils.newInstance(reader.getClass().getName());
        this.initComponent (this.reader, conf);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
    }

    public void setSerializer (Serializer serializer, String source,
                               Configuration conf, Parameters param)
    throws Exception {
        this.setSerializer (serializer, source, conf, param, null);
    }

    public void setSerializer (Serializer serializer, String source,
                               Configuration conf, Parameters param, String mimeType)
    throws Exception {
        this.serializer = (Serializer)ClassUtils.newInstance(serializer.getClass().getName());
        this.initComponent (this.serializer, conf);
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
    }

    public void addTransformer (Transformer transformer, String source,
                               Configuration conf, Parameters param)
    throws Exception {
        Transformer transfmr = (Transformer)ClassUtils.newInstance(transformer.getClass().getName());
        this.initComponent (transfmr, conf);
        this.transformers.add (transfmr);
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment)
                            throws ProcessingException, IOException, SAXException {
        String mime_type=null;
        if (generator == null) {
            if (reader != null) {
                reader.setup ((EntityResolver)environment, environment.getObjectModel(), readerSource, readerParam);
                mime_type = this.reader.getMimeType();
                if (mime_type != null) {
                    environment.setContentType (mime_type);
                } else if (readerMimeType != null) {
                    environment.setContentType (readerMimeType);
                } else {
                    /* (GP)FIXME: Reaching here we havn't set a mime-type. This
                     * case should be prevented by the sitemap generating stylesheet */
                }
                reader.setOutputStream (environment.getOutputStream());
                reader.generate();
            } else {
                throw new ProcessingException ("Generator or Reader not specified");
            }
        } else {
            if (serializer == null) {
                throw new ProcessingException ("Serializer not specified");
            }

            generator.setup ((EntityResolver)environment, environment.getObjectModel(), generatorSource, generatorParam);
            Transformer transformer = null;
            XMLProducer producer = generator;
            int i = transformers.size();
            for (int j=0; j < i; j++) {
                transformer = (Transformer) transformers.elementAt (j);
                transformer.setup ((EntityResolver)environment, environment.getObjectModel(),
                               (String)transformerSources.elementAt (j),
                               (Parameters)transformerParams.elementAt (j));
                producer.setConsumer (transformer);
                producer = transformer;
            }

            mime_type = this.serializer.getMimeType();
            if (mime_type != null)
                    environment.setContentType (mime_type);
            else if (serializerMimeType != null)
                    environment.setContentType (serializerMimeType);
            serializer.setOutputStream (environment.getOutputStream());
            producer.setConsumer (serializer);
            generator.generate();
        }
        return true;
    }

    /**
     * Initializes a Component (inversion of control)
     *
     * @param comp <code>Component</code> to initialize
     * @param conf <code>Configuration</code> of the <code>Component</code>
     */
    private void initComponent (Component comp, Configuration conf) {
        if (comp instanceof Composer)
            ((Composer)comp).setComponentManager (manager);
        if (comp instanceof Configurable)
            ((Configurable)comp).setConfiguration (conf);
    }
}
