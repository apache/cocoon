/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet.source;

import java.io.IOException;
import java.io.InputStream;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

public class SourceIOInputStream extends InputStream {

    protected final InputStream delegate;

    protected final Source source;

    protected boolean closed;

    protected final SourceFactory factory;

    public SourceIOInputStream(SourceFactory factory, Source source) throws IOException {
        this.source = source;
        this.delegate = source.getInputStream();
        this.factory = factory;
        this.closed = false;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        this.check();
        return this.delegate.available();
    }

    /**
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        if ( !this.closed ) {
            this.closed = true;
            IOException e = null;
            try {
                this.delegate.close();
            } catch (IOException i) {
                e = i;
            } finally {
                this.factory.release(this.source);
            }
            if ( e != null ) {
                throw e;
            }
        }
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    public void mark(int arg0) {
        this.delegate.mark(arg0);
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return this.delegate.markSupported();
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        this.check();
        return this.delegate.read();
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        this.check();
        return this.delegate.read(arg0, arg1, arg2);
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws IOException {
        this.check();
        return this.delegate.read(arg0);
    }

    /**
     * @see java.io.InputStream#reset()
     */
    public void reset() throws IOException {
        this.check();
        this.delegate.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws IOException {
        this.check();
        return this.delegate.skip(arg0);
    }

    protected void check() throws IOException {
        if ( this.closed ) {
            throw new IOException("Input stream has already been closed.");
        }
    }
}
