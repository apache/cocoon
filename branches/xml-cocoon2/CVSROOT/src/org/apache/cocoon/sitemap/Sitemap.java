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
import java.util.Hashtable;
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
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.5 $ $Date: 2000-02-28 18:43:21 $
 */
public class Sitemap
implements Composer, Configurable, Processor, LinkResolver {
    
    /** The default partition */
    private SitemapPartition partition=null;
    /** The partitions table */
    private Hashtable partitions=new Hashtable();

    /** The component manager instance */
    private ComponentManager manager=null;

    /**
     * Create a new <code>Sitemap</code> instance.
     */
    public Sitemap() {
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
        if (!conf.getName().equals("sitemap"))
            throw new ConfigurationException("Invalid sitemap configuration",
                                             conf);
        // Set components
        Enumeration e=conf.getConfigurations("partition");
        while (e.hasMoreElements()) {
            Configuration co=(Configuration)e.nextElement();
            SitemapPartition p=new SitemapPartition(this);
            p.setComponentManager(this.manager);
            p.setConfiguration(co);
            
            String name=co.getAttribute("name","default");
            if ("default".equals(name)) {
                if (this.partition==null) this.partition=p;
                else throw new ConfigurationException("Dupliacate definition "+
                                               "of the default partition",co);
            } else {
                if (this.partitions.put(name,p)!=null)
                    throw new ConfigurationException("Duplicate definition "+
                                    "of the partition named \""+name+"\"",co);
            }
        }
    }

    /**
     * Process the given <code>Request</code> producing the output to the
     * specified <code>Response</code> and <code>OutputStream</code>.
     */
    public boolean process(Request req, Response res, OutputStream out)
    throws SAXException, IOException, ProcessingException {
        if(this.partition!=null)
            if(this.partition.process(req,res,out)) return(true);

        Enumeration e=partitions.elements();
        while (e.hasMoreElements()) {
            SitemapPartition p=(SitemapPartition)e.nextElement();
            if (p.process(req,res,out)) return(true);
        }
        return(false);
    }

    public String resolve(String source, String part) {
        if (part==null) return(null);
        if ("default".equals(part)) return(this.partition.resolve(source,part));

        SitemapPartition p=(SitemapPartition)this.partitions.get(part);
        if (p!=null) return(p.resolve(source,part));

        return(null);
    }
}
