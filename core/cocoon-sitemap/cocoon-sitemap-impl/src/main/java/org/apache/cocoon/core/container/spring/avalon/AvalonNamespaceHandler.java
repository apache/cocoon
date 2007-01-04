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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.core.container.spring.avalon.BridgeElementParser;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for the cocoon avalon namespace.
 * Currently this namespace defines the following elements
 * (in the namespace "http://cocoon.apache.org/schema/avalon"):
 * "bridge" : This adds all Avalon configured components to the bean factory.
 * "sitemap":
 *
 * @version $Id$
 * @since 2.2
 */
public class AvalonNamespaceHandler extends NamespaceHandlerSupport {

    // Register the location finder for Avalon configuration objects and exceptions
    // and keep a strong reference to it.
    private static final LocationUtils.LocationFinder confLocFinder = new LocationUtils.LocationFinder() {
        public Location getLocation(Object obj, String description) {
            if (obj instanceof Configuration) {
                Configuration config = (Configuration)obj;
                String locString = config.getLocation();
                Location result = LocationUtils.parse(locString);
                if (LocationUtils.isKnown(result)) {
                    // Add description
                    StringBuffer desc = new StringBuffer().append('<');
                    // Unfortunately Configuration.getPrefix() is not public
                    try {
                        if (config.getNamespace().startsWith("http://apache.org/cocoon/sitemap/")) {
                            desc.append("map:");
                        }
                    } catch (ConfigurationException e) {
                        // no namespace: ignore
                    }
                    desc.append(config.getName()).append('>');
                    return new LocationImpl(desc.toString(), result);
                } else {
                    return result;
                }
            }
            // Try next finders.
            return null;
        }
    };
    
    static {
        LocationUtils.addFinder(confLocFinder);
    }
    
    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("bridge", new BridgeElementParser());
        registerBeanDefinitionParser("sitemap", new SitemapElementParser());
    }
}
