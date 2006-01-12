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
        // TODO Auto-generated method stub
        return super.getRequestDispatcher(path);
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
            context = block.getBlockServlet().getServletConfig().getServletContext();

        return context;
    }
    


    private class NamedDispatcher implements RequestDispatcher {

        private Block block;
        
        public NamedDispatcher(String name) {
            this.block = BlocksContext.this.blocks.getBlock(name);
        }

        public void forward(ServletRequest request, ServletResponse response)
                throws ServletException, IOException {
            this.block.getBlockServlet().service(request, response);
        }

        public void include(ServletRequest request, ServletResponse response)
                throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }

        private boolean exists() {
            return this.block != null;
        }
    }
}
