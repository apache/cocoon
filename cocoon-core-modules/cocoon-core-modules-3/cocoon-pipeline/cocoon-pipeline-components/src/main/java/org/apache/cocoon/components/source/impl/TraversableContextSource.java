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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.TraversableSource;

/**
 * @version SVN $Id$
 */
public class TraversableContextSource
    implements Source, TraversableSource { 

    final protected Source wrappedSource;
    final protected Set children;
    final protected ContextSourceFactory factory;
    final protected String path;
    final protected String scheme;

    public TraversableContextSource(Source source, 
                                    Set children, 
                                    ContextSourceFactory factory,
                                    String path,
                                    String scheme) {
        this.wrappedSource = source;
        this.children = children;
        this.factory = factory;
        this.path = path;
        this.scheme = scheme;
    }

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return this.wrappedSource.exists();
    }

    /**
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public long getContentLength() {
        return this.wrappedSource.getContentLength();
    }

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() 
    throws IOException, SourceNotFoundException {
        return this.wrappedSource.getInputStream();
    }

    /**
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified() {
        return this.wrappedSource.getLastModified();
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        return this.wrappedSource.getMimeType();
    }

    /**
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getURI() {
        return this.wrappedSource.getURI();
    }

    /**
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        return this.wrappedSource.getValidity();
    }

    /**
     * @see org.apache.excalibur.source.Source#refresh()
     */
    public void refresh() {
        this.wrappedSource.refresh();
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getChild(java.lang.String)
     */
    public Source getChild(String name) throws SourceException {
        final String postfixOne = '/' + name + '/';
        final String postfixTwo = '/' + name;
        final Iterator i = this.children.iterator();
        while ( i.hasNext() ) {
            String uri = (String)i.next();
            if ( uri.endsWith(postfixOne ) ){
                uri = "context:/" + uri;
                uri = uri.substring(0, uri.length()-1);
                try {
                    return this.factory.getSource(uri, null);
                } catch (IOException ioe) {
                    throw new SourceException("Unable to get source for: " + uri);
                }                
            } else if ( uri.endsWith(postfixTwo) ) {
                uri = "context:/" + uri;
                try {
                    return this.factory.getSource(uri, null);
                } catch (IOException ioe) {
                    throw new SourceException("Unable to get source for: " + uri);
                }                
            }
        }
        return null;
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        final List l = new ArrayList();
        final Iterator i = this.children.iterator();
        while ( i.hasNext() ) {
            String uri = (String)i.next();
            uri = "context:/" + uri;
            try {
                l.add(this.factory.getSource(uri, null));
            } catch (IOException ioe) {
                final Iterator ci = l.iterator();
                while ( ci.hasNext() ) {
                    this.factory.release((Source)ci.next());
                }
                throw new SourceException("Unable to get source for: " + uri);
            }
        }
        return l;
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getName()
     */
    public String getName() {
        final String uri = this.wrappedSource.getURI();
        return uri.substring(uri.lastIndexOf('/')+1);
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#getParent()
     */
    public Source getParent() throws SourceException {
        String uri = "context:/" + this.path;
        uri = uri.substring(0, uri.lastIndexOf('/'));
        try {
            return this.factory.getSource(uri, null);
        } catch (IOException ioe) {
            throw new SourceException("Unable to get source for: " + uri);
        }                
    }

    /**
     * @see org.apache.excalibur.source.TraversableSource#isCollection()
     */
    public boolean isCollection() {
        return true;
    }
}
