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

/**
 * Defines constants used within portal classes.
 *
 * @version $Id$
 */
public abstract class Constants {

    /** Configuration key for full screen enabled (default is true). */
    public static final String CONFIGURATION_FULL_SCREEN_ENABLED = "enable-full-screen";
    public static final boolean DEFAULT_CONFIGURATION_FULL_SCREEN_ENABLED = true;

    /** Configuration key for maximized enabled (default is true). */
    public static final String CONFIGURATION_MAXIMIZED_ENABLED = "enable-maximized";
    public static final boolean DEFAULT_CONFIGURATION_MAXIMIZED_ENABLED = true;

    /** Configuration key to use ajax (default is false). */
    public static final String CONFIGURATION_USE_AJAX = "use-ajax";
    public static final boolean DEFAULT_CONFIGURATION_USE_AJAX = false;

    /** Configuration key to find all skins. */
    public static final String CONFIGURATION_SKINS_PATH = "skins-path";
    public static final String DEFAULT_CONFIGURATION_SKINS_PATH = "skins";
}
