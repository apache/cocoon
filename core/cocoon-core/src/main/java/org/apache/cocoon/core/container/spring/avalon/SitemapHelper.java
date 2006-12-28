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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.classloader.reloading.Monitor;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.spring.CocoonWebApplicationContext;
import org.apache.cocoon.core.container.spring.Container;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 *
 * @version $Id$
 * @since 2.2
 */
public class SitemapHelper {

    public static final ThreadLocal PARENT_CONTEXT = new ThreadLocal();

    private static final String CLASSLOADER_CONFIG_NAME = "classloader";

    private static final String DEFAULT_CONFIG_XCONF  = "config/avalon";

    protected static String createDefinition(String     uriPrefix,
                                             String     sitemapLocation,
                                             String     runningMode) {
        final StringBuffer buffer = new StringBuffer();
        addHeader(buffer);
        // Settings
        buffer.append("  <cocoon:sitemap location=\"");
        buffer.append(sitemapLocation);
        buffer.append("\" runningMode=\"");
        buffer.append(runningMode);
        buffer.append("\"/>\n");
        // Avalon
        buffer.append("  <avalon:sitemap location=\"");
        buffer.append(sitemapLocation);
        buffer.append("\" uriPrefix=\"");
        buffer.append(uriPrefix);
        buffer.append("\"/>\n");
        addFooter(buffer);
        return buffer.toString();
    }

    protected static boolean isUsingDefaultIncludes(Configuration config) {
        return config.getChild("components").getAttributeAsBoolean("use-default-includes", true);
    }

    public static Configuration createSitemapConfiguration(Configuration config)
    throws ConfigurationException {
        Configuration componentConfig = config.getChild("components", false);
        Configuration classPathConfig = null;

        // by default we include configuration files and properties from
        // predefined locations
        final boolean useDefaultIncludes = isUsingDefaultIncludes(config);

        // if we want to add the default includes and have no component section
        // we have to create one!
        if ( componentConfig == null && useDefaultIncludes ) {
            componentConfig = new DefaultConfiguration("components",
                                                       config.getLocation(),
                                                       config.getNamespace(),
                                                       "");
        }

        if ( componentConfig != null ) {
            // before we pass the configuration we have to strip the
            // additional configuration parts, like classpath as these
            // are not configurations for the component container
            final DefaultConfiguration c = new DefaultConfiguration(componentConfig.getName(), 
                                                                    componentConfig.getLocation(),
                                                                    componentConfig.getNamespace(),
                                                                    "");
            c.addAll(componentConfig);
            classPathConfig = c.getChild(CLASSLOADER_CONFIG_NAME, false);
            if ( classPathConfig != null ) {
                c.removeChild(classPathConfig);
            }
            // and now add default includes
            if ( useDefaultIncludes ) {
                DefaultConfiguration includeElement;
                includeElement = new DefaultConfiguration("include", 
                                                          c.getLocation(),
                                                          c.getNamespace(),
                                                          "");
                includeElement.setAttribute("dir", DEFAULT_CONFIG_XCONF);
                includeElement.setAttribute("pattern", "*.xconf");
                includeElement.setAttribute("optional", "true");
                c.addChild(includeElement);
            }
            componentConfig = c;
        }
        return componentConfig;
    }

    protected static void addHeader(StringBuffer buffer) {
        buffer.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"");
        buffer.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        buffer.append(" xmlns:util=\"http://www.springframework.org/schema/util\"");
        buffer.append(" xmlns:cocoon=\"http://cocoon.apache.org/sitemap\"");
        buffer.append(" xmlns:avalon=\"http://cocoon.apache.org/avalon\"");
        buffer.append(" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd");
        buffer.append(" http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd");
        buffer.append(" http://cocoon.apache.org/sitemap http://cocoon.apache.org/sitemap.xsd");
        buffer.append(" http://cocoon.apache.org/avalon http://cocoon.apache.org/avalon.xsd\">\n");
    }

    protected static void addFooter(StringBuffer buffer) {
        buffer.append("</beans>\n");
    }

    public static WebApplicationContext createContainer(Configuration  config,
                                                        String         sitemapLocation,
                                                        Monitor        fam,
                                                        ServletContext servletContext)
    throws Exception {
        // let's get the root container first
        final WebApplicationContext rootContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        final ProcessInfoProvider infoProvider = (ProcessInfoProvider) rootContext.getBean(ProcessInfoProvider.ROLE);
        final Request request = ObjectModelHelper.getRequest(infoProvider.getObjectModel());
        // let's determine our context url
        int pos = sitemapLocation.lastIndexOf('/');
        if ( sitemapLocation.lastIndexOf(File.separatorChar) > pos ) {
            pos = sitemapLocation.lastIndexOf(File.separatorChar);
        }
        final String contextUrl = sitemapLocation.substring(0, pos + 1);

        final WebApplicationContext parentContext = Container.getCurrentWebApplicationContext();

        // get classloader
//      TODO rcl            
//        final ClassLoader classloader = createClassLoader(parentContext, config, fam, servletContext, sitemapResolver);
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        
        // create root bean definition
        final String definition = createDefinition(request.getSitemapURIPrefix(),
                                                   sitemapLocation.substring(pos+1),
                                                   ((Settings)parentContext.getBean(Settings.ROLE)).getRunningMode());
        PARENT_CONTEXT.set(parentContext);
        try {
            final CocoonWebApplicationContext context = new CocoonWebApplicationContext(classloader,
                                                                                        parentContext,
                                                                                        contextUrl,
                                                                                        definition);
            return context;
        } finally {
            PARENT_CONTEXT.set(null);
        }
    }

// TODO rcl    
//    /**
//     * Build a processing tree from a <code>Configuration</code>.
//     * @param processor 
//     */
//    protected static ClassLoader createClassLoader(BeanFactory    parentFactory,
//                                                   Configuration  config,
//                                                   Monitor fam,
//                                                   ServletContext servletContext,
//                                                   SourceResolver sitemapResolver)
//    throws Exception {
//        final Configuration componentConfig = config.getChild("components", false);
//        Configuration classPathConfig = null;
//
//        if ( componentConfig != null ) {
//            classPathConfig = componentConfig.getChild(CLASSLOADER_CONFIG_NAME, false);
//        }
//        // Create class loader
//        // we don't create a new class loader if there is no new configuration
//        if ( classPathConfig == null ) {
//            return Thread.currentThread().getContextClassLoader();            
//        }
//        final String factoryRole = classPathConfig.getAttribute("factory-role", ClassLoaderFactory.ROLE);
//
//        // Create a new classloader
//        ReloadingClassLoaderConfiguration configBean = AvalonUtils.createConfiguration(sitemapResolver, classPathConfig);
//        configBean.setMonitor(fam);
//        ClassLoaderFactory clFactory = (ClassLoaderFactory)parentFactory.getBean(factoryRole);
//        return clFactory.createClassLoader(Thread.currentThread().getContextClassLoader(),
//                                           configBean,
//                                           servletContext);
//    }

}
