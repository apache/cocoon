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
package org.apache.cocoon.servlet.multipart;

import java.io.File;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;

import org.apache.cocoon.configuration.Settings;

/**
 * Configuration object for the multipart filter.
 *
 * @version $Id$
 * @since 2.2
 */
public class MultipartConfigurationHelper {

    public static final boolean ENABLE_UPLOADS = false;
    public static final boolean SAVE_UPLOADS_TO_DISK = true;
    public static final int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    String KEY_UPLOADS_DIRECTORY = "org.apache.cocoon.uploads.directory";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    String KEY_UPLOADS_ENABLE = "org.apache.cocoon.uploads.enable";

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    String KEY_UPLOADS_AUTOSAVE = "org.apache.cocoon.uploads.autosave";

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    String KEY_UPLOADS_OVERWRITE = "org.apache.cocoon.uploads.overwrite";

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    String KEY_UPLOADS_MAXSIZE = "org.apache.cocoon.uploads.maxsize";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    protected boolean enableUploads;

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    protected String uploadDirectory;

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    protected boolean autosaveUploads;

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    protected String overwriteUploads;

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    protected int maxUploadSize;

    public MultipartConfigurationHelper() {
        this.enableUploads = ENABLE_UPLOADS;
        this.autosaveUploads = SAVE_UPLOADS_TO_DISK;
        this.maxUploadSize = MAX_UPLOAD_SIZE;        
    }

    /**
     * Configure this from the settings object.
     * @param settings
     */
    public void configure(Settings settings, Log logger) {
        String value;

        value = settings.getProperty(KEY_UPLOADS_ENABLE);
        if (value != null) {
            setEnableUploads(BooleanUtils.toBoolean(value));
        }
        value = settings.getProperty(KEY_UPLOADS_DIRECTORY);
        if (value != null) {
            setUploadDirectory(value);
        }
        value = settings.getProperty(KEY_UPLOADS_AUTOSAVE);
        if (value != null) {
            setAutosaveUploads(BooleanUtils.toBoolean(value));
        }
        value = settings.getProperty(KEY_UPLOADS_OVERWRITE);
        if (value != null) {
            setOverwriteUploads(value);
        }
        value = settings.getProperty(KEY_UPLOADS_MAXSIZE);
        if (value != null) {
            setMaxUploadSize(Integer.valueOf(value).intValue());
        }

        final String uploadDirParam = getUploadDirectory();
        File uploadDir;
        if (uploadDirParam != null) {
            uploadDir = new File(uploadDirParam);
            if (logger.isDebugEnabled()) {
                logger.debug("Using upload-directory " + uploadDir);
            }
        } else {
            uploadDir = new File(settings.getWorkDirectory(), "upload-dir" + File.separator);
            if (logger.isDebugEnabled()) {
                logger.debug("Using default upload-directory " + uploadDir);
            }
        }

        uploadDir.mkdirs();
        setUploadDirectory(uploadDir.getAbsolutePath());
    }

    /**
     * @return Returns the autosaveUploads.
     * @see #KEY_UPLOADS_AUTOSAVE
     */
    public boolean isAutosaveUploads() {
        return this.autosaveUploads;
    }

    /**
     * @return Returns the enableUploads.
     * @see #KEY_UPLOADS_ENABLE
     */
    public boolean isEnableUploads() {
        return this.enableUploads;
    }

    /**
     * @return Returns the maxUploadSize.
     * @see #KEY_UPLOADS_MAXSIZE
     */
    public int getMaxUploadSize() {
        return this.maxUploadSize;
    }

    /**
     * @return Returns the overwriteUploads.
     * @see #KEY_UPLOADS_OVERWRITE
     */
    public String getOverwriteUploads() {
        return this.overwriteUploads;
    }

    /**
     * @return Returns the uploadDirectory.
     * @see #KEY_UPLOADS_DIRECTORY
     */
    public String getUploadDirectory() {
        return this.uploadDirectory;
    }

    public boolean isAllowOverwrite() {
        final String value = this.getOverwriteUploads();
        if ("deny".equalsIgnoreCase(value)) {
            return false;
        } else if ("allow".equalsIgnoreCase(value)) {
            return true;
        } else {
            // either rename is specified or unsupported value - default to rename.
            return false;
        }
    }

    public boolean isSilentlyRename() {
        final String value = getOverwriteUploads();
        if ("deny".equalsIgnoreCase(value)) {
            return false;
        } else if ("allow".equalsIgnoreCase(value)) {
            return false; // ignored in this case
        } else {
            // either rename is specified or unsupported value - default to rename.
            return true;
        }
    }

    /**
     * @param autosaveUploadsValue The autosaveUploads to set.
     */
    public void setAutosaveUploads(boolean autosaveUploadsValue) {
        this.autosaveUploads = autosaveUploadsValue;
    }

    /**
     * @param enableUploads The enableUploads to set.
     */
    public void setEnableUploads(boolean enableUploads) {
        this.enableUploads = enableUploads;
    }

    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * @param overwriteUploads The overwriteUploads to set.
     */
    public void setOverwriteUploads(String overwriteUploads) {
        this.overwriteUploads = overwriteUploads;
    }
    
    /**
     * @param uploadDirectory The uploadDirectory to set.
     */
    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
}
