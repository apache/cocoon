/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.avalon.Configuration;
import org.apache.avalon.Configurable;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentSelector;
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
import org.apache.cocoon.Roles;

import org.apache.cocoon.sitemap.ErrorNotifier;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.24 $ $Date: 2001-02-23 14:01:27 $
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
    private Serializer serializer;
    private Parameters serializerParam;
    private String serializerSource;
    private String serializerMimeType;
    private String sitemapSerializerMimeType;

    /** the component manager */
    private ComponentManager manager;

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

    public boolean process (Environment environment)
                            throws Exception {
        String mime_type;

        if (this.generator == null) {
            if (this.reader != null) {
                try {
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
                } catch (Exception e) {
                    throw new ProcessingException("There was an error with the reader", e);
                } finally {
                    ((ComponentSelector) this.manager.lookup(Roles.READERS)).release((Component) reader);
                }
            } else {
                throw new ProcessingException ("Generator or Reader not specified");
            }
        } else {
            Transformer myTransformer[] = (Transformer []) transformers.toArray(new Transformer[] {});

            try {
                if (this.serializer == null) {
                    if (this.generator != null) {
                        ((ComponentSelector) this.manager.lookup(Roles.GENERATORS)).release((Component) generator);
                    }

                    if (this.transformers.isEmpty() == false) {
                        for (int i = 0; i < myTransformer.length; i++) {
                            ((ComponentSelector) this.manager.lookup(Roles.TRANSFORMERS)).release((Component) myTransformer[i]);
                        }
                    }
                    throw new ProcessingException ("Serializer not specified");
                }

                if (generatorException != null) {
                    ((ErrorNotifier)this.generator).setException (generatorException);
                }

                this.generator.setup ((EntityResolver) environment, environment.getObjectModel(), generatorSource, generatorParam);
                Transformer transformer = null;
                XMLProducer producer = this.generator;
                for (int i = 0; i < myTransformer.length; i++) {
                    myTransformer[i].setup ((EntityResolver) environment, environment.getObjectModel(),
                            (String)transformerSources.get (i),
                            (Parameters)transformerParams.get (i));
                    producer.setConsumer (myTransformer[i]);
                    producer = myTransformer[i];
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
            } catch (Exception e) {
                throw new ProcessingException("Error generating the resource", e);
            } finally {
                ((ComponentSelector) this.manager.lookup(Roles.GENERATORS)).release((Component) generator);

                for (int i = 0; i < myTransformer.length; i++) {
                    ((ComponentSelector) this.manager.lookup(Roles.TRANSFORMERS)).release((Component) myTransformer[i]);
                }

                ((ComponentSelector) this.manager.lookup(Roles.SERIALIZERS)).release((Component) serializer);
            }
        }
        return true;
    }
}
