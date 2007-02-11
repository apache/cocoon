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
package org.apache.cocoon.components.language.markup.xsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * This is a helper class to remove redundant code in
 * esql pages.
 *
 * Based on the orginal esql.xsl.
 *
 * @version $Id$
 */
public class EsqlHelper {

    private static void close(Object lob) {
        if (lob == null) {
            return;
        }

        // ORACLE 'temporary lob' problem patch start
        Class clazz = lob.getClass();
        String name = clazz.getName();
        if (name.equals("oracle.sql.BLOB") || name.equals("oracle.sql.CLOB")) {
            try {
                if (clazz.getMethod("isTemporary", new Class[0]).invoke(lob, new Object[0]).equals(Boolean.TRUE)) {
                    clazz.getMethod("freeTemporary", new Class[0]).invoke(lob, new Object[0]);
                }
            } catch (IllegalAccessException e) {
                /* ignored */
            } catch (InvocationTargetException e) {
                /* ignored */
            } catch (NoSuchMethodException e) {
                /* ignored */
            }
        }
    }

    private static byte[] readBytes(Blob blob, byte[] defaultValue)
    throws SQLException, IOException {
        if (blob == null) {
            return defaultValue;
        }

        InputStream is = null;
        try {
            int length = (int) blob.length();
            if (length == 0) {
                return defaultValue;
            }

            byte[] buffer = new byte[length];
            is = blob.getBinaryStream();
            is.read(buffer);

            return buffer;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) { /* ignored */ }
            close(blob);
        }
    }

    private static String readAscii(Clob clob, String defaultValue)
    throws SQLException, IOException {
        if (clob == null) {
            return defaultValue;
        }

        InputStream is = null;
        try {
            int length = (int) clob.length();
            if (length == 0) {
                return defaultValue;
            }

            byte[] buffer = new byte[length];
            is = clob.getAsciiStream();
            is.read(buffer);

            return new String(buffer, 0, length);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) { /* ignored */ }
            close(clob);
        }
    }

    private static String read(Clob clob, String defaultValue)
    throws SQLException, IOException {
        if (clob == null) {
            return defaultValue;
        }

        Reader r = null;
        try {
            int length = (int) clob.length();
            if (length == 0) {
                return defaultValue;
            }

            char[] buffer = new char[length];
            r = clob.getCharacterStream();
            r.read(buffer);

            return new String(buffer, 0, length);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException e) { /* ignored */ }
            close(clob);
        }
    }

    /** returns byte array from BLOB */
    public static byte[] getBlob(ResultSet set, String column)
    throws RuntimeException {
        try {
            return EsqlHelper.getBlob(set, set.findColumn(column));
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        }
    }

    /** returns byte array from BLOB */
    public static byte[] getBlob(ResultSet set, int column)
    throws Exception {
        try {
            if (set.getMetaData().getColumnType(column) == java.sql.Types.BLOB) {
                return readBytes(set.getBlob(column), null);
            } else {
                String value = set.getString(column);
                if (value == null) {
                    return null;
                }
                return value.getBytes();
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        }
    }

    /** returns byte array from BLOB */
    public static byte[] getBlob(CallableStatement cs, int column, String defaultString)
    throws Exception {

        byte[] defaultValue = null;
        if (defaultString != null && !defaultString.equals("_null_")) {
            defaultValue = defaultString.getBytes();
        }

        try {
            if (cs.getMetaData().getColumnType(column) == java.sql.Types.BLOB) {
                return readBytes(cs.getBlob(column), defaultValue);
            } else {
                String value = cs.getString(column);
                if (value == null) {
                    return defaultValue;
                }
                return value.getBytes();
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting blob data for column " + column, e);
        }
    }

    /** returns Unicode encoded string from CLOB or String column */
    public static String getStringOrClob(ResultSet set, String column, String defaultString)
    throws RuntimeException {
        try {
            return EsqlHelper.getStringOrClob(set, set.findColumn(column), defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        }
    }

    /** returns Unicode encoded string from CLOB or String column */
    public static String getStringOrClob(ResultSet set, int column, String defaultString)
    throws Exception {
        if (defaultString != null && defaultString.equals("_null_")) {
            defaultString = null;
        }

        try {
            if (set.getMetaData().getColumnType(column) == java.sql.Types.CLOB) {
                return read(set.getClob(column), defaultString);
            } else {
                String result = set.getString(column);
                if (result == null) {
                    result = defaultString;
                }
                return result;
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        }
    }

    /** returns Unicode encoded string from CLOB or String column */
    public static String getStringOrClob(CallableStatement cs,
                                         int column,
                                         String defaultString)
    throws Exception {
        if (defaultString != null && defaultString.equals("_null_")) {
            defaultString = null;
        }

        try {
            return read(cs.getClob(column), defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting text from column " + column, e);
        }
    }

    /** returns ascii string from CLOB or String column */
    public static String getAscii(ResultSet set, String column, String defaultString)
    throws RuntimeException {
        try {
            int colIndex = set.findColumn(column);
            return EsqlHelper.getAscii(set, colIndex, defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting ascii data for column " + column, e);
        }
    }

    /** returns ascii string from CLOB or String column */
    public static String getAscii(ResultSet set, int column, String defaultString) {
        if (defaultString != null && defaultString.equals("_null_")) {
            defaultString = null;
        }

        try {
            if (set.getMetaData().getColumnType(column) == Types.CLOB) {
                return readAscii(set.getClob(column), defaultString);
            } else {
                String result = set.getString(column);
                if (result == null) {
                    result = defaultString;
                }
                return result;
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting ascii data from column " + column, e);
        }
    }

    /** returns ascii string from CLOB or String column */
    public static String getAscii(CallableStatement cs, int column, String defaultString) {
        try {
            return readAscii(cs.getClob(column), defaultString);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error getting ascii data for column " + column, e);
        }
    }

    public static String getStringFromByteArray(byte[] bytes,
                                                String encoding,
                                                String defaultString) {
        if (bytes != null) {
            try {
                return new String(bytes, encoding);
            } catch (java.io.UnsupportedEncodingException uee) {
                throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
            }
        } else {
            if (defaultString != null && !defaultString.equals("_null_")) {
                return defaultString;
            } else {
                return null; /* before was "" but null is more consequent */
            }
        }
    }
}
