/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

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
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ComponentContext;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.ClassUtils;

import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.command.CommandManager;
import org.apache.excalibur.event.command.TPCThreadManager;
import org.apache.excalibur.event.command.ThreadManager;
import org.apache.excalibur.instrument.InstrumentManageable;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.URLSource;
import org.apache.excalibur.xml.impl.XercesParser;
import org.apache.excalibur.xml.sax.SAXParser;

import org.xml.sax.InputSource;

/**
 * The Cocoon Object is the main Kernel for the entire Cocoon system.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a> (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: Cocoon.java,v 1.23 2004/03/10 12:58:09 stephan Exp $
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

    private ThreadManager threads;

    private CommandManager commands;
    
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

    /** flag for disposed or not */
    private boolean disposed = false;

    /** active request count */
    private volatile int activeRequestCount = 0;

    /** the Processor if it is ThreadSafe */
    private Processor threadSafeProcessor;

    /** The source resolver */
    protected SourceResolver sourceResolver;
    
    /**
     * Creates a new <code>Cocoon</code> instance.
     *
     * @exception ConfigurationException if an error occurs
     */
    public Cocoon() throws ConfigurationException {
        // Set the system properties needed by Xalan2.
        setSystemProperties();
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
            
            try {
                DefaultContext setup = (DefaultContext)this.context;
                this.threads = new TPCThreadManager();
                
                Parameters params = new Parameters();
                params.setParameter("threads-per-processor", "1");
                params.setParameter("sleep-time", "100");
                params.setParameter("block-timeout", "1000");
                params.setParameter("force-shutdown", "true");
                params.makeReadOnly();
                
                ContainerUtil.enableLogging(this.threads, getLogger().getChildLogger("thread.manager"));
                ContainerUtil.parameterize(this.threads, params);
                ContainerUtil.initialize(this.threads);
                
                this.commands = new CommandManager();
                ContainerUtil.enableLogging(this.commands, getLogger().getChildLogger("thread.manager"));
                this.threads.register(this.commands);
                
                setup.put(Queue.ROLE, this.commands.getCommandSink());
                
                setup.makeReadOnly();
            } catch (Exception e) {
                getLogger().error("Could not set up the Command Manager", e);
            }
            
            this.classpath = (String)context.get(Constants.CONTEXT_CLASSPATH);
            this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
            try {
                // FIXME : add a configuration option for the refresh delay.
                // for now, hard-coded to 1 second.
                URLSource urlSource = new URLSource();
                urlSource.init((URL)context.get(Constants.CONTEXT_CONFIG_URL), null);
                this.configurationFile = new DelayedRefreshSourceWrapper(urlSource,
                    1000L
                );

            } catch (IOException ioe) {
                throw new ContextException("Could not open configuration file.", ioe);
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
        if (parentComponentManager != null) {
            this.componentManager = new CocoonComponentManager(parentComponentManager,(ClassLoader)this.context.get(Constants.CONTEXT_CLASS_LOADER));
        } else {
            this.componentManager = new CocoonComponentManager((ClassLoader)this.context.get(Constants.CONTEXT_CLASS_LOADER));
        }
        ContainerUtil.enableLogging(this.componentManager, getLogger().getChildLogger("manager"));
        ContainerUtil.contextualize(this.componentManager, this.context);
        this.componentManager.setInstrumentManager(this.instrumentManager);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New Cocoon object.");
        }

        // Log the System Properties.
        dumpSystemProperties();

        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        // first check for deprecated property to be compatible:
        String parser = System.getProperty(Constants.DEPRECATED_PARSER_PROPERTY, Constants.DEFAULT_PARSER);
        if ( !Constants.DEFAULT_PARSER.equals( parser ) ) {
            getLogger().warn("Deprecated property " +
                             Constants.DEPRECATED_PARSER_PROPERTY + " is used. Please use " +
                             Constants.PARSER_PROPERTY + " instead.");
            if ( "org.apache.cocoon.components.parser.XercesParser".equals(parser) ) {
                parser = XercesParser.class.getName();
            } else {
                getLogger().warn("Unknown value for deprecated property: " +
                                 Constants.DEPRECATED_PARSER_PROPERTY + ", value: " + parser +
                                 ". If you experience problems during startup, check the parser configuration section of the documentation.");
            }
        } else {
            parser = System.getProperty(Constants.PARSER_PROPERTY, Constants.DEFAULT_PARSER);
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using parser: " + parser);
            getLogger().debug("Classpath = " + classpath);
            getLogger().debug("Work directory = " + workDir.getCanonicalPath());
        }

        ExcaliburComponentManager startupManager = new ExcaliburComponentManager((ClassLoader)this.context.get(Constants.CONTEXT_CLASS_LOADER));
        ContainerUtil.enableLogging(startupManager, getLogger().getChildLogger("startup"));
        ContainerUtil.contextualize(startupManager, this.context);
        startupManager.setLoggerManager(this.loggerManager);

        try {
            startupManager.addComponent(SAXParser.ROLE, ClassUtils.loadClass(parser), new DefaultConfiguration("", "empty"));
        } catch (Exception e) {
            throw new ConfigurationException("Could not load parser " + parser, e);
        }
        
        ContainerUtil.initialize(startupManager);
        
        this.configure(startupManager);
        
        ContainerUtil.dispose(startupManager);
        startupManager = null;

        // add the logger manager to the component locator
        final ComponentProxyGenerator proxyGenerator = new ComponentProxyGenerator();
        final Component loggerManagerProxy = proxyGenerator.getProxy(LoggerManager.class.getName(),loggerManager);
        componentManager.addComponentInstance(LoggerManager.ROLE,loggerManagerProxy);

        ContainerUtil.initialize(this.componentManager);

        // Get the Processor and keep it if it's ThreadSafe
        Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
        if (processor instanceof ThreadSafe) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processor of class " + processor.getClass().getName() + " is ThreadSafe");
            }
            this.threadSafeProcessor = processor;
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processor of class " + processor.getClass().getName() +
                " is NOT ThreadSafe -- will be looked up at each request");
            }
            this.componentManager.release(processor);
        }

        this.sourceResolver = (SourceResolver)this.componentManager.lookup(SourceResolver.ROLE);
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
     * @param startupManager an <code>ExcaliburComponentManager</code> value
     * @exception ConfigurationException if an error occurs
     * @exception ContextException if an error occurs
     */
    public void configure(ExcaliburComponentManager startupManager) throws ConfigurationException, ContextException {
        SAXParser p = null;
        Configuration roleConfig = null;

        try {
            this.configurationFile.refresh();
            p = (SAXParser)startupManager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler b = new SAXConfigurationHandler();
            InputStream inputStream = ClassUtils.getResource("org/apache/cocoon/cocoon.roles").openStream();
            InputSource is = new InputSource(inputStream);
            is.setSystemId(this.configurationFile.getURI());
            p.parse(is, b);
            roleConfig = b.getConfiguration();
        } catch (Exception e) {
            throw new ConfigurationException("Error trying to load configurations", e);
        } finally {
            if (p != null) startupManager.release((Component)p);
        }

        DefaultRoleManager drm = new DefaultRoleManager();
        ContainerUtil.enableLogging(drm, getLogger().getChildLogger("roles"));
        ContainerUtil.configure(drm, roleConfig);
        roleConfig = null;

        try {
            p = (SAXParser)startupManager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler b = new SAXConfigurationHandler();
            InputSource is = SourceUtil.getInputSource(this.configurationFile);
            p.parse(is, b);
            this.configuration = b.getConfiguration();
        } catch (Exception e) {
            throw new ConfigurationException("Error trying to load configurations",e);
        } finally {
            if (p != null) startupManager.release((Component)p);
        }

        Configuration conf = this.configuration;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Root configuration: " + conf.getName());
        }
        if (! "cocoon".equals(conf.getName())) {
            throw new ConfigurationException("Invalid configuration file\n" + conf.toString());
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configuration version: " + conf.getAttribute("version"));
        }
        if (!Constants.CONF_VERSION.equals(conf.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '" + Constants.CONF_VERSION + "'.");
        }

        String userRoles = conf.getAttribute("user-roles", "");
        if (!"".equals(userRoles)) {
            try {
                p = (SAXParser)startupManager.lookup(SAXParser.ROLE);
                SAXConfigurationHandler b = new SAXConfigurationHandler();
                org.apache.cocoon.environment.Context context =
                    (org.apache.cocoon.environment.Context) this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
                URL url = context.getResource(userRoles);
                if (url == null) {
                    throw new ConfigurationException("User-roles configuration '"+userRoles+"' cannot be found.");
                }
                InputSource is = new InputSource(new BufferedInputStream(url.openStream()));
                is.setSystemId(this.configurationFile.getURI());
                p.parse(is, b);
                roleConfig = b.getConfiguration();
            } catch (Exception e) {
                throw new ConfigurationException("Error trying to load user-roles configuration", e);
            } finally {
                startupManager.release((Component)p);
            }

            DefaultRoleManager urm = new DefaultRoleManager(drm);
            ContainerUtil.enableLogging(urm, getLogger().getChildLogger("roles").getChildLogger("user"));
            ContainerUtil.configure(urm, roleConfig);
            roleConfig = null;
            drm = urm;
        }

        this.componentManager.setRoleManager(drm);
        this.componentManager.setLoggerManager(this.loggerManager);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setting up components...");
        }
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
     * Sets required system properties.
     */
    protected void setSystemProperties() {
        java.util.Properties props = new java.util.Properties();
        // FIXME We shouldn't have to specify the SAXParser...
        // This is needed by Xalan2, it is used by org.xml.sax.helpers.XMLReaderFactory
        // to locate the SAX2 driver.
        props.put("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
        java.util.Properties systemProps = System.getProperties();
        Enumeration propEnum = props.propertyNames();
        while (propEnum.hasMoreElements()) {
            String prop = (String)propEnum.nextElement();
            if (!systemProps.containsKey(prop)) {
                systemProps.put(prop, props.getProperty(prop));
            }
        }
        // FIXME We shouldn't have to specify these. Needed to override jaxp implementation of weblogic.
        if (systemProps.containsKey("javax.xml.parsers.DocumentBuilderFactory") &&
            systemProps.getProperty("javax.xml.parsers.DocumentBuilderFactory").startsWith("weblogic")) {
            systemProps.put("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            systemProps.put("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
        }
        System.setProperties(systemProps);
    }

    /**
     * Dispose this instance
     */
    public void dispose() {
        if (this.commands != null && this.threads != null ) {
            this.threads.deregister(this.commands);
        }
        ContainerUtil.dispose(this.commands);
        this.commands = null;
        ContainerUtil.dispose(this.threads);
        this.threads = null;
        
        if ( this.componentManager != null ) {
            this.componentManager.release(this.threadSafeProcessor);
            this.threadSafeProcessor = null;
            
            this.componentManager.release(this.sourceResolver);
            this.sourceResolver = null;

            ContainerUtil.dispose(this.componentManager);
            this.componentManager = null;            
        }
        
        this.context = null;
        this.disposed = true;
    }

    /**
     * Log debug information about the current environment.
     *
     * @param environment an <code>Environment</code> value
     */
    protected void debug(Environment environment, boolean internal) {
        String lineSeparator = System.getProperty("line.separator");
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
        CocoonComponentManager.enterEnvironment(environment,
                                                this.componentManager,
                                                this);
        try {
            boolean result;
            if (getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                this.debug(environment, false);
            }

            if (this.threadSafeProcessor != null) {
                result = this.threadSafeProcessor.process(environment);
            } else {
                Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
                try {
                    result = processor.process(environment);
                }
                finally {
                    this.componentManager.release(processor);
                }
            }
            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            CocoonComponentManager.leaveEnvironment();
            CocoonComponentManager.endProcessing(environment, key);
            if (getLogger().isDebugEnabled()) {
                --activeRequestCount;
            }
            
            // TODO (CZ): This is only for testing - remove it later on
            CocoonComponentManager.checkEnvironment(getLogger());
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {
        if (disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        try {
            if (getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                this.debug(environment, true);
            }

            if (this.threadSafeProcessor != null) {
                return this.threadSafeProcessor.buildPipeline(environment);
            } else {
                Processor processor = (Processor)this.componentManager.lookup(Processor.ROLE);
                try {
                    return processor.buildPipeline(environment);
                }
                finally {
                    this.componentManager.release(processor);
                }
            }

        } finally {
            if (getLogger().isDebugEnabled()) {
                --activeRequestCount;
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
        return activeRequestCount;
    }
    
    public ExcaliburComponentManager getComponentManager() {
        return this.componentManager;
    }
}

