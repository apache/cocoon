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

import org.apache.excalibur.source.Source;

/** The SourceDataSource class provides an object, that wraps a
 * Cocoon <CODE>org.apache.excalibur.source.Source</CODE> object
 * in a DataSource interface.
 * @see org.apache.excalibur.source.Source
 * @see javax.activation.DataSource
 * @author <a href="mailto:frank.ridderbusch@gmx.de">Frank Ridderbusch</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SourceDataSource.java,v 1.2 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class SourceDataSource implements DataSource {
    private Source src;
    private String contentType = null;
    private String name = null;
    
    /** Creates a new instance of SourceDataSource
     * @param src A <CODE>org.apache.excalibur.source.Source</CODE> Object.
     */
    public SourceDataSource(Source src) {
        this(src,null,null);
    }
    
    /** Creates a new instance of SourceDataSource
     * @param src A <CODE>org.apache.excalibur.source.Source</CODE> Object.
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
    
    /** Returns the result of a call to the <CODE>Source</CODE>
     * objects <CODE>getMimeType()</CODE> method. Returns
     * "application/octet-stream", if <CODE>getMimeType()</CODE>
     * returns <code>null</code>.
     * @return The content type (mime type) of this <CODE>DataSource</CODE> object.
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
    
    /** Get the <CODE>InputStream</CODE> object from the
     * <CODE>Source</CODE> object.
     * @throws java.io.IOException if an I/O error occurs.
     * @return The InputStream object from the <CODE>Source</CODE> object.
     * @see org.apache.excalibur.source.Source#getInputStream()
     */    
    public java.io.InputStream getInputStream() throws java.io.IOException {
        return src.getInputStream();
    }
    
    /** Returns the name for this <CODE>DataSource</CODE> object. This is
     * actually the last path component (after the last '/') from the value
     * returned by the <CODE>getURI()</CODE> method of the <CODE>Source</CODE>
     * object.
     * @return the name for this <CODE>DataSource</CODE>
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
    
    /** Unimplemented. Directly throws <CODE>IOException</CODE>.
     * @throws java.io.IOException since unimplemented
     * @return nothing
     */    
    public java.io.OutputStream getOutputStream() throws java.io.IOException {
        throw new java.io.IOException("no data sink available");
    }
    
}
