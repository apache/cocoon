/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.avalon.Contextualizable;
import org.apache.avalon.Context;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Loggable;
import org.apache.avalon.AbstractLoggable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.Handler;
import org.apache.cocoon.sitemap.XSLTFactoryLoader;

import org.xml.sax.SAXException;

/**
 * This class manages all sub <code>Sitemap</code>s of a <code>Sitemap</code>
 * Invokation of sub sitemaps will be done by this instance as well
 * checking regeneration of the sub <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-02-20 12:47:43 $
 */
public class Manager extends AbstractLoggable implements Configurable, Composer, Contextualizable {

    private Context context;

    /** The vectors of sub sitemaps */
    private HashMap sitemaps = new HashMap();

    /** The configuration */
    private Configuration conf;

    /** The component manager */
    private ComponentManager manager;

    public void configure (Configuration conf) {
        this.conf = conf;
    }

    public void contextualize (Context context) {
        this.context = context;
    }

    public void compose (ComponentManager manager) {
        this.manager = manager;
    }

    public boolean invoke (Environment environment, String uri_prefix,
                           String source, boolean check_reload)
    throws Exception {
        // make sure the uri_prefix ends with a slash
        String prefix;
        if (uri_prefix.length() > 0)
            prefix = (uri_prefix.charAt(uri_prefix.length() - 1) == '/' ? uri_prefix : uri_prefix + "/");
        else
            prefix = uri_prefix;
        Handler sitemapHandler = (Handler) sitemaps.get(source);

        /* FIXME: Workaround -- set the logger XSLTFactoryLoader used to generate source
         * within the sitemap generation phase.
         * Needed because we never have the opportunity to handle the lifecycle of the
         * XSLTFactoryLoader, since it is created by the Xalan engine.
         */
        XSLTFactoryLoader.setLogger(getLogger());
        
        if (sitemapHandler != null) {
            if (sitemapHandler.available()) {
                if (check_reload
                 && sitemapHandler.hasChanged()
                 && !sitemapHandler.isRegenerating()) {
                    sitemapHandler.regenerateAsynchronously(environment);
                }
            } else {
                sitemapHandler.regenerate(environment);
            }
        } else {
            sitemapHandler = new Handler(source, check_reload);
            sitemapHandler.contextualize(this.context);
            sitemapHandler.setLogger(getLogger());
            sitemapHandler.compose(this.manager);
            sitemapHandler.configure(this.conf);
            sitemapHandler.regenerate(environment);
            sitemaps.put(source, sitemapHandler);
        }

        environment.changeContext(uri_prefix, source);
        if (! sitemapHandler.available())
            throw new ProcessingException("The sitemap handler's sitemap is not available.");
        return sitemapHandler.process(environment);
    }

    public boolean hasChanged() {
        Handler sitemapHandler = null;
        Iterator iter = sitemaps.values().iterator();
        while (iter.hasNext()) {
            sitemapHandler = (Handler) iter.next();
            if ((sitemapHandler != null) && (sitemapHandler.hasChanged())) {
                return true;
            }
        }
        return false;
    }
}
