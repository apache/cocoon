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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.CascadingRuntimeException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Types;

/**
 * This is a helper class to remove redundant code in
 * esql pages
 *
 * based on the orginal esql.xsl
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Id: EsqlHelper.java,v 1.3 2004/02/28 04:17:50 antonio Exp $
 */

public class EsqlHelper {

    private static Boolean TRUE;

    static {
        EsqlHelper.TRUE = Boolean.TRUE;
    }

    /** returns byte array from BLOB
     */
    public final static byte[] getBlob(ResultSet set, String column) throws RuntimeException {

        byte[] result = null;
        try {
            result = EsqlHelper.getBlob(set, set.findColumn(column));
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        }
        return result;
    }

    /** returns byte array from BLOB
     */
    public final static byte[] getBlob(ResultSet set, int column) throws java.lang.Exception {

        InputStream reader = null;
        byte[] buffer = null;
        Blob dbBlob = null;

        try {
            if (set.getMetaData().getColumnType(column) == java.sql.Types.BLOB) {
                dbBlob = set.getBlob(column);
                int length = (int) dbBlob.length();
                reader = dbBlob.getBinaryStream();
                buffer = new byte[length];
                reader.read(buffer);
                reader.close();
                if (reader != null)
                    reader.close();
                if (buffer == null)
                    return null;
                return buffer;
            } else {
                return set.getString(column).getBytes();
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        } finally {
            // ORACLE 'temporary lob' problem patch start
            if (dbBlob != null && dbBlob.getClass().getName().equals("oracle.sql.BLOB")) {
                if (dbBlob
                    .getClass()
                    .getMethod("isTemporary", new Class[0])
                    .invoke(dbBlob, new Object[0])
                    .equals(TRUE))
                    dbBlob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                        dbBlob,
                        new Object[0]);
            }
        }
    }

