/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.cocoon.components.LifecycleHelper;

/**
 * A very simple ServiceSelector for ThreadSafe services.
 *
 * @version $Id$
 */
public class SimpleServiceSelector extends AbstractLogEnabled
                                   implements ServiceSelector, Configurable,
                                              Serviceable, Disposable, Contextualizable {

    private Context context;
    private ServiceManager serviceManager;

    private final String hintShortHand;
    private final Class componentClass;
    private Map components = new HashMap();

    //
    // Lifecycle
    //

    public SimpleServiceSelector(String hintShortHand, Class componentClass) {
        this.hintShortHand = hintShortHand;
        this.componentClass = componentClass;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration[] componentConfs = configuration.getChildren(hintShortHand);
        for (int i = 0; i < componentConfs.length; i++) {
            String name = componentConfs[i].getAttribute("name");
            String src = componentConfs[i].getAttribute("src");

            Class clazz;
            try {
                clazz = Class.forName(src);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Class not found: " + src + ", declared at " + componentConfs[i].getLocation(), e);
            }

            if (!componentClass.isAssignableFrom(clazz)) {
                throw new ConfigurationException("The class \"" + src + "\" is of an incorrect type, it should implement or extend " + componentClass.getName());
            }

            Object component;
            try {
                component = clazz.newInstance();
                LifecycleHelper.setupComponent(component,
					                           getLogger(),
                                               context,
					                           serviceManager,
					                           componentConfs[i]);
            } catch (Exception e) {
                throw new ConfigurationException("Error creating " + hintShortHand + " declared at " + componentConfs[i].getLocation(), e);
            }

            components.put(name, component);
        }
    }

    public void dispose() {
        Iterator i = components.values().iterator();
        while (i.hasNext()) {
            Object service = i.next();
            if (service instanceof Disposable) {
                try {
                    ((Disposable) service).dispose();
                } catch (Exception e) {
                    getLogger().error("Error disposing service " + service, e);
                }
            }
        }
        components.clear();
    }

    //
    // ServiceSelector
    //

    public Object select(Object hint) throws ServiceException {
        if (!isSelectable(hint)) {
            throw new ServiceException((String) hint, "Non-existing component for this hint");
        }
        String stringHint = (String) hint;
        return components.get(stringHint);
    }

    public boolean isSelectable(Object hint) {
        String stringHint = (String) hint;
        return components.containsKey(stringHint);
    }

    public void release(Object o) {
    }

}
