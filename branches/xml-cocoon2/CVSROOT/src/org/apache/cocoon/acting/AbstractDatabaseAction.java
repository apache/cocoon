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
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
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

import org.apache.avalon.Component;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;
import org.apache.avalon.SAXConfigurationHandler;
import org.apache.avalon.util.datasource.DataSourceComponent;

import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.components.parser.Parser;

/**
 * Set up environment for configurable form handling data.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-02-26 20:48:59 $
 */
public abstract class AbstractDatabaseAction extends ComposerAction implements Configurable {
    private static Map configurations = new HashMap();
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

        typeConstants = Collections.unmodifiableMap(constants);
    }

    /**
     * Set up the Database environment so that the configuration for the
     * Form is handled properly.  Please note that multiple Actions can
     * share the same configurations.  By using this approach, we can
     * premake our PreparedStatements, and cache them for later use.
     * Also note that the configuration file does not have to be a file.
     */
    protected Configuration getConfiguration(String descriptor) throws ConfigurationException {
        Configuration conf = null;

        synchronized (AbstractDatabaseAction.configurations) {
            conf = (Configuration) AbstractDatabaseAction.configurations.get(descriptor);

            if (conf == null) {
                URLFactory urlFactory = null;
                Parser parser = null;

                try {
                    urlFactory = (URLFactory) this.manager.lookup(Roles.URL_FACTORY);
                    URL resource = urlFactory.getURL(descriptor);

                    parser = (Parser)this.manager.lookup(Roles.PARSER);
                    SAXConfigurationHandler builder = new SAXConfigurationHandler();
                    InputSource inputStream = new InputSource(resource.openStream());

                    parser.setContentHandler(builder);
                    inputStream.setSystemId(resource.toExternalForm());
                    parser.parse(inputStream);

                    conf = builder.getConfiguration();
                } catch (Exception e) {
                    getLogger().error("Could not configure Database mapping environment", e);
                    throw new ConfigurationException("Error trying to load configurations");
                } finally {
                    if (urlFactory != null) this.manager.release((Component) urlFactory);
                    if (parser != null) this.manager.release((Component) parser);
                }

                this.cacheConfiguration(descriptor, conf);
            }
        }

        return conf;
    }

    /**
     * Cache the configuration so that we can use it later.
     */
    private void cacheConfiguration(String descriptor, Configuration conf) {
        synchronized (AbstractDatabaseAction.configurations) {
            AbstractDatabaseAction.configurations.put(descriptor, conf);
        }
    }

    /**
     * Get the Datasource we need.
     */
    protected final DataSourceComponent getDataSource(Configuration conf) throws ComponentManagerException {
        Configuration dsn = conf.getChild("connection");
        return (DataSourceComponent) this.dbselector.select(dsn.getValue(null));
    }

    /**
     * Set the Statement column so that the results are mapped correctly.
     */
    protected final void setColumn(PreparedStatement statement, int position, Object value, Configuration entry) throws Exception {
        Integer typeObject = (Integer) AbstractDatabaseAction.typeConstants.get(entry.getAttribute("type"));

        if (typeObject == null) {
            throw new SQLException("Can't set column because the type is invalid");
        }

        switch (typeObject.intValue()) {
            case Types.CLOB:
                File asciiFile = (File) value;
                FileInputStream asciiStream = new FileInputStream(asciiFile);
                statement.setAsciiStream(position, asciiStream, (int) asciiFile.length());
                break;
            case Types.BIGINT:
                statement.setBigDecimal(position, new BigDecimal((String) value));
                break;
            case Types.BLOB:
                File binaryFile = (File) value;
                FileInputStream binaryStream = new FileInputStream(binaryFile);
                statement.setBinaryStream(position, binaryStream, (int) binaryFile.length());
                break;
            case Types.TINYINT:
                statement.setByte(position, (new Byte((String) value)).byteValue());
                break;
            case Types.VARCHAR:
                statement.setString(position, (String) value);
                break;
            case Types.DATE:
                statement.setDate(position, new Date(this.dateValue((String) value, entry.getAttribute("format", "M/d/yyyy"))));
                break;
            case Types.DOUBLE:
                statement.setDouble(position, (new Double((String) value)).doubleValue());
                break;
            case Types.FLOAT:
                statement.setFloat(position, (new Float((String) value)).floatValue());
                break;
            case Types.INTEGER:
                statement.setInt(position, (new Integer((String) value)).intValue());
                break;
            case Types.NUMERIC:
                statement.setLong(position, (new Long((String) value)).longValue());
                break;
            case Types.SMALLINT:
                statement.setShort(position, (new Short((String) value)).shortValue());
                break;
            case Types.TIME:
                statement.setTime(position, new Time(this.dateValue((String) value, entry.getAttribute("format", "h:m:s a"))));
                break;
            case Types.TIMESTAMP:
                statement.setTimestamp(position, new Timestamp(this.dateValue((String) value, entry.getAttribute("format", "M/d/yyyy h:m:s a"))));
                break;
            case Types.OTHER:
                statement.setTimestamp(position, new Timestamp((new java.util.Date()).getTime()));
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
}