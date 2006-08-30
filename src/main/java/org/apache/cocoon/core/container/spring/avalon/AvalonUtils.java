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
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.impl.PropertyHelper;

/**
 * Some utility methods for handling Avalon stuff.
 *
 * @version $Id$
 * @since 2.2
 */
public class AvalonUtils {

    /**
     * Replace all properties
     */
    public static Configuration replaceProperties(Configuration tree, Settings settings)
    throws ConfigurationException {
        final DefaultConfiguration root = new DefaultConfiguration(tree, true);
        convert(root, settings);
        return tree;
    }

    protected static void convert(DefaultConfiguration config, Settings settings)
    throws ConfigurationException {
        final String[] names = config.getAttributeNames();
        for(int i=0; i<names.length; i++) {
            final String value = config.getAttribute(names[i]);
            config.setAttribute(names[i], PropertyHelper.replace(value, settings));
        }
        final String value = config.getValue(null);
        if ( value != null ) {
            config.setValue(PropertyHelper.replace(value, settings));
        }
        final Configuration[] children = config.getChildren();
        for(int m=0; m<children.length; m++) {
            convert((DefaultConfiguration)children[m], settings);
        }
    }
}
