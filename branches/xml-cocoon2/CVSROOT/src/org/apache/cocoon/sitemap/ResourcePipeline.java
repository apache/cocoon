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
import org.apache.avalon.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.PoolClient;

import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.20 $ $Date: 2001-02-19 15:58:10 $
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
    private Vector transformers = new Vector();
    private Vector transformerParams = new Vector();
    private Vector transformerSources = new Vector();
    private Serializer serializer;
    private Parameters serializerParam;
    private String serializerSource;
    private String serializerMimeType;
    private String sitemapSerializerMimeType;

    /** the component manager */
    private ComponentManager manager;

    /** the sitemap component manager */
    private SitemapComponentManager sitemapComponentManager;

    public ResourcePipeline (SitemapComponentManager sitemapComponentManager) {
        this.sitemapComponentManager = sitemapComponentManager;
    }

    public void compose (ComponentManager manager) {
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
        this.generator = (Generator)sitemapComponentManager.lookup(role);
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
        this.reader = (Reader)sitemapComponentManager.lookup(role);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
        this.sitemapReaderMimeType = this.sitemapComponentManager.getMimeTypeForRole(role);
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
        this.serializer = (Serializer)sitemapComponentManager.lookup(role);
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.sitemapSerializerMimeType = this.sitemapComponentManager.getMimeTypeForRole(role);
    }

    public void addTransformer (String role, String source, Parameters param)
    throws Exception {
        this.transformers.add ((Transformer)sitemapComponentManager.lookup(role));
        this.transformerSources.add (source);
        this.transformerParams.add (param);
    }

    public boolean process (Environment environment)
                            throws Exception {
        String mime_type;

        if (this.generator == null) {
            if (this.reader != null) {
                this.reader.setup ((EntityResolver) environment, environment.getObjectModel(), readerSource, readerParam);
                mime_type = this.reader.getMimeType();
                if (mime_type != null) {
                    // we have a mimeType freom the component itself
                    environment.setContentType (mime_type);
                } else if (readerMimeType != null) {
                    // there was a mimeType specified in the sitemap pipeline
                    environment.setContentType (this.readerMimeType);
                } else {
                    // use the mimeType specified in the sitemap component declaration
                    environment.setContentType (this.sitemapReaderMimeType);
                }
                reader.setOutputStream (environment.getOutputStream());
                reader.generate();

                if (reader instanceof PoolClient) {
                   ((PoolClient)reader).returnToPool();
                }

            } else {
                throw new ProcessingException ("Generator or Reader not specified");
            }
        } else {
            if (this.serializer == null) {
                throw new ProcessingException ("Serializer not specified");
            }

            if (generatorException != null) {
                ((ErrorNotifier)this.generator).setException (generatorException);
            }
            int i = transformers.size();
            Transformer myTransformer[] = new Transformer[i];
            int num_transformers = 0;

            for (num_transformers=0; num_transformers < i; num_transformers++) {
                myTransformer[num_transformers] = (Transformer) transformers.elementAt (num_transformers);
            }

            this.generator.setup ((EntityResolver) environment, environment.getObjectModel(), generatorSource, generatorParam);
            Transformer transformer = null;
            XMLProducer producer = this.generator;
            for (int j=0; j < i; j++) {
                myTransformer[j].setup ((EntityResolver) environment, environment.getObjectModel(),
                        (String)transformerSources.elementAt (j),
                        (Parameters)transformerParams.elementAt (j));
                producer.setConsumer (myTransformer[j]);
                producer = myTransformer[j];
            }

            mime_type = this.serializer.getMimeType();
            if (mime_type != null) {
                // we have a mimeType freom the component itself
                environment.setContentType (mime_type);
            } else if (serializerMimeType != null) {
                // there was a mimeType specified in the sitemap pipeline
                environment.setContentType (serializerMimeType);
            } else {
                // use the mimeType specified in the sitemap component declaration
                environment.setContentType (this.sitemapSerializerMimeType);
            }
            this.serializer.setOutputStream (environment.getOutputStream());
            producer.setConsumer (this.serializer);
            this.generator.generate();

            if (generator instanceof PoolClient) {
               ((PoolClient)generator).returnToPool();
            }

            for (int j=0; j < i; j++) {
                if (myTransformer[j] instanceof PoolClient) {
                   ((PoolClient)myTransformer[j]).returnToPool();
                }
            }

            if (serializer instanceof PoolClient) {
               ((PoolClient)serializer).returnToPool();
            }
        }
        return true;
    }
}
