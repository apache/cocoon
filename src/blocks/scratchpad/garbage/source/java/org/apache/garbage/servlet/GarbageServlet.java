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
package org.apache.garbage.servlet;

import java.net.URL;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.garbage.serializer.HTMLSerializer;
import org.apache.garbage.serializer.encoding.CharsetFactory;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: GarbageServlet.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class GarbageServlet extends HttpServlet {

    private ServletContext context = null;
    private ServletConfig config = null;
    private String charset = null;

    public void init()
    throws ServletException {
        this.context = this.getServletContext();
        this.config = this.getServletConfig();

        /** Preload all charsets and configure our default */
        this.charset = this.config.getInitParameter("charset");
        try {
            CharsetFactory factory = CharsetFactory.newInstance();
            this.charset = factory.getCharset(this.charset).getName();
        } catch (Throwable throwable) {
            throw new ServletException("Unable to set up default charset \""
                                       + this.charset + "\"", throwable);
        }

        this.log("GarbageServlet initialized (charset=" + this.charset + ")");
    }

    public void destroy() {
        this.log("GarbageServlet destroyed");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        this.doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        long time = System.currentTimeMillis();

        /* Figure out where the original template is */
        String resource = req.getServletPath();
        if (req.getPathInfo() != null) resource += req.getPathInfo();
        URL url = this.getServletContext().getResource(resource);
        if (url == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to locate template \""
                          + resource + "\" in application context.");
            return;
        }

        /* Start building up the serializer */
        HTMLSerializer serializer = new HTMLSerializer();
        ServletOutputStream out = res.getOutputStream();
        serializer.setOutput(out, this.charset);

        String content = serializer.getContentType();
        res.setContentType(serializer.getContentType());


        out.println("<HTML><BODY>&quot;" + url + "&quot;</BODY></HTML>");

        out.flush();
        this.log("Template \"" + resource + "\" written as \"" + content + "\""
                 + " in " + (((double)(System.currentTimeMillis()-time))/1000)
                 + " seconds.");
    }
}
