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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.ComponentLocator;

/**
 * Wrapper for a service manager.
 *
 * @version Id: ComponentLocatorWrapper.java 179038 2005-05-30 08:19:24Z cziegeler $
 * @since 2.2
 */
final public class ComponentLocatorWrapper
implements ComponentLocator {

    protected final ServiceManager manager;

    public ComponentLocatorWrapper(ServiceManager m) {
        this.manager = m;
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#hasComponent(java.lang.String)
     */
    public boolean hasComponent(String key) {
        return this.manager.hasService(key);
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#getComponent(java.lang.String)
     */
    public Object getComponent(String key) 
    throws ProcessingException {
        try {
            return this.manager.lookup(key);
        } catch (ServiceException se) {
            throw new ProcessingException("Unable to lookup component for key: " + key, se);
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#release(java.lang.Object)
     */
    public void release(Object component) {
        this.manager.release(component);
    }

}