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
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Default component selector for Cocoon's components.
 *
 * @version CVS $Id: CocoonServiceSelector.java 55144 2004-10-20 12:26:09Z ugo $
 */
public class CocoonServiceSelector
extends AbstractServiceManager
implements ServiceSelector, Serviceable, Configurable {
    
    /** The application context for components
     */
    private ServiceManager serviceManager;

    /** The parent selector, if any */
    protected CocoonServiceSelector parentSelector;

    /** The parent locator, if any */
    protected ServiceManager parentLocator;

    /** The role of this selector. Set in <code>configure()</code>. */
    protected String roleName;

    /** The default hint */
    protected String defaultHint;

    /** Create the ComponentSelector */
    public CocoonServiceSelector() {
        super(null);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceSelector#select(java.lang.Object)
     */
    public Object select( Object hint )
    throws ServiceException {
        if( !this.initialized ) {
            if( this.getLogger().isWarnEnabled() ) {
                this.getLogger().warn( "Looking up component on an uninitialized ComponentLocator "
                    + "with hint [" + hint + "]" );
            }
        }

        if( this.disposed ) {
            throw new IllegalStateException(
                "You cannot select a Component from a disposed ComponentSelector" );
        }

        if (hint == null) {
            hint = this.defaultHint;
        }

        AbstractComponentHandler handler = (AbstractComponentHandler)this.componentHandlers.get( hint );

        // Retrieve the instance of the requested component
        if( null == handler ) {
            // Doesn't exist here : try in parent selector
            if ( this.parentSelector != null ) {
                return this.parentSelector.select(hint);                
            }
            final String message = this.roleName
                + ": ComponentSelector could not find the component for hint [" + hint + "]";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message );
            }
            throw new ServiceException( hint.toString(), message );
        }

        Object component = null;

        try {
            component = handler.get();
        } catch( final ServiceException ce ) {
            //rethrow
            throw ce;
        } catch( final Exception e ) {
            final String message = this.roleName
                + ": ComponentSelector could not access the Component for hint [" + hint + "]";

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message, e );
            }
            throw new ServiceException( hint.toString(), message, e );
        }

        if( null == component ) {
            // Doesn't exist here : try in parent selector
            if ( this.parentSelector != null ) {
                component = this.parentSelector.select(hint);
            } else {
                final String message = this.roleName
                    + ": ComponentSelector could not find the component for hint [" + hint + "]";
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( message );
                }
                throw new ServiceException( hint.toString(), message );
            }
        }

        this.componentMapping.put( component, handler );
        return component;

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceSelector#isSelectable(java.lang.Object)
     */
    public boolean isSelectable( Object hint ) {
        if( !this.initialized ) return false;
        if( this.disposed ) return false;

        if (hint == null) {
            hint = this.defaultHint;
        }

        boolean exists = false;

        try {
            AbstractComponentHandler handler = (AbstractComponentHandler)this.componentHandlers.get( hint );
            exists = (handler != null);
        } catch( Throwable t ) {
            // We can safely ignore all exceptions
        }

        if ( !exists && this.parentSelector != null ) {
            exists = this.parentSelector.isSelectable( hint );
        }
        return exists;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
     */
    public void release( final Object component ) {
        if( null == component ) {
            return;
        }

        // Was it selected on the parent ?
        if ( this.parentSelector != null &&
             this.parentSelector.canRelease(component) ) {
            this.parentSelector.release(component);

        } else {
            final AbstractComponentHandler handler =
                (AbstractComponentHandler)this.componentMapping.get( component );
    
            if( null == handler ) {
                this.getLogger().warn( "Attempted to release a " + component.getClass().getName()
                    + " but its handler could not be located." );
                return;
            }
    
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
                    this.getLogger().debug( "Error trying to release component", e );
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service( final ServiceManager componentManager )
    throws ServiceException {
        this.serviceManager = componentManager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure( final Configuration config )
    throws ConfigurationException {
        this.roleName = getRoleName(config);

        // Get default hint
        this.defaultHint = config.getAttribute(this.getDefaultHintAttributeName(), null);

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];

            Object hint = instance.getAttribute("name").trim();

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            if (compInstanceName == null) {
                // component-instance implicitly defined by the presence of the 'class' attribute
                if (classAttr == null) {
                    className = this.roleManager.getDefaultClassNameForHint(roleName, instance.getName());
                } else {
                    className = classAttr.trim();
                }

            } else {
                // component-instances names explicitly defined
                if (compInstanceName.equals(instance.getName())) {
                    className = (classAttr == null) ? null : classAttr.trim();
                } else {
                    className = this.roleManager.getDefaultClassNameForHint(roleName, instance.getName());
                }
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + hint +
                    "' at " + instance.getLocation();

                getLogger().error(message);
                throw new ConfigurationException(message);
            }
            
            this.addComponent( className, hint, instance );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() 
    throws Exception {
        super.initialize();

        List keys = new ArrayList( this.componentHandlers.keySet() );

        for( int i = 0; i < keys.size(); i++ ) {
            final Object key = keys.get( i );
            final AbstractComponentHandler handler =
                (AbstractComponentHandler)this.componentHandlers.get( key );

            try {
                handler.initialize();
            } catch( Exception e ) {
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( "Caught an exception trying to initialize "
                        + "of the component handler.", e );
                }
            }

        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        Iterator keys = this.componentHandlers.keySet().iterator();
        List keyList = new ArrayList();

        while( keys.hasNext() ) {
            Object key = keys.next();
            AbstractComponentHandler handler =
                (AbstractComponentHandler)this.componentHandlers.get( key );

            handler.dispose();

            keyList.add( key );
        }

        keys = keyList.iterator();

        while( keys.hasNext() ) {
            this.componentHandlers.remove( keys.next() );
        }

        keyList.clear();

        if ( this.parentLocator != null ) {
            this.parentLocator.release( this.parentSelector );
            this.parentLocator = null;
            this.parentSelector = null;
        }
        
        super.dispose();
    }

    /**
     * Makes the ComponentHandlers available to subclasses.
     *
     * @return A reference to the componentHandler Map.
     */
    protected Map getComponentHandlers() {
        return this.componentHandlers;
    }

    /** Add a new component to the manager.
     * @param hint the hint name for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final Object hint,
                              final Class component,
                              final Configuration configuration )
    throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException( hint.toString(),
                "Cannot add components to an initialized ComponentSelector", null );
        }

        try {
            final AbstractComponentHandler handler = getComponentHandler( this.roleName,
                                                                  component,
                                                                  configuration,
                                                                  this.serviceManager);

            handler.initialize();
            this.componentHandlers.put( hint, handler );

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug(
                    "Adding " + component.getName() + " for hint [" + hint.toString() + "]" );
            }
        } catch (ServiceException se) {
            throw se;
        } catch( final Exception e ) {
            final String message =
                "Could not set up Component for hint [ " + hint + "]";
            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, e );
            }

            throw new ServiceException( hint.toString(), message, e );
        }
    }

    /**
     * Get the name for component-instance elements (i.e. components not defined
     * by their role shortcut. If <code>null</code>, any element having a 'class'
     * attribute will be considered as a component instance.
     * <p>
     * The default here is to return <code>null</code>, and subclasses can redefine
     * this method to return particular values.
     *
     * @return <code>null</code>, but can be changed by subclasses
     */
    protected String getComponentInstanceName() {
        return null;
    }

    /**
     * Get the name of the attribute giving the class name of a component.
     * The default here is "class", but this can be overriden in subclasses.
     *
     * @return "<code>class</code>", but can be changed by subclasses
     */
    protected String getClassAttributeName() {
        return "class";
    }

    /**
     * Get the name of the attribute giving the default hint to use if
     * none is given. The default here is "default", but this can be
     * overriden in subclasses. If this method returns <code>null</code>,
     * no default hint can be specified.
     *
     * @return "<code>default</code>", but can be changed by subclasses
     */
    protected String getDefaultHintAttributeName() {
        return "default";
    }
    
    /**
     * Get the role name for this selector. This is called by <code>configure()</code>
     * to set the value of <code>this.roleName</code>.
     *
     * @return the role name, or <code>null<code> if it couldn't be determined.
     */
    protected String getRoleName(Configuration config) {
        // Get the role for this selector
        String name = config.getAttribute("role", null);
        if (name == null && this.roleManager != null) {
            name = this.roleManager.getRoleForName(config.getName());
        }

        return name;
    }

    /**
     * Get the default hint, if any for this selector.
     */
    public String getDefaultHint() {
        return this.defaultHint;
    }

    /**
     * Does this selector declare a given hint? Check is performed on the components declared for this
     * selector only, and <strong>not</strong> those potentially inherited from the parent selector.
     * 
     * @param hint the hint to check for
     * @return <code>true</code> if this selector has the specified hint
     */
    protected boolean hasDeclaredComponent(Object hint) {
        if (hint == null) {
            hint = this.defaultHint;
        }

        return this.isSelectable(hint);
    }

    /**
     * Set the ComponentLocatorImpl that allows access to a possible
     * parent of this selector
     * @param locator
     * @throws ComponentException
     */
    public void setParentLocator(ServiceManager locator, String role)
    throws ServiceException {
        if (this.parentSelector != null) {
            throw new ServiceException(null, "Parent selector is already set");
        }
        this.parentLocator = locator;
        
        // Get the parent, unwrapping it as far as needed
        Object parent = locator.lookup(role);
        
        if (parent instanceof CocoonServiceSelector) {
            this.parentSelector = (CocoonServiceSelector)parent;
        } else {
            throw new IllegalArgumentException("Parent selector is not an extended component selector (" + parent + ")");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.ExcaliburComponentSelector#canRelease(org.apache.avalon.framework.component.Component)
     */
    protected boolean canRelease(Object component) {
        if ( this.parentSelector != null &&
             this.parentSelector.canRelease(component) ) {
            return true;
        }
        return this.componentMapping.containsKey( component );
    }

    

}