    /** returns byte array from BLOB
     */
    public final static byte[] getBlob(CallableStatement cs, int column, String defaultString)
        throws java.lang.Exception {

        InputStream reader = null;
        byte[] buffer = null;
        byte[] result = null;
        Blob dbBlob = null;

        try {
            dbBlob = cs.getBlob(column);
            int length = (int) dbBlob.length();
            reader = dbBlob.getBinaryStream();
            buffer = new byte[length];
            reader.read(buffer);
            reader.close();
            if (reader != null)
                reader.close();
            if (buffer != null)
                result = buffer;
            else if (defaultString != null && !defaultString.equals("_null_"))
                result = defaultString.getBytes();
            else
                result = null;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        } finally {
            // ORACLE 'temporary lob' problem patch start
            if (dbBlob != null && dbBlob.getClass().getName().equals("oracle.sql.BLOB")) {
                if (dbBlob
                    .getClass()
                    .getMethod("isTemporary", new Class[0])
                    .invoke(dbBlob, new Object[0])
                    .equals(TRUE))
                    dbBlob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                        dbBlob,
                        new Object[0]);
            }
        }
        return result;
    }

    /** returns Unicode encoded string from CLOB or String column 
     */
    public final static String getStringOrClob(ResultSet set, String column, String defaultString)
        throws RuntimeException {

        String result = null;
        try {
            result = EsqlHelper.getStringOrClob(set, set.findColumn(column), defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        }
        return result;
    }

    /** returns Unicode encoded string from CLOB or String column 
     */
    public final static String getStringOrClob(ResultSet set, int column, String defaultString)
        throws java.lang.Exception {

        Reader reader = null;
        char[] buffer = null;
        String result = null;
        Clob dbClob = null;

        try {
            if (set.getMetaData().getColumnType(column) == java.sql.Types.CLOB) {
                dbClob = set.getClob(column);
                int length = (int) dbClob.length();
                reader = new BufferedReader(dbClob.getCharacterStream());
                buffer = new char[length];
                reader.read(buffer);
                reader.close();
                if (reader != null)
                    reader.close();
                if (buffer != null)
                    result = new String(buffer);
                else if (defaultString != null && !defaultString.equals("_null_"))
                    result = defaultString;
                else
                    result = null;
            } else {
                result = set.getString(column);
                if (result == null && defaultString != null && !defaultString.equals("_null_"))
                    result = defaultString;
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        } finally {
            // ORACLE 'temporary lob' problem patch start
            if (dbClob != null && dbClob.getClass().getName().equals("oracle.sql.CLOB")) {
                try {
                    if (dbClob
                        .getClass()
                        .getMethod("isTemporary", new Class[0])
                        .invoke(dbClob, new Object[0])
                        .equals(TRUE))
                        dbClob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                            dbClob,
                            new Object[0]);
                } catch (Exception e1) {
                    // swallow
                }
            }
        }
        return result;
    }

    /** returns Unicode encoded string from CLOB or String column 
     */
    public final static String getStringOrClob(
        CallableStatement cs,
        int column,
        String defaultString)
        throws java.lang.Exception {

        Reader reader = null;
        char[] buffer = null;
        String result = null;
        Clob dbClob = null;

        try {
            dbClob = cs.getClob(column);
            int length = (int) dbClob.length();
            reader = new BufferedReader(dbClob.getCharacterStream());
            buffer = new char[length];
            reader.read(buffer);
            reader.close();
            if (reader != null)
                reader.close();
            if (buffer != null)
                result = new String(buffer);
            else if (defaultString != null && !defaultString.equals("_null_"))
                result = defaultString;
            else
                result = null;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        } finally {
            // ORACLE 'temporary lob' problem patch start
            if (dbClob != null && dbClob.getClass().getName().equals("oracle.sql.CLOB")) {
                try {
                    if (dbClob
                        .getClass()
                        .getMethod("isTemporary", new Class[0])
                        .invoke(dbClob, new Object[0])
                        .equals(TRUE))
                        dbClob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                            dbClob,
                            new Object[0]);
                } catch (Exception e1) {
                    // swallow
                }
            }
        }
        return result;
    }

    /** returns ascii string from CLOB or String column 
     */
    public final static String getAscii(ResultSet set, String column, String defaultString)
        throws RuntimeException {

        String result = null;
        try {
            result = EsqlHelper.getAscii(set, set.findColumn(column), defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting ascii data for column " + column, e);
        }
        return result;
    }

    /** returns ascii string from CLOB or String column 
     */
    public final static String getAscii(ResultSet set, int column, String defaultString) {
        InputStream asciiStream = null;
        String result = null;
        Clob dbClob = null;

        try {
            if (set.getMetaData().getColumnType(column) == Types.CLOB) {
                byte[] buffer = null;
                dbClob = set.getClob(column);
                int length = (int) dbClob.length();
                asciiStream = new BufferedInputStream(dbClob.getAsciiStream());
                buffer = new byte[length];
                asciiStream.read(buffer);
                asciiStream.close();
                if (buffer != null)
                    result = new String(buffer);
                else if (defaultString != null && !defaultString.equals("_null_"))
                    result = defaultString;
                else
                    result = null;
            } else {
                result = set.getString(column);
                if (result == null && defaultString != null && !defaultString.equals("_null_"))
                    result = defaultString;
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException(
                "Error getting ascii data from column " + column,
                e);
        } finally {
            if (asciiStream != null) {
                try {
                    asciiStream.close();
                } catch (Exception ase) {
                    throw new CascadingRuntimeException("Error closing clob stream", ase);
                }
            }
            // ORACLE 'temporary lob' problem patch start
            if (dbClob != null && dbClob.getClass().getName().equals("oracle.sql.CLOB")) {
                try {
                    if (dbClob
                        .getClass()
                        .getMethod("isTemporary", new Class[0])
                        .invoke(dbClob, new Object[0])
                        .equals(TRUE))
                        dbClob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                            dbClob,
                            new Object[0]);
                } catch (Exception e1) {
                    // swallow
                }
            }

        }

        return result;
    }

    /** returns ascii string from CLOB or String column 
     */
    public final static String getAscii(CallableStatement cs, int column, String defaultString) {
        InputStream asciiStream = null;
        String result = null;
        Clob dbClob = null;

        try {
            byte[] buffer = null;
            dbClob = cs.getClob(column);
            int length = (int) dbClob.length();
            asciiStream = new BufferedInputStream(dbClob.getAsciiStream());
            buffer = new byte[length];
            asciiStream.read(buffer);
            asciiStream.close();
            if (buffer != null)
                result = new String(buffer);
            else if (defaultString != null && !defaultString.equals("_null_"))
                result = defaultString;
            else
                result = null;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting ascii data for column " + column, e);
        } finally {
            if (asciiStream != null) {
                try {
                    asciiStream.close();
                } catch (Exception ase) {
                    throw new CascadingRuntimeException("Error closing clob stream", ase);
                }
            }
            // ORACLE 'temporary lob' problem patch start
            if (dbClob != null && dbClob.getClass().getName().equals("oracle.sql.CLOB")) {
                try {
                    if (dbClob
                        .getClass()
                        .getMethod("isTemporary", new Class[0])
                        .invoke(dbClob, new Object[0])
                        .equals(TRUE))
                        dbClob.getClass().getMethod("freeTemporary", new Class[0]).invoke(
                            dbClob,
                            new Object[0]);
                } catch (Exception e1) {
                    // swallow
                }
            }
        }

        return result;
    }

    public final static String getStringFromByteArray(
        byte[] bytes,
        String encoding,
        String defaultString) {
        if (bytes != null) {
            try {
                return new String(bytes, encoding);
            } catch (java.io.UnsupportedEncodingException uee) {
                throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
            }
        } else {
            if (defaultString != null && !defaultString.equals("_null_"))
                return defaultString;
            else
                return null; /* before was "" but null is more consequent */
        }
    }

}
