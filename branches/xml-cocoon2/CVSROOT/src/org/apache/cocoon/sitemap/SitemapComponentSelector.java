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
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.cocoon.components.CocoonComponentSelector;

import org.apache.cocoon.components.url.URLFactory;

/** Default component manager for Cocoon's sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: SitemapComponentSelector.java,v 1.1.2.4 2001-03-12 05:18:07 bloritsch Exp $
 */
public class SitemapComponentSelector extends CocoonComponentSelector {
    HashMap mime_types;

    /** The conctructors (same as the Avalon CocoonComponentManager)
     */
    public SitemapComponentSelector () {
        super();
        this.mime_types = new HashMap();
    }

    protected String getMimeTypeForRole(String role) {
        return (String) this.mime_types.get(role);
    }

    protected void addSitemapComponent(Object hint, Class component, Configuration conf, String mime_type)
    throws ComponentManagerException,
           ConfigurationException {
        super.addComponent(hint, component, conf);
        this.mime_types.put(hint, mime_type);
    }
}
