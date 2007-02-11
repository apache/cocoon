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
package org.apache.cocoon.core.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.apache.cocoon.core.container.DefaultServiceSelector;
import org.apache.cocoon.core.container.handler.ComponentHandler;
import org.apache.cocoon.core.container.handler.PoolableComponentHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * An extension of {@link org.apache.cocoon.core.container.CoreServiceManager} that registers some
 * of its components as OSGi services.
 * <p>
 * The component role is used to find a component's interface. If the role is a hinted one (e.g.
 * "org.apache.cocoon.generation.Generator/file", then the hint is registered as the "component.hint"
 * property of the OSGi service.
 * <p>
 * Only those components whose configuration has an <code>exported="true"</code> attribute are exported.
 * An important constraint is that such components be singleton (either real singletons or singleton proxies
 * to non-singleton components).
 * 
 * @version $Id$
 * @since 2.2
 */
public class OSGiCoreServiceManager extends CoreServiceManager {
    
    BundleContext ctx;
    
    public OSGiCoreServiceManager(ServiceManager parent, ClassLoader classloader, BundleContext ctx) {
        super(parent, classloader);
        this.ctx = ctx;
    }

    /**
     * Catch component declarations and register exported ones as OSGi services.
     */
    public void addComponent(String role, String className, Configuration configuration, ComponentInfo info) throws ConfigurationException {
        super.addComponent(role, className, configuration, info);

        Class clazz;
        try {
            clazz = this.componentEnv.loadClass(className);
        } catch(ClassNotFoundException cnfe) {
            throw new ConfigurationException("Cannot load class " + className + " for component at " +
                                             configuration.getLocation(), cnfe);
        }

        // The DefaultServiceSelector just add its children, no need to add it as a service
        if (DefaultServiceSelector.class.isAssignableFrom(clazz))
            return;
        if (configuration.getAttributeAsBoolean("exported", true)) {
            ComponentHandler handler = (ComponentHandler)super.componentHandlers.get(role);
            // Shouldn't PoolableComponentHandler be marked as a singleton?
            if (handler.isSingleton() ||
                handler instanceof PoolableComponentHandler) {
                this.addService(role, handler);
            } else {
                throw new ConfigurationException("Only singleton services and thread safe pool proxies can be exported as OSGi services, at " +
                                                 configuration.getLocation() +
                                                 " handler=" + handler);
            }
        }
    }
    
    /**
     * Catch a component insertion and register it as OSGi services.
     */
    public void addInstance(String role, Object instance) throws ServiceException {
        super.addInstance(role, instance);
        ComponentHandler handler = (ComponentHandler)super.componentHandlers.get(role);
        this.addService(role, handler);
    }

    /**
     * Register a component as an OSGi service.
     */
    protected void addService(String role, ComponentHandler handler) {
        String itfName = OSGiServiceManager.getServiceInterface(role);
        String hint = OSGiServiceManager.getServiceHint(role);
        Dictionary dict = null;
        if (hint != null) {
            dict = new Hashtable();
            dict.put(OSGiServiceManager.HINT_PROPERTY, hint);
        }

        Object service = new ComponentHandlerFactory(handler);
        ctx.registerService(itfName, service, dict);
    }
    
    /**
     * An OSGi service factory implemented on top of a {@link ComponentHandler}.
     *
     */
    public static class ComponentHandlerFactory implements ServiceFactory {
        
        private ComponentHandler handler;

        public ComponentHandlerFactory(ComponentHandler handler) {
            this.handler = handler;
        }

        public Object getService(Bundle bundle, ServiceRegistration reg) {
            try {
                return handler.get();
            } catch (Exception e) {
                throw new CoreOSGIServiceException("Cannot get service", e);
            }
        }

        public void ungetService(Bundle bundle, ServiceRegistration reg, Object obj) {
            try {
                handler.put(obj);
            } catch (Exception e) {
                throw new CoreOSGIServiceException("Cannot unget service", e);
            }
        }
    }
}
