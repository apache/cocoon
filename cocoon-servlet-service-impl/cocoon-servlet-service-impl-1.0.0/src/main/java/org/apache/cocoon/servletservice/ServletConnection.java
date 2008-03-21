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
package org.apache.cocoon.servletservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;

import javax.servlet.ServletException;

/**
 * <p>
 * Contract to connect to a servlet service. The implementing classes differ from
 * how they look up the servlet service.
 * </p>
 * <p>
 * This class was designed similar to {@link URLConnection}.
 * </p>
 *
 * @version $Id$
 * @since 1.0.0
 */
public interface ServletConnection {


    // ~~~~~~~~~~~~~~~~~~~~~~~~ Pre-connect methods ~~~~~~~~~~~~~~~~~~

    /**
     * Set the last modification date if you want to make use of caching. This
     * method has to be called before any of the other methods of this interface
     * is invoked.
     *
     * @param ifmodifiedsince
     *            The timestamp of the last known resource.
     */
    void setIfModifiedSince(long ifmodifiedsince);

    /**
     * Get an output stream that writes as POST to this connection.
     *
     * @return An output stream that writes as POST to this connection.
     */
    OutputStream getOutputStream();

    // ~~~~~~~~~~~~~~~~~~~~~~~~ connect method ~~~~~~~~~~~~~~~~~~

    /**
     * Connect to the servlet service. Establishing a connection means that the
     * service is executed and the response is available.
     *
     * @throws IOException
     *             The connection to the servlet service can't be established.
     * @throws ServletException
     *             Any other problem when connecting to a servlet service.
     */
    void connect() throws IOException, ServletException;

    // ~~~~~~~~~~~~~~~~~~~~~~~~ Post-connect methods ~~~~~~~~~~~~~~~~~~

    /**
     * Read the input stream from the servlet service response.
     *
     * @return An input stream on the servlet service response.
     * @throws IOException
     *             The connection to the servlet service can't be established.
     * @throws ServletException
     *             Any other problem when connecting to a servlet service.
     */
    InputStream getInputStream() throws IOException, ServletException;

    /**
     *
     * @return The HTTP status code returned by the servlet service.
     * @throws IOException
     */
    int getResponseCode() throws IOException;

    /**
     * Get the last modification date of the servlet service.
     *
     * @return The last modification date of the servlet service.
     */
    long getLastModified();

    /**
     * Get the mime-type of the servlet-service.
     *
     * @return The mime-type of the servlet-service.
     */
    String getContentType();

    // ~~~~~~~~~~~~~~~~~~~~~~~~ Idempotent methods ~~~~~~~~~~~~~~~~~~

    /**
     * Get a URI representing this servlet connection.
     *
     * @return a URI representing this servlet connection.
     */
    URI getURI();


}
