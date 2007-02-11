/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.woody.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;

/**
 * A very simple ServiceSelector for ThreadSafe services.
 * 
 * @version $Id: SimpleServiceSelector.java,v 1.7 2004/03/05 13:02:34 bdelacretaz Exp $
 */
public class SimpleServiceSelector extends AbstractLogEnabled implements ServiceSelector, Configurable, LogEnabled,
        Serviceable, Disposable {
    private final String hintShortHand;
    private final Class componentClass;
    private Map components = new HashMap();
    private ServiceManager serviceManager;

    public SimpleServiceSelector(String hintShortHand, Class componentClass) {
        this.hintShortHand = hintShortHand;
        this.componentClass = componentClass;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration[] componentConfs = configuration.getChildren(hintShortHand);
        for (int i = 0; i < componentConfs.length; i++) {
            String name = componentConfs[i].getAttribute("name");
            String src = componentConfs[i].getAttribute("src");

            Class clazz = null;
            try {
                clazz = Class.forName(src);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Class not found: " + src + ", declared at " + componentConfs[i].getLocation(), e);
            }

            if (!componentClass.isAssignableFrom(clazz))
                throw new ConfigurationException("The class \"" + src + "\" is of an incorrect type, it should implement or exted " + componentClass.getName());

            Object component = null;
            try {
                component = clazz.newInstance();
                LifecycleHelper lifecycleHelper = new LifecycleHelper(getLogger(), null, serviceManager, null, componentConfs[i]);
                lifecycleHelper.setupComponent(component);
            } catch (Exception e) {
                throw new ConfigurationException("Error creating " + hintShortHand + " declared at " + componentConfs[i].getLocation(), e);
            }

            components.put(name, component);
        }
    }

    public Object select(Object hint) throws ServiceException {
        if (!isSelectable(hint))
            throw new ServiceException((String)hint, "Non-existing component for this hint");
        String stringHint = (String)hint;
        return components.get(stringHint);
    }

    public boolean isSelectable(Object hint) {
        String stringHint = (String)hint;
        return components.containsKey(stringHint);
    }

    public void release(Object o) {
    }

    public void dispose() {
        Iterator serviceIt = components.values().iterator();
        while (serviceIt.hasNext()) {
            Object service = serviceIt.next();
            if (service instanceof Disposable) {
                try {
                    ((Disposable)service).dispose();
                } catch (Exception e) {
                    getLogger().error("Error disposing service " + service, e);
                }
            }
        }
    }
}
