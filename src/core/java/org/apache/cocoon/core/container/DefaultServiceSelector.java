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

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ServiceInfo;

/**
 * Default component selector for Cocoon's components. This selector "flattens" its declaration
 * by adding them as components of its containing ServiceManager. This allows a smooth transition towards
 * a fully flat configuration by allowing the use of selectors in legacy components and also
 * declaration of hinted components (i.e. with a role of type "rolename/hint") in the service manager.
 *
 * @version SVN $Id$
 */
public class DefaultServiceSelector extends AbstractLogEnabled implements ThreadSafe, Serviceable, Configurable, ServiceSelector {

    private CocoonServiceManager manager;
    private RoleManager roleManager;
    private String roleName;
    private String rolePrefix;
    private String defaultKey;
    private String location;
    
    public void service(ServiceManager manager) throws ServiceException {
        try {
            this.manager = (CocoonServiceManager)manager;
        } catch (ClassCastException cce) {
            throw new ServiceException ("A FlatServiceSelector can only be hosted by a CocoonServiceManager");
        }
    }
    
    public void setRole(String role) {
        this.roleName = role;
    }
    
    public void setRoleManager(RoleManager roles) {
        this.roleManager = roles;
    }

    public void configure(Configuration config) throws ConfigurationException {
        // Get the role for this selector
        
        this.location = config.getLocation();
        
        if (roleName == null) {
            throw new ConfigurationException("No role given for DefaultServiceSelector at " + this.location);
        }
        
        // Remove "Selector" suffix, if any and add a trailing "/"
        if (roleName.endsWith("Selector")) {
            this.rolePrefix = roleName.substring(0, roleName.length() - 8) + "/";
        } else {
            this.rolePrefix = roleName + "/";
        }

        // Get default key
        this.defaultKey = config.getAttribute(this.getDefaultKeyAttributeName(), null);

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];

            String key = instance.getAttribute("name");

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            if (compInstanceName == null) {
                // component-instance implicitly defined by the presence of the 'class' attribute
                if (classAttr == null) {
                    final ServiceInfo info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
                    className = info.getServiceClassName();
                } else {
                    className = classAttr;
                }

            } else {
                // component-instances names explicitly defined
                if (compInstanceName.equals(instance.getName())) {
                    className = (classAttr == null) ? null : classAttr;
                } else {
                    final ServiceInfo info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
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
            this.manager.addComponent(className, this.rolePrefix + key, instance);
        }
    }
    
    public Object select(Object hint) throws ServiceException {
        String key = (hint == null) ? this.defaultKey : hint.toString();
        
        if (key == null) {
            throw new ServiceException(roleName, "Hint is null and no default hint provided for selector at " + this.location);
        }

        return this.manager.lookup(this.rolePrefix + key);
    }

    public boolean isSelectable(Object hint) {
        String key = hint == null ? this.defaultKey : hint.toString();
        
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
        private String role;

        public Factory(ServiceManager serviceManager, Context context, Logger logger,
                LoggerManager loggerManager, RoleManager roleManager, ServiceInfo info, String role) {
            super(serviceManager, context, logger, loggerManager, roleManager, info);
            this.role = role;
        }
        
        public Object newInstance()
        throws Exception {
            final DefaultServiceSelector component = (DefaultServiceSelector)this.serviceInfo.getServiceClass().newInstance();

            ContainerUtil.enableLogging(component, this.logger);
            ContainerUtil.contextualize(component, this.context);
            ContainerUtil.service(component, this.serviceManager);
            
            component.setRoleManager(this.roleManager);
            component.setRole(this.role);
            
            ContainerUtil.configure(component, this.serviceInfo.getConfiguration());
            ContainerUtil.initialize(component);
            ContainerUtil.start(component);

            return component;
        }
    }
}
