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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Default service manager for Cocoon's components.
 *
 * @version CVS $Revision: 1.6 $Id: CocoonServiceManager.java 55165 2004-10-20 16:51:50Z cziegeler $
 */
public class CocoonServiceManager
extends AbstractServiceManager
implements ServiceManager, Configurable {
    
    /** The parent ServiceManager */
    protected ServiceManager parentManager;

    /** added component handlers before initialization to maintain
     *  the order of initialization
     */
    private final List newComponentHandlers = new ArrayList();

    /** Create the ServiceManager with a Classloader and parent ServiceManager */
    public CocoonServiceManager( final ServiceManager parent, 
                                 final ClassLoader loader ) {
        super(loader);
        this.parentManager = parent;
        // get role manager and logger manager
        if ( parent instanceof CocoonServiceManager ) {
            this.roleManager = ((CocoonServiceManager)parent).roleManager;
            this.loggerManager = ((CocoonServiceManager)parent).loggerManager;
        }
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

        AbstractComponentHandler handler = (AbstractComponentHandler)this.componentHandlers.get( role );

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
                final String className = this.roleManager.getDefaultClassNameForRole( role );

                if( className != null ) {
                    if( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().debug( "Could not find ComponentHandler, attempting to create "
                            + "one for role [" + role + "]" );
                    }

                    try {
                        final Class componentClass = this.loader.loadClass( className );

                        final Configuration configuration = new DefaultConfiguration( "", "-" );

                        handler = this.getComponentHandler( componentClass,
                                                       configuration,
                                                       this);

                        handler.initialize();
                    } catch (ServiceException se) {
                        throw se;
                    } catch( final Exception e ) {
                        final String message = "Could not find component";
                        if( this.getLogger().isDebugEnabled() ) {
                            this.getLogger().debug( message + " for role: " + role, e );
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
            final String message = "Could not find component";
            if( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( message + " for role: " + role );
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
                
                this.initialize( role, component );
            } catch( final ServiceException ce ) {
                // Rethrow instead of wrapping a ComponentException with another one
                throw ce;
            } catch( final Exception e ) {
                final String message = "Could not access the Component";
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( message + " for role [" + role + "]", e );
                }

                throw new ServiceException( role, message, e );
            }
        } catch ( ServiceException se) {
            throw se;
        } catch( final Exception e ) {
            final String message = "Could not access the Component";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message + " for role [" + role + "]", e );
            }

            throw new ServiceException( role, message, e );
        }

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

        final AbstractComponentHandler handler =
            (AbstractComponentHandler)this.componentMapping.get( component );

        if ( handler != null ) {
            // ThreadSafe components will always be using a ThreadSafeComponentHandler,
            //  they will only have a single entry in the m_componentMapping map which
            //  should not be removed until the ComponentLocator is disposed.  All
            //  other components have an entry for each instance which should be
            //  removed.
            if( !( handler instanceof ThreadSafeComponentHandler ) ) {
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
    public void configure( final Configuration configuration )
    throws ConfigurationException {
        if( null == roleManager ) {
            final RoleManager roleInfo = new RoleManager();
            roleInfo.enableLogging( getLogger() );
            roleInfo.configure( configuration );
            this.roleManager = roleInfo;
            this.getLogger().debug( "No RoleManager given, deriving one from configuration" );
        }

        // Set components

        final Configuration[] configurations = configuration.getChildren();

        for( int i = 0; i < configurations.length; i++ ) {
            String type = configurations[ i ].getName();

            if( !type.equals( "role" ) ) {
                String role = configurations[ i ].getAttribute( "role", "" );
                String className = configurations[ i ].getAttribute( "class", "" );

                if( role.equals( "" ) ) {
                    role = roleManager.getRoleForName( type );
                }

                if( null != role && !role.equals( "" ) ) {
                    if( className.equals( "" ) ) {
                        className = roleManager.getDefaultClassNameForRole( role );
                    }

                    this.addComponent(className, role, configurations[i]);
                }
            }
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
            final AbstractComponentHandler handler =
                (AbstractComponentHandler)this.newComponentHandlers.get( i );
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
            final AbstractComponentHandler handler =
                (AbstractComponentHandler)this.componentHandlers.get( key );

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

                final AbstractComponentHandler handler =
                    (AbstractComponentHandler)componentHandlers.get( role );

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
    public void addComponent( final Object role,
                              final Class component,
                              final Configuration configuration )
    throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException( role.toString(),
                "Cannot add components to an initialized CocoonServiceManager." );
        }

        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Attempting to get handler for role [" + role.toString() + "]" );
            }

            final AbstractComponentHandler handler = this.getComponentHandler( component,
                                                                  configuration,
                                                                  this);

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Handler type = " + handler.getClass().getName() );
            }

            this.componentHandlers.put( role.toString(), handler );
            this.newComponentHandlers.add( handler );
        } catch ( final ServiceException se ) {
            throw se;
        } catch( final Exception e ) {
            throw new ServiceException( role.toString(), "Could not set up component handler.", e );
        }
    }

}
