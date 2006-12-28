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
package org.apache.cocoon.components.sax;

import org.xml.sax.SAXException;

/**
 * This a simple xml compiler which takes a byte array as input.
 * If you want to reuse this interpreter make sure to call first {@link #recycle()}
 * and then set the new consumer for the sax events.
 *
 * @version $Id$
 */
public final class XMLByteStreamInterpreter extends AbstractXMLByteStreamInterpreter {

    private byte[] input;
    private int currentPos;

    public void recycle() {
        this.input = null;
        super.recycle();
    }

    public void deserialize(Object saxFragment) throws SAXException {
        if (!(saxFragment instanceof byte[])) {
            throw new SAXException("XMLDeserializer needs byte array for deserialization.");
        }
        this.input = (byte[])saxFragment;
        this.currentPos = 0;
        super.parse();
    }

    protected int read() throws SAXException {
        if (currentPos >= input.length)
            return -1;
        return input[currentPos++] & 0xff;
    }

    protected int read(byte[] b) throws SAXException {
        final int bytesRead = (this.currentPos + b.length > this.input.length ? this.input.length - this.currentPos : b.length);
        System.arraycopy(this.input, this.currentPos, b, 0, bytesRead);
        this.currentPos += bytesRead;
        return bytesRead;
    }
}
