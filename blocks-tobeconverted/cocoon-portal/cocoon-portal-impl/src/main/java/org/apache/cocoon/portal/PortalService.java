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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the central component in the portal. It holds the configuration
 * of the portal, the current name etc.
 * The main use of this component is to get other components of the portal,
 * like the link service or the event manager.
 * This component is a singleton.
 *
 * @version $Id$
 */
public interface PortalService extends PortalComponentManager {

    /** The role to lookup this component. */
    String ROLE = PortalService.class.getName();

    /** Configuration key for full screen enabled (default is true). */
    String CONFIGURATION_FULL_SCREEN_ENABLED = "enable-full-screen";

    /** Configuration key for maximized enabled (default is true). */
    String CONFIGURATION_MAXIMIZED_ENABLED = "enable-maximized";

    /**
     * The name of the portal - as defined in the portal configuration.
     */
    String getPortalName();

    /**
     * Return the value of an attribute.
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getAttribute(String key);

    /**
     * Set an attribute.
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setAttribute(String key, Object value);

    /**
     * Remove an attribute.
     * @param key The key of the attribute
     */
    Object removeAttribute(String key);

    /**
     * Return the names of all attributes.
     */
    Iterator getAttributeNames();

    /**
     * Return the value of a temporary attribute.
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getTemporaryAttribute(String key);

    /**
     * Set a temporary attribute.
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setTemporaryAttribute(String key, Object value);

    /**
     * Remove a temporary attribute.
     * @param key The key of the attribute
     */
    Object removeTemporaryAttribute(String key);

    /**
     * Return the names of all temporary attributes.
     */
    Iterator getTemporaryAttributeNames();

    /**
     * Return the component manager for the current portal.
     * @deprecated Starting with 2.2, this service extends the PortalComponentManager.
     */
    PortalComponentManager getComponentManager();

    /**
     * Change the default layout key for most functions
     */
    void setDefaultLayoutKey(String layoutKey);

    /**
     * Get the default layout key
     */
    String getDefaultLayoutKey();

    /**
     * Return all skins
     */
    List getSkinDescriptions();

    /**
     * Return the current object model.
     * @since 2.1.8
     */
    Map getObjectModel();

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
}
