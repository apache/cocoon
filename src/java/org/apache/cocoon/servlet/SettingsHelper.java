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

import javax.servlet.ServletConfig;

import org.apache.cocoon.configuration.Settings;

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
    
    public static Settings getSettings(ServletConfig config) {
        final Settings s = new Settings(null);
        
        // logging
        s.setCocoonLogger(config.getInitParameter("cocoon-logger"));
        s.setServletLogger(config.getInitParameter("servlet-logger"));
        s.setLogLevel(config.getInitParameter("log-level"));
        s.setLoggerClassName(config.getInitParameter("logger-class"));
        String value = config.getInitParameter("logkit-config");
        if ( value != null ) {
            s.setLoggingConfiguration("context:/" + value);
        }
        value = config.getInitParameter("log4j-config");
        if ( value != null ) {
            s.setLog4jConfiguration("context:/" + value);
        }
        
        return s;
    }
}
