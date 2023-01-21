/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.springframework.core.io.Resource;

/**
 * @version $Id$
 */
public class SourceResource implements Resource {

    protected Source source;

    protected SourceResolver resolver;

    protected boolean open;

    public SourceResource(Source s, SourceResolver r) {
        this.source = s;
        this.resolver = r;
    }

    /**
     * @see org.springframework.core.io.InputStreamSource#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        this.open = true;
        return new SourceIOInputStream(this.resolver, this.source);
    }

    /**
     * @see org.springframework.core.io.Resource#createRelative(java.lang.String)
     */
    @Override
    public Resource createRelative(String uri) throws IOException {
        int pos = this.source.getURI().lastIndexOf('/');
        return new SourceResource(
                this.resolver.resolveURI(uri, this.source.getURI().substring(0, pos), null), this.resolver);
    }

    /**
     * @see org.springframework.core.io.Resource#exists()
     */
    @Override
    public boolean exists() {
        return this.source.exists();
    }

    /**
     * @see org.springframework.core.io.Resource#getDescription()
     */
    @Override
    public String getDescription() {
        return "Source: " + this.source;
    }

    /**
     * @see org.springframework.core.io.Resource#getFile()
     */
    @Override
    public File getFile() throws IOException {
        return SourceUtil.getFile(this.source);
    }

    /**
     * @see org.springframework.core.io.Resource#getFilename()
     */
    @Override
    public String getFilename() {
        int pos = this.source.getURI().lastIndexOf('/');
        return this.source.getURI().substring(pos + 1);
    }

    /**
     * @see org.springframework.core.io.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
        return new URL(this.source.getURI());
    }

    public String getUrlString() {
        return this.source.getURI();
    }

    /**
     * @see org.springframework.core.io.Resource#isOpen()
     */
    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public URI getURI() throws IOException {
        try {
            return new URI(this.source.getURI());
        } catch (URISyntaxException e) {
            IOException ioe = new IOException("Exception because of the URI syntax.", e);
            throw ioe;
        }
    }

    /**
     * @see org.springframework.core.io.Resource#isReadable()
     */
    @Override
    public boolean isReadable() {
        return this.source.exists();
    }

    @Override
    public long lastModified() throws IOException {
        return this.source.getLastModified();
    }

    @Override
    public long contentLength() throws IOException {
        return this.source.getContentLength();
    }

}
