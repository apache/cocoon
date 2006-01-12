/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.cocoon.blocks.util.ServletContextWrapper;

/**
 * @version $Id$
 */
public class BlockContext extends ServletContextWrapper {

    private Hashtable attributes;

    private BlockWiring wiring;

    private Blocks blocks;

    public BlockContext(ServletContext parentContext, BlockWiring wiring,
            Blocks blocks) throws ServletException, MalformedURLException {
        super(parentContext);
        this.wiring = wiring;
        this.blocks = blocks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
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
        // A path starting with '/' should be resolved relative to the context
        // and the '/' need to be removed to work with the URI resolver.
        while (path.length() >= 1 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        String location = this.wiring.getLocation();
        if (location.length() > 0 && location.charAt(0) != '/') {
            location = "/" + location;
        }
        return super.servletContext.getResource(location + path);
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
        String value = this.wiring.getProperty(name);
        // Ask the super block for the property
        if (value == null) {
            String superId = this.wiring.getBlockId(Block.SUPER);
            // this.getLogger().debug("Try super property=" + name + " block=" +
            // superId);
            Block block = this.blocks.getBlock(superId);
            if (block != null) {
                // FIXME Should be taken from the ServletConfig rather than the
                // ServletContext
                value = block.getBlockServlet().getServletConfig()
                        .getServletContext().getInitParameter(name);
            }
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
        // TODO Auto-generated method stub
        return null;
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    public Set getResourcePaths(String arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
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
     * Takes the scheme specific part of a block URI (the scheme is the
     * responsibilty of the BlockSource) and resolve it with respect to the
     * blocks mount point.
     */
    public URI absolutizeURI(URI uri) throws URISyntaxException {
        String blockName = uri.getScheme();
        String blockId = null;
        if (blockName == null) {
            // this block
            blockId = this.wiring.getId();
        } else {
            // another block
            blockId = this.wiring.getBlockId(blockName);
        }
        Block block = this.blocks.getBlock(blockId);
        if (block == null)
            throw new URISyntaxException(uri.toString(), "Unknown block name");

        String mountPath = block.getMountPath();
        if (mountPath == null)
            throw new URISyntaxException(uri.toString(),
                    "No mount point for this URI");
        if (mountPath.endsWith("/"))
            mountPath = mountPath.substring(0, mountPath.length() - 1);
        String absoluteURI = mountPath + uri.getSchemeSpecificPart();
        log("Resolving " + uri.toString() + " to " + absoluteURI);
        return new URI(absoluteURI);
    }

    private class NamedDispatcher implements RequestDispatcher {

        private String blockName;

        private String blockId;

        private boolean superCall = false;

        private Servlet servlet;

        private RequestDispatcher dispatcher;

        private NamedDispatcher(String blockName) {
            this.blockName = blockName;
            this.blockId = BlockContext.this.wiring.getBlockId(this.blockName);

            this.superCall = Block.SUPER.equals(this.blockName);

            if (blockId != null) {
                // Call to a named block that exists in the current context
                Block block = BlockContext.this.blocks.getBlock(blockId);
                if (block != null)
                    this.servlet = block.getBlockServlet();
            } else {
                // If there is a super block, the connection might
                // be defined there instead.
                blockId = BlockContext.this.wiring.getBlockId(Block.SUPER);
                if (blockId != null) {
                    Block superBlock = BlockContext.this.blocks
                            .getBlock(blockId);
                    if (superBlock != null) {
                        Servlet superServlet = superBlock.getBlockServlet();
                        this.dispatcher = superServlet.getServletConfig()
                                .getServletContext().getNamedDispatcher(
                                        blockName);
                        this.superCall = true;
                    }
                }
            }
        }

        private boolean exists() {
            return this.servlet != null || this.dispatcher != null;
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

            BlockContext.this
                    .log("Enter processing in block " + this.blockName);
            if (this.servlet != null) {
                if (!this.superCall) {
                    try {
                        // It is important to set the current block each time
                        // a new block is entered, this is used for the block
                        // protocol
                        BlockCallStack.enterBlock(this.servlet);

                        this.servlet.service(request, response);
                    } finally {
                        BlockCallStack.leaveBlock();
                    }
                } else {
                    // A super block should be called in the context of
                    // the called block to get polymorphic calls resolved
                    // in the right way. Therefore no new current block is
                    // set.
                    this.servlet.service(request, response);
                }
            } else if (this.dispatcher != null) {
                this.dispatcher.forward(request, response);
            } else {
                // Cannot happen
                throw new IllegalStateException();
            }
            BlockContext.this.log("Leaving processing in block "
                    + this.blockName);
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
}
