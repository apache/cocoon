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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.CoreResourceNotFoundException;
import org.apache.cocoon.core.container.ComponentEnvironment;
import org.apache.cocoon.util.JMXUtils;

/**
 * 
 * @version $Id$
 * @since 2.2
 */
public class LazyHandler implements ComponentHandler {
    
    private String role;
    private String className;
    private Configuration config;
    private ComponentEnvironment compEnv;
    
    private ComponentHandler delegate;
    
    public LazyHandler(String role, String className, Configuration configuration, ComponentEnvironment environment) {
        this.role = role;
        this.className = className;
        this.config = configuration;
        this.compEnv = environment;
    }
    
    private ComponentHandler getDelegate() throws Exception {
        if (this.delegate == null) {
//            System.err.println("######## " + System.identityHashCode(compEnv.serviceManager) + " creating handler for " + this.role);
            ComponentInfo info = new ComponentInfo();
            info.setConfiguration(config);
            info.setServiceClassName(className);
            info.setJmxDomain(JMXUtils.findJmxDomain(info.getJmxDomain(), this.compEnv.serviceManager));
            info.setJmxName(JMXUtils.findJmxName(info.getJmxName(), className));
            info.setRole(this.role);
            this.delegate = AbstractComponentHandler.getComponentHandler(role, compEnv, info);
            this.delegate.initialize();
            JMXUtils.setupJmxFor(this.delegate, info);
        }
        
        return this.delegate;
    }
    
    private ComponentHandler getDelegateRE() {
        try {
            return getDelegate();
        } catch (Exception e) {
            throw new CoreResourceNotFoundException("Cannot get delegate handler", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#get()
     */
    public Object get() throws Exception {
        return getDelegate().get();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#put(java.lang.Object)
     */
    public void put(Object component) throws Exception {
        getDelegate().put(component);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#isSingleton()
     */
    public boolean isSingleton() {
        return getDelegateRE().isSingleton();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#canBeDisposed()
     */
    public boolean canBeDisposed() {
        // We can always be disposed if handler was never used
        if (this.delegate == null) {
            return true;
        } 
        return getDelegateRE().canBeDisposed();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#dispose()
     */
    public void dispose() {
        // Dispose only if handler was actually used
        if (this.delegate != null) {
            this.delegate.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#initialize()
     */
    public void initialize() throws Exception {
        // nothing (delegate is initialized when created)
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#getInfo()
     */
    public ComponentInfo getInfo() {
        if (this.delegate == null) {
            final ComponentInfo info = new ComponentInfo();
            info.setServiceClassName(className);
            info.setConfiguration(config);
            info.setJmxDomain(JMXUtils.findJmxDomain(info.getJmxDomain(), this.compEnv.serviceManager));
            info.setJmxName(JMXUtils.findJmxName(info.getJmxName(), className));
            info.setRole(role);
            return info;
        } 
        return this.delegate.getInfo();
    }
}
