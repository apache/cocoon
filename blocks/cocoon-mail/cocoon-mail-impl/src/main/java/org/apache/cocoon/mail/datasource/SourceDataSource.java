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
package org.apache.cocoon.mail.datasource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.excalibur.source.Source;

/**
 * The SourceDataSource class provides an object, that wraps a
 * Cocoon <code>org.apache.excalibur.source.Source</code> object
 * in a DataSource interface.
 *
 * @see org.apache.excalibur.source.Source
 * @see javax.activation.DataSource
 * @version $Id$
 */
public class SourceDataSource extends AbstractDataSource {

    private Source src;

    /**
     * Creates a new instance of SourceDataSource.
     *
     * @param src A <code>org.apache.excalibur.source.Source</code> Object.
     */
    public SourceDataSource(Source src) {
        this(src, null, null);
    }

    /**
     * Creates a new instance of SourceDataSource.
     *
     * @param src A <code>org.apache.excalibur.source.Source</code> Object.
     */
    public SourceDataSource(Source src, String type, String name) {
        super(getName(name, src), getType(type, src));
        this.src = src;
    }

    /**
     * Determines the name for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>name</code> argument.
     * <li>The last path component (after the last '/') from the value
     * returned by the {@link Source#getURI()}.
     * <li>"attachment".
     * </ul>
     *
     * @return the name for this <code>DataSource</code>
     */
    private static String getName(String name, Source src) {
        if (isNullOrEmpty(name)) {
            name = src.getURI();
            name = name.substring(name.lastIndexOf('/') + 1);
            int idx = name.indexOf('?');
            if (idx > -1) {
                name = name.substring(0, idx);
            }

            if (isNullOrEmpty(name)) {
                name = "attachment";
            }
        }

        return name;
    }

    /**
     * Determines the mime type for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>type</code> argument.
     * <li>The value returned by {@link Source#getMimeType()}.
     * <li>"application/octet-stream".
     * </ul>
     *
     * @return The content type (mime type) of this <code>DataSource</code> object.
     */
    private static String getType(String type, Source src) {
        if (isNullOrEmpty(type)) {
            type = src.getMimeType();
            if (isNullOrEmpty(type)) {
                type = "application/octet-stream";
            }
        }

        return type;
    }

    /**
     * Get the <code>InputStream</code> object from the
     * <code>Source</code> object.
     *
     * @throws java.io.IOException if an I/O error occurs.
     * @return The InputStream object from the <code>Source</code> object.
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        try {
            return src.getInputStream();
        } catch (IOException e) {
            // Sun's SMTPTransport looses cause exception. Log it now.
            if (getLogger() != null) {
                getLogger().warn("Unable to obtain input stream for '" + src.getURI() + "'", e);
            }
            throw e;
        }
    }
}
