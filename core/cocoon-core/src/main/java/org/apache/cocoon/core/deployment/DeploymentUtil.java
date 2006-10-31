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
package org.apache.cocoon.core.deployment;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Helper class for deploying resources and configuration files from the
 * block artifacts.
 *
 * @version $Id$
 * @since 2.2
 */
public class DeploymentUtil {

    protected static final String CONFIGURATION_PATH = "META-INF/cocoon";

    protected void deploy(JarFile jarFile, String prefix, String destination) {
        // FIXME - We should check if a deploy is required
        final Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry)entries.nextElement();
            if ( entry.getName().startsWith(prefix) ) {
                final String relativeFileName = destination + entry.getName().substring(prefix.length());
                System.out.println(".." + entry.getName() + " -> " + relativeFileName);
                // FIXME - copy entry to relativeFileName in webapp directory
            }
        }        
    }

    public void deploy()
    throws IOException {
        // FIXME - First check if we run unexpanded!
        // find out all artifacts containing Cocoon specific configuration files
        final Enumeration jarUrls = this.getClass().getClassLoader().getResources(DeploymentUtil.CONFIGURATION_PATH);
        while ( jarUrls.hasMoreElements() ) {
            final URL resourceUrl = (URL)jarUrls.nextElement();
            // we only handle jar files!
            // TODO - Should we throw an exception if it's not a jar file? (or log?)
            if ( "jar".equals(resourceUrl.getProtocol()) ) {
                // if this is a jar url, it has this form "jar:{url-to-jar}!/{resource-path}
                // to open the jar, we can simply remove everything after "!/"
                String url = resourceUrl.toExternalForm();
                int pos = url.indexOf('!');
                url = url.substring(0, pos+2); // +2 as we include "!/"
                final URL jarUrl = new URL(url);
                final JarURLConnection connection = (JarURLConnection)jarUrl.openConnection();
                this.deploy(connection.getJarFile(), DeploymentUtil.CONFIGURATION_PATH, "WEB-INF/cocoon");
            }
        }
    }
}
