/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.DefaultComponentManager;

import org.apache.cocoon.components.url.URLFactory;

/** Default component manager for Cocoon's sitemap components.
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: SitemapComponentManager.java,v 1.1.2.1 2001-02-14 11:39:08 giacomo Exp $
 */
public class SitemapComponentManager extends DefaultComponentManager {

    /** The URLFactory
     */
    private URLFactory urlFactory;

    /** The conctructors (same as the Avalon DefaultComponentManager)
     */
    public SitemapComponentManager () {
        super();
    }

    public SitemapComponentManager (ComponentManager parent) {
        super(parent);
    }

    public void setURLFactory(URLFactory urlFactory) {
        if (this.urlFactory == null) {
            this.urlFactory = urlFactory;
        }
    }
}
