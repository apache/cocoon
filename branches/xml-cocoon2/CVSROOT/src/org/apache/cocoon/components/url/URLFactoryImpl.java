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

import org.apache.avalon.component.Component;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.context.Context;
import org.apache.avalon.context.ContextException;
import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.avalon.logger.Loggable;

//import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.ClassUtils;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: URLFactoryImpl.java,v 1.1.2.8 2001-04-24 12:14:42 dims Exp $
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

            String root = null;
            try {
                root = (String)context.get(Constants.CONTEXT_ROOT_PATH);
            } catch (ContextException e){
                getLogger().error("ContextException in getURL",e);
            }

            if (root != null) {
                File file = new File(root, location);
                if(file.exists())
                    return file.toURL();
            }

            getLogger().debug("Making URL a File (assuming that it is full path)", mue);
            return (new File(location)).toURL();
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        if ( base != null ) {
            return getURL(base.toExternalForm() + location);
        } else {
            return getURL(location);
        }
    }

    /**
     * Get the context
     */
    public void contextualize(Context context) throws ContextException {
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
            Configuration[] configs = conf.getChildren("protocol");
            URLFactory urlFactory = null;
            String protocol = null;
            for (int i = 0; i < configs.length; i++) {
                protocol = configs[i].getAttribute("name");
                getLogger().debug("\tfor protocol: " + protocol + " " + configs[i].getAttribute("class"));
                urlFactory = (URLFactory) ClassUtils.newInstance(configs[i].getAttribute("class"));
                if (urlFactory instanceof Contextualizable) {
                    ((Contextualizable) urlFactory).contextualize (this.context);
                }
                if (urlFactory instanceof Loggable) {
                    ((Loggable) urlFactory).setLogger(getLogger());
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
