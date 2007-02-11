/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.serializer.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: XMLEncoder.java,v 1.1 2003/06/21 21:11:48 pier Exp $
 */
public class XMLEncoder extends CompiledEncoder {

    private static final char ENCODE_HEX[] = "0123456789ABCDEF".toCharArray();
    private static final char ENCODE_QUOT[] = "&quot;".toCharArray();
    private static final char ENCODE_AMP[]  = "&amp;".toCharArray();
    private static final char ENCODE_APOS[] = "&apos;".toCharArray();
    private static final char ENCODE_LT[]   = "&lt;".toCharArray();
    private static final char ENCODE_GT[]   = "&gt;".toCharArray();

    /**
     * Create a new instance of this <code>XMLEncoder</code>.
     */
    public XMLEncoder() {
        super("X-W3C-XML");
    }

    /**
     * Return true or false wether this encoding can encode the specified
     * character or not.
     * <p>
     * This method will return true for the following character range:
     * <br />
     * <code>
     *   <nobr>#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD]</nobr>
     * </code>
     * </p>
     *
     * @see <a href="http://www.w3.org/TR/REC-xml#charsets">W3C XML 1.0</a>
     */
    protected boolean compile(char c) {
        if ((c == 0x09) || // [\t]
            (c == 0x0a) || // [\n]
            (c == 0x0d)) { // [\r]
            return(true);
        }

        if ((c == 0x22) || // ["]
            (c == 0x26) || // [&]
            (c == 0x27) || // [']
            (c == 0x3c) || // [<]
            (c == 0x3e) || // [>]
            (c <  0x20) || // See <http://www.w3.org/TR/REC-xml#charsets>
            ((c > 0xd7ff) && (c < 0xe000)) || (c > 0xfffd)) {
            return(false);
        }

        return(true);
    }

    /**
     * Return an array of characters representing the encoding for the
     * specified character.
     */
    public char[] encode(char c) {
        switch (c) {
            case 0x22: return(ENCODE_QUOT); // (") [&quot;]
            case 0x26: return(ENCODE_AMP);  // (&) [&amp;]
            case 0x27: return(ENCODE_APOS); // (') [&apos;]
            case 0x3c: return(ENCODE_LT);   // (<) [&lt;]
            case 0x3e: return(ENCODE_GT);   // (>) [&gt;]
            default: {
                if (c > 0xfff) {
                    char ret[] = { '&', '#', 'x',
                        ENCODE_HEX[c >> 0xc & 0xf],
                        ENCODE_HEX[c >> 0x8 & 0xf],
                        ENCODE_HEX[c >> 0x4 & 0xf],
                        ENCODE_HEX[c & 0xf], ';'
                    };
                    return(ret);
                }
                if (c > 0xff) {
                    char ret[] = { '&', '#', 'x',
                        ENCODE_HEX[c >> 0x8 & 0xf],
                        ENCODE_HEX[c >> 0x4 & 0xf],
                        ENCODE_HEX[c & 0xf], ';'
                    };
                    return(ret);
                }
                char ret[] = { '&', '#', 'x',
                    ENCODE_HEX[c >> 0x4 & 0xf],
                    ENCODE_HEX[c & 0xf], ';'
                };
                return(ret);
            }
        }
    }
}
