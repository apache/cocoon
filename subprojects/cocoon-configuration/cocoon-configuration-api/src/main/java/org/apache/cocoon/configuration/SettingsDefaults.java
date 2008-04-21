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

/**
 * This object defines the default values for the {@link Settings}.
 *
 * @version $Id$
 * @since 1.0
 */
public abstract class SettingsDefaults {

    /**
     * The default running mode.
     * @see Settings#getRunningMode()
     */
    public static final String DEFAULT_RUNNING_MODE = "prod";

    /**
     * Default value for {@link Settings#isReloadingEnabled(String)} parameter (false).
     */
    public static final boolean RELOADING_ENABLED_DEFAULT = false;

    /**
     * Default reload delay for configurations in milliseconds.
     * @see Settings#getReloadDelay(String)
     */
    public static final long DEFAULT_CONFIGURATION_RELOAD_DELAY = 1000;

    /**
     * The default encoding for the web container.
     * @see Settings#getContainerEncoding()
     */
    public static final String DEFAULT_CONTAINER_ENCODING = "ISO-8859-1";
}
