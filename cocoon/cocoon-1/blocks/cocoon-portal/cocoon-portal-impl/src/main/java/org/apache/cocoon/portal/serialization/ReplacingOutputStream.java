/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * 
 * @version $Id$
 * @since 2.2
 */
final class ReplacingOutputStream extends OutputStream {

    public static final char token = '\0';

    /** Stream. */
    protected final OutputStream stream;

    protected boolean inKey;

    protected final LinkedList orderedValues;

    /** Encoding. */
    protected final String encoding;

    /**
     * Constructor.
     */
    public ReplacingOutputStream(OutputStream stream,
                                 LinkedList   values,
                                 String       enc) {
        this.stream = stream;    
        this.orderedValues = values;
        this.inKey = false;
        this.encoding = enc;
    }

    /**
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        this.stream.close();
    }

    /**
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        this.stream.flush();
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if ( len == 0 ) {
            return;
        }
        if ( this.inKey ) {
            if ( b[off] == ReplacingOutputStream.token ) {
                this.writeNextValue();
                off++;
                len--;
            } else {
                this.write(ReplacingOutputStream.token);
            }
            this.inKey = false;
        }
        // search for key
        boolean end = false;
        do {
            int s = off;
            int e = off+len;
            while (s < e && b[s] != ReplacingOutputStream.token) {
                s++;
            }
            if ( s == e ) {
                this.stream.write(b, off, len);
                end = true;
            } else if ( s == e-1 ) {
                this.stream.write(b, off, len-1);
                this.inKey = true;
                end = true;                
            } else {
                if ( b[s+1] == ReplacingOutputStream.token) {
                    final int l = s-off;
                    this.stream.write(b, off, l);
                    off += (l+2);
                    len -= (l+2);
                    this.writeNextValue();
                    
                } else {
                    final int l = s-off+2;
                    this.stream.write(b, off, l);
                    off += l;
                    len -= l;
                }
                end = (len == 0);
            }
        } while (!end);
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        if ( b == ReplacingOutputStream.token ) {
            if ( this.inKey ) {
                this.writeNextValue();
            } 
            this.inKey = !this.inKey;
        } else {
            if ( this.inKey ) {
                this.inKey = false;
                this.stream.write(ReplacingOutputStream.token);
            }
            this.stream.write(b);
        }
    }

    /** 
     * Write next value
     */
    protected void writeNextValue() throws IOException {
        final String value = (String)this.orderedValues.removeLast();
        if ( value != null ) {
            this.stream.write(value.getBytes(this.encoding), 0, value.length());
        }        
    }
}
