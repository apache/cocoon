/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.url;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.Component;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.AbstractLoggable;
import org.apache.avalon.Loggable;

//import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.ClassUtils;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: URLFactoryImpl.java,v 1.1.2.3 2001-02-14 11:39:45 giacomo Exp $
 */
public class URLFactoryImpl extends AbstractLoggable implements URLFactory, Component, Configurable, Contextualizable {

    /**
     * The context
     */
    protected Context context;

    /**
     * The special URL factories
     */
    protected Map factories;

    /**
     * Create a URL from a location. This method supports specific
     * pseudo-protocol as defined in its configuration
     *
     * @param location The location
     * @return The URL pointed to by the location
     * @exception MalformedURLException If the location is malformed
     */
    public URL getURL(String location) throws MalformedURLException {
        Iterator iter = factories.keySet().iterator();
        String protocol = null;
        while (iter.hasNext()) {
            protocol = (String)iter.next();
            if (location.startsWith(protocol + "://")) {
                return ((URLFactory)factories.get(protocol)).getURL(location.substring(protocol.length() + 3));
            }
        }
        try {
            getLogger().debug("Making URL from " + location);
            return new URL(location);
        } catch (MalformedURLException mue) {
            getLogger().debug("Making URL a File relative to context root", mue);

            String root = (String)context.get(Constants.CONTEXT_ROOT_PATH);
            if (root != null) {
                return (new File(root, location)).toURL();
            }

            return (new File(location)).toURL();
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        return getURL(base.toExternalForm() + location);
    }

    /**
     * Get the context
     */
    public void contextualize(Context context) {
        if (this.context == null) {
            this.context = context;
        }
    }

    /**
     * Configure the URLFactories
     */
    public void configure(final Configuration conf) throws ConfigurationException {
        try {
            getLogger().debug("Getting the URLFactories");
            factories = new HashMap();
            Iterator iter = conf.getChildren("protocol");
            Configuration config = null;
            URLFactory urlFactory = null;
            String protocol = null;
            while (iter.hasNext()) {
                config = (Configuration)iter.next();
                protocol = config.getAttribute("name");
                getLogger().debug("\tfor protocol: " + protocol + " " + config.getAttribute("class"));
                urlFactory = (URLFactory) ClassUtils.newInstance(config.getAttribute("class"));
                if (urlFactory instanceof Loggable) {
                    ((Loggable) urlFactory).setLogger(getLogger());
                }
                if (urlFactory instanceof Contextualizable) {
                    ((Contextualizable) urlFactory).contextualize (this.context);
                }
                factories.put(protocol, urlFactory);
            }
        } catch (Exception e) {
            getLogger().error("Could not get URLFactories", e);
            throw new ConfigurationException("Could not get parameters because: " +
                                           e.getMessage());
        }
    }
}
