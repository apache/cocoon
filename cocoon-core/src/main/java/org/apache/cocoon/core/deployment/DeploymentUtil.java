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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Helper class for deploying resources and configuration files from the
 * block artifacts.
 *
 * @version $Id$
 * @since 2.2
 */
public class DeploymentUtil {

    public void deploy()
    throws IOException {
        try {
            // find out all artifacts containing Cocoon specific configuration files
            final Enumeration artifactUrls = this.getClass().getClassLoader().getResources("META-INF/cocoon");
            while ( artifactUrls.hasMoreElements() ) {
                final URL url = (URL)artifactUrls.nextElement();
                String artifactUrl = url.toString();
                int pos = artifactUrl.indexOf('!');
                // url starts with "jar:"
                artifactUrl = artifactUrl.substring(4, pos);
                System.out.println("Trying to open " + artifactUrl);
                final URL archiveUrl = new URL(artifactUrl);
                final InputStream is = archiveUrl.openStream();
                final byte[] b = new byte[2];
                is.read(b);
                System.out.println("..first two bytes: " + new String(b));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
