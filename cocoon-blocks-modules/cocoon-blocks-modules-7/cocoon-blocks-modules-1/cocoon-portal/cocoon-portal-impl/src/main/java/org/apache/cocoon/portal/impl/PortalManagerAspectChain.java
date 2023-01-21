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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;

/**
 * This chain holds all configured aspects for a portal manager.
 * @since 2.1.8
 * @version $Id$
 */
public final class PortalManagerAspectChain {

    protected List aspects = new ArrayList(3);

    protected List configs = new ArrayList(3);

    public void configure(ServiceSelector     aspectSelector,
                          ServiceSelector     adapterSelector,
                          Configuration       conf,
                          PortalManagerAspect endAspect,
                          Parameters          endAspectParameters) 
    throws ConfigurationException {
        if ( conf != null ) {
            final Configuration[] aspectConfigs = conf.getChildren("aspect");
            for(int i=0; i < aspectConfigs.length; i++) {
                final Configuration current = aspectConfigs[i];
                final String role = current.getAttribute("type", null);
                PortalManagerAspect pAspect;
                if ( role != null ) {
                    if ( aspectSelector == null ) {
                        throw new ConfigurationException("No selector for aspects defined.");
                    }
                    try {
                        pAspect = (PortalManagerAspect) aspectSelector.select(role);                        
                    } catch (ServiceException se) {
                        throw new ConfigurationException("Unable to lookup aspect " + role, current, se);
                    }
                } else {
                    final String adapterName = current.getAttribute("adapter", null);
                    if ( adapterName == null ) {
                        throw new ConfigurationException("Aspect configuration requires either a type or an adapter attribute.", current);
                    }
                    try {
                        pAspect = (PortalManagerAspect)adapterSelector.select(adapterName);
                    } catch (ServiceException se) {
                        throw new ConfigurationException("Unable to lookup coplet adapter " + adapterName, current, se);
                    }
                }
                this.aspects.add(pAspect);               
                Parameters aspectConfiguration = Parameters.fromConfiguration(current);
                this.configs.add(aspectConfiguration);
            }
        }
        this.aspects.add(endAspect);
        this.configs.add(endAspectParameters);
    }

    public Iterator getIterator() {
        return this.aspects.iterator();
    }

    public Iterator getConfigIterator() {
        return this.configs.iterator();
    }

    public void dispose(ServiceSelector aspectSelector, ServiceSelector adapterSelector) {
        Iterator i = this.aspects.iterator();
        while (i.hasNext()) {
            final Object component = i.next();
            if ( component instanceof CopletAdapter ) {
                adapterSelector.release(component);
            } else {
                aspectSelector.release(component);
            }
        }
        this.aspects.clear();
        this.configs.clear();
    }
}
