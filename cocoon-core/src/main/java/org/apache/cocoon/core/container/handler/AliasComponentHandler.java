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
import org.apache.cocoon.core.container.CoreServiceManager;

/**
 * A component handler used to alias roles: it delegates all its calls to another
 * handler.
 * 
 * @version $Id$
 * @since 2.2
 */
public class AliasComponentHandler extends AbstractComponentHandler {

    ComponentHandler aliasedHandler;
    
    public AliasComponentHandler(Logger logger, ComponentHandler aliasedHandler) {
        super(new ComponentInfo(), logger);
        getInfo().setConfiguration(CoreServiceManager.EMPTY_CONFIGURATION);
        this.aliasedHandler = aliasedHandler;
    }

    protected Object doGet() throws Exception {
        return this.aliasedHandler.get();
    }

    protected void doPut(Object component) throws Exception {
        this.aliasedHandler.put(component);
    }
    
    protected void doInitialize() {
        // nothing to do here
    }
    
    public boolean isSingleton() {
        return this.aliasedHandler.isSingleton();
    }
}
