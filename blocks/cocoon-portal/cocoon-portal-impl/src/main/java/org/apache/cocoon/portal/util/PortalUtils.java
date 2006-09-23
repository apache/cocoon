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
package org.apache.cocoon.portal.util;

import org.apache.commons.lang.StringUtils;

/**
 * Some utility methods.
 *
 * @version $Id$
 */
public class PortalUtils {

    /**
     * Tests if the string represents a correct id for any portal object.
     * The id of an object follows very strict rules: only characters, an underscore and numbers are allowed
     * and the id has to start with a character. This allows to use the id as an identifier
     * for ajax/javascript based portlets.
     */
    public static String testId(String id) {
        if ( id == null || id.length() == 0 ) {
            return "Id must not be null or empty.";
        }
        if ( !StringUtils.containsOnly(id, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_") ) {
            return "Id contains invalid characters (only a-z,A-Z, 0-9 and '_' are allowed): " + id;
        }
        final char firstChar = id.charAt(0);
        if ( firstChar >= 'a' && firstChar <= 'z' ) {
            return null;
        }
        if ( firstChar >= 'A' && firstChar <= 'Z' ) {
            return null;
        }
        return "Id must start with a character : " + id;
    }

}
