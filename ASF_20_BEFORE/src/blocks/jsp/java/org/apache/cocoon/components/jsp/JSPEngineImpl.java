/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: JSPEngineImpl.java,v 1.10 2004/01/29 10:34:13 joerg Exp $
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
