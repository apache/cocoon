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

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.cocoon.kernel.CoreWirings;
import org.apache.cocoon.kernel.Installer;
import org.apache.cocoon.kernel.KernelDeployer;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationBuilder;

/**
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public class Servlet extends HttpServlet {

    private Logger logger = null;

    public void init()
    throws ServletException {

        /* Create a logger */
        ServletContext ctxt = this.getServletContext();
        String level = this.getInitParameter("log-level");
        String temp = this.getInitParameter("log-trace");
        boolean trace = ("true".equalsIgnoreCase(temp) ? true : false);
        if ("fatal".equalsIgnoreCase(level)) {
            this.logger = new ServletLogger(ServletLogger.FATAL, trace, ctxt);
        } else if ("error".equalsIgnoreCase(level)) {
            this.logger = new ServletLogger(ServletLogger.ERROR, trace, ctxt);
        } else if ("warn".equalsIgnoreCase(level)) {
            this.logger = new ServletLogger(ServletLogger.WARN,  trace, ctxt);
        } else if ("info".equalsIgnoreCase(level)) {
            this.logger = new ServletLogger(ServletLogger.INFO,  trace, ctxt);
        } else if ("debug".equalsIgnoreCase(level)) {
            this.logger = new ServletLogger(ServletLogger.DEBUG, trace, ctxt);
        } else {
            this.logger = new ServletLogger(ServletLogger.INFO,  trace, ctxt);
        }

        /* Find our configurations */
        String deplconf = this.getInitParameter("deployer-config");
        String instconf = this.getInitParameter("installer-config");
        if (deplconf == null) {
            String message = "Parameter \"deployer-config\" not specified";
            logger.fatal(message);
            throw new ServletException(message);
        } else if (instconf == null) {
            String message = "Parameter \"installer-config\" not specified";
            logger.fatal(message);
            throw new ServletException(message);
        }

        /* Let's start up */
        this.logger.info("Kernel startup");
        try {
            URL deplurl = ctxt.getResource(deplconf);
            if (deplurl == null) {
            		String message = "Unable to find deployer configurations \""
            					     + deplconf + "\"";
            		logger.fatal(message);
            		throw new ServletException(message);
            }
            
            URL insturl = ctxt.getResource(instconf);
            if (insturl == null) {
            		String message = "Unable to find installer configuration \""
            						 + deplconf + "\"";
            		logger.fatal(message);
            		throw new ServletException(message);
            }

            Configuration conf = null;

            /* Now let's create our core deployer */
            KernelDeployer deployer = new KernelDeployer();
            deployer.logger(logger);
            conf = ConfigurationBuilder.parse(deplurl);
            deployer.configure(conf);
            
            /* Instantiate an installer and process deployment */
            Installer installer = new Installer(deployer);
            conf = ConfigurationBuilder.parse(insturl);
            installer.process(conf);
            
            /* Store the current wirings as an application attribute */
            String attribute = Wirings.class.getName();
            ctxt.setAttribute(attribute, new CoreWirings(deployer));

        } catch (Throwable throwable) {
            String message = "An error occurred initializing the kernel";
            logger.fatal(message, throwable);
            throw new ServletException(message, throwable);
        }
    }

    public void destroy() {
        this.logger.info("Kernel shutdown");
    }
}
