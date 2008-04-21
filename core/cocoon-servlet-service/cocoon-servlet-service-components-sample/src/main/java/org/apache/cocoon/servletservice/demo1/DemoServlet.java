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
package org.apache.cocoon.servletservice.demo1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servletservice.RelativeServletConnection;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @version $Id$
 */
public class DemoServlet extends HttpServlet {

    BeanFactory beanFactory;
    SourceResolver resolver;

    public void init() throws ServletException {
        this.beanFactory =
            WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
        this.resolver = (SourceResolver) this.beanFactory.getBean(SourceResolver.ROLE);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

        request.setAttribute("foo", "bar");

        if ("/test1".equals(path)) {
            response.setContentType("text/plain");
            String attr = this.getInitParameter("foo");
            PrintWriter writer = response.getWriter();
            writer.println("Demo1.1 " + attr);
            writer.close();

        } else if ("/test2".equals(path)) {
            RequestDispatcher demo2 = this.getServletContext().getNamedDispatcher("demo2");
            demo2.forward(request, response);

        } else if ("/test3".equals(path)) {
            Source source = this.resolver.resolveURI("servlet:/test1");
            InputStream is = source.getInputStream();

            response.setContentType("text/plain");
            OutputStream os = response.getOutputStream();

            copy(is, os);
            is.close();
            os.close();

        } else if ("/test4".equals(path)) {
            response.setContentType("text/xml");
            OutputStream os = response.getOutputStream();

            // write a session attribute
            request.getSession().setAttribute("attribute-from-calling-request", "42");

            os.write("<page>".getBytes());
            RequestDumb.dumb(request, os);

            // calling another resource
            os.write(("\n<sub-request name=\"demo2-test1\">").getBytes());
            Source source = this.resolver.resolveURI("servlet:demo2:/test1?xyz=5");
            InputStream is = source.getInputStream();
            copy(is, os);
            os.write(("</sub-request>").getBytes());
            is.close();

            os.write(("\n<sub-request name=\"demo2-test2\">").getBytes());
            source = this.resolver.resolveURI("servlet:demo2:/test2?xyz=5");
            is = source.getInputStream();
            copy(is, os);
            os.write(("</sub-request>").getBytes());
            is.close();

            // check if sub calls are hidden
            os.write(("\n<check-sub name=\"" + this.getClass().getName() + "\">").getBytes());
            String foo1 = "\n  <request-attribute name=\"foo1\"><value>"
                            + request.getAttribute("foo1") + "</value></request-attribute>";
            os.write(foo1.getBytes());
            String sessionAttribute = "\n  <session-attribute name=\"attribute-from-called-request\"><value>"
                            + request.getSession().getAttribute("attribute-from-called-request")
                            + "</value></session-attribute>";
            os.write(sessionAttribute.getBytes());
            os.write(("</check-sub>").getBytes());

            os.write("</page>".getBytes());

            os.close();

        } else if ("/test5".equals(path)) {
            RelativeServletConnection con = new RelativeServletConnection("demo2", "/test1", null);
            InputStream is = con.getInputStream();
            OutputStream os = response.getOutputStream();
            copy(is, os);
            is.close();
            os.close();

        } else if ("/test6".equals(path)) {
            RelativeServletConnection con = new RelativeServletConnection(null, "/test1", null);
            response.setContentType("text/plain");
            InputStream is = con.getInputStream();
            OutputStream os = response.getOutputStream();
            copy(is, os);
            is.close();
            os.close();

        } else if ("/test7".equals(path)) {
            Source source = this.resolver.resolveURI("servlet:org.apache.cocoon.servletservice.demo1.servlet+:/test4");
            InputStream is = source.getInputStream();
            OutputStream os = response.getOutputStream();
            response.setContentType("text/plain");

            copy(is, os);
            is.close();
            os.close();

        } else if ("/test8".equals(path)) {
            response.setContentType("text/plain");
            OutputStream os = response.getOutputStream();
            os.write(("\nForbidden").getBytes());
            response.setStatus(403);
            os.close();

        } else if ("/test9".equals(path)) {
            RelativeServletConnection con = new RelativeServletConnection(null, "/test8", null);
            response.setContentType("text/plain");
            InputStream is = con.getInputStream();
            OutputStream os = response.getOutputStream();
            response.setStatus(con.getResponseCode());
            copy(is, os);
            is.close();
            os.close();

        } else {
            throw new ServletException("Unknown path " + path);
        }
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        int bytesRead = 0;
        byte buffer[] = new byte[512];
        while ((bytesRead = is.read(buffer)) != -1)
            os.write(buffer, 0, bytesRead);
    }
}
