/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.url;

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @deprecated by the new source resolving of avalon excalibur
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: ContextURLFactory.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public class ContextURLFactory extends AbstractLogEnabled implements URLFactory, Contextualizable {

    /**
     * The context
     */
    protected org.apache.avalon.framework.context.Context context;

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
        URL u = envContext.getResource("/" + location);
        if (u != null)
            return u;
        else {
            getLogger().info(location + " could not be found. (possible context problem)");
            throw new MalformedURLException(location + " could not be found. (possible context problem)");
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        return getURL(base.toExternalForm() + location);
    }

    /**
     * Get the context
     */
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException {
        if (this.context == null) {
            this.context = context;
        }
    }
}
