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
package org.apache.cocoon.servlet.multipart;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.ModifiableSource;


/**
 * This abstract class represents a file part parsed from a http post stream. The concrete
 * class, {@link PartOnDisk} or {@link PartInMemory} that is used depends on the upload configuration
 * in <code>web.xml</code>.
 * <p>
 * If uploaded data size exceeds the maximum allowed upload size (also specified in <code>web.xml</code>),
 * then an {@link RejectedPart} is used, from which no data can be obtained, but which gives some
 * information on the rejected uploads.
 *
 * @version $Id$
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
     * Is this part a rejected part? Provided as an alternative to <code>instanceof RejectedPart</code>
     * in places where it's not convenient such as flowscript.
     * 
     * @return <code>true</code> if this part was rejected
     */
    public boolean isRejected() {
        return false;
    }

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
     * @throws IOException
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Convenience method to copy a part to a modifiable source.
     * 
     * @param source the modifiable source to write to
     * @throws IOException
     * @since 2.1.8
     */
    public void copyToSource(ModifiableSource source) throws IOException {
        InputStream is = getInputStream();
        try {
            OutputStream os = source.getOutputStream();
            try {
                IOUtils.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }
    
    /**
     * Convenience method to copy a part to a file.
     * 
     * @param filename name of the file to write to
     * @throws IOException
     * @since 2.1.8
     */
    public void copyToFile(String filename) throws IOException {
        InputStream is = getInputStream();
        try {
            OutputStream os = new FileOutputStream(filename);
            try {
                IOUtils.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }
    
    /**
     * Dispose any resources held by this part, such as a file or memory buffer.
     * <p>
     * Disposal occurs in all cases when the part is garbage collected, but calling it explicitely
     * allows to cleanup resources more quickly.
     */
    public abstract void dispose();
}
