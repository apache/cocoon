/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This is a Cocoon specific implementation of a Spring {@link ApplicationContext}.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonXmlWebApplicationContext extends XmlWebApplicationContext {

    public static final String DEFAULT_SPRING_CONFIG = "conf/applicationContext.xml";

    final private Resource avalonResource;
    protected SourceResolver resolver;
    protected String baseURL;


    public CocoonXmlWebApplicationContext(Resource avalonResource,
                                          ApplicationContext parent) {
        this.setParent(parent);
        this.avalonResource = avalonResource;
    }

    /**
     * @see org.springframework.web.context.support.XmlWebApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader)
    throws BeansException, IOException {
        super.loadBeanDefinitions(reader);
        if ( this.avalonResource != null ) {
            reader.loadBeanDefinitions(this.avalonResource);
        }
    }

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
        if ( this.resolver != null ) {
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
        }        
        return new String[]{};
    }

}
