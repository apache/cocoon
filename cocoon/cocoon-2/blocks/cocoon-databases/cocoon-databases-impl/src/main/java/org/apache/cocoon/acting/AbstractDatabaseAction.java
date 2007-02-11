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
package org.apache.cocoon.acting;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.ImageProperties;
import org.apache.cocoon.util.ImageUtils;

/**
 * Set up environment for configurable form handling data.  It is
 * important to note that all DatabaseActions use a common configuration
 * format.  This group of actions are unique in that they employ a
 * terciary mapping.  There is the Form parameter, the database column,
 * and the type.
 *
 * Each configuration file must use the same format in order to be
 * effective.  The name of the root configuration element is irrelevant.
 *
 * <pre>
 *   &lt;root&gt;
 *     &lt;connection&gt;personnel&lt;connection&gt;
 *     &lt;table&gt;
 *       &lt;keys&gt;
 *         &lt;key param="id" dbcol="id" type="int"/&gt;
 *       &lt;/keys&gt;
 *       &lt;values&gt;
 *         &lt;value param="name" dbcol="name" type="string"/&gt;
 *         &lt;value param="department" dbcol="department_id" type="int"/&gt;
 *       &lt;/values&gt;
 *     &lt;/table&gt;
 *   &lt;/root&gt;
 * </pre>
 *
 * The types recognized by this system are:
 *
 * <table>
 *   <tr>
 *     <th>Type</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>ascii</td>
 *     <td>ASCII Input Stream, a CLOB input</td>
 *   </tr>
 *   <tr>
 *     <td>big-decimal</td>
 *     <td>a <code>java.math.BigDecimal</code> value</td>
 *   </tr>
 *   <tr>
 *     <td>binary</td>
 *     <td>Binary Input Stream, a BLOB input</td>
 *   </tr>
 *   <tr>
 *     <td>byte</td>
 *     <td>a Byte</td>
 *   </tr>
 *   <tr>
 *     <td>string</td>
 *     <td>a String</td>
 *   </tr>
 *   <tr>
 *     <td>date</td>
 *     <td>a Date</td>
 *   </tr>
 *   <tr>
 *     <td>double</td>
 *     <td>a Double</td>
 *   </tr>
 *   <tr>
 *     <td>float</td>
 *     <td>a Float</td>
 *   </tr>
 *   <tr>
 *     <td>int</td>
 *     <td>an Integer</td>
 *   </tr>
 *   <tr>
 *     <td>long</td>
 *     <td>a Long</td>
 *   </tr>
 *   <tr>
 *     <td>short</td>
 *     <td>a Short</td>
 *   </tr>
 *   <tr>
 *     <td>time</td>
 *     <td>a Time</td>
 *   </tr>
 *   <tr>
 *     <td>time-stamp</td>
 *     <td>a Timestamp</td>
 *   </tr>
 *   <tr>
 *     <td>now</td>
 *     <td>a Timestamp with the current day/time--the form value is ignored.</td>
 *   </tr>
 *   <tr>
 *     <td>image</td>
 *     <td>a binary image file, we cache the attribute information</td>
 *   </tr>
 *   <tr>
 *     <td>image-width</td>
 *     <td>
 *       the width attribute of the cached file attribute.  NOTE:
 *       param attribute must equal the param for image with a
 *       "-width" suffix.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>image-height</td>
 *     <td>
 *     the width attribute of the cached file attribute  NOTE:
 *       param attribute must equal the param for image with a
 *       "-height" suffix.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>image-size</td>
 *     <td>
 *       the size attribute of the cached file attribute  NOTE:
 *       param attribute must equal the param for image with a
 *       "-size" suffix.
 *     </td>
 *   </tr>
 * </table>
 *
 * @version $Id$
 */
