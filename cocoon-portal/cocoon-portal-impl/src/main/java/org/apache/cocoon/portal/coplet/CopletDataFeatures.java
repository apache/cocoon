/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.coplet;

import org.apache.commons.lang.BooleanUtils;

/**
 * This class contains constants and utility methods for the standard features
 * of a coplet.
 *
 * @version $Id$
 */
public final class CopletDataFeatures {

    /** This is the name of the coplet data attribute containing a boolean value
     * indicating if the user is allowed to resize the coplet. (default is true) */
    public static final String ATTRIBUTE_SIZABLE = "sizable";

    /** This is the name of the coplet data attribute containing a boolean value
     * indicating if a user is allowed to remove the coplet (default is false meaning
     * it's not mandatory and the user is allowed to remove it. */
    public static final String ATTRIBUTE_MANDATORY = "mandatory";

    /** This is the name of the coplet data attribute containing a boolean value
     * indicating if the coplet supports the full-screen mode. (default is true) */
    public static final String ATTRIBUTE_FULLSCREEN = "full-screen";

    /** This is the name of the coplet data attribute containing a boolean value
     * indicating if the coplet handles the minimized sizing state (default is false
     * meaning the portal handles the state and renders only the title). */
    public static final String ATTRIBUTE_HANDLE_SIZING = "handle-sizing";

    public static Object getAttributeValue(CopletData data, String key, Object defaultValue) {
        Object value = data.getAttribute(key);
        if ( value == null ) {
            value = data.getCopletBaseData().getCopletConfig(key);
            if ( value == null ) {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Is this coplet sizable?
     */
    public static boolean isSizable(CopletData data) {
        Boolean sizable = (Boolean)getAttributeValue(data, ATTRIBUTE_SIZABLE, Boolean.TRUE);
        return sizable.booleanValue();
    }

    /**
     * Is this coplet mandatory?
     */
    public static boolean isMandatory(CopletData data) {
        Boolean mandatory = (Boolean)getAttributeValue(data, ATTRIBUTE_MANDATORY, Boolean.FALSE);
        return mandatory.booleanValue();
    }

    /**
     * Does this coplet support the full screen mode?
     */
    public static boolean supportsFullScreenMode(CopletData data) {
        Boolean supportsMode = (Boolean)getAttributeValue(data, ATTRIBUTE_FULLSCREEN, Boolean.TRUE);
        return supportsMode.booleanValue();
    }

    /**
     * Does this coplet handles sizing by itself?
     */
    public static boolean handlesSizing(CopletData data) {
        Boolean handlesSizing = (Boolean)getAttributeValue(data, ATTRIBUTE_HANDLE_SIZING, Boolean.FALSE);
        return handlesSizing.booleanValue();
    }

    public static void setSizable(CopletData data, boolean value) {
        data.setAttribute(ATTRIBUTE_SIZABLE, BooleanUtils.toBooleanObject(value));
    }

    public static void setMandatory(CopletData data, boolean value) {
        data.setAttribute(ATTRIBUTE_MANDATORY, BooleanUtils.toBooleanObject(value));
    }
}
