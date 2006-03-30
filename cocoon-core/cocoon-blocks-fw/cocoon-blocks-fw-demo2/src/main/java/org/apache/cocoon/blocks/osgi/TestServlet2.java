/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version $Id$
 */
public class TestServlet2 extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        
        if ("/test1".equals(path)) {
            response.setContentType("text/plain");
            String attr = this.getInitParameter("attr");
            PrintWriter writer = response.getWriter();
            writer.println("Test! " + attr);
            writer.close();
        } else if ("/test2".equals(path)) {
            RequestDispatcher block1 = this.getServletContext().getNamedDispatcher("block1");
            block1.forward(request, response);
        } else if ("/test3".equals(path)) {
            URL url = new URL("block:/test1");
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            response.setContentType("text/plain");
            OutputStream os = response.getOutputStream();
            
            copy(is, os);
            is.close();
            os.close();
        } else if ("/test4".equals(path)) {
            URL url = new URL("block:block1:/any");
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            response.setContentType("text/plain");
            OutputStream os = response.getOutputStream();
            
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
