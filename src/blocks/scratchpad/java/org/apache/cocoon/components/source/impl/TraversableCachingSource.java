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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

/**
 * Traversable version of {@link org.apache.cocoon.components.source.impl.CachingSource}.
 */
public class TraversableCachingSource extends CachingSource implements TraversableSource {

	// the Source children in case of a collection
    private TraversableSource[] m_children;
    
    public TraversableCachingSource(String protocol,
                                    String uri,
                                    String sourceURI,
                                    String cacheName,
                                    int expires,
                                    Map parameters,
                                    boolean async) {
        super(protocol, uri, sourceURI, cacheName, expires, parameters, async);
    }
    
    public TraversableCachingSource(String protocol,
                                    String location,
                                    TraversableSource source,
                                    String cacheName,
                                    int expires,
                                    Map parameters,
                                    boolean async) {
        super(protocol, location, source, cacheName, expires, parameters, async);
    }
    
    public void dispose() {
		super.dispose();
		m_children = null;
    }
    
    // ---------------------------------------------------- TraversableSource implementation

    public String getName() {
        
        try {
            initMetaResponse(false);
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
            initMetaResponse(false);
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
        
        try {
            initMetaResponse(false);
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
        
        return createChildSource(name);
    }

    public Collection getChildren() throws SourceException {
        
        try {
            initMetaResponse(false);
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
        if (m_children != null) {
            for (int i = 0; i < m_children.length; i++) {
                result.add(createChildSource(m_children[i]));
            }
        }
        else {
            final String[] children = ((TraversableSourceMeta) super.response.getExtra()).getChildren();
            for (int i = 0; i < children.length; i++) {
                result.add(createChildSource(children[i]));
            }
        }
        
        return result;
    }

    public Source getParent() throws SourceException {
        
        try {
            initMetaResponse(false);
        }
        catch (SourceException e) {
            throw e;
        }
        catch (IOException e) {
            throw new SourceException("Failure getting child", e);
        }
        
        return createSource(getParentURI(super.uri), getParentURI(super.sourceURI));
    }


    // ---------------------------------------------------- helper methods
    
    protected SourceMeta readMeta() throws IOException {
        final TraversableSourceMeta meta = new TraversableSourceMeta();
        
        meta.setName(getTraversableSource().getName());
        meta.setIsCollection(getTraversableSource().isCollection());
        meta.setLastModified(getTraversableSource().getLastModified());
        meta.setMimeType(getTraversableSource().getMimeType());
        
        if (meta.isCollection()) {
            final Collection children = getTraversableSource().getChildren();
            if (children != null) {
                m_children = new TraversableSource[children.size()];
                final String[] names = new String[children.size()];
                final Iterator iter = children.iterator();
                int count = 0;
                while(iter.hasNext()) {
                    TraversableSource child = (TraversableSource) iter.next();
                    m_children[count] = child;
                    names[count] = child.getName();
                    count++;
                }
                meta.setChildren(names);
            }
        }
        return meta;
    }
    
    protected TraversableSource getTraversableSource() throws MalformedURLException, IOException {
        return (TraversableSource) getSource();
    }
    
    private TraversableCachingSource createChildSource(TraversableSource childSource)
    throws SourceException {
        return createSource(childSource, getChildURI(super.uri, childSource.getName()));
    }

    private TraversableCachingSource createSource(TraversableSource wrappedSource, String sourceURI)
    throws SourceException {
        final TraversableCachingSource child = 
            new TraversableCachingSource(super.protocol,
                                         sourceURI,
                                         wrappedSource,
                                         super.cacheName,
                                         super.expires,
                                         super.parameters,
                                         super.async);
        child.cache = super.cache;
        child.resolver = super.resolver;
        ContainerUtil.enableLogging(child,getLogger());
        try {
            ContainerUtil.service(child,super.manager);
            ContainerUtil.initialize(child);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }
        return child;
    }
    
    private TraversableCachingSource createChildSource(String childName) 
    throws SourceException {
        return createSource(getChildURI(super.uri, childName), getChildURI(super.sourceURI, childName));
    }

    private TraversableCachingSource createSource(String uri, String sourceURI) 
    throws SourceException {
        final TraversableCachingSource source = 
            new TraversableCachingSource(super.protocol,
                                         uri,
                                         sourceURI,
                                         super.cacheName,
                                         super.expires,
                                         super.parameters,
                                         super.async);
        source.cache = super.cache;
        source.resolver = super.resolver;
        ContainerUtil.enableLogging(source, getLogger());
        try {
            ContainerUtil.service(source, super.manager);
            ContainerUtil.initialize(source);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }
        return source;
    }
    
    /**
     * Calculate the cached child URI based on a parent URI
     * and a child name.
     */
    private String getChildURI(String parentURI, String childName) {
        
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
    private String getParentURI(String childURI) {
        
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
