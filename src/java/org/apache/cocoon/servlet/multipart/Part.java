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
package org.apache.cocoon.servlet.multipart;

import java.io.InputStream;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;


/**
 * This (abstract) class represents a file part parsed from a http post stream.
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @version CVS $Id: Part.java,v 1.4 2004/03/08 14:03:30 cziegeler Exp $
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
