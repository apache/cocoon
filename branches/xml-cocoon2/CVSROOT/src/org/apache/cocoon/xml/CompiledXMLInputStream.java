/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */
package org.apache.cocoon.xml;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UTFDataFormatException;

import java.util.ArrayList;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */

public final class CompiledXMLInputStream extends InputStream {

    private InputStream in;
    private ArrayList list = new ArrayList();

    public CompiledXMLInputStream(InputStream input) throws IOException {
        this.in = new FastInputStream(input, 1 << 13);
        this.checkProlog();
    }

    public final void checkProlog() throws IOException {
        InputStream in = this.in;
        int valid = 0;
        if (in.read() == 'C') valid++;
        if (in.read() == 'X') valid++;
        if (in.read() == 'M') valid++;
        if (in.read() == 'L') valid++;
        if (in.read() == 1) valid++;
        if (in.read() == 0) valid++;
        if (valid != 6) throw new IOException("Unrecognized file format.");
    }

    public final int readEvent() throws IOException {
        return this.in.read();
    }

    public final int readAttributes() throws IOException {
        InputStream in = this.in;
        int ch1 = in.read();
        int ch2 = in.read();
        return ((ch1 << 8) + (ch2 << 0));
    }

    public final String readString() throws IOException {
        int length = this.readLength();
        int index = length & 0x00007FFF;
        if (length >= 0x00008000) {
            return (String) list.get(index);
        } else {
            String str = new String(this.readChars(index));
            list.add(str);
            return str;
        }
    }

    /**
     * The returned char array might contain any number of zero bytes
     * at the end
     */
    public final char[] readChars() throws IOException {
        return readChars(this.readLength());
    }

    public final int read() throws IOException {
        return this.in.read();
    }

    /**
     * The returned char array might contain any number of zero bytes
     * at the end
     */
    private char[] readChars(int len) throws IOException {
        char[] str = new char[len];
        byte[] bytearr = new byte[len];
        int c, char2, char3;
        int count = 0;
        int i = 0;

        this.readBytes(bytearr);

        while (count < len) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    str[i++] = (char) c;
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    char2 = (int) bytearr[count-1];
                    str[i++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    str[i++] = ((char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
            }
        }

        return str;
    }

    private void readBytes(byte[] b) throws IOException {
        InputStream in = this.in;
        int n = 0;
        int len = b.length;
        while (n < len) {
            int count = in.read(b, n, len - n);
            if (count < 0) throw new EOFException();
            n += count;
        }
    }

    private int readLength() throws IOException {
        InputStream in = this.in;
        int ch1 = in.read();
        int ch2 = in.read();
        return ((ch1 << 8) + (ch2 << 0));
    }
}
