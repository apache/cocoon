/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.url;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.avalon.context.ContextException;
import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version $Id: ContextURLFactory.java,v 1.1.2.8 2001-04-25 17:06:53 donaldp Exp $
 */
public class ContextURLFactory extends AbstractLoggable implements URLFactory, Contextualizable {

    /**
     * The context
     */
    protected org.apache.avalon.context.Context context;

    /**
     * Create a URL from a location. This method supports the
     * <i>context://</i> pseudo-protocol for loading
     * resources accessible from the context root path
     *
     * @param location The location
     * @return The URL pointed to by the location
     * @exception MalformedURLException If the location is malformed
     */
    public URL getURL(String location) throws MalformedURLException {
        Context envContext = null;
        try {
            envContext = (Context)this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        } catch (ContextException e){
            getLogger().error("ContextException in getURL",e);
        }
        if (envContext == null) {
            getLogger().warn("no environment-context in application context (making an absolute URL)");
            return new URL(location);
        }
        URL u = envContext.getResource(location);
        if (u != null)
            return u;
        else {
            getLogger().error(location + " could not be found. (possible context problem)");
            throw new RuntimeException(location + " could not be found. (possible context problem)");
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        return getURL(base.toExternalForm() + location);
    }

    /**
     * Get the context
     */
    public void contextualize(org.apache.avalon.context.Context context) throws ContextException {
        if (this.context == null) {
            this.context = context;
        }
    }
}
