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
package org.apache.garbage.serializer;

import org.apache.garbage.serializer.encoding.DefaultEncoder;
import org.apache.garbage.serializer.encoding.Encoder;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: EncodingSerializer.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public abstract class EncodingSerializer extends AbstractSerializer {

    /** Our <code>Encoder</code> instance. */
    private Encoder encoder = null;

    /**
     * Create a new instance of this <code>EncodingSerializer</code>
     */
    public EncodingSerializer() {
        this(new DefaultEncoder());
    }

    /**
     * Create a new instance of this <code>EncodingSerializer</code>
     */
    public EncodingSerializer(Encoder encoder) {
        super();
        this.encoder = encoder;
    }

    /* ====================================================================== */

    /**
     * Encode and write a <code>String</code>
     */
    protected void encode(String data)
    throws SAXException {
        char array[] = data.toCharArray();
        this.encode(array, 0, array.length);
    }

    /**
     * Encode and write an array of characters.
     */
    protected void encode(char data[])
    throws SAXException {
        this.encode(data, 0, data.length);
    }

    /**
     * Encode and write a specific part of an array of characters.
     */
    protected void encode(char data[], int start, int length)
    throws SAXException {
        int end = start + length;

        if (data == null) throw new NullPointerException("Null data");
        if ((start < 0) || (start > data.length) || (length < 0) ||
            (end > data.length) || (end < 0))
            throw new IndexOutOfBoundsException("Invalid data");
        if (length == 0) return;

        for (int x = start; x < end; x++) {
            char c = data[x];

            if (this.charset.allows(c) && this.encoder.allows(c)) {
                continue;
            }

            if (start != x) this.write(data, start, x - start );
            this.write(this.encoder.encode(c));
            start = x + 1;
            continue;
        }
        if (start != end) this.write(data, start, end - start );
    }
}
