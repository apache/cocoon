/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.precept;

import java.util.HashMap;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.precept.preceptors.PreceptorBuilder;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 18, 2002
 * @version CVS $Id: InstanceFactory.java,v 1.2 2003/03/16 17:49:04 vgritsenko Exp $
 */
public class InstanceFactory extends AbstractLogEnabled implements Component, Configurable, Composable, ThreadSafe {

    public final static String ROLE = "org.apache.cocoon.precept.InstanceFactory";

    private ComponentManager manager = null;
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

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public Instance createInstance(String name) {
        getLogger().debug("creating instance [" + String.valueOf(name) + "]");
        Configuration instanceConf = (Configuration) instanceConfigurationMap.get(name);
        ComponentSelector instanceSelector = null;
        Instance instance = null;
        try {
            instanceSelector = (ComponentSelector) manager.lookup(Instance.ROLE + "Selector");
            instance = (Instance) instanceSelector.select(instanceConf.getAttribute("impl"));

            Configuration builderConf = instanceConf.getChild("preceptor");
            if (builderConf != null) {
                ComponentSelector preceptorBuilderSelector = null;
                PreceptorBuilder preceptorBuilder = null;
                try {
                    preceptorBuilderSelector = (ComponentSelector) manager.lookup(PreceptorBuilder.ROLE + "Selector");
                    preceptorBuilder = (PreceptorBuilder) preceptorBuilderSelector.select(builderConf.getAttribute("impl"));

                    String uri = builderConf.getAttribute("uri");

                    getLogger().debug("building preceptor from [" + String.valueOf(uri) + "]");

                    //FIXME: use a resolver here
                    Preceptor newPreceptor = preceptorBuilder.buildPreceptor(uri);

                    instance.setPreceptor(newPreceptor);
                }
                catch (ComponentException e) {
                    if (preceptorBuilderSelector != null) {
                        getLogger().error("could not get preceptor builder", e);
                    }
                    else {
                        getLogger().error("could not get preceptor builder selector", e);
                    }
                }
                catch (Exception e) {
                    getLogger().error("", e);
                    e.printStackTrace(System.out);
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
        catch (ComponentException e) {
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
