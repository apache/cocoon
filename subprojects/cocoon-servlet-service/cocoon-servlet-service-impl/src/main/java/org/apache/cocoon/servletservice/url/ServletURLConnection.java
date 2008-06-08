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
package org.apache.cocoon.servletservice.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;

import org.apache.cocoon.servletservice.AbsoluteServletConnection;
import org.apache.cocoon.servletservice.Absolutizable;
import org.apache.cocoon.servletservice.CallStackHelper;
import org.apache.cocoon.servletservice.ServletConnection;

public class ServletURLConnection extends URLConnection {

    private final ServletConnection servletConnection;
    private final URL url;

    protected ServletURLConnection(URL url) throws URISyntaxException {
        super(url);

        this.url = url;

        URI locationUri = null;
        String query = url.getQuery();
        if (query != null) {
            locationUri = new URI(url.getPath() + "?" + url.getQuery());
        } else {
            locationUri = new URI(url.getPath());
        }

        final String servletReference = locationUri.getScheme();
        final Absolutizable absolutizable = (Absolutizable) CallStackHelper.getCurrentServletContext();
        final String servletName;

        // find out the type of the reference and create a service name
        if (servletReference == null) {
            // self-reference
            servletName = absolutizable.getServiceName();
        } else if (servletReference.endsWith(AbsoluteServletConnection.ABSOLUTE_SERVLET_SOURCE_POSTFIX)) {
            // absolute reference
            servletName = servletReference.substring(0, servletReference.length() - 1);
        } else {
            // relative reference
            servletName = absolutizable.getServiceName(servletReference);
        }

        this.servletConnection = new AbsoluteServletConnection(servletName, locationUri.getRawPath(), locationUri
                .getRawQuery());
        this.servletConnection.setIfModifiedSince(0);
    }

    public void connect() throws IOException {
        try {
            if (this.connected) {
                return;
            }

            this.servletConnection.connect();

            this.connected = true;
        } catch (ServletException e) {
            IOException ioException = new IOException("Can't connect to servlet URL " + this.url + ".");
            ioException.initCause(e);
            throw ioException;
        }
    }

    public InputStream getInputStream() throws IOException {
        try {
            this.connect();
            return this.servletConnection.getInputStream();
        } catch (ServletException e) {
            IOException ioException = new IOException("Can't read from servlet URL " + this.url + ".");
            ioException.initCause(e);
            throw ioException;
        }
    }

    public long getLastModified() {
        try {
            this.connect();
        } catch (IOException e) {
            return 0;
        }
        return this.servletConnection.getLastModified();
    }

}
