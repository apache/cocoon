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
package org.apache.cocoon.configuration;

import java.util.Properties;

/**
 *
 * @version CVS $Revision: 1.33 $ $Date: 2004/04/03 23:55:54 $
 */
public class Settings {

    protected String classloaderClassName;
    protected boolean initClassloader;
    protected String[] forceProperties;
    protected String configuration;    
    protected String loggingConfiguration;
    protected String cocoonLogger;    
    protected String logLevel;
    protected String loggerClassName;
    protected boolean allowReload;
    protected String[] loadClasses;    
    protected boolean enableUploads;
    protected String uploadDirectory;    
    protected boolean autosaveUploads;
    protected boolean overwriteUploads;
    protected long maxUploadSize;
    protected String cacheDirectory;
    protected String workDirectory;
    protected String[] extraClasspaths;
    protected String parentServiceManagerClassName;
    protected boolean showTime;
    protected boolean manageExceptions;
    protected String formEncoding;

    public Settings(Properties properties) {
        // ignore
    }
}
