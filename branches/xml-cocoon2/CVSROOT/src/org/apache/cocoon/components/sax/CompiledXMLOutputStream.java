/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */
package org.apache.cocoon.components.sax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

import java.util.HashMap;

/**
 * This is the OutputStream for the compiled xml.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */

public final class CompiledXMLOutputStream
extends ByteArrayOutputStream {

    private HashMap map = new HashMap();
    private int count = 0;

    public CompiledXMLOutputStream()
    throws IOException {
        super.write('C');
        super.write('X');
        super.write('M');
        super.write('L');
        super.write(1);
        super.write(0);
    }

    public final void writeEvent(int event) throws IOException {
        super.write(event);
    }

    public final void writeAttributes(int attributes) throws IOException {
        super.write((attributes >>> 8) & 0xFF);
        super.write((attributes >>> 0) & 0xFF);
    }

    public final void writeString(String str) throws IOException {
        Integer index = (Integer) map.get(str);
        if (index == null) {
            int length = str.length();
            map.put(str, new Integer(count++));
            if (length > (2 << 15)) throw new IOException("String cannot be bigger than 32K");
            this.writeChars(str.toCharArray(), 0, length);
        } else {
            int i = index.intValue();
            super.write(((i >>> 8) & 0xFF) | 0x80);
            super.write((i >>> 0) & 0xFF);
        }
    }

    public final void writeChars(char[] ch, int start, int length) throws IOException {
        int utflen = 0;
        int c, count = 0;

        for (int i = 0; i < length; i++) {
            c = ch[i + start];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535) throw new UTFDataFormatException();

        byte[] bytearr = new byte[utflen+2];
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
        for (int i = 0; i < length; i++) {
            c = ch[i + start];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }

        super.write(bytearr);
    }
}
