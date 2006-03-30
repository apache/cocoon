/* 
 * Copyright 2005 The Apache Software Foundation
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * This is a simple service manager implementation that just serves one
 * single component.
 * @since 2.2
 * @version $Id$
 */
public final class SingleComponentServiceManager
implements ServiceManager, Disposable {

    protected final ServiceManager parent;
    protected final Object component;
    protected final String role;

    public SingleComponentServiceManager(ServiceManager parent,
                                         Object         component,
                                         String         role) {
        this.parent = parent;
        this.component = component;
        this.role = role;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String key) {
        if ( this.role.equals(key) ) {
            return true;
        }
        if ( this.parent != null ) {
            return this.parent.hasService(key);
        }
        return false;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String key) throws ServiceException {
        if ( this.role.equals(key) ) {
            return this.component;
        }
        if ( this.parent != null ) {
            return this.parent.lookup(key);
        }
        throw new ServiceException("Cocoon", "Component for key '" + key + "' not found.");
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object component) {
        if ( component != this.component && parent != null ) {
            this.parent.release(component);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        ContainerUtil.dispose(this.parent);
    }
}