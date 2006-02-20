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
package org.apache.cocoon.core.container.spring;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * This component reads in Avalon style configuration files and returns all
 * contained components and their configurations.
 *
 * FIXME - Add logging statements
 *
 * @since 2.2
 * @version $Id$
 */
public class ConfigReader {

    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    protected SourceResolver resolver;

    /** The configuration info. */
    protected ConfigurationInfo configInfo;

    /** Avalon environment. */
    protected AvalonEnvironment environment;

    public static ConfigurationInfo readConfiguration(String source, AvalonEnvironment env)
    throws Exception {
        final SimpleSourceResolver sourceResolver = new SimpleSourceResolver();
        sourceResolver.enableLogging(env.logger);
        sourceResolver.contextualize(env.context);

        ConfigReader converter = new ConfigReader();
        converter.resolver = sourceResolver;
        converter.environment = env;
        converter.configInfo = new ConfigurationInfo();
        
        converter.convert(source);
        final Iterator i = converter.configInfo.getClassNames().values().iterator();
        while ( i.hasNext() ) {
            final ComponentInfo current = (ComponentInfo)i.next();
            converter.configInfo.addComponent(current);
        }
        converter.configInfo.clearClassNames();

        return converter.configInfo;
    }

    public static ConfigurationInfo readConfiguration(Configuration config,
                                                      ConfigurationInfo parentInfo,
                                                      AvalonEnvironment env)
    throws Exception {
        final SimpleSourceResolver sourceResolver = new SimpleSourceResolver();
        sourceResolver.enableLogging(env.logger);
        sourceResolver.contextualize(env.context);

        ConfigReader converter = new ConfigReader();
        converter.resolver = sourceResolver;
        converter.environment = env;
        converter.configInfo = new ConfigurationInfo(parentInfo);
        // now add selectors from parent
        if ( parentInfo != null ) {
            final Iterator i = parentInfo.getComponents().values().iterator();
            while ( i.hasNext() ) {
                final ComponentInfo current = (ComponentInfo)i.next();
                if ( current.isSelector() ) {
                    converter.configInfo.getClassNames().put(current.getRole(), current);
                }
            }
        }
        converter.convert(config);
        final Iterator i = converter.configInfo.getClassNames().values().iterator();
        while ( i.hasNext() ) {
            final ComponentInfo current = (ComponentInfo)i.next();
            converter.configInfo.addComponent(current);
        }
        converter.configInfo.clearClassNames();

        return converter.configInfo;        
    }

    private ConfigReader() {
        // nothing to do
    }

    protected void convert(String relativePath)
    throws Exception {
        final Source root = this.resolver.resolveURI(relativePath);
        try {
            ConfigurationBuilder b = new ConfigurationBuilder(this.environment.settings);
            Configuration config = b.build(SourceUtil.getInputSource(root));
            
            // It's possible to define a logger on a per sitemap/service manager base.
            // This is the default logger for all components defined with this sitemap/manager.
            this.configInfo.setRootLogger(config.getAttribute("logger", null));

            // and load configuration with a empty list of loaded configurations
            final Set loadedConfigs = new HashSet();
            this.parseConfiguration(config, root.getURI(),loadedConfigs);
            // test for optional user-roles attribute
            final String userRoles = config.getAttribute("user-roles", null);
            if (userRoles != null) {
                final Source userRolesSource = this.resolver.resolveURI(userRoles, root.getURI(), null);
                try {
                    Configuration userRolesConfig = b.build(SourceUtil.getInputSource(userRolesSource));
                    this.parseConfiguration(userRolesConfig, userRolesSource.getURI(), loadedConfigs);
                } finally {
                    this.resolver.release(userRolesSource);
                }
            }
        } finally {
            this.resolver.release(root);
        }
    }

    protected void convert(Configuration config)
    throws Exception {
        // It's possible to define a logger on a per sitemap/service manager base.
        // This is the default logger for all components defined with this sitemap/manager.
        this.configInfo.setRootLogger(config.getAttribute("logger", null));

        // and load configuration with a empty list of loaded configurations
        this.parseConfiguration(config, null, new HashSet());
    }

