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

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.CocoonAccess;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.portlet.PortletContext;
import org.apache.cocoon.environment.portlet.PortletEnvironment;
import org.apache.cocoon.portlet.multipart.MultipartActionRequest;
import org.apache.cocoon.portlet.multipart.RequestFactory;
import org.apache.commons.lang.BooleanUtils;

/**
 * This is the entry point for Cocoon execution as an JSR-168 Portlet.
 *
 * <p>This implementation of the Portlet interface requires that someone will
 * first create and initialize an instance of the Cocoon object, and this
 * Portlet will use this instance to process requests.</p>
 *
 * TODO - Rewrite this to use the CoreUtil!
 *
 * @version $Id$
 */
public class ManagedCocoonPortlet extends GenericPortlet {

    // Processing time message
    protected static final String PROCESSED_BY = "Processed by "
            + Constants.COMPLETE_NAME + " in ";

    // Used by "show-time"
    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;

    private Logger log;

    /**
     * Holds exception happened during initialization (if any)
     */
    protected Exception exception;


    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hiddenShowTime;


    /**
     * Default value for {@link #enableUploads} parameter (false)
     */
    private static final boolean ENABLE_UPLOADS = false;
    private static final boolean SAVE_UPLOADS_TO_DISK = true;
    private static final int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    /**
     * Allow processing of upload requests (mime/multipart)
     */
    private boolean enableUploads;
    private boolean autoSaveUploads;
    private boolean allowOverwrite;
    private boolean silentlyRename;
    private int maxUploadSize;

    private File uploadDir;
    private File workDir;

    private String containerEncoding;
    private String defaultFormEncoding;

    protected javax.portlet.PortletContext portletContext;
    protected PortletContext envPortletContext;


    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the portlet container.
     */
    private boolean manageExceptions;

    /**
     * This is the path to the portlet context (or the result
     * of calling getRealPath('/') on the PortletContext.
     * Note, that this can be null.
     */
    protected String portletContextPath;

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

        String value;

        this.portletContext = conf.getPortletContext();
        this.envPortletContext = new PortletContext(this.portletContext);
        this.portletContextPath = this.portletContext.getRealPath("/");

        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = getInitParameter("work-directory");
        if (workDirParam != null) {
            if (this.portletContextPath == null) {
                // No context path : consider work-directory as absolute
                this.workDir = new File(workDirParam);
            } else {
                // Context path exists : is work-directory absolute ?
                File workDirParamFile = new File(workDirParam);
                if (workDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    this.workDir = workDirParamFile;
                } else {
                    // No : consider it relative to context path
                    this.workDir = new File(portletContextPath, workDirParam);
                }
            }
        } else {
            // TODO: Check portlet specification
            this.workDir = (File) this.portletContext.getAttribute("javax.servlet.context.tempdir");
            if (this.workDir == null) {
                this.workDir = new File(this.portletContext.getRealPath("/WEB-INF/work"));
            }
            this.workDir = new File(workDir, "cocoon-files");
        }
        this.workDir.mkdirs();

        // Init logger
        // FIXME - how do we get the settings object?
        this.log = BeanFactoryUtil.createRootLogger(this.envPortletContext, new MutableSettings());

