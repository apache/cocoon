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

import org.apache.arch.ComponentManager; 
import org.apache.arch.Composer;
import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.cocoon.ProcessingException; 
import org.apache.cocoon.Processor; 
import org.apache.cocoon.Request; 
import org.apache.cocoon.Response; 

import org.xml.sax.SAXException; 


/**
 * Base class for XSP-generated <code>SitemapProcessor</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-02 18:59:24 $
 */
public abstract class AbstractSitemapProcessor 
        implements SitemapProcessor {
     
    /** The component manager instance */ 
    protected ComponentManager manager=null; 
 
    /** 
     * Set the current <code&gt;ComponentManager</code> instance used by this 
     * <code>Composer</code>. 
     */ 
    public void setComponentManager(ComponentManager manager) { 
        this.manager=manager; 
    } 

    /** 
     * Loads a class specified in a sitemap component definition
     */ 
    protected Object load_component (String ClassURL, Configuration conf) {
        return ("");
    }
 
    /** 
     * Process the given <code>Request</code> producing the output to the 
     * specified <code>Response</code> and <code>OutputStream</code>. 
     */ 
    public boolean process(Request req, Response res, OutputStream out)  
    throws SAXException, IOException, ProcessingException { 
      ResourcePipeline pipeline = constructPipeline (req, res); 
      return pipeline.startPipeline(req, res, out); 
    } 

    /**
     * Resolve a link against a source into the target URI space.
     */
    public String resolve(String source, String part) { 
        if (part==null) return(null); 
        return(null); 
    } 

    /**
     * Constructs a <code>ResourcePipeline</code> for the <code>Request</code>.
     * This method is supplied by the generated SitemapProcessor .
     */
    protected abstract ResourcePipeline constructPipeline (Request request, Response resposne);

    /** Following methods are for testing purposes only and should later be deleted */
    protected boolean uri_wildcard_matcher (String pattern) {
       return (true);
    }
    protected boolean uri_regexp_matcher (String pattern) {
       return (true);
    }
    protected boolean browser_matcher (String pattern) {
       return (true);
    }
}
