/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.servletservice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

import org.apache.cocoon.servletservice.util.BlockCallHttpServletRequest;
import org.apache.cocoon.servletservice.util.BlockCallHttpServletResponse;
import org.easymock.MockControl;

public class ServletServiceContextTest extends TestCase {
    
    private ServletServiceContext mainContext;
    private Servlet servletA;
    private Servlet servletB;
    private MockControl servletAControl;
    private MockControl servletBControl;
    
    private MockControl servletAConfigControl;
    private ServletConfig servletAConfig;
    
    private ServletServiceContext servletAContext;
    
    
    protected void setUp() throws Exception {
        super.setUp();
        this.mainContext = new ServletServiceContext();
        
        servletAControl = MockControl.createControl(Servlet.class);
        servletA = (Servlet)servletAControl.getMock();
        
        servletAContext = new ServletServiceContext();
        servletAContext.setServlet(servletA);
        
        servletAConfigControl = MockControl.createControl(ServletConfig.class);
        servletAConfig = (ServletConfig)servletAConfigControl.getMock();
        servletAConfig.getServletContext();
        servletAConfigControl.setReturnValue(servletAContext);
        
        servletA.getServletConfig();
        servletAControl.setReturnValue(servletAConfig);
        
        Map connections = new HashMap();
        connections.put("servletA", servletA);
        mainContext.setConnections(connections);
    }
    
    public void testBasicConnection() throws Exception {
        ServletRequest request = new BlockCallHttpServletRequest(new URI("dummy"), null);
        ServletResponse response = new BlockCallHttpServletResponse();
       
        servletA.service(request, response);
        servletAControl.replay();
        servletAConfigControl.replay();
        
        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        
        servletAControl.verify();
        servletAConfigControl.verify();
    }
}
