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
package org.apache.cocoon.servlet;

import java.util.StringTokenizer;

import javax.servlet.ServletConfig;

import org.apache.cocoon.configuration.Settings;
import org.apache.commons.lang.BooleanUtils;

/**
 * This helper class initializes the {@link Settings} object from the servlet
 * configuration.
 * 
 * @version SVN $Id: Settings.java 125448 2005-01-17 22:28:43Z cziegeler $
 */
public class SettingsHelper {

    private SettingsHelper() {
        //  no instantiation
    }
    
    public static void fill(Settings s, ServletConfig config) {
        // logging
        s.setCocoonLogger(config.getInitParameter("cocoon-logger"));
        s.setAccessLogger(config.getInitParameter("servlet-logger"));
        s.setBootstrapLogLevel(config.getInitParameter("log-level"));
        s.setLoggerClassName(config.getInitParameter("logger-class"));
        String value = config.getInitParameter("logkit-config");
        if ( value != null ) {
            s.setLoggingConfiguration("context:/" + value);
        }
        value = config.getInitParameter("log4j-config");
        if ( value != null ) {
            s.setLog4jConfiguration("context:/" + value);
        }
        
        s.setInitClassloader(getInitParameterAsBoolean(config, "init-classloader", false));
        s.setForceProperties(getInitParameterAsArray(config, "force-property"));
        s.setConfiguration(config.getInitParameter("configurations"));
        s.setAllowReload(getInitParameterAsBoolean(config, "allow-reload", Settings.ALLOW_RELOAD));
        s.setLoadClasses(getInitParameterAsArray(config, "load-class"));
        s.setEnableUploads(getInitParameterAsBoolean(config, "enable-uploads", Settings.ENABLE_UPLOADS));
        s.setUploadDirectory(config.getInitParameter("upload-directory"));
        s.setAutosaveUploads(getInitParameterAsBoolean(config, "autosave-uploads", Settings.SAVE_UPLOADS_TO_DISK));
        s.setOverwriteUploads(config.getInitParameter("overwrite-uploads"));
        s.setMaxUploadSize(getInitParameterAsInteger(config, "upload-max-size", Settings.MAX_UPLOAD_SIZE));
        s.setCacheDirectory(config.getInitParameter("cache-directory"));
        s.setWorkDirectory(config.getInitParameter("work-directory"));
        s.setParentServiceManagerClassName(config.getInitParameter("parent-service-manager"));
        value = config.getInitParameter("show-time");
        if ( value != null && value.equalsIgnoreCase("hide") ) {
            s.setShowTime(true);
            s.setHideShowTime(true);
        } else {
            s.setShowTime(getInitParameterAsBoolean(config, "show-time", false));
            s.setHideShowTime(false);
        }
        s.setManageExceptions(getInitParameterAsBoolean(config, "manage-exceptions", true));
        s.setFormEncoding(config.getInitParameter("form-encoding"));
        
        // TODO extra classpath
    }
    
    /** Convenience method to access boolean servlet parameters */
    protected static boolean getInitParameterAsBoolean(ServletConfig config, String name, boolean defaultValue) {
        String value = config.getInitParameter(name);
        if (value == null) {
            return defaultValue;
        }

        return BooleanUtils.toBoolean(value);
    }

    protected static int getInitParameterAsInteger(ServletConfig config, String name, int defaultValue) {
        String value = config.getInitParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
    
    protected static String[] getInitParameterAsArray(ServletConfig config, String name) {
        final String param = config.getInitParameter(name);
        if ( param == null ) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(param, " \t\r\n\f;,", false);
        String[] array = null;    
        while (tokenizer.hasMoreTokens()) {
            final String value = tokenizer.nextToken().trim();
            if ( array == null ) {
                array = new String[1];
            } else {
                String[] ca = new String[array.length+1];
                System.arraycopy(array, 0, ca, 0, array.length);
                array = ca;
            }
            array[array.length-1] = value;
        }
        return array;
    }
    
}
