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

import java.net.MalformedURLException;
import java.net.URL;

import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xml.sax.SAXException;

import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.DefaultContext;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Notifier;
import org.apache.cocoon.Notification;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.http.RequestWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.components.classloader.RepositoryClassLoader;

import org.apache.log.Logger;
import org.apache.log.LogKit;
import org.apache.log.Priority;
import org.apache.log.Category;
import org.apache.log.output.FileOutputLogTarget;
import org.apache.log.output.ServletOutputLogTarget;
import org.apache.log.LogTarget;

/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.4.72 $ $Date: 2001-03-23 16:15:21 $
 */

public class CocoonServlet extends HttpServlet {

    protected Logger log;

    static final long second = 1000;
    static final long minute = 60 * second;
    static final long hour   = 60 * minute;

    protected long creationTime = 0;
    protected Cocoon cocoon;
    protected Exception exception;
    protected DefaultContext appContext = new DefaultContext();

    private static final boolean ALLOW_OVERWRITE = false;
    private static final boolean SILENTLY_RENAME = true;
    private File uploadDir;

    /**
     * Initialize this <code>CocoonServlet</code> instance.  You will
     * notice that I have broken the init into sub methods to make it
     * easier to maintain (BL).  The context is passed to a couple of
     * the subroutines.  This is also because it is better to explicitly
     * pass variables than implicitely.  It is both more maintainable,
     * and more elegant.
     *
     * @param conf The ServletConfig object from the servlet engine.
     *
     * @throws ServletException
     */
    public void init(ServletConfig conf)
    throws ServletException {

        super.init(conf);
        ClassLoader classloader = buildInitClassLoader();
        ServletContext context = conf.getServletContext();

        ClassUtils.setClassLoader(classloader);
        this.appContext.put(Constants.CONTEXT_CLASS_LOADER, classloader);

        this.appContext.put(Constants.CONTEXT_SERVLET_CONTEXT, context);

        this.initLogger(context);

        this.appContext.put(Constants.CONTEXT_CLASSPATH, this.getClassPath(context));

        this.forceLoad(conf.getInitParameter("load-class"));

        File workDir = (File) context.getAttribute("javax.servlet.context.tempdir");
        this.appContext.put(Constants.CONTEXT_WORK_DIR, workDir);
        this.uploadDir = IOUtils.createFile(workDir, "image-dir" + File.separator);
        this.uploadDir.mkdirs();

        this.appContext.put(Constants.CONTEXT_CONFIG_URL,
                this.getConfigFile(conf.getInitParameter("configurations"), context));

        this.appContext.put(Constants.CONTEXT_ROOT_PATH, context.getRealPath("/"));

        this.createCocoon();
    }

    /**
     * get the classloader to use for Cocoon instantiation
     */
     protected ClassLoader buildInitClassLoader() {
        return this.getClass().getClassLoader();
     }

    /**
     * This builds the important ClassPath used by this Servlet.  It
     * does so in a Servlet Engine neutral way.  It uses the
     * <code>ServletContext</code>'s <code>getRealPath</code> method
     * to get the Servlet 2.2 identified classes and lib directories.
     * It iterates through every file in the lib directory and adds
     * it to the classpath.
     *
     * Also, we add the files to the ClassLoader for the Cocoon system.
     * In order to protect ourselves from skitzofrantic classloaders,
     * we need to work with a known one.
     *
     * @param context  The ServletContext to perform the lookup.
     *
     * @throws ServletException
     */
     protected String getClassPath(final ServletContext context)
     throws ServletException {
        StringBuffer buildClassPath = new StringBuffer();
        String classDir = context.getRealPath("/WEB-INF/classes");
        File root = new File(context.getRealPath("/WEB-INF/lib"));

        buildClassPath.append(classDir);

        if (root.isDirectory()) {
            File[] libraries = root.listFiles();

            for (int i = 0; i < libraries.length; i++) {
                buildClassPath.append(File.pathSeparatorChar)
                              .append(IOUtils.getFullFilename(libraries[i]));
            }
        }

        buildClassPath.append(File.pathSeparatorChar)
                      .append(System.getProperty("java.class.path"));

        buildClassPath.append(File.pathSeparatorChar)
                      .append(getExtraClassPath(context));

        return buildClassPath.toString();
     }


