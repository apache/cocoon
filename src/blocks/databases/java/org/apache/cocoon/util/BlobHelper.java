/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: BlobHelper.java,v 1.2 2003/03/11 17:44:19 vgritsenko Exp $
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

