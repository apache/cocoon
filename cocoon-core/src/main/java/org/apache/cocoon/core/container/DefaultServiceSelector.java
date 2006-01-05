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
package org.apache.cocoon.core.container;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.components.Preloadable;

/**
 * Default component selector for Cocoon's components. This selector "flattens" its declaration
 * by adding them as components of its containing ServiceManager. This allows a smooth transition towards
 * a fully flat configuration by allowing the use of selectors in legacy components and also
 * declaration of hinted components (i.e. with a role of type "rolename/hint") in the service manager.
 *
 * @version $Id$
 * @since 2.2
 */
public class DefaultServiceSelector extends AbstractLogEnabled implements ThreadSafe, Preloadable, Serviceable, Configurable, ServiceSelector {

    /** Synthetic hint to alias the default component */
    public static final String DEFAULT_HINT = "$default$";

    private CoreServiceManager manager;
    private RoleManager roleManager;
    private String roleName;
    private String rolePrefix;
    
    public void service(ServiceManager manager) throws ServiceException {
        try {
            this.manager = (CoreServiceManager)manager;
        } catch (ClassCastException cce) {
            throw new ServiceException ("DefaultServiceSelector", 
                                        "A DefaultServiceSelector can only be hosted by a CoreServiceManager");
        }
    }
    
    public void setRole(String role) {
        this.roleName = role;
    }
    
    public void setRoleManager(RoleManager roles) {
        this.roleManager = roles;
    }

    public void configure(Configuration config) throws ConfigurationException {

        if (roleName == null) {
            throw new ConfigurationException("No role given for DefaultServiceSelector at " + config.getLocation());
        }
        
        // Remove "Selector" suffix, if any and add a trailing "/"
        if (roleName.endsWith("Selector")) {
            this.rolePrefix = roleName.substring(0, roleName.length() - 8) + "/";
        } else {
            this.rolePrefix = roleName + "/";
        }

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];
            ComponentInfo info = null;

            String key = instance.getAttribute("name");

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            if (compInstanceName == null) {
                // component-instance implicitly defined by the presence of the 'class' attribute
                if (classAttr == null) {
                    info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
                    className = info.getServiceClassName();
                } else {
                    className = classAttr;
                }

            } else {
                // component-instances names explicitly defined
                if (compInstanceName.equals(instance.getName())) {
                    className = (classAttr == null) ? null : classAttr;
                } else {
                    info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
                    className = info.getServiceClassName();
                }
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + key +
                    "' at " + instance.getLocation();

                getLogger().error(message);
                throw new ConfigurationException(message);
            }
            
            // Add this component in the manager
            this.manager.addComponent(this.rolePrefix + key, className, instance, info);
        }
        
        // Register default key, if any
        String defaultKey = config.getAttribute(this.getDefaultKeyAttributeName(), null);
        if (defaultKey != null) {
            try {
                this.manager.addRoleAlias(this.rolePrefix + defaultKey, this.rolePrefix + DEFAULT_HINT);
            } catch (ServiceException e) {
                throw new ConfigurationException("Cannot set default to " + defaultKey + " at " + config.getLocation(), e);
            }
        }
    }
    
    public Object select(Object hint) throws ServiceException {
        String key = (hint == null) ? DEFAULT_HINT : hint.toString();

        return this.manager.lookup(this.rolePrefix + key);
    }

    public boolean isSelectable(Object hint) {
        String key = hint == null ? DEFAULT_HINT : hint.toString();
        
        return key != null && this.manager.hasService(this.rolePrefix + key);
    }

    public void release(Object obj) {
        this.manager.release(obj);
    }

    // ---------------------------------------------------------------
    /**
     * Get the name for component-instance elements (i.e. components not defined
     * by their role shortcut. If <code>null</code>, any element having a 'class'
     * attribute will be considered as a component instance.
     * <p>
     * The default here is to return <code>null</code>, and subclasses can redefine
     * this method to return particular values.
     *
     * @return <code>null</code>, but can be changed by subclasses
     */
    protected String getComponentInstanceName() {
        return null;
    }

    /**
     * Get the name of the attribute giving the class name of a component.
     * The default here is "class", but this can be overriden in subclasses.
     *
     * @return "<code>class</code>", but can be changed by subclasses
     */
    protected String getClassAttributeName() {
        return "class";
    }

    /**
     * Get the name of the attribute giving the default key to use if
     * none is given. The default here is "default", but this can be
     * overriden in subclasses. If this method returns <code>null</code>,
     * no default key can be specified.
     *
     * @return "<code>default</code>", but can be changed by subclasses
     */
    protected String getDefaultKeyAttributeName() {
        return "default";
    }
    
    /**
     * A special factory for <code>DefaultServiceSelector</code>, that passes it the
     * <code>RoleManager</code> and its role name.
     */
    public static class Factory extends ComponentFactory {
        private final String role;
        
        public Factory(ComponentEnvironment env, ComponentInfo info, String role) 
        throws Exception {
            super(env, info);
            this.role = role;
        }
        
        protected void setupInstance(Object object)
        throws Exception {
            DefaultServiceSelector component = (DefaultServiceSelector)object;
            
            ContainerUtil.enableLogging(component, this.environment.logger);
            ContainerUtil.contextualize(component, this.environment.context);
            ContainerUtil.service(component, this.environment.serviceManager);
            
            component.setRoleManager(this.environment.roleManager);
            component.setRole(this.role);
            
            ContainerUtil.configure(component, this.serviceInfo.getConfiguration());
            ContainerUtil.initialize(component);
            ContainerUtil.start(component);
        }
    }
}
