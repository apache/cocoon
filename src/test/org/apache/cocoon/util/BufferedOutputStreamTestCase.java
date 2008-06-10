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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

public class BufferedOutputStreamTestCase extends TestCase {

    public void testUnlimitedBuffering() throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, -1, 32);

        byte[] bytes = new byte[1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // reset, all back to beginning
        bufferedOutputStream.reset();

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // eventually flush
        bufferedOutputStream.flush();
        byte[] streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", bytes.length, byteArrayOutputStream.size());
        assertTrue("Streamed bytes are wrong", Arrays.equals(bytes, streamedBytes));
    }

    public void testCompleteBuffering() throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, 1024, 32);

        byte[] bytes = new byte[1000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // reset, all back to beginning
        bufferedOutputStream.reset();

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // eventually flush
        bufferedOutputStream.flush();
        byte[] streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", bytes.length, byteArrayOutputStream.size());
        assertTrue("Streamed bytes are wrong", Arrays.equals(bytes, streamedBytes));
    }

    public void testCompleteBufferingHittingFlushSize() throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, 1024, 32);

        byte[] bytes = new byte[1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer, buffer will be flushed automatically
        bufferedOutputStream.write(bytes);
        byte[] streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", bytes.length, byteArrayOutputStream.size());
        assertTrue("Streamed bytes are wrong", Arrays.equals(bytes, streamedBytes));

        // reset should not change anything
        bufferedOutputStream.reset();
        streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", bytes.length, byteArrayOutputStream.size());
        assertTrue("Streamed bytes are wrong", Arrays.equals(bytes, streamedBytes));
    }

    public void testBufferingExceedingFlushSize() throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int flushSize = 512;
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, flushSize, 32);

        byte[] bytes = new byte[2000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        assertEquals("Count of buffered and flushed bytes is wrong", 0, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);
        int flushed = ((int)bytes.length / flushSize) * flushSize;

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // reset, back to last flush
        bufferedOutputStream.reset();

        assertEquals("Count of buffered and flushed bytes is wrong", flushed, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes, flushed, bytes.length - flushed);

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // eventually flush
        bufferedOutputStream.flush();
        byte[] streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", bytes.length, byteArrayOutputStream.size());
        assertTrue("Streamed bytes are wrong", Arrays.equals(bytes, streamedBytes));
    }

    public void testBufferingExceedingFlushSizeWithInitialContent() throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int flushSize = 512;
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, flushSize, 32);

        byte[] bytes = new byte[2000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        int initialContentSize = 50;
        bufferedOutputStream.write(bytes, 0, initialContentSize);

        assertEquals("Count of buffered and flushed bytes is wrong", initialContentSize, bufferedOutputStream.getCount());
        assertEquals("Streamed bytes is not empty", 0, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes);
        int flushed = ((int)((initialContentSize + bytes.length) / flushSize)) * flushSize;

        assertEquals("Count of buffered and flushed bytes is wrong", initialContentSize + bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // reset, back to last flush
        bufferedOutputStream.reset();

        assertEquals("Count of buffered and flushed bytes is wrong", flushed, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // write bytes into buffer
        bufferedOutputStream.write(bytes, flushed - initialContentSize, bytes.length - flushed + initialContentSize);

        assertEquals("Count of buffered and flushed bytes is wrong", initialContentSize + bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", flushed, byteArrayOutputStream.size());

        // eventually flush
        bufferedOutputStream.flush();
        byte[] streamedBytes = byteArrayOutputStream.toByteArray();

        assertEquals("Count of buffered and flushed bytes is wrong", initialContentSize + bytes.length, bufferedOutputStream.getCount());
        assertEquals("Count of streamed bytes is wrong", initialContentSize + bytes.length, byteArrayOutputStream.size());
        byte[] expectedBytes = new byte[2050];
        System.arraycopy(bytes, 0, expectedBytes, 0, initialContentSize);
        System.arraycopy(bytes, 0, expectedBytes, initialContentSize, bytes.length);
        assertTrue("Streamed bytes are wrong", Arrays.equals(expectedBytes, streamedBytes));
    }

}