public abstract class AbstractDatabaseAction extends AbstractComplementaryConfigurableAction
                                             implements Disposable {

    protected Map files = new HashMap();
    protected static final Map typeConstants;
    protected ServiceSelector dbselector;

    static {
        /*
         * Initialize the map of type names to jdbc column types.
         * Note that INTEGER, BLOB, and VARCHAR column types map to more than
         * one type name.
         */
        Map constants = new HashMap();
        constants.put("ascii", new Integer(Types.CLOB));
        constants.put("big-decimal", new Integer(Types.BIGINT));
        constants.put("binary", new Integer(Types.BLOB));
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
        constants.put("now", new Integer(Types.LONGVARBINARY));
        //constants.put("image", new Integer(Types.DISTINCT));
        //constants.put("image-width", new Integer(Types.ARRAY));
        //constants.put("image-height", new Integer(Types.BIT));
        //constants.put("image-size", new Integer(Types.CHAR));
        constants.put("image",new Integer(Types.BLOB));
        constants.put("image-width",new Integer(Types.INTEGER));
        constants.put("image-height",new Integer(Types.INTEGER));
        constants.put("image-size",new Integer(Types.INTEGER));
        constants.put("row-index",new Integer(Types.INTEGER));
        constants.put("image-mime-type",new Integer(Types.VARCHAR));
        constants.put("array", new Integer(Types.ARRAY));
        constants.put("row", new Integer(Types.STRUCT));
        constants.put("object", new Integer(Types.OTHER));
        typeConstants = Collections.unmodifiableMap(constants);
    }

    /**
     * Compose the Actions so that we can select our databases.
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.dbselector = (ServiceSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
    }

    /**
     * Get the Datasource we need.
     */
    protected final DataSourceComponent getDataSource(Configuration conf) throws ServiceException {
        Configuration dsn = conf.getChild("connection");
        return (DataSourceComponent) this.dbselector.select(dsn.getValue(""));
    }

    /**
     * Return whether a type is a Large Object (BLOB/CLOB).
     */
    protected final boolean isLargeObject (String type) {
        if ("ascii".equals(type)) return true;
        if ("binary".equals(type)) return true;
        if ("image".equals(type)) return true;

        return false;
    }

    /**
     * Get the Statement column so that the results are mapped correctly.
     */
    protected Object getColumn(ResultSet set, Request request, Configuration entry)
    throws Exception {
        Integer type = (Integer) AbstractDatabaseAction.typeConstants.get(entry.getAttribute("type"));
        String attribute = entry.getAttribute("param", "");
        String dbcol = entry.getAttribute("dbcol", "");
        Object value = null;

        switch (type.intValue()) {
            case Types.CLOB:
                Clob dbClob = set.getClob(dbcol);
                if (dbClob != null) {
                    int length = (int) dbClob.length();
                    InputStream is = new BufferedInputStream(dbClob.getAsciiStream());
                    try {
                        byte[] buffer = new byte[length];
                        length = is.read(buffer);
                        value = new String(buffer, 0, length);
                    } finally {
                        is.close();
                    }
                }
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
                value = set.getArray(dbcol);
                break;
            case Types.BIT:
                value = new Integer(set.getInt(dbcol));
                break;
            case Types.CHAR:
                value = new Integer(set.getInt(dbcol));
                break;
        case Types.STRUCT:
        value = set.getObject(dbcol);
        break;
        case Types.OTHER:
        value = set.getObject(dbcol);
        break;

            default:
                // The blob types have to be requested separately, via a Reader.
                value = "";
                break;
        }

        setRequestAttribute(request,attribute,value);

        return value;
    }

    /**
     * Set the Statement column so that the results are mapped correctly.
     * The name of the parameter is retrieved from the configuration object.
     *
     * @param statement the prepared statement
     * @param position the position of the column
     * @param request the request
     * @param entry the configuration object
     */
    protected void setColumn(PreparedStatement statement, int position, Request request, Configuration entry)
    throws Exception {
        setColumn(statement,position,request,entry,entry.getAttribute("param",""));
    }

    /**
     * Set the Statement column so that the results are mapped correctly. The
     * value of the column is retrieved from the request object. If the
     * named parameter exists in the request object's parameters, that value
     * is used. Otherwise if the named parameter exists in the request object's
     * attributes, that value is used. Otherwise the request object is
     * retrieved using Request.get(attribute), which is documented to be the
     * same as Request.getAttribute(attribute), so something weird must be
     * going on.
     *
     * @param statement the prepared statement
     * @param position the position of the column
     * @param request the request
     * @param entry the configuration object
     * @param param the name of the request parameter
     */
    protected void setColumn(PreparedStatement statement, int position, Request request, Configuration entry, String param)
    throws Exception {
        Object value = request.getParameter(param);
        if (value == null) value = request.getAttribute(param);
        if (value == null) value = request.get(param);
        setColumn(statement,position,request,entry,param,value);
    }

    /**
     * Set the Statement column so that the results are mapped correctly.
     *
     * @param statement the prepared statement
     * @param position the position of the column
     * @param request the request
     * @param entry the configuration object
     * @param param the name of the request parameter
     * @param value the value of the column
     */
    protected void setColumn(PreparedStatement statement, int position, Request request, Configuration entry, String param, Object value) throws Exception {
        setColumn(statement,position,request,entry,param,value,0);
    }

    /**
     * Set the Statement column so that the results are mapped correctly.
     *
     * @param statement the prepared statement
     * @param position the position of the column
     * @param request the request
     * @param entry the configuration object
     * @param param the name of the request parameter
     * @param value the value of the column
     * @param rowIndex the index of the current row for manyrows inserts
     */
    protected void setColumn(PreparedStatement statement, int position, Request request, Configuration entry, String param, Object value, int rowIndex) throws Exception {
        getLogger().debug("Setting column "+position+" named "+param+" with value "+value);
        if (value instanceof String) {
            value = ((String) value).trim();
        }
        String typeName = entry.getAttribute("type");
        Integer typeObject = (Integer) AbstractDatabaseAction.typeConstants.get(typeName);
        if (typeObject == null) {
            throw new SQLException("Can't set column because the type "+typeName+" is unrecognized");
        }
        if (value == null) {
            /** If the value is null, set the column value null and return **/
            if (typeName.equals("image-width") || typeName.equals("image-height") || typeName.equals("image-size") || typeName.equals("row-index") || typeName.equals("image-mime-type")) {
              /** these column types are automatically generated so it's ok **/
            } else {
              statement.setNull(position, typeObject.intValue());
              return;
            }
        }
        if ("".equals(value)) {
            switch (typeObject.intValue()) {
                case Types.CHAR:
                case Types.CLOB:
                case Types.VARCHAR:
                    /** If the value is an empty string and the column is
                        a string type, we can continue **/
                    break;
                case Types.INTEGER:
                  if (typeName.equals("image-width") || typeName.equals("image-height") || typeName.equals("image-size") || typeName.equals("row-index")) {
                    /** again, these types are okay to be absent **/
                    break;
                  }
                default:
                    /** If the value is an empty string and the column
                        is something else, we treat it as a null value **/
                    statement.setNull(position, typeObject.intValue());
                    return;
            }
        }

        /** Store the column value in the request attribute
            keyed by the request parameter name. we do this so possible future
            actions can access this data. not sure about the key tho... **/
        setRequestAttribute(request,param,value);
        File file;

        switch (typeObject.intValue()) {
            case Types.CLOB:
                int length = -1;
                InputStream asciiStream = null;

                if (value instanceof File) {
                    File asciiFile = (File) value;
                    asciiStream = new BufferedInputStream(new FileInputStream(asciiFile));
                    length = (int) asciiFile.length();
                } else {
                    String asciiText = (String) value;
                    asciiStream = new BufferedInputStream(new ByteArrayInputStream(asciiText.getBytes()));
                    length = asciiText.length();
                }

                statement.setAsciiStream(position, asciiStream, length);
                break;
            case Types.BIGINT:
                BigDecimal bd = null;

                if (value instanceof BigDecimal) {
                    bd = (BigDecimal) value;
                } else {
                    bd = new BigDecimal((String) value);
                }

                statement.setBigDecimal(position, bd);
                break;
            case Types.TINYINT:
                Byte b = null;

                if (value instanceof Byte) {
                    b = (Byte) value;
                } else {
                    b = new Byte((String) value);
                }

                statement.setByte(position, b.byteValue());
                break;
            case Types.DATE:
                Date d = null;

                if (value instanceof Date) {
                    d = (Date) value;
                } else if (value instanceof java.util.Date) {
                    d = new Date(((java.util.Date) value).getTime());
                } else {
                    d = new Date(this.dateValue((String) value, entry.getAttribute("format", "M/d/yyyy")));
                }

                statement.setDate(position, d);
                break;
            case Types.DOUBLE:
                Double db = null;

                if (value instanceof Double) {
                    db = (Double) value;
                } else {
                    db = new Double((String) value);
                }

                statement.setDouble(position, db.doubleValue());
                break;
            case Types.FLOAT:
                Float f = null;

                if (value instanceof Float) {
                    f = (Float) value;
                } else {
                    f = new Float((String) value);
                }

                statement.setFloat(position, f.floatValue());
                break;
            case Types.NUMERIC:
                Long l = null;

                if (value instanceof Long) {
                    l = (Long) value;
                } else {
                    l = new Long((String) value);
                }

                statement.setLong(position, l.longValue());
                break;
            case Types.SMALLINT:
                Short s = null;

                if (value instanceof Short) {
                    s = (Short) value;
                } else {
                    s = new Short((String) value);
                }

                statement.setShort(position, s.shortValue());
                break;
            case Types.TIME:
                Time t = null;

                if (value instanceof Time) {
                    t = (Time) value;
                } else {
                    t = new Time(this.dateValue((String) value, entry.getAttribute("format", "h:m:s a")));
                }

                statement.setTime(position, t);
                break;
            case Types.TIMESTAMP:
                Timestamp ts = null;

                if (value instanceof Time) {
                    ts = (Timestamp) value;
                } else {
                    ts = new Timestamp(this.dateValue((String) value, entry.getAttribute("format", "M/d/yyyy h:m:s a")));
                }

                statement.setTimestamp(position, ts);
                break;
            case Types.ARRAY:
                statement.setArray(position, (Array) value); // no way to convert string to array
                break;
            case Types.STRUCT:
            case Types.OTHER:
                statement.setObject(position, value);
                break;
            case Types.LONGVARBINARY:
                statement.setTimestamp(position, new Timestamp((new java.util.Date()).getTime()));
                break;
            case Types.VARCHAR:
                if ("string".equals(typeName)) {
                   statement.setString(position, (String) value);
                   break;
                } else if ("image-mime-type".equals(typeName)) {
                    String imageAttr = param.substring(0, (param.length() - "-mime-type".length()));
                    file = (File) request.get(imageAttr);
                    synchronized (this.files) {
                        Parameters parameters = (Parameters) this.files.get(file);
                        String imageMimeType = parameters.getParameter("image-mime-type",
                                                                       (String) settings.get("image-mime-type",""));
                        statement.setString(position, imageMimeType);
                        /** Store the image mime type in the request attributes.
                            Why do we do this? **/
                        setRequestAttribute(request, param, imageMimeType);
                    }
                    break;
                }
            case Types.BLOB:
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
                if ("image".equals(typeName)) {
                    /** If this column type is an image, store the
                        size, width, and height in a static table **/
                    Parameters parameters = new Parameters();
                    parameters.setParameter("image-size", Long.toString(file.length()));
                    ImageProperties prop = ImageUtils.getImageProperties(file);
                    parameters.setParameter("image-width", Integer.toString(prop.width));
                    parameters.setParameter("image-height", Integer.toString(prop.height));
                    // TC: if it's really mime-type shouldn't we prepend "image/"?
                    parameters.setParameter("image-mime-type",prop.type);
                    synchronized (this.files) {
                        this.files.put(file, parameters);
                    }
                }
                break;
            case Types.INTEGER:
                if ("int".equals(typeName)) {
                    Integer i = null;
                    if (value instanceof Integer) {
                        i = (Integer) value;
                    } else {
                        i = new Integer((String) value);
                    }
                    statement.setInt(position, i.intValue());
                    break;
                } else if ("image-width".equals(typeName)) {
                    /** Get the image width from the cached image data **/
                    /** Is this why we store the values in the request
                        attributes? **/
                    String imageAttr = param.substring(0, (param.length() - "-width".length()));
                    file = (File) request.get(imageAttr);
                    synchronized (this.files) {
                        Parameters parameters = (Parameters) this.files.get(file);
                        statement.setInt(position, parameters.getParameterAsInteger("image-width",
                         Integer.parseInt((String)settings.get("image-width","-1"))));
                        /** Store the image width in the request attributes.
                            Why do we do this? **/
                        setRequestAttribute(request,
                        param,
                        parameters.getParameter("image-width",
                                    (String) settings.get("image-width","")));
                    }
                    break;
                } else if ("image-height".equals(typeName)) {
                    /** Get the image height from the cached image data **/
                    String imageAttr = param.substring(0, (param.length() - "-height".length()));
                    file = (File) request.get(imageAttr);
                    synchronized (this.files) {
                        Parameters parameters = (Parameters) this.files.get(file);
                        statement.setInt(position, parameters.getParameterAsInteger("image-height",
                         Integer.parseInt((String)settings.get("image-height","-1"))));
                        setRequestAttribute(request,
                        param,
                        parameters.getParameter("image-height",
                                    (String) settings.get("image-height","")));
                    }
                    break;
                } else if ("image-size".equals(typeName)) {
                    /** Get the image file size from the cached image data **/
                    String imageAttr = param.substring(0, (param.length() - "-size".length()));
                    file = (File) request.get(imageAttr);
                    synchronized (this.files) {
                        Parameters parameters = (Parameters) this.files.get(file);
                        statement.setInt(position, parameters.getParameterAsInteger("image-size",
                         Integer.parseInt((String)settings.get("image-height","-1"))));
                        setRequestAttribute(request,
                        param,
                        parameters.getParameter("image-size",
                                    (String) settings.get("image-size","")));
                    }
                    break;
                } else if ("row-index".equals(typeName)) {
                    statement.setInt(position,rowIndex);
                    break;
                }
            default:
                throw new SQLException("Impossible exception - invalid type "+typeName);
        }
    }

    /**
     * Convert a String to a long value.
     */
    private final long dateValue(String value, String format) throws Exception {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(value).getTime();
    }

    /**
     *  dispose
     */
    public void dispose() {
        this.manager.release(dbselector);
    }

    /**
     * Store a key/value pair in the request attributes. We prefix the key
     * with the name of this class to prevent potential name collisions.
     */
    protected void setRequestAttribute(Request request, String key, Object value) {
      request.setAttribute("org.apache.cocoon.acting.AbstractDatabaseAction:"+key,value);
    }

    /**
     * Retreive a value from the request attributes.
     */
    protected Object getRequestAttribute(Request request, String key) {
      return request.getAttribute("org.apache.cocoon.acting.AbstractDatabaseAction:"+key);
    }

    /**
     * Build a separed list with the Values of a Configuration Array
     * @param values - build the list from
     * @param separator - Put a separator between the values of the list
     * @return - an StringBuffer with the builded List
     * @throws ConfigurationException
     */
    protected StringBuffer buildList(Configuration[] values, String separator) throws ConfigurationException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                buffer.append(separator);
            }
            buffer.append(values[i].getAttribute("dbcol"));
            buffer.append(" = ?");
        }
        return buffer;
    }

    /**
     * Build a separed list with the Values of a Configuration Array
     * @param values - build the list from
     * @param begin - Initial index
     * @return - an StringBuffer with the builded List
     * @throws ConfigurationException
     */
    protected StringBuffer buildList(Configuration[] values, int begin) throws ConfigurationException {
        StringBuffer buffer = new StringBuffer();
        int length = values.length;
        boolean prependComma = begin > 0;
        for (int i = 0; i < length; i++) {
            if (prependComma) {
                buffer.append(", ");
            } else {
                prependComma = true;
            }
            buffer.append(values[i].getAttribute("dbcol"));
        }
        return buffer;
    }
}
