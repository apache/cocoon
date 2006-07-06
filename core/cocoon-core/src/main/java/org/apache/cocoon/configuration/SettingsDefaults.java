/*
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.cocoon.configuration;

/**
 * This object defines the default values for the {@link Settings}.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class SettingsDefaults {

    /** Default value for {@link Settings#isManageExceptions()}. */
    public static final boolean MANAGE_EXCEPTIONS = true;

    /** The default running mode. */
    public static final String DEFAULT_RUNNING_MODE = "dev";

    /** The default configuration location. */
    public static final String DEFAULT_CONFIGURATION = "/WEB-INF/cocoon.xconf";

    /** The default logging configuration location. */
    public static final String DEFAULT_LOGGING_CONFIGURATION = "/WEB-INF/log4j.xconf";

    /**
     * Default value for {@link Settings#isReloadingEnabled(String)} parameter (false).
     */
    public static final boolean RELOADING_ENABLED_DEFAULT = false;

    /**
     * Default value for {@link Settings#isEnableUploads()} parameter (false).
     */
    public static final boolean ENABLE_UPLOADS = false;
    public static final boolean SAVE_UPLOADS_TO_DISK = true;
    public static final int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    public static final boolean SHOW_TIME = false;
    public static final boolean HIDE_SHOW_TIME = false;

    /**
     * Default value for {@link Settings#isShowVersion()} parameter (true).
     */
    public static final boolean SHOW_COCOON_VERSION = true;

    public static final long DEFAULT_CONFIGURATION_RELOAD_DELAY = 1000;

    public static final String DEFAULT_CONTAINER_ENCODING = "ISO-8859-1";
}
