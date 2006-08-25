/*
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.cocoon.core;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;

/**
 * This is an utility class.
 * 
 * @version $Id$
 * @since 2.2
 */
public class CoreUtil {

    // Register the location finder for Avalon configuration objects and exceptions
    // and keep a strong reference to it.
    // TODO - we should move the avalon specific part to the spring.avalon package!
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
            
            if (obj instanceof Exception) {
                // Many exceptions in Cocoon have a message like "blah blah at file://foo/bar.xml:12:1"
                String msg = ((Exception)obj).getMessage();
                if (msg == null) return null;
                
                int pos = msg.lastIndexOf(" at ");
                if (pos != -1) {
                    return LocationUtils.parse(msg.substring(pos + 4));
                } else {
                    // Will try other finders
                    return null;
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
     * Get the location of the webapp context as a url.
     * @param servletContext The servlet context 
     * @param knownFile      A known file in the webapp
     */
    public static String getContextUrl(ServletContext servletContext, String knownFile) {
        String servletContextURL;
        String servletContextPath = servletContext.getRealPath("/");
        String path = servletContextPath;

        if (path == null) {
            // Try to figure out the path of the root from that of a known file in the context
            try {
                path = servletContext.getResource(knownFile).toString();
            } catch (MalformedURLException me) {
                throw new CoreInitializationException("Unable to get resource '" + knownFile + "'.", me);
            }
            path = path.substring(0, path.length() - (knownFile.length() - 1));
        }
        try {
            if (path.indexOf(':') > 1) {
                servletContextURL = path;
            } else {
                servletContextURL = new File(path).toURL().toExternalForm();
            }
        } catch (MalformedURLException me) {
            // VG: Novell has absolute file names starting with the
            // volume name which is easily more then one letter.
            // Examples: sys:/apache/cocoon or sys:\apache\cocoon
            try {
                servletContextURL = new File(path).toURL().toExternalForm();
            } catch (MalformedURLException ignored) {
                throw new CoreInitializationException("Unable to determine servlet context URL.", me);
            }
        }
        return servletContextURL;
    }
}