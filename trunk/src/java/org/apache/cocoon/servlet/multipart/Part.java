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
package org.apache.cocoon.servlet.multipart;

import java.io.InputStream;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;


/**
 * This (abstract) class represents a file part parsed from a http post stream.
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @version CVS $Id: Part.java,v 1.3 2003/11/13 15:02:07 sylvain Exp $
 */
public abstract class Part implements Disposable {

    private boolean disposeWithRequest = true;

    /** Field headers */
    protected Map headers;

    protected Part(Map headers) {
	    this.headers = headers;
    }

    /**
     * Returns the part headers
     */
    public Map getHeaders() {
        return headers;
    }

    /**
     * Returns the filename
     */
    public abstract String getFileName();
    
    /**
     * Returns the original filename
     */
    public String getUploadName(){
        return (String) headers.get("filename");
    }
    
    /**
     * Returns the length of the file content
     */
    public abstract int getSize();

    /**
     * Returns the mime type (or null if unknown)
     */
    public String getMimeType() {
        return (String) headers.get("content-type");
    }
    
    /**
     * Do we want any temporary resource held by this part to be cleaned up when processing of
     * the request that created it is finished? Default is <code>true</code>.
     * 
     * @return <code>true</code> if the part should be disposed with the request.
     */
    public boolean disposeWithRequest() {
        return this.disposeWithRequest;
    }
    
    /**
     * Set the value of the <code>disposeWithRequest</code> flag (default is <code>true</code>).
     * 
     * @param dispose <code>true</code> if the part should be disposed after request processing
     */
    public void setDisposeWithRequest(boolean dispose) {
        this.disposeWithRequest = dispose;
    }
    
    /**
     * Returns an InputStream containing the file data
     * @throws Exception
     */
    public abstract InputStream getInputStream() throws Exception;
}
