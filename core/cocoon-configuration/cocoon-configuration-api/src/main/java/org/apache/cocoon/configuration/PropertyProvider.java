/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.configuration;

import java.util.Properties;

/**
 * This is an interface for custom components delivering properties to
 * configure Cocoon.
 * This component must be setup as a Spring bean in the root application
 * context using {@link #ROLE} as the bean identifier.
 * The registered provider is asked once on startup for a set of properties.
 *
 * @version $Id$
 * @since 1.0
 */
public interface PropertyProvider {

    /**
     * The bean must be registered with this name.
     */
    String ROLE = PropertyProvider.class.getName();

    /**
     * Provide the properties.
     *
     * @param settings    The already loaded settings.
     * @param runningMode The current running mode.
     * @param path        A path specifying the application context the settings are applied to.
     *                    A path of null indicates the root application context.
     * @return            The additional properties.
     */
    Properties getProperties(Settings settings, String runningMode, String path);
}
