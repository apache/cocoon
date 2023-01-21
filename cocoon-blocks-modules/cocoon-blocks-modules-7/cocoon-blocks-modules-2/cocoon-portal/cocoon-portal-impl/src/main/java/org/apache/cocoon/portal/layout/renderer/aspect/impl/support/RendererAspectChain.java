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
package org.apache.cocoon.portal.layout.renderer.aspect.impl.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;

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
public final class RendererAspectChain extends AspectChain {

    /** The list of the configuration objects for each renderer aspects. */
    protected final List configs = new ArrayList(3);

    public RendererAspectChain(Class aClass) {
        super(aClass);
    }

    public Iterator getConfigurationIterator() {
        return this.configs.iterator();
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.support.AspectChain#addAspect(java.lang.Object, java.util.Properties, int)
     */
    public void addAspect(Object aspect, Properties config, int index) throws PortalException {
        super.addAspect(aspect, config, index);
        final RendererAspect rendererAspect = (RendererAspect)aspect;
        final Properties props = (config == null ? EMPTY_PROPERTIES : config);
        this.configs.add(rendererAspect.prepareConfiguration(props));
    }

}
