/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>,
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:42 $
 * @since Cocoon 2.0
 */
public class CocoonServlet extends HttpServlet {
    private Cocoon cocoon=null;
    private long creationTime=0;
    private ConfigurationException exception=null;
    private Configurations configurations=null;

    public void init(ServletConfig conf)
    throws ServletException {
        super.init(conf);
        String file=conf.getInitParameter("configurationFile");
        if (file==null)
            this.exception=new ConfigurationException("Servlet init argument "+
                                           "'configurationFile' not specified");
        this.configurations=new Configurations();
        this.configurations.setParameter("configurationFile",file);
        this.cocoon=create();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        // Reload cocoon if configuration changed or we are reloading
        if (this.cocoon!=null) {
            if (cocoon.modifiedSince(this.creationTime)) this.cocoon=create();
        } else if(req.getParameter("reload")!=null) this.cocoon=create();

        // Check if cocoon was initialized
        if (this.cocoon==null) {
            res.setContentType("text/html");
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            PrintStream out=new PrintStream(res.getOutputStream());
            out.println("<html><head>");
            out.println("<title>Cocoon: Not initialized</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Not initialized</h1></center>");
            out.println("<hr>");
            out.print("Attempt to <a href=\"");
            out.print(req.getScheme()+"://"+req.getServerName()+":");
            out.print(req.getServerPort()+req.getServletPath());
            out.print("?reload=true\">Reload</a>");
            out.println("<hr>");
            if(this.exception==null) {
                out.println("No additional informations.");
            } else {
                this.printException(out,this.exception);
                if(this.exception.getException()!=null) {
                    out.println("<hr>");
                    out.println("Nested Exception Data:");
                    this.printException(out,this.exception.getException());
                }
            }
            out.println("<hr></body></html>");
            out.flush();
            return;
        }
        // We got it... Process the request
        ServletOutputStream os=res.getOutputStream();
        String uri=req.getPathInfo();
        if (uri!=null) try {
            Job job=new ServletJob(req,res);
            if (!this.cocoon.handle(job,os)) res.sendError(res.SC_NOT_FOUND);
        } catch (IOException e) {
            throw(e);    
        } catch (SAXException e) {
            res.setContentType("text/html");
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            PrintStream out=new PrintStream(os);
            out.println("<html><head>");
            out.println("<title>Cocoon: Exception</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Exception</h1></center>");
            out.println("<hr>");
            this.printException(out,e);
            if(e.getException()!=null) {
                out.println("<hr>");
                out.println("Nested Exception Data:");
                this.printException(out,e.getException());
            }
            out.println("<hr></body></html>");
            out.flush();
        } else {
            res.setContentType("text/html");
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            PrintStream out=new PrintStream(os);
            out.println("<html><head>");
            out.println("<title>Cocoon: Version 2.0</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon: Version 2.0</h1></center>");
            out.println("<hr>");
            out.print("Ready to process requests...");
            out.println("<hr></body></html>");
            out.flush();
        }
        os.flush();
        os.close();
    }

    private Cocoon create() {
        try {
            Cocoon ccn=new Cocoon();
            ccn.configure(this.configurations);
            this.creationTime=System.currentTimeMillis();
            return(ccn);
        } catch (ConfigurationException e) {
            this.exception=e;
        } catch (Exception e) {
            this.exception=new ConfigurationException("Unknown Exception",e);
        }
        return(null);
    }

    private void printException(PrintStream out, Exception e) {
        out.println("<b>"+e.getClass().getName()+"</b><br>");
        out.println("Message: <i>"+e.getMessage()+"</i><br>");
        out.println("<pre>");
        e.printStackTrace(out);
        out.println("</pre>");
    }
}