    /**
     * Retreives the "extra-classpath" attribute, that needs to be
     * added to the class path.
     *
     * @param context  The ServletContext to perform the lookup.
     *
     * @throws ServletException
     */
     protected String getExtraClassPath(final ServletContext context)
     throws ServletException {
         String extraClassPath = getInitParameter("extra-classpath");
         if ((extraClassPath != null) && (extraClassPath.trim().equals("") == false)) {
             StringBuffer sb = new StringBuffer();
             StringTokenizer st = new StringTokenizer(extraClassPath, System.getProperty("path.separator"), false);
             int i = 0;
             while (st.hasMoreTokens()) {
                 String s = st.nextToken();
                 if (i++ > 0)
                     sb.append(System.getProperty("path.separator"));
                 if (s.charAt(0) == System.getProperty("file.separator").charAt(0)) {
                     log.debug ("extraClassPath is absolute: " + extraClassPath);
                     sb.append(s);
                 } else {
                     log.debug ("extraClassPath is not absolute: " + context.getRealPath("/") + extraClassPath);
                     sb.append(context.getRealPath("/") + extraClassPath);
                 }
             }
             return sb.toString();
         }
         return "";
     }

    /**
     * Set up the log level and path.  The default log level is
     * Priority.ERROR, although it can be overwritten by the parameter
     * "log-level".  The log system goes to both a file and the Servlet
     * container's log system.  Only messages that are Priority.ERROR
     * and above go to the servlet context.  The log messages can
     * be as restrictive (Priority.FATAL_ERROR and above) or as liberal
     * (Priority.DEBUG and above) as you want that get routed to the
     * file.
     *
     * @param logLevel The minimum log message handling priority.
     * @param context  The ServletContext for the real path.
     *
     * @throws ServletException
     */
    private void initLogger(final ServletContext context)
    throws ServletException {
        final Priority.Enum logPriority;
        String logDir = getInitParameter("log-dir");
        String logLevel = getInitParameter("log-level");
        if (logLevel != null) {
            logLevel.trim();
        }

        if (logDir == null) {
            logDir = context.getRealPath("/") + "/WEB-INF/logs/";
        }

        this.appContext.put(Constants.CONTEXT_LOG_DIR, logDir);

        if (logLevel != null) {
            logPriority = LogKit.getPriorityForName(logLevel);
        } else {
            logPriority = Priority.ERROR;
        }

        try {
            final String path = logDir + "cocoon.log";

            final Category cocoonCategory = LogKit.createCategory("cocoon", logPriority);
            this.log = LogKit.createLogger(cocoonCategory, new LogTarget[] {
                       new FileOutputLogTarget(path),
                       new ServletOutputLogTarget(context, Priority.ERROR)
                });
        } catch (Exception e) {
            LogKit.log("Could not set up Cocoon Logger, will use screen instead", e);
        }

        LogKit.setGlobalPriority(logPriority);
    }

    /**
     * Set the ConfigFile for the Cocoon object.
     *
     * @param configFileName The file location for the cocoon.xconf
     * @param context        The servlet context to get the file handle
     *
     * @throws ServletException
     */
    private URL getConfigFile(final String configFileName, final ServletContext context)
    throws ServletException {
        final String usedFileName;

        if (configFileName == null) {
            log.warn("Servlet initialization argument 'configurations' not specified, attempting to use '/cocoon.xconf'");
            usedFileName = "/cocoon.xconf";
        } else {
            usedFileName = configFileName;
        }

        log.debug("Using configuration file: " + usedFileName);

        try {
            return context.getResource(usedFileName);
        } catch (Exception mue) {
            log.error("Servlet initialization argument 'configurations' not found at " + usedFileName, mue);
            throw new ServletException("Servlet initialization argument 'configurations' not found at " + usedFileName);
        }
    }

