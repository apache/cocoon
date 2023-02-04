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
package org.apache.cocoon.components.store.impl;

import java.io.File;
import java.io.IOException;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.store.impl.AbstractFilesystemStore;

/**
 * Stores objects on the filesystem: String objects as text files,
 * all other objects are serialized.
 *
 * @version $Id$
 */
public final class FilesystemStore extends AbstractFilesystemStore {

    private static final boolean USE_CACHE_DIRECTORY = false;
    private static final boolean USE_WORK_DIRECTORY = false;
    
    /** The default logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    private Settings settings;
    private boolean useCacheDirectory = USE_CACHE_DIRECTORY;
    private boolean useWorkDirectory = USE_WORK_DIRECTORY;
    private String directory;

    protected File workDir;
    protected File cacheDir;

    
    /**
     * @param settings
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * @param useCacheDirectory
     */
    public void setUseCacheDirectory(boolean useCacheDirectory) {
        this.useCacheDirectory = useCacheDirectory;
    }

    /**
     * @param useWorkDirectory
     */
    public void setUseWorkDirectory(boolean useWorkDirectory) {
        this.useWorkDirectory = useWorkDirectory;
    }

    public void init() throws Exception {
        enableLogging(new CLLoggerWrapper(this.logger));
        this.workDir = new File(settings.getWorkDirectory());
        this.cacheDir = new File(settings.getCacheDirectory());
        
        try {
            if (this.useCacheDirectory) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + cacheDir);
                }
                setDirectory(cacheDir);
            } else if (this.useWorkDirectory) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + workDir);
                }
                setDirectory(workDir);
            } else if (this.directory != null) {
                this.directory = IOUtils.getContextFilePath(workDir.getPath(), this.directory);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + this.directory);
                }
                setDirectory(new File(this.directory));
            } else {
                try {
                    // Legacy: use working directory by default
                    setDirectory(workDir);
                } catch (IOException e) {
                    // Legacy: Always was ignored
                }
            }
        } catch (IOException e) {
            throw new Exception("Unable to set directory", e);
        }
    }
}
