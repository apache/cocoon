/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portlet;

import java.util.StringTokenizer;

import javax.portlet.PortletConfig;

import org.apache.cocoon.core.MutableSettings;
import org.apache.commons.lang.BooleanUtils;

/**
 * This helper class initializes the {@link MutableSettings} object from the servlet
 * configuration.
 * 
 * @version $Id$
 */
public class SettingsHelper {

    private SettingsHelper() {
        //  no instantiation
    }
    
    public static void fill(MutableSettings s, PortletConfig config) {
        String value;

        value = getInitParameter(config, "configurations");
        if ( value != null ) {
            s.setConfiguration(value);
        } else if ( s.getConfiguration() == null ) {
            s.setConfiguration("/WEB-INF/cocoon.xconf");
        }

        value = getInitParameter(config, "logkit-config");
        if ( value != null ) {
            s.setLoggingConfiguration("context:/" + value);
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

        value = getInitParameter(config, "show-time");
        if ( value != null && value.equalsIgnoreCase("hide") ) {
            s.setShowTime(true);
            s.setHideShowTime(true);
        } else {
            s.setShowTime(getInitParameterAsBoolean(config, "show-time", false));
            s.setHideShowTime(false);
        }

        s.setManageExceptions(getInitParameterAsBoolean(config, "manage-exceptions", s.isManageExceptions()));

        value = getInitParameter(config, "form-encoding");
        if ( value != null ) {
            s.setFormEncoding(value);
        }
    }
    
    /** Convenience method to access boolean servlet parameters */
    protected static boolean getInitParameterAsBoolean(PortletConfig config, String name, boolean defaultValue) {
        String value = getInitParameter(config, name);
        if (value == null) {
            return defaultValue;
        }

        return BooleanUtils.toBoolean(value);
    }

    protected static int getInitParameterAsInteger(PortletConfig config, String name, int defaultValue) {
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
     * Get an initialisation parameter. The value is trimmed, and null is returned if the trimmed value
     * is empty.
     */
    private static String getInitParameter(PortletConfig config, String name) {
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
