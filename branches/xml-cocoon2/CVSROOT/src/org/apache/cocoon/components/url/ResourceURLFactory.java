/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.url;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.Loggable;

import org.apache.log.Logger;

import org.apache.cocoon.util.ClassUtils;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: ResourceURLFactory.java,v 1.1.2.1 2001-02-12 13:33:15 giacomo Exp $
 */
public class ResourceURLFactory implements URLFactory, Loggable, Contextualizable {

    /**
     * The logger
     */
    protected Logger log;

    /**
     * The context
     */
    protected Context context;

    /**
     * Create a URL from a location. This method supports the
     * <i>resource://</i> pseudo-protocol for loading resources
     * accessible to this same class' <code>ClassLoader</code>
     *
     * @param location The location
     * @return The URL pointed to by the location
     * @exception MalformedURLException If the location is malformed
     */
    public URL getURL(String location) throws MalformedURLException {
        URL u = ClassUtils.getResource(location);
        if (u != null)
            return u;
        else {
            log.error(location + " could not be found. (possible classloader problem)");
            throw new RuntimeException(location + " could not be found. (possible classloader problem)");
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        return getURL (base.toExternalForm() + location);
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
     * Get the logger
     */
    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }
}