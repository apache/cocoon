/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.util.HashMap;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManagerException;
import org.apache.cocoon.DefaultComponentManager;

import org.apache.cocoon.components.url.URLFactory;

/** Default component manager for Cocoon's sitemap components.
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: SitemapComponentManager.java,v 1.1.2.2 2001-02-16 22:07:48 bloritsch Exp $
 */
public class SitemapComponentManager extends DefaultComponentManager {
    ComponentManager manager;
    HashMap mime_types;

    /** The conctructors (same as the Avalon DefaultComponentManager)
     */
    public SitemapComponentManager () {
        super();
        this.mime_types = new HashMap();
    }

    public SitemapComponentManager (ComponentManager parent) {
        this();
        this.manager = parent;
    }

    public Component lookup(String role) throws ComponentManagerException {
        try {
            Component comp = super.lookup(role);
            return comp;
        } catch (ComponentManagerException cme) {
            if (this.manager != null) {
                return this.manager.lookup(role);
            }

            throw cme;
        }
    }

    protected String getMimeTypeForRole(String role) {
        return (String) this.mime_types.get(role);
    }

    protected void addSitemapComponent(String type, Class component, Configuration conf, String mime_type)
    throws ComponentManagerException,
           ConfigurationException {
        super.addComponent(type, component, conf);
        this.mime_types.put(type, mime_type);
    }
}
