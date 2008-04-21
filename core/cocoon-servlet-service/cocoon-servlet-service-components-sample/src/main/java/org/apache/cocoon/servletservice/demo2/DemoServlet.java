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
package org.apache.cocoon.servletservice.demo2;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servletservice.demo1.RequestDumb;

/**
 * @version $Id$
 */
public class DemoServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/xml");

        String path = request.getPathInfo();

        request.setAttribute("foo", "bar");

        if ("/test1".equals(path)) {

            // set a request attribute
            request.setAttribute("foo1", "bar1");

            // set a session attribute
            request.getSession().setAttribute("attribute-from-called-request", "84");

            OutputStream os = response.getOutputStream();
            os.write("<page>".getBytes());
            RequestDumb.dumb(request, os);
            os.write("</page>".getBytes());

            os.close();

        } else if("/test2".equals(path)) {
            OutputStream os = response.getOutputStream();
            os.write("<page>".getBytes());
            RequestDumb.dumb(request, os);
            os.write("</page>".getBytes());
            os.close();
        } else {
            throw new ServletException("Unknown path " + path);
        }
    }

}
