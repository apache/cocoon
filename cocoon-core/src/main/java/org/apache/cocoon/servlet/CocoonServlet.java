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
package org.apache.cocoon.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.servlet.multipart.MultipartHttpServletRequest;
import org.apache.cocoon.servlet.multipart.RequestFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;

/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 *
 * @version $Id$
 */
public class CocoonServlet extends HttpServlet {

    /**
     * Application <code>Context</code> Key for the servlet configuration
     * @since 2.1.3
     */
    public static final String CONTEXT_SERVLET_CONFIG = "servlet-config";

    // Processing time message
    protected static final String PROCESSED_BY = "Processed by "
            + Constants.COMPLETE_NAME + " in ";

    // Used by "show-time"
    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;

    /**
     * The <code>Processor</code> instance
     */
    protected Processor processor;

    /**
     * Holds exception happened during initialization (if any)
     */
    protected Exception exception;

    private String containerEncoding;

    protected ServletContext servletContext;

    /**
     * The RequestFactory is responsible for wrapping multipart-encoded
     * forms and for handing the file payload of incoming requests
     */
    protected RequestFactory requestFactory;

    /** CoreUtil */
    protected CoreUtil coreUtil;

    /** The logger */
    protected Logger log;

    protected Context environmentContext;

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
        this.servletContext = conf.getServletContext();
        this.servletContext.log("Initializing Apache Cocoon " + Constants.VERSION);

        super.init(conf);

        // initialize settings
        ServletBootstrapEnvironment env = new ServletBootstrapEnvironment(conf);

        try {
            this.environmentContext = new HttpContext(conf.getServletContext());
            this.coreUtil = new CoreUtil(this.environmentContext, env);
            this.log = this.coreUtil.getRootLogger();
        } catch (Exception e) {
            this.servletContext.log("Error during initialization - aborting.");
            this.servletContext.log(ExceptionUtils.getFullStackTrace(e));
            if ( e instanceof ServletException ) {
                throw (ServletException)e;
            }
            throw new ServletException(e);
        }

        this.containerEncoding = this.getInitParameter("container-encoding", "ISO-8859-1");
        this.requestFactory = new RequestFactory(coreUtil.getSettings().isAutosaveUploads(),
                                                 new File(coreUtil.getSettings().getUploadDirectory()),
                                                 coreUtil.getSettings().isAllowOverwrite(),
                                                 coreUtil.getSettings().isSilentlyRename(),
                                                 coreUtil.getSettings().getMaxUploadSize(),
                                                 this.containerEncoding);

