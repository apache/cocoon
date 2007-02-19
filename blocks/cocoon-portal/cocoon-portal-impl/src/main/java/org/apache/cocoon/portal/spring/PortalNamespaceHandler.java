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
package org.apache.cocoon.portal.spring;

import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 
 * @version $Id$
 */
public class PortalNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        this.registerBeanDefinitionParser("event-aspects", new AspectsBeanDefinitionParser(EventAspect.class.getName()));
        this.registerBeanDefinitionParser("renderer-aspects", new AspectsBeanDefinitionParser(RendererAspect.class.getName()));
        this.registerBeanDefinitionParser("portal-manager-aspects", new AspectsBeanDefinitionParser(PortalManagerAspect.class.getName()));
        this.registerBeanDefinitionParser("profile-manager-aspects", new AspectsBeanDefinitionParser(ProfileManagerAspect.class.getName()));
    }
}
