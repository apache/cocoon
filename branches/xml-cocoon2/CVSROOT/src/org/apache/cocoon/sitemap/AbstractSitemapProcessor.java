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
import java.util.List;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.cocoon.ProcessingException; 
import org.apache.cocoon.Processor; 
import org.apache.cocoon.environment.Environment; 
import org.apache.cocoon.sitemap.patterns.PatternException;

import org.xml.sax.SAXException; 

/**
 * Base class for XSP-generated <code>SitemapProcessor</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-07-20 21:57:12 $
 */
public abstract class AbstractSitemapProcessor
         implements SitemapProcessor {      
    /** The component manager instance */ 
    protected ComponentManager manager=null; 

    /** The Sitemap instances */ 
    protected SitemapManager sitemapManager = new SitemapManager (); 

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
     * @return Whether any of the files this sitemap depends on has changed
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
    public boolean hasContentChanged(Environment environment) {
        return true;
    }

     /** 
      * Loads a class specified in a sitemap component definition and
      * initialize it
      */ 
    protected Component load_component (String classURL, Configuration conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class cl = this.getClass().getClassLoader().loadClass(classURL);
        Object comp = cl.newInstance();
        if (comp instanceof Composer) {
            ((Composer)comp).setComponentManager (this.manager);
        }
        if (comp instanceof Configurable) {
            ((Configurable)comp).setConfiguration (conf);
        }
        return ((SitemapComponent)comp); 
    } 

     /** 
      * Replaces occurences of xpath like expressions ina argument String
      * with content from a List of Lists
      */ 
    protected String substitute (List list, String expr) 
    throws PatternException, NumberFormatException {
        StringBuffer result = new StringBuffer();
        String s = null;
        int j = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        int n = 0;
        int ii = 0;
        int i = -1;
        try {
            while (ii <= expr.length() && (i = expr.indexOf('{', ii)) != -1) {
                result.append(expr.substring(ii, i));
                j = expr.indexOf('}', i);
                if (j < i) 
                    throw new PatternException ("invalid expression in \""+expr+"\"");
                ii = j+1;
                if (j == -1) 
                    throw new PatternException ("invalid expression in URL "+expr);
                k = list.size() - 1;
                s = expr.substring (i+1,j);
                for (l = -1; (l = s.indexOf("../",l+1)) != -1; k--);
                m = s.lastIndexOf('/');
                if (m == -1) {
                    n = Integer.parseInt(s) - 1;
                } else {
                    n = Integer.parseInt(s.substring(m+1)) - 1;
                }
                result.append((String)((List)list.get(k)).get(n));
            }
            if (ii < expr.length()) {
                result.append(expr.substring(ii));
            }
            return (result.toString()); 
        } catch (Exception e) {
            throw new PatternException 
                    ("error occurred during evaluation of expression \""
                     +expr+"\" at position "+(i+1)+"\n"
                     + e.getMessage());        
        }
    }

    /**
     * Resolve a link against a source into the target URI space.
     */
    public String resolve(String source, String part) { 
        if (part==null) return(null); 
        return(null); 
    } 

    /**
     * Constructs a resource to the supplied <code>OutputStream</code>
     * for the <code>Request</code> and <code>Response</code> arguments.
     * This method is supplied by the generated SitemapProcessor .
     */
/*
    public abstract boolean process (Environment environment, OutputStream out);
*/
} 