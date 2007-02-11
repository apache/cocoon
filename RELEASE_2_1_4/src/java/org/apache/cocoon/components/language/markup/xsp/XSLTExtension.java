/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.language.markup.xsp;

/**
 * This class is used as a XSLT extension class. It is used by the XSP
 * generation stylesheet to escape XML characters to make a valid Java strings.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XSLTExtension.java,v 1.4 2004/02/11 00:17:34 vgritsenko Exp $
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
