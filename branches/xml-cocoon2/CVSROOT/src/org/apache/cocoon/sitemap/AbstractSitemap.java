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
import org.apache.avalon.DefaultComponentManager;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.ComponentHolderFactory;
import org.apache.cocoon.util.ClassUtils;

import org.xml.sax.SAXException;

/**
 * Base class for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.13 $ $Date: 2000-10-30 18:39:17 $
 */
public abstract class AbstractSitemap implements Sitemap {

    /** The component manager instance */
    protected ComponentManager manager;

    /** The sitemap component manager instance */
    protected DefaultComponentManager sitemapComponentManager;

    /** The sitemap manager instance */
    protected Manager sitemapManager;

    /** The creation date */
    protected static long dateCreated = -1L;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setParentSitemapComponentManager(ComponentManager parentSitemapComponentManager) {
        this.sitemapComponentManager = new DefaultComponentManager (parentSitemapComponentManager);
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Determines whether this generator's source files have changed
     *
     * @return Whether any of the files this sitemap depends on has changed
     * since it was created
     */
    public final boolean modifiedSince(long date) {
        return (dateCreated < date);
    }

    /**
     * Determines whether generated content has changed since
     * last invocation. Users may override this method to take
     * advantage of SAX event cacheing
     *
     * @param request The request whose data must be inspected to assert whether
     * dynamically generated content has changed
     * @return Whether content has changes for this request's data
     *//*
    public boolean hasContentChanged(HttpServletRequest resuest) {
        return true;
    }

     /**
      * Loads a class specified in a sitemap component definition and
      * initialize it
      */
    protected void load_component(String type, String classURL, Configuration configuration, String mime_type)
    throws Exception {
        if (!(ClassUtils.implementsInterface (classURL, Component.class.getName()))) {
            throw new IllegalAccessException ("Object " + classURL + " is not a Component");
        }
        this.sitemapComponentManager.put(
            type, ComponentHolderFactory.getComponentHolder(
                classURL, configuration, this.manager, mime_type
            )
        );
    }

     /**
      * Replaces occurences of xpath like expressions in an argument String
      * with content from a List of Lists
      */
    protected String substitute (List list, String expr)
    throws PatternException, NumberFormatException {
        if (expr == null)
            return null;
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
     * Constructs a resource for the <code>Environment</code> arguments.
     * This method is supplied by the generated Sitemap.
     */
    public abstract boolean process (Environment environment)
    throws Exception;
}
