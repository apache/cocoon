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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Allows a Servlet or JSP to be used as a generator.
 * 
 * <p>
 * This implementation includes the servlet response using the 
 * RequestDispatcher from ServletContext.getNamedDispatcher().
 * </p>
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:bh22351@i-one.at">Bernhard Huber</a>
 * @version CVS $Id: JSPEngineImplNamedDispatcherInclude.java,v 1.7 2004/01/16 13:49:32 unico Exp $
 */
public class JSPEngineImplNamedDispatcherInclude extends AbstractLogEnabled
    implements JSPEngine, Parameterizable, ThreadSafe {

    /**
     * 'servlet-name' configuration parameter name for specifying 
     * the servlet name to dispatch to.
     */
    public static final String CONFIG_SERVLET_NAME = "servlet-name";
    
    /**
     * 'forward' configuration parameter name for specifying
     * whether or not the dispather should use forward 
     * instead of include.
     */
    public static final String CONFIG_FORWARD = "forward";
    
    /** 
     * Default value of CONFIG_SERVLET_NAME.
     * The value is <code>*.jsp</code>, 
     * this is the WLS JSP servlet default name.
     */
    public static final String DEFAULT_SERVLET_NAME = "*.jsp";
    
    /**
     * Default value of CONFIG_FORWARD.
     * The value is <code>false</code>.
     */
    public static final boolean DEFAULT_FORWARD = false;
    
    /** 
     * the configured name of the jsp servlet
     */
    private String servletName = DEFAULT_SERVLET_NAME;
    
    /**
     * Whether or not to use forward instead of include
     * when dispatching to the Servlet.
     */
    private boolean forward = DEFAULT_FORWARD;

    /**
     * <p>
     * The <code>forward</code> configuration parameter allows you to
     * control whether to use the forward dispatch method instead of 
     * the include method which is used by default.
     * </p>
     * <p>
     * Using the <code>servlet-name</code> configuration parameter
     * you can specify the name of the Servlet to dispatch to.
     * </p>
     */
    public void parameterize(Parameters params) {
        this.servletName = params.getParameter(CONFIG_SERVLET_NAME, DEFAULT_SERVLET_NAME);
        this.forward = params.getParameterAsBoolean(CONFIG_FORWARD, DEFAULT_FORWARD);
    }

    /**
     * Execute the Servlet and return the output.
     */
    public byte[] executeJSP(String url,
                             HttpServletRequest servletRequest,
                             HttpServletResponse servletResponse,
                             ServletContext servletContext)
        throws IOException, ServletException, Exception {
        
        JSPEngineServletOutputStream output = new JSPEngineServletOutputStream();
        JSPEngineServletRequest request = new JSPEngineServletRequest(servletRequest,url);
        JSPEngineServletResponse response = new JSPEngineServletResponse(servletResponse,output);
        
        byte[] bytes = null;
        
        // dispatch to the named servlet
        RequestDispatcher rd = servletContext.getNamedDispatcher(servletName);
        if (rd != null) {
            if (forward) {
                rd.forward(request,response);
            }
            else {
                rd.include(request,response);
            }
            response.flushBuffer();
            bytes = output.toByteArray();
        } else {
            throw new Exception("No RequestDispatcher found. Specify a correct '"
                                 + CONFIG_SERVLET_NAME + "': " + servletName);
        }
        return bytes;
    }
}