    protected void parseConfiguration(final Configuration configuration,
                                      String contextURI,
                                      Set loadedURIs) 
    throws ConfigurationException {
        final Configuration[] configurations = configuration.getChildren();

        for( int i = 0; i < configurations.length; i++ ) {
            final Configuration componentConfig = configurations[i];

            final String componentName = componentConfig.getName();

            if ("include".equals(componentName)) {
                this.handleInclude(contextURI, loadedURIs, componentConfig);
            } else if ( "include-beans".equals(componentName) ) {
                this.handleBeanInclude(contextURI, componentConfig);
            } else {
                // Component declaration
                // Find the role
                String role = componentConfig.getAttribute("role", null);
                if (role == null) {
                    // Get the role from the role manager if not explicitely specified
                    role = (String)this.configInfo.getShorthands().get( componentName );
                    if (role == null) {
                        // Unknown role
                        throw new ConfigurationException("Unknown component type '" + componentName +
                            "' at " + componentConfig.getLocation());
                    }
                }

                // Find the className
                String className = componentConfig.getAttribute("class", null);
                ComponentInfo info;
                if (className == null) {
                    // Get the default class name for this role
                    info = (ComponentInfo)this.configInfo.getClassNames().get( role );
                    if (info == null) {
                        throw new ConfigurationException("Cannot find a class for role " + role + " at " + componentConfig.getLocation());
                    }
                    this.configInfo.getClassNames().remove(info);
                    className = info.getComponentClassName();
                } else {                    
                    info = new ComponentInfo();
                }
                // If it has a "name" attribute, add it to the role (similar to the
                // declaration within a service selector)
                // Note: this has to be done *after* finding the className above as we change the role
                String name = componentConfig.getAttribute("name", null);
                if (name != null) {
                    role = role + "/" + name;
                }
                info.fill(componentConfig);
                info.setComponentClassName(className);
                info.setRole(role);
                info.setConfiguration(componentConfig);

                this.configInfo.addComponent(info);
                // now if this is a selector, then we have to register the single components
                if ( info.getConfiguration() != null && className.endsWith("Selector") ) {
                    String classAttribute = null;
                    if ( className.equals("org.apache.cocoon.core.container.DefaultServiceSelector") ) {
                        classAttribute = "class";
                    } else if (className.equals("org.apache.cocoon.components.treeprocessor.sitemap.ComponentsSelector") ) {
                        classAttribute = "src";
                    } 
                    if ( classAttribute == null ) {
                        // TODO: Unknown selector
                        try {
                            System.out.println("** Found unknown selector: " + className);
                            DefaultConfigurationSerializer dcs = new DefaultConfigurationSerializer();
                            System.out.println(dcs.serialize(info.getConfiguration()));
                            System.out.println("----------------------------------------------------");
                        } catch (Exception ignore) {
                        }
                    } else {
                        String componentRole = role;
                        if ( componentRole.endsWith("Selector") ) {
                            componentRole = componentRole.substring(0, componentRole.length() - 8);
                        }
                        componentRole += '/';
                        Configuration[] children = info.getConfiguration().getChildren();
                        for (int j=0; j<children.length; j++) {
                            final Configuration current = children[j];
                            final ComponentInfo childInfo = new ComponentInfo();
                            childInfo.fill(current);
                            childInfo.setConfiguration(current);
                            childInfo.setComponentClassName(current.getAttribute(classAttribute));
                            childInfo.setRole(componentRole + current.getAttribute("name"));
                            this.configInfo.addComponent(childInfo);
                        }
                    }
                }
            }
        }
    }

