/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cocoon.components.treeprocessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * This is the selector used to select/create node builders.
 *
 * @version $Id$
 * @since 2.2
 */
public class NodeBuilderSelector extends AbstractLogEnabled
                                 implements Serviceable, Configurable, Initializable,
                                            Contextualizable {

    /** The application context for components */
    protected ServiceManager serviceManager;

    /** The application context for components */
    protected Context context;

    /** Used to map roles to component infos. */
    protected final Map componentInfos = Collections.synchronizedMap(new HashMap());

    /** All singletons. */
    protected final Map singletons = Collections.synchronizedMap(new HashMap());

    protected static class BuilderInfo {
        public Configuration configuration;
        public Class         builderClass;
    }


    /**
     * @see Contextualizable#contextualize(Context)
     */
    public void contextualize(final Context avalonContext) {
        this.context = avalonContext;
    }

    /**
     * @see Serviceable#service(ServiceManager)
     */
    public void service(final ServiceManager componentManager)
    throws ServiceException {
        this.serviceManager = componentManager;
    }

    /**
     * @see Configurable#configure(Configuration)
     */
    public void configure(final Configuration config)
    throws ConfigurationException {
        final Configuration[] instances = config.getChildren();
        for (int i = 0; i < instances.length; i++) {
            final Configuration instance = instances[i];
            final String name = instance.getAttribute("name").trim();
            final String className = instance.getAttribute("builder").trim();
            try {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Adding builder (" + name + " = " + className + ")");
                }

                final Class clazz = getClass().getClassLoader().loadClass(className);

                final BuilderInfo info = new BuilderInfo();
                info.builderClass = clazz;
                info.configuration = instance;

                this.componentInfos.put(name, info);

            } catch (final ClassNotFoundException cnfe) {
                final String message = "Could not get class (" + className + ") for builder " + name + " at " +
                                       instance.getLocation();
                throw new ConfigurationException(message, cnfe);
            } catch (final Exception e) {
                final String message = "Unexpected exception when setting up builder " + name + " at " + instance.getLocation();
                throw new ConfigurationException(message, e);
            }
        }
    }

    /**
     * @see Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        final Iterator i = this.componentInfos.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry entry = (Map.Entry) i.next();
            final BuilderInfo info = (BuilderInfo) entry.getValue();
            if (ThreadSafe.class.isAssignableFrom(info.builderClass)) {
                this.singletons.put(entry.getKey(), this.createComponent(info));
            }
        }
    }

    public Object getBuilder(String name)
    throws Exception {
        Object component = this.singletons.get(name);
        if (component == null) {
            final BuilderInfo info = (BuilderInfo) this.componentInfos.get(name);
            if (info == null) {
                throw new Exception("Node builder selector could not find builder for key [" + name + "]");
            }

            // Retrieve the instance of the requested component
            try {
                component = createComponent(info);
            } catch (Exception e) {
                throw new Exception("Unable to create new builder: " + name, e);
            }
        }

        return component;
    }

    /**
     * Create a new component.
     */
    protected Object createComponent(BuilderInfo info)
    throws Exception {
        final Object component = info.builderClass.newInstance();
        ContainerUtil.contextualize(component, this.context);
        ContainerUtil.service(component, this.serviceManager);
        ContainerUtil.configure(component, info.configuration);
        if (component instanceof Parameterizable) {
            ContainerUtil.parameterize(component, Parameters.fromConfiguration(info.configuration));
        }
        ContainerUtil.initialize(component);
        return component;
    }
}
