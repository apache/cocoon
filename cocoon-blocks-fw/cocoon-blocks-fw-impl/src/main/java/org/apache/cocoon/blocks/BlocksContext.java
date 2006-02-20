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
package org.apache.cocoon.blocks;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.blocks.util.ServletContextWrapper;

/**
 * @version $Id$
 */
public class BlocksContext extends ServletContextWrapper {

    Blocks blocks;

    /**
     * @param servletContext
     */
    public BlocksContext(ServletContext servletContext, Blocks blocks) {
        super(servletContext);
        this.blocks = blocks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.blocks.ServletContextWrapper#getNamedDispatcher(java.lang.String)
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        NamedDispatcher dispatcher = new NamedDispatcher(name); 
        return dispatcher.exists() ? dispatcher : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.blocks.ServletContextWrapper#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        PathDispatcher dispatcher = new PathDispatcher(path);
        return dispatcher.exists() ? dispatcher : null;
    }
    
    // BlocksContext specific method
    
    /**
     * Get the context of a block with a given name.
     */
    public ServletContext getNamedContext(String name) {
        Block block;
        ServletContext context = null;
        block = BlocksContext.this.blocks.getBlock(name);
        if (block != null)
            context = block.getBlockContext();

        return context;
    }
    


    private class NamedDispatcher implements RequestDispatcher {

        private Block block;
        
        public NamedDispatcher(String name) {
            this.block = BlocksContext.this.blocks.getBlock(name);
        }

        public void forward(ServletRequest request0, ServletResponse response)
                throws ServletException, IOException {
            HttpServletRequest request = (HttpServletRequest) request0;
            RequestDispatcher dispatcher =
                this.block.getBlockContext().getRequestDispatcher(request.getPathInfo());
            dispatcher.forward(request, response);
        }

        public void include(ServletRequest request, ServletResponse response)
                throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }

        private boolean exists() {
            return this.block != null;
        }
    }
    private class PathDispatcher implements RequestDispatcher {
        
        private String mountPath;
        private String path;
        private Block block;
        /**
         * @param path
         */
        private PathDispatcher(String path) {
            this.path = path;
            this.block= null;
        
            this.mountPath = this.path;
            int index = this.mountPath.length();
            while (this.block == null && index != -1) {
                this.mountPath = this.mountPath.substring(0, index);
                this.block = BlocksContext.this.blocks.getMountedBlock(this.mountPath);
                index = this.mountPath.lastIndexOf('/');
            }
        }
        
        private boolean exists() {
            return this.block != null;
        }

        public void forward(ServletRequest request0, ServletResponse response0) throws ServletException, IOException {
            HttpServletRequest request = (HttpServletRequest) request0;
            HttpServletResponse response = (HttpServletResponse) response0;
            // We got it... Process the request
            System.out.println("Service: contextPath=" + request.getContextPath() +
                               " servletPath=" + request.getServletPath() +
                               " pathInfo=" + request.getPathInfo() +
                               " requestURI=" + request.getRequestURI() +
                               " requestURL=" + request.getRequestURL());
        
            // This servlet is the context for the called block servlet
            String contextPath = trimPath(request.getContextPath());
            String servletPath = trimPath(request.getServletPath());
            final String newContextPath = contextPath + servletPath;
            
            // Resolve the URI relative to the mount point
            final String newServletPath = this.mountPath;
            final String newPathInfo = this.path.substring(newServletPath.length());
            
            HttpServletRequest newRequest = new HttpServletRequestWrapper(request) {
        
                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getContextPath()
                 */
                public String getContextPath() {
                    return newContextPath;
                }
                
                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
                 */
                public String getPathInfo() {
                    return newPathInfo;
                }
                
                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
                 */
                public String getServletPath() {
                    return newServletPath;
                }
                    
            };
            
            BlocksContext.this.log("Enter processing in block at " + newServletPath);
            this.block.service(newRequest, response);
            BlocksContext.this.log("Leaving processing in block at " + newServletPath);
        }

        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
        /**
         * Utility function to ensure that the parts of the request URI not is null
         * and not ends with /
         * @param path
         * @return the trimmed path
         */
        private String trimPath(String path) {
            if (path == null)
                    return "";
            int length = path.length();
            if (length > 0 && path.charAt(length - 1) == '/')
                    path = path.substring(0, length - 1);
            return path;
        }
    }
}
