/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.services.aspects.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Reusable implementation of an aspect chain.
 *
 * @since 2.2
 * @version $Id$
 */
public final class AspectChain {

    protected static Properties EMPTY_PROPERTIES = new Properties();

    /** The aspects. */
    protected List aspects = new ArrayList(3);

    /** The configuration for the aspects. */
    protected List configs = new ArrayList(3);

    /** Do we have any aspects? */
    protected boolean process = false;

    public void configure(ServiceManager manager,
                          Class          aspectClass,
                          Configuration  conf)
    throws ConfigurationException {
        if ( conf != null ) {
            final Configuration[] aspectConfigs = conf.getChild("aspects").getChildren("aspect");
            for(int i=0; i < aspectConfigs.length; i++) {
                this.process = true;
                final Configuration current = aspectConfigs[i];
                final String role = current.getAttribute("type");
                try {
                    final Object aspect = manager.lookup(aspectClass.getName() + '/' + role);
                    if ( !aspectClass.isInstance(aspect) ) {
                        throw new ConfigurationException("Configured aspect is not an instance of class " + aspectClass.getName() + " : " + role);
                    }
                    this.aspects.add(aspect);               
                    this.configs.add(Parameters.toProperties(Parameters.fromConfiguration(current)));

                } catch (ServiceException se) {
                    throw new ConfigurationException("Unable to lookup aspect (" + aspectClass.getName() + "): " + role, se);
                }
            }
        }
    }

    public void addAspect(Object aspect, Properties config) {
        this.addAspect(aspect, config, -1);
    }

    public void addAspect(Object aspect, Properties config, int index) {
        final Properties aspectConfig = (config == null ? EMPTY_PROPERTIES : config);
        this.process = true;
        if ( index == -1 ) {
            this.aspects.add(aspect);
            this.configs.add(aspectConfig);
        } else {
            this.aspects.add(index, aspect);
            this.configs.add(index, aspectConfig);
        }
    }

    public boolean hasAspects() {
        return this.process;
    }

    public Iterator getIterator() {
        return this.aspects.iterator();
    }

    public Iterator getConfigIterator() {
        return this.configs.iterator();
    }

    public void dispose(ServiceManager manager) {
        final Iterator i = this.aspects.iterator();
        while (i.hasNext()) {
            final Object component = i.next();
            manager.release(component);
        }
        this.aspects.clear();
        this.configs.clear();
    }
}
