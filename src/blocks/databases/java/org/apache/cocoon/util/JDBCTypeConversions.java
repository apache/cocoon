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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.excalibur.source.Source;


/**
 * Provide some utility methods to read from JDBC result sets or store
 * them to JDBC statements. Largely copied from
 * org.apache.cocoon.acting.AbstractDatabaseAction.
 *
 * <p>The following table lists all available column type names
 * together with the JDBC methods used to get or set a column with
 * that type. In some cases the returned type differs from the type
 * returned by the getXXX method. To set a column, a number of
 * conversions are automatically used. For details, please see the
 * actual code.</p>
 *
 * <p><table border="1">
 * <tr><th>type       </th><th>getXXX      </th><th>returns </th><th>setXXX       </th></tr>
 * <tr><td>clob       </td><td>Clob        </td><td>String  </td><td>Clob         </td></tr>
 * <tr><td>ascii      </td><td>Clob        </td><td>String  </td><td>asciStream   </td></tr>
 * <tr><td>big-decimal</td><td>BigDecimal  </td><td>        </td><td>BigDecimal   </td></tr>
 * <tr><td>binary     </td><td>            </td><td>        </td><td>BinaryStream </td></tr>
 * <tr><td>blob       </td><td>            </td><td>        </td><td>Blob         </td></tr>
 * <tr><td>boolean    </td><td>Boolean  </td><td>Boolean </td><td>Boolean      </td></tr>
 * <tr><td>byte       </td><td>Byte        </td><td>Byte    </td><td>Byte         </td></tr>
 * <tr><td>string     </td><td>String      </td><td>        </td><td>String       </td></tr>
 * <tr><td>date       </td><td>Date        </td><td>        </td><td>Date         </td></tr>
 * <tr><td>double     </td><td>Double      </td><td>Double  </td><td>Double       </td></tr>
 * <tr><td>float      </td><td>Float       </td><td>Float   </td><td>Float        </td></tr>
 * <tr><td>int        </td><td>Int         </td><td>Integer </td><td>Int          </td></tr>
 * <tr><td>long       </td><td>Long        </td><td>Long    </td><td>Long         </td></tr>
 * <tr><td>short      </td><td>Short       </td><td>        </td><td>Short        </td></tr>
 * <tr><td>time       </td><td>Time        </td><td>        </td><td>Time         </td></tr>
 * <tr><td>time-stamp </td><td>Timestamp   </td><td>        </td><td>Timestamp    </td></tr>
 * <tr><td>array      </td><td>Array       </td><td>        </td><td>Array        </td></tr>
 * <tr><td>row        </td><td>Object      </td><td>Struct  </td><td>Object       </td></tr>
 * <tr><td>object     </td><td>Object      </td><td>        </td><td>Object       </td></tr>
 * </table></p>
 *
 * @version CVS $Id: JDBCTypeConversions.java,v 1.6 2004/03/01 03:50:59 antonio Exp $
 */
public class JDBCTypeConversions {
    public static final Map typeConstants;

    static {
        /** Initialize the map of type names to jdbc column types.
            Note that INTEGER, BLOB, and VARCHAR column types map to more than
            one type name. **/
        Map constants = new HashMap();
        constants.put("clob", new Integer(Types.CLOB));
        constants.put("ascii", new Integer(Types.CHAR));
        constants.put("big-decimal", new Integer(Types.BIGINT));
        constants.put("binary", new Integer(Types.VARBINARY));
        constants.put("blob", new Integer(Types.BLOB));
        constants.put("boolean", new Integer(Types.BIT));
        constants.put("byte", new Integer(Types.TINYINT));
        constants.put("string", new Integer(Types.VARCHAR));
        constants.put("date", new Integer(Types.DATE));
        constants.put("double", new Integer(Types.DOUBLE));
        constants.put("float", new Integer(Types.FLOAT));
        constants.put("int", new Integer(Types.INTEGER));
        constants.put("long", new Integer(Types.NUMERIC));
        constants.put("short", new Integer(Types.SMALLINT));
        constants.put("time", new Integer(Types.TIME));
        constants.put("time-stamp", new Integer(Types.TIMESTAMP));
        constants.put("array", new Integer(Types.ARRAY));
        constants.put("row", new Integer(Types.STRUCT));
        constants.put("object", new Integer(Types.OTHER));
        typeConstants = Collections.unmodifiableMap(constants);
    }

