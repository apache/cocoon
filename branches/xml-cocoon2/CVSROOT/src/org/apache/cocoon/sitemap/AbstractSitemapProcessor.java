/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.File; 
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
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-06 04:57:50 $
 */
public abstract class AbstractSitemapProcessor 
        implements SitemapProcessor {
     
    /** The component manager instance */ 
    protected ComponentManager manager=null; 

    /** The creation date */
    protected static long dateCreated = -1L;

    /** The dependency file list */
    protected static File[] dependencies = null;
 
    /** 
     * Set the current <code&gt;ComponentManager</code> instance used by this 
     * <code>Composer</code>. 
     */ 
    public void setComponentManager(ComponentManager manager) { 
        this.manager=manager; 
    } 

  /**
   * Determines whether this generator's source files have changed
   *
   * @return Whether any of the files this generator depends on has changed
   * since it was created
   */
  public final boolean modifiedSince(long date) {
    if (dateCreated < date) {
      return true;
    }

    for (int i = 0; i < dependencies.length; i++) {
      if (dateCreated < dependencies[i].lastModified()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether generated content has changed since
   * last invocation. Users may override this method to take
   * advantage of SAX event cacheing
   *
   * @param request The request whose data must be inspected to assert whether
   * dynamically generated content has changed
   * @return Whether content has changes for this request's data
   */
  public boolean hasContentChanged(Request request) {
    return true;
  }

    /** 
     * Loads a class specified in a sitemap component definition
     */ 
    protected Object load_component (String ClassURL, Configuration conf) {
        return ("");
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
