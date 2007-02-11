/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
  
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: FilePartDataSource.java,v 1.4 2004/02/19 22:13:28 joerg Exp $
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
