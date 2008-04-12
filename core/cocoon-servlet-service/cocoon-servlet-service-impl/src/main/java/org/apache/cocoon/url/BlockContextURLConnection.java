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
package org.apache.cocoon.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class BlockContextURLConnection extends URLConnection {

    private URL resolvedPath;
    private final Map blockContexts;

    protected BlockContextURLConnection(URL url, Map blockContexts) {
        super(url);
        this.blockContexts = blockContexts;
        this.url = url;
    }

    public void connect() throws IOException {
        this.getRealPath().openConnection().connect();
    }

    public InputStream getInputStream() throws IOException {
        return getRealPath().openStream();
    }

    private URL getRealPath() {
        if (this.resolvedPath == null) {
            String location = this.url.toExternalForm();

            // Remove the protocol and the first '/'
            int pos = location.indexOf(":/");
            String path = location.substring(pos + 2);

            pos = path.indexOf('/');
            if (pos != -1) {
                // extract the block name and get the block context path
                String blockName = path.substring(0, pos);
                path = path.substring(pos + 1);
                String blockContext = (String) this.blockContexts.get(blockName);

                try {
                    this.resolvedPath = new URL(new URL(blockContext), path);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Can create URL for '" + blockContext + path + "'.'");
                }
            } else {
                throw new RuntimeException("The block name part of a block context uri must end with a '/' in "
                        + location);
            }
        }
        return this.resolvedPath;
    }
}
