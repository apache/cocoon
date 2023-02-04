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
package org.apache.cocoon.environment.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import junit.framework.AssertionFailedError;
import org.apache.cocoon.environment.Environment;

public class MockEnvironment implements Environment {

    private String uri;
    private String uriprefix;
    private String view;
    private String action;
    private String contenttype;
    private int contentlength;
    private int status;
    private ByteArrayOutputStream outputstream;
    private Map objectmodel;
    private Hashtable attributes = new Hashtable();

    public MockEnvironment() {
        // empty constructor
    }

    public String getURI() {
        return uri;
    }

    public String getURIPrefix() {
        return uriprefix;
    }

    public String getView() {
        return view;
    }

    public String getAction() {
        return action;
    }

    public void setURI(String prefix, String uri) {
        this.uriprefix = prefix;
        this.uri = uri;
    }

    public void redirect(String url, boolean global, boolean permanent) throws IOException {
        throw new AssertionFailedError("Use Redirector.redirect instead!");
    }

    public void setContentType(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getContentType() {
        return contenttype;
    }

    public void setContentLength(int length) {
        this.contentlength = length;
    }

    public int getContentLength() {
        return contentlength;
    }

    public void setStatus(int statusCode) {
        this.status = statusCode;
    }

    public int getStatus() {
        return status;
    }

    public OutputStream getOutputStream(int bufferSize) throws IOException {
        outputstream = new ByteArrayOutputStream();
        return outputstream;
    }

    public byte[] getOutput() {
        return outputstream.toByteArray();
    }

    public Map getObjectModel() {
        return objectmodel;
    }

    public void setObjectModel(Map objectmodel) {
        this.objectmodel = objectmodel;
    }

    public boolean isResponseModified(long lastModified) {
        return true;
    }

    public void setResponseIsNotModified() {
        // nothing to do
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public boolean tryResetResponse() throws IOException {
        return false;
    }

    public void commitResponse() throws IOException {
        // do nothing
    }
    
    public void startingProcessing() {
        // do nothing
    }
    
    public void finishingProcessing() {
        // do nothing
    }

    /**
     * Always return <code>true</code>.
     */
    public boolean isExternal() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isInternalRedirect()
     */
    public boolean isInternalRedirect() {
        return false;
    }
}
