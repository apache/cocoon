/*
 * Created on Jan 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.cocoon.core.container;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.components.ServiceInfo;

/**
 * @author sylvain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
            ServiceInfo info = new ServiceInfo();
            info.setConfiguration(config);
            info.setServiceClassName(className);

            this.delegate = AbstractComponentHandler.getComponentHandler(role, compEnv, info);
            this.delegate.initialize();
        }
        
        return this.delegate;
    }
    
    private ComponentHandler getDelegateRE() {
        try {
            return getDelegate();
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot get delegate handler", e);
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
    public ServiceInfo getInfo() {
        if (this.delegate == null) {
            final ServiceInfo info = new ServiceInfo();
            info.setServiceClassName(className);
            info.setConfiguration(config);
            return info;
        } 
        return this.delegate.getInfo();
    }
}
