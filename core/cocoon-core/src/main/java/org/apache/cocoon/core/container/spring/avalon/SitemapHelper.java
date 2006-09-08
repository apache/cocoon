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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.classloader.ClassLoaderConfiguration;
import org.apache.cocoon.classloader.ClassLoaderFactory;
import org.apache.cocoon.core.container.spring.CocoonRequestAttributes;
import org.apache.cocoon.core.container.spring.CocoonWebApplicationContext;
import org.apache.cocoon.core.container.spring.Container;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.Deprecation;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.scope.RequestAttributes;


/**
 * FIXME - This class can only handle the case if the sitemap is named "sitemap.xmap"!!!
 * @version $Id$
 * @since 2.2
 */
public class SitemapHelper {

    public static final ThreadLocal PARENT_CONTEXT = new ThreadLocal();

    private static final String CLASSLOADER_CONFIG_NAME = "classloader";

    private static final String DEFAULT_CONFIG_XCONF  = "config/xconf";

    private static final String DEFAULT_CONFIG_SPRING = "config/spring";

    protected static String createDefinition(String     uriPrefix,
                                             boolean    useDefaultIncludes,
                                             List       includes,
                                             Properties globalVariables,
                                             String     sitemapUri) {
        final StringBuffer buffer = new StringBuffer();
        addHeader(buffer);
        // Settings
        buffer.append("  <cocoon:properties useDefaultIncludes=\"");
        buffer.append(useDefaultIncludes);
        buffer.append("\" sitemapUri=\"");
        buffer.append(sitemapUri);
        buffer.append("\">\n");
        if ( includes != null && includes.size() > 0 ) {
            buffer.append("    <property name=\"directories\">\n      <list>\n");
            final Iterator i = includes.iterator();
            while ( i.hasNext() ) {
                final String current = (String)i.next();
                buffer.append("        <value>");
                buffer.append(current);
                buffer.append("</value>\n");
            }
            buffer.append("      </list>\n    </property>");            
        }
        if ( globalVariables != null && globalVariables.size() > 0 ) {
            buffer.append("    <property name=\"globalSitemapVariables\">\n      <props>\n");
            final Iterator i = globalVariables.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                buffer.append("        <prop key=\"");
                buffer.append(current.getKey().toString());
                buffer.append("\">");
                buffer.append(current.getValue().toString());
                buffer.append("</prop>\n");
            }
            buffer.append("      </props>\n    </property>\n");
        }
        buffer.append("  </cocoon:properties>\n");
        // Avalon
        buffer.append("  <avalon:sitemap location=\"sitemap.xmap\" uriPrefix=\"");
        buffer.append(uriPrefix);
        buffer.append("\"/>\n");
        addFooter(buffer);
        return buffer.toString();
    }

    protected static boolean isUsingDefaultIncludes(Configuration config) {
        return config.getChild("components").getAttributeAsBoolean("use-default-includes", true);
    }

    protected static List getPropertyIncludes(Configuration config)
    throws ConfigurationException {
        List propertyDirs = null;
        final Configuration componentConfig = config.getChild("components", false);
        if ( componentConfig != null ) {
            Configuration[] propertyDirConfigs = componentConfig.getChildren("include-properties");
            if ( propertyDirConfigs.length > 0 ) {
                propertyDirs = new ArrayList();
                for(int i=0; i < propertyDirConfigs.length; i++) {
                    propertyDirs.add(propertyDirConfigs[i].getAttribute("dir"));
                }
            }
        }
        return propertyDirs;        
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
        buffer.append(" http://org.apache.cocoon/avalon http://org.apache.cocoon/avalon.xsd\">\n");
    }

    protected static void addFooter(StringBuffer buffer) {
        buffer.append("</beans>\n");
    }

    /**
     * compatibility with 2.1.x - check for global variables in sitemap
     * TODO - This will be removed in later versions!
     */
    protected static Properties getGlobalSitemapVariables(Configuration tree)
    throws ConfigurationException {
        // compatibility with 2.1.x - check for global variables in sitemap
        // TODO - This will be removed in later versions!
        Properties globalSitemapVariables = null;
        if ( tree.getChild("pipelines").getChild("component-configurations", false) != null ) {
            Deprecation.logger.warn("The 'component-configurations' section in the sitemap is deprecated. Please check for alternatives.");
            globalSitemapVariables = new Properties();
            // now check for global variables - if any other element occurs: throw exception
            Configuration[] children = tree.getChild("pipelines").getChild("component-configurations").getChildren();
            for(int i=0; i<children.length; i++) {
                if ( "global-variables".equals(children[i].getName()) ) {
                    Configuration[] variables = children[i].getChildren();
                    for(int v=0; v<variables.length; v++) {
                        globalSitemapVariables.setProperty(variables[v].getName(), variables[v].getValue());
                    }
               } else {
                    throw new ConfigurationException("Component configurations in the sitemap are not allowed for component: " + children[i].getName());
                }
            }
        }
        return globalSitemapVariables;
    }

    public static Container createContainer(Configuration  config,
                                            ServletContext servletContext,
                                            SourceResolver sitemapResolver,
                                            Request        request)
    throws Exception {
        // let's determine our context url
        Source s = sitemapResolver.resolveURI("a");
        String contextUrl = s.getURI();
        sitemapResolver.release(s);
        contextUrl = contextUrl.substring(0, contextUrl.length() - 1);

        final RequestAttributes attr = new CocoonRequestAttributes(request);
        final Container container = Container.getCurrentContainer(servletContext, attr);
        // for now we require that the parent container is a web application context (FIXME)
        if ( !(container.getBeanFactory() instanceof WebApplicationContext) ) {
            throw new Exception("Parent container is not a web application context: " + container.getBeanFactory());
        }
        final WebApplicationContext parentContext = (WebApplicationContext)container.getBeanFactory();

        // get classloader
        final ClassLoader classloader = createClassLoader(parentContext, config, servletContext, sitemapResolver);
        // create root bean definition
        final boolean useDefaultIncludes = isUsingDefaultIncludes(config);
        final List propIncludes = getPropertyIncludes(config);
        final String definition = createDefinition(request.getSitemapURIPrefix(),
                                                   useDefaultIncludes,
                                                   propIncludes,
                                                   getGlobalSitemapVariables(config),
                                                   "sitemap.xmap");
        PARENT_CONTEXT.set(parentContext);
        try {
            final CocoonWebApplicationContext context = new CocoonWebApplicationContext(classloader,
                                                                                        parentContext,
                                                                                        contextUrl,
                                                                                        definition);
            return new Container(context, context.getClassLoader());
        } finally {
            PARENT_CONTEXT.set(null);
        }
    }

    /**
     * Build a processing tree from a <code>Configuration</code>.
     */
    protected static ClassLoader createClassLoader(BeanFactory    parentFactory,
                                                   Configuration  config,
                                                   ServletContext servletContext,
                                                   SourceResolver sitemapResolver)
    throws Exception {
        final Configuration componentConfig = config.getChild("components", false);
        Configuration classPathConfig = null;

        if ( componentConfig != null ) {
            classPathConfig = componentConfig.getChild(CLASSLOADER_CONFIG_NAME, false);
        }
        // Create class loader
        // we don't create a new class loader if there is no new configuration
        if ( classPathConfig == null ) {
            return Thread.currentThread().getContextClassLoader();            
        }
        final String factoryRole = config.getAttribute("factory-role", ClassLoaderFactory.ROLE);

        // Create a new classloader
        ClassLoaderConfiguration configBean = AvalonUtils.createConfiguration(sitemapResolver, config);
        ClassLoaderFactory clFactory = (ClassLoaderFactory)parentFactory.getBean(factoryRole);
        return clFactory.createClassLoader(Thread.currentThread().getContextClassLoader(),
                                           configBean,
                                           servletContext);
    }

}
