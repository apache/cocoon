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
package org.apache.cocoon.caching;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

public class CachingOutputStreamTestCase extends TestCase {

    public void testWriteInt() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CachingOutputStream cachingOutputStream = new CachingOutputStream(byteArrayOutputStream);

        for (int i = 0; i < 1000; i++) {
            cachingOutputStream.write(i);
        }

        // the content in the original destination
        byte[] content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        byte[] cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));
        
        // Test to exceed CachingOutputStream's buffer of 1024.
        for (int i = 0; i < 1000; i++) {
            cachingOutputStream.write(i);
        }

        // the content in the original destination
        content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));
    }

    public void testWriteByteArray() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CachingOutputStream cachingOutputStream = new CachingOutputStream(byteArrayOutputStream);

        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        cachingOutputStream.write(data);

        // the content in the original destination
        byte[] content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        byte[] cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(data, cachedContent));
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));

        // Test to exceed CachingOutputStream's buffer of 1024.
        cachingOutputStream.write(data);

        // the content in the original destination
        content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));
    }

    public void testWriteByteArrayPart() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CachingOutputStream cachingOutputStream = new CachingOutputStream(byteArrayOutputStream);

        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        for (int i = 0; i < 10; i++) {
            cachingOutputStream.write(data, i * 100, 100);
        }

        // the content in the original destination
        byte[] content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        byte[] cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(data, cachedContent));
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));

        // Test to exceed CachingOutputStream's buffer of 1024.
        cachingOutputStream.write(data, 200, 100);

        // the content in the original destination
        content = byteArrayOutputStream.toByteArray();
        // the content collected in the CachingOutputStream
        cachedContent = cachingOutputStream.getContent();

        assertEquals("Length of cached content is wrong:", content.length, cachedContent.length);
        assertTrue("Cached content differs", Arrays.equals(content, cachedContent));
    }

}
