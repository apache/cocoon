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
package org.apache.cocoon.core;

/**
 * The settings (configuration) for the Cocoon core are described through the {@link BaseSettings}
 * interface and the {@link DynamicSettings} interface.
 * Whereas the settings of the {@link BaseSettings} object can't be changed at runtime,
 * the settings of the {@link DynamicSettings} object are mutable.
 *
 * @version $Id$
 * @since 2.2
 */
public interface DynamicSettings {

    /**
     * Default value for {@link #isReloadingEnabled(String)} parameter (false).
     */
    boolean RELOADING_ENABLED_DEFAULT = false;

    /**
     * Default value for {@link #isEnableUploads()} parameter (false).
     */
    boolean ENABLE_UPLOADS = false;
    boolean SAVE_UPLOADS_TO_DISK = true;
    int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    boolean SHOW_TIME = false;
    boolean HIDE_SHOW_TIME = false;

    /**
     * Default value for {@link #isShowVersion()} parameter (true).
     */
    boolean SHOW_COCOON_VERSION = true;

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    String KEY_RELOADING = "reloading";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    String KEY_UPLOADS_ENABLE = "uploads.enable";

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    String KEY_UPLOADS_AUTOSAVE = "uploads.autosave";

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    String KEY_UPLOADS_OVERWRITE = "uploads.overwrite";

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    String KEY_UPLOADS_MAXSIZE = "uploads.maxsize";

    /**
     * Allow adding processing time to the response
     */
    String KEY_SHOWTIME = "showtime";

    /**
     * If true, processing time will be added as an HTML comment
     */
    String KEY_HIDE_SHOWTIME = "hideshowtime";

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    String KEY_SHOW_VERSION = "show-version";

    /**
     * Delay between reload checks for the configuration
     */
    String KEY_RELOAD_DELAY = "reload-delay";

    /**
     * @return Returns the hideShowTime.
     * @see #KEY_HIDE_SHOWTIME
     */
    boolean isHideShowTime();

    /**
     * @return Returns the showCocoonVersion.
     * @see #KEY_SHOW_VERSION
     */
    boolean isShowVersion();

    /**
     * This method can be used by components to query if they are
     * configured to check for reloading.
     * @param type The type of the component that wants to check for reload.
     * @return Returns if reloading is enabled for this component.
     * @see #KEY_RELOADING
     */
    boolean isReloadingEnabled(String type);

    /**
     * This method can be used by components to get the configured
     * delay period inbetween checks.
     * @param type The type of the component that wants to check for reload.
     * @return Returns the delay inbetween checks in milliseconds.
     * @see #KEY_RELOAD_DELAY
     */
    long getReloadDelay(String type);

    /**
     * @return Returns the autosaveUploads.
     * @see #KEY_UPLOADS_AUTOSAVE
     */
    boolean isAutosaveUploads();

    /**
     * @return Returns the enableUploads.
     * @see #KEY_UPLOADS_ENABLE
     */
    boolean isEnableUploads();

    /**
     * @return Returns the maxUploadSize.
     * @see #KEY_UPLOADS_MAXSIZE
     */
    int getMaxUploadSize();

    /**
     * @return Returns the overwriteUploads.
     * @see #KEY_UPLOADS_OVERWRITE
     */
    String getOverwriteUploads();

    /**
     * @return Returns the showTime.
     * @see #KEY_SHOWTIME
     */
    boolean isShowTime();

    boolean isAllowOverwrite();

    boolean isSilentlyRename();

}
