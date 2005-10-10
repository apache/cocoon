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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.container.CoreServiceManager;

/**
 * A component handler for instances created outside the container.
 * 
 * @version $Id$
 * @since 2.2
 */
public class InstanceComponentHandler extends AbstractComponentHandler {

    private Object obj;

    /**
     * Creates a new ComponentHandler.
     */
    public InstanceComponentHandler(Logger logger, Object obj) {
        super(new ComponentInfo(), logger);
        // For info.getLocation() to work properly
        this.getInfo().setConfiguration(CoreServiceManager.EMPTY_CONFIGURATION);
        this.obj = obj;
    }

    public boolean isSingleton() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.AbstractComponentHandler#doGet()
     */
    protected Object doGet() throws Exception {
        return this.obj;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.AbstractComponentHandler#doPut(java.lang.Object)
     */
    protected void doPut(Object component) throws Exception {
        // nothing
    }
    
    protected void doInitialize() {
        // nothing to do here
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#dispose()
     */
    public void dispose() {
        if (this.obj instanceof Disposable) {
            ((Disposable)this.obj).dispose();
        }
    }
}
