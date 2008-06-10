/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * This class is similar to the {@link java.io.BufferedOutputStream}. In
 * addition it provides an increasing buffer, the possibility to reset the
 * buffer and it counts the number of bytes written to the output stream.
 *
 * @version $Id$
 * @since   2.1
 */
public class BufferedOutputStream extends FilterOutputStream {

    private byte buffer[];

    private int count;
    private int totalCount;

    private final int flushBufferSize;

    /**
     * Creates a new buffered output stream to write data to the specified
     * underlying output stream with a default flush buffer size of 32768 bytes
     * and a default initial buffer size of 8192 bytes.
     *
     * @param   out   the underlying output stream.
     */
    public BufferedOutputStream(final OutputStream out) {
        this(out, 32768);
    }

    /**
     * Creates a new buffered output stream to write data to the specified
     * underlying output stream with the specified flush buffer size and a
     * default initial buffer size of 8192 bytes.
     *
     * @param   out   the underlying output stream.
     */
    public BufferedOutputStream(final OutputStream out, final int flushBufferSize) {
        this(out, flushBufferSize, 8192);
    }

    /**
     * Creates a new buffered output stream to write data to the specified
     * underlying output stream with the specified buffer sizes.
     *
     * @param out    the underlying output stream.
     * @param flushBufferSize  the buffer size when the stream is flushed. Must
     *                         be greater than 0 or -1 meaning the stream never
     *                         flushes itself.
     * @param initialBufferSize  the initial buffer size. Must be greater than 0.
     *                           Will be limited to the flush buffer size.
     */
    public BufferedOutputStream(final OutputStream out,
                                final int flushBufferSize,
                                final int initialBufferSize) {
        super(out);
        if (flushBufferSize <= 0 && flushBufferSize != -1) {
            throw new IllegalArgumentException("Flush buffer size <= 0 && != -1");
        }
        if (initialBufferSize <= 0) {
            throw new IllegalArgumentException("Initial buffer size <= 0");
        }
        int actualInitialBufferSize =
            flushBufferSize > 0 && initialBufferSize > flushBufferSize ? flushBufferSize
                                                                       : initialBufferSize;
        this.buffer = new byte[actualInitialBufferSize];
        this.flushBufferSize = flushBufferSize;
    }

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(final int b) throws IOException {
        if (this.count == this.buffer.length) {
            // No need to check return value, can NEVER be 0.
            this.increaseBuffer(1);
        }

        this.buffer[this.count++] = (byte)b;
        this.totalCount++;

        checkForFlush();
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
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int free = this.buffer.length - this.count;
        int necessaryIncrease = len - free;
        if (necessaryIncrease > 0) {
            int actualIncrease = this.increaseBuffer(necessaryIncrease);
            if (actualIncrease < necessaryIncrease) {
                free += actualIncrease;
                // Needs to be written in chunks by recursive calls to this method.
                writeToBuffer(b, off, free);
                int newOff = off + free;
                int newLen = len - free;
                while (newLen > 0) {
                    writeToBuffer(b, newOff, Math.min(newLen, this.flushBufferSize));
                    newOff += this.flushBufferSize;
                    newLen -= this.flushBufferSize;
                }
                return;
            }
        }

        writeToBuffer(b, off, len);
    }

    private void writeToBuffer(final byte[] b, final int off, final int len) throws IOException {
        System.arraycopy(b, off, this.buffer, this.count, len);
        this.count += len;
        this.totalCount += len;

        checkForFlush();
    }

    private void checkForFlush() throws IOException {
        if (this.count == this.flushBufferSize) {
            flush();
        }
    }

    /**
     * Flushes this buffered output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
        if (this.count > 0) {
            this.out.write(this.buffer, 0, this.count);
            this.count = 0;
        }
        this.out.flush();
    }

    /**
     * Closes this buffered output stream.
     * Flush before closing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        flush();
        super.close();
    }

    /**
     * Increase the buffer by at least as many bytes as specified via the
     * parameter, but not exceeding the flushBufferSize. The actual increase is
     * returned.
     *
     * @return  increase in buffer size.
     */
    private int increaseBuffer(final int increase) {
        int oldLength = this.buffer.length;
        if (oldLength == this.flushBufferSize) {
            // fast way out
            return 0;
        }

        int newLength = oldLength;
        int actualIncrease;
        do {
            newLength = newLength * 2;
            if (this.flushBufferSize > 0 && newLength >= this.flushBufferSize) {
                newLength = this.flushBufferSize;
                actualIncrease = newLength - oldLength;
                break;
            }
            actualIncrease = newLength - oldLength;
        } while (actualIncrease < increase);

        // Because of the "fast way out" above at this point there should always be an increase.
        byte[] newBuffer = new byte[newLength];
        if (this.count > 0) {
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.count);
        }
        this.buffer = newBuffer;
        return actualIncrease;
    }

    /**
     * Clear the buffer.
     * @deprecated Public access is deprecated. Use {@link #reset()} instead.
     */
    public void clearBuffer() {
        this.totalCount -= this.count;
        this.count = 0;
    }

    /**
     * Reset the BufferedOutputStream to the last {@link #flush()}.
     */
    public void reset() {
        clearBuffer();
    }

    /**
     * @return if it is possible to reset the buffer completely, i.e. nothing has been flushed yet.
     */
    public boolean isResettable() {
        return this.count == this.totalCount;
    }

    /**
     * Return the size of the current buffer
     */
    public int getCount() {
        return this.totalCount;
    }

}
