/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.util;

/**
 * A collection of <code>String</code> handling utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: StringUtils.java,v 1.3 2004/03/28 21:01:20 antonio Exp $
 */
public class StringUtils {

    /**
     * Tests whether a given character is alphabetic, numeric or
     * underscore
     *
     * @param c The character to be tested
     * @return whether the given character is alphameric or not
     */
    public static boolean isAlphaNumeric(char c) {
        return c == '_' ||
            (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            (c >= '0' && c <= '9');
    }

    /**
     * Replaces tokens in input with Value present in System.getProperty
     */
    public static String replaceToken(String s) {
        int startToken = s.indexOf("${");
        int endToken = s.indexOf("}",startToken);
        String token = s.substring(startToken+2,endToken);
        StringBuffer value = new StringBuffer();
        value.append(s.substring(0,startToken));
        value.append(System.getProperty(token));
        value.append(s.substring(endToken+1));
        return value.toString();
    }
}
