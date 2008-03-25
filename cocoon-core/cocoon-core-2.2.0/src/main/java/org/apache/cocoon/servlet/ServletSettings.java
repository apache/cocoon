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
package org.apache.cocoon.servlet;

import org.apache.cocoon.configuration.Settings;
import org.apache.commons.lang.BooleanUtils;

/**
 * Helper class for managing Cocoon servlet specific settings.
 * 
 * @since 2.2
 * @version $Id$
 */
public class ServletSettings {

    /**
     * Allow adding processing time to the response
     */
    public static String KEY_SHOWTIME = "org.apache.cocoon.showtime";

    /**
     * If true, processing time will be added as an HTML comment
     */
    public static String KEY_HIDE_SHOWTIME = "org.apache.cocoon.hideshowtime";

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    public static String KEY_SHOW_VERSION = "org.apache.cocoon.show-version";

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    public static String KEY_MANAGE_EXCEPTIONS = "org.apache.cocoon.manageexceptions";


    /** Default value for {@link #KEY_SHOWTIME} parameter (false). */
    public static final boolean SHOW_TIME = false;

    /** Default value for {@link #KEY_HIDE_SHOWTIME} parameter (false). */
    public static final boolean HIDE_SHOW_TIME = false;

    /** Default value for {@link #KEY_SHOW_VERSION} parameter (true). */
    public static final boolean SHOW_COCOON_VERSION = true;

    /** Default value for {@link #KEY_MANAGE_EXCEPTIONS} parameter (true). */
    public static final boolean MANAGE_EXCEPTIONS = true;


    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime;

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    protected boolean showCocoonVersion;

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected boolean manageExceptions;

    
    public ServletSettings(Settings s) {
        // set defaults
        this.showTime = SHOW_TIME;
        this.hideShowTime = HIDE_SHOW_TIME;
        this.showCocoonVersion = SHOW_COCOON_VERSION;
        this.manageExceptions = MANAGE_EXCEPTIONS;

        if (s != null) {
            String value;
            value = s.getProperty(KEY_SHOWTIME);
            if (value != null) {
                this.setShowTime(BooleanUtils.toBoolean(value));
            }
            value = s.getProperty(KEY_HIDE_SHOWTIME);
            if (value != null) {
                this.setHideShowTime(BooleanUtils.toBoolean(value));
            }
            value = s.getProperty(KEY_SHOW_VERSION);
            if (value != null) {
                this.setShowCocoonVersion(BooleanUtils.toBoolean(value));
            }
            value = s.getProperty(KEY_MANAGE_EXCEPTIONS);
            if (value != null) {
                this.setManageExceptions(BooleanUtils.toBoolean(value));
            }
        }
    }

    /**
     * @return Returns the hideShowTime.
     * @see #KEY_HIDE_SHOWTIME
     */
    public boolean isHideShowTime() {
        return this.hideShowTime;
    }

    /**
     * @return Returns the manageExceptions.
     * @see #KEY_MANAGE_EXCEPTIONS
     */
    public boolean isManageExceptions() {
        return this.manageExceptions;
    }

    /**
     * @return Returns the showTime.
     * @see #KEY_SHOWTIME
     */
    public boolean isShowTime() {
        return this.showTime;
    }

    /**
     * @return Returns the showCocoonVersion.
     * @see #KEY_SHOW_VERSION
     */
    public boolean isShowVersion() {
        return this.showCocoonVersion;
    }

    /**
     * @param hideShowTime The hideShowTime to set.
     */
    public void setHideShowTime(boolean hideShowTime) {
        this.hideShowTime = hideShowTime;
    }

    /**
     * @param manageExceptions The manageExceptions to set.
     */
    public void setManageExceptions(boolean manageExceptions) {
        this.manageExceptions = manageExceptions;
    }

    /**
     * @param showTime The showTime to set.
     */
    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    /**
     * @param showCocoonVersion The showCocoonVersion flag to set.
     */
    public void setShowCocoonVersion(boolean showCocoonVersion) {
        this.showCocoonVersion = showCocoonVersion;
    }
}
