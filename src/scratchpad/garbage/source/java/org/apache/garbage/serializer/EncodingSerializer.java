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
package org.apache.garbage.serializer;

import org.apache.garbage.serializer.encoding.DefaultEncoder;
import org.apache.garbage.serializer.encoding.Encoder;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: EncodingSerializer.java,v 1.2 2003/06/24 16:59:19 cziegeler Exp $
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
