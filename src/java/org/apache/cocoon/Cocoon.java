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
package org.apache.cocoon;

import org.apache.avalon.excalibur.component.ComponentProxyGenerator;
import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ComponentContext;
import org.apache.cocoon.components.PropertyAwareSAXConfigurationHandler;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.Deprecation;
import org.apache.cocoon.util.SimpleSourceResolver;
import org.apache.cocoon.util.Settings;
import org.apache.cocoon.util.SettingsHelper;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;

import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.URLSource;
import org.apache.excalibur.xml.impl.XercesParser;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ConcurrentModificationException;

/**
 * The Cocoon Object is the main Kernel for the entire Cocoon system.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a> (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id$
 */
public class Cocoon
        extends AbstractLogEnabled
        implements ThreadSafe,
                   Component,
                   Initializable,
                   Disposable,
                   Modifiable,
                   Processor,
                   Contextualizable,
                   Composable,
                   InstrumentManageable {

    // Register the location finder for Avalon configuration objects and exceptions
    // and keep a strong reference to it.
    private static final LocationUtils.LocationFinder LOCATION_FINDER = new LocationUtils.LocationFinder() {
        public Location getLocation(Object obj, String description) {
            if (obj instanceof Configuration) {
                Configuration config = (Configuration) obj;
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
                if (msg == null) {
                    return null;
                }

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
        LocationUtils.addFinder(LOCATION_FINDER);
    }

    static Cocoon instance;

    /** The root Cocoon logger */
    private Logger rootLogger;

    /** The application context */
    private Context context;

    /** The configuration file */
    private Source configurationFile;

    /** The configuration tree */
    private Configuration configuration;

    /** The logger manager */
    private LoggerManager loggerManager;

    /** The instrument manager */
    private InstrumentManager instrumentManager;

    /** The classpath (null if not available) */
    private String classpath;

    /** The working directory (null if not available) */
    private File workDir;

    /** The component manager. */
    private ExcaliburComponentManager componentManager;

    /** The parent component manager. */
    private ComponentManager parentComponentManager;

    /** Flag for disposed or not */
    private boolean disposed;

    /** Active request count */
    private volatile int activeRequestCount;

    /** The Processor if it is ThreadSafe */
    private Processor threadSafeProcessor;

    /** The source resolver */
    protected SourceResolver sourceResolver;

    /** An optional Avalon Component that is called before and after processing all requests. */
    protected RequestListener requestListener;

    /**
     * Creates a new <code>Cocoon</code> instance.
     *
     * @exception ConfigurationException if an error occurs
     */
    public Cocoon() throws ConfigurationException {
        // Set the system properties needed by Xalan2.
        this.setSystemProperties();

        // HACK: Provide a way to share an instance of Cocoon object between
        //       several servlets/portlets.
        Cocoon.instance = this;
    }

    public void enableLogging(Logger logger) {
        this.rootLogger = logger;
        super.enableLogging(logger.getChildLogger("cocoon"));
    }

    /**
     * Get the parent component manager. For purposes of
     * avoiding extra method calls, the manager parameter may be null.
     *
     * @param manager the parent component manager. May be <code>null</code>
     */
    public void compose(ComponentManager manager)
    throws ComponentException {
        this.parentComponentManager = manager;
    }

    /**
     * Describe <code>contextualize</code> method here.
     *
     * @param context a <code>Context</code> value
     * @exception ContextException if an error occurs
     */
    public void contextualize(Context context) throws ContextException {
        if (this.context == null) {
            this.context = new ComponentContext(context);
            SettingsHelper.createSettings((DefaultContext)this.context, this.getLogger());
            ((DefaultContext) this.context).makeReadOnly();

            this.classpath = (String)context.get(Constants.CONTEXT_CLASSPATH);
            this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
            try {
                // FIXME: add a configuration option for the refresh delay.
                // for now, hard-coded to 1 second.
                URLSource urlSource = new URLSource();
                urlSource.init((URL) context.get(Constants.CONTEXT_CONFIG_URL), null);
                this.configurationFile = new DelayedRefreshSourceWrapper(urlSource,
                                                                         1000L);

            } catch (IOException e) {
                throw new ContextException("Could not open configuration file.", e);
            } catch (Exception e) {
                throw new ContextException("contextualize(..) Exception", e);
            }
        }
    }

    /**
     * The <code>setLoggerManager</code> method will get a <code>LoggerManager</code>
     * for further use.
     *
     * @param loggerManager a <code>LoggerManager</code> value
     */
    public void setLoggerManager(LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
        Deprecation.setLogger(this.loggerManager.getLoggerForCategory("deprecation"));
    }

    /**
     * Set the <code>InstrumentManager</code> for this Cocoon instance.
     *
     * @param manager an <code>InstrumentManager</code> instance
     */
    public void setInstrumentManager(final InstrumentManager manager) {
        this.instrumentManager = manager;
    }

    /**
     * The <code>initialize</code> method
     *
     * @exception Exception if an error occurs
     */
    public void initialize() throws Exception {
        if (this.parentComponentManager != null) {
            this.componentManager = new CocoonComponentManager(this.parentComponentManager,
                                                               (ClassLoader) this.context.get(Constants.CONTEXT_CLASS_LOADER));
        } else {
            this.componentManager = new CocoonComponentManager((ClassLoader) this.context.get(Constants.CONTEXT_CLASS_LOADER));
        }
        ContainerUtil.enableLogging(this.componentManager, this.rootLogger.getChildLogger("manager"));
        ContainerUtil.contextualize(this.componentManager, this.context);
        this.componentManager.setInstrumentManager(this.instrumentManager);
        this.getLogger().debug("New Cocoon object.");

        // Log the System Properties.
        this.dumpSystemProperties();

        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        // first check for deprecated property to be compatible:
        String parser = getSystemProperty(Constants.DEPRECATED_PARSER_PROPERTY, Constants.DEFAULT_PARSER);
        if (!Constants.DEFAULT_PARSER.equals(parser)) {
            this.getLogger().warn("Deprecated property " +
                             Constants.DEPRECATED_PARSER_PROPERTY + " is used. Please use " +
                             Constants.PARSER_PROPERTY + " instead.");
            if ("org.apache.cocoon.components.parser.XercesParser".equals(parser)) {
                parser = XercesParser.class.getName();
            } else {
                this.getLogger().warn("Unknown value for deprecated property: " +
                                 Constants.DEPRECATED_PARSER_PROPERTY + ", value: " + parser +
                                 ". If you experience problems during startup, check the parser configuration section of the documentation.");
            }
        } else {
            parser = getSystemProperty(Constants.PARSER_PROPERTY, Constants.DEFAULT_PARSER);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Parser: " + parser);
            this.getLogger().debug("Classpath: " + this.classpath);
            this.getLogger().debug("Work directory: " + this.workDir.getCanonicalPath());
        }

        ExcaliburComponentManager startupManager = new ExcaliburComponentManager((ClassLoader) this.context.get(Constants.CONTEXT_CLASS_LOADER));
        ContainerUtil.enableLogging(startupManager, this.rootLogger.getChildLogger("startup"));
        ContainerUtil.contextualize(startupManager, this.context);
        startupManager.setLoggerManager(this.loggerManager);

        try {
            startupManager.addComponent(SAXParser.ROLE,
                                        ClassUtils.loadClass(parser),
                                        new DefaultConfiguration("", "empty"));
        } catch (Exception e) {
            throw new ConfigurationException("Could not load parser " + parser, e);
        }

        ContainerUtil.initialize(startupManager);
        this.configure(startupManager);
        ContainerUtil.dispose(startupManager);
        startupManager = null;

        // add the logger manager to the component locator
        final ComponentProxyGenerator proxyGenerator = new ComponentProxyGenerator();
        final Component loggerManagerProxy = proxyGenerator.getProxy(LoggerManager.class.getName(),this.loggerManager);
        this.componentManager.addComponentInstance(LoggerManager.ROLE,loggerManagerProxy);

        ContainerUtil.initialize(this.componentManager);

        // Get the Processor and keep it if it's ThreadSafe
        Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
        if (processor instanceof ThreadSafe) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Processor of class " + processor.getClass().getName() +
                                  " is ThreadSafe");
            }
            this.threadSafeProcessor = processor;
        } else {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Processor of class " + processor.getClass().getName() +
                                  " is NOT ThreadSafe -- will be looked up at each request");
            }
            this.componentManager.release(processor);
        }

        this.sourceResolver = (SourceResolver)this.componentManager.lookup(SourceResolver.ROLE);

        if (this.componentManager.hasComponent(RequestListener.ROLE)){
            this.requestListener = (RequestListener) this.componentManager.lookup(RequestListener.ROLE);
        }
    }

    /** Dump System Properties */
    private void dumpSystemProperties() {
        if (this.getLogger().isDebugEnabled()) {
            try {
                Enumeration e = System.getProperties().propertyNames();
                this.getLogger().debug("===== System Properties Start =====");
                for (; e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    this.getLogger().debug(key + "=" + System.getProperty(key));
                }
                this.getLogger().debug("===== System Properties End =====");
            } catch (SecurityException se) {
                // Ignore Exceptions.
            }
        }
    }

    /**
     * Configure this <code>Cocoon</code> instance.
     *
     * @param startupManager an <code>ExcaliburComponentManager</code> value
     * @exception ConfigurationException if an error occurs
     * @exception ContextException if an error occurs
     */
    public void configure(ExcaliburComponentManager startupManager) throws ConfigurationException, ContextException {
        SAXParser p = null;
        Settings settings = SettingsHelper.getSettings(this.context);

        Configuration roles = null;
        try {
            p = (SAXParser) startupManager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler b = new PropertyAwareSAXConfigurationHandler(settings, this.getLogger());
            URL url = ClassUtils.getResource("org/apache/cocoon/cocoon.roles");
            InputSource is = new InputSource(url.openStream());
            is.setSystemId(url.toString());
            p.parse(is, b);
            roles = b.getConfiguration();
        } catch (Exception e) {
            throw new ConfigurationException("Error trying to load configurations", e);
        } finally {
            if (p != null) startupManager.release((Component) p);
        }

        DefaultRoleManager drm = new DefaultRoleManager();
        ContainerUtil.enableLogging(drm, this.rootLogger.getChildLogger("roles"));
        ContainerUtil.configure(drm, roles);
        roles = null;

        try {
            this.configurationFile.refresh();
            p = (SAXParser)startupManager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler b = new PropertyAwareSAXConfigurationHandler(settings, this.getLogger());
            InputSource is = SourceUtil.getInputSource(this.configurationFile);
            p.parse(is, b);
            this.configuration = b.getConfiguration();
        } catch (Exception e) {
            throw new ConfigurationException("Error trying to load configurations",e);
        } finally {
            if (p != null) startupManager.release((Component)p);
        }

        Configuration conf = this.configuration;
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Root configuration: " + conf.getName());
        }
        if (!"cocoon".equals(conf.getName())) {
            throw new ConfigurationException("Invalid configuration file\n" + conf.toString());
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Configuration version: " + conf.getAttribute("version"));
        }
        if (!Constants.CONF_VERSION.equals(conf.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '" + Constants.CONF_VERSION + "'.");
        }

        String userRoles = conf.getAttribute("user-roles", "");
        if (!"".equals(userRoles)) {
            try {
                p = (SAXParser)startupManager.lookup(SAXParser.ROLE);
                SAXConfigurationHandler b = new PropertyAwareSAXConfigurationHandler(settings, this.getLogger());
                org.apache.cocoon.environment.Context context =
                    (org.apache.cocoon.environment.Context) this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
                URL url = context.getResource(userRoles);
                if (url == null) {
                    throw new ConfigurationException("User-roles configuration '"+userRoles+"' cannot be found.");
                }
                InputSource is = new InputSource(new BufferedInputStream(url.openStream()));
                is.setSystemId(url.toString());
                p.parse(is, b);
                roles = b.getConfiguration();
            } catch (Exception e) {
                throw new ConfigurationException("Error trying to load user-roles configuration", e);
            } finally {
                startupManager.release((Component)p);
            }

            DefaultRoleManager urm = new DefaultRoleManager(drm);
            ContainerUtil.enableLogging(urm, this.rootLogger.getChildLogger("roles").getChildLogger("user"));
            ContainerUtil.configure(urm, roles);
            roles = null;
            drm = urm;
        }

        this.componentManager.setRoleManager(drm);
        this.componentManager.setLoggerManager(this.loggerManager);

        this.getLogger().debug("Setting up components...");
        ContainerUtil.configure(this.componentManager, conf);
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
     * Dispose this instance
     */
    public void dispose() {
        if (this.componentManager != null) {
            if (this.requestListener != null) {
                this.componentManager.release(this.requestListener);
            }
            this.componentManager.release(this.threadSafeProcessor);
            this.threadSafeProcessor = null;

            this.componentManager.release((Component)this.sourceResolver);
            this.sourceResolver = null;

            ContainerUtil.dispose(this.componentManager);
            this.componentManager = null;
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
        StringBuffer msg = new StringBuffer(2048);
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
        msg.append("CURRENT ACTIVE REQUESTS: ").append(this.activeRequestCount).append(lineSeparator);

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
            StringBuffer buffer = new StringBuffer("");
            int count = -1;
            while (count <= 0) {
                // Fix bug #12139: Session can be modified while still
                // being enumerated here
                try {
                    e = session.getAttributeNames();
                    while (e.hasMoreElements()) {
                        String p = (String) e.nextElement();
                        buffer.append("PARAM: '").append(p).append("' ")
                       .append("VALUE: '").append(session.getAttribute(p)).append("'")
                       .append(lineSeparator);
                    }
                    break;
                } catch (ConcurrentModificationException ex) {
                    buffer = new StringBuffer("");
                    ++count;
                }

            }
            msg.append(buffer.toString());
        }

        this.getLogger().debug(msg.toString());
    }

    /**
     * Process the given <code>Environment</code> to produce the output.
     *
     * @param environment an <code>Environment</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean process(Environment environment)
    throws Exception {
        if (this.disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        Object key = CocoonComponentManager.startProcessing(environment);
        final int environmentDepth = CocoonComponentManager.markEnvironment();
        CocoonComponentManager.enterEnvironment(environment,
                                                this.componentManager,
                                                this);
        try {
            boolean result;
            if (this.getLogger().isDebugEnabled()) {
                ++this.activeRequestCount;
                this.debug(environment, false);
            }


            if (this.requestListener != null) {
                try {
                    this.requestListener.onRequestStart(environment);
                } catch (Exception e) {
                    this.getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                }
            }

            if (this.threadSafeProcessor != null) {
                result = this.threadSafeProcessor.process(environment);
                if (this.requestListener != null) {
                    try {
                        this.requestListener.onRequestEnd(environment);
                    } catch (Exception e) {
                        this.getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                    }
                }
            } else {
                Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
                try {
                    result = processor.process(environment);
                    if (this.requestListener != null) {
                        try {
                            this.requestListener.onRequestEnd(environment);
                        } catch (Exception e) {
                            this.getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                        }
                    }
                } finally {
                    this.componentManager.release(processor);
                }
            }
            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            if (this.requestListener != null) {
                try {
                    this.requestListener.onRequestException(environment, any);
                } catch (Exception e) {
                    this.getLogger().error("Error encountered monitoring request start: " + e.getMessage());
                }
            }
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            CocoonComponentManager.leaveEnvironment();
            CocoonComponentManager.endProcessing(environment, key);
            if (this.getLogger().isDebugEnabled()) {
                --this.activeRequestCount;
            }

            // TODO (CZ): This is only for testing - remove it later on
            CocoonComponentManager.checkEnvironment(environmentDepth, this.getLogger());
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {
        if (this.disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        try {
            if (this.getLogger().isDebugEnabled()) {
                ++this.activeRequestCount;
                this.debug(environment, true);
            }

            if (this.threadSafeProcessor != null) {
                return this.threadSafeProcessor.buildPipeline(environment);
            } else {
                Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
                try {
                    return processor.buildPipeline(environment);
                } finally {
                    this.componentManager.release(processor);
                }
            }

        } finally {
            if (this.getLogger().isDebugEnabled()) {
                --this.activeRequestCount;
            }
        }
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Map getComponentConfigurations() {
        return Collections.EMPTY_MAP;
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
        return this.activeRequestCount;
    }

    public ExcaliburComponentManager getComponentManager() {
        return this.componentManager;
    }

        /**
     * Create a simple source resolver.
     */
    protected SourceResolver createSourceResolver(Logger logger) throws ContextException {
        // Create our own resolver
        final SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(this.context);
        } catch (ContextException ce) {
            throw new ContextException(
                    "Cannot setup source resolver.", ce);
        }
        return resolver;
    }
}
