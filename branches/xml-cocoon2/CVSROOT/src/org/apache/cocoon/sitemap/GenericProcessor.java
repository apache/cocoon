/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.arch.Component;
import org.apache.arch.ComponentManager;
import org.apache.arch.Composer;
import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.filters.Filter;
import org.apache.cocoon.generators.Generator;
import org.apache.cocoon.serializers.Serializer;
import org.apache.cocoon.sitemap.patterns.PatternException;
import org.apache.cocoon.sitemap.patterns.PatternMatcher;
import org.apache.cocoon.sitemap.patterns.PatternTranslator;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:25:39 $
 */
public class GenericProcessor implements Composer, Configurable, Processor {

    /** The component manager instance */
    private ComponentManager manager=null;
    /** The matcher */
    private PatternMatcher matcher=null;
    /** The source->uri translator */
    private PatternTranslator sourceTranslator=null;
    /** The uri->source translator */
    private PatternTranslator targetTranslator=null;
    /** The generator role */
    private String generator=null;
    /** The filter roles vector */
    private Vector filters=new Vector();
    /** The serializer role */
    private String serializer=null;

    /**
     * Create a new <code>SitemapPartition</code> instance.
     */
    public GenericProcessor() {
        super();
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager=manager;
    }

    /**
     * Pass a <code>Configuration</code> instance to this
     * <code>Configurable</code> class.
     */
    public void setConfiguration(Configuration conf)
    throws ConfigurationException {
        String uri=conf.getAttribute("uri");
        String source=conf.getAttribute("source",null);
        try {
            if(source==null) {
                this.matcher=new PatternMatcher(uri);
            } else {
                this.sourceTranslator=new PatternTranslator(source,uri);
                this.targetTranslator=new PatternTranslator(uri,source);
                this.matcher=this.targetTranslator;
            }
        } catch (PatternException e) {
            throw new ConfigurationException(e.getMessage(),conf);
        }
        // Get the generator
        Configuration c=conf.getConfiguration("generator");
        if (c==null)
            throw new ConfigurationException("Generator not specified",conf);
        this.generator="generator:"+c.getAttribute("name");
        // Get the serializer
        c=conf.getConfiguration("serializer");
        if (c==null)
            throw new ConfigurationException("Serializer not specified",conf);
        this.generator="serializer:"+c.getAttribute("name");
        // Set up the filters vetctor
        Enumeration e=conf.getConfigurations("filter");
        while (e.hasMoreElements()) {
            Configuration f=(Configuration)e.nextElement();
            this.filters.addElement("filter:"+c.getAttribute("name"));
        }
        // NOTE NOTE NOTE NOTE NOTE NOTE
        // STILL MISSING PARAMETERS RETRIEVAL
    }

    /**
     * Process the given <code>Request</code> producing the output to the
     * specified <code>Response</code> and <code>OutputStream</code>.
     */
    public boolean process(Request req, Response res, OutputStream out)
    throws SAXException, IOException, ProcessingException {
        if (!this.matcher.match(req.getUri())) return(false);
        String src=null;
        if (this.targetTranslator!=null) {
            if ((src=this.targetTranslator.translate(req.getUri()))!=null)
                throw new ProcessingException("Error translating \""+
                                              req.getUri()+"\"");
        }
        Serializer s=(Serializer)this.manager.getComponent(this.serializer);
        s.setup(req,res,src,null);
        s.setOutputStream(out);
        XMLConsumer current=s;
        for (int x=0; x<this.filters.size(); x++) {
            String k=(String)this.filters.elementAt(x);
            Filter f=(Filter)this.manager.getComponent(k);
            f.setup(req,res,src,null);
            f.setConsumer(current);
            current=f;
        }
        Generator g=(Generator)this.manager.getComponent(this.generator);
        g.setConsumer(current);
        g.setup(req,res,src,null);
        g.generate();
        return(true);
    }
}
