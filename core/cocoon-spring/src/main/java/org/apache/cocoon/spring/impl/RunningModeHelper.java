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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.impl;

import org.apache.cocoon.configuration.SettingsDefaults;

/**
 * Helper class to determine the running mode.
 *
 * @since 1.0
 * @version $Id$
 */
public class RunningModeHelper {

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

    /** Name of the property specifying the running mode. */
    private RunningModeHelper() {
        // never initiate
    }

    /** 
     * Determine the runningmode. 
     * A non-null system property will have precedence over everything else.
     * The system default running mode will be used if the passed parameter mode is null.
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
}