        final String uploadDirParam = conf.getInitParameter("upload-directory");
        if (uploadDirParam != null) {
            if (this.portletContextPath == null) {
                this.uploadDir = new File(uploadDirParam);
            } else {
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    this.uploadDir = uploadDirParamFile;
                } else {
                    // No : consider it relative to context path
                    this.uploadDir = new File(portletContextPath, uploadDirParam);
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Using upload-directory " + this.uploadDir);
            }
        } else {
            this.uploadDir = new File(workDir, "upload-dir" + File.separator);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("upload-directory was not set - defaulting to " + this.uploadDir);
            }
        }
        this.uploadDir.mkdirs();

        this.enableUploads = getInitParameterAsBoolean("enable-uploads", ENABLE_UPLOADS);
        this.autoSaveUploads = getInitParameterAsBoolean("autosave-uploads", SAVE_UPLOADS_TO_DISK);

        String overwriteParam = getInitParameter("overwrite-uploads", "rename");
        // accepted values are deny|allow|rename - rename is default.
        if ("deny".equalsIgnoreCase(overwriteParam)) {
            this.allowOverwrite = false;
            this.silentlyRename = false;
        } else if ("allow".equalsIgnoreCase(overwriteParam)) {
            this.allowOverwrite = true;
            this.silentlyRename = false; // ignored in this case
        } else {
            // either rename is specified or unsupported value - default to rename.
            this.allowOverwrite = false;
            this.silentlyRename = true;
        }

        this.maxUploadSize = getInitParameterAsInteger("upload-max-size", MAX_UPLOAD_SIZE);

        value = conf.getInitParameter("show-time");
        this.showTime = BooleanUtils.toBoolean(value) || (this.hiddenShowTime = "hide".equals(value));
        if (value == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("show-time was not set - defaulting to false");
            }
        }

        this.containerEncoding = getInitParameter("container-encoding", "ISO-8859-1");
        this.defaultFormEncoding = getInitParameter("form-encoding", "ISO-8859-1");

        this.manageExceptions = getInitParameterAsBoolean("manage-exceptions", true);

        this.requestFactory = new RequestFactory(this.autoSaveUploads,
                                                 this.uploadDir,
                                                 this.allowOverwrite,
                                                 this.silentlyRename,
                                                 this.maxUploadSize,
                                                 this.defaultFormEncoding);

        this.servletPath = getInitParameter("servlet-path", null);
        if (this.servletPath != null) {
            if (this.servletPath.startsWith("/")) {
                this.servletPath = this.servletPath.substring(1);
            }
            if (this.servletPath.endsWith("/")) {
                this.servletPath = servletPath.substring(0, servletPath.length() - 1);
            }
        }

        final String sessionScopeParam = getInitParameter("default-session-scope", "portlet");
        if ("application".equalsIgnoreCase(sessionScopeParam)) {
            this.defaultSessionScope = javax.portlet.PortletSession.APPLICATION_SCOPE;
        } else {
            this.defaultSessionScope = javax.portlet.PortletSession.PORTLET_SCOPE;
        }

        this.storeSessionPath = getInitParameterAsBoolean("store-session-path", false);
    }

    public void processAction(ActionRequest req, ActionResponse res)
    throws PortletException, IOException {

        // remember when we started (used for timing the processing)
        long start = System.currentTimeMillis();

        // add the cocoon header timestamp
        res.setProperty("X-Cocoon-Version", Constants.VERSION);

        // get the request (wrapped if contains multipart-form data)
        ActionRequest request;
        try {
            if (this.enableUploads) {
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
        Cocoon cocoon = getCocoon();

        // Check if cocoon was initialized
        if (cocoon == null) {
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
                if (!cocoon.process(env)) {
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
        Cocoon cocoon = getCocoon();

        // Check if cocoon was initialized
        if (cocoon == null) {
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
                if (!cocoon.process(env)) {
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
                boolean show = this.showTime;
                if (showTime != null) {
                    show = !showTime.equalsIgnoreCase("no");
                }
                if (show) {
                    boolean hide = this.hiddenShowTime;
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
        if (this.manageExceptions) {
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
            formEncoding = this.defaultFormEncoding;
        }
        env = new PortletEnvironment(servletPath,
                                     pathInfo,
                                     uri,
                                     req,
                                     res,
                                     this.portletContext,
                                     this.envPortletContext,
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
            formEncoding = this.defaultFormEncoding;
        }
        env = new PortletEnvironment(servletPath,
                                     pathInfo,
                                     uri,
                                     req,
                                     res,
                                     this.portletContext,
                                     this.envPortletContext,
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
     * Gets the current cocoon object.
     * Reload cocoon if configuration changed or we are reloading.
     */
    private Cocoon getCocoon() {
        return new CocoonAccess() {
            final Cocoon instance() {
                return super.getCocoon();
            }
        }.instance();
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
    protected String getInitParameter(String name, String defaultValue) {
        String result = getInitParameter(name);
        if (result == null) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
                getLogger().debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            result = defaultValue;
        } 
        return result;
    }

    /** Convenience method to access boolean portlet parameters */
    protected boolean getInitParameterAsBoolean(String name, boolean defaultValue) {
        String value = getInitParameter(name);
        if (value == null) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
                getLogger().debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        }

        return BooleanUtils.toBoolean(value);
    }

    protected int getInitParameterAsInteger(String name, int defaultValue) {
        String value = getInitParameter(name);
        if (value == null) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
                getLogger().debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    protected Logger getLogger() {
        return this.log;
    }
}
