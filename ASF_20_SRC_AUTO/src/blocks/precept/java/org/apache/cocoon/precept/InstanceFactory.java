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

package org.apache.cocoon.precept;

import java.util.HashMap;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.precept.preceptors.PreceptorBuilder;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 18, 2002
 * @version CVS $Id: InstanceFactory.java,v 1.5 2004/03/05 13:02:18 bdelacretaz Exp $
 */
public class InstanceFactory extends AbstractLogEnabled
        implements Component, Configurable, Serviceable, ThreadSafe {

    public final static String ROLE = "org.apache.cocoon.precept.InstanceFactory";

    private ServiceManager manager = null;
    private HashMap instanceConfigurationMap;

    public void configure(Configuration conf) throws ConfigurationException {
        instanceConfigurationMap = new HashMap();
        Configuration[] instances = conf.getChildren("instance");
        if (instances.length > 0) {
            for (int p = 0; p < instances.length; p++) {
                Configuration instance = instances[p];
                String name = instance.getAttribute("name");
                getLogger().debug("registering instance [name=" + name
                                  + ";impl=" + instance.getAttribute("impl") + "]");
                if (instanceConfigurationMap.containsKey(name)) {
                    getLogger().error("instance [name=" + name + "] appears more than once");
                    throw new ConfigurationException("instance [name=" + name + "] appears more than once");
                }

                instanceConfigurationMap.put(name, instance);
            }
        } else {
            getLogger().warn("no instances are configured");
        }
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public Instance createInstance(String name) {
        getLogger().debug("creating instance [" + String.valueOf(name) + "]");
        Configuration instanceConf = (Configuration) instanceConfigurationMap.get(name);
        ServiceSelector instanceSelector = null;
        Instance instance = null;
        try {
            instanceSelector = (ServiceSelector) manager.lookup(Instance.ROLE + "Selector");
            instance = (Instance) instanceSelector.select(instanceConf.getAttribute("impl"));

            Configuration builderConf = instanceConf.getChild("preceptor");
            if (builderConf != null) {
                ServiceSelector preceptorBuilderSelector = null;
                PreceptorBuilder preceptorBuilder = null;
                try {
                    preceptorBuilderSelector = (ServiceSelector) manager.lookup(PreceptorBuilder.ROLE + "Selector");
                    preceptorBuilder = (PreceptorBuilder) preceptorBuilderSelector.select(builderConf.getAttribute("impl"));

                    String uri = builderConf.getAttribute("uri");

                    getLogger().debug("building preceptor from [" + String.valueOf(uri) + "]");

                    //FIXME: use a resolver here
                    Preceptor newPreceptor = preceptorBuilder.buildPreceptor(uri);

                    instance.setPreceptor(newPreceptor);
                }
                catch (ServiceException e) {
                    if (preceptorBuilderSelector != null) {
                        getLogger().error("could not get preceptor builder", e);
                    }
                    else {
                        getLogger().error("could not get preceptor builder selector", e);
                    }
                }
                catch (Exception e) {
                    getLogger().error("", e);
                }
                finally {
                    manager.release(preceptorBuilder);
                    manager.release(preceptorBuilderSelector);
                }
            }
        }
        catch (ConfigurationException e) {
            getLogger().error("", e);
        }
        catch (ServiceException e) {
            getLogger().error("could not get instance selector", e);
        }
        finally {
            //manager.release(instance);
            //should be released while session invalidation
            manager.release(instanceSelector);
        }
        return (instance);
    }

}
