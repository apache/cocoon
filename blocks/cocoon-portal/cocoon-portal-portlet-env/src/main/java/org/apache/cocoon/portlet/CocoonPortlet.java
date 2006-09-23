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
package org.apache.cocoon.portlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.portlet.PortletContext;
import org.apache.cocoon.environment.portlet.PortletEnvironment;
import org.apache.cocoon.portlet.multipart.MultipartActionRequest;
import org.apache.cocoon.portlet.multipart.RequestFactory;

/**
 * This is the entry point for Cocoon execution as an JSR-168 Portlet.
 *
 * @version $Id$
 */
public class CocoonPortlet extends GenericPortlet {

    /**
     * Application <code>Context</code> Key for the portlet configuration
     * @since 2.1.3
     */
    public static final String CONTEXT_PORTLET_CONFIG = "portlet-config";

    // Processing time message
    protected static final String PROCESSED_BY = "Processed by "
            + Constants.COMPLETE_NAME + " in ";

    // Used by "show-time"
    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;

    private Logger log;

    /**
     * The <code>Cocoon</code> instance
     */
    protected Cocoon cocoon;

    /**
     * Holds exception happened during initialization (if any)
     */
    protected Exception exception;

    private String containerEncoding;

    protected javax.portlet.PortletContext portletContext;

    /**
     * The RequestFactory is responsible for wrapping multipart-encoded
     * forms and for handing the file payload of incoming requests
     */
    protected RequestFactory requestFactory;

    /**
     * Value to be used as servletPath in the request.
     * Provided via configuration parameter, if missing, defaults to the
     * '/portlets/' + portletName.
     */
    protected String servletPath;

    /**
     * Default scope for the session attributes, either
     * {@link javax.portlet.PortletSession#PORTLET_SCOPE} or
     * {@link javax.portlet.PortletSession#APPLICATION_SCOPE}.
     * This corresponds to <code>default-session-scope</code>
     * parameter, with default value <code>portlet</code>.
     *
     * @see org.apache.cocoon.environment.portlet.PortletSession
     */
    protected int defaultSessionScope;

    /**
     * Store pathInfo in session
     */
    protected boolean storeSessionPath;

    /** CoreUtil */
    protected CoreUtil coreUtil;

    /** Settings */
    protected Settings settings;

    protected Context environmentContext;

    /**
     * Initialize this <code>CocoonPortlet</code> instance.
     *
     * <p>Uses the following parameters:
     *  portlet-logger
     *  enable-uploads
     *  autosave-uploads
     *  overwrite-uploads
     *  upload-max-size
     *  show-time
     *  container-encoding
     *  form-encoding
     *  manage-exceptions
     *  servlet-path
     *
     * @param conf The PortletConfig object from the portlet container.
     * @throws PortletException
     */
    public void init(PortletConfig conf) throws PortletException {

        super.init(conf);
        this.portletContext = conf.getPortletContext();

        // initialize settings
        PortletBootstrapEnvironment env = new PortletBootstrapEnvironment(conf);

        try {
            this.environmentContext = new PortletContext(conf.getPortletContext());
            this.coreUtil = new CoreUtil(this.environmentContext, env);
            this.log = this.coreUtil.getRootLogger();
        } catch (Exception e) {
            if ( e instanceof PortletException ) {
                throw (PortletException)e;
            }
            throw new PortletException(e);
        }

        this.containerEncoding = getInitParameter("container-encoding", "ISO-8859-1");
        this.settings = this.coreUtil.getSettings();
        this.requestFactory = new RequestFactory(this.settings.isAutosaveUploads(),
                new File(this.settings.getUploadDirectory()),
                this.settings.isAllowOverwrite(),
                this.settings.isSilentlyRename(),
                this.settings.getMaxUploadSize(),
                this.settings.getFormEncoding());

        final String sessionScopeParam = getInitParameter("default-session-scope", "portlet");
        if ("application".equalsIgnoreCase(sessionScopeParam)) {
            this.defaultSessionScope = javax.portlet.PortletSession.APPLICATION_SCOPE;
        } else {
            this.defaultSessionScope = javax.portlet.PortletSession.PORTLET_SCOPE;
        }

        try {
            this.exception = null;
            // FIXME
            this.cocoon = (Cocoon)this.coreUtil.createProcessor();
        } catch (Exception e) {
            this.exception = e;
        }
    }