    protected void handleInclude(final String contextURI,
                                 final Set loadedURIs,
                                 final Configuration includeStatement)
    throws ConfigurationException {
        final String includeURI = includeStatement.getAttribute("src", null);
        String directoryURI = null;
        if ( includeURI == null ) {
            // check for directories
            directoryURI = includeStatement.getAttribute("dir", null);                    
        }
        if ( includeURI == null && directoryURI == null ) {
            throw new ConfigurationException("Include statement must either have a 'src' or 'dir' attribute, at " +
                    includeStatement.getLocation());
        }

        if ( includeURI != null ) {
            Source src = null;
            try {
                src = this.resolver.resolveURI(includeURI, contextURI, null);

                this.loadURI(src, loadedURIs, includeStatement);
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load '" + includeURI + "' at " + includeStatement.getLocation(), e);
            } finally {
                this.resolver.release(src);
            }
            
        } else {
            final String pattern = includeStatement.getAttribute("pattern", null);
            int[] parsedPattern = null;
            if ( pattern != null ) {
                parsedPattern = WildcardHelper.compilePattern(pattern);
            }
            Source directory = null;
            try {
                directory = this.resolver.resolveURI(directoryURI, contextURI, CONTEXT_PARAMETERS);
                if ( directory instanceof TraversableSource ) {
                    final Iterator children = ((TraversableSource)directory).getChildren().iterator();
                    while ( children.hasNext() ) {
                        final Source s = (Source)children.next();
                        try {
                            if ( parsedPattern == null || this.match(s.getURI(), parsedPattern)) {
                                this.loadURI(s, loadedURIs, includeStatement);
                            }
                        } finally {
                            this.resolver.release(s);
                        }
                    }
                } else {
                    throw new ConfigurationException("Include.dir must point to a directory, '" + directory.getURI() + "' is not a directory.'");
                }
            } catch (IOException ioe) {
                throw new ConfigurationException("Unable to read configurations from " + directoryURI);
            } finally {
                this.resolver.release(directory);
            }
        }
    }

