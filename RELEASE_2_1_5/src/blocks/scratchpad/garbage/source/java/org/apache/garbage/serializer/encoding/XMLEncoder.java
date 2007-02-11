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
package org.apache.garbage.serializer.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: XMLEncoder.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
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
