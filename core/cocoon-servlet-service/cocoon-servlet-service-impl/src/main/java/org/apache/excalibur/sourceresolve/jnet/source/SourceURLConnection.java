/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.SAXResult;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.xml.sax.XMLizable;

public class SourceURLConnection extends URLConnection {

    protected final SourceFactory factory;

    protected final String url;

    protected Source source;

    protected Map requestProperties;

    protected String contentType = "text/plain";

    public SourceURLConnection(SourceFactory factory, URL url) {
        super(url);
        this.factory = factory;
        this.url = url.toExternalForm();
    }

    /**
     * @see java.net.URLConnection#connect()
     */
    public void connect() throws IOException {
        if ( this.source != null ) {
            this.factory.release(this.source);
            this.source = null;
            throw new IllegalStateException("Connection to " + this.url + " already established.");
        }
        this.source = this.factory.getSource(this.url, this.getRequestProperties());
        this.connected = true;
        final String contentType = this.source.getMimeType();
        if ( contentType != null ) {
            this.contentType = contentType;
        }
    }

    /**
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        if ( !this.connected ) {
            this.connect();
        }
        final InputStream is;
        is = new SourceIOInputStream(this.factory, this.source);
        return is;
    }

    /**
     * @see java.net.URLConnection#addRequestProperty(java.lang.String, java.lang.String)
     */
    public void addRequestProperty(String arg0, String arg1) {
        this.setRequestProperty(arg0, arg1);
    }

    /**
     * @see java.net.URLConnection#getRequestProperties()
     */
    public Map getRequestProperties() {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        if ( this.requestProperties == null ) {
            return Collections.EMPTY_MAP;
        }
        return this.requestProperties;
    }

    /**
     * @see java.net.URLConnection#getRequestProperty(java.lang.String)
     */
    public String getRequestProperty(String key) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        if (key == null) {
            throw new NullPointerException ("key is null");
        }
        if ( this.requestProperties == null ) {
            return null;
        }
        return (String)this.requestProperties.get(key);
    }

    /**
     * @see java.net.URLConnection#setRequestProperty(java.lang.String, java.lang.String)
     */
    public void setRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        if (key == null) {
            throw new NullPointerException ("key is null");
        }
        if ( this.requestProperties == null ) {
            this.requestProperties = new HashMap();
        }
        if ( value == null ) {
            this.requestProperties.remove(key);
        } else {
            this.requestProperties.put(key, value);
        }
    }

    /**
     * @see java.net.URLConnection#getContentType()
     */
    public String getContentType() {
        return this.contentType;
    }

    public Object getContent(Class[] classes) throws IOException {
        if ( !this.connected ) {
            this.connect();
        }
        if ( this.source instanceof XMLizable && classes != null) {
            boolean found = false;
            int index = 0;
            while ( !found && index < classes.length ) {
                if ( classes[index].getName().equals(SAXResult.class.getName())) {
                    found = true;
                } else {
                    index++;
                }
            }
            if ( found ) {
                return new SourceSAXResult(this.factory, this.source, (XMLizable)this.source);
            }
        }
        return super.getContent(classes);
    }

}
