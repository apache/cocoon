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

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.16 $ $Date: 2000-10-08 21:09:26 $
 */
public class ResourcePipeline implements Composer {
    private ComponentHolder generator = null;
    private Parameters generatorParam = null;
    private String generatorSource = null;
    private ComponentHolder reader = null;
    private Parameters readerParam = null;
    private String readerSource = null;
    private String readerMimeType = null;
    private Vector transformers = new Vector();
    private Vector transformerParams = new Vector();
    private Vector transformerSources = new Vector();
    private ComponentHolder serializer = null;
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

    public void setGenerator (ComponentHolder holder, String source, Parameters param)
    throws Exception {
        if (this.generator != null) {
            throw new ProcessingException ("Generator " + holder.getName() + " already set. You can only select one Generator");
        }
        this.generator = holder;
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public ComponentHolder getGenerator () {
        return this.generator;
    }

    public void setReader (ComponentHolder holder, String source, Parameters param)
    throws Exception {
        this.setReader (holder, source, param, null);
    }

    public void setReader (ComponentHolder holder, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.reader != null) {
            throw new ProcessingException ("Reader " + holder.getName() + " already set. You can only select one Reader");
        }
        this.reader = holder;
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
    }

    public void setSerializer (ComponentHolder holder, String source, Parameters param)
    throws Exception {
        this.setSerializer (holder, source, param, null);
    }

    public void setSerializer (ComponentHolder holder, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.serializer != null) {
            throw new ProcessingException ("Serializer " + holder.getName() + " already set. You can only select one Serializer");
        }
        this.serializer = holder;
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
    }

    public void addTransformer (ComponentHolder holder, String source, Parameters param)
    throws Exception {
        this.transformers.add (holder);
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment)
                            throws Exception {
        String mime_type;

        if (generator == null) {
            if (reader != null) {
                Reader myReader = (Reader) reader.get();
                try {
                    myReader.setup ((EntityResolver) environment, environment.getObjectModel(), readerSource, readerParam);
                    mime_type = myReader.getMimeType();
                    if (mime_type != null) {
                        environment.setContentType (mime_type);
                    } else if (readerMimeType != null) {
                        environment.setContentType (readerMimeType);
                    } else {
                        /* (GP)FIXME: Reaching here we havn't set a mime-type. This
                         * case should be prevented by the sitemap generating stylesheet */
                    }
                    myReader.setOutputStream (environment.getOutputStream());
                    myReader.generate();
                } finally {
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
            int i = transformers.size();
            Transformer myTransformer[] = new Transformer[i];
            for (int j=0; j < i; j++) {
                myTransformer[j] = (Transformer)((ComponentHolder) transformers.elementAt (j)).get();
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
                if (mime_type != null)
                        environment.setContentType (mime_type);
                else if (serializerMimeType != null)
                        environment.setContentType (serializerMimeType);
                mySerializer.setOutputStream (environment.getOutputStream());
                producer.setConsumer (mySerializer);
                myGenerator.generate();
            } finally {
                serializer.put(mySerializer);
                for (int j=0; j < i; j++) {
                    ((ComponentHolder) transformers.elementAt (j)).put(myTransformer[j]);
                }
                generator.put(myGenerator);
            }
        }
        return true;
    }
}
