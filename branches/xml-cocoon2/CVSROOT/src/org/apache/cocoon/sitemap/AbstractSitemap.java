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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.components.classloader.RepositoryClassLoader;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.ComponentHolderFactory;
import org.apache.cocoon.sitemap.SitemapComponentManager;
import org.apache.cocoon.util.ClassUtils;

import org.apache.avalon.AbstractLoggable;
//import org.apache.log.Logger;

import org.xml.sax.SAXException;

/**
 * Base class for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.19 $ $Date: 2001-02-14 11:39:10 $
 */
public abstract class AbstractSitemap extends AbstractLoggable implements Sitemap {

    private static final int BYTE_ARRAY_SIZE = 1024;

    /** The component manager instance */
    protected ComponentManager manager;

    /** The sitemap component manager instance */
    protected SitemapComponentManager sitemapComponentManager;

    /** The sitemap manager instance */
    protected Manager sitemapManager;

    /** The URLFactory instance */
    protected URLFactory urlFactory;

    /** The creation date */
    protected static long dateCreated = -1L;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setParentSitemapComponentManager(ComponentManager parentSitemapComponentManager) {
        this.sitemapComponentManager = new SitemapComponentManager (parentSitemapComponentManager);
        try {
            this.sitemapComponentManager.setURLFactory((URLFactory)manager.lookup(Roles.URL_FACTORY));
        } catch (Exception e) {
            getLogger().warn("cannot obtain URLFactory", e);
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Configure this instance
     */
    public void configure(Configuration conf) throws ConfigurationException {
        try {
            this.urlFactory = (URLFactory)manager.lookup(Roles.URL_FACTORY);
        } catch (Exception e) {
            getLogger().error("cannot obtain the URLFactory", e);
            throw new ConfigurationException ("cannot obtain the URLFactory", e);
        }
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
    public void load_component(String type, String classURL, Configuration configuration, String mime_type)
    throws Exception {
        Class clazz;
        //FIXME(GP): Is it true that a class name containing a colon should be an URL?
        if (classURL.indexOf(':') > 1) {
            URL url = urlFactory.getURL(classURL);
            byte [] b = getByteArrayFromStream(url.openStream());
            clazz = ((RepositoryClassLoader)ClassUtils.getClassLoader()).defineClass(b);
        } else {
            clazz = ClassUtils.loadClass(classURL);
        }
        if (!Component.class.isAssignableFrom(clazz)) {
            throw new IllegalAccessException ("Object " + classURL + " is not a Component");
        }
        this.sitemapComponentManager.put(
            type, ComponentHolderFactory.getComponentHolder(
                getLogger(), clazz, configuration, this.manager, mime_type
            )
        );
    }

    private byte [] getByteArrayFromStream (InputStream stream) {
        List list = new ArrayList();
        byte [] b = new byte[BYTE_ARRAY_SIZE];
        int last = 0;
        try {
            while ((last = stream.read(b)) == BYTE_ARRAY_SIZE) {
                list.add(b);
                b = new byte[BYTE_ARRAY_SIZE];
            }
        } catch (IOException ioe) {
            getLogger().error ("cannot read class byte stream", ioe);
        }
        list.add(b);
        b = new byte [(list.size()-1) * BYTE_ARRAY_SIZE + last];
        int i;
        for (i = 0; i < list.size()-1; i++) {
            System.arraycopy(list.get(i), 0, b, i * BYTE_ARRAY_SIZE, BYTE_ARRAY_SIZE);
        }
        System.arraycopy(list.get(i), 0, b, i * BYTE_ARRAY_SIZE, last);
        return b;
    }

     /**
      * Replaces occurences of xpath like expressions in an argument String
      * with content from a List of Maps
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
                    result.append((String)((Map)list.get(k)).get(s));
                } else {
                    result.append((String)((Map)list.get(k)).get(s.substring(m+1)));
                }
                getLogger().debug("substitute evaluated value for " + (m == -1 ? s : s.substring(m+1))
                       + " as " + (String)((Map)list.get(k)).get(m == -1 ? s : s.substring(m+1)));
            }
            if (ii < expr.length()) {
                result.append(expr.substring(ii));
            }
            return (result.toString());
        } catch (Exception e) {
            getLogger().error("AbstractSitemap:substitute()", e);
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
