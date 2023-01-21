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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.excalibur.source.SourceUtil;

/**
 * The InputStreamDataSource class provides an object, that wraps an
 * {@link InputStream} object in a DataSource interface.
 *
 * @see javax.activation.DataSource
 * @version $Id$
 */
public class InputStreamDataSource extends AbstractDataSource {

    private byte[] data;

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link InputStream} object.
     *
     * @param in An {@link InputStream} object.
     */
    public InputStreamDataSource(InputStream in) throws IOException {
        this(in, null, null);
    }

    /**
     * Creates a new instance of FilePartDataSource from a byte array.
     */
    public InputStreamDataSource(byte[] data, String type, String name) {
        super(getName(name), getType(type));

        if (data == null) {
            this.data = new byte[0];
        } else {
            this.data = data;
        }
    }

    /**
     * Creates a new instance of FilePartDataSource from an
     * {@link InputStream} object.
     *
     * @param in An {@link InputStream} object.
     */
    public InputStreamDataSource(InputStream in, String type, String name) throws IOException {
        super(getName(name), getType(type));

        // Need to copy contents of InputStream into byte array since getInputStream
        // method is called more than once by JavaMail API.
        if (in == null) {
            data = new byte[0];
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SourceUtil.copy(in, out);
            data = out.toByteArray();
        }
    }

    /**
     * Determines the name for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>name</code> argument.
     * <li>"attachment".
     * </ul>
     *
     * @return the name for this <code>DataSource</code>
     */
    private static String getName(String name) {
        if (isNullOrEmpty(name)) {
            name = "attachment";
        }

        return name;
    }

    /**
     * Determines the mime type for this <code>DataSource</code> object.
     * It is first non empty value from the list:
     * <ul>
     * <li>The value of <code>type</code> argument.
     * <li>"application/octet-stream".
     * </ul>
     *
     * @return The content type (mime type) of this <code>DataSource</code> object.
     */
    private static String getType(String type) {
        if (isNullOrEmpty(type)) {
            type = "application/octet-stream";
        }

        return type;
    }

    /**
     * The InputStream object passed into contructor.
     *
     * @return The InputStream object for this <code>DataSource</code> object.
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
}
