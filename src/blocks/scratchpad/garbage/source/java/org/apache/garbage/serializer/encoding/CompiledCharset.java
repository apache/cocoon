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
 * @version CVS $Id: CompiledCharset.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public abstract class CompiledCharset extends AbstractCharset {

    /** The encoding table of this <code>Charset</code>. */
    protected byte encoding[];

    /**
     * Create a new instance of this <code>CompiledCharset</code>.
     * <p>
     * After construction, the <code>compile()</code> method will have to
     * be called for proper operation of this <code>Charset</code>.
     *
     * @param name This <code>Charset</code> name.
     * @param aliases This <code>Charset</code> alias names.
     * @throws NullPointerException If one of the arguments is <b>null</b>.
     */
    protected CompiledCharset(String name, String aliases[]) {
        super(name, aliases);
        this.encoding = new byte[8192];
        for (int x = 0; x < this.encoding.length; x++) this.encoding[x] = 0;
    }

    /**
     * Create a new instance of this <code>CompiledCharset</code>.
     * <p>
     * The encodings table passed to this constructor <b>needs</b> to be 8192
     * bytes long, or (in other words), must contain exactly 65536 bits.
     * </p>
     * <p>
     * As in the Java Programming Language a <code>char</code> can assume
     * values between 0 (zero) and 65535 (inclusive), each bit in the specified
     * array refers to a specific <code>char</code> value.
     * </p>
     * <p>
     * When this specific bit is set to 1 (one or true) we assume that the
     * charset <b>can</b> encode the given character, while when the bit is
     * set to 0 (zero or false), the character cannot be represented using
     * this <code>Charset</code>.
     * </p>
     * <p>
     * For example, the <b>US-ASCII</b> <code>Charset</code> can represent
     * only Java characters between 0 (zero) and 255 (inclusive), therefore
     * the specified byte array will contain only 256 true bits.
     * </p>
     * <p>
     * To check if a character can be encoded by this <code>Charset</code>,
     * given &quot;<code>c</code>&quot; as the character to verify, one
     * can write this simple formula:
     * </p>
     * <p>
     * <nobr><code>((encoding[c >> 3] & (1 << (c & 0x07))) > 0)
     * </p>
     * <p>
     * If the result of this operation is 0 (zero) the bit was set to zero,
     * and therefore &quot;<code>c</code>&quot; cannot be represented in
     * this <code>Charset</code>, while if the result is greater than 0 (zero)
     * the character &quot;<code>c</code>&quot; can actually be represented
     * by this <code>Charset</code>
     * </p>
     *
     * @param name This <code>Charset</code> name.
     * @param aliases This <code>Charset</code> alias names.
     * @param encoding This <code>Charset</code> encoding table as specified
     *                 above.
     * @throws NullPointerException If one of the arguments is <b>null</b>.
     * @throws IllegalArgumentException If the length of the encoding table
     *                                  is <b>not</b> 8192 precisely.
     */
    protected CompiledCharset(String name, String aliases[], byte encoding[])
    throws NullPointerException, IllegalArgumentException {
        super(name, aliases);
        if (encoding == null) throw new NullPointerException("Invalid table");
        if (encoding.length != 8192) {
            throw new IllegalArgumentException("Invalid encoding table size: "
                + "current length is " + encoding.length + ", required 8192.");
        }
        this.encoding = encoding;
    }

    /**
     * Check if the specified character is representable by this specifiec
     * <code>Charset</code> instance.
     * </p>
     */
    public boolean allows(char c) {
        /* This is tied to haw the compiler does stuff. */
        return((this.encoding[c >> 3] & (1 << (c & 0x07))) > 0);
    }

    /**
     * Compile the encoding table of this <code>CompiledCharset</code>.
     * <p>
     * This method will invoke the <code>compile(...)</code> method for any
     * possible value of a Java character (65536 times, from 0, zero, to
     * 65535 inclusive), building the encoding table of the characters this
     * <code>Charset</code> can successfully represent.
     */
    protected final void compile() {
        for (int x = 0; x <= Character.MAX_VALUE; x ++) {
            if (this.compile((char)x)) {
                int pos = x >> 3;
                encoding[pos] = (byte) (encoding[pos] | (1 << (x & 0x07)));
            }
        }
    }

    /**
     * Return true or false wether this encoding can encode the specified
     * character or not.
     * <p>
     * This method is equivalent to the <code>allows(...)</code> method, but
     * it will be called upon construction of the encoding table.
     * </p>
     */
    protected abstract boolean compile(char c);
}