    private HttpServletRequest getServletRequest(HttpServletRequest request) {
        HttpServletRequest req = request;
        String contentType = req.getContentType();

        if (contentType == null) {
            contentType = "application/x-www-form-urlencoded";
        }

        if (contentType.startsWith("multipart/form-data")) {
            try {
                req = new RequestWrapper(request,
                                         this.uploadDir,
                                         CocoonServlet.ALLOW_OVERWRITE,
                                         CocoonServlet.SILENTLY_RENAME);
            } catch (Exception e) {
                log.warn("Could not create RequestWrapper", e);
                req = request;
            }
        }

        return req;
    }

    /**
     * Handle the "force-load" parameter.  This overcomes limits in
     * many classpath issues.  One of the more notorious ones is a
     * bug in WebSphere that does not load the URL handler for the
     * "classloader://" protocol.  In order to overcome that bug,
     * set "force-load" to "com.ibm.servlet.classloader.Handler".
     *
     * If you need to force more than one class to load, then
     * separate each entry with whitespace, a comma, or a semi-colon.
     * Cocoon will strip any whitespace from the entry.
     *
     * @param forceLoading The array of fully qualified classes to force loading.
     *
     * @throws ServletException
     */
    private void forceLoad(final String forceLoading) {
        if (forceLoading != null) {
            StringTokenizer fqcnTokenizer = new StringTokenizer(forceLoading, " \t\r\n\f;,", false);

            while (fqcnTokenizer.hasMoreTokens()) {
                final String fqcn = fqcnTokenizer.nextToken().trim();

                try {
                    log.debug("Trying to load class: " + fqcn);
                    ClassUtils.loadClass(fqcn);
                } catch (Exception e) {
                    log.warn("Could not force-load class: " + fqcn, e);
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
        }
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

        // This is more scalable
        long start = new Date().getTime();
        HttpServletRequest request = this.getServletRequest(req);

        Cocoon cocoon = getCocoon(request.getPathInfo(), request.getParameter(Constants.RELOAD_PARAM));

        // Check if cocoon was initialized
        if (this.cocoon == null) {
            res.setStatus(res.SC_INTERNAL_SERVER_ERROR);

            Notification n = new Notification(this);
            n.setType("internal-servlet-error");
            n.setTitle("Internal servlet error");
            n.setSource("Cocoon servlet");
            n.setMessage("Internal servlet error");
            n.setDescription("Cocoon was not initialized.");
            n.addExtraDescription("request-uri", request.getRequestURI());
            Notifier.notify(n, request, res);

            return;
        }

        // We got it... Process the request
        String uri = request.getServletPath();
        if (uri == null) uri = "";
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) uri += pathInfo;

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String prefix = request.getRequestURI();

            if (prefix == null) prefix = "";

            res.sendRedirect(res.encodeRedirectURL(prefix + "/"));
            return;
        }

        try {
            if (uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }

            Environment env = this.getEnvironment(uri, request, res);

            if (!this.cocoon.process(env)) {

                // FIXME (NKB) It is not true that !this.cocoon.process(env)
                // means only SC_NOT_FOUND
                res.setStatus(res.SC_NOT_FOUND);

                Notification n = new Notification(this);
                n.setType("resource-not-found");
                n.setTitle("Resource not found");
                n.setSource("Cocoon servlet");
                n.setMessage("Resource not found");
                n.setDescription("The requested URI \""
                                 + request.getRequestURI()
                                 + "\" was not found.");
                n.addExtraDescription("request-uri", request.getRequestURI());
                n.addExtraDescription("path-info", uri);
                Notifier.notify(n, request, res);
            }
        } catch (ResourceNotFoundException rse) {
            log.warn("The resource was not found", rse);

            res.setStatus(res.SC_NOT_FOUND);
            Notification n = new Notification(this);
            n.setType("resource-not-found");
            n.setTitle("Resource not found");
            n.setSource("Cocoon servlet");
            n.setMessage("Resource not found");
            n.setDescription("The requested URI \""
                             + request.getRequestURI()
                             + "\" was not found.");
            n.addExtraDescription("request-uri", request.getRequestURI());
            n.addExtraDescription("path-info", uri);
            Notifier.notify(n, request, res);
        } catch (Exception e) {
            log.error("Problem with servlet", e);
            //res.setStatus(res.SC_INTERNAL_SERVER_ERROR);
            Notification n = new Notification(this, e);
            n.setType("internal-server-error");
            n.setTitle("Internal server error");
            n.setSource("Cocoon servlet");
            n.addExtraDescription("request-uri", request.getRequestURI());
            n.addExtraDescription("path-info", uri);
            Notifier.notify(n, request, res);
        }

        ServletOutputStream out = res.getOutputStream();

        long end = new Date().getTime();
        String timeString = processTime(end - start);
        log.info("'" + uri + "' " + timeString);

        String showTime = request.getParameter(Constants.SHOWTIME_PARAM);

        if ((showTime != null) && !showTime.equalsIgnoreCase("no")) {
            boolean hide = showTime.equalsIgnoreCase("hide");
            out.print((hide) ? "<!-- " : "<p>");

            out.print(timeString);

            out.println((hide) ? " -->" : "</p>");
        }

        out.flush();
    }

