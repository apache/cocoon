/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.Settings;

/**
 * Some utility methods for handling Avalon stuff.
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonUtils {

    /** Bean name for the Avalon context. */
    public static final String CONTEXT_ROLE = "org.apache.avalon.framework.context.Context";

    /** Bean name for the service manager. */
    public static final String SERVICE_MANAGER_ROLE = "org.apache.avalon.framework.service.ServiceManager";


    /**
     * Replace all properties in the configuration object.
     * @param tree     The configuration.
     * @param settings The settings object to resolve the properties.
     */
    public static Configuration replaceProperties(Configuration tree, Settings settings)
    throws ConfigurationException {
        if (tree == null || settings == null) {
            return tree;
        }

        // first clone the tree
        final DefaultConfiguration root = new DefaultConfiguration(tree, true);
        // now replace properties
        _replaceProperties(root, settings);
        return root;
    }

    /**
     * Recursivly replace the properties of a configuration object.
     * @param config   The configuration.
     * @param settings The settings object to resolve the properties.
     * @throws ConfigurationException
     */
    protected static void _replaceProperties(DefaultConfiguration config, Settings settings)
    throws ConfigurationException {
        final String[] names = config.getAttributeNames();
        for (int i = 0; i < names.length; i++) {
            final String value = config.getAttribute(names[i]);
            config.setAttribute(names[i], PropertyHelper.replace(value, settings));
        }

        final String value = config.getValue(null);
        if (value != null) {
            config.setValue(PropertyHelper.replace(value, settings));
        }

        final Configuration[] children = config.getChildren();
        for (int m = 0; m < children.length; m++) {
            _replaceProperties((DefaultConfiguration) children[m], settings);
        }
    }

//  TODO rcl    
//    /**
//     * Read the configuration for a class loader from an Avalon configuration.
//     * This method is used by the sitemap to determine the classpath for the sitemap.
//     * If a lib-directory is configured this method scans the directory and adds
//     * all found jar/zip files to the classpath.
//     * @param resolver   The source resolver for the current sitemap.
//     * @param config     The configuration for the class loader
//     * @return           A class loader configuration object.
//     * @throws Exception
//     */

//    public static ReloadingClassLoaderConfiguration createConfiguration(SourceResolver resolver,
//                                                                        Configuration  config)
//    throws Exception {
//        final ReloadingClassLoaderConfiguration configBean = new ReloadingClassLoaderConfiguration();
//        final Configuration[] children = config.getChildren();
//        for (int i = 0; i < children.length; i++) {
//            final Configuration child = children[i];
//            final String name = child.getName();
//            if ("class-dir".equals(name)) {
//                Source src = null;
//                try {
//                    src = resolver.resolveURI(child.getAttribute("src"));
//                    if (!src.exists()) {
//                        throw new ConfigurationException(src.getURI() + " doesn't exist");
//                    }
//                    configBean.addClassDirectory(src.getURI());
//                    configureStore(configBean,src.getURI(),child.getChild("store",false));
//                } finally {
//                    resolver.release(src);
//                }
//            } else if ("lib-dir".equals(name)) {
//                Source src = null;
//                try {
//                    src = resolver.resolveURI(child.getAttribute("src"));
//                    if (!src.exists()) {
//                        throw new Exception(src.getURI() + " doesn't exist");
//                    } else if (!(src instanceof TraversableSource) || !((TraversableSource)src).isCollection()) {
//                        throw new Exception(src.getURI() + " is not a directory");
//                    }
//                    Iterator iter = ((TraversableSource)src).getChildren().iterator();
//                    while (iter.hasNext()) {
//                        final Source childSrc = (Source)iter.next();
//                        String childURI = childSrc.getURI();
//                        resolver.release(childSrc);
//                        if (childURI.endsWith(".jar") || childURI.endsWith(".zip")) {
//                            configBean.addClassDirectory(childURI);
//                        }
//                    }
//                } finally {
//                    resolver.release(src);
//                }
//            } else if ("include-classes".equals(name)) {
//                configBean.addInclude(child.getAttribute("pattern"));
//            } else if ("exclude-classes".equals(name)) {
//                configBean.addExclude(child.getAttribute("pattern"));
//            } else {
//                throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
//            }
//        }
//        return configBean;
//    }
//    
//    /**
//     * If a store node is configured in the class-dir/src-dir configuration, 
//     * let's configure the store; if no store node is configured add a default;
//     * the default one is the JCI MemoryStore
//     */
//    private static void configureStore(ReloadingClassLoaderConfiguration configBean,
//                                       String                            dirUri,
//                                       Configuration                     storeConfig)
//    throws Exception {
//        final String storeClassName = (storeConfig != null 
//            ? storeConfig.getAttribute("class","org.apache.commons.jci.stores.MemoryResourceStore")
//                : "org.apache.commons.jci.stores.MemoryResourceStore");
//        final ResourceStore store = (ResourceStore)Class.forName(storeClassName).newInstance();
//        final URL url = new URL(dirUri);
//        configBean.addStore(url.getFile(),store);
//    }
}
