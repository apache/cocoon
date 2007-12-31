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
package org.apache.cocoon.util;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;

/**
 * Helper class to create Stettings and for replacing property references with the value of the
 * property
 *
 * @version $Id$
 * @since 2.1.9
 */
public class SettingsHelper {

    /**
     * Create the settings object
     * @param context The current context
     * @param logger A logger to use to log any errors.
     * @throws ContextException
     */
    public static void createSettings(DefaultContext context, Logger logger) throws ContextException {
        SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        resolver.contextualize(context);
        Settings settings = new PropertySettings(resolver, logger);
        context.put(Settings.PROPERTY_USER_SETTINGS, settings);
    }

    /**
     * Return the Settings object
     * @param context The context
     * @return The global Settings
     */
    public static Settings getSettings(Context context) {
        Settings settings = null;
        try {
            settings = (Settings) context.get(Settings.PROPERTY_USER_SETTINGS);
        } catch (Exception e) {
            // Ignore the exception;
        }
        return settings;
    }

    /**
     * Replace all property references in the string with the current value
     * and return it.
     */
    public static String replace(String value, Settings settings, Logger logger) {
        // quick test for null or no references
        if (value == null || value.indexOf("${") == -1 || settings == null) {
            return value;
        }
        final StringBuffer buffer = new StringBuffer();
        int prev = 0;
        int pos;

        // search for the next instance of $ from the 'prev' position
        while ((pos = value.indexOf("$", prev)) >= 0) {

            // if there was any text before this, add it
            if (pos > prev) {
                buffer.append(value.substring(prev, pos));
            }

            // if we are at the end of the string, end
            if (pos == (value.length() - 1)) {
                buffer.append("$");
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {
                // peek ahead to see if the next char is a property or not
                // not a property: insert the char as a literal
                buffer.append(value.substring(pos, pos + 2));
                prev = pos + 2;

            } else {
                // start token found, check for end token
                int endName = value.indexOf('}', pos);
                if (endName == -1) {
                    // no end token found, just append the rest
                    buffer.append(value.substring(pos));
                    prev = value.length();
                } else {
                    final String propertyName = value.substring(pos + 2, endName);
                    String propertyValue = getProperty(propertyName, settings);
                    // compatibility fallback - if the value is null, just readd token
                    if (propertyValue == null) {
                        logger.warn("Property " + propertyName + " not found.");
                        buffer.append("${");
                        buffer.append(propertyName);
                        buffer.append('}');
                    } else {
                        buffer.append(propertyValue);
                    }
                    prev = endName + 1;
                }
            }
        }
        // no more tokens found
        // append the rest
        if (prev < value.length()) {
            buffer.append(value.substring(prev));
        }
        return buffer.toString();
    }

    static String getProperty(String name, Settings settings) {
        String value = null;
        if (settings != null) {
            value = settings.getProperty(name);
        }
        if (value == null) {
            value = System.getProperty(name);
        }
        return value;
    }
}
