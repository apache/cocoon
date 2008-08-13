/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.servletservice.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.servletservice.AbsoluteServletConnection;
import org.apache.cocoon.servletservice.Absolutizable;
import org.apache.cocoon.servletservice.CallStackHelper;
import org.apache.cocoon.servletservice.NoCallingServletServiceRequestAvailableException;
import org.apache.cocoon.servletservice.ServletConnection;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.store.Store;

/**
 * Implementation of a {@link Source} that gets its content by invoking a servlet service.
 *
 * @version $Id$
 * @since 1.0.0
 */
public class ServletSource extends AbstractSource
                           implements PostableSource {

    private transient Log logger = LogFactory.getLog(getClass());

    /**
     * <p>The store is used to store values of Last-Modified header (if it exists).
     * This store is required because in {@link #getValidity()} we need value
     * of Last-Modified header of previous response in order to perform conditional
     * GET.</p>
     */
    private Store store;

    private String location;

    private ServletConnection servletConnection;

    private boolean connected;


    public ServletSource(String location, Store store) throws IOException {
        this.store = store;
        this.location = location;
        this.servletConnection = createServletConnection(location);
        this.setSystemId(this.servletConnection.getURI().toASCIIString());
    }

    public InputStream getInputStream() throws IOException, SourceException {
        try {
            connect();
            
            if (!exists())
                throw new SourceNotFoundException("Location " + this.getURI() + " cannot be found."
                       + "The servlet returned " + servletConnection.getResponseCode() + " response code.");
            
            // FIXME: This is not the most elegant solution
            if (servletConnection.getResponseCode() != HttpServletResponse.SC_OK) {
                //most probably, servlet returned 304 (not modified) and we need to perform second request to get data

                //
                // FIXME This does not work: previous instance of servletConnection
                //       most probably had non empty requestBody. Re-instantiating
                //       it results in new servletConnection with null requestBody
                //       and, as a result, GET request instead of POST.
                //

                servletConnection = createServletConnection(location);
                servletConnection.connect();
            }

            return this.servletConnection.getInputStream();
        } catch (ServletException e) {
            throw new CascadingIOException(e.getMessage(), e);
        }
    }

    /**
     * Factory method that creates either a {@link ServletConnection}.
     *
     * @param The URI as {@link String} pointing to the servlet.
     * @return An {@link ServletConnection} pointing to the referenced servlet.
     * @throws MalformedURLException if there is a problem with the location URI.
     */
    private ServletConnection createServletConnection(String location) throws MalformedURLException {
        URI locationUri = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Trying to create a servlet connection for location " + location);
            }

            locationUri = new URI(location);
            if (!locationUri.isAbsolute()) {
                throw new MalformedURLException("Only absolute URIs are allowed for the block protocol. "
                        + locationUri.toString());
            }

            // get the referenced servlet and find out if it is an absolute or
            // relative connection
            locationUri = new URI(locationUri.getRawSchemeSpecificPart());

            final String servletReference = locationUri.getScheme();
            final Absolutizable absolutizable = (Absolutizable) CallStackHelper.getCurrentServletContext();
            final String servletName;

            // find out the type of the reference and create a service name
            if (servletReference == null) {
                // self-reference
                if (absolutizable == null) {
                    throw new NoCallingServletServiceRequestAvailableException(
                            "A self-reference requires an active servlet request.");
                }

                servletName = absolutizable.getServiceName();
            } else if (servletReference.endsWith(AbsoluteServletConnection.ABSOLUTE_SERVLET_SOURCE_POSTFIX)) {
                // absolute reference
                servletName = servletReference.substring(0, servletReference.length() - 1);
            } else {
                // relative reference
                if (absolutizable == null) {
                    throw new NoCallingServletServiceRequestAvailableException(
                            "A relative servlet call requires an active servlet request.");
                }

                servletName = absolutizable.getServiceName(servletReference);
                
                if (servletName == null)
                    throw new RuntimeException("Could not find connection named '" + servletReference + "'. Did you forgot to declare it in servlet bean configuration?");
            }
            
            return new AbsoluteServletConnection(servletName, locationUri.getRawPath(), locationUri.getRawQuery());
        } catch (URISyntaxException e) {
            MalformedURLException malformedURLException = new MalformedURLException("Invalid URI syntax. "
                    + e.getMessage());
            malformedURLException.initCause(e);
            throw malformedURLException;
        }
    }

    public SourceValidity getValidity() {
        try {
            connect();
            return servletConnection.getLastModified() > 0 ? new ServletValidity(servletConnection.getResponseCode()) : null;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Exception occured while making servlet request", e);
            return null;
        }
    }

    public long getLastModified() {
        try {
            connect();
            return servletConnection.getLastModified() > 0 ? servletConnection.getLastModified() : 0;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Exception occured while making servlet request", e);
            return 0;
        }
    }

    /**
     * The mime-type of the content described by this object. If the source is
     * not able to determine the mime-type by itself this can be null.
     */
    public String getMimeType() {
        try {
            connect();
            return servletConnection.getContentType();
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Exception occured while making servlet request", e);
            return null;
        }
    }

    /**
     * Returns true always.
     *
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        try {
            connect();
            int rc = servletConnection.getResponseCode();
            return rc == HttpServletResponse.SC_OK || rc == HttpServletResponse.SC_FOUND || rc == HttpServletResponse.SC_NOT_MODIFIED;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Exception occured while making servlet request", e);
            return false;
        }
    }

    /**
     * Return an {@link OutputStream} to post to. The returned stream must be closed by
     * the calling code.
     *
     * @return {@link OutputStream} to post to
     */
    public OutputStream getOutputStream() throws IOException {
        return servletConnection.getOutputStream();
    }

    private void connect() throws IOException, ServletException {
        if (connected) {
            return;
        }

        long lastModified = getStoredLastModified();
        if (lastModified > 0) {
            servletConnection.setIfModifiedSince(lastModified);
        }

        servletConnection.connect();
        connected = true;

        // If header is present, Last-Modified value will be stored for further
        // use in conditional gets
        setStoredLastModified(servletConnection.getLastModified());
    }

    /**
     * Returns Last-Modified value from previous response if present. Otherwise 0 is returned.
     *
     * @return Last-Modified value from previous response if present. Otherwise 0 is returned.
     */
    private long getStoredLastModified() {
        Long lastModified = (Long) store.get(calculateInternalKey());
        return lastModified != null ? lastModified.longValue() : 0;
    }

    /**
     * Stores value of Last-Modified header in {@link #store}.
     *
     * @param lastModified
     *            value that will be stored in {@link #store}. Only positive values will be stored.
     * @throws IOException
     *             if exception occured while storing value
     */
    private void setStoredLastModified(long lastModified) throws IOException {
        String key = calculateInternalKey();
        if (lastModified > 0) {
            store.store(key, new Long(lastModified));
        } else {
            store.remove(key);
        }
    }

    /**
     * @return key that will be used to store value of Last-Modified header
     */
    private String calculateInternalKey() {
        return ServletSource.class.getName() + "$" + getURI();
    }


    private static final class ServletValidity implements SourceValidity {

        private int responseCode;

        public ServletValidity(int responseCode) {
            setResponseCode(responseCode);
        }

        public int isValid() {
            return SourceValidity.UNKNOWN;
        }

        public int isValid(SourceValidity newValidity) {
            if (newValidity instanceof ServletValidity) {
                ServletValidity newServletValidity = (ServletValidity) newValidity;

                switch (newServletValidity.getResponseCode()) {
                    case HttpServletResponse.SC_NOT_MODIFIED:
                        return SourceValidity.VALID;

                    case HttpServletResponse.SC_OK:
                        return SourceValidity.INVALID;

                    default:
                        return SourceValidity.UNKNOWN;
                }
            }

            return SourceValidity.UNKNOWN;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }
    }
}
