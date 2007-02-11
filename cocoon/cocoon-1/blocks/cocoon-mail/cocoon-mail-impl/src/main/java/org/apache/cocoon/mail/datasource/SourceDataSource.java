/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.OutputStream;

import javax.activation.DataSource;

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
public class SourceDataSource implements DataSource {
    private Source src;
    private String contentType;
    private String name;

    /**
     * Creates a new instance of SourceDataSource
     * @param src A <code>org.apache.excalibur.source.Source</code> Object.
     */
    public SourceDataSource(Source src) {
        this(src, null, null);
    }

    /**
     * Creates a new instance of SourceDataSource
     * @param src A <code>org.apache.excalibur.source.Source</code> Object.
     */
    public SourceDataSource(Source src, String type, String name) {
        this.src = src;
        this.contentType = type;
        this.name = name;
        if (isNullOrEmpty(this.name)) this.name = null;
        if (isNullOrEmpty(this.contentType)) this.contentType = null;
    }

    /**
     * Check String for null or empty.
     * @param str
     * @return true if str is null, empty string, or equals "null"
     */
     private boolean isNullOrEmpty(String str) {
         return (str == null || "".equals(str) || "null".equals(str));
     }

    /**
     * Returns the result of a call to the <code>Source</code>
     * objects <code>getMimeType()</code> method. Returns
     * "application/octet-stream", if <code>getMimeType()</code>
     * returns <code>null</code>.
     * @return The content type (mime type) of this <code>DataSource</code> object.
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getContentType() {
        if (this.contentType != null) {
            return this.contentType;
        }
        String mimeType = src.getMimeType();
        if (isNullOrEmpty(mimeType)) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    /**
     * Get the <code>InputStream</code> object from the
     * <code>Source</code> object.
     * @throws java.io.IOException if an I/O error occurs.
     * @return The InputStream object from the <code>Source</code> object.
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return src.getInputStream();
    }

    /**
     * Returns the name for this <code>DataSource</code> object. This is
     * actually the last path component (after the last '/') from the value
     * returned by the <code>getURI()</code> method of the <code>Source</code>
     * object.
     * @return the name for this <code>DataSource</code>
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getName() {
        if (this.name != null){
            return this.name;
        }
        String name = src.getURI();
        name = name.substring(name.lastIndexOf('/') + 1);
        return ("".equals(name)? "attachment" : name);
    }

    /**
     * Unimplemented. Directly throws <code>IOException</code>.
     * @throws java.io.IOException since unimplemented
     * @return nothing
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("no data sink available");
    }
}
