/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.impl;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.CocoonTestCase;
import org.apache.cocoon.core.container.spring.ComponentInfo;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.impl.PortalServiceImpl;
import org.apache.cocoon.servlet.CocoonServlet;

/**
 * $Id$ 
 */
public class DefaultEventManagerTestCase extends CocoonTestCase {

    protected DefaultEventManager eventManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.eventManager = (DefaultEventManager)this.getBeanFactory().getBean(EventManager.ROLE);
    }

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
     * @see org.apache.cocoon.core.container.ContainerTestCase#addContext(org.apache.avalon.framework.context.DefaultContext)
     */
    protected void addContext(DefaultContext context) {
        super.addContext(context);
        context.put(CocoonServlet.CONTEXT_SERVLET_CONFIG, this.getServletConfig());
    }

    /**
     * @see org.apache.cocoon.CocoonTestCase#addComponents(org.apache.cocoon.core.container.spring.ConfigurationInfo)
     */
    protected void addComponents(ConfigurationInfo info) throws Exception {
        super.addComponents(info);
        // Add portal service
        final ComponentInfo portalServiceInfo = new ComponentInfo();
        portalServiceInfo.setComponentClassName(PortalServiceImpl.class.getName());
        portalServiceInfo.setRole(PortalService.ROLE);
        portalServiceInfo.setConfiguration(this.getPortalServiceConfig());
        info.addComponent(portalServiceInfo);

        // Add event manager
        final ComponentInfo component = new ComponentInfo();
        component.setComponentClassName(DefaultEventManager.class.getName());
        component.setRole(EventManager.ROLE);
        component.setConfiguration(new DefaultConfiguration("-"));
        info.addComponent(component);
    }

    public void testEventReceiver() throws Exception {
        EventReceiver1 receiver = new EventReceiver1();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(2, receiver.receiveCount);
    }

    public static final class EventReceiver1 implements Receiver {

        public int receiveCount;

        public void inform(Event event, PortalService service) {
            receiveCount++;
        }
    }

    public static class Event1 implements Event {
        // dummy event
    }
}
