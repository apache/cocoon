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

import org.apache.cocoon.servlet.multipart.Part;

/**
 * The FilePartDataSource class provides an object, that wraps a
 * Cocoon {@link Part} object in a DataSource interface.
 *
 * @see javax.activation.DataSource
 * @version $Id$
 */
public class FilePartDataSource extends AbstractDataSource {

    private Part part;

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link Part} object.
     * @param part An {@link Part} object.
     */
    public FilePartDataSource(Part part) {
        this(part,null,null);
    }

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link Part} object.
     * @param part An {@link Part} object.
     */
    public FilePartDataSource(Part part, String type, String name) {
        super(getName(name, part), getType(type, part));
        this.part = part;
    }

    /**
     * Determines the name for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>name</code> argument.
     * <li>The value returned by the {@link Part#getFileName()}.
     * <li>"attachment".
     * </ul>
     *
     * @return the name for this <code>DataSource</code>
     */
    private static String getName(String name, Part part) {
        if (isNullOrEmpty(name)) {
            name = part.getFileName();
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
     * <li>The value returned by {@link Part#getMimeType()}.
     * <li>"application/octet-stream".
     * </ul>
     *
     * @return The content type (mime type) of this <code>DataSource</code> object.
     */
    private static String getType(String type, Part part) {
        if (isNullOrEmpty(type)) {
            type = part.getMimeType();
            if (isNullOrEmpty(type)) {
                type = "application/octet-stream";
            }
        }

        return type;
    }

    /**
     * The InputStream object obtained from {@link Part#getInputStream()}
     * object.
     *
     * @throws java.io.IOException if an I/O error occurs.
     * @return The InputStream object for this <code>DataSource</code> object.
     */
    public InputStream getInputStream() throws IOException {
        try {
            return part.getInputStream();
        } catch (IOException e) {
            // Sun's SMTPTransport looses cause exception. Log it now.
            if (getLogger() != null) {
                getLogger().warn("Unable to obtain input stream for '" + part.getUploadName() + "'", e);
            }
            throw e;
        }
    }
}
