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

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */

public class FastInputStream extends InputStream {

    private static final int DEFAULT_SIZE = 2048;

    private InputStream in;
    private byte buf[];
    private int count;
    private int pos;
    private int markpos = -1;
    private int marklimit;

    public FastInputStream(InputStream in) {
        this(in, DEFAULT_SIZE);
    }

    public FastInputStream(InputStream in, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.in = in;
        this.buf = new byte[size];
    }

    private void fill() throws IOException {
        if (markpos < 0) {
            pos = 0;
        } else if (pos >= buf.length) {
            if (markpos > 0) {
                int sz = pos - markpos;
                System.arraycopy(buf, markpos, buf, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buf.length >= marklimit) {
                markpos = -1;
                pos = 0;
            } else {
                int nsz = pos * 2;
                if (nsz > marklimit) nsz = marklimit;
                byte nbuf[] = new byte[nsz];
                System.arraycopy(buf, 0, nbuf, 0, pos);
                buf = nbuf;
            }
        }
        count = pos;
        int n = in.read(buf, pos, buf.length - pos);
        if (n > 0) count = n + pos;
    }

    public final int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count) return -1;
        }
        return buf[pos++] & 0xff;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            if (len >= buf.length && markpos < 0) {
                return in.read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(buf, pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    public final int read(byte b[], int off, int len) throws IOException {
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = read1(b, off, len);
        if (n <= 0) return n;
        while ((n < len) && (in.available() > 0)) {
            int n1 = read1(b, off + n, len - n);
            if (n1 <= 0) break;
            n += n1;
        }
        return n;
    }

    public final void close() throws IOException {
        if (in == null) return;
        in.close();
        in = null;
        buf = null;
    }
}
