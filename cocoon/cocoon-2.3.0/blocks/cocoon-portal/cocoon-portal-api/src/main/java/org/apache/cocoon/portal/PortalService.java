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

import java.util.List;

import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.services.ProfileManager;
import org.apache.cocoon.portal.services.UserService;
import org.apache.cocoon.portal.services.VariableResolver;

/**
 * This is the central component of the portal. It holds the global configuration,
 * the current name of the portal and other important information.
 * Apart from configuration, the main purpose if this component is to provide a unique
 * and simple access to other portal components and services like the link
 * service or the event manager. A portal component should never lookup these services by
 * itself. It must only lookup the portal service and get the components/services
 * from there.
 * The portal service is a singleton.
 *
 * @version $Id$
 */
public interface PortalService {

    /**
     * The name of the portal - as defined in the portal configuration.
     */
    String getPortalName();

    /**
     * Return all skins
     */
    List getSkinDescriptions();

    /**
     * Get a configuration value.
     * @param key The key for the configuration.
     * @return The value of the configuration or null.
     * @since 2.2
     */
    String getConfiguration(String key);

    /**
     * Get a configuration value.
     * @param key The key for the configuration.
     * @param defaultValue The default value if no configuration for the key is available.
     * @return The value of the configuration or the default value.
     * @since 2.2
     */
    String getConfiguration(String key, String defaultValue);

    /**
     * Get a configuration value as a boolean.
     * @param key The key for the configuration.
     * @param defaultValue The default value if no configuration for the key is available.
     * @return The value of the configuration or the default value.
     * @since 2.2
     */
    boolean getConfigurationAsBoolean(String key, boolean defaultValue);

    /**
     * Get the link service.
     */
    LinkService getLinkService();

    /**
     * Get the current profile manager.
     */
    ProfileManager getProfileManager();

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

    /** Get the event converter. */
    EventConverter getEventConverter();

    /**
     * Get the portal manager
     * @since 2.1.8
     */
    PortalManager getPortalManager();

    /**
     * Get the current request context.
     * @since 2.2
     */
    RequestContext getRequestContext();

    /**
     * Get the user service.
     */
    UserService getUserService();

    /**
     * Get the variable resolver
     * @since 2.2
     */
    VariableResolver getVariableResolver();
}
