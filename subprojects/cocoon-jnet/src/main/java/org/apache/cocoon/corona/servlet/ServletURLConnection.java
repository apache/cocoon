/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.corona.servlet;

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

public class ServletURLConnection extends URLConnection {

    private AbsoluteServletConnection connection;

    public ServletURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        if (this.connected) {
            return;
        }

        // get the referenced servlet and find out if it is an absolute or relative connection
        URI locationUri;
        try {
            locationUri = new URI(this.url.getPath());
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
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

        this.connection = new AbsoluteServletConnection(servletName, locationUri.getRawPath(), locationUri.getRawQuery());
        try {
            this.connection.connect();
        } catch (ServletException e) {
            throw new IOException(e.toString());
        }

        this.connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        this.connect();

        try {
            return this.connection.getInputStream();
        } catch (ServletException e) {
            throw new IOException(e.toString());
        }
    }
}