    /**
     * Dispose Cocoon when portlet is destroyed
     */
    public void destroy() {
        this.portletContext.log("Destroying Cocoon Portlet.");
        if ( this.coreUtil != null ) {
            this.coreUtil.destroy();
            this.coreUtil = null;
        }
        super.destroy();
    }

    /**
     * Process the specified <code>ActionRequest</code> producing output
     * on the specified <code>ActionResponse</code>.
     */
    public void processAction(ActionRequest req, ActionResponse res)
    throws PortletException, IOException {

        // remember when we started (used for timing the processing)
        long start = System.currentTimeMillis();

        // add the cocoon header timestamp
        res.setProperty("X-Cocoon-Version", Constants.VERSION);

        // get the request (wrapped if contains multipart-form data)
        ActionRequest request;
        try {
            if (this.settings.isEnableUploads()) {
                request = requestFactory.getServletRequest(req);
            } else {
                request = req;
            }
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Problem with Cocoon portlet", e);
            }

            manageException(req, res, null, null,
                            "Problem in creating the Request", null, null, e);
            return;
        }

        // Get the cocoon engine instance
        try {
            this.exception = null;
            // FIXME
            this.cocoon = (Cocoon)this.coreUtil.getProcessor(request.getParameter(Constants.RELOAD_PARAM) != null);
        } catch (Exception e) {
            this.exception = e;
        }

        // Check if cocoon was initialized
        if (this.cocoon == null) {
            manageException(request, res, null, null,
                            "Initialization Problem",
                            null /* "Cocoon was not initialized" */,
                            null /* "Cocoon was not initialized, cannot process request" */,
                            this.exception);
            return;
        }

        // We got it... Process the request
        String servletPath = this.servletPath;
        if (servletPath == null) {
            servletPath = "portlets/" + getPortletConfig().getPortletName();
        }
        String pathInfo = getPathInfo(request);

        String uri = servletPath;
        if (pathInfo != null) {
            uri += pathInfo;
        }

        Environment env;
        try {
            if (uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            env = getEnvironment(servletPath, pathInfo, uri, request, res);
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Problem with Cocoon portlet", e);
            }

            manageException(request, res, null, uri,
                            "Problem in creating the Environment", null, null, e);
            return;
        }

        try {
            try {
                if (!this.cocoon.process(env)) {
                    // We reach this when there is nothing in the processing change that matches
                    // the request. For example, no matcher matches.
                    getLogger().fatalError("The Cocoon engine failed to process the request.");
                    manageException(request, res, env, uri,
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
                                "Resource Not Found",
                                "Resource Not Found",
                                "The requested portlet could not be found",
                                e);
                return;

            } catch (ConnectionResetException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.getMessage(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.getMessage());
                }

            } catch (IOException e) {
                // Tomcat5 wraps SocketException into ClientAbortException which extends IOException.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.getMessage(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.getMessage());
                }

            } catch (Exception e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Internal Cocoon Problem", e);
                }

