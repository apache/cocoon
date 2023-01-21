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
package org.apache.cocoon.portal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.CocoonTestCase;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.impl.PortalServiceImpl;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * Abstract test case class that can be used as a base for own portal
 * test cases.
 * It provides a service manager with a setup portal service etc.
 *
 * $Id$
 */
public abstract class AbstractPortalTestCase extends CocoonTestCase {

    protected ServletConfig getServletConfig() {
        return new ServletConfig() {

            public String getInitParameter(String arg0) {
                return null;
            }

            public Enumeration getInitParameterNames() {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }

            public ServletContext getServletContext() {
                return new MockContext();
            }

            public String getServletName() {
                return "cocoon";
            }

        };
    }

    protected Configuration getPortalServiceConfig() {
        DefaultConfiguration rootConfig = new DefaultConfiguration("component");
        DefaultConfiguration portalConfig = new DefaultConfiguration("portal");
        portalConfig.setAttribute("name", "portaltest");
        rootConfig.addChild(portalConfig);
        rootConfig.makeReadOnly();
        return rootConfig;
    }

    /**
     * @see org.apache.cocoon.CocoonTestCase#addComponents(org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo)
     */
    protected void addComponents(ConfigurationInfo info) throws Exception {
        super.addComponents(info);
        // ProcessInfoProvider
        final ComponentInfo processInfoProvider = new ComponentInfo();
        processInfoProvider.setComponentClassName(MockProcessInfoProvider.class.getName());
        processInfoProvider.setRole(ProcessInfoProvider.ROLE);
        info.addComponent(processInfoProvider);
        // RendererMap
        final ComponentInfo rendererMap = new ComponentInfo();
        rendererMap.setComponentClassName(HashMap.class.getName());
        rendererMap.setRole(Renderer.class.getName()+"Map");
        info.addComponent(rendererMap);
        // CopletAdapterMap
        final ComponentInfo copletAdapterMap = new ComponentInfo();
        copletAdapterMap.setComponentClassName(HashMap.class.getName());
        copletAdapterMap.setRole(CopletAdapter.class.getName()+"Map");
        info.addComponent(copletAdapterMap);
        // Add portal service
        final ComponentInfo portalServiceInfo = new ComponentInfo();
        portalServiceInfo.setComponentClassName(PortalServiceImpl.class.getName());
        portalServiceInfo.setRole(PortalService.class.getName());
        portalServiceInfo.setConfiguration(this.getPortalServiceConfig());
        info.addComponent(portalServiceInfo);
    }
}
