/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.OutputStream;
import java.util.Hashtable;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapHandler;

/**
 * This class manages all sub <code>Sitemap</code>s of a <code>Sitemap</code>
 * Invokation of sub sitemaps will be done by this instance as well 
 * checking regeneration of the sub <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-20 21:57:17 $
 */
public class SitemapManager {

    /** The vectors of sub sitemaps */
    private Hashtable sitemaps = new Hashtable();

    protected SitemapManager () {
    }

    public boolean invoke (Environment environment, String uri_prefix, 
                           String source, boolean check_reload, OutputStream out) 
    throws Exception{
        SitemapHandler sm = (SitemapHandler) sitemaps.get (source);
        if (sm != null) {
            sm.throwError();
            if (sm.available()) {
                if (check_reload 
                 && sm.hasChanged()
                 && !sm.isRegenerating()) {
                    sm.regenerateAsynchroniously();
                }
                environment.addUriPrefix (uri_prefix);
                return sm.process (environment, out);
            } else {
                sm.regenerate();
            }
            environment.addUriPrefix (uri_prefix);
            return sm.process (environment, out);
        } else {
            sm = new SitemapHandler(source);
            sitemaps.put(source, sm);
            environment.addUriPrefix (uri_prefix);
            return sm.process (environment, out);
        }
    }
}
