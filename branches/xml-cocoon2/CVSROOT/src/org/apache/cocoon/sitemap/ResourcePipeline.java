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
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.generators.Generator;
import org.apache.cocoon.filters.Filter;
import org.apache.cocoon.serializers.Serializer;
import org.apache.cocoon.xml.XMLProducer;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-11 03:10:04 $
 */
public class ResourcePipeline {
    private Generator generator = null;
    private Parameters generatorParam = null;
    private String generatorSource = null;
    private Vector filters = new Vector();
    private Vector filterParams = new Vector();
    private Vector filterSources = new Vector();
    private Serializer serializer = null;
    private Parameters serializerParam = null;
    private String serializerSource = null;

    public ResourcePipeline () {
    }

    public void setGenerator (Generator generator, String source, Parameters param) {
        this.generator = generator;
        this.generatorSource = source;
        this.generatorParam = param;
    }

    public void setSerializer (Serializer serializer, String source, Parameters param) {
        this.serializer = serializer;
        this.serializerSource = source;
        this.serializerParam = param;
    }

    public void addFilter (Filter filter, String source, Parameters param) {
        this.filters.add (filter);
        this.filterSources.add (source);
        this.filterParams.add (param);
    }

    public boolean startPipeline (Request req, Response res, OutputStream out)
                            throws ProcessingException, IOException, SAXException {
        if (generator == null) {
            throw new ProcessingException ("Generator not specified");
        }

        if (serializer == null) {
            throw new ProcessingException ("Serializer not specified");
        }

        generator.setup (req, res, generatorSource, generatorParam);
        Filter filter = null;
        XMLProducer producer = generator;
        int i = filters.size();

        for (int j=0; j < i; j++) {
            filter = (Filter) filters.elementAt (j);
            filter.setup (req, res, (String)filterSources.elementAt (j),
                           (Parameters)filterParams.elementAt (j));
            producer.setConsumer (filter);
            producer = filter;
        }

        serializer.setup (req, res, serializerSource, generatorParam);
        serializer.setOutputStream (out);
        producer.setConsumer (serializer);
        generator.generate();
        return true;
    }
} 