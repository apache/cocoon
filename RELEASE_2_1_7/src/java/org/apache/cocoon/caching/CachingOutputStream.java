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
package org.apache.cocoon.caching;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an {@link OutputStream} which forwards all received bytes to another
 * output stream and in addition caches all bytes, thus acting like a
 * TeeOutputStream.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachingOutputStream.java,v 1.2 2004/03/05 13:02:45 bdelacretaz Exp $
 */

public final class CachingOutputStream
extends OutputStream {

    private OutputStream receiver;

    /** The buffer for the compile xml byte stream. */
    private byte buf[];

    /** The number of valid bytes in the buffer. */
    private int bufCount;

    public CachingOutputStream(OutputStream os) {
        this.receiver = os;
        this.buf = new byte[1024];
        this.bufCount = 0;
    }

    public byte[] getContent() {
        byte newbuf[] = new byte[this.bufCount];
        System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
        return newbuf;
    }

    public void write(int b) throws IOException {
        this.receiver.write(b);
        int newcount = this.bufCount + 1;
        if (newcount > this.buf.length) {
            byte newbuf[] = new byte[Math.max(this.buf.length << 1, newcount)];
            System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
            this.buf = newbuf;
        }
        this.buf[this.bufCount] = (byte)b;
        this.bufCount = newcount;
    }

    public void write( byte b[] ) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        this.receiver.write(b, off, len);
        if (len == 0) return;
        int newcount = this.bufCount + (len-off);
        if (newcount > this.buf.length) {
            byte newbuf[] = new byte[Math.max(this.buf.length << 1, newcount)];
            System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
            this.buf = newbuf;
        }
        System.arraycopy(b, off, this.buf, this.bufCount, len);
        this.bufCount = newcount;
    }

    public void flush() throws IOException {
        this.receiver.flush();
    }

    public void close() throws IOException {
        this.receiver.close();
    }


}
