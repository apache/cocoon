/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an OutputStream which forwards all received bytes to another
 * output stream and in addition caches all bytes, thus acting like a
 * TeeOutputStream.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-17 10:32:52 $
 */

public final class CachingOutputStream
extends OutputStream {

    private OutputStream receiver;
    private ByteArrayOutputStream baOutputStream;

    public CachingOutputStream(OutputStream os) {
        this.receiver = os;
        this.baOutputStream = new ByteArrayOutputStream();
    }

    public byte[] getContent() {
        return this.baOutputStream.toByteArray();
    }

    public void write(int b) throws IOException {
        this.receiver.write(b);
        this.baOutputStream.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        this.receiver.write(b, off, len);
        this.baOutputStream.write(b, off, len);
    }

    public void flush() throws IOException {
        this.receiver.flush();
        this.baOutputStream.flush();
    }

    public void close() throws IOException {
        this.receiver.close();
        this.baOutputStream.close();
    }


}