/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.servlet;

import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.SAXException;

import org.apache.avalon.ConfigurationException;
import org.apache.avalon.ComponentNotAccessibleException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Notifier;
import org.apache.cocoon.Notification;
import org.apache.cocoon.environment.http.HttpEnvironment;


/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @version CVS $Revision: 1.1.4.18 $ $Date: 2000-08-31 16:00:53 $
 */
 
public class CocoonServlet extends HttpServlet {

    private Cocoon         cocoon            = null;
    private long           creationTime      = 0;
    private String         configurationFile = null;
    private Exception      exception         = null;
    private ServletContext context           = null;

    /**
     * Initialize this <code>CocoonServlet</code> instance.
     */
    public void init(ServletConfig conf) throws ServletException {

        super.init(conf);

        this.context = conf.getServletContext();

        String configFile = conf.getInitParameter("configurations");

        this.context.log("this.configurationFile: " + configFile);

        if (configFile == null) {
            ServletException fatalException =
                new ServletException("Servlet initialization argument "
                                     + "'configurations' not specified");
            Notification n = new Notification(this, fatalException);
            n.setType("cocoon-init-error");
            n.setTitle("Cocoon error upon init.");
            // FIXME (SM) We should use Servlet log channels for this
            // since otherwise it might get lost or end up being in bad
            // places if we are run under server frameworks such as avalon
            // since our servlet container might hook to Avalon own loggin
            // methods.
            Notifier.notify(n, System.out);

            throw fatalException;
        }

        try {
            this.configurationFile =
                this.context.getResource(configFile).getFile();

            this.context.log("this.configurationFile: "
                             + this.configurationFile);
        } catch (java.net.MalformedURLException mue) {
            ServletException fatalException =
                new ServletException("Servlet initialization argument "
                                     + "'configurations' not found at "
                                     + this.configurationFile);
            Notification n = new Notification(this, fatalException);
            n.setType("cocoon-init-error");
            n.setTitle("Cocoon error upon init.");
            n.addExtraDescription("requested-configuration-file",
                                  this.configurationFile);
            // FIXME (SM) see above
            Notifier.notify(n, System.out);

            throw fatalException;
        }

        this.cocoon = this.create();

        System.setProperty(Cocoon.TEMPDIR_PROPERTY,
                           ((File) this.context
                               .getAttribute("javax.servlet.context.tempdir"))
                                   .toString());
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long start = new Date().getTime();
        long end   = 0;

        // Reload cocoon if configuration changed or we are reloading
        boolean reloaded = false;

        synchronized (this) {
            if (this.cocoon != null) {
                if (this.cocoon.modifiedSince(this.creationTime)) {
                    this.context.log("Configuration changed reload attempt");

                    this.cocoon = this.create();
                    reloaded    = true;
                } else if ((req.getPathInfo() == null)
                           && (req.getParameter(Cocoon.RELOAD_PARAM)
                               != null)) {
                    this.context.log("Forced reload attempt");

                    this.cocoon = this.create();
                    reloaded    = true;
                }
            } else if ((req.getPathInfo() == null)
                       && (req.getParameter(Cocoon.RELOAD_PARAM) != null)) {
                this.context.log("Invalid configurations reload");

                this.cocoon = this.create();
                reloaded    = true;
            }
        }

        // Check if cocoon was initialized
        if (this.cocoon == null) {
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);

            Notification n = new Notification(this);
            n.setType("internal-servlet-error");
            n.setTitle("Internal servlet error");
            n.setSource("Cocoon servlet");
            n.setMessage("Internal servlet error");
            n.setDescription("Cocoon was not initialized.");
            n.addExtraDescription("request-uri", req.getRequestURI());
            Notifier.notify(n, req, res);

            return;
        }

        // We got it... Process the request
        // We should use getRequestURI(), minus the Context path otherwise
        // we break compatability with Tomcat and other Servlet 2.2 engines
        // (like Gefion LWS and Orion)
        //-----> FIXME (SM) Check what this means now that we support only Servlet 2.2 and above!
        String uri =
            req.getRequestURI().substring(req.getContextPath().length());

        if (!uri.equals("")) {
            try {
                if (uri.charAt(0) == '/') {
                    uri = uri.substring(1);
                }

                HttpEnvironment env = new HttpEnvironment(uri, req, res,
                                                          context);

                if (!this.cocoon.process(env)) {

                    //-----> FIXME (NKB) It is not true that!this.cocoon.process(env) means only SC_NOT_FOUND!
                    res.setStatus(res.SC_NOT_FOUND);

                    Notification n = new Notification(this);
                    n.setType("resource-not-found");
                    n.setTitle("Resource not found");
                    n.setSource("Cocoon servlet");
                    n.setMessage("Resource not found");
                    n.setDescription("The requested URI \""
                                     + req.getRequestURI()
                                     + "\" was not found.");
                    n.addExtraDescription("request-uri", req.getRequestURI());
                    n.addExtraDescription("path-info", uri);
                    Notifier.notify(n, req, res);
                }
            } catch (Exception e) {

                //res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
                Notification n = new Notification(this, e);
                n.setType("internal-server-error");
                n.setTitle("Internal server error");
                n.setSource("Cocoon servlet");
                n.addExtraDescription("request-uri", req.getRequestURI());
                n.addExtraDescription("path-info", uri);
                Notifier.notify(n, req, res);
            }
        } else {
            Notification n = new Notification(this);
            n.setType("information");
            n.setTitle(Cocoon.VERSION + "  :)");
            n.setSource("Cocoon servlet");
            n.setMessage("Ready to process requests...");

            String OkDescription = "Ready to process requests...";

            if (reloaded) {
                OkDescription += "configurations reloaded.";
            }

            n.setDescription(OkDescription);
            n.addExtraDescription("request-uri", req.getRequestURI());
            n.addExtraDescription("path-info", uri);
            Notifier.notify(n, req, res);
        }

        ServletOutputStream out = res.getOutputStream();

        end = new Date().getTime();

        String showTime = req.getParameter(Cocoon.SHOWTIME_PARAM);

        if ((showTime != null) &&!showTime.equalsIgnoreCase("no")) {
            float time   = (float) (end - start);
            float second = (float) 1000;
            float minute = (float) 60 * second;
            float hour   = (float) 60 * minute;

            if (showTime.equalsIgnoreCase("hide")) {
                out.print("<!-- ");
            } else {
                out.print("<p>");
            }

            out.print("Processed by Cocoon " + Cocoon.VERSION + " in ");

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

            if (showTime.equalsIgnoreCase("hide")) {
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
            this.context.log("Reloading from: " + this.configurationFile);

            Cocoon c = new Cocoon(this.configurationFile);

            this.creationTime = System.currentTimeMillis();

            return c;
        } catch (Exception e) {
            this.context.log("Exception reloading: " + e.getMessage());

            this.exception = e;
        }

        return (null);
    }
}

