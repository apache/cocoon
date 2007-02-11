/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;

/**
 * This chain holds all configured renderer aspects for one renderer.
 *
 * <h2>Configuration</h2>
 * <table><tbody>
 *  <tr><th>aspect</th>
 *      <td>Multiple aspect renderer configurations. Required attribute
 *          <code>type</code>. Nested configuration must contain parameters
 *          for aspect rederer.
 *      </td>
 *      <td>req</td><td>Configuration</td><td><code>null</code></td>
 *  </tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public final class RendererAspectChain {

    /** The list of renderer aspects. */
    protected List aspects = new ArrayList(3);

    /** The list of the configuration obejcts for each renderer aspects. */
    protected List configs = new ArrayList(3);

    public void configure(ServiceSelector selector, Configuration conf) 
    throws ConfigurationException {
        if ( conf != null ) {
            final Configuration[] aspectConfigs = conf.getChildren("aspect");
            for(int i=0; i < aspectConfigs.length; i++) {
                final Configuration current = aspectConfigs[i];
                final String role = current.getAttribute("type");
                try {
                    RendererAspect rAspect = (RendererAspect) selector.select(role);
                    this.aspects.add(rAspect);               
                    Parameters aspectConfiguration = Parameters.fromConfiguration(current);
                    Object compiledConf = rAspect.prepareConfiguration(aspectConfiguration);
                    this.configs.add(compiledConf);

                } catch (ParameterException pe) {
                    throw new ConfigurationException("Unable to configure renderer aspect: " + role, pe);
                } catch (ServiceException se) {
                    throw new ConfigurationException("Unable to lookup renderer aspect: " + role, se);
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
            selector.release(i.next()); 
        }
        this.aspects.clear();
    }
}
