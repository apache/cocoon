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
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class RequestDumb {

    public static void dumb(HttpServletRequest request, OutputStream os) throws IOException {
        // Request parameter
        os.write(("\n<request-parameters>").getBytes());
        Enumeration requestParamNames = request.getParameterNames();
        while(requestParamNames.hasMoreElements()) {
            String name = (String) requestParamNames.nextElement();
            String parameter = "\n  <parameter name=\"" + name  + "\"><value>" + request.getParameter(name) + "</value></parameter>";
            os.write(parameter.getBytes());
        }
        os.write(("\n</request-parameters>").getBytes());

        // Header parameter
        os.write(("\n\n<header-parameters>").getBytes());
        os.write(("\n******************************************************************").getBytes());
        Enumeration headers = request.getHeaderNames();
        while(headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            String parameter = "\n  <parameter name=\"" + name  + "\"><value>" + request.getHeader(name) + "</value></parameter>";
            os.write(parameter.getBytes());
        }
        os.write(("\n</header-parameters>").getBytes());

        // Request attributes
        os.write(("\n<request-attributes>").getBytes());
        Enumeration requestAttributes = request.getAttributeNames();
        while(requestAttributes.hasMoreElements()) {
            String name = (String) requestAttributes.nextElement();
            String attribute = "\n  <attribute name=\"" + name  + "\"><value>" + request.getAttribute(name) + "</value></attribute>";
            os.write(attribute.getBytes());
        }
        os.write(("\n</request-attributes>").getBytes());

        // session attributes
        os.write(("\n<session-attributes>").getBytes());
        Enumeration sessionAttributes = request.getSession().getAttributeNames();
        while(sessionAttributes.hasMoreElements()) {
            String name = (String) sessionAttributes.nextElement();
            String parameter = "\n  <attribute name=\"" + name  + "\"><value>" + request.getSession().getAttribute(name) + "</value></attribute>";
            os.write(parameter.getBytes());
        }
        os.write(("\n</session-attributes>").getBytes());
    }

}
