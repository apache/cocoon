/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ServiceInfo;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * Default service manager for Cocoon's components.
 *
 * @version SVN $Revision: 1.6 $Id$
 */
public class CocoonServiceManager
extends AbstractServiceManager
implements ServiceManager, Configurable {
    
    /** The location where this manager is defined */
    protected String location;
    
    /** The parent ServiceManager */
    protected ServiceManager parentManager;
    
    /** added component handlers before initialization to maintain
     *  the order of initialization
     */
    private final List newComponentHandlers = new ArrayList();

    /** Temporary list of parent-aware components.  Will be null for most of
     * our lifecycle. */
    private ArrayList parentAwareComponents = new ArrayList();

    /** The resolver used to resolve includes. It is lazily loaded in {@link #getSourceResolver()}. */
    private SourceResolver cachedSourceResolver;

    /** Create the ServiceManager with a parent ServiceManager */
    public CocoonServiceManager( final ServiceManager parent ) {
        this.parentManager = parent;
        
        RoleManager parentRoleManager = null;
        // get role manager and logger manager
        if ( parent instanceof CocoonServiceManager ) {
            parentRoleManager = ((CocoonServiceManager)parent).roleManager;
            this.loggerManager = ((CocoonServiceManager)parent).loggerManager;
        }
        
        // Always create a role manager, it can be filled several times either through
        // the root "roles" attribute or through loading of includes
        this.roleManager = new RoleManager(parentRoleManager);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.roleManager.enableLogging(logger);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup( final String role )
    throws ServiceException {
        if( !this.initialized ) {
            if( this.getLogger().isWarnEnabled() ) {
                this.getLogger().warn(
                    "Looking up component on an uninitialized CocoonServiceManager [" + role + "]" );
            }
        }

        if( this.disposed ) {
            throw new IllegalStateException(
                "You cannot lookup components on a disposed CocoonServiceManager" );
        }

        if( role == null ) {
            final String message =
                "CocoonServiceManager attempted to retrieve service with null role.";

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
                final ServiceInfo info = this.roleManager.getDefaultServiceInfoForRole( role );

                if( info != null ) {
                    if( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().debug( "Could not find ComponentHandler, attempting to create "
                            + "one for role [" + role + "]" );
                    }

                    try {
                        final Class componentClass = this.getClass().getClassLoader().loadClass( info.getServiceClassName() );

                        final Configuration configuration = new DefaultConfiguration( "", "-" );

                        handler = this.getComponentHandler(role,
                                                       componentClass,
                                                       configuration,
                                                       this);

                        handler.initialize();
                    } catch (ServiceException se) {
                        throw se;
                    } catch( final Exception e ) {
                        final String message = "Could not find component for role [" + role + "]";
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
            throw new ServiceException( role, message );
        }

        Object component = null;

        try {
            component = handler.get();
        } catch( final IllegalStateException ise ) {
            try {
                handler.initialize();
                component = handler.get();
                
            } catch( final ServiceException ce ) {
                // Rethrow instead of wrapping a ServiceException with another one
                throw ce;
            } catch( final Exception e ) {
                final String message = "Could not access the component for role [" + role + "]";
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( message, e );
                }

                throw new ServiceException( role, message, e );
            }
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

    /**
     * Initialize the component
     * @throws ServiceException
     */
    protected void initialize(String role, Object component) 
    throws ServiceException {
        // we do nothing here, can be used in subclasses
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService( final String role ) {
        if( !this.initialized ) return false;
        if( this.disposed ) return false;

        boolean exists = this.componentHandlers.containsKey( role );

        if( !exists && null != this.parentManager ) {
            exists = this.parentManager.hasService( role );
        }

        return exists;
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
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
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
        
        try {
            // and load configuration with a empty list of loaded configurations
            doConfigure(configuration, currentURI, new HashSet());
        } finally {
            // Release any source resolver that may have been created to load includes
            releaseCachedSourceResolver();
        }
    }

    private void doConfigure(final Configuration configuration, String contextURI, Set loadedURIs) 
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
                    final ServiceInfo info = roleManager.getDefaultServiceInfoForRole(role);
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
    
                this.addComponent(className, role, componentConfig);
            }
        }
    }
    
    protected void handleInclude(String contextURI, Set loadedURIs, Configuration includeStatement)
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
            Source directory = null;
            try {
                directory = this.cachedSourceResolver.resolveURI(directoryURI, contextURI, null);
                if ( directory instanceof TraversableSource ) {
                    final Iterator children = ((TraversableSource)directory).getChildren().iterator();
                    while ( children.hasNext() ) {
                        Source s = (Source)children.next();
                        this.loadURI(s, loadedURIs, includeStatement);
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

    protected void loadURI(Source src, Set loadedURIs, Configuration includeStatement) 
    throws ConfigurationException {
        // If already loaded: do nothing
        try {
            
            String uri = src.getURI();
            
            if (!loadedURIs.contains(uri)) {
                // load it and store it in the read set
                Configuration includeConfig = null;
                try {
                    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                    includeConfig = builder.build(src.getInputStream(), uri);
                } catch (ConfigurationException ce) {
                    throw ce;
                } catch (Exception e) {
                    throw new ConfigurationException("Cannot load '" + uri + "' at " + includeStatement.getLocation(), e);
                }
                loadedURIs.add(uri);
                
                // what is it?
                String includeKind = includeConfig.getName();
                if (includeKind.equals("components")) {
                    // more components
                    doConfigure(includeConfig, uri, loadedURIs);
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
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize( final Context context ) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        super.initialize();

        for( int i = 0; i < this.newComponentHandlers.size(); i++ ) {
            final ComponentHandler handler =
                (ComponentHandler)this.newComponentHandlers.get( i );
            try {
                handler.initialize();
            } catch( Exception e ) {
                if( this.getLogger().isErrorEnabled() )
                {
                    this.getLogger().error( "Caught an exception trying to initialize "
                                       + "the component handler.", e );
                }

                // Rethrow the exception
                throw e;
            }
        }

        List keys = new ArrayList( this.componentHandlers.keySet() );

        for( int i = 0; i < keys.size(); i++ ) {
            final Object key = keys.get( i );
            final ComponentHandler handler =
                (ComponentHandler)this.componentHandlers.get( key );

            if( !this.newComponentHandlers.contains( handler ) ) {
                try {
                    handler.initialize();

                } catch( Exception e ) {
                    if( this.getLogger().isErrorEnabled() ) {
                        this.getLogger().error( "Caught an exception trying to initialize "
                                           + "the component handler.", e );

                    }
                    // Rethrow the exception
                    throw e;
                }
            }
        }
        this.newComponentHandlers.clear();
        
        // Initialize parent aware components
        if (this.parentAwareComponents == null) {
            throw new ServiceException(null, "CocoonServiceManager already initialized");
        }

        // Set parents for parentAware components
        Iterator iter = this.parentAwareComponents.iterator();
        while (iter.hasNext()) {
            String role = (String)iter.next();
            if ( this.parentManager != null && this.parentManager.hasService( role ) ) {
                // lookup new component
                Object component = null;
                try {
                    component = this.lookup( role );
                    ((CocoonServiceSelector)component).setParentLocator( this.parentManager, role );
                } catch (ServiceException ignore) {
                    // we don't set the parent then
                } finally {
                    this.release( component );
                }
            }
        }
        this.parentAwareComponents = null;  // null to save memory, and catch logic bugs.
        
//        Object[] keyArray = this.componentHandlers.keySet().toArray();
//        Arrays.sort(keyArray);
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
        super.dispose();
    }

    /**
     * Add a new component to the manager.
     *
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final String role,
                              final Class component,
                              final Configuration configuration )
    throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException( role,
                "Cannot add components to an initialized CocoonServiceManager." );
        }

        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Attempting to get handler for role [" + role + "]" );
        }

        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get(role);
        if (handler != null) {
            // Overloaded component: we only allow selectors to be overloaded
            ServiceInfo info = handler.getInfo();
            if (!DefaultServiceSelector.class.isAssignableFrom(component) ||
                !DefaultServiceSelector.class.isAssignableFrom(info.getServiceClass())) {
                throw new ServiceException(role, "Component declared at " + info.getLocation() + " is redefined at " +
                        configuration.getLocation());
            }
        }
        try {
            handler = this.getComponentHandler(role, component, configuration, this);

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Handler type = " + handler.getClass().getName() );
            }

            this.componentHandlers.put( role, handler );
            this.newComponentHandlers.add( handler );
        } catch ( final ServiceException se ) {
            throw se;
        } catch( final Exception e ) {
            throw new ServiceException( role, "Could not set up component handler.", e );
        }
        
        if ( CocoonServiceSelector.class.isAssignableFrom( component ) ) {
            this.parentAwareComponents.add(role);
        }
        // Initialize shadow selector now, it will feed this service manager
        if ( DefaultServiceSelector.class.isAssignableFrom( component )) {
            try {
                handler.initialize();
            } catch(ServiceException se) {
                throw se;
            } catch(Exception e) {
                throw new ServiceException(role, "Could not initialize selector", e);
            }
        }
    }
    
    /**
     * If the parent manager does not exist or does not
     * provide a source resolver, a simple one is created here to load the file.
     */
    protected void setupSourceResolver() {
        if (this.cachedSourceResolver == null) {
            
            if (this.parentManager != null && this.parentManager.hasService(SourceResolver.ROLE)) {
                try {
                    this.cachedSourceResolver = (SourceResolver)this.parentManager.lookup(SourceResolver.ROLE);
                } catch(ServiceException se) {
                    // Unlikely to happen
                    throw new CascadingRuntimeException("Cannot get source resolver from parent, at " + location, se);
                }
            } else {
                // Create our own
                SimpleSourceResolver simpleSR = new SimpleSourceResolver();
                simpleSR.enableLogging(getLogger());
                try {
                    simpleSR.contextualize(this.context);
                } catch (ContextException ce) {
                    throw new CascadingRuntimeException("Cannot setup source resolver, at " + location, ce);
                }
                this.cachedSourceResolver = simpleSR;
            }
        }        
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
}
