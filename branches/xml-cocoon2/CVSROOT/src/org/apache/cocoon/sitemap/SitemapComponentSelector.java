/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.util.HashMap;

import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.Component;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.DefaultComponentSelector;

import org.apache.cocoon.components.url.URLFactory;

/** Default component manager for Cocoon's sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: SitemapComponentSelector.java,v 1.1.2.8 2001-04-11 12:41:28 donaldp Exp $
 */
public class SitemapComponentSelector extends DefaultComponentSelector {
    HashMap mime_types;

    /** The conctructors (same as the Avalon ComponentManager)
     */
    public SitemapComponentSelector () {
        super();
        this.mime_types = new HashMap();
    }

    public String getMimeTypeForRole(String role) {
        return (String) this.mime_types.get(role);
    }

    protected void addSitemapComponent(Object hint, Class component, Configuration conf, String mime_type)
    throws ComponentException,
           ConfigurationException {
        super.addComponent(hint, component, conf);
        this.mime_types.put(hint, mime_type);
    }
}
