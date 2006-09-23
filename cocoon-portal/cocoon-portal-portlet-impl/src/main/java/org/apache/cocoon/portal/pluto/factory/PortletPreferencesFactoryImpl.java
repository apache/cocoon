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
package org.apache.cocoon.portal.pluto.factory;

import javax.portlet.PortletPreferences;

import org.apache.pluto.core.impl.PortletPreferencesImpl;
import org.apache.pluto.factory.PortletPreferencesFactory;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * Our own preferences factory.
 *
 * @version $Id$
 */
public class PortletPreferencesFactoryImpl
    extends AbstractFactory
    implements PortletPreferencesFactory {

    /**
     * @see org.apache.pluto.factory.PortletPreferencesFactory#getPortletPreferences(java.lang.Integer, org.apache.pluto.om.entity.PortletEntity)
     */
    public PortletPreferences getPortletPreferences(Integer methodId, 
                                                    PortletEntity portletEntity) {
        return new PortletPreferencesImpl(methodId, portletEntity);
    }

    /**
     * @see org.apache.pluto.factory.PortletPreferencesFactory#getPortletPreferences(java.lang.Integer, org.apache.pluto.om.portlet.PortletDefinition)
     */
    public PortletPreferences getPortletPreferences(Integer methodId, 
                                                    PortletDefinition portletDefinition) {
        return new PortletPreferencesImpl(methodId, portletDefinition);
    }
}