        try {
            this.exception = null;
            this.processor = this.coreUtil.createProcessor();          
        } catch (Exception e) {
            this.exception = e;
        }
        if (this.exception == null) {
            this.servletContext.log("Apache Cocoon " + Constants.VERSION + " is up and ready.");
        } else {
            final String message = "Errors during initializing Apache Cocoon " + Constants.VERSION + " : " + this.exception.getMessage();
            this.servletContext.log(message, this.exception);
        }
    }

    /**
     * Dispose Cocoon when servlet is destroyed
     */
    public void destroy() {
        this.servletContext.log("Destroying Cocoon Servlet.");
        if (this.coreUtil != null) {
            this.coreUtil.destroy();
            this.coreUtil = null;
            // coreUtil will dispose it.
            this.processor = null;
        }

        this.requestFactory = null;
        this.servletContext = null;
        this.environmentContext = null;
        this.log = null;
        super.destroy();
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        
        // used for timing the processing
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // add the cocoon header timestamp
        if (this.coreUtil.getSettings().isShowVersion()) {
            res.addHeader("X-Cocoon-Version", Constants.VERSION);
        }

        // get the request (wrapped if contains multipart-form data)
        HttpServletRequest request;
        try{
            if (this.coreUtil.getSettings().isEnableUploads()) {
                request = requestFactory.getServletRequest(req);
            } else {
                request = req;
            }
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Problem with Cocoon servlet", e);
            }

            manageException(req, res, null, null,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Problem in creating the Request", null, null, e);
            return;
        }

        // Get the cocoon engine instance
        try {
            this.exception = null;
            this.processor = this.coreUtil.getProcessor(request.getPathInfo(), request.getParameter(Constants.RELOAD_PARAM));
        } catch (Exception e) {
            this.exception = e;
        }

        // Check if cocoon was initialized
        if (this.processor == null) {
            manageException(request, res, null, null,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Initialization Problem",
                            null /* "Cocoon was not initialized" */,
                            null /* "Cocoon was not initialized, cannot process request" */,
                            this.exception);
            return;
        }

        // We got it... Process the request
        String uri = request.getServletPath();
        if (uri == null) {
            uri = "";
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            // VG: WebLogic fix: Both uri and pathInfo starts with '/'
            // This problem exists only in WL6.1sp2, not in WL6.0sp2 or WL7.0b.
            if (uri.length() > 0 && uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            uri += pathInfo;
        }

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String prefix = request.getRequestURI();
            if (prefix == null) {
                prefix = "";
            }

            res.sendRedirect(res.encodeRedirectURL(prefix + "/"));
            return;
        }

        String contentType = null;

        Environment env;
        try{
            if (uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            // Pass uri into environment without URLDecoding, as it is already decoded.
            env = getEnvironment(uri, request, res);
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Problem with Cocoon servlet", e);
            }

            manageException(request, res, null, uri,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Problem in creating the Environment", null, null, e);
            return;
        }

        try {
            try {
                if (this.processor.process(env)) {
                    contentType = env.getContentType();
                } else {
                    // We reach this when there is nothing in the processing change that matches
                    // the request. For example, no matcher matches.
                    getLogger().fatalError("The Cocoon engine failed to process the request.");
                    manageException(request, res, env, uri,
                                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "Request Processing Failed",
                                    "Cocoon engine failed in process the request",
                                    "The processing engine failed to process the request. This could be due to lack of matching or bugs in the pipeline engine.",
                                    null);
                    return;
                }
            } catch (ResourceNotFoundException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().warn(e.getMessage(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.getMessage());
                }

                manageException(request, res, env, uri,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Resource Not Found",
                                "Resource Not Found",
                                "The requested resource \"" + request.getRequestURI() + "\" could not be found",
                                e);
                return;

            } catch (ConnectionResetException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.toString(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.toString());
                }

            } catch (IOException e) {
                // Tomcat5 wraps SocketException into ClientAbortException which extends IOException.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.toString(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.toString());
                }

            } catch (Exception e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Internal Cocoon Problem", e);
                }

                manageException(request, res, env, uri,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Internal Server Error", null, null, e);
                return;
            }

            stopWatch.stop();
            String timeString = null;
            if (getLogger().isInfoEnabled()) {
                timeString = processTime(stopWatch.getTime());
                getLogger().info("'" + uri + "' " + timeString);
            }

            if (contentType != null && contentType.equals("text/html")) {
                String showTime = request.getParameter(Constants.SHOWTIME_PARAM);
                boolean show = this.coreUtil.getSettings().isShowTime();
                if (showTime != null) {
                    show = !showTime.equalsIgnoreCase("no");
                }
                if (show) {
                    if ( timeString == null ) {
                        timeString = processTime(stopWatch.getTime());
                    }
                    boolean hide = this.coreUtil.getSettings().isHideShowTime();
                    if (showTime != null) {
                        hide = showTime.equalsIgnoreCase("hide");
                    }
                    ServletOutputStream out = res.getOutputStream();
                    out.print((hide) ? "<!-- " : "<p>");
                    out.print(timeString);
                    out.println((hide) ? " -->" : "</p>");
                }
            }
        } finally {
            try {
                if (request instanceof MultipartHttpServletRequest) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Deleting uploaded file(s).");
                    }
                    ((MultipartHttpServletRequest) request).cleanup();
                }
            } catch (IOException e) {
                getLogger().error("Cocoon got an Exception while trying to cleanup the uploaded files.", e);
            }

            /*
             * Servlet Specification 2.2, 6.5 Closure of Response Object:
             *
             *   A number of events can indicate that the servlet has provided all of the
             *   content to satisfy the request and that the response object can be
             *   considered to be closed. The events are:
             *     o The termination of the service method of the servlet.
             *     o When the amount of content specified in the setContentLength method
             *       of the response has been written to the response.
             *     o The sendError method is called.
             *     o The sendRedirect method is called.
             *   When a response is closed, all content in the response buffer, if any remains,
             *   must be immediately flushed to the client.
             *
             * Due to the above, out.flush() and out.close() are not necessary, and sometimes
             * (if sendError or sendRedirect were used) request may be already closed.
             */
        }
    }

    protected void manageException(HttpServletRequest req, HttpServletResponse res, Environment env,
                                   String uri, int errorStatus,
                                   String title, String message, String description,
                                   Exception e)
    throws IOException {
        if (this.coreUtil.getSettings().isManageExceptions()) {
            if (env != null) {
                env.tryResetResponse();
            } else {
                res.reset();
            }

            String type = Notifying.FATAL_NOTIFICATION;
            HashMap extraDescriptions = null;

            if (errorStatus == HttpServletResponse.SC_NOT_FOUND) {
                type = "resource-not-found";
                // Do not show the exception stacktrace for such common errors.
                e = null;
            } else {
                extraDescriptions = new HashMap(2);
                extraDescriptions.put(Notifying.EXTRA_REQUESTURI, req.getRequestURI());
                if (uri != null) {
                     extraDescriptions.put("Request URI", uri);
                }

                // Do not show exception stack trace when log level is WARN or above. Show only message.
                if (!getLogger().isInfoEnabled()) {
                    Throwable t = DefaultNotifyingBuilder.getRootCause(e);
                    if (t != null) extraDescriptions.put(Notifying.EXTRA_CAUSE, t.getMessage());
                    e = null;
                }
            }

            Notifying n = new DefaultNotifyingBuilder().build(this,
                                                              e,
                                                              type,
                                                              title,
                                                              "Cocoon Servlet",
                                                              message,
                                                              description,
                                                              extraDescriptions);

            res.setContentType("text/html");
            res.setStatus(errorStatus);
            Notifier.notify(n, res.getOutputStream(), "text/html");
        } else {
            res.sendError(errorStatus, title);
            res.flushBuffer();
        }
    }

    /**
     * Create the environment for the request
     */
    protected Environment getEnvironment(String uri,
                                         HttpServletRequest req,
                                         HttpServletResponse res)
    throws Exception {
        HttpEnvironment env;

        String formEncoding = req.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.coreUtil.getSettings().getFormEncoding();
        }
        env = new HttpEnvironment(uri,
                                  req,
                                  res,
                                  this.servletContext,
                                  this.environmentContext,
                                  this.containerEncoding,
                                  formEncoding);
        env.enableLogging(getLogger());
        return env;
    }

    private String processTime(long time) {
        StringBuffer out = new StringBuffer(PROCESSED_BY);
        if (time <= SECOND) {
            out.append(time);
            out.append(" milliseconds.");
        } else if (time <= MINUTE) {
            out.append(time / SECOND);
            out.append(" seconds.");
        } else if (time <= HOUR) {
            out.append(time / MINUTE);
            out.append(" minutes.");
        } else {
            out.append(time / HOUR);
            out.append(" hours.");
        }
        return out.toString();
    }

    /**
     * Get an initialisation parameter. The value is trimmed, and null is returned if the trimmed value
     * is empty.
     */
    public String getInitParameter(String name) {
        String result = super.getInitParameter(name);
        if (result != null) {
            result = result.trim();
            if (result.length() == 0) {
                result = null;
            }
        }

        return result;
    }

    /** Convenience method to access servlet parameters */
    protected String getInitParameter(String name, String defaultValue) {
        String result = getInitParameter(name);
        if (result == null) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
                getLogger().debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        }
        return result;
    }

    protected Logger getLogger() {
        return this.log;
    }

    protected static final class ServletBootstrapEnvironment
        implements BootstrapEnvironment {

        private final ServletConfig config;

        public ServletBootstrapEnvironment(ServletConfig config) {
            this.config = config;
        }

        /**
         * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.cocoon.core.MutableSettings)
         */
        public void configure(MutableSettings settings) {
            // fill from the servlet parameters
            SettingsHelper.fill(settings, this.config);
            if ( settings.getWorkDirectory() == null ) {
                final File workDir = (File)this.config.getServletContext().getAttribute("javax.servlet.context.tempdir");
                settings.setWorkDirectory(workDir.getAbsolutePath());
            }
            if ( settings.getLoggingConfiguration() == null ) {
                settings.setLoggingConfiguration("/WEB-INF/log4j.xconf");
            }
        }

        /**
         * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.avalon.framework.context.DefaultContext)
         */
        public void configure(DefaultContext context) {
            context.put(CONTEXT_SERVLET_CONFIG, this.config);
        }
    }
}
