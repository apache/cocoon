/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Own implementation of a {@link XmlWebApplicationContext} which is configured with
 * a base url specifying the root directory for this web application context.
 *
 * @since 1.0
 * @version $Id$
 */
public class ChildXmlWebApplicationContext extends XmlWebApplicationContext {

    /** The base url (already postfixed with a '/'). */
    protected String baseUrl;

    /** The bean definition for this context. */
    protected String beanDefinition;

    public ChildXmlWebApplicationContext() {
        // nothing to do here
    }

    public ChildXmlWebApplicationContext(String url,
                                       String rootDefinition) {
        this.setBaseUrl(url);
        this.setBeanDefinition(rootDefinition);
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String url) {
        if ( url.endsWith("/") ) {
            this.baseUrl = url;
        } else {
            this.baseUrl = url + '/';
        }
    }

    public String getBeanDefinition() {
        return beanDefinition;
    }

    public void setBeanDefinition(String beanDefinition) {
        this.beanDefinition = beanDefinition;
    }

    /**
     * @see org.springframework.web.context.support.XmlWebApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        if ( this.beanDefinition != null ) {
            reader.loadBeanDefinitions(new ByteArrayResource(this.beanDefinition.getBytes("utf-8")));
        }
        super.loadBeanDefinitions(reader);
    }

    /**
     * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext#getResourceByPath(java.lang.String)
     */
    protected Resource getResourceByPath(String path) {
        if ( this.baseUrl != null ) {
            // only if the path does not start with a "/" and is not a url
            // we assume it is relative
            if ( path != null && !path.startsWith("/") && !ResourceUtils.isUrl(path) ) {
                final String absoluteUrl = this.baseUrl + path;
                if (absoluteUrl.startsWith(CLASSPATH_URL_PREFIX)) {
                    return new ClassPathResource(absoluteUrl.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
                }
                try {
                    // try URL
                    URL url = new URL(absoluteUrl);
                    return new UrlResource(url);
                } catch (MalformedURLException ex) {
                    // no URL -> resolve resource path
                    return super.getResourceByPath(absoluteUrl);
                }
            }
        }
        return super.getResourceByPath(path);
    }

    /**
     * A child application context has no default configuration.
     * @see org.springframework.web.context.support.XmlWebApplicationContext#getDefaultConfigLocations()
     */
    protected String[] getDefaultConfigLocations() {
        return new String[0];
    }
}
