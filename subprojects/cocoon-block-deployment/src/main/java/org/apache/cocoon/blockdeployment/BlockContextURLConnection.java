/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.blockdeployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class BlockContextURLConnection extends URLConnection {

    private final Map<String, String> blockContexts;

    private URLConnection urlConnection;

    protected BlockContextURLConnection(URL url, Map<String, String> blockContexts) {
        super(url);

        // check if there the BlockContext map is not null.
        // This null check can't be done earlier because in development
        // environments, the cocoon-block-deployment library is available, but
        // you don't necessarily use the blockcontext protocol.
        if (blockContexts == null) {
            throw new BlockContextInitializationException("There are no block contexts available. Make sure that the "
                    + BlockDeploymentServletContextListener.class.getName()
                    + " is configured correctly in the web.xml.");
        }
        this.blockContexts = blockContexts;
        this.url = url;
    }

    @Override
    public void connect() throws IOException {
        this.getConnection().connect();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.getConnection().getInputStream();
    }

    private URLConnection getConnection() {
        if (this.urlConnection == null) {
            URL resolvedPath = null;
            String location = this.url.toExternalForm();

            // Remove the protocol and the first '/'
            int pos = location.indexOf(":/");
            String path = location.substring(pos + 2);

            pos = path.indexOf('/');
            if (pos != -1) {
                // extract the block name and get the block context path
                String blockName = path.substring(0, pos);
                path = path.substring(pos + 1);
                String blockContext = this.blockContexts.get(blockName);

                if (blockContext == null) {
                    throw new RuntimeException("There is no block '" + blockName
                            + "' deployed. The available blocks are " + this.blockContexts + ".");
                }

                try {
                    resolvedPath = new URL(new URL(blockContext), path);
                    this.urlConnection = resolvedPath.openConnection();
                } catch (IOException e) {
                    throw new RuntimeException("Can create URL for '" + blockContext + path + "'.'");
                }
            } else {
                throw new RuntimeException("The block name part of a block context uri must end with a '/' in "
                        + location);
            }
        }
        return this.urlConnection;
    }

    @Override
    public long getLastModified() {
        return this.getConnection().getLastModified();
    }
}
