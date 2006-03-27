/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.util;

import java.util.List;

/**
 * This object holds the property settings for Cocoon. This interface is loosely based on the Settings interface
 * introduced in 2.2 but is note equivalent to it as it is only meant to hold configuration properties.
 *
 * @version $Id$
 */
public interface Settings {
        /** The role to lookup this bean. */
    String ROLE = Settings.class.getName();

        /** Name of the property specifying a custom user properties file. */
    String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @return The value of the property or null.
     */
    String getProperty(String key);

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @param defaultValue The value returned if the property is not available.
     * @return The value of the property or if the property cannot
     *         be found the default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return all available properties starting with the prefix.
     * @param keyPrefix The prefix each property name must have.
     * @return A list of property names (including the prefix) or
     *         an empty list.
     */
    List getProperties(String keyPrefix);

    /**
     * Return all available properties
     * @return A list of all property names or an empty list.
     */
    List getProperties();

    /**
     * Return the number of properties that have been defined
     * @return The number of properties that have been defined.
     */
    int size();
}
