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
package org.apache.cocoon.kernel.startup;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.cocoon.kernel.composition.Wire;


/**
 * <p>.</p> 
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public class ServletWrapper implements Servlet {

    private KernelServlet kernel = null;
    private Servlet servlet = null;
    private boolean expose = false;

    public void init(ServletConfig config)
    throws ServletException {
        /* Get the deployed block name exposing the servlet */
        String inst = config.getInitParameter("org.apache.cocoon.kernel.block.name");
        if (inst == null) {
            throw new ServletException("Block instance not specified in \""
                    + "org.apache.cocoon.kernel.block\" servlet property");
        }
        
        /* Check if we have to expose the wirings in the request attributes */
        String expose = config.getInitParameter("org.apache.cocoon.kernel.wirings.expose");
        this.expose = "true".equalsIgnoreCase(expose);

        /* Get a hold on the kernel servlet, its wirings and logger */
        this.kernel = KernelServlet.instance;
        if (this.kernel == null) {
            throw new ServletException("Framework not initialized");
        }

        /* Get a hold on the wrapped servlet and initialize it */
        try {
            this.servlet = (Servlet) this.kernel.getWirings().lookup(Servlet.class, inst);
            this.servlet.init(config);
        } catch (ServletException e) {
            throw (e);
        } catch (Throwable t) {
            this.kernel.getLogger().fatal("Unable to access wrapped servlet", t);
            throw new ServletException("Unable to access wrapped servlet");
        }
    }

    public void destroy() {
        this.servlet.destroy();
        ((Wire)this.servlet).release();
    }

    public void service(ServletRequest request, ServletResponse response)
    throws ServletException, IOException {
        if (this.expose) {
            request.setAttribute("org.apache.cocoon.kernel.wirings.global",
                                 this.kernel.getWirings());
        }

        try {
            this.servlet.service(request, response);
        } catch (ServletException exception) {
            this.kernel.getLogger().error("Exception serviceing page", exception);
            throw(exception);
        } catch (java.io.IOException exception) {
            this.kernel.getLogger().error("Exception serviceing page", exception);
            throw(exception);
        } catch (RuntimeException exception) {
            this.kernel.getLogger().error("Exception serviceing page", exception);
            throw(exception);
        } catch (Throwable throwable) {
            this.kernel.getLogger().error("Exception serviceing page", throwable);
            throw(new ServletException("Whoha bessie"));
        }
    }

    public ServletConfig getServletConfig() {
        return(this.servlet.getServletConfig());
    }

    public String getServletInfo() {
        return(this.servlet.getServletInfo());
    }
}