    /**
     * Create the environment for the request
     */
    protected Environment getEnvironment(String uri,
                                       HttpServletRequest req,
                                       HttpServletResponse res)
    throws Exception {
        HttpEnvironment env;
        env = new HttpEnvironment(uri,
                                  req,
                                  res,
                                  (ServletContext) this.appContext.get(Constants.CONTEXT_SERVLET_CONTEXT));
        env.setLogger(this.log);
        return env;
    }

    /**
     * Creates the Cocoon object and handles exception handling.
     */
    private void createCocoon() {
        try {
            URL configFile = (URL) this.appContext.get(Constants.CONTEXT_CONFIG_URL);
            log.info("Reloading from: " + configFile.toExternalForm());
            Cocoon c = (Cocoon) ClassUtils.newInstance("org.apache.cocoon.Cocoon");
            c.contextualize(this.appContext);
            c.setLogger(this.log);
            c.init();
            this.creationTime = new Date().getTime();

            if (this.cocoon != null) {
                this.cocoon.dispose();
                this.cocoon = null;
            }

            this.cocoon = c;
        } catch (Exception e) {
            log.error("Exception reloading", e);
            this.exception = e;
            this.cocoon.dispose();
            this.cocoon = null;
        }
    }

    private String processTime(long time) throws IOException {

        StringBuffer out = new StringBuffer("Processed by ")
                           .append(Constants.COMPLETE_NAME)
                           .append(" in ");

        if (time > hour) {
            out.append((float) time / (float) hour);
            out.append(" hours.");
        } else if (time > minute) {
            out.append((float) time / (float) minute);
            out.append(" minutes.");
        } else if (time > second) {
            out.append((float) time / (float) second);
            out.append(" seconds.");
        } else {
            out.append(time);
            out.append(" milliseconds.");
        }

        return out.toString();
    }

    /**
     * Gets the current cocoon object.  Reload cocoon if configuration
     * changed or we are reloading.
     *
     * @returns Cocoon
     */
    private synchronized Cocoon getCocoon(final String pathInfo, final String reloadParam) {
        if (this.cocoon != null) {
            if (this.cocoon.modifiedSince(this.creationTime)) {
                log.info("Configuration changed reload attempt");
                this.createCocoon();
                return this.cocoon;
            } else if ((pathInfo == null) && (reloadParam != null)) {
                log.info("Forced reload attempt");
                this.createCocoon();
                return this.cocoon;
            }
        } else if ((pathInfo == null) && (reloadParam != null)) {
            log.info("Invalid configurations reload");
            this.createCocoon();
            return this.cocoon;
        }

        return this.cocoon;
    }
}

