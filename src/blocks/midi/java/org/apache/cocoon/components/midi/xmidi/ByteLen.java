/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.midi.xmidi;

/**
 * The MIDI file parsing parts of this class are based on code from the XMidi project, written
 * by  Peter Arthur Loeb (http://www.palserv.com/XMidi/) and used with permission.
 * The warranty disclaimer of the MIT license (http://www.opensource.org/licenses/mit-license.html)
 * applies to Peter Arthur Loeb's code.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @author <a href="mailto:peter@palserv.com">Peter Loeb</a>
 */

public class ByteLen {
    /**
     * Default constructor.
     * As of Jan 8, 2001, this is not used.
     */
    public ByteLen() {
    }

    /**
     * Constructor used in the
     * {@link org.apache.cocoon.components.midi.xmidi.Utils#deltaToInt(byte[],int) MX.deltaToInt}
     * method to create this class, which it then returns.
     * @param b  a byte array; used to set {@link #ba}
     * @param l  a length; used to set {@link #len}
     */
    public ByteLen(byte[] b, int l) {
        ba = b;
        len = l;
    }

    /**
     * A byte array.
     */
    public byte[] ba;

    /**
     * As used in the
     * {@link org.apache.cocoon.components.midi.xmidi.Utils#deltaToInt(byte[],int) MX.deltaToInt}
     * method, it is the length of the delta field being converted,
     * not the length of the array.
     * <p>
     * There is nothing about this class that requires that this variable
     * be used in this way.  It could be any int.
     */
    public int len;
}
