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
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;

/**
 * @version $Id$
 */
public class BlockServlet extends HttpServlet {
    private BlockContext blockContext;
    private String blockServletClass;
    private Servlet blockServlet;

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.blockContext.setServletContext(servletConfig.getServletContext());
        ServletConfig blockServletConfig =
            new ServletConfigurationWrapper(servletConfig, this.blockContext) {

                // FIXME: The context should get the init parameters from the
                // config rather than the oposite way around.
                public String getInitParameter(String name) {
                    return super.getServletContext().getInitParameter(name);
                }

                public Enumeration getInitParameterNames() {
                    return super.getServletContext().getInitParameterNames();
                }
            
        };
        try {
            this.blockServlet =
                (Servlet) this.getClass().getClassLoader().loadClass(this.blockServletClass).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.blockServlet.init(blockServletConfig);
        this.blockContext.setServlet(this.blockServlet);
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        this.blockServlet.destroy();
        super.destroy();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        RequestDispatcher dispatcher =
            this.blockContext.getRequestDispatcher(request.getPathInfo());
        try {
            BlockCallStack.enterBlock(this.blockContext);
            dispatcher.forward(request, response);
        } finally {
            BlockCallStack.leaveBlock();
        }
    }

    /**
     * @return the blockContext
     */
    public BlockContext getBlockContext() {
        return this.blockContext;
    }
    
    public BlockServlet() {
        this.blockContext = new BlockContext();
    }

    public void setProperties(Map properties) {
        this.blockContext.setProperties(properties);
    }
    
    public void setConnections(Map connections) {
        this.blockContext.setConnections(connections);
    }
    
    public void setMountPath(String mountPath) {
        this.blockContext.setMountPath(mountPath);        
    }
    
    public void setBlockServletClass(String blockServletClass) {
        this.blockServletClass = blockServletClass;
    }
}
