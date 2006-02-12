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

import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.util.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * This helper class initializes the {@link MutableSettings} object from the servlet
 * configuration.
 * 
 * @version SVN $Id$
 */
public class SettingsHelper {

    private SettingsHelper() {
        //  no instantiation
    }
    
    public static void fill(MutableSettings s, ServletConfig config) {
        String value;

        handleForceProperty(getInitParameter(config, "force-property"), s);

        value = getInitParameter(config, "configurations");
        if ( value != null ) {
            s.setConfiguration(value);
        } else if ( s.getConfiguration() == null ) {
            s.setConfiguration("/WEB-INF/cocoon.xconf");
        }

        // upto 2.1.x the logging configuration was named "logkit-config"
        // we still support this, but provide a new unbiased name as well
        value = getInitParameter(config, "logkit-config");
        if ( value != null ) {
            s.setLoggingConfiguration("context:/" + value);
        } else {
            value = getInitParameter(config, "logging-config");
            if ( value != null ) {
                s.setLoggingConfiguration("context:/" + value);                
            }
        }

        value = getInitParameter(config, "servlet-logger");
        if ( value != null ) {
            s.setEnvironmentLogger(value);
        }

        value = getInitParameter(config, "cocoon-logger");
        if ( value != null ) {
            s.setCocoonLogger(value);
        }

        value = getInitParameter(config, "log-level");
        if ( value != null ) {
            s.setBootstrapLogLevel(value);
        }

        value = getInitParameter(config, "logger-class");
        if ( value != null ) {
            s.setLoggerManagerClassName(value);
        }

        s.setReloadingEnabled(getInitParameterAsBoolean(config, "allow-reload", s.isReloadingEnabled(null)));

        handleLoadClass(getInitParameter(config, "load-class"), s);

        s.setEnableUploads(getInitParameterAsBoolean(config, "enable-uploads", s.isEnableUploads()));

        value = getInitParameter(config, "upload-directory");
        if ( value != null ) {
            s.setUploadDirectory(value);
        }

        s.setAutosaveUploads(getInitParameterAsBoolean(config, "autosave-uploads", s.isAutosaveUploads()));

        value = getInitParameter(config, "overwrite-uploads");
        if ( value != null ) {
            s.setOverwriteUploads(config.getInitParameter(value));
        }

        s.setMaxUploadSize(getInitParameterAsInteger(config, "upload-max-size", s.getMaxUploadSize()));
        
        value = getInitParameter(config, "cache-directory");
        if ( value != null ) {
            s.setCacheDirectory(value);
        }

        value = getInitParameter(config, "work-directory");
        if ( value != null ) {
            s.setWorkDirectory(value);
        }

        handleExtraClassPath(config.getInitParameter("extra-classpath"), s);

        value = getInitParameter(config, "show-time");
        if ( value != null && value.equalsIgnoreCase("hide") ) {
            s.setShowTime(true);
            s.setHideShowTime(true);
        } else {
            s.setShowTime(getInitParameterAsBoolean(config, "show-time", false));
            s.setHideShowTime(false);
        }

        s.setShowCocoonVersion(getInitParameterAsBoolean(config, "show-cocoon-version", s.isShowVersion()));

        s.setManageExceptions(getInitParameterAsBoolean(config, "manage-exceptions", s.isManageExceptions()));

        value = getInitParameter(config, "form-encoding");
        if ( value != null ) {
            s.setFormEncoding(value);
        }
    }
    
    /** Convenience method to access boolean servlet parameters */
    protected static boolean getInitParameterAsBoolean(ServletConfig config, String name, boolean defaultValue) {
        String value = getInitParameter(config, name);
        if (value == null) {
            return defaultValue;
        }

        return BooleanUtils.toBoolean(value);
    }

    protected static int getInitParameterAsInteger(ServletConfig config, String name, int defaultValue) {
        String value = getInitParameter(config, name);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
    
    private static void handleLoadClass(String param, MutableSettings s) {
        if ( param == null ) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(param, " \t\r\n\f;,", false);
        while (tokenizer.hasMoreTokens()) {
            final String value = tokenizer.nextToken().trim();
            s.addToLoadClasses(value);
        }
    }

    /**
     * Handle the "force-property" parameter.
     *
     * If you need to force more than one property to load, then
     * separate each entry with whitespace, a comma, or a semi-colon.
     * Cocoon will strip any whitespace from the entry.
     */
    private static void handleForceProperty(String forceSystemProperty, MutableSettings s) {
        if (forceSystemProperty != null) {
            StringTokenizer tokenizer = new StringTokenizer(forceSystemProperty, " \t\r\n\f;,", false);

            while (tokenizer.hasMoreTokens()) {
                final String property = tokenizer.nextToken().trim();
                if (property.indexOf('=') == -1) {
                    continue;
                }
                try {
                    String key = property.substring(0, property.indexOf('='));
                    String value = property.substring(property.indexOf('=') + 1);
                    if (value.indexOf("${") != -1) {
                        value = StringUtils.replaceToken(value);
                    }
                    s.addToForceProperties(key, value);
                } catch (Exception e) {
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
        }
    }

    /**
     * Retreives the "extra-classpath" attribute, that needs to be
     * added to the class path.
     */
    private static void handleExtraClassPath(String extraClassPath, MutableSettings settings) {
        if (extraClassPath != null) {
            StringTokenizer st = new StringTokenizer(extraClassPath, SystemUtils.PATH_SEPARATOR, false);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                settings.addToExtraClasspaths(s);
            }
        }
    }

    /**
     * Get an initialisation parameter. The value is trimmed, and null is returned if the trimmed value
     * is empty.
     */
    private static String getInitParameter(ServletConfig config, String name) {
        String result = config.getInitParameter(name);
        if (result != null) {
            result = result.trim();
            if (result.length() == 0) {
                result = null;
            }
        }

        return result;
    }
    
}
