/*
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.CoreResourceNotFoundException;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.handler.AbstractComponentHandler;
import org.apache.cocoon.core.container.handler.AliasComponentHandler;
import org.apache.cocoon.core.container.handler.ComponentHandler;
import org.apache.cocoon.core.container.handler.InstanceComponentHandler;
import org.apache.cocoon.core.container.handler.LazyHandler;
import org.apache.cocoon.core.container.util.ConfigurationBuilder;
import org.apache.cocoon.core.container.util.SimpleSourceResolver;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.util.JMXUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * Default service manager for Cocoon's components.
 *
 * @version $Id$
 * @since 2.2
 */
public class CoreServiceManager
        extends AbstractLogEnabled
        implements Contextualizable, ThreadSafe, Disposable, Initializable, ServiceManager, Configurable, RoleManagerOwner {

    /** The attribute containing the JMX domain name */
    public static final String JMX_DOMAIN_ATTR_NAME = "jmx-domain";

    /** The attribute containing the JMX domain name */
    public static final String JMX_NAME_ATTR_NAME = "jmx-name";

    /** The attribute containing the JMX domain name */
    public static final String JMX_DEFAULT_DOMAIN_NAME = "Cocoon";
    
    /**
     * An empty configuration object, that can be used when no configuration is known but one
     * is needed.
     */
    public static final Configuration EMPTY_CONFIGURATION = new DefaultConfiguration("-", "unknown location");

    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /** The application context for components */
    protected Context context;

    /** Static component mapping handlers. */
    protected final Map componentMapping = Collections.synchronizedMap(new HashMap());

    /** Used to map roles to ComponentHandlers. */
    protected final Map componentHandlers = Collections.synchronizedMap(new HashMap());

    /** Is the Manager disposed or not? */
    protected boolean disposed;

    /** Is the Manager initialized? */
    protected boolean initialized;

    /** RoleInfos. */
    protected RoleManager roleManager;

    /** LoggerManager. */
    protected LoggerManager loggerManager;

    protected ComponentEnvironment componentEnv;

    /** The settings */
    private Settings settings;

    /** The location where this manager is defined */
    protected String location;

    /** The parent ServiceManager */
    protected ServiceManager parentManager;

    /** The classloader to get classes from */
    protected ClassLoader classloader;

    /** The resolver used to resolve includes. It is lazily loaded in {@link #setupSourceResolver()}. */
    private SourceResolver cachedSourceResolver;

    private String jmxDefaultDomain = null;
    
    /** Create the ServiceManager with a parent ServiceManager */
    public CoreServiceManager( final ServiceManager parent ) {
        this(parent, null);
    }

    /** Create the ServiceManager with a parent ServiceManager and a ClassLoader */
    public CoreServiceManager( final ServiceManager parent, final ClassLoader classloader ) {
        this.parentManager = parent;
        this.classloader = classloader;

        RoleManager parentRoleManager = null;
        // FIXME - We should change this to a cleaner way!
        ServiceManager coreServicemanager = parent;
        // get role manager
        if ( coreServicemanager instanceof RoleManagerOwner ) {
            parentRoleManager = ((RoleManagerOwner)coreServicemanager).getRoleManager();
        }
        // get logger manager
        if ( coreServicemanager instanceof CoreServiceManager ) {
            this.loggerManager = ((CoreServiceManager)coreServicemanager).loggerManager;
        }

        // Always create a role manager, it can be filled several times either through
        // the root "roles" attribute or through loading of includes
        this.roleManager = new RoleManager(parentRoleManager);
    }

    //=============================================================================================
    // Avalon lifecycle
    //=============================================================================================

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.roleManager.enableLogging(logger);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize( final Context context ) 
    throws ContextException {
        this.context = context;
        this.settings = ((Core)context.get(Core.ROLE)).getSettings();
    }

    /**
     * Configure the LoggerManager.
     */
    public void setLoggerManager( final LoggerManager manager ) {
        this.loggerManager = manager;
    }

    public void setRoleManager (RoleManager rm) {
        if (rm != null) {
            // Override the one eventually got in the parent (see constructor)
            // FIXME - Why do we wrap the role manager?
            this.roleManager = new RoleManager(rm);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        // It's possible to define a logger on a per sitemap/service manager base.
        // This is the default logger for all components defined with this sitemap/manager.
        if ( configuration.getAttribute("logger", null) != null ) {
            this.enableLogging(this.loggerManager.getLoggerForCategory(configuration.getAttribute("logger")));
        }
        this.componentEnv = new ComponentEnvironment(this.classloader, getLogger(), this.roleManager, this.loggerManager, this.context, this);

        // Setup location
        this.location = configuration.getLocation();

        // Find the current URI
        String currentURI;
        int pos = this.location.lastIndexOf(':');
        if (pos == -1) {
            // No available location: start at the context
            currentURI = "context://";
        } else {
            pos = this.location.lastIndexOf(':', pos);
            currentURI = this.location.substring(0, pos-1);
        }

        // find possible JMX domain name
        this.jmxDefaultDomain = configuration.getAttribute(JMX_DOMAIN_ATTR_NAME, null);

        try {
            // and load configuration with a empty list of loaded configurations
            parseConfiguration(configuration, currentURI, new HashSet());
        } finally {
            // Release any source resolver that may have been created to load includes
            releaseCachedSourceResolver();
        }
    }

    /**
     * @return The default JMX domain name
     */
    public String getJmxDefaultDomain() {
        if (this.jmxDefaultDomain == null) {
            if (this.parentManager instanceof CoreServiceManager) {
                return ((CoreServiceManager)this.parentManager).getJmxDefaultDomain();
            }
            return JMX_DEFAULT_DOMAIN_NAME;
        }
        return this.jmxDefaultDomain;
    }
    
    /**
     * Return the service manager logger.
     */
    public Logger getServiceManagerLogger() {
        return this.getLogger();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        this.initialized = true;

        // Initialize component handlers. This is done in no particular order, but initializing a
        // handler may indirectly initialize another handler through a call to lookup().
        // This isn't a problem as a handler's initialize() method can be called several times.

        // We copy the list of handlers as the componentHandler Map may change if implicitely declared
        // components are looked up.
        ComponentHandler[] handlers = (ComponentHandler[])this.componentHandlers.values().toArray(
                new ComponentHandler[this.componentHandlers.size()]);

        for( int i = 0; i < handlers.length; i++ ) {
            try {
                handlers[i].initialize();
            } catch( Exception e ) {
                if( this.getLogger().isErrorEnabled() ) {
                    this.getLogger().error( "Caught an exception trying to initialize "
                                       + "the component handler.", e );
                }
                // Rethrow the exception
                throw e;
            }
        }

//        Object[] keyArray = this.componentHandlers.keySet().toArray();
//        java.util.Arrays.sort(keyArray);
//        for (int i = 0; i < keyArray.length; i++) {
//            System.err.println("Component key = " + keyArray[i]);
//        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        boolean forceDisposal = false;

        final List disposed = new ArrayList();

        while( componentHandlers.size() > 0 ) {
            for( Iterator iterator = componentHandlers.keySet().iterator();
                 iterator.hasNext(); ) {
                final Object role = iterator.next();

                final ComponentHandler handler =
                    (ComponentHandler)componentHandlers.get( role );

                if( forceDisposal || handler.canBeDisposed() ) {
                    if( forceDisposal && getLogger().isWarnEnabled() ) {
                        this.getLogger().warn
                            ( "disposing of handler for unreleased component."
                              + " role [" + role + "]" );
                    }

                    handler.dispose();
                    disposed.add( role );
                }
            }

            if( disposed.size() > 0 ) {
                final Iterator i = disposed.iterator();
                while ( i.hasNext() ) {
                    this.componentHandlers.remove( i.next() );
                }
                disposed.clear();
            } else {   
                // no more disposable handlers!
                forceDisposal = true;
            }
        }
        this.disposed = true;
    }

    //=============================================================================================
    // ServiceManager implementation
    //=============================================================================================

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService( final String role ) {
        if( !this.initialized || this.disposed ) return false;

        boolean exists = this.componentHandlers.containsKey( role );

        if( !exists && null != this.parentManager ) {
            exists = this.parentManager.hasService( role );
        }

        return exists;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup( final String role )
    throws ServiceException {
        if( !this.initialized ) {
            if( this.getLogger().isWarnEnabled() ) {
                this.getLogger().warn(
                    "Looking up component on an uninitialized CoreServiceManager [" + role + "]" );
            }
        }

        if( this.disposed ) {
            throw new IllegalStateException(
                "You cannot lookup components on a disposed CoreServiceManager" );
        }

        if( role == null ) {
            final String message =
                "CoreServiceManager attempted to retrieve service with null role.";

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message );
            }
            throw new ServiceException( role, message );
        }

        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get( role );

        // Retrieve the instance of the requested component
        if ( handler == null ) {
            if( this.parentManager != null ) {
                try {
                    return this.parentManager.lookup( role );
                } catch ( ServiceNotFoundException snfe) {
                    // ignore.  If the exception is thrown, we try to
                    // create the component next
                } catch( Exception e ) {
                    if( this.getLogger().isWarnEnabled() ) {
                        final String message =
                            "ComponentLocator exception from parent SM during lookup.";
                        this.getLogger().warn( message, e );
                    }
                    // ignore.  If the exception is thrown, we try to
                    // create the component next
                }
            }

            if( this.roleManager != null ) {
                final ComponentInfo info = this.roleManager.getDefaultServiceInfoForRole( role );

                if( info != null ) {
                    if( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().debug( "Could not find ComponentHandler, attempting to create "
                            + "one for role [" + role + "]" );
                    }

                    try {
                        final Configuration configuration = new DefaultConfiguration( "", "-" );

                        handler = this.getComponentHandler(role,
                                                           info.getServiceClassName(),
                                                           configuration.getChild(role),
                                                           info);

                    } catch (ServiceException se) {
                        throw se;
                    } catch( final Exception e ) {
                        final String message = "Could not find component for role [" + role + "]";
                        if( this.getLogger().isDebugEnabled() ) {
                            this.getLogger().debug( message, e );
                        }
                        throw new ServiceException( role, message, e );
                    }
                    try {
                        handler.initialize();
                    } catch (ServiceException se) {
                        throw se;
                    } catch( final Exception e ) {
                        final String message = "Could not create component for role [" + role + "]: " + handler.getClass();
                        if( this.getLogger().isDebugEnabled() ) {
                            this.getLogger().debug( message, e );
                        }
                        throw new ServiceException( role, message, e );
                    }

                    this.componentHandlers.put( role, handler );
                }
            } else {
                this.getLogger().debug( "Component requested without a RoleManager set.\n"
                    + "That means setRoleManager() was not called during initialization." );
            }
        }

        if( handler == null ) {
            final String message = "Could not find component for role: [" + role + "]";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message );
            }
            throw new ServiceNotFoundException( role, message );
        }

        Object component = null;

        try {
            component = handler.get();
        } catch ( ServiceException se) {
            // Rethrow insteand of wrapping it again
            throw se;
        } catch( final Exception e ) {
            final String message = "Could not access the component for role [" + role + "]";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message, e );
            }

            throw new ServiceException( role, message, e );
        }
        this.initialize( role, component );

        // Add a mapping between the component and its handler.
        //  In the case of a ThreadSafeComponentHandler, the same component will be mapped
        //  multiple times but because each put will overwrite the last, this is not a
        //  problem.  Checking to see if the put has already been done would be slower.
        this.componentMapping.put( component, handler );

        return component;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release( final Object component ) {
        if( null == component ) {
            return;
        }

        // The componentMapping StaticBucketMap itself is threadsafe, and because the same component
        //  will never be released by more than one thread, this method does not need any
        //  synchronization around the access to the map.

        final ComponentHandler handler =
            (ComponentHandler)this.componentMapping.get( component );

        if ( handler != null ) {
            // ThreadSafe components will always be using a ThreadSafeComponentHandler,
            //  they will only have a single entry in the m_componentMapping map which
            //  should not be removed until the ComponentLocator is disposed.  All
            //  other components have an entry for each instance which should be
            //  removed.
            if( !handler.isSingleton() ) {
                // Remove the component before calling put.  This is critical to avoid the
                //  problem where another thread calls put on the same component before
                //  remove can be called.
                this.componentMapping.remove( component );
            }

            try {
                handler.put( component );
            } catch( Exception e ) {
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( "Error trying to release component.", e );
                }
            }
        }
        else if( this.parentManager != null ) {
            this.parentManager.release( component );
        } else {
            this.getLogger().warn( "Attempted to release a " + component.getClass().getName() +
                              " but its handler could not be located." );
        }
    }

    //=============================================================================================
    // Additional public & protected contract
    //=============================================================================================

    /**
     * Add a new component to the manager.
     *
     * @param role the role name for the new component.
     * @param className the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( String role,
                              String className,
                              Configuration configuration,
                              ComponentInfo info)
    throws ConfigurationException {
        if( this.initialized ) {
            throw new IllegalStateException("Cannot add components to an initialized CoreServiceManager." );
        }

        // check for old excalibur class names - we only test against the selector
        // implementation
        if ( "org.apache.cocoon.components.ExtendedComponentSelector".equals(className)) {
            className = DefaultServiceSelector.class.getName();
        }

        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
        }

        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get(role);
        if (handler != null) {
            // Check that override is allowed. If yes, the handler will be redefined below, allowing
            // the new definition to feed this manager with its components.
            checkComponentOverride(role, className, configuration, handler);
        }

        try {
            handler = this.getComponentHandler(role, className, configuration, info);

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Handler type = " + handler.getClass().getName() );
            }

            this.componentHandlers.put( role, handler );
        } catch ( final ConfigurationException ce ) {
            throw ce;
        } catch( final Exception e ) {
            throw new ConfigurationException( "Could not add component defined at " + configuration.getLocation(), e );
        }

//        // Initialize shadow selector now, it will feed this service manager
//        if ( DefaultServiceSelector.class.isAssignableFrom( component )) {
//            try {
//                handler.initialize();
//            } catch(ServiceException se) {
//                throw se;
//            } catch(Exception e) {
//                throw new ServiceException(role, "Could not initialize selector", e);
//            }
//        }
    }

    /**
     * Add an existing object to the manager. The object should be fully configured as no
     * setup lifecycle methods are called. On manager disposal, the <code>Disposable</code>
     * method is considered.
     * 
     * @param role the role under which the object will be known
     * @param instance the component instance
     * @throws ServiceException
     */
    public void addInstance(String role, Object instance) throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException(role,
                "Cannot add components to an initialized CoreServiceManager.");
        }

        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get(role);
        if (handler != null) {
            ComponentInfo info = handler.getInfo();
            throw new ServiceException(role, "Component already defined at " + info.getLocation()); 
        }

        this.componentHandlers.put(role, new InstanceComponentHandler(getLogger(), instance));
    }

    /**
     * Add an alias to a role, i.e. define a synonym for the role.
     * 
     * @param existingRole the existing role that will be aliased
     * @param newRole the new role
     * @throws ServiceException if the existing role could not be found in the current
     *         manager and its ancestors
     */
    public void addRoleAlias(String existingRole, String newRole) throws ServiceException {
        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get(existingRole);
        if (handler == null) {
            // Aliased component not found here, but can be defined by an ancestor
            CoreServiceManager current = this;
            while(handler == null && current.parentManager != null) {
                if (!(current.parentManager instanceof CoreServiceManager)) {
                    throw new ServiceException(newRole, "Cannot alias to components not managed by CoreServiceManager");
                }
                current = (CoreServiceManager)current.parentManager;
                handler = (ComponentHandler)current.componentHandlers.get(existingRole);
            }
        }

        if (handler == null) {
            throw new ServiceException(newRole, "Cannot alias non-existing role " + existingRole);
        }

        this.componentHandlers.put(newRole, new AliasComponentHandler(this.getLogger(), handler));
    }

    /* 
     * Get the role manager
     */
    public RoleManager getRoleManager() {
        return this.roleManager;
    }

    /**
     * Initialize the component
     * @throws ServiceException
     */
    protected void initialize(String role, Object component) 
    throws ServiceException {
        // we do nothing here, can be used in subclasses
    }

    //=============================================================================================
    // Private methods
    //=============================================================================================
    
    /**
     * Obtain a new ComponentHandler for the specified component. 
     * 
     * @param role the component's role.
     * @param className Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     * @param baseInfo The information for managing the component, like service manager etc.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    private ComponentHandler getComponentHandler( final String role,
                                                  final String className,
                                                  final Configuration configuration,
                                                  final ComponentInfo baseInfo)
    throws Exception {

        boolean lazyLoad;

        if (configuration.getAttribute("preload", null) != null) {
            // This one has precedence
            lazyLoad = configuration.getAttributeAsBoolean("preload");

        } else {
            lazyLoad = false;
        }

        ComponentHandler handler;
        if (lazyLoad) {
            handler = new LazyHandler(role, className, configuration, componentEnv);
        } else {
            
            // FIXME - we should ensure that we always get an info
            ComponentInfo info;
            if ( baseInfo != null ) {
                info = baseInfo.duplicate();
            } else {
                info = new ComponentInfo();
                info.fill(configuration);
                info.setJmxDomain(JMXUtils.findJmxDomain(info.getJmxDomain(), this));
                info.setJmxName(JMXUtils.findJmxName(info.getJmxName(), className));
                info.setRole(role);
            }
            info.setConfiguration(configuration);
            info.setServiceClassName(className);

            handler = AbstractComponentHandler.getComponentHandler(role, this.componentEnv, info);
            // TODO we probably need to keep the ObjectInstance returnde for setupJmxFor for 
            //      later deregistering.
            JMXUtils.setupJmxFor(handler, info, getLogger());
        }
        return handler;
    }

    private void parseConfiguration(final Configuration configuration, String contextURI, Set loadedURIs) 
        throws ConfigurationException {

        final Configuration[] configurations = configuration.getChildren();

        for( int i = 0; i < configurations.length; i++ ) {
            final Configuration componentConfig = configurations[i];

            final String componentName = componentConfig.getName();

            if ("include".equals(componentName)) {
                handleInclude(contextURI, loadedURIs, componentConfig);

            } else {
                // Component declaration
                // Find the role
                String role = componentConfig.getAttribute("role", null);
                if (role == null) {
                    // Get the role from the role manager if not explicitely specified
                    role = roleManager.getRoleForName(componentName);
                    if (role == null) {
                        // Unknown role
                        throw new ConfigurationException("Unknown component type '" + componentName +
                            "' at " + componentConfig.getLocation());
                    }
                }

                // Find the className
                String className = componentConfig.getAttribute("class", null);
                if (className == null) {
                    // Get the default class name for this role
                    final ComponentInfo info = roleManager.getDefaultServiceInfoForRole(role);
                    info.setJmxDomain(JMXUtils.findJmxDomain(info.getJmxDomain(), this));
                    info.setJmxName(JMXUtils.findJmxName(info.getJmxName(), info.getServiceClassName()));
                    if (info == null) {
                        throw new ConfigurationException("Cannot find a class for role " + role + " at " + componentConfig.getLocation());
                    }
                    className = info.getServiceClassName();
                }

                // If it has a "name" attribute, add it to the role (similar to the
                // declaration within a service selector)
                // Note: this has to be done *after* finding the className above as we change the role
                String name = componentConfig.getAttribute("name", null);
                if (name != null) {
                    role = role + "/" + name;
                }

                this.addComponent(role, className, componentConfig, null);
            }
        }
    }

    private void handleInclude(String contextURI, Set loadedURIs, Configuration includeStatement)
            throws ConfigurationException {
        String includeURI = includeStatement.getAttribute("src", null);
        String directoryURI = null;
        if ( includeURI == null ) {
            // check for directories
            directoryURI = includeStatement.getAttribute("dir", null);                    
        }
        if ( includeURI == null && directoryURI == null ) {
            throw new ConfigurationException("Include statement must either have a 'src' or 'dir' attribute, at " +
                    includeStatement.getLocation());
        }

        // Setup the source resolver if needed
        setupSourceResolver();

        if ( includeURI != null ) {
            Source src;
            try {
                src = this.cachedSourceResolver.resolveURI(includeURI, contextURI, null);
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load '" + includeURI + "' at " + includeStatement.getLocation(), e);
            }
            
            loadURI(src, loadedURIs, includeStatement);
        } else {
            final String pattern = includeStatement.getAttribute("pattern", null);
            int[] parsedPattern = null;
            if ( pattern != null ) {
                parsedPattern = WildcardHelper.compilePattern(pattern);
            }
            Source directory = null;
            try {
                directory = this.cachedSourceResolver.resolveURI(directoryURI, contextURI, CONTEXT_PARAMETERS);
                if ( directory instanceof TraversableSource ) {
                    final Iterator children = ((TraversableSource)directory).getChildren().iterator();
                    while ( children.hasNext() ) {
                        final Source s = (Source)children.next();
                        if ( parsedPattern == null || this.match(s.getURI(), parsedPattern)) {
                            this.loadURI(s, loadedURIs, includeStatement);
                        }
                    }
                } else {
                    throw new ConfigurationException("Include.dir must point to a directory, '" + directory.getURI() + "' is not a directory.'");
                }
            } catch (IOException ioe) {
                throw new ConfigurationException("Unable to read configurations from " + directoryURI);
            } finally {
                this.cachedSourceResolver.release(directory);
            }
        }
    }

    private void loadURI(Source src, Set loadedURIs, Configuration includeStatement) 
    throws ConfigurationException {
        // If already loaded: do nothing
        try {

            String uri = src.getURI();

            if (!loadedURIs.contains(uri)) {
                if ( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug("Loading configuration from: " + uri);
                }
                // load it and store it in the read set
                Configuration includeConfig = null;
                try {
                    ConfigurationBuilder builder = new ConfigurationBuilder(this.settings);
                    includeConfig = builder.build(src.getInputStream(), uri);
                } catch (Exception e) {
                    throw new ConfigurationException("Cannot load '" + uri + "' at " + includeStatement.getLocation(), e);
                }
                loadedURIs.add(uri);

                // what is it?
                String includeKind = includeConfig.getName();
                if (includeKind.equals("components")) {
                    // more components
                    parseConfiguration(includeConfig, uri, loadedURIs);
                } else if (includeKind.equals("role-list")) {
                    // more roles
                    this.roleManager.configure(includeConfig);
                } else {
                    throw new ConfigurationException("Unknow document '" + includeKind + "' included at " +
                            includeStatement.getLocation());
                }
            }
        } finally {
            this.cachedSourceResolver.release(src);
        }
    }

    /**
     * If the parent manager does not exist or does not
     * provide a source resolver, a simple one is created here to load the file.
     */
    private void setupSourceResolver() {
        if (this.cachedSourceResolver == null) {

            if (this.parentManager != null && this.parentManager.hasService(SourceResolver.ROLE)) {
                try {
                    this.cachedSourceResolver = (SourceResolver)this.parentManager.lookup(SourceResolver.ROLE);
                } catch(ServiceException se) {
                    // Unlikely to happen
                    throw new CoreResourceNotFoundException("Cannot get source resolver from parent, at " + location, se);
                }
            } else {
                // Create our own
                SimpleSourceResolver simpleSR = new SimpleSourceResolver();
                simpleSR.enableLogging(getLogger());
                try {
                    simpleSR.contextualize(this.context);
                } catch (ContextException ce) {
                    throw new CoreResourceNotFoundException("Cannot setup source resolver, at " + location, ce);
                }
                this.cachedSourceResolver = simpleSR;
            }
        }        
    }

    private boolean match(String uri, int[] parsedPattern ) {
        int pos = uri.lastIndexOf('/');
        if ( pos != -1 ) {
            uri = uri.substring(pos+1);
        }
        return WildcardHelper.match(null, uri, parsedPattern);      
    }

    /**
     * Release the source resolver that may have been created by the first call to
     * loadConfiguration().
     */
    private void releaseCachedSourceResolver() {
        if (this.cachedSourceResolver != null &&
            this.parentManager != null && this.parentManager.hasService(SourceResolver.ROLE)) {
            this.parentManager.release(this.cachedSourceResolver);
        }
        this.cachedSourceResolver = null;
    }

    /** 
     * Check if a component can be overriden. Only {@link DefaultServiceSelector} or its subclasses can be
     * overriden, as they directly feed this manager with their component definitions and are empty
     * shells delegating to this manager afterwards.
     */
    private void checkComponentOverride(String role, String className, Configuration config,
            ComponentHandler existingHandler) throws ConfigurationException {
        
        // We only allow selectors to be overloaded
        ComponentInfo info = existingHandler.getInfo();
        if (!className.equals(info.getServiceClassName())) {
            throw new ConfigurationException("Role " + role + " redefined with a different class name, at " +
                    config.getLocation());
        }

        Class clazz;
        try {
            clazz = this.componentEnv.loadClass(className);
        } catch(ClassNotFoundException cnfe) {
            throw new ConfigurationException("Cannot load class " + className + " for component at " +
                    config.getLocation(), cnfe);
        }

        if (!DefaultServiceSelector.class.isAssignableFrom(clazz)) {
            throw new ConfigurationException("Component declared at " + info.getLocation() + " is redefined at " +
                    config.getLocation());
        }
    }
}
