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
package org.apache.cocoon.components.language.markup.xsp;

/**
 * This class is used as a XSLT extension class. It is used by the XSP
 * generation stylesheet to escape XML characters to make a valid Java strings.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XSLTExtension.java,v 1.5 2004/03/05 13:02:47 bdelacretaz Exp $
 */
public class XSLTExtension {

    /**
     * Escapes '"' and '\' characters in a String (add a '\' before them) so that it can
     * be inserted in java source + quote special characters as UTF-8
     */
    public static String escapeJavaString(String string) {
        char chr[] = string.toCharArray();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < chr.length; i++) {
            char c = chr[i];
            switch (c) {
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '"':
                case '\\':
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                default:
                    if (' ' <= c && c <= 127) {
                        buffer.append(c);
                    } else {
                        buffer.append("\\u");
                        buffer.append(int2digit(c >> 12));
                        buffer.append(int2digit(c >> 8));
                        buffer.append(int2digit(c >> 4));
                        buffer.append(int2digit(c));
                    }
                    break;
            }
        }

        final String encoded = buffer.toString();
        return encoded;
    }

    /**
     * Quote special characters as UTF-8
     *
     * TC: It's code duplication but that way we don't
     *     have to iterate through the StringBuffer twice
     */
    public static String escapeString(String string) {
        char chr[] = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < chr.length; i++) {
            char c = chr[i];
            if (c <= 127) {
                buffer.append(c);
            } else {
                buffer.append("\\u");
                buffer.append(int2digit(c >> 12));
                buffer.append(int2digit(c >> 8));
                buffer.append(int2digit(c >> 4));
                buffer.append(int2digit(c));
            }
        }

        final String encoded = buffer.toString();
        return encoded;
    }

    private static char int2digit(int x) {
        x &= 0xF;
        if (x <= 9) return (char)(x + '0');
        else return (char)(x - 10 + 'A');
    }

    /**
     * @see #escapeString(String)
     */
    public String escape(String string) {
        return escapeString(string);
    }

    /**
     * This method used by Java XSP core logicsheet.
     * @see #escapeJavaString(String)
     */
    public String escapeJava(String string) {
        return escapeJavaString(string);
    }

    /**
     * Counts amount of spaces in the input line from the beginning
     * to the first new line symbol and returns a string with this
     * amount of spaces.
     *
     * Used by the Python XSP core logicsheet.
     */
    public String prefix(String string) {
        char chr[] = string.toCharArray();
        int i;
        for (i = 0; i < chr.length; i++) {
            if (chr[i] == '\n' || chr[i] == '\r')
                break;
        }
        if (i == chr.length) {
            return "";
        }

        int j = 0;
        for (; i < chr.length; i++) {
            if (chr[i] == '\n' || chr[i] == '\r') {
                j = 0;
            } else if (!Character.isSpaceChar(chr[i])) {
                break;
            } else {
                j ++;
            }
        }

        // System.out.println("<" + string + "> prefix:" + j);
        StringBuffer buffer = new StringBuffer();
        for (i = 0; i < j; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
    }

    /**
     * Counts amount of spaces in the input line from the end
     * to the last new line symbol and returns a string with this
     * amount of spaces.
     *
     * Used by the Python XSP core logicsheet.
     */
    public String suffix(String string) {
        char chr[] = string.toCharArray();

        int j = 0;
        for (int i = chr.length-1; i >=0; i--) {
            if (chr[i] == '\n' || chr[i] == '\r')
                break;
            if (!Character.isSpaceChar(chr[i]))
                return "";
            j ++;
        }

        // System.out.println("<" + string + "> suffix:" + j);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < j; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
    }
}
