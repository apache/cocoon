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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.thread.ThreadSafe;
import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.context.Context;
import org.apache.avalon.component.Component;
import org.apache.avalon.component.Composable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.avalon.Disposable;
import org.apache.excalibur.component.DefaultComponentSelector;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.classloader.RepositoryClassLoader;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.ClassUtils;

import org.xml.sax.SAXException;

/**
 * Base class for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.36 $ $Date: 2001-04-23 17:58:21 $
 */
public abstract class AbstractSitemap extends AbstractLoggable implements Sitemap, Disposable, ThreadSafe {
    private Context context;

    private static final int BYTE_ARRAY_SIZE = 1024;

    /** The component manager instance */
    protected ComponentManager manager;

    /** The sitemap manager instance */
    protected Manager sitemapManager;

    /** The URLFactory instance */
    protected URLFactory urlFactory;

    /** The creation date */
    protected static long dateCreated = -1L;

    protected DefaultComponentSelector generators;
    protected DefaultComponentSelector transformers;
    protected SitemapComponentSelector serializers;
    protected SitemapComponentSelector readers;
    protected DefaultComponentSelector actions;
    protected DefaultComponentSelector matchers;
    protected DefaultComponentSelector selectors;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager)  throws ComponentException {
        this.manager = manager;

        try {
            this.urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
            this.generators = (DefaultComponentSelector) this.manager.lookup(Roles.GENERATORS);
            this.transformers = (DefaultComponentSelector) this.manager.lookup(Roles.TRANSFORMERS);
            this.serializers = (SitemapComponentSelector) this.manager.lookup(Roles.SERIALIZERS);
            this.readers = (SitemapComponentSelector) this.manager.lookup(Roles.READERS);
            this.actions = (DefaultComponentSelector) this.manager.lookup(Roles.ACTIONS);
            this.matchers = (DefaultComponentSelector) this.manager.lookup(Roles.MATCHERS);
            this.selectors = (DefaultComponentSelector) this.manager.lookup(Roles.SELECTORS);
        } catch (Exception e) {
            getLogger().error("cannot obtain the Component", e);
            throw new ComponentException ("cannot obtain the URLFactory", e);
        }
    }

    public void contextualize(Context context) {
        this.context = context;
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
    public boolean hasContentChanged(Request request) {
        return true;
    }

     /**
      * Loads a class specified in a sitemap component definition and
      * initialize it
      */
    public void load_component(int type, Object hint, String classURL, Configuration configuration, String mime_type)
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

        switch (type) {
            case Sitemap.GENERATOR:
                this.generators.addComponent(hint, clazz, configuration);
                break;
            case Sitemap.TRANSFORMER:
                this.transformers.addComponent(hint, clazz, configuration);
                break;
            case Sitemap.SERIALIZER:
                this.serializers.addSitemapComponent(hint, clazz, configuration, mime_type);
                break;
            case Sitemap.READER:
                this.readers.addSitemapComponent(hint, clazz, configuration, mime_type);
                break;
            case Sitemap.ACTION:
                this.actions.addComponent(hint, clazz, configuration);
                break;
            case Sitemap.MATCHER:
                this.matchers.addComponent(hint, clazz, configuration);
                break;
            case Sitemap.SELECTOR:
                this.selectors.addComponent(hint, clazz, configuration);
                break;
        }
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

    /**
     * Constructs a resource for the <code>Environment</code> arguments.
     * This method is supplied by the generated Sitemap.
     */
    public abstract boolean process (Environment environment, StreamPipeline pipeline, EventPipeline eventPipeline)
    throws Exception;

    /**
     * dispose
     */
    public void dispose() {
        if (this.urlFactory!=null)  manager.release((Component)this.urlFactory);
        if (this.generators!=null)  manager.release((Component)this.generators);
        if (this.transformers!=null)  manager.release((Component)this.transformers);
        if (this.serializers!=null)  manager.release((Component)this.serializers);
        if (this.readers!=null)  manager.release((Component)this.readers);
        if (this.actions!=null)  manager.release((Component)this.actions);
        if (this.matchers!=null)  manager.release((Component)this.matchers);
        if (this.selectors!=null)  manager.release((Component)this.selectors);
    }
}
