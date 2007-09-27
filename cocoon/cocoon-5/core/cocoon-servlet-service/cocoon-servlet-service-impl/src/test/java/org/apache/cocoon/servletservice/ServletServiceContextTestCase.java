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

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.cocoon.servletservice.util.BlockCallHttpServletRequest;
import org.apache.cocoon.servletservice.util.BlockCallHttpServletResponse;

/**
 * @version $Id$
 */
public class ServletServiceContextTestCase extends TestCase {
    
    private ServletServiceContext mainContext;
    private HttpServlet servletA;
    private HttpServlet servletB;
    private HttpServlet servletC;
    
    private ServletServiceContext servletAContext;
    private ServletServiceContext servletBContext;
    private ServletServiceContext servletCContext;
    
    BlockCallHttpServletRequest request;
    BlockCallHttpServletResponse response;
    
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.mainContext = new ServletServiceContext();
        
        request = new BlockCallHttpServletRequest(new URI("dummy"), null);
        response = new BlockCallHttpServletResponse();
        
        //creating ServletContexts
        servletAContext = new ServletServiceContext();
        servletBContext = new ServletServiceContext();
        servletCContext = new ServletServiceContext();
    }
    
    /**
     * Tests basic connection to the servlet.
     * 
     * @throws Exception if test fails
     */
    public void testBasicConnection() throws Exception {
        servletA = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletAContext); }

            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_OK);
            }
        };
        servletAContext.setServlet(servletA);
       
        setMainConnection(servletA, "servletA");
        
        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    /**
     * <p>Tests if explicit super call works and if ServletContexts are properly set up when super call is performed.</p>
     * 
     * <p>Servlets are connected that way:</p>
     * <pre>
     *    ServletB
     *       ^
     *       |
     *    ServletA
     * </pre>
     * @throws Exception if test fails
     */
    public void testExplicitSuperCall() throws Exception {
        servletA = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletAContext); }

            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                ServletContext context = CallStackHelper.getCurrentServletContext();
                RequestDispatcher dispatcher = context.getNamedDispatcher("super");
                dispatcher.forward(request, response);
            }

        };
        
        servletB = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletBContext); }
            
            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                assertEquals(servletAContext, CallStackHelper.getBaseServletContext());
                assertEquals(servletBContext, CallStackHelper.getCurrentServletContext());
                response.setStatus(HttpServletResponse.SC_OK);
            }
        };
        
        servletAContext.setServlet(servletA);
        servletBContext.setServlet(servletB);
        
        //connecting servlets
        setMainConnection(servletA, "servletA");
        connectServlets(servletA, servletB, "super");

        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    /**
     * <p>Tests if ServletContext is properly set up in a servlet (C) that is called from super servlet (B) for another one(A).</p>
     * 
     * <p>Servlets are connected that way:</p>
     * <pre>
     *    ServletB --> ServletC
     *       ^
     *       |
     *    ServletA
     * </pre>
     * 
     * @throws Exception if test fails
     */
    public void testContextInServletCalledFromExplicitSuperCall() throws Exception {
        servletA = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletAContext); }

            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                ServletContext context = CallStackHelper.getCurrentServletContext();
                RequestDispatcher dispatcher = context.getNamedDispatcher("super");
                dispatcher.forward(request, response);
            }
        };
        
        servletB = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletBContext); }
            
            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                ServletContext context = CallStackHelper.getBaseServletContext();
                RequestDispatcher dispatcher = context.getNamedDispatcher("servletC");
                dispatcher.forward(request, response);
            }
        };
        
        servletC = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletCContext); }
            
            protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                super.service(req, response);
                assertEquals(servletCContext, CallStackHelper.getBaseServletContext());
                assertEquals(servletCContext, CallStackHelper.getCurrentServletContext());
                res.setStatus(200);
            }
        };
        servletAContext.setServlet(servletA);
        servletBContext.setServlet(servletB);
        servletCContext.setServlet(servletC);
        
        //connecting servlets
        setMainConnection(servletA, "servletA");
        connectServlets(servletA, servletB, "super");
        connectServlets(servletB, servletC, "servletC");

        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        assertEquals(200, response.getStatus());
    }
    
    /**
     * <p>This test is very similar to the {@link #testContextInServletCalledFromExplicitSuperCall()} but tries to use true OO approach so there is
     *    no explicit super call. See COCOON-2038.</p>
     * 
     * <p>Servlets are connected that way:</p>
     * <pre>
     *    ServletB --> ServletC
     *       ^
     *       |
     *    ServletA
     * </pre>
     * 
     * @throws Exception if test fails
     */
    public void testTrueObjectOrientedBehaviour() throws Exception {
        servletA = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletAContext); }

            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                super.service(request, response);
                response.setStatus(500);
            }

        };
        
        servletB = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletBContext); }
            
            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                ServletContext context = CallStackHelper.getBaseServletContext();
                RequestDispatcher dispatcher = context.getNamedDispatcher("servletC");
                dispatcher.forward(request, response);
            }
        };
        
        servletC = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletCContext); }
            
            protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                super.service(req, response);
                assertEquals(servletCContext, CallStackHelper.getBaseServletContext());
                assertEquals(servletCContext, CallStackHelper.getCurrentServletContext());
                res.setStatus(200);
            }
        };
        servletAContext.setServlet(servletA);
        servletBContext.setServlet(servletB);
        servletCContext.setServlet(servletC);
        
        //connecting servlets
        setMainConnection(servletA, "servletA");
        connectServlets(servletA, servletB, "super");
        connectServlets(servletB, servletC, "servletC");

        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        assertEquals(200, response.getStatus());
    }
    
    /**
     * <p>This test is for bug COCOON-1939 for more than 2 level of inheritance.
     *        
     * <p>Servlets are connected that way:</p>
     * <pre>
     * 	  SerlvetC
     * 		 ^
     * 		 |
     *    ServletB
     *       ^
     *       |
     *    ServletA
     * </pre>
     * 
     * @throws Exception if test fails
     */
    public void testThreeLevelInheritance() throws Exception {
        servletA = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletAContext); }

            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                super.service(request, response);
                response.setStatus(500);
            }

        };
        
        servletB = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletBContext); }
            
            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            	super.service(request, response);
                response.setStatus(500);
            }
        };
        
        servletC = new HttpServlet() {
            public ServletConfig getServletConfig() { return new ServletConfigWithContext(servletCContext); }
            
            protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                super.service(req, response);
                assertEquals(servletAContext, CallStackHelper.getBaseServletContext());
                assertEquals(servletCContext, CallStackHelper.getCurrentServletContext());
                res.setStatus(200);
            }
        };
        servletAContext.setServlet(servletA);
        servletBContext.setServlet(servletB);
        servletCContext.setServlet(servletC);
        
        //connecting servlets
        setMainConnection(servletA, "servletA");
        connectServlets(servletA, servletB, "super");
        connectServlets(servletB, servletC, "super");

        RequestDispatcher dispatcher = mainContext.getNamedDispatcher("servletA");
        dispatcher.forward(request, response);
        assertEquals(200, response.getStatus());
    }    
    
    //-----------------------------------------------------------------------------------------
    
    private void connectServlets(HttpServlet connectFrom, HttpServlet connectTo, String connectionName) {
        ServletServiceContext context = (ServletServiceContext)connectFrom.getServletConfig().getServletContext();
        Map connections = new HashMap();
        connections.put(connectionName, connectTo);
        context.setConnections(connections);
    }
    
    private void setMainConnection(HttpServlet connectTo, String connectionName) {
        Map connections = new HashMap();
        connections.put(connectionName, connectTo);
        mainContext.setConnections(connections);
    }
    
    private class ServletConfigWithContext implements ServletConfig {
        private ServletContext context;
        
        public ServletConfigWithContext(ServletContext context) {
            this.context = context;
        }
        
        public String getInitParameter(String arg0) { return null; }
        public Enumeration getInitParameterNames() { return null; }
        public String getServletName() { return null; }
        
        public ServletContext getServletContext() {
            return context;
        }
    }

}
