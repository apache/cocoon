/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
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
 * @version CVS $Id: GarbageServlet.java,v 1.2 2003/06/24 16:59:28 cziegeler Exp $
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