                manageException(request, res, env, uri,
                                "Internal Server Error", null, null, e);
                return;
            }

            long end = System.currentTimeMillis();
            String timeString = processTime(end - start);
            if (getLogger().isInfoEnabled()) {
                getLogger().info("'" + uri + "' " + timeString);
            }
            res.setProperty("X-Cocoon-Time", timeString);
        } finally {
            if (request instanceof MultipartActionRequest) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Deleting uploaded file(s).");
                }
                ((MultipartActionRequest) request).cleanup();
            }
        }
    }

    /**
     * Process the specified <code>RenderRequest</code> producing output
     * on the specified <code>RenderResponse</code>.
     */
    public void render(RenderRequest req, RenderResponse res)
    throws PortletException, IOException {
        // remember when we started (used for timing the processing)
        long start = System.currentTimeMillis();

        // add the cocoon header timestamp
        res.setProperty("X-Cocoon-Version", Constants.VERSION);

        // get the request (wrapped if contains multipart-form data)
        RenderRequest request = req;

        // Get the cocoon engine instance
        try {
            this.exception = null;
            // FIXME
            this.cocoon = (Cocoon)this.coreUtil.getProcessor(request.getParameter(Constants.RELOAD_PARAM) != null);
        } catch (Exception e) {
            this.exception = e;
        }

        // Check if cocoon was initialized
        if (this.cocoon == null) {
            manageException(request, res, null, null,
                            "Initialization Problem",
                            null /* "Cocoon was not initialized" */,
                            null /* "Cocoon was not initialized, cannot process request" */,
                            this.exception);
            return;
        }

        // We got it... Process the request
        String servletPath = this.servletPath;
        if (servletPath == null) {
            servletPath = "portlets/" + getPortletConfig().getPortletName();
        }
        String pathInfo = getPathInfo(request);

        String uri = servletPath;
        if (pathInfo != null) {
            uri += pathInfo;
        }

        String contentType = null;

        Environment env;
        try {
            if (uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            env = getEnvironment(servletPath, pathInfo, uri, request, res);
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Problem with Cocoon portlet", e);
            }

            manageException(request, res, null, uri,
                            "Problem in creating the Environment", null, null, e);
            return;
        }

        try {
            try {
                if (this.cocoon.process(env)) {
                    contentType = env.getContentType();
                } else {
                    // We reach this when there is nothing in the processing change that matches
                    // the request. For example, no matcher matches.
                    getLogger().fatalError("The Cocoon engine failed to process the request.");
                    manageException(request, res, env, uri,
                                    "Request Processing Failed",
                                    "Cocoon engine failed in process the request",
                                    "The processing engine failed to process the request. This could be due to lack of matching or bugs in the pipeline engine.",
                                    null);
                    return;
                }
            } catch (ResourceNotFoundException rse) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("The resource was not found", rse);
                }

                manageException(request, res, env, uri,
                                "Resource Not Found",
                                "Resource Not Found",
                                "The requested portlet could not be found",
                                rse);
                return;

            } catch (ConnectionResetException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.getMessage(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.getMessage());
                }

            } catch (IOException e) {
                // Tomcat5 wraps SocketException into ClientAbortException which extends IOException.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(e.getMessage(), e);
                } else if (getLogger().isWarnEnabled()) {
                    getLogger().warn(e.getMessage());
                }

            } catch (Exception e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Internal Cocoon Problem", e);
                }

                manageException(request, res, env, uri,
                                "Internal Server Error", null, null, e);
                return;
            }

            long end = System.currentTimeMillis();
            String timeString = processTime(end - start);
            if (getLogger().isInfoEnabled()) {
                getLogger().info("'" + uri + "' " + timeString);
            }
            res.setProperty("X-Cocoon-Time", timeString);

            if (contentType != null && contentType.equals("text/html")) {
                String showTime = request.getParameter(Constants.SHOWTIME_PARAM);
                boolean show = this.settings.isShowTime();
                if (showTime != null) {
                    show = !showTime.equalsIgnoreCase("no");
                }
                if (show) {
                    boolean hide = this.settings.isHideShowTime();
                    if (showTime != null) {
                        hide = showTime.equalsIgnoreCase("hide");
                    }
                    PrintStream out = new PrintStream(res.getPortletOutputStream());
                    out.print((hide) ? "<!-- " : "<p>");
                    out.print(timeString);
                    out.println((hide) ? " -->" : "</p>\n");
                }
            }
        } finally {
            if (request instanceof MultipartActionRequest) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Deleting uploaded file(s).");
                }
                ((MultipartActionRequest) request).cleanup();
            }

            /*
             * Portlet Specification 1.0, PLT.12.3.2 Output Stream and Writer Objects:
             *   The termination of the render method of the portlet indicates
             *   that the portlet has satisfied the request and that the output
             *   object is to be closed.
             *
             * Portlet container will close the stream, no need to close it here.
             */
        }
    }

    private String getPathInfo(PortletRequest request) {
        PortletSession session = null;

        String pathInfo = request.getParameter(PortletEnvironment.PARAMETER_PATH_INFO);
        if (storeSessionPath) {
            session = request.getPortletSession(true);
            if (pathInfo == null) {
                pathInfo = (String)session.getAttribute(PortletEnvironment.PARAMETER_PATH_INFO);
            }
        }

        // Make sure it starts with or equals to '/'
        if (pathInfo == null) {
            pathInfo = "/";
        } else if (!pathInfo.startsWith("/")) {
            pathInfo = '/' + pathInfo;
        }

        if (storeSessionPath) {
            session.setAttribute(PortletEnvironment.PARAMETER_PATH_INFO, pathInfo);
        }
        return pathInfo;
    }

    protected void manageException(ActionRequest req, ActionResponse res, Environment env,
                                   String uri, String title, String message, String description,
                                   Exception e)
    throws PortletException {
        throw new PortletException("Exception in CocoonPortlet", e);
    }

    protected void manageException(RenderRequest req, RenderResponse res, Environment env,
                                   String uri, String title, String message, String description,
                                   Exception e)
    throws IOException, PortletException {
        if (this.settings.isManageExceptions()) {
            if (env != null) {
                env.tryResetResponse();
            } else {
                res.reset();
            }

            String type = Notifying.FATAL_NOTIFICATION;
            HashMap extraDescriptions = null;

            extraDescriptions = new HashMap(2);
            extraDescriptions.put(Notifying.EXTRA_REQUESTURI, getPortletConfig().getPortletName());
            if (uri != null) {
                extraDescriptions.put("Request URI", uri);
            }

            // Do not show exception stack trace when log level is WARN or above. Show only message.
            if (!getLogger().isInfoEnabled()) {
                Throwable t = DefaultNotifyingBuilder.getRootCause(e);
                if (t != null) extraDescriptions.put(Notifying.EXTRA_CAUSE, t.getMessage());
                e = null;
            }

            Notifying n = new DefaultNotifyingBuilder().build(this,
                                                              e,
                                                              type,
                                                              title,
                                                              "Cocoon Portlet",
                                                              message,
                                                              description,
                                                              extraDescriptions);

            res.setContentType("text/html");
            Notifier.notify(n, res.getPortletOutputStream(), "text/html");
        } else {
            res.flushBuffer();
            throw new PortletException("Exception in CocoonPortlet", e);
        }
    }

    /**
     * Create the environment for the request
     */
    protected Environment getEnvironment(String servletPath,
                                         String pathInfo,
                                         String uri,
                                         ActionRequest req,
                                         ActionResponse res)
    throws Exception {
        PortletEnvironment env;

        String formEncoding = req.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.settings.getFormEncoding();
        }
        env = new PortletEnvironment(servletPath,
                                     pathInfo,
                                     uri,
                                     req,
                                     res,
                                     this.portletContext,
                                     this.environmentContext,
                                     this.containerEncoding,
                                     formEncoding,
                                     this.defaultSessionScope);
        env.enableLogging(getLogger());
        return env;
    }

    /**
     * Create the environment for the request
     */
    protected Environment getEnvironment(String servletPath,
                                         String pathInfo,
                                         String uri,
                                         RenderRequest req,
                                         RenderResponse res)
    throws Exception {
        PortletEnvironment env;

        String formEncoding = req.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.settings.getFormEncoding();
        }
        env = new PortletEnvironment(servletPath,
                                     pathInfo,
                                     uri,
                                     req,
                                     res,
                                     this.portletContext,
                                     this.environmentContext,
                                     this.containerEncoding,
                                     formEncoding,
                                     this.defaultSessionScope);
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

    /** Convenience method to access portlet parameters */
    private String getInitParameter(String name, String defaultValue) {
        String result = getInitParameter(name);
        if (result == null) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
                getLogger().debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            result = defaultValue;
        }
        return result;
    }

    protected Logger getLogger() {
        return this.log;
    }

    protected static final class PortletBootstrapEnvironment
    implements BootstrapEnvironment {

        private final PortletConfig config;

        public PortletBootstrapEnvironment(PortletConfig config) {
            this.config = config;
        }

        /**
         * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.cocoon.core.MutableSettings)
         */
        public void configure(MutableSettings settings) {
            // fill from the portlet parameters
            SettingsHelper.fill(settings, this.config);
            if ( settings.getWorkDirectory() == null ) {
                final File workDir = (File)this.config.getPortletContext().getAttribute("javax.servlet.context.tempdir");
                settings.setWorkDirectory(workDir.getAbsolutePath());
            }
        }

        /**
         * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.avalon.framework.context.DefaultContext)
         */
        public void configure(DefaultContext context) {
            // Add the portlet configuration
            context.put(CONTEXT_PORTLET_CONFIG, this.config);
        }
    }

}
