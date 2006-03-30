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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

/**
 * Traversable version of {@link org.apache.cocoon.components.source.impl.CachingSource}.
 */
public class TraversableCachingSource extends CachingSource implements TraversableSource {
    
    private TraversableSource tsource;
    
    public TraversableCachingSource(String protocol,
                                    String uri,
                                    TraversableSource source,
                                    int expires,
                                    String cacheName,
                                    boolean async,
                                    boolean eventAware) {
        super(protocol, uri, source, expires, cacheName, async, eventAware);
        this.tsource = source;
    }
    
    // ---------------------------------------------------- TraversableSource implementation

    public String getName() {
        
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing traversable response", e);
            }
            return null;
        }
        
        return ((TraversableSourceMeta) super.response.getExtra()).getName();
    }

    public boolean isCollection() {
        
        try {
            initMetaResponse();
        }
        catch (IOException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failure initializing traversable response", e);
            }
            return false;
        }
        
        return ((TraversableSourceMeta) super.response.getExtra()).isCollection();
    }

    public Source getChild(String name) throws SourceException {
        
        Source child;
        try {
            initMetaResponse();
            child = this.tsource.getChild(name);
        }
        catch (SourceException e) {
            throw e;
        }
        catch (IOException e) {
            throw new SourceException("Failure getting child", e);
        }
        
        if (!isCollection()) {
            throw new SourceException("Source is not a collection");
        }
        
        return createSource(getChildURI(super.uri, name), child);
    }

    public Collection getChildren() throws SourceException {
        
        try {
            initMetaResponse();
        }
        catch (SourceException e) {
            throw e;
        }
        catch (IOException e) {
            throw new SourceException("Failure getting child", e);
        }
        
        if (!isCollection()) {
            throw new SourceException("Source is not a collection");
        }
        
        final Collection result = new ArrayList();
        final TraversableSourceMeta meta = (TraversableSourceMeta) super.response.getExtra();
        final String[] children = meta.getChildren();
        for (int i = 0; i < children.length; i++) {
            Source child;
            try {
                child = this.tsource.getChild(children[i]);
            }
            catch (IOException e) {
                throw new SourceException("Failure getting child", e);
            }
            result.add(createSource(getChildURI(super.uri, children[i]), child));
        }
        
        return result;
    }

    public Source getParent() throws SourceException {
        
        Source parent;
        try {
            initMetaResponse();
            parent = this.tsource.getParent();
        }
        catch (SourceException e) {
            throw e;
        }
        catch (IOException e) {
            throw new SourceException("Failure getting parent", e);
        }
        
        return createSource(getParentURI(super.uri), parent);
    }


    // ---------------------------------------------------- helper methods

    
    
    protected final TraversableCachingSource createSource(String uri, Source wrapped) 
    throws SourceException {
        final TraversableCachingSource source = newSource(uri, wrapped);
        initializeSource(source);
        return source;
    }

    protected TraversableCachingSource newSource(String uri, Source wrapped) {
        return  new TraversableCachingSource(super.protocol,
                                             uri,
                                             (TraversableSource) wrapped,
                                             super.expires,
                                             super.cacheName,
                                             super.async,
                                             super.eventAware);
    }

    protected void initializeSource(TraversableCachingSource source) throws SourceException {
        source.cache = super.cache;
        ContainerUtil.enableLogging(source, getLogger());
        try {
            ContainerUtil.service(source, super.manager);
            ContainerUtil.initialize(source);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }
    }
    
    protected SourceMeta createMeta() {
        return new TraversableSourceMeta();
    }
    
    protected void initMeta(SourceMeta meta, Source source) throws IOException {
        super.initMeta(meta, source);

        final TraversableSource tsource = (TraversableSource) source;
        final TraversableSourceMeta tmeta = (TraversableSourceMeta) meta;

        tmeta.setName(tsource.getName());
        tmeta.setIsCollection(tsource.isCollection());

        if (tmeta.isCollection()) {
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
                tmeta.setChildren(names);
            }
        }

    }
    
    protected void remove() {
        remove(true);
    }

    /**
     * The parent's cached response needs to be removed from cache
     * as well because it's cached list of children is no longer valid.
     */
    private void remove(boolean flag) {
        super.remove();
        if (flag) {
            try {
                TraversableCachingSource parent = (TraversableCachingSource) getParent();
                parent.remove(false);
            }
            catch (SourceException e) {
                getLogger().error("Error removing parent's cached response");
            }
        }
    }

    /**
     * Calculate the cached child URI based on a parent URI
     * and a child name.
     */
    private static String getChildURI(String parentURI, String childName) {
        
        // separate query string from rest of parentURI
        String rest, qs;
        int index = parentURI.indexOf('?');
        if (index != -1) {
            rest = parentURI.substring(0,index);
            qs = parentURI.substring(index);
        }
        else {
            rest = parentURI;
            qs = "";
        }
        
        // calculate qs-less child uri
        String childURI;
        if (rest.charAt(rest.length()-1) == '/') {
            childURI = rest + childName;
        }
        else {
            childURI = rest + "/" + childName;
        }
        
        return childURI + qs;
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
        }
        else {
            rest = childURI;
            qs = "";
        }
        
        // calculate qs-less parent uri
        String parentUri;
        index = rest.lastIndexOf('/',rest.length()-2);
        if (index != -1) {
            parentUri = rest.substring(0,index);
        }
        else {
            parentUri = rest;
        }
        
        return parentUri + qs;
    }

    protected static class TraversableSourceMeta extends SourceMeta {
        private String   m_name;
        private boolean  m_isCollection;
        private String[] m_children;

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
