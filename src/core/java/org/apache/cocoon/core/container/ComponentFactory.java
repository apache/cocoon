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

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Factory for Avalon based components.
 *
 * @version CVS $Id: ComponentFactory.java 55172 2004-10-20 17:52:18Z cziegeler $
 */
public class ComponentFactory {
    
    /** The class which this <code>ComponentFactory</code>
     * should create.
     */
    private final Class componentClass;

    /** The Context for the component
     */
    private final Context context;

    /** The service manager for this component
     */
    private final ServiceManager serviceManager;
    
    /** The configuration for this component.
     */
    private final Configuration configuration;

    /** The parameters for this component
     */
    private Parameters parameters;
    
    private final Logger logger;
    
    private final String role;
    
    private final LoggerManager loggerManager;

    private final RoleManager roleManager;
    
    /**
     * Construct a new component factory for the specified component.
     *
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     * @param seerviceManager the service manager to pass to <code>Serviceable</code>s.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     *
     */
    public ComponentFactory( final String role,
                             final Class componentClass,
                             final Configuration configuration,
                             final ServiceManager serviceManager,
                             final Context context,
                             final Logger logger,
                             final LoggerManager loggerManager,
                             final RoleManager roleManager) {
        this.role = role;
        this.componentClass = componentClass;
        this.configuration = configuration;
        this.serviceManager = serviceManager;
        this.context = context;
        this.logger = logger;
        this.loggerManager = loggerManager;
        this.roleManager = roleManager;
    }

    /**
     * Create a new instance
     */
    public Object newInstance()
    throws Exception {
        final Object component = this.componentClass.newInstance();

        if( this.logger.isDebugEnabled() ) {
            this.logger.debug( "ComponentFactory creating new instance of " +
                    this.componentClass.getName() + "." );
        }

        if ( component instanceof LogEnabled ) {
            if( null == this.configuration ) {
                ContainerUtil.enableLogging( component, this.logger );
            } else {
                final String logger = this.configuration.getAttribute( "logger", null );
                if( null == logger ) {
                    this.logger.debug( "no logger attribute available, using standard logger" );
                    ContainerUtil.enableLogging( component, this.logger );
                } else {
                    this.logger.debug( "logger attribute is " + this.logger );
                    ContainerUtil.enableLogging( component, this.loggerManager.getLoggerForCategory( logger ) );
                }
            }
        }


        if( component instanceof Contextualizable ) {
            ContainerUtil.contextualize( component, this.context );
        }

        if( component instanceof Serviceable ) {
            ContainerUtil.service( component, this.serviceManager );
        }

        if ( component instanceof CocoonServiceSelector ) {
            ((CocoonServiceSelector)component).setLoggerManager(this.loggerManager);
            ((CocoonServiceSelector)component).setRoleManager(this.roleManager);
        }
        
        ContainerUtil.configure( component, this.configuration );

        if( component instanceof Parameterizable ) {
            if ( this.parameters == null ) {
                this.parameters = Parameters.fromConfiguration( this.configuration );
            }
            ContainerUtil.parameterize( component, this.parameters );
        }

        ContainerUtil.initialize( component );

        ContainerUtil.start( component );

        return component;
    }

    public Class getCreatedClass() {
        return this.componentClass;
    }

    /**
     * Destroy an instance
     */
    public void decommission( final Object component )
    throws Exception {
        if( this.logger.isDebugEnabled() ) {
            this.logger.debug( "ComponentFactory decommissioning instance of " +
                    this.componentClass.getName() + "." );
        }

        ContainerUtil.stop( component );
        ContainerUtil.dispose( component );
    }

}
