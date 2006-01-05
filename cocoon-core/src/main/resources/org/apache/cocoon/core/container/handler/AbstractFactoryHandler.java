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
package org.apache.cocoon.core.container.handler;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.container.ComponentFactory;

/**
 * This class acts like a Factory to instantiate the correct version
 * of the component handler that you need.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractFactoryHandler extends AbstractComponentHandler {
    
    /** This factory is used to created new objects */
    protected final ComponentFactory factory;
    
    /**
     * Creates a new ComponentHandler.
     */
    public AbstractFactoryHandler(ComponentInfo info, Logger logger, ComponentFactory factory) {
        super(info, logger);
        this.factory = factory;
    }

    /**
     * Decommission a component
     * @param component Object to be decommissioned
     */
    protected void decommission( final Object component ) {
        try {
            this.factory.decommission( component );
        } catch( final Exception e ) {
            if( this.logger.isWarnEnabled() ) {
                this.logger.warn( "Error decommissioning component: "
                    + this.factory.getCreatedClass().getName(), e );
            }
        }
    }
    
}
