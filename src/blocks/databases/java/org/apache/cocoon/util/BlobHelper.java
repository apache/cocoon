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

import java.sql.Blob;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A minimal implementation just enough to send a BLOB to a
 * database. Advanced methods and all methods for modifying the BLOB
 * are not implemented.
 *
 * @version CVS $Id: BlobHelper.java,v 1.3 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class BlobHelper implements Blob{

    InputStream in = null;
    long length = 0;

    public BlobHelper(InputStream is, long len) {
        this.in = is;
        this.length = len;
    }

    public InputStream getBinaryStream() {
        return this.in;
    }

    public long length() {
        return length;
    }

    /**
     * Not implemented.
     */
    public byte[] getBytes(long pos, int length) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** getBytes");
        return null;
    }

    /**
     * Not implemented.
     */
    public long position(Blob pattern, long start) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** position(blog,long)");
        return -1; // we don't implement this
    }

    /**
     * Not implemented.
     */
    public long position(byte[] pattern, long start) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** position(byte[],long)");
        return -1; // we don't implement this
    }


    // if ever implemented.... the following  are the JDBC3 methods
    // since not implemented anyway, included in JDBC2 builds as well.
    // @JDBC3_START@
    // @JDBC3_END@


    /**
     * Not implemented.
     */
    public OutputStream setBinaryStream(long pos) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** setBinaryStream");
        return null;
    }

    /**
     * Not implemented.
     */
    public int setBytes(long pos, byte[] bytes) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** setBytes(long,byte[])");
        return 0;
    }

    /**
     * Not implemented.
     */
    public int setBytes(long pos, byte[] bytes, int offset, int len) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** setBytes(long,byte[],int,int)");
        return 0;
    }

    /**
     * Not implemented.
     */
    public void truncate(long len) {
        System.out.println("BlobHelper ** NOT IMPLEMENTED ** truncate");
    }


}

