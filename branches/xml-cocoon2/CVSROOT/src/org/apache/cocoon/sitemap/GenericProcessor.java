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
import org.apache.cocoon.Parameters;
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
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-02-27 17:49:41 $
 */
public class GenericProcessor
implements Composer, Configurable, Processor, LinkResolver {

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
    /** The generator parameters */
    private Parameters generatorParam=null;
    /** The filter roles vector */
    private Vector filters=new Vector();
    /** The filter parameters vector */
    private Vector filtersParam=new Vector();
    /** The serializer role */
    private String serializer=null;
    /** The serializer role */
    private Parameters serializerParam=null;
    /** The sitemap partition */
    private SitemapPartition partition=null;

    /**
     * Create a new <code>SitemapPartition</code> instance.
     */
    public GenericProcessor(SitemapPartition partition) {
        super();
        this.partition=partition;
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
        //if ((uri.length()==0)||(uri.charAt(0)!='/')) uri='/'+uri;
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
        this.generatorParam=Parameters.fromConfiguration(c);
        // Get the serializer
        c=conf.getConfiguration("serializer");
        if (c==null)
            throw new ConfigurationException("Serializer not specified",conf);
        this.serializer="serializer:"+c.getAttribute("name");
        this.serializerParam=Parameters.fromConfiguration(c);
        // Set up the filters vetctor
        Enumeration e=conf.getConfigurations("filter");
        while (e.hasMoreElements()) {
            Configuration f=(Configuration)e.nextElement();
            this.filters.addElement("filter:"+f.getAttribute("name"));
            this.filtersParam.addElement(Parameters.fromConfiguration(f));
        }
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
            if ((src=this.targetTranslator.translate(req.getUri()))==null)
                throw new ProcessingException("Error translating \""+
                                              req.getUri()+"\"");
        }
        Serializer s=(Serializer)this.manager.getComponent(this.serializer);
        s.setup(req,res,src,this.serializerParam);
        s.setOutputStream(out);
        LinkTranslator t=new LinkTranslator(this,req.getUri());
        t.setConsumer(s);
        XMLConsumer current=t;
        for (int x=0; x<this.filters.size(); x++) {
            String k=(String)this.filters.elementAt(x);
            Filter f=(Filter)this.manager.getComponent(k);
            f.setup(req,res,src,(Parameters)this.filtersParam.elementAt(x));
            f.setConsumer(current);
            current=f;
        }

        Generator g=(Generator)this.manager.getComponent(this.generator);
        g.setConsumer(current);
        g.setup(req,res,src,this.generatorParam);
        g.generate();
        return(true);
    }
    
    /**
     * Resolve a link against a source into the target URI space.
     */
    public String resolve(String source, String partition) {
        if ((partition==null)||(this.partition.name.equals(partition))) {
            //if (source!=null) {
                String translated=this.sourceTranslator.translate(source);
                if (translated!=null) return(translated);
            //}
        }
        return(this.partition.resolve(source,partition,this));
    }
}
