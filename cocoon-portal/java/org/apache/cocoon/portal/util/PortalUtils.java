/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.util;

import org.apache.commons.lang.StringUtils;

/**
 * Some utility methods.
 *
 * @version $Id$
 */
public class PortalUtils {

    public static String testId(String id) {
        if ( !StringUtils.containsOnly(id, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789 ") ) {
            return "Id contains invalid characters (only a-z,A-Z,0-9, space and '-' are allowed): " + id;
        }
        return null;
    }

}
