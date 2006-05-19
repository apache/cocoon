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
package org.apache.cocoon.components.classloader;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Utility class for converting Avalon based Configuration into a {@link ClassLoaderConfiguration}.
 * @version $Id$
 * @since 2.2
 */
public abstract class ClassLoaderUtils {

    public static ClassLoaderConfiguration createConfiguration(Configuration config)
    throws ConfigurationException {
        final ClassLoaderConfiguration configBean = new ClassLoaderConfiguration();
        final Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Configuration child = children[i];
            final String name = child.getName();
            if ("class-dir".equals(name)) {
                configBean.addClassDirectory(child.getAttribute("src"));
            } else if ("lib-dir".equals(name)) {
                configBean.addLibDirectory(child.getAttribute("src"));
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
