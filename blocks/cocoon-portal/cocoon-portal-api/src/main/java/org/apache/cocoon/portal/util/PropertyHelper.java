/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.util;

import java.util.Properties;


/**
 * Helper class for replacing property references with the value of the
 * property.
 *
 * @version $Id$
 */
public class PropertyHelper {

    /**
     * Replace all property references in the string with the current value
     * and return it.
     */
    public static String replace(String value, Properties properties) {
        // quick test for null or no references
        if ( value == null || value.indexOf("{") == -1 ) {
            return value;
        }
        final StringBuffer buffer = new StringBuffer();
        int prev = 0;
        int pos;

        // search for the next instance of { from the 'prev' position
        while ((pos = value.indexOf("{", prev)) >= 0) {

            // if there was any text before this, add it
            if (pos > prev) {
                buffer.append(value.substring(prev, pos));
            }

            // if we are at the end of the string, end
            if (pos == (value.length() - 1)) {
                buffer.append("{");
                prev = pos + 1;
            } else {
                // start token found, check for end token
                int endName = value.indexOf('}', pos);
                if (endName == -1) {
                    // no end token found, just append the rest
                    buffer.append(value.substring(pos));
                    prev = value.length();
                } else {
                    final String propertyName = value.substring(pos + 1, endName);
                    String propertyValue = getProperty(propertyName, properties);
                    // compatibility fallback - if the value is null, just readd token
                    if (propertyValue == null) {
                        buffer.append("{");
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

    /**
     * Return the value of the property.
     * If a properties object is provided, this is searched first for a value.
     * If still no value is found, the system properties are searched.
     * The key might contain a default value separated by a ':'! If no value is found
     * and such a default value is provided, the default value is returned.
     *
     * @param key The key for the property.
     * @param properties A set of properties.
     * @return Return the value or null.
     */
    public static String getProperty(String key, Properties properties) {
        final int pos = key.indexOf(':');
        final String name;
        final String defaultValue;
        if ( pos != -1 ) {
            name = key.substring(0, pos);
            defaultValue = key.substring(pos+1);
        } else {
            name = key;
            defaultValue = null;
        }
        String value = null;
        if ( properties != null ) {
            value = properties.getProperty(name);
        }
        if ( value == null ) {
            try {
                value = System.getProperty(name);
            } catch (SecurityException ex) {
                // ignore this
            }
        }
        if ( value == null ) {
            value = defaultValue;
        }
        return value;
    }
}