    /**
     * Converts an object to a JDBC type. This has just been started
     * and does not do much at the moment.
     *
     */
    public static Object convert(Object value, String jType) {

        Object object=null;
        if (jType.equalsIgnoreCase("string")) {
            if (value instanceof String) {
                object = value;
            } else {
                object = value.toString();
            }
        } else if (jType.equalsIgnoreCase("int")) {
            if (value instanceof String) {
                object = Integer.decode((String)value);
            } else if (value instanceof Integer) {
                object = value;
            } else {
                //
            }
        } else if (jType.equalsIgnoreCase("long")) {
            if (value instanceof String) {
                object = Long.decode((String)value);
            } else if (value instanceof Long) {
                object = value;
            } else {
                //
            }
        } else {
            // other types need parsing & creation
            //
        }
        return object;
    }
                    
      

    /**
     * Get the Statement column so that the results are mapped correctly.
     * (this has been copied from AbstractDatabaseAction and modified slightly)
     */
    public static Object getColumn(ResultSet set, Configuration column ) throws Exception {

        Integer type = (Integer) JDBCTypeConversions.typeConstants.get(column.getAttribute("type"));
        String dbcol = column.getAttribute("name");
        Object value = null;

        switch (type.intValue()) {
        case Types.CLOB:
        case Types.CHAR:
            Clob dbClob = set.getClob(dbcol);
            int length = (int) dbClob.length();
            InputStream asciiStream = new BufferedInputStream(dbClob.getAsciiStream());
            byte[] buffer = new byte[length];
            asciiStream.read(buffer);
            String str = new String(buffer);
            asciiStream.close();
            value = str;
            break;
        case Types.BIGINT:
            value = set.getBigDecimal(dbcol);
            break;
        case Types.TINYINT:
            value = new Byte(set.getByte(dbcol));
            break;
        case Types.VARCHAR:
            value  = set.getString(dbcol);
            break;
        case Types.DATE:
            value = set.getDate(dbcol);
            break;
        case Types.DOUBLE:
            value = new Double(set.getDouble(dbcol));
            break;
        case Types.FLOAT:
            value = new Float(set.getFloat(dbcol));
            break;
        case Types.INTEGER:
            value = new Integer(set.getInt(dbcol));
            break;
        case Types.NUMERIC:
            value = new Long(set.getLong(dbcol));
            break;
        case Types.SMALLINT:
            value = new Short(set.getShort(dbcol));
            break;
        case Types.TIME:
            value = set.getTime(dbcol);
            break;
        case Types.TIMESTAMP:
            value = set.getTimestamp(dbcol);
            break;
        case Types.ARRAY:
            value = set.getArray(dbcol); // new Integer(set.getInt(dbcol));
            break;
        case Types.BIT:
            value = new Boolean(set.getBoolean(dbcol));
            break;
        case Types.STRUCT:
            value = (Struct) set.getObject(dbcol);
            break;
        case Types.OTHER:
            value = set.getObject(dbcol);
            break;

        default:
            // The blob types have to be requested separately, via a Reader.
            value = "";
            break;
        }

        return value;
    }


