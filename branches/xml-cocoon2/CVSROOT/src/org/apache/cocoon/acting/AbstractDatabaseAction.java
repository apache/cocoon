/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Array;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Blob;
import java.sql.Clob;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.ComponentSelector;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.Disposable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.parameters.Parameters;
import org.apache.avalon.configuration.SAXConfigurationHandler;
import org.apache.excalibur.datasource.DataSourceComponent;

import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.components.parser.Parser;

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
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.28 $ $Date: 2001-04-20 20:49:44 $
 */
public abstract class AbstractDatabaseAction extends AbstractComplementaryConfigurableAction implements Configurable, Disposable {
    protected Map files = new HashMap();
    private static final Map typeConstants;
    protected ComponentSelector dbselector;

    static {
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
        constants.put("now", new Integer(Types.OTHER));
        constants.put("image", new Integer(Types.DISTINCT));
        constants.put("image-width", new Integer(Types.ARRAY));
        constants.put("image-height", new Integer(Types.BIT));
        constants.put("image-size", new Integer(Types.CHAR));

        typeConstants = Collections.unmodifiableMap(constants);
    }

    /**
     * Compose the Actions so that we can select our databases.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.dbselector = (ComponentSelector) manager.lookup(Roles.DB_CONNECTION);

        super.compose(manager);
    }
    /**
     * Get the Datasource we need.
     */
    protected final DataSourceComponent getDataSource(Configuration conf) throws ComponentException {
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
                value = new Integer(set.getInt(dbcol));
                break;
            case Types.BIT:
                value = new Integer(set.getInt(dbcol));
                break;
            case Types.CHAR:
                value = new Integer(set.getInt(dbcol));
                break;
            default:
                // The blob types have to be requested separately, via a Reader.
                value = "";
                break;
        }

        request.setAttribute(attribute, value);

        return value;
    }

    /**
     * Set the Statement column so that the results are mapped correctly.
     */
    protected void setColumn(PreparedStatement statement, int position, Request request, Configuration entry)
    throws Exception {
        Integer typeObject = (Integer) AbstractDatabaseAction.typeConstants.get(entry.getAttribute("type"));

        if (typeObject == null) {
            throw new SQLException("Can't set column because the type is invalid");
        }

        String attribute = entry.getAttribute("param", "");
        Object value = request.getParameter(attribute);

        if (value == null) value = request.getAttribute(attribute);
        if (value == null) value = request.get(attribute);

        if (value instanceof String) {
            value = ((String) value).trim();
        }

        if (value == null || "".equals(value)) {
            switch (typeObject.intValue()) {
                case Types.DISTINCT:
                    statement.setNull(position, Types.BINARY);
                    return;
                case Types.ARRAY:
                case Types.BIT:
                case Types.CHAR:
                    if (value == null) {
                        statement.setNull(position, Types.INTEGER);
                    }
                    break;
                case Types.CLOB:
                case Types.VARCHAR:
                case Types.OTHER:
                    break;
                default:
                    statement.setNull(position, typeObject.intValue());
                    return;
            }
        }

        request.setAttribute(attribute, value);

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
            case Types.BLOB:
                File binaryFile = (File) value;
                InputStream binaryStream = new BufferedInputStream(new FileInputStream(binaryFile));
                statement.setBinaryStream(position, binaryStream, (int) binaryFile.length());
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
            case Types.VARCHAR:
                statement.setString(position, (String) value);
                break;
            case Types.DATE:
                Date d = null;

                if (value instanceof Date) {
                    d = (Date) value;
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
            case Types.INTEGER:
                Integer i = null;

                if (value instanceof Integer) {
                    i = (Integer) value;
                } else {
                    i = new Integer((String) value);
                }

                statement.setInt(position, i.intValue());
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
            case Types.OTHER:
                statement.setTimestamp(position, new Timestamp((new java.util.Date()).getTime()));
                break;
            case Types.DISTINCT:
                // Upload an image (just like binary), but cache attributes
                Parameters param = new Parameters();
                File imageFile = (File) value;
                InputStream imageStream = new BufferedInputStream(new FileInputStream(imageFile));
                statement.setBinaryStream(position, imageStream, (int) imageFile.length());

                param.setParameter("image-size", Long.toString(imageFile.length()));

                int [] dimensions = ImageDirectoryGenerator.getSize(imageFile);
                param.setParameter("image-width", Integer.toString(dimensions[0]));
                param.setParameter("image-height", Integer.toString(dimensions[1]));

                synchronized (this.files) {
                    this.files.put(imageFile, param);
                }
                break;
            case Types.ARRAY:
                // Grab the image-width attribute from the cached attributes
                String imageAttr = attribute.substring(0, (attribute.length() - "-width".length()));
                imageFile = (File) request.get(imageAttr);
                synchronized (this.files) {
                    param = (Parameters) this.files.get(imageFile);
                    statement.setInt(position, param.getParameterAsInteger("image-width", -1));
                    request.setAttribute(attribute, param.getParameter("image-width", ""));
                }
                break;
            case Types.BIT:
                // Grab the image-height attribute from the cached attributes
                imageAttr = attribute.substring(0, (attribute.length() - "-height".length()));
                imageFile = (File) request.get(imageAttr);
                synchronized (this.files) {
                    param = (Parameters) this.files.get(imageFile);
                    statement.setInt(position, param.getParameterAsInteger("image-height", -1));
                    request.setAttribute(attribute, param.getParameter("image-height", ""));
                }
                break;
            case Types.CHAR:
                // Grab the image-size attribute from the cached attributes
                imageAttr = attribute.substring(0, (attribute.length() - "-size".length()));
                imageFile = (File) request.get(imageAttr);
                synchronized (this.files) {
                    param = (Parameters) this.files.get(imageFile);
                    statement.setInt(position, param.getParameterAsInteger("image-size", -1));
                    request.setAttribute(attribute, param.getParameter("image-size", ""));
                }
                break;
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
        this.manager.release((Component)dbselector);
        super.dispose();
    }
}
