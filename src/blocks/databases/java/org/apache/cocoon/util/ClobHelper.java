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

import java.sql.Clob;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

/**
 * A minimal implementation just enough to send a CLOB to a
 * database. Advanced methods and all methods for modifying the CLOB
 * are not implemented.
 *
 * @version CVS $Id: ClobHelper.java,v 1.3 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class ClobHelper implements Clob{

    InputStream in = null;
    long length = 0;

    public ClobHelper(InputStream is, long len) {
        this.in = is;
        this.length = len;
    }

    public InputStream getAsciiStream() {
        return this.in;
    }

    public Reader getCharacterStream() {
        return new BufferedReader(new InputStreamReader(this.in));
    }

    public long length() {
        return length;
    }

    /**
     * Not implemented.
     */
    public String getSubString(long pos, int length) {
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** getSubString");
        return "";
    }

    /**
     * Not implemented.
     */
    public long position(Clob searchstr, long start) {
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** position(clob,long)");
        return -1; // we don't implement this
    }

    /**
     * Not implemented.
     */
    public long position(String searchstr, long start) {
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** position(str,long)");
        return -1; // we don't implement this
    }


    // if ever implemented.... the following  are the JDBC3 methods
    // since not implemented anyway, included in JDBC2 builds as well.
    // @JDBC3_START@
    // @JDBC3_END@


    /**
     * Not implemented.
     */
    public OutputStream setAsciiStream(long pos) {
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** setAsciiStream");
        return null;
    }

    /**
     * Not implemented.
     */
    public Writer setCharacterStream(long pos) {
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** setCharacterStream");
        return null;
    }

    /**
     * Not implemented.
     */
    public int setString(long pos, String str){
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** setString(long,str)");
        return 0;
    }

    /**
     * Not implemented.
     */
    public int setString(long pos, String str, int offset, int len){
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** setString(long,str,int,int)");
        return 0;
    }

    /**
     * Not implemented.
     */
    public void truncate(long len){
        System.out.println("ClobHelper ** NOT IMPLEMENTED ** truncate");
    }


}

