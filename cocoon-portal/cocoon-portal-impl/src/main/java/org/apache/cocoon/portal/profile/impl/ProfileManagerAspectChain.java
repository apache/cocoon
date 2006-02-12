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
package org.apache.cocoon.portal.profile.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.profile.ProfileManagerAspect;

/**
 * This chain holds all configured aspects for a profile manager.
 * @since 2.2
 * @version $Id$
 */
public final class ProfileManagerAspectChain {

    protected List aspects = new ArrayList(3);

    protected List configs = new ArrayList(3);

    public void configure(ServiceSelector selector,
                          Configuration   conf)
    throws ConfigurationException {
        if ( conf != null ) {
            Configuration[] aspects = conf.getChildren("aspect");
            for(int i=0; i < aspects.length; i++) {
                final Configuration current = aspects[i];
                final String role = current.getAttribute("type");
                try {
                    ProfileManagerAspect rAspect = (ProfileManagerAspect) selector.select(role);
                    this.aspects.add(rAspect);               
                    Parameters aspectConfiguration = Parameters.fromConfiguration(current);
                    this.configs.add(aspectConfiguration);

                } catch (ServiceException se) {
                    throw new ConfigurationException("Unable to lookup profile manager aspect: " + role, se);
                }
            }
        } else {
            throw new ConfigurationException("No aspects configured.");
        }
    }

    public Iterator getIterator() {
        return this.aspects.iterator();
    }

    public Iterator getConfigIterator() {
        return this.configs.iterator();
    }

    public void dispose(ServiceSelector selector) {
        Iterator i = this.aspects.iterator();
        while (i.hasNext()) {
            final Object component = i.next();
            selector.release(component);
        }
        this.aspects.clear();
        this.configs.clear();
    }
}
