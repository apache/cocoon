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
package org.apache.cocoon;

import org.apache.avalon.excalibur.logger.LoggerManageable;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.RoleManager;
import org.apache.cocoon.core.container.spring.ApplicationContextFactory;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.ConfigReader;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;

import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.URLSource;
import org.springframework.context.ApplicationContext;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;

/**
 * The Cocoon Object is the main Kernel for the entire Cocoon system.
 *
 * @version $Id$
 */
public class Cocoon
        extends AbstractLogEnabled
        implements ThreadSafe,
                   Initializable,
                   Disposable,
                   Modifiable,
                   Processor,
                   Contextualizable,
                   Serviceable,
                   LoggerManageable {

    // Register the location finder for Avalon configuration objects and exceptions
    // and keep a strong reference to it.
    private static final LocationUtils.LocationFinder confLocFinder = new LocationUtils.LocationFinder() {
        public Location getLocation(Object obj, String description) {
            if (obj instanceof Configuration) {
                Configuration config = (Configuration)obj;
                String locString = config.getLocation();
                Location result = LocationUtils.parse(locString);
                if (LocationUtils.isKnown(result)) {
                    // Add description
                    StringBuffer desc = new StringBuffer().append('<');
                    // Unfortunately Configuration.getPrefix() is not public
                    try {
                        if (config.getNamespace().startsWith("http://apache.org/cocoon/sitemap/")) {
                            desc.append("map:");
                        }
                    } catch (ConfigurationException e) {
                        // no namespace: ignore
                    }
                    desc.append(config.getName()).append('>');
                    return new LocationImpl(desc.toString(), result);
                } else {
                    return result;
                }
            }
            
            if (obj instanceof Exception) {
                // Many exceptions in Cocoon have a message like "blah blah at file://foo/bar.xml:12:1"
                String msg = ((Exception)obj).getMessage();
                if (msg == null) return null;
                
                int pos = msg.lastIndexOf(" at ");
                if (pos != -1) {
                    return LocationUtils.parse(msg.substring(pos + 4));
                } else {
                    // Will try other finders
                    return null;
                }
            }
            
            // Try next finders.
            return null;
        }
    };
    
    static {
        LocationUtils.addFinder(confLocFinder);
    }
    
    static Cocoon instance;

    /** The root Cocoon logger */
    private Logger rootLogger;

    /** The application context */
    private Context context;

    /** The configuration file */
    private Source configurationFile;

    /** The logger manager */
    private LoggerManager loggerManager;

    /** The parent service manager. */
    private ServiceManager parentServiceManager;

    /** Flag for disposed or not */
    private boolean disposed;

    /** Active request count */
    private volatile int activeRequestCount;

    /** the Processor */
    private Processor processor;

    /** The source resolver */
    protected SourceResolver sourceResolver;

    /** The environment helper */
    protected EnvironmentHelper environmentHelper;

    /** A service manager */
    protected CocoonServiceManager serviceManager;

    /** An optional Avalon Component that is called before and after processing all requests. */
    protected RequestListener requestListener;

    /** The Cocoon Core */
    protected Core core;

    /** Processor attributes */
    protected Map processorAttributes = new HashMap();

    /**
     * Creates a new <code>Cocoon</code> instance.
     */
    public Cocoon() {
        // Set the system properties needed by Xalan2.
        setSystemProperties();

        // HACK: Provide a way to share an instance of Cocoon object between
        //       several servlets/portlets.
        Cocoon.instance = this;
    }

    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        this.rootLogger = logger;
        super.enableLogging(logger.getChildLogger("cocoon"));
    }

    /**
     * Get the parent service manager. For purposes of
     * avoiding extra method calls, the manager parameter may be null.
     *
     * @param manager the parent component manager. May be <code>null</code>
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.parentServiceManager = manager;
        this.core = (Core)this.parentServiceManager.lookup(Core.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = new ComponentContext(context);
        ((DefaultContext)this.context).makeReadOnly();
    }

    /**
     * The <code>setLoggerManager</code> method will get a <code>LoggerManager</code>
     * for further use.
     *
     * @param loggerManager a <code>LoggerManager</code> value
     */
    public void setLoggerManager(LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        getLogger().debug("Initializing new Cocoon object.");
        final Settings settings = this.core.getSettings();
        try {
            URLSource urlSource = new URLSource();
            urlSource.init(new URL(settings.getConfiguration()), null);
            this.configurationFile = new DelayedRefreshSourceWrapper(urlSource,
                                                                     settings.getReloadDelay("config"));

        } catch (IOException e) {
            throw new ConfigurationException(
                    "Could not open configuration file: " + settings.getConfiguration(), e);
        }

        // Test setup spring container
        System.out.println("Setting up test Spring container");
        AvalonEnvironment env = new AvalonEnvironment();
        env.context = this.context;
        env.core = this.core;
        env.logger = this.getLogger();
        env.servletContext = ((ServletConfig)this.context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG)).getServletContext();
        env.settings = this.core.getSettings();
        ApplicationContext rootContext = ApplicationContextFactory.createRootApplicationContext(env);
        ConfigurationInfo result = ConfigReader.readConfiguration(this.configurationFile.getURI(), env);
        ApplicationContext mainContext = ApplicationContextFactory.createApplicationContext(env, result, rootContext);
        System.out.println("Getting something from mainContext: " + mainContext.getBean(Core.ROLE));
        // END Test setup

        this.serviceManager = new CocoonServiceManager(this.parentServiceManager);
        ContainerUtil.enableLogging(this.serviceManager, this.rootLogger.getChildLogger("manager"));
        ContainerUtil.contextualize(this.serviceManager, this.context);

        // Log the System Properties.
        dumpSystemProperties();

        this.configure();

        // add the logger manager to the component locator

        ContainerUtil.initialize(this.serviceManager);

        // Get the Processor and keep it
        this.processor = (Processor)this.serviceManager.lookup(Processor.ROLE);

        this.environmentHelper = new EnvironmentHelper(
                (URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL));
        ContainerUtil.enableLogging(this.environmentHelper, this.rootLogger);
        ContainerUtil.service(this.environmentHelper, this.serviceManager);

        this.sourceResolver = (SourceResolver)this.serviceManager.lookup(SourceResolver.ROLE);

        if (this.serviceManager.hasService(RequestListener.ROLE)){
            this.requestListener = (RequestListener) this.serviceManager.lookup(RequestListener.ROLE);
        }
        Core.cleanup();
    }

    /** Dump System Properties */
    private void dumpSystemProperties() {
        if (getLogger().isDebugEnabled()) {
            try {
                Enumeration e = System.getProperties().propertyNames();
                getLogger().debug("===== System Properties Start =====");
                for (; e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    getLogger().debug(key + "=" + System.getProperty(key));
                }
                getLogger().debug("===== System Properties End =====");
            } catch (SecurityException se) {
                // Ignore Exceptions.
            }
        }
    }

    /**
     * Configure this <code>Cocoon</code> instance.
     *
     * @exception ConfigurationException if an error occurs
     * @exception ContextException if an error occurs
     */
    private void configure() throws Exception {
        InputSource is = SourceUtil.getInputSource(this.configurationFile);

        final Settings settings = this.core.getSettings();
        ConfigurationBuilder builder = new ConfigurationBuilder(settings);
        Configuration conf = builder.build(is);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Root configuration: " + conf.getName());
        }
        if (!"cocoon".equals(conf.getName())) {
            throw new ConfigurationException("Invalid configuration file\n" + conf.toString());
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configuration version: " + conf.getAttribute("version"));
        }
        if (!Constants.CONF_VERSION.equals(conf.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '" + Constants.CONF_VERSION + "'.");
        }

        RoleManager drm = null;
        String userRoles = conf.getAttribute("user-roles", "");
        if (!"".equals(userRoles)) {
            Configuration roles;
            try {
                org.apache.cocoon.environment.Context context =
                    (org.apache.cocoon.environment.Context) this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
                URL url = context.getResource(userRoles);
                if (url == null) {
                    throw new ConfigurationException("User-roles configuration '"+userRoles+"' cannot be found.");
                }
                is = new InputSource(new BufferedInputStream(url.openStream()));
                is.setSystemId(url.toString());
                roles = builder.build(is);
            } catch (Exception e) {
                throw new ConfigurationException("Error trying to load user-roles configuration", e);
            }

            RoleManager urm = new RoleManager(drm);
            ContainerUtil.enableLogging(urm, this.rootLogger.getChildLogger("roles").getChildLogger("user"));
            ContainerUtil.configure(urm, roles);
            roles = null;
            drm = urm;
        }

        this.serviceManager.setRoleManager(drm);
        this.serviceManager.setLoggerManager(this.loggerManager);

        getLogger().debug("Setting up components...");
        ContainerUtil.configure(this.serviceManager, conf);
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     *
     * @param date a <code>long</code> value
     * @return a <code>boolean</code> value
     */
    public boolean modifiedSince(long date) {
        return date < this.configurationFile.getLastModified();
    }

    /**
     * Helper method to retrieve system property.
     * Returns default value if SecurityException is caught.
     */
    public static String getSystemProperty(String property, String value) {
        try {
            return System.getProperty(property, value);
        } catch (SecurityException e) {
            System.err.println("Caught a SecurityException reading the system property '" + property + "';" +
                               " Cocoon will default to '" + value + "' value.");
            return value;
        }
    }

    /**
     * Sets required system properties.
     */
    protected void setSystemProperties() {
        try {
            // FIXME We shouldn't have to specify the SAXParser...
            // This is needed by Xalan2, it is used by org.xml.sax.helpers.XMLReaderFactory
            // to locate the SAX2 driver.
            if (getSystemProperty("org.xml.sax.driver", null) == null) {
                System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
            }
        } catch (SecurityException e) {
            // Ignore security exceptions
            System.out.println("Caught a SecurityException writing the system property: " + e);
        }

        try {
            // FIXME We shouldn't have to specify these. Needed to override jaxp implementation of weblogic.
            if (getSystemProperty("javax.xml.parsers.DocumentBuilderFactory", "").startsWith("weblogic")) {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
                System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
            }
        } catch (SecurityException e) {
            // Ignore security exceptions
            System.out.println("Caught a SecurityException writing the system property: " + e);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.serviceManager != null) {
            this.serviceManager.release(this.requestListener);
            this.requestListener = null;

            this.serviceManager.release(this.processor);
            this.processor = null;

            this.serviceManager.release(this.sourceResolver);
            this.sourceResolver = null;

            ContainerUtil.dispose(this.serviceManager);
            this.serviceManager = null;
        }
        if ( this.parentServiceManager != null ) {
            this.parentServiceManager.release(this.core);
            this.core = null;
            this.parentServiceManager = null;
        }
        this.context = null;
        if (Cocoon.instance == this) {
            Cocoon.instance = null;
        }
        this.disposed = true;
    }

    /**
     * Log debug information about the current environment.
     *
     * @param environment an <code>Environment</code> value
     */
    protected void debug(Environment environment, boolean internal) {
        String lineSeparator = SystemUtils.LINE_SEPARATOR;
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        StringBuffer msg = new StringBuffer();
        msg.append("DEBUGGING INFORMATION:").append(lineSeparator);
        if (internal) {
            msg.append("INTERNAL ");
        }
        msg.append("REQUEST: ").append(request.getRequestURI()).append(lineSeparator).append(lineSeparator);
        msg.append("CONTEXT PATH: ").append(request.getContextPath()).append(lineSeparator);
        msg.append("SERVLET PATH: ").append(request.getServletPath()).append(lineSeparator);
        msg.append("PATH INFO: ").append(request.getPathInfo()).append(lineSeparator).append(lineSeparator);

        msg.append("REMOTE HOST: ").append(request.getRemoteHost()).append(lineSeparator);
        msg.append("REMOTE ADDRESS: ").append(request.getRemoteAddr()).append(lineSeparator);
        msg.append("REMOTE USER: ").append(request.getRemoteUser()).append(lineSeparator);
        msg.append("REQUEST SESSION ID: ").append(request.getRequestedSessionId()).append(lineSeparator);
        msg.append("REQUEST PREFERRED LOCALE: ").append(request.getLocale().toString()).append(lineSeparator);
        msg.append("SERVER HOST: ").append(request.getServerName()).append(lineSeparator);
        msg.append("SERVER PORT: ").append(request.getServerPort()).append(lineSeparator).append(lineSeparator);

        msg.append("METHOD: ").append(request.getMethod()).append(lineSeparator);
        msg.append("CONTENT LENGTH: ").append(request.getContentLength()).append(lineSeparator);
        msg.append("PROTOCOL: ").append(request.getProtocol()).append(lineSeparator);
        msg.append("SCHEME: ").append(request.getScheme()).append(lineSeparator);
        msg.append("AUTH TYPE: ").append(request.getAuthType()).append(lineSeparator).append(lineSeparator);
        msg.append("CURRENT ACTIVE REQUESTS: ").append(activeRequestCount).append(lineSeparator);

        // log all of the request parameters
        Enumeration e = request.getParameterNames();

        msg.append("REQUEST PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e.hasMoreElements()) {
            String p = (String) e.nextElement();

            msg.append("PARAM: '").append(p).append("' ")
               .append("VALUES: '");
            String[] params = request.getParameterValues(p);
            for (int i = 0; i < params.length; i++) {
                msg.append("[" + params[i] + "]");
                if (i != (params.length - 1)) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        // log all of the header parameters
        Enumeration e2 = request.getHeaderNames();

        msg.append("HEADER PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e2.hasMoreElements()) {
            String p = (String) e2.nextElement();

            msg.append("PARAM: '").append(p).append("' ")
               .append("VALUES: '");
            Enumeration e3 = request.getHeaders(p);
            while (e3.hasMoreElements()) {
                msg.append("[" + e3.nextElement() + "]");
                if (e3.hasMoreElements()) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        msg.append(lineSeparator).append("SESSION ATTRIBUTES:").append(lineSeparator).append(lineSeparator);

        // log all of the session attributes
        if (session != null) {
            // Fix bug #12139: Session can be modified while still
            // being enumerated here
            synchronized (session) {
                e = session.getAttributeNames();
                while (e.hasMoreElements()) {
                    String p = (String) e.nextElement();
                    msg.append("PARAM: '").append(p).append("' ")
                       .append("VALUE: '").append(session.getAttribute(p)).append("'")
                       .append(lineSeparator);
                }
            }
        }

        getLogger().debug(msg.toString());
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment)
    throws Exception {

        if (this.disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        environment.startingProcessing();
        final int environmentDepth = EnvironmentHelper.markEnvironment();
        EnvironmentHelper.enterProcessor(this, this.serviceManager, environment);
        try {
            boolean result;
            if (getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                debug(environment, false);
            }


            if (this.requestListener != null) {
                try {
                    requestListener.onRequestStart(environment);
                } catch (Exception e) {
                    getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                }
            }

            result = this.processor.process(environment);

            if (this.requestListener != null) {
                try {
                    requestListener.onRequestEnd(environment);
                } catch (Exception e) {
                    getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                }
            }

            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            if (this.requestListener != null) {
                try {
                    requestListener.onRequestException(environment, any);
                } catch (Exception e) {
                    getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                }
            }
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            EnvironmentHelper.leaveProcessor();
            environment.finishingProcessing();
            if (getLogger().isDebugEnabled()) {
                --activeRequestCount;
            }
            Core.cleanup();

            EnvironmentHelper.checkEnvironment(environmentDepth, getLogger());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#buildPipeline(org.apache.cocoon.environment.Environment)
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {
        if (disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        try {
            if (getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                debug(environment, true);
            }

            return this.processor.buildPipeline(environment);

        } finally {
            if (getLogger().isDebugEnabled()) {
                --activeRequestCount;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Configuration[] getComponentConfigurations() {
        return null;
    }

    /**
     * Return this (Cocoon is always at the root of the processing chain).
     * @since 2.1.1
     */
    public Processor getRootProcessor() {
        return this;
    }

    /**
     * Accessor for active request count
     */
    public int getActiveRequestCount() {
        return activeRequestCount;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getEnvironmentHelper()
     */
    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return this.environmentHelper;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return this.environmentHelper.getContext();
    }

    /**
     * FIXME -  Do we really need this method?
     */
    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.processorAttributes.get(name);
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.processorAttributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.processorAttributes.put(name, value);
    }

}
