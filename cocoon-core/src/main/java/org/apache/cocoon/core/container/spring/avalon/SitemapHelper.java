/*
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;


/**
 * @version $Id$
 * @since 2.2
 */
public class SitemapHelper {

    private static final String CLASSLOADER_CONFIG_NAME = "classloader";

    private static final String DEFAULT_CONFIG_XCONF  = "config/xconf";

    private static final String DEFAULT_CONFIG_SPRING = "config/spring";

    public static String createDefinition(String uriPrefix) {
        final StringBuffer buffer = new StringBuffer();
        addHeader(buffer);
        // Settings
        // Avalon
        buffer.append("<avalon:sitemap uriPrefix=\"");
        buffer.append(uriPrefix);
        buffer.append("\"/>");
        addFooter(buffer);
        return buffer.toString();
    }

    public static Configuration createSitemapConfiguration(Configuration config)
    throws ConfigurationException {
        Configuration componentConfig = config.getChild("components", false);
        Configuration classPathConfig = null;

        // by default we include configuration files and properties from
        // predefined locations
        boolean useDefaultIncludes = true;
        if ( componentConfig != null ) {
            useDefaultIncludes = componentConfig.getAttributeAsBoolean("use-default-includes", true);
        }

        List propertyDirs = null;
        if ( componentConfig != null ) {
            Configuration[] propertyDirConfigs = componentConfig.getChildren("include-properties");
            if ( propertyDirConfigs.length > 0 ) {
                propertyDirs = new ArrayList();
                for(int i=0; i < propertyDirConfigs.length; i++) {
                    propertyDirs.add(propertyDirConfigs[i].getAttribute("dir"));
                }
            }
        }

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

                includeElement = new DefaultConfiguration("include-beans", 
                                                          c.getLocation(),
                                                          c.getNamespace(),
                                                          "");
                includeElement.setAttribute("dir", DEFAULT_CONFIG_SPRING);
                includeElement.setAttribute("pattern", "*.xml");
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
        buffer.append(" xmlns:cocoon=\"http://org.apache.cocoon/core\"");
        buffer.append(" xmlns:avalon=\"http://org.apache.cocoon/avalon\"");
        buffer.append(" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd");
        buffer.append(" http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd");
        buffer.append(" http://org.apache.cocoon/core http://org.apache.cocoon/core.xsd");
        buffer.append(" http://org.apache.cocoon/avalon http://org.apache.cocoon/avalon.xsd\">");
    }

    protected static void addFooter(StringBuffer buffer) {
        buffer.append("</beans>");
    }
}
