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
package org.apache.cocoon.webapps.session.xml;



/**
 * A utility class which will soon be removed...
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: XMLUtil.java,v 1.2 2004/03/05 13:02:23 bdelacretaz Exp $
*/
public final class XMLUtil {

    /**
     * Convert umlaute to entities
     */
    public static String encode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) > 127) {
                buffer.replace(i, i+1, "__"+((int)buffer.charAt(i))+";");
            }
        }
        return buffer.toString();
    }

    /**
     * Convert entities to umlaute
     */
    public static String decode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        int pos;
        boolean found;
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '_' &&
                buffer.charAt(i+1) == '_') {
                pos = i + 2;
                found = false;
                while (buffer.charAt(pos) >= '0'
                       && buffer.charAt(pos) <= '9') {
                    found = true;
                    pos++;
                }
                if (found == true
                    && pos > i + 2
                    && buffer.charAt(pos) == ';') {
                    int ent = new Integer(buffer.substring(i+2, pos)).intValue();
                    buffer.replace(i, pos+1, ""+ (char)ent);
                }
            }
        }
        return buffer.toString();
    }

}