    protected void loadURI(final Source src,
                           final Set loadedURIs,
                           final Configuration includeStatement) 
    throws ConfigurationException {
        // If already loaded: do nothing
        final String uri = src.getURI();

        if (!loadedURIs.contains(uri)) {
            // load it and store it in the read set
            Configuration includeConfig = null;
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder(this.environment.settings);
                includeConfig = builder.build(src.getInputStream(), uri);
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load '" + uri + "' at " + includeStatement.getLocation(), e);
            }
            loadedURIs.add(uri);

            // what is it?
            String includeKind = includeConfig.getName();
            if (includeKind.equals("components")) {
                // more components
                this.parseConfiguration(includeConfig, uri, loadedURIs);
            } else if (includeKind.equals("role-list")) {
                // more roles
                this.configureRoles(includeConfig);
            } else {
                throw new ConfigurationException("Unknow document '" + includeKind + "' included at " +
                        includeStatement.getLocation());
            }
        }
    }

    private boolean match(String uri, int[] parsedPattern ) {
        int pos = uri.lastIndexOf('/');
        if ( pos != -1 ) {
            uri = uri.substring(pos+1);
        }
        return WildcardHelper.match(null, uri, parsedPattern);      
    }

    protected void handleBeanInclude(final String contextURI,
                                     final Configuration includeStatement)
    throws ConfigurationException {
        final String includeURI = includeStatement.getAttribute("src", null);
        String directoryURI = null;
        if (includeURI == null) {
            // check for directories
            directoryURI = includeStatement.getAttribute("dir", null);
        }
        if (includeURI == null && directoryURI == null) {
            throw new ConfigurationException(
                    "Include statement must either have a 'src' or 'dir' attribute, at "
                            + includeStatement.getLocation());
        }

        if (includeURI != null) {
            Source src = null;
            try {
                src = this.resolver.resolveURI(includeURI, contextURI, null);

                this.configInfo.addImport(src.getURI());
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load '" + includeURI + "' at "
                        + includeStatement.getLocation(), e);
            } finally {
                this.resolver.release(src);
            }

        } else {
            final String pattern = includeStatement.getAttribute("pattern", null);
            int[] parsedPattern = null;
            if (pattern != null) {
                parsedPattern = WildcardHelper.compilePattern(pattern);
            }
            Source directory = null;
            try {
                directory = this.resolver.resolveURI(directoryURI, contextURI, CONTEXT_PARAMETERS);
                if (directory instanceof TraversableSource) {
                    final Iterator children = ((TraversableSource) directory).getChildren()
                            .iterator();
                    while (children.hasNext()) {
                        final Source s = (Source) children.next();
                        try {
                            if (parsedPattern == null || this.match(s.getURI(), parsedPattern)) {
                                this.configInfo.addImport(s.getURI());
                            }
                        } finally {
                            this.resolver.release(s);
                        }
                    }
                } else {
                    throw new ConfigurationException("Include.dir must point to a directory, '"
                            + directory.getURI() + "' is not a directory.'");
                }
            } catch (IOException ioe) {
                throw new ConfigurationException("Unable to read configurations from "
                        + directoryURI);
            } finally {
                this.resolver.release(directory);
            }
        }
    }

    /**
     * Reads a configuration object and creates the role, shorthand,
     * and class name mapping.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    protected final void configureRoles( final Configuration configuration )
    throws ConfigurationException {
        final Configuration[] roles = configuration.getChildren();

        for( int i = 0; i < roles.length; i++ ) {
            Configuration role = roles[i];
            
            if (!"role".equals(role.getName())) {
                throw new ConfigurationException("Unexpected '" + role.getName() + "' element at " + role.getLocation());
            }
            
            final String roleName = role.getAttribute("name");
            final String shorthand = role.getAttribute("shorthand", null);
            final String defaultClassName = role.getAttribute("default-class", null);

            if (shorthand != null) {
                // Store the shorthand and check that its consistent with any previous one
                Object previous = this.configInfo.getShorthands().put( shorthand, roleName );
                if (previous != null && !previous.equals(roleName)) {
                    throw new ConfigurationException("Shorthand '" + shorthand + "' already used for role " +
                            previous + ": inconsistent declaration at " + role.getLocation());
                }
            }

            if ( defaultClassName != null ) {
                ComponentInfo info = (ComponentInfo)this.configInfo.getClassNames().get(roleName);
                if (info == null) {
                    // Create a new info and store it
                    info = new ComponentInfo();
                    info.setComponentClassName(defaultClassName);
                    info.fill(role);
                    info.setRole(roleName);
                    info.setConfiguration(role);
                    this.configInfo.getClassNames().put(roleName, info);
                } else {
                    // Check that it's consistent with the existing info
                    if (!defaultClassName.equals(info.getComponentClassName())) {
                        throw new ConfigurationException("Invalid redeclaration: default class already set to " + info.getComponentClassName() +
                                " for role " + roleName + " at " + role.getLocation());
                    }
                    //FIXME: should check also other ServiceInfo members
                }
            }

            final Configuration[] keys = role.getChildren( "hint" );
            if( keys.length > 0 ) {
                Map keyMap = (Map)this.configInfo.getKeyClassNames().get(roleName);
                if (keyMap == null) {
                    keyMap = new HashMap();
                    this.configInfo.getKeyClassNames().put(roleName, keyMap);
                }

                for( int j = 0; j < keys.length; j++ ) {
                    Configuration key = keys[j];
                    
                    final String shortHand = key.getAttribute( "shorthand" ).trim();
                    final String className = key.getAttribute( "class" ).trim();

                    ComponentInfo info = (ComponentInfo)keyMap.get(shortHand);
                    if (info == null) {       
                        info = new ComponentInfo();
                        info.setComponentClassName(className);
                        info.fill(key);
                        info.setConfiguration(key);
                        keyMap.put( shortHand, info );
                    } else {
                        // Check that it's consistent with the existing info
                        if (!className.equals(info.getComponentClassName())) {
                            throw new ConfigurationException("Invalid redeclaration: class already set to " + info.getComponentClassName() +
                                    " for hint " + shortHand + " at " + key.getLocation());
                        }
                        //FIXME: should check also other ServiceInfo members
                    }
                }
            }
        }
    }
}
