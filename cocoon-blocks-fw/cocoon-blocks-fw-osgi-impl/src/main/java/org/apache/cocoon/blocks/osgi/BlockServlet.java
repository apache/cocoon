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
package org.apache.cocoon.blocks.osgi;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;
import org.osgi.service.component.ComponentContext;

/**
 * @version $Id$
 */
public class BlockServlet extends HttpServlet {
    private ComponentContext componentContext;
    private BlockContext blockContext;
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
        this.blockServlet.init(blockServletConfig);
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
        dispatcher.forward(request, response);
    }

    /**
     * @return the blockContext
     */
    public BlockContext getBlockContext() {
        return this.blockContext;
    }
    
    protected void activate(ComponentContext componentContext) {
        this.componentContext = componentContext;
        this.blockContext = new BlockContext();
        
        this.blockContext.setProperties(this.componentContext.getProperties());
        this.blockContext.setMountPath((String) this.componentContext.getProperties().get("path"));
        this.blockServlet =
            (Servlet) this.componentContext.locateService("blockServlet");
        this.blockContext.setServlet(this.blockServlet);
        this.blockContext.activate(this.componentContext);
    }
}
