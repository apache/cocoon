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

import javax.activation.DataSource;

import org.apache.cocoon.servlet.multipart.Part;

/** The FilePartDataSource class provides an object, that wraps a
 * Cocoon {@link Part}
 * object in a DataSource interface.
 * @see javax.activation.DataSource
 *
 * @author <a href="mailto:frank.ridderbusch@gmx.de">Frank Ridderbusch</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: FilePartDataSource.java,v 1.5 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class FilePartDataSource implements DataSource {
    private Part part;
    private String contentType = null;
    private String name = null;
    
    /** Creates a new instance of FilePartDataSource from an
     * {@link Part} object.
     * @param part An {@link Part} object.
     */
    public FilePartDataSource(Part part) {
        this(part,null,null);
    }
    
    /** Creates a new instance of FilePartDataSource from an
     * {@link Part} object.
     * @param part An {@link Part} object.
     */
    public FilePartDataSource(Part part, String type, String name) {
        this.part = part;
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
        
    /** Return the content type (mime type) obtained from
     * {@link Part#getMimeType()}.
     * Return <CODE>application/octet-stream</CODE> if <CODE>getMimeType()</CODE>
     * returns <CODE>null</CODE>.
     * @return The content type (mime type) for this <CODE>DataSource</CODE> object.
     */    
    public String getContentType() {
        if (this.contentType != null) { 
            return this.contentType;
        }
        String mimeType = part.getMimeType();
        if (isNullOrEmpty(mimeType)) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
    
    /** The InputStream object obtained from
     * {@link Part#getInputStream()}
     * object.
     * @throws java.io.IOException if an I/O error occurs.
     * @return The InputStream object for this <CODE>DataSource</CODE> object.
     */    
    public java.io.InputStream getInputStream() throws java.io.IOException {
        java.io.InputStream inp;
        try {
            inp = part.getInputStream();
        } catch (Exception e) {
            throw new java.io.IOException(e.getMessage());
        }
        return inp;
    }
    
    /** Returns the name for this <CODE>DataSource</CODE> object. This is
     * what is returned by
     * {@link Part#getFileName()}.
     * @return the name for this <CODE>DataSource</CODE> object.
     */    
    public String getName() {
        String name = (this.name != null ? this.name : part.getFileName());
        if (isNullOrEmpty(name)) name="attachment";
        return name;
    }
    
    /** Unimplemented. Directly throws <CODE>IOException</CODE>.
     * @throws java.io.IOException since unimplemented
     * @return nothing
     */    
    public java.io.OutputStream getOutputStream() throws java.io.IOException {
        throw new java.io.IOException("no data sink available");
    }
    
}
