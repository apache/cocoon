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
package org.apache.cocoon.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is like the {@link java.io.BufferedOutputStream} but it
 * extends it with a logic to count the number of bytes written to
 * the output stream.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: BufferedOutputStream.java,v 1.4 2004/06/24 07:14:44 cziegeler Exp $
 * @since   2.1
 */
public final class BufferedOutputStream extends FilterOutputStream {
    
    protected byte buf[];

    protected int count;
    
    /**
     * Creates a new buffered output stream to write data to the 
     * specified underlying output stream with a default 8192-byte
     * buffer size.
     *
     * @param   out   the underlying output stream.
     */
    public BufferedOutputStream(OutputStream out) {
        this(out, 8192);
    }

    /**
     * Creates a new buffered output stream to write data to the 
     * specified underlying output stream with the specified buffer 
     * size. 
     *
     * @param   out    the underlying output stream.
     * @param   size   the buffer size.
     * @exception IllegalArgumentException if size <= 0.
     */
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.buf = new byte[size];
    }

    /**
     * Writes the specified byte to this buffered output stream. 
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        if (this.count >= this.buf.length) {
            this.incBuffer();
        }
        this.buf[count++] = (byte)b;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this buffered output stream.
     *
     * <p> Ordinarily this method stores bytes from the given array into this
     * stream's buffer, flushing the buffer to the underlying output stream as
     * needed.  If the requested length is at least as large as this stream's
     * buffer, however, then this method will flush the buffer and write the
     * bytes directly to the underlying output stream.  Thus redundant
     * <code>BufferedOutputStream</code>s will not copy data unnecessarily.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
        while (len > buf.length - count) {
            this.incBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * Flushes this buffered output stream. 
     * We don't flush here. 
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
    }

    /**
     * Closes this buffered output stream.
     * Flush before closing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        realFlush();
        super.close ();
    }

    /**
     * Flushes this buffered output stream. 
     * We don't flush here. 
     */
    public void realFlush() throws IOException {
        this.writeBuffer();
        this.out.flush();
    }
    
    /**
     * Write the buffer
     */
    private void writeBuffer() 
    throws IOException {
        if (this.count > 0) {
            this.out.write(this.buf, 0, this.count);
        }
    }

    /**
     * Increment the buffer
     */
    private void incBuffer() {
        // currently we double the buffer size
        // this is not so fast but is a very simple logic
        byte[] newBuf = new byte[this.buf.length * 2];
        System.arraycopy(this.buf, 0, newBuf, 0, this.buf.length);
        this.buf = newBuf;
    }
    
    /**
     * Clear/reset the buffer
     */
    public void clearBuffer() {
        this.count = 0;
    }
    
}

