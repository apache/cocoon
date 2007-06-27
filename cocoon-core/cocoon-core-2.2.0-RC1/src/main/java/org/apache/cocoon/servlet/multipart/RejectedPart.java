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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An upload part that was rejected because request length exceeded the maximum upload size.
 * 
 * @version $Id$
 * @since 2.1.8
 */
public class RejectedPart extends Part {
    
    private int size;
    public int contentLength;
    public int maxContentLength;

    public RejectedPart(Map headers, int partSize, int contentLength, int maxContentLength) {
        super(headers);
        this.size = partSize;
        this.contentLength = contentLength;
        this.maxContentLength = maxContentLength;
    }

    public String getFileName() {
        return (String) headers.get("filename");
    }

    /**
     * Get the size of this part.
     * 
     * @return the size in bytes
     */
    public int getSize() {
        return this.size;
    }
    
    /**
     * Get the maximum allowed upload size. Not that this applies to the full request content length,
     * including multipart boundaries and other form data values.
     * <p>
     * This means that an upload part can be rejected although it's individual size is (a bit) smaller
     * than the maximum size. It is therefore advisable to use {@link #getContentLength()} to build
     * error messages rather than {@link #getSize()}.
     * 
     * @return the maximum content length in bytes
     */
    public int getMaxContentLength() {
        return this.maxContentLength;
    }

    /**
     * Get the content length of the request that cause this part to be rejected.
     * 
     * @return the content length in bytes
     */
    public int getContentLength() {
        return this.contentLength;
    }
    /**
     * Always throw an <code>IOException</code> as this part was rejected.
     */
    public InputStream getInputStream() throws IOException {
        throw new IOException("Multipart element '" + getFileName() + "' is too large (" +
                this.size + " bytes) and was discarded.");
    }

    /**
     * Always return <code>true</code>
     */
    public boolean isRejected() {
        return true;
    }

    public void dispose() {
        // nothing
    }
}
