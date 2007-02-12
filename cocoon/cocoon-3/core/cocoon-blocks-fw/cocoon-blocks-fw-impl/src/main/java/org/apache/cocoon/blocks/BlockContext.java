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
package org.apache.cocoon.blocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.blocks.util.ServletContextWrapper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @version $Id$
 */
public class BlockContext extends ServletContextWrapper {
    
    public static final String SUPER = "super";

    private Hashtable attributes = new Hashtable();
    private Servlet servlet;
    private String mountPath;
    private String blockContextURL;
    private Map properties;
    private Map connections;


    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    /*
     *  TODO ineritance of attributes from the parent context is only
     *  partly implemented: removeAttribute and getAttributeNames
     *  doesn't respect inheritance yet.  
     */
    public Object getAttribute(String name) {
        Object value = this.attributes.get(name);
        return value != null ? value : super.getAttribute(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        // hack for getting a file protocol or other protocols that can be used as context
        // path in the getResource method in the servlet context
        if (!(blockContextURL.startsWith("file:") || blockContextURL.startsWith("/")
                || blockContextURL.indexOf(':') == -1)) {
            SourceResolver resolver = null;
            Source source = null;
            try {
                BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(this);
                resolver = (SourceResolver) factory.getBean(SourceResolver.ROLE);
                source = resolver.resolveURI(blockContextURL);
                blockContextURL = source.getURI();
            } catch (IOException e) {
                throw new MalformedURLException("Could not resolve " + blockContextURL);
            } finally {
                if (resolver != null)
                    resolver.release(source);
            }
        }

        // HACK: allow file:/ URLs for reloading of sitemaps during development
        if (this.blockContextURL.startsWith("file:")) {
            return new URL("file", null, this.blockContextURL.substring("file:".length()) + path);
        } else {
            if (this.blockContextURL.length() != 0 && this.blockContextURL.charAt(0) != '/')
                throw new MalformedURLException("The blockContextURL must be empty or start with '/' "
                        + this.blockContextURL);
            
            // prefix the path with the block context resolve and resolve in the embeding
            // servlet context
            return super.getResource(this.blockContextURL + path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        // We better don't assume that blocks are unpacked
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    // FIXME, this should be defined in the config instead
    public String getInitParameter(String name) {
        if (this.properties == null)
            return null;
        String value = (String) this.properties.get(name);
        // Ask the super block for the property
        if (value == null) {
            ServletContext superContext = this.getNamedContext(SUPER);
            if (superContext != null)
                value = superContext.getInitParameter(name);
        }
        // Ask the parent context
        if (value == null) {
            super.getInitParameter(name);
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        Vector names = new Vector();
        
        // add all names of the parent servlet context
        Enumeration enumeration = super.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            names.add(enumeration.nextElement());
        }
        
        // add names of the super block
        ServletContext superContext = this.getNamedContext(SUPER);
        if (superContext != null) {
            enumeration = superContext.getInitParameterNames();
            while (enumeration.hasMoreElements()) {
                names.add(enumeration.nextElement());
            }
        }

        // add property names of this block
        if (this.properties != null) {
            names.addAll(this.properties.keySet());
        }

        return names.elements();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path) {
        try {
            return this.getResource(path).openStream();
        } catch (IOException e) {
            // FIXME Error handling
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    public ServletContext getContext(String uripath) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
    public int getMajorVersion() {
        return 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    public int getMinorVersion() {
        return 3;
    }
    
    private Collection getDirectoryList(File file, String pathPrefix) {
        ArrayList filenames = new ArrayList();

        if (!file.isDirectory()) {
            filenames.add("/" + file.toString().substring(pathPrefix.length()-1));
            return filenames;
        }

        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File subfile = files[i];
            filenames.addAll(getDirectoryList(subfile, pathPrefix));
        }

        return filenames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    public Set getResourcePaths(String path) {
        String pathPrefix;
        if (this.blockContextURL.startsWith("file:")) {
            pathPrefix = this.blockContextURL.substring("file:".length());
        } else {
            pathPrefix = this.blockContextURL;
        }
        
        path = pathPrefix + path;
        
        if (path == null) {
            return Collections.EMPTY_SET;
        }

        File file = new File(path);

        if (!file.exists()) {
            return Collections.EMPTY_SET;
        }

        HashSet set = new HashSet();
        set.addAll(getDirectoryList(file, pathPrefix));

        return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        PathDispatcher dispatcher = new PathDispatcher(path);
        return dispatcher.exists() ? dispatcher : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        NamedDispatcher dispatcher = new NamedDispatcher(name);
        return dispatcher.exists() ? dispatcher : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    // Block specific methods
    
    /**
     * Set the servlet of the block
     * @param servlet
     */
    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Takes the scheme specific part of a block URI (the scheme is the
     * responsibilty of the BlockSource) and resolve it with respect to the
     * blocks mount point.
     */
    public URI absolutizeURI(URI uri) throws URISyntaxException {
        String blockName = uri.getScheme();
        BlockContext blockContext;
        if (blockName == null) {
            // this block
            blockContext = this;
        } else {
            // another block
            blockContext = (BlockContext) this.getNamedContext(blockName);
            if (blockContext == null)
                throw new URISyntaxException(uri.toString(), "Unknown block name");
        }

        String mountPath = blockContext.getMountPath();
        if (mountPath == null)
            throw new URISyntaxException(uri.toString(),
                    "No mount point for this URI");
        if (mountPath.endsWith("/"))
            mountPath = mountPath.substring(0, mountPath.length() - 1);
        String absoluteURI = mountPath + uri.getSchemeSpecificPart();
        log("Resolving " + uri.toString() + " to " + absoluteURI);
        return new URI(absoluteURI);
    }
    
    /**
     * Get the context of a block with a given name.
     */
    // FIXME implement NPE handling
    public ServletContext getNamedContext(String name) {
        if (this.connections == null) {
            return null;
        }
        
        BlockServlet blockServlet =
            (BlockServlet) this.connections.get(name);
        return blockServlet != null ? blockServlet.getBlockContext() : null;
    }
        
    /**
     * @param mountPath The mountPath to set.
     */
    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    /**
     * Get the mount path of the block context
     */
    public String getMountPath() {
        return this.mountPath;
    }
    
    /**
     * @param blockContextURL the blockContextURL to set
     */
    public void setBlockContextURL(String blockContextURL) {
        this.blockContextURL = blockContextURL;
    }

    /**
     * @param properties The properties to set.
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * @param connections the connections to set
     */
    public void setConnections(Map connections) {
        this.connections = connections;
    }

    protected class NamedDispatcher implements RequestDispatcher {

        private String blockName;
        private boolean superCall = false;
        private ServletContext context;

        public NamedDispatcher(String blockName) {
            this.blockName = blockName;
            this.superCall = SUPER.equals(this.blockName);

            // Call to a named block that exists in the current context
            this.context = BlockContext.this.getNamedContext(this.blockName);
            if (this.context == null) {
                // If there is a super block, the connection might
                // be defined there instead.
                BlockContext superContext =
                    (BlockContext) BlockContext.this.getNamedContext(SUPER);
                if (superContext != null) {
                    this.context = superContext.getNamedContext(this.blockName);
                    this.superCall = true;
                }
            }
        }

        protected boolean exists() {
            return this.context != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest,
         *      javax.servlet.ServletResponse)
         */
        public void forward(ServletRequest request, ServletResponse response)
                throws ServletException, IOException {
            // Call to named block

            BlockContext.this.log("Enter processing in block " + this.blockName);
            RequestDispatcher dispatcher =
                this.context.getRequestDispatcher(((HttpServletRequest)request).getPathInfo());
            if (dispatcher != null && dispatcher instanceof PathDispatcher) {
                ((PathDispatcher)dispatcher).forward(request, response, this.superCall);
            } else {
                // Cannot happen
                throw new IllegalStateException();
            }
            BlockContext.this.log("Leaving processing in block " + this.blockName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest,
         *      javax.servlet.ServletResponse)
         */
        public void include(ServletRequest request, ServletResponse response)
                throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     *  Limited functionality, assumes that there is at most one servlet in the block
     */
    private class PathDispatcher implements RequestDispatcher {
        
        // Ignores path, as the assumed only servlet within the block is
        // implicitly mounted on '/*'
        private PathDispatcher(String path) {
        }

        private boolean exists() {
            return BlockContext.this.servlet != null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
         */
        public void forward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {
            this.forward(request, response, false);
        }

        protected void forward(ServletRequest request, ServletResponse response, boolean superCall)
        throws ServletException, IOException {
            try {
                if (!superCall) {
                    // It is important to set the current block each time
                    // a new block is entered, this is used for the block
                    // protocol
                    BlockCallStack.enterBlock(BlockContext.this);
                } else {
                    // A super block should be called in the context of
                    // the called block to get polymorphic calls resolved
                    // in the right way. We still need to register the
                    // current context for resolving super calls relative it.
                    BlockCallStack.enterSuperBlock(BlockContext.this);
                }                        
                BlockContext.this.servlet.service(request, response);
            } finally {
                BlockCallStack.leaveBlock();
            }
        }

        /* (non-Javadoc)
         * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
         */
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }
}
