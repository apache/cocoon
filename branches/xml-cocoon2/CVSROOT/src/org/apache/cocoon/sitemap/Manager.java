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

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.Handler;

import org.xml.sax.SAXException;

/**
 * This class manages all sub <code>Sitemap</code>s of a <code>Sitemap</code>
 * Invokation of sub sitemaps will be done by this instance as well 
 * checking regeneration of the sub <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-09-28 19:14:04 $
 */
public class Manager implements Configurable, Composer {

    /** The vectors of sub sitemaps */
    private HashMap sitemaps = new HashMap();

    /** The configuration */
    private Configuration conf = null;

    /** The component manager */
    private ComponentManager manager = null;

    public void setConfiguration (Configuration conf) {
        this.conf = conf;
    }

    public void setComponentManager (ComponentManager manager) {
        this.manager = manager;
    }

    public boolean invoke (Environment environment, String uri_prefix, 
                           String source, boolean check_reload) 
    throws Exception {
        Handler sitemapHandler = (Handler) sitemaps.get(source);
        
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
            if (sitemapHandler instanceof Composer) sitemapHandler.setComponentManager(this.manager);
            if (sitemapHandler instanceof Configurable) sitemapHandler.setConfiguration(this.conf); 
            sitemaps.put(source, sitemapHandler);
            sitemapHandler.regenerate(environment); 
        }
        
        environment.changeContext(uri_prefix, source);
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
