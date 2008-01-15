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
package org.apache.cocoon.servletservice;

import java.net.URI;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;

public class AbsoluteServletConnectionTestCase extends ContainerTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        //setting up environment for AbsoluteServletConnection
        CallStack.enter();
        CallStack.getCurrentFrame().setAttribute(CallStackHelper.SUPER_CALL, new Boolean(false));
        CallFrameHelper.setContext(getContext());
    }
    
    protected void addComponents(ConfigurationInfo info) throws Exception {
        super.addComponents(info);
        //Add MockServlet bean
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setComponentClassName(MockServlet.class.getName());
        componentInfo.setRole(MockServlet.beanId);
        componentInfo.setConfiguration(new DefaultConfiguration("-"));
        info.addComponent(componentInfo);
    }
    
    /**
     * Tests if absolute URI conforms our definition of absolute URI when servlet protocol is used.
     * Test for COCOON-2161.
     */
    public void testURI() throws Exception {
        String path = "/path";
        AbsoluteServletConnection connection = new AbsoluteServletConnection(MockServlet.beanId, path, null);
        URI uri = new URI(MockServlet.beanId + AbsoluteServletConnection.ABSOLUTE_SERVLET_SOURCE_POSTFIX, null, path, null, null);
        uri = new URI("servlet", uri.toASCIIString(), null);
        assertEquals("Check if absolute URI is constructed properly", uri, connection.getURI());
    }
}
