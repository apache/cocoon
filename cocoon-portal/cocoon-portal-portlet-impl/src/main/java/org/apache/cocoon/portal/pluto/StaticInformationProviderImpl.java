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
package org.apache.cocoon.portal.pluto;

import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.apache.pluto.services.information.PortalContextProvider;

/**
 *
 * @version $Id$
 */
public class StaticInformationProviderImpl 
implements StaticInformationProvider { 

    protected PortalContextProvider provider;

    protected PortletDefinitionRegistry registry;
    
    /**
     * Constructor
     */
    public StaticInformationProviderImpl(PortalContextProvider provider,
                                         PortletDefinitionRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.StaticInformationProvider#getPortalContextProvider()
     */
    public PortalContextProvider getPortalContextProvider() {
        return this.provider;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.StaticInformationProvider#getPortletDefinition(org.apache.pluto.om.common.ObjectID)
     */
    public PortletDefinition getPortletDefinition(ObjectID portletGUID) {
        return this.registry.getPortletDefinition(portletGUID);
    }

}
