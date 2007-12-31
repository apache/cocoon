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

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * This component provides access to all other components used
 * throughout the portal.
 * Any component in the portal should never lookup these components
 * itself, but use this component manager instead.
 *
 * This manager allows to run differently configured portals in Cocoon
 * at the same time. This component can't be looked up using the
 * usual Avalon mechanisms, it has to be get by the {@link PortalService}.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 *
 * @version CVS $Id$
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
    Renderer getRenderer(String hint);

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
     * @deprecated Use the Avalon Contextualizable interface instead.
     */
    Context getComponentContext();
}
