/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.arch.config.ConfigurationException;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.2 $ $Date: 2000-02-27 01:33:08 $
 */
public class CocoonServlet extends HttpServlet {
    private Cocoon cocoon=null;
    private long creationTime=0;
    private String configurationFile=null;
    private Exception exception=null;

    /**
     * Initialize this <code>CocoonServlet</code> instance.
     */
    public void init(ServletConfig conf)
    throws ServletException {
        super.init(conf);
        this.configurationFile=conf.getInitParameter("configurationFile");
        if (this.configurationFile==null) {
            throw new ServletException("Servlet initialization argument "+
                                       "'configurationFile' not specified");
        }
        this.cocoon=this.create();
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        // Reload cocoon if configuration changed or we are reloading
        synchronized (this) {
            if (this.cocoon!=null) {
                if (this.cocoon.modifiedSince(this.creationTime)) {
                    this.cocoon=this.create();
                }
            } else if ((req.getParameter("reload")!=null) &&
                       (req.getPathInfo()!=null)) {
                this.cocoon=this.create();
            }
        }

        ServletOutputStream out=res.getOutputStream();

        // Check if cocoon was initialized
        if (this.cocoon==null) {
            res.setContentType("text/html");
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            out.println("<html><head>");
            out.println("<title>Cocoon: Not initialized</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Not initialized</h1></center>");
            out.println("<hr>");
            out.print("Try to <a href=\"");
            out.print(req.getScheme()+"://"+req.getServerName()+":");
            out.print(req.getServerPort()+req.getServletPath());
            out.print("?reload=true\">Reload</a>");
            out.println("<hr>");
            this.printException(out,this.exception);
            if (exception instanceof SAXException) {
                Exception nested=((SAXException)this.exception).getException();
                out.println("<hr>");
                this.printException(out,nested);
            }
            out.println("<hr></body></html>");
            out.flush();
            return;
        }
        // We got it... Process the request
        String uri=req.getPathInfo();
        if (uri!=null) try {
            CocoonServletRequest request=new CocoonServletRequest(req,uri);
            CocoonServletResponse response=new CocoonServletResponse(res);
            if (!this.cocoon.process(request,response,out))
                res.sendError(res.SC_NOT_FOUND);
        } catch (Exception e) {
            res.setContentType("text/html");
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            out.println("<html><head>");
            out.println("<title>Cocoon: Exception</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Exception</h1></center>");
            out.println("<hr>");
            this.printException(out,e);
            if (exception instanceof SAXException) {
                Exception nested=((SAXException)this.exception).getException();
                out.println("<hr>");
                this.printException(out,nested);
            }
            out.println("<hr></body></html>");
        } else {
            res.setContentType("text/html");
            out.println("<html><head>");
            out.println("<title>Cocoon: Version 2.0</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Version 2.0</h1></center>");
            out.println("<hr>");
            out.print("Ready to process requests...");
            out.println("<hr></body></html>");
        }
        out.flush();
    }

    /** Create a new <code>Cocoon</code> object. */
    private Cocoon create() {
        try {
            Cocoon c=new Cocoon(this.configurationFile);
            this.creationTime=System.currentTimeMillis();
            return(c);
        } catch (Exception e) {
            this.exception=e;
        }
        return(null);
    }

    /** Dump an exception to the specified <code>ServletOutputStream</code> */
    private void printException(ServletOutputStream o, Exception e) {
        PrintWriter out=new PrintWriter(o);
        out.println("<b>"+e.getClass().getName()+"</b><br>");
        out.println("Message: <i>"+e.getMessage()+"</i><br>");
        out.println("<pre>");
        e.printStackTrace(out);
        out.println("</pre>");
        out.flush();
    }
}
