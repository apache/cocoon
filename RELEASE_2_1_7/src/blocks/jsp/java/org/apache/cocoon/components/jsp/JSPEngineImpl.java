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
package org.apache.cocoon.components.jsp;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Allows Servlets and JSPs to be used as a generator.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JSPEngineImpl.java,v 1.11 2004/03/05 13:01:57 bdelacretaz Exp $
 */
public class JSPEngineImpl extends AbstractLogEnabled
    implements JSPEngine, Parameterizable, ThreadSafe {

    /** The Default Servlet Class Name for Tomcat 3.X and 4.X */
    public static final String DEFAULT_SERVLET_CLASS = "org.apache.jasper.servlet.JspServlet";

    /** Servlet Class Name */
    public String jspServletClass = DEFAULT_SERVLET_CLASS;
    
    /**
     * @param params The configuration parameters
     */
    public void parameterize(Parameters params) {
        this.jspServletClass = params.getParameter("servlet-class", DEFAULT_SERVLET_CLASS);
    }
    
    /**
     * Execute the Servlet/JSP and return the output in UTF8 encoding.
     */
    public byte[] executeJSP(String url, 
                             HttpServletRequest servletRequest, 
                             HttpServletResponse servletResponse,
                             ServletContext context)
        throws IOException, ServletException, Exception {
        
        JSPEngineServletOutputStream output = new JSPEngineServletOutputStream();
        JSPEngineServletRequest request = new JSPEngineServletRequest(servletRequest, url);
        JSPEngineServletResponse response = new JSPEngineServletResponse(servletResponse,output);
        
        byte[] bytes = null;

        // start the servlet
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(this.jspServletClass);
        Servlet servlet = (Servlet) clazz.newInstance();
        servlet.init(new JSPEngineServletConfig(context,"JSPEngineImpl"));
        
        try {
            servlet.service(request, response);
            bytes = output.toByteArray();
        } finally {
            // clean up
            servlet.destroy();
        }
        
        return bytes;
    }
}
