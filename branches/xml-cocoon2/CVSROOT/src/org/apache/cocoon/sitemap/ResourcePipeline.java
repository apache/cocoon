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
import org.apache.cocoon.xml.XMLProducer;

import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.17 $ $Date: 2000-10-13 04:14:43 $
 */
public class ResourcePipeline implements Composer {
    private ComponentHolder generator;
    private Parameters generatorParam;
    private String generatorSource;
    private Exception generatorException;
    private ComponentHolder reader;
    private Parameters readerParam;
    private String readerSource;
    private String readerMimeType;
    private Vector transformers = new Vector();
    private Vector transformerParams = new Vector();
    private Vector transformerSources = new Vector();
    private ComponentHolder serializer;
    private Parameters serializerParam;
    private String serializerSource;
    private String serializerMimeType;

    /** the component manager */
    private ComponentManager manager;

    /** the sitemap component manager */
    private ComponentManager sitemapComponentManager;

    public ResourcePipeline (ComponentManager sitemapComponentManager) {
        this.sitemapComponentManager = sitemapComponentManager;
    }

    public void setComponentManager (ComponentManager manager) {
        this.manager = manager;
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
        this.generator = (ComponentHolder)sitemapComponentManager.getComponent(role);
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
        this.reader = (ComponentHolder)sitemapComponentManager.getComponent(role);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
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
        this.serializer = (ComponentHolder)sitemapComponentManager.getComponent(role);
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
    }

    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        this.transformers.add ((ComponentHolder)sitemapComponentManager.getComponent(role));
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment)
                            throws Exception {
        String mime_type;

        if (generator == null) {
            if (reader != null) {
                Reader myReader = null;
                try {
                    myReader = (Reader) reader.get();
                    myReader.setup ((EntityResolver) environment, environment.getObjectModel(), readerSource, readerParam);
                    mime_type = myReader.getMimeType();
                    if (mime_type != null) {
                        // we have a mimeType freom the component itself
                        environment.setContentType (mime_type);
                    } else if (readerMimeType != null) {
                        // there was a mimeType specified in the sitemap pipeline
                        environment.setContentType (readerMimeType);
                    } else {
                        // use the mimeType specified in the sitemap component declaration
                        environment.setContentType (reader.getMimeType());
                    }
                    myReader.setOutputStream (environment.getOutputStream());
                    myReader.generate();
                } finally {
                    if (myReader != null)
                        reader.put(myReader);
                }

            } else {
                throw new ProcessingException ("Generator or Reader not specified");
            }
        } else {
            if (serializer == null) {
                throw new ProcessingException ("Serializer not specified");
            }

            Generator myGenerator = (Generator) generator.get();
            try {
                if (generatorException != null) {
                    ((ErrorNotifier)myGenerator).setException (generatorException);
                }
                int i = transformers.size();
                Transformer myTransformer[] = new Transformer[i];
                int num_transformers = 0;

                try {
                    for (num_transformers=0; num_transformers < i; num_transformers++) {
                        myTransformer[num_transformers] = (Transformer)((ComponentHolder) transformers.elementAt (num_transformers)).get();
                    }

                    Serializer mySerializer = (Serializer) serializer.get();
                    try {

                        myGenerator.setup ((EntityResolver) environment, environment.getObjectModel(), generatorSource, generatorParam);
                        Transformer transformer = null;
                        XMLProducer producer = myGenerator;
                        for (int j=0; j < i; j++) {
                            myTransformer[j].setup ((EntityResolver) environment, environment.getObjectModel(),
                                    (String)transformerSources.elementAt (j),
                                    (Parameters)transformerParams.elementAt (j));
                            producer.setConsumer (myTransformer[j]);
                            producer = myTransformer[j];
                        }

                        mime_type = mySerializer.getMimeType();
                        if (mime_type != null) {
                            // we have a mimeType freom the component itself
                            environment.setContentType (mime_type);
                        } else if (serializerMimeType != null) {
                            // there was a mimeType specified in the sitemap pipeline
                            environment.setContentType (serializerMimeType);
                        } else {
                            // use the mimeType specified in the sitemap component declaration
                            environment.setContentType (serializer.getMimeType());
                        }
                        mySerializer.setOutputStream (environment.getOutputStream());
                        producer.setConsumer (mySerializer);
                        myGenerator.generate();
                    } finally {
                        serializer.put(mySerializer);
                    }
                } finally {
                    for (int j=0; j < num_transformers; j++) {
                        ((ComponentHolder) transformers.elementAt (j)).put(myTransformer[j]);
                    }
                }
            } finally {
                if (myGenerator != null)
                    generator.put(myGenerator);
            }
        }
        return true;
    }
}
