/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.servlet;

import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.ConfigurationException;
import org.apache.avalon.ComponentNotAccessibleException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpEnvironment;

import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.15 $ $Date: 2000-07-29 18:30:38 $
 */
public class CocoonServlet extends HttpServlet {
    private Cocoon cocoon=null;
    private long creationTime=0;
    private String configurationFile=null;
    private Exception exception=null;
    private ServletContext context=null;

    /**
     * Initialize this <code>CocoonServlet</code> instance.
     */
    public void init(ServletConfig conf)
    throws ServletException {
        super.init(conf);
        this.context=conf.getServletContext();
        String configFile=conf.getInitParameter("configurations");
        this.context.log ("this.configurationFile: "+this.configurationFile);
        if (configFile==null) {
            throw new ServletException("Servlet initialization argument "+
                                       "'configurations' not specified");
        }
        try {
            this.configurationFile=this.context.getResource(configFile).getFile();
            this.context.log ("this.configurationFile: "+this.configurationFile);
        } catch (java.net.MalformedURLException mue) {
            throw new ServletException("Servlet initialization argument "+
                                       "'configurations' not found at "+this.configurationFile);
        }
        this.cocoon=this.create();
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        long start = new Date().getTime();
        long end = 0;
        // Reload cocoon if configuration changed or we are reloading
        boolean reloaded=false;
        synchronized (this) {
            if (this.cocoon!=null) {
                if (this.cocoon.modifiedSince(this.creationTime)) {
                    this.context.log("Configuration changed reload attempt");
                    this.cocoon=this.create();
                    reloaded=true;
                } else if ((req.getPathInfo()==null) &&
                           (req.getParameter("reload")!=null)) {
                    this.context.log("Forced reload attempt");
                    this.cocoon=this.create();
                    reloaded=true;
                }
            } else if ((req.getPathInfo()==null) &&
                       (req.getParameter("reload")!=null)) {
                this.context.log("Invalid configurations reload");
                this.cocoon=this.create();
                reloaded=true;
            }
        }

        ServletOutputStream out=res.getOutputStream();

        // Check if cocoon was initialized
        if (this.cocoon==null) {
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            res.setContentType("text/html");
            out.println("<html><head>");
            out.println("<title>Cocoon Version 2.0: Not initialized</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon 2.0: Not initialized</h1></center>");
            out.println("<hr>");
            out.print("Try to <a href=\"");
            out.print(req.getScheme()+"://"+req.getServerName()+":");
            out.print(req.getServerPort()+req.getServletPath());
            out.print("?reload=true\">Reload</a>");
            out.println("<!-- PATH_INFO=\""+req.getPathInfo()+"\" -->");
            out.println("<hr>");
            this.printException(out,this.exception);
            if (exception instanceof SAXException) {
                Exception nested=((SAXException)this.exception).getException();
                out.println("<hr>");
                this.printException(out,nested);
            } else if (exception instanceof ComponentNotAccessibleException) {
                out.println("Component not accessible<br>");
                Exception nested=this.exception;
                nested=((ComponentNotAccessibleException)nested).getException();
                out.println("<hr>");
                this.printException(out,nested);
            }
            out.println("<hr></body></html>");
            out.flush();
            return;
        }
        // We got it... Process the request
        // We should use getRequestURI(), minus the Context path otherwise
        // we break compatability with Tomcat and other Servlet 2.2 engines
        // (like Gefion LWS and Orion)
        String uri=req.getRequestURI().substring(req.getContextPath().length());
        if (!uri.equals("")) {
            try {
                if (uri.charAt(0)=='/') uri=uri.substring(1);
                HttpEnvironment env = new HttpEnvironment (uri, req, res, context);
                if (!this.cocoon.process(env,out)) {
                    res.setStatus(res.SC_NOT_FOUND);
                    res.setContentType("text/html");
                    out.println("<html><head>");
                    out.println("<title>Cocoon Version 2.0: Not Found</title>");
                    out.println("<body>");
                    out.println("<center><h1>Cocoon 2.0: Not Found</h1></center>");
                    out.println("<hr>");
                    out.print("The requested URI \""+req.getRequestURI());
                    out.print("\" was not found.");
                    out.println("<!-- PATH_INFO=\""+uri+"\" -->");
                    out.println("<hr></body></html>");
                }
            } catch (Exception e) {
                //res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
                res.setContentType("text/html");
                out.println("<html><head>");
                out.println("<title>Cocoon Version 2.0: Exception</title>");
                out.println("<body>");
                out.println("<center><h1>Cocoon 2.0: Exception</h1></center>");
                out.println("<hr>");
                this.printException(out,e);
                if (e instanceof SAXException) {
                    out.println("<hr>");
                    out.println("SAX processing exception<br>");
                    Exception nested=((SAXException)e).getException();
                    this.printException(out,nested);
                } else if (e instanceof ComponentNotAccessibleException) {
                    out.println("<hr>");
                    out.println("Component not accessible<br>");
                    Exception nested=e;
                    nested=((ComponentNotAccessibleException)nested).getException();
                    this.printException(out,nested);
                }

                out.println("<hr></body></html>");
            }
        } else {
            res.setContentType("text/html");
            out.println("<html><head>");
            out.println("<title>Cocoon Version 2.0</title>");
            out.println("<body>");
            out.println("<center><h1>Cocoon 2.0: Version 2.0</h1></center>");
            out.println("<hr>");
            if (reloaded) out.println("Configurations reloaded.<br>");
            out.println("Ready to process requests...");
            out.println("<!-- PATH_INFO=\""+uri+"\" -->");
            out.println("<hr></body></html>");
        }

        end = new Date().getTime();
        String showTime = req.getParameter("showtime");
        if ((showTime != null) && !showTime.equalsIgnoreCase("no")) {
            float time = (float) (end - start);
            float second = (float) 1000;
            float minute = (float) 60 * second;
            float hour = (float) 60 * minute;

            if (showTime.equalsIgnoreCase("hide")) {
                out.print("<!--");
            } else {
                out.print("<p>");
            }
            out.print("Processed by Cocoon in ");

            if (time > hour) {
                out.print(time / hour);
                out.print(" hours.");
            } else if (time > minute) {
                out.print(time / minute);
                out.print(" minutes.");
            } else if (time > second) {
                out.print(time / second);
                out.print(" seconds.");
            } else {
                out.print(time);
                out.print(" milliseconds.");
            }

            if (showTime == "hide") {
                out.print("-->");
            } else {
                out.print("</p>");
            }
        }

        out.flush();
    }

    /** Create a new <code>Cocoon</code> object. */
    private Cocoon create() {
        try {
            this.context.log("Reloading from: "+this.configurationFile);
            Cocoon c=new Cocoon(this.configurationFile);
            this.creationTime=System.currentTimeMillis();
            return(c);
        } catch (Exception e) {
            this.context.log("Exception reloading: "+e.getMessage());
            this.exception=e;
        }
        return(null);
    }

    /** Dump an exception to the specified <code>ServletOutputStream</code> */
    private void printException(ServletOutputStream o, Exception e) {
        PrintWriter out=new PrintWriter(o);
        out.println("Class: <b>"+e.getClass().getName()+"</b><br>");
        out.println("Message: <i>"+e.getMessage()+"</i><br>");
        out.println("<pre>");
        e.printStackTrace(out);
        out.println("</pre>");
        out.flush();
    }
}
