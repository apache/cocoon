/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Allows a Servlet or JSP to be used as a generator.
 * 
 * <p>
 * This implementation includes the servlet response using the 
 * RequestDispatcher from ServletContext.getNamedDispatcher().
 * </p>
 *
 * @version $Id$
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
        
        byte[] bytes;
        
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

