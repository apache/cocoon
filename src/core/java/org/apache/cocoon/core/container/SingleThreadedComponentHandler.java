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
 * The DefaultComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @version CVS $Id: SingleThreadedComponentHandler.java 55144 2004-10-20 12:26:09Z ugo $
 */
public class SingleThreadedComponentHandler
extends AbstractComponentHandler {

    /**
     * Create a SingleThreadedComponentHandler which manages a pool of Components
     *  created by the specified factory object.
     *
     * @param logger The logger to use
     * @param factory The factory object which is responsible for creating the components
     *                managed by the handler.
     */
    public SingleThreadedComponentHandler( final Logger logger,
                                    final ComponentFactory factory ) {
        super(logger, factory);
    }

    /**
     * Get a reference of the desired Component
     *
     * @return A component instance.
     *
     * @throws Exception If there are any problems encountered acquiring a
     *                   component instance.
     */
    protected Object doGet()
    throws Exception {
        return this.factory.newInstance();
    }

    /**
     * Return a reference of the desired Component
     *
     * @param component Component to be be put/released back to the handler.
     */
    protected void doPut( final Object component ) {
        this.decommission( component );
    }

}
