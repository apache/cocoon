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
package org.apache.cocoon.core.container.util;

import java.util.Iterator;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.classloader.ClassLoaderConfiguration;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * Utility class for converting Avalon based Configuration into a {@link ClassLoaderConfiguration}.
 * @version $Id$
 * @since 2.2
 */
public class ClassLoaderUtils {

    private ClassLoaderUtils() {
        // avoid instantiation
    }

    /**
     * Read the configuration for a class loader from an Avalon configuration.
     * This method is used by the sitemap to determine the classpath for the sitemap.
     * If a lib-directory is configured this method scans the directory and adds
     * all found jar/zip files to the classpath.
     * @param resolver   The source resolver for the current sitemap.
     * @param config     The configuration for the class loader
     * @return           A class loader configuration object.
     * @throws Exception
     */
    public static ClassLoaderConfiguration createConfiguration(SourceResolver resolver,
                                                               Configuration  config)
    throws Exception {
        final ClassLoaderConfiguration configBean = new ClassLoaderConfiguration();
        final Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Configuration child = children[i];
            final String name = child.getName();
            if ("class-dir".equals(name)) {
                Source src = null;
                try {
                    src = resolver.resolveURI(child.getAttribute("src"));
                    configBean.addClassDirectory(src.getURI());
                } finally {
                    resolver.release(src);
                }
            } else if ("lib-dir".equals(name)) {
                Source src = null;
                try {
                    src = resolver.resolveURI(child.getAttribute("src"));
                    if (!src.exists()) {
                        throw new Exception(src.getURI() + " doesn't exist");
                    } else if (!(src instanceof TraversableSource) || !((TraversableSource)src).isCollection()) {
                        throw new Exception(src.getURI() + " is not a directory");
                    }
                    Iterator iter = ((TraversableSource)src).getChildren().iterator();
                    while (iter.hasNext()) {
                        final Source childSrc = (Source)iter.next();
                        String childURI = childSrc.getURI();
                        resolver.release(childSrc);
                        if (childURI.endsWith(".jar") || childURI.endsWith(".zip")) {
                            configBean.addClassDirectory(childURI);
                        }
                    }
                } finally {
                    resolver.release(src);
                }
            } else if ("include-classes".equals(name)) {
                configBean.addInclude(child.getAttribute("pattern"));
            } else if ("exclude-classes".equals(name)) {
                configBean.addExclude(child.getAttribute("pattern"));
            } else {
                throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
            }
        }
        return configBean;
    }
}
