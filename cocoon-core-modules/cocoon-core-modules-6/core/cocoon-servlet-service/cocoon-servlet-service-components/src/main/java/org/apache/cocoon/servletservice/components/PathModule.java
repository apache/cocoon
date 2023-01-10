/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.servletservice.components;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.servletservice.Absolutizable;

/**
 * BlockPathModule returns the absolute path of a block protocol path.
 *
 * @version $Id$
 * @since 1.0.0
 */
public class PathModule implements InputModule {

    private ServletContext servletContext;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public Object getAttribute( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        String absoluteURI = null;
        /* No relative block paths yet
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String baseURI = env.getURIPrefix();
        if (baseURI.length() == 0 || !baseURI.startsWith("/"))
            baseURI = "/" + baseURI;
         */
        try {
            // URI uri = ServletSource.resolveURI(new URI(name), new URI(null, null, baseURI, null));
            URI uri = new URI(name);
            absoluteURI = ((Absolutizable)this.servletContext).absolutizeURI(uri).toString();
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Couldn't absolutize " + name +
                    ". Make sure that the configuration of your servlet-service " +
                    "contains a connection to '" + name + "'.");
        }
        return absoluteURI;
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        throw new UnsupportedOperationException();
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        throw new UnsupportedOperationException();
    }
}
