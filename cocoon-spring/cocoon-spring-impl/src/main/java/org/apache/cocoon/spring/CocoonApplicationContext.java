/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.spring;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * The application context implementation. By default we search the configuration
 * at "conf/applicationContext.xml" in the sitemap directory.
 *
 * @version $Id$
 */
public class CocoonApplicationContext extends XmlWebApplicationContext {

    public static final String DEFAULT_SPRING_CONFIG = "conf/applicationContext.xml";

    protected SourceResolver resolver;
    protected String baseURL;

    public void setSourceResolver(SourceResolver aResolver) {
        this.resolver = aResolver;
    }

    public void setEnvironmentHelper(EnvironmentHelper eh) {
        this.baseURL = eh.getContext();
        if ( !this.baseURL.endsWith("/") ) {
            this.baseURL = this.baseURL + '/';
        }
    }

    /**
     * Resolve file paths beneath the root of the web application.
     * <p>Note: Even if a given path starts with a slash, it will get
     * interpreted as relative to the web application root directory
     * (which is the way most servlet containers handle such paths).
     * @see org.springframework.web.context.support.ServletContextResource
     */
    protected Resource getResourceByPath(String path) {
        if ( path.startsWith("/") ) {
            path = path.substring(1);
        }
        path = this.baseURL + path;
        try {
            return new UrlResource(path);
        } catch (MalformedURLException mue) {
            // FIXME - for now, we simply call super
            return super.getResourceByPath(path);
        }
    }
    
    /**
     * The default location for the context is "conf/applicationContext.xml"
     * which is searched relative to the current sitemap.
     * @return The default config locations if they exist otherwise an empty array.
     */
    protected String[] getDefaultConfigLocations() {
        Source testSource = null;
        try {
            testSource = this.resolver.resolveURI(DEFAULT_SPRING_CONFIG);
            if (testSource.exists()) {
                return new String[] {DEFAULT_SPRING_CONFIG};
            }
        } catch(MalformedURLException e) {
            throw new CascadingRuntimeException("Malformed URL when resolving Spring default config location [ " + DEFAULT_SPRING_CONFIG + "]. This is an unrecoverable programming error. Check the code where this exception was thrown.", e);
        } catch(IOException e) {
            throw new CascadingRuntimeException("Cannot resolve default config location ["+ DEFAULT_SPRING_CONFIG + "] due to an IOException. See cause for details.", e);
        } finally {
            this.resolver.release(testSource);
        }
        
        return new String[]{};
    }
}
