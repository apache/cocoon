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
package org.apache.cocoon.spring.configurator.impl;

import org.apache.cocoon.configuration.SettingsDefaults;

/**
 * Helper class to determine the running mode.
 *
 * @since 1.0
 * @version $Id$
 */
public abstract class RunningModeHelper {

    /**
     * Name of the system property specifying the running mode.
     */
    public final static String PROPERTY_RUNNING_MODE = "org.apache.cocoon.mode";

    // determine an ev. set running mode from the system properties
    private static final String SYSTEM_RUNNING_MODE;
    static {
        String mode = null;
        try {
            mode = System.getProperty( PROPERTY_RUNNING_MODE, null );
        } catch (SecurityException se) {
            // we ignore this
        }
        SYSTEM_RUNNING_MODE = mode;
    }

    /**
     * Determine the running mode.
     * A non-null system property will have precedence over everything else.
     * The system default running mode will be used if the passed parameter mode is null.
     *
     * @param mode The default running mode.
     * @return The current running mode.
     * @see #PROPERTY_RUNNING_MODE
     * @see SettingsDefaults#DEFAULT_RUNNING_MODE
     */
    public static String determineRunningMode(String mode) {
        if (SYSTEM_RUNNING_MODE != null) {
            return SYSTEM_RUNNING_MODE;
        }
        if (mode == null) {
            return SettingsDefaults.DEFAULT_RUNNING_MODE;
        }
        return mode;
    }

    /**
     * Check if the value for the running mode is valid.
     * Currently this method does not check anything. All modes (apart from null)
     * are valid.
     * @param mode Check if the mode is a valid mode.
     * @throws IllegalArgumentException if the mode is invalid
     */
    public static void checkRunningMode(String mode)
    throws IllegalArgumentException {
        if ( mode == null ) {
            throw new IllegalArgumentException("Running mode can't be null.");
        }
        /*
        if ( !Arrays.asList(SettingsDefaults.RUNNING_MODES).contains(mode) ) {
            final String msg =
                "Invalid running mode: " + mode + " - Use one of: " + Arrays.asList(SettingsDefaults.RUNNING_MODES);
            throw new IllegalArgumentException(msg);
        }
        */
    }
}