    /**
     * Set the Statement column so that the results are mapped correctly.
     *
     * @param statement the prepared statement
     * @param position the position of the column
     * @param value the value of the column
     */
    public static void setColumn(PreparedStatement statement, int position, Object value, Integer typeObject) throws Exception {
        if (value instanceof String) {
            value = ((String) value).trim();
        }
        if (typeObject == null) {
            throw new SQLException("Can't set column because the type is unrecognized");
        }
        if (value == null) {
            /** If the value is null, set the column value null and return **/
            statement.setNull(position, typeObject.intValue());
            return;
        }
        if ("".equals(value)) {
            switch (typeObject.intValue()) {
            case Types.CHAR:
            case Types.CLOB:
            case Types.VARCHAR:
                /** If the value is an empty string and the column is
                    a string type, we can continue **/
                break;
            default:
                /** If the value is an empty string and the column
                    is something else, we treat it as a null value **/
                statement.setNull(position, typeObject.intValue());
                return;
            }
        }

        File file = null;
        int length = -1;
        InputStream asciiStream = null;

        //System.out.println("========================================================================");
        //System.out.println("JDBCTypeConversions: setting type "+typeObject.intValue());
        switch (typeObject.intValue()) {
        case Types.CLOB:
            //System.out.println("CLOB");
            Clob clob = null;
            if (value instanceof Clob) {
                clob = (Clob) value;
            } else if (value instanceof File) {
                File asciiFile = (File) value;
                asciiStream = new BufferedInputStream(new FileInputStream(asciiFile));
                length = (int) asciiFile.length();
                clob = new ClobHelper(asciiStream, length);
            } else if (value instanceof Part) {
                Part anyFile = (Part) value;
                asciiStream = new BufferedInputStream(anyFile.getInputStream());
                length = anyFile.getSize();
                clob = new ClobHelper(asciiStream, length);
            } else if (value instanceof JDBCxlobHelper) {
                asciiStream = ((JDBCxlobHelper) value).inputStream;
                length = ((JDBCxlobHelper) value).length;
                clob = new ClobHelper(asciiStream, length);
            } else if (value instanceof Source) {
                asciiStream = ((Source) value).getInputStream();
                length = (int)((Source) value).getContentLength();
                clob = new ClobHelper(asciiStream, length);
            } else {
                String asciiText = value.toString();
                asciiStream = new ByteArrayInputStream(asciiText.getBytes());
                length = asciiText.length();
                clob = new ClobHelper(asciiStream, length);
            }
            
            statement.setClob(position, clob);
            break;
        case Types.CHAR:
            // simple large object, e.g. Informix's TEXT
            //System.out.println("CHAR");
            
            if (value instanceof File) {
                File asciiFile = (File) value;
                asciiStream = new BufferedInputStream(new FileInputStream(asciiFile));
                length = (int) asciiFile.length();
            } else if (value instanceof JDBCxlobHelper) {
                asciiStream = ((JDBCxlobHelper) value).inputStream;
                length = ((JDBCxlobHelper) value).length;
            } else if (value instanceof Source) {
                asciiStream = ((Source) value).getInputStream();
                length = (int)((Source) value).getContentLength();
            } else if (value instanceof Part) {
                Part anyFile = (Part) value;
                asciiStream = new BufferedInputStream(anyFile.getInputStream());
                length = anyFile.getSize();
                clob = new ClobHelper(asciiStream, length);
            } else {
                String asciiText = value.toString();
                asciiStream = new BufferedInputStream(new ByteArrayInputStream(asciiText.getBytes()));
                length = asciiText.length();
            }
            
            statement.setAsciiStream(position, asciiStream, length);
            break;
        case Types.BIGINT:
            //System.out.println("BIGINT");
            BigDecimal bd = null;
            
            if (value instanceof BigDecimal) {
                bd = (BigDecimal) value;
            } else if (value instanceof Number) {
                bd = BigDecimal.valueOf(((Number)value).longValue());
            } else {
                bd = new BigDecimal(value.toString());
            }
            
            statement.setBigDecimal(position, bd);
            break;
        case Types.TINYINT:
            //System.out.println("TINYINT");
            Byte b = null;
            
            if (value instanceof Byte) {
                b = (Byte) value;
            } else if (value instanceof Number) {
                b = new Byte(((Number) value).byteValue());
            } else {
                b = new Byte(value.toString());
            }
            
            statement.setByte(position, b.byteValue());
            break;
        case Types.DATE:
            //System.out.println("DATE");
            Date d = null;
            
            if (value instanceof Date) {
                d = (Date) value;
            } else if (value instanceof java.util.Date) {
                d = new Date(((java.util.Date) value).getTime());
            } else if (value instanceof Calendar) {
                d = new Date(((Calendar) value).getTime().getTime());
            } else {
                d = Date.valueOf(value.toString());
            }
            
            statement.setDate(position, d);
            break;
        case Types.DOUBLE:
            //System.out.println("DOUBLE");
            double db;
            
            if (value instanceof Number) {
                db = (((Number) value).doubleValue());
            } else {
                db = Double.parseDouble(value.toString());
            }
            statement.setDouble(position, db);
            break;
        case Types.FLOAT:
            //System.out.println("FLOAT");
            float f;
            
            if (value instanceof Number) {
                f = (((Number) value).floatValue());
            } else {
                f = Float.parseFloat(value.toString());
            }
            statement.setFloat(position, f);
            break;
        case Types.NUMERIC:
            //System.out.println("NUMERIC");
            long l;
            
            if (value instanceof Number) {
                l = (((Number) value).longValue());
            } else {
                l = Long.parseLong(value.toString());
            }
            
            statement.setLong(position, l);
            break;
        case Types.SMALLINT:
            //System.out.println("SMALLINT");
            Short s = null;
            
            if (value instanceof Short) {
                s = (Short) value;
            } else if (value instanceof Number) {
                s = new Short(((Number) value).shortValue());
            } else {
                s = new Short(value.toString());
            }
            
            statement.setShort(position, s.shortValue());
            break;
        case Types.TIME:
            //System.out.println("TIME");
            Time t = null;
            
            if (value instanceof Time) {
                t = (Time) value;
            } else if (value instanceof java.util.Date){
                t = new Time(((java.util.Date) value).getTime());
            } else {
                t = Time.valueOf(value.toString());
            }
            
            statement.setTime(position, t);
            break;
        case Types.TIMESTAMP:
            //System.out.println("TIMESTAMP");
            Timestamp ts = null;
            
            if (value instanceof Time) {
                ts = (Timestamp) value;
            } else if (value instanceof java.util.Date) {
                ts = new Timestamp(((java.util.Date) value).getTime());
            } else {
                ts = Timestamp.valueOf(value.toString());
            }
            
            statement.setTimestamp(position, ts);
            break;
        case Types.ARRAY:
            //System.out.println("ARRAY");
            statement.setArray(position, (Array) value); // no way to convert string to array
            break;
        case Types.STRUCT:
            //System.out.println("STRUCT");
        case Types.OTHER:
            //System.out.println("OTHER");
            statement.setObject(position, value);
            break;
        case Types.LONGVARBINARY:
            //System.out.println("LONGVARBINARY");
            statement.setTimestamp(position, new Timestamp((new java.util.Date()).getTime()));
            break;
        case Types.VARCHAR:
            //System.out.println("VARCHAR");
            statement.setString(position, value.toString());
            break;
        case Types.BLOB:
            //System.out.println("BLOB");
            if (value instanceof JDBCxlobHelper) {
                statement.setBinaryStream(position, ((JDBCxlobHelper)value).inputStream, ((JDBCxlobHelper)value).length);
            } else if (value instanceof Source){
                statement.setBinaryStream(position, ((Source)value).getInputStream(), (int)((Source)value).getContentLength());
            } else {
                Blob blob = null;
                if (value instanceof Blob) {
                    blob = (Blob) value;
                } else if( value instanceof File) {
                    file = (File)value;
                    blob = new BlobHelper(new FileInputStream(file), (int) file.length());
                } else if (value instanceof String) {
                    file = new File((String)value);
                    blob = new BlobHelper(new FileInputStream(file), (int) file.length());
                } else if (value instanceof Part) {
                    Part anyFile = (Part) value;
                    blob = new BlobHelper(new BufferedInputStream(anyFile.getInputStream()), anyFile.getSize());
                } else {
                    throw new SQLException("Invalid type for blob: "+value.getClass().getName());
                }
                //InputStream input = new BufferedInputStream(new FileInputStream(file));
                statement.setBlob(position, blob);
            }
            break;
        case Types.VARBINARY:
            //System.out.println("VARBINARY");
            if (value instanceof JDBCxlobHelper) {
                statement.setBinaryStream(position, ((JDBCxlobHelper)value).inputStream, ((JDBCxlobHelper)value).length);
            } else if (value instanceof Source){
                statement.setBinaryStream(position, ((Source)value).getInputStream(), (int)((Source)value).getContentLength());
            } else if (value instanceof Part) {
                statement.setBinaryStream(position, ((Part)value).getInputStream(), ((Part)value).getSize());
            } else {
                if (value instanceof File) {           
                   file = (File)value;
               } else if (value instanceof String) {
                   file = new File((String)value);
               } else {
                   throw new SQLException("Invalid type for blob: "+value.getClass().getName());
                }
                 //InputStream input = new BufferedInputStream(new FileInputStream(file));
                 FileInputStream input = new FileInputStream(file);
                 statement.setBinaryStream(position, input, (int)file.length());
            }
            break;
        case Types.INTEGER:
            //System.out.println("INTEGER");
            Integer i = null;
            if (value instanceof Integer) {
                i = (Integer) value;
            } else if (value instanceof java.lang.Number) {
                i = new Integer(((java.lang.Number) value).intValue());
            } else {
                i = new Integer(value.toString());
            }
            statement.setInt(position, i.intValue());
            break;
        case Types.BIT:
            //System.out.println("BIT");
            Boolean bo = null;
            if (value instanceof Boolean) {
                bo = (Boolean) value;
            } else if (value instanceof java.lang.Number) {
                bo = new Boolean(((java.lang.Number) value).intValue()==1);
            } else {
                bo = new Boolean(value.toString());
            }
            statement.setBoolean(position, bo.booleanValue());
            break;
            
        default:
            //System.out.println("default");
            throw new SQLException("Impossible exception - invalid type ");
        }
        //System.out.println("========================================================================");
    }
    
    
}
