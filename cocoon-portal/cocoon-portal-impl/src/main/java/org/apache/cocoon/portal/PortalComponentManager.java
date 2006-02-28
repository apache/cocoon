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
package org.apache.cocoon.portal;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * This component provides access to the most important portal components
 * like the link service, all renderers etc.
 * These components should never be looked up directly using the Cocoon
 * mechanisms (ServiceManager) - always use the PortalComponentManager
 * and the {@link PortalService}. 
 *
 * @version $Id$
 */
public interface PortalComponentManager {

    /**
     * Get the link service.
     */
    LinkService getLinkService();

    /**
     * Get the current profile manager.
     */
    ProfileManager getProfileManager();

    /**
     * Get the renderer.
     */
    Renderer getRenderer(String name);

    /**
     * Get the coplet adapter.
     */
    CopletAdapter getCopletAdapter(String name);

    /**
     * Get the coplet factory.
     */
    CopletFactory getCopletFactory();

    /**
     * Get the layout factory
     */
    LayoutFactory getLayoutFactory();

    /**
     * Get the event manager
     */
    EventManager getEventManager();

    /**
     * Get the portal manager
     * @since 2.1.8
     */
    PortalManager getPortalManager();

    /**
     * Return the component context.
     * @since 2.1.8
     * @deprecated Use the core object instead.
     */
    Context getComponentContext();
}
