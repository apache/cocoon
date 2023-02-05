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


/**
 * This a simple xml compiler which outputs a byte array.
 * If you want to reuse this instance, make sure to call {@link #recycle()}
 * inbetween two compilation tasks.
 *
 * @version $Id$
 */
public final class XMLByteStreamCompiler extends AbstractXMLByteStreamCompiler {

    /** The buffer for the compile xml byte stream. */
    private byte buf[];

    /** The number of valid bytes in the buffer. */
    private int bufCount;

    private int bufCountAverage;


    public XMLByteStreamCompiler() {
        super();
        this.bufCountAverage = 2000;
        this.initOutput();
    }

    private void initOutput() {
        this.buf = new byte[bufCountAverage];
        this.bufCount = 0;
    }

    public void recycle() {
        bufCountAverage = (bufCountAverage + bufCount) / 2;
        initOutput();
        super.recycle();
    }


    public Object getSAXFragment() {
        if (this.bufCount == 0) { // no event arrived yet
            return null;
        }
        byte newbuf[] = new byte[this.bufCount];
        System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
        return newbuf;
    }

    protected void write( final int b ) {
        int newcount = this.bufCount + 1;
        assure(newcount);
        this.buf[this.bufCount] = (byte)b;
        this.bufCount = newcount;
    }

    private void assure( final int size ) {
        if (size > this.buf.length) {
            byte newbuf[] = new byte[Math.max(this.buf.length << 1, size)];
            System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
            this.buf = newbuf;
        }
    }
}

