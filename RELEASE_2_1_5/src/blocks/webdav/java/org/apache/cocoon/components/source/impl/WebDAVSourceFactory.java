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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;

/**
 * A factory for WebDAV sources
 *
 * @version $Id: WebDAVSourceFactory.java,v 1.10 2004/04/13 17:13:29 stephan Exp $
*/
public class WebDAVSourceFactory extends AbstractLogEnabled 
implements SourceFactory, Configurable, ThreadSafe {

    private String protocol;
    private boolean secure;
    
    /**
     * Read the scheme name.
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.protocol = configuration.getAttribute("name");
        
        // parse parameters
        Parameters parameters = Parameters.fromConfiguration(configuration);
        this.secure = parameters.getParameterAsBoolean("secure", false);
    }
    
    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(String location, Map parameters)
        throws MalformedURLException, IOException, SourceException {
        
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Creating source object for " + location);
        }
        
        int index = location.indexOf(':');
        if (index != -1) {
            location = location.substring(index+3);
        }
        
        HttpURL url;
        if (this.secure) {
            url = new HttpsURL("https://" + location);
        } else {
            url = new HttpURL("http://" + location);
        }
        
        return WebDAVSource.newWebDAVSource(url, this.protocol, getLogger());
    }

    public void release(Source source) {
        // do nothing
    }
}
