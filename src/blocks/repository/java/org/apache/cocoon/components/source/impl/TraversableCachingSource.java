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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

/**
 * Traversable version of {@link org.apache.cocoon.components.source.impl.CachingSource}.
 */
public class TraversableCachingSource extends CachingSource implements TraversableSource {

    private CachingSourceFactory factory;

    public TraversableCachingSource(final CachingSourceFactory factory,
                                    final String protocol,
                                    final String uri,
                                    final String sourceUri,
                                    TraversableSource source,
                                    int expires,
                                    String cacheName,
                                    boolean async,
                                    boolean eventAware) {
        super(protocol, uri, sourceUri, source, expires, cacheName, async, eventAware);
        this.factory = factory;
    }

    private TraversableSource getTraversableSource() {
        return (TraversableSource) super.source;
    }

    private TraversableSourceMeta getTraversableResponseMeta() throws IOException {
        return (TraversableSourceMeta) getResponseMeta();
    }

    // ---------------------------------------------------- TraversableSource implementation

    public String getName() {
        try {
            return getTraversableResponseMeta().getName();
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing traversable response", e);
            }
            return null;
        }
    }

    public boolean isCollection() {
        try {
            return getTraversableResponseMeta().isCollection();
        } catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing traversable response", e);
            }
            return false;
        }
    }

    public Source getChild(String name) throws SourceException {
        if (!isCollection()) {
            throw new SourceException("Source is not a collection");
        }

        Source child;
        try {
            getResponseMeta();
            child = getTraversableSource().getChild(name);
        } catch (SourceException e) {
            throw e;
        } catch (IOException e) {
            throw new SourceException("Failure getting child", e);
        }

        boolean isCollection = false;
        if (child instanceof TraversableSource) {
            isCollection = ((TraversableSource) child).isCollection();
        }

        return createSource(getChildURI(super.uri, isCollection, name), getChildURI(super.sourceUri, isCollection, name), child);
    }

    public Collection getChildren() throws SourceException {
        final TraversableSourceMeta meta;
        try {
            meta = getTraversableResponseMeta();
        } catch (SourceException e) {
            throw e;
        } catch (IOException e) {
            throw new SourceException("Failure getting child", e);
        }

        if (!isCollection()) {
            throw new SourceException("Source is not a collection");
        }

        final Collection result = new ArrayList();
        final String[] children = meta.getChildren();
        for (int i = 0; i < children.length; i++) {
            Source child;
            try {
                child = getTraversableSource().getChild(children[i]);
            } catch (IOException e) {
                throw new SourceException("Failure getting child", e);
            }

            boolean isCollection = false;
            if (child instanceof TraversableSource) {
                isCollection = ((TraversableSource) child).isCollection();
            }

            result.add(createSource(getChildURI(super.uri, isCollection, children[i]), getChildURI(super.sourceUri, isCollection, children[i]), child));
        }

        return result;
    }

    public Source getParent() throws SourceException {
        Source parent;
        try {
            getResponseMeta();
            parent = getTraversableSource().getParent();
        } catch (SourceException e) {
            throw e;
        } catch (IOException e) {
            throw new SourceException("Failure getting parent", e);
        }

        return createSource(getParentURI(super.uri), getParentURI(super.sourceUri), parent);
    }


    // ---------------------------------------------------- helper methods

    protected final TraversableCachingSource createSource(String uri, String wrappedUri, Source wrapped)
    throws SourceException {
        return (TraversableCachingSource) this.factory.createCachingSource(uri, wrappedUri, wrapped, expires, cacheName);
    }

    protected SourceMeta readMeta(Source source) throws SourceException {
        return new TraversableSourceMeta(source);
    }

    /**
     * Calculate the cached child URI based on a parent URI
     * and a child name.
     */
    private static String getChildURI(String parentURI, boolean isCollection, String childName) {
        // separate query string from rest of parentURI
        String rest, qs;
        int index = parentURI.indexOf('?');
        if (index != -1) {
            rest = parentURI.substring(0,index);
            qs = parentURI.substring(index);
        } else {
            rest = parentURI;
            qs = "";
        }

        // calculate child uri
        StringBuffer childURI = new StringBuffer(rest);
        if (rest.charAt(rest.length()-1) != '/') {
            childURI.append('/');
        }
        childURI.append(childName);
        if (isCollection) {
            childURI.append('/');
        }
        childURI.append(qs);
        return childURI.toString();
    }

    /**
     * Calculate the cached parent URI based on a child URI.
     */
    private static String getParentURI(String childURI) {
        // separate query string from rest of uri
        String rest, qs;
        int index = childURI.indexOf('?');
        if (index != -1) {
            rest = childURI.substring(0, index);
            qs = childURI.substring(index);
        } else {
            rest = childURI;
            qs = "";
        }

        // calculate qs-less parent uri
        String parentUri;
        index = rest.lastIndexOf('/', rest.length() - 2);
        if (index != -1) {
            parentUri = rest.substring(0, index + 1);
        } else {
            parentUri = rest;
        }

        return parentUri + qs;
    }

    protected static class TraversableSourceMeta extends SourceMeta {
        private String   m_name;
        private boolean  m_isCollection;
        private String[] m_children;

        public TraversableSourceMeta() {
            super();
        }

        public TraversableSourceMeta(Source source) throws SourceException {
            super(source);

            final TraversableSource tsource = (TraversableSource) source;

            setName(tsource.getName());
            setIsCollection(tsource.isCollection());

            if (isCollection()) {
                final Collection children = tsource.getChildren();
                if (children != null) {
                    final String[] names = new String[children.size()];
                    final Iterator iter = children.iterator();
                    int count = 0;
                    while(iter.hasNext()) {
                        TraversableSource child = (TraversableSource) iter.next();
                        names[count] = child.getName();
                        count++;
                    }
                    setChildren(names);
                }
            }
        }

        protected String getName() {
            return m_name;
        }

        protected void setName(String name) {
            m_name = name;
        }

        protected boolean isCollection() {
            return m_isCollection;
        }

        protected void setIsCollection(boolean isCollection) {
            m_isCollection = isCollection;
        }

        protected String[] getChildren() {
            return m_children;
        }

        protected void setChildren(String[] children) {
            m_children = children;
        }
    }

}
