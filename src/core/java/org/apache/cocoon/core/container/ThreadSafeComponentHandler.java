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

import org.apache.avalon.framework.logger.Logger;

/**
 * The ThreadSafeComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @version CVS $Id: ThreadSafeComponentHandler.java 55144 2004-10-20 12:26:09Z ugo $
 */
public class ThreadSafeComponentHandler
extends AbstractComponentHandler {
    
    private Object instance;

    /**
     * Create a ThreadSafeComponentHandler which manages a single instance
     * of an object return by the component factory.
     * @param logger The logger to use
     * @param factory The factory object which is responsible for creating the components
     *                managed by the handler.
     */
    public ThreadSafeComponentHandler( final Logger logger,
                                       final ComponentFactory factory ) {
        super(logger, factory);
    }
    
    public void initialize() 
    throws Exception {
        if( this.initialized ) {
            return;
        }
        if( this.instance == null ) {
            this.instance = this.factory.newInstance();
        }
        super.initialize();
    }

    /**
     * Get a reference of the desired Component
     */
    protected Object doGet()
    throws Exception {
        return this.instance;
    }

    /**
     * Return a reference of the desired Component
     */
    protected void doPut( final Object component ) {
        // nothing to do
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose() {
        try {
            this.factory.decommission( this.instance );
            this.instance = null;
        } catch( final Exception e ) {
            if( this.logger.isWarnEnabled() ) {
                this.logger.warn( "Error decommissioning component: " +
                                  this.factory.getCreatedClass().getName(), e );
            }
        }
        super.dispose();
    }
}
