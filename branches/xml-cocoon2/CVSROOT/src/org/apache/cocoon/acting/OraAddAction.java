/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import org.apache.avalon.util.datasource.DataSourceComponent;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.generation.ImageDirectoryGenerator;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.sql.BLOB;
import oracle.sql.CLOB;
import oracle.jdbc.OracleResultSet;

import org.xml.sax.EntityResolver;

/**
 * Add a record in a database.  This Action assumes that there is
 * only one table at a time to update.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-03-13 18:12:17 $
 */
public class OraAddAction extends DatabaseAddAction {
    private static final Map selectLOBStatements = new HashMap();

    /**
     * Add a record to the database.  This action assumes that
     * the file referenced by the "form-descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public Map act(EntityResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        int currentIndex = 0;

        try {
            Configuration conf = this.getConfiguration(param.getParameter("form-descriptor", null));
            String query = this.getAddQuery(conf);

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            HttpRequest request = (HttpRequest) objectModel.get(Constants.REQUEST_OBJECT);

            if (conn.getAutoCommit() == true) {
                conn.setAutoCommit(false);
            }

            PreparedStatement statement = conn.prepareStatement(query);
            getLogger().info(query);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");
            Configuration[] values = conf.getChild("table").getChild("values").getChildren("value");
            currentIndex = 1;

            // Insert the keys into the query
            for (int i = 0; i < keys.length; i++) {
                String mode = keys[i].getAttribute("mode", "automatic");

                if ("manual".equals(mode)) {
                    String selectQuery = this.getSelectQuery(keys[i]);

                    ResultSet set = conn.createStatement().executeQuery(selectQuery);
                    set.next();
                    int value = set.getInt("maxid") + 1;

                    statement.setInt(currentIndex, value);

                    request.setAttribute(keys[i].getAttribute("param"), String.valueOf(value));

                    set.close();
                    set.getStatement().close();
                    currentIndex++;
                } else if ("form".equals(mode)) {
                    String parameter = keys[i].getAttribute("param");
                    request.setAttribute(parameter, request.getParameter(parameter));
                    this.setColumn(statement, currentIndex, request, keys[i]);
                    currentIndex++;
                }
            }

            // insert the values into the query
            for (int i = 0; i < values.length; i++) {
                String type = values[i].getAttribute("type");
                String parameter = values[i].getAttribute("param");

                if (type.equals("image")) {
                    File binaryFile = (File) request.get(parameter);
                    Parameters iparam = new Parameters();

                    iparam.setParameter("image-size", String.valueOf(binaryFile.length()));

                    int [] dimensions = ImageDirectoryGenerator.getSize(binaryFile);
                    iparam.setParameter("image-width", String.valueOf(dimensions[0]));
                    iparam.setParameter("image-height", String.valueOf(dimensions[1]));

                    synchronized (this.files) {
                        this.files.put(binaryFile, param);
                    }
                }

                if (! this.isLargeObject(type)) {
                    this.setColumn(statement, currentIndex, request, values[i]);
                    currentIndex++;
                }
            }

            statement.execute();
            statement.close();

            query = this.getSelectLOBQuery(conf);

            // Process the large objects if they exist
            if (query != null) {
                PreparedStatement LOBstatement = conn.prepareStatement(query);
                getLogger().info(query);

                if (keys.length > 0) {
                    currentIndex = 1;

                    for (int i = 0; i < keys.length; i++) {
                        this.setColumn(LOBstatement, currentIndex, request, keys[i]);
                        currentIndex++;
                    }
                }

                OracleResultSet set = (OracleResultSet) LOBstatement.executeQuery();

                if (set.next()) {
                    int index = 0;

                    for (int i = 0; i < values.length; i++) {
                        Object attr = request.get(values[i].getAttribute("param"));
                        int length = -1;
                        InputStream stream = null;
                        OutputStream output = null;
                        int bufSize = 1024;

                        String type = values[i].getAttribute("type", "");
                        if (this.isLargeObject(type)) {
                            index++;

                            if (type.equals("ascii")) {
                                CLOB ascii = set.getCLOB(index);

                                if (attr instanceof File) {
                                    File asciiFile = (File) attr;
                                    stream = new BufferedInputStream(new FileInputStream(asciiFile));
                                } else {
                                    String asciiText = (String) attr;
                                    stream = new BufferedInputStream(new ByteArrayInputStream(asciiText.getBytes()));
                                }

                                output = ascii.getAsciiOutputStream();
                                bufSize = ascii.getBufferSize();
                            } else {
                                BLOB binary = set.getBLOB(index);
                                File binaryFile = (File) attr;
                                stream = new BufferedInputStream(new FileInputStream(binaryFile));
                                length = (int) binaryFile.length();

                                output = binary.getBinaryOutputStream();
                                bufSize = binary.getBufferSize();
                            }
                        }

                        byte[] buffer = new byte[bufSize];
                        while ((length = stream.read(buffer)) != -1) {
                            output.write(buffer, 0, length);
                        }

                        stream.close();
                        output.close();
                    }
                }

                set.close();
                set.getStatement().close();
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    getLogger().debug("There was an error rolling back the transaction", se);
                }
            }

            throw new ProcessingException("Could not add record :position = " + (currentIndex - 1), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqe) {
                    getLogger().warn("There was an error closing the datasource", sqe);
                }
            }

            if (datasource != null) this.dbselector.release(datasource);
        }

        return null;
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    protected final String getAddQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseAddAction.addStatements) {
            query = (String) DatabaseAddAction.addStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");

                StringBuffer queryBuffer = new StringBuffer("INSERT INTO ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" (");

                int numKeys = 0;

                for (int i = 0; i < keys.length; i++) {
                    String mode = keys[i].getAttribute("mode", "automatic");
                    if ("manual".equals(mode) || "form".equals(mode)) {
                        if (numKeys > 0) {
                            queryBuffer.append(", ");
                        }

                        queryBuffer.append(keys[i].getAttribute("dbcol"));

                        if ("manual".equals(mode)) {
                            this.setSelectQuery(table.getAttribute("name"), keys[i]);
                        }

                        numKeys++;
                    }
                }

                for (int i = 0; i < values.length; i++) {
                    if ((numKeys + i) > 0) {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append(values[i].getAttribute("dbcol"));
                }

                queryBuffer.append(") VALUES (");

                numKeys = 0;

                for (int i = 0; i < keys.length; i++) {
                    if (numKeys > 0) queryBuffer.append(", ");
                    if ("automatic".equals(keys[i].getAttribute("mode", "automatic")) == false) {
                        queryBuffer.append("?");
                        numKeys++;
                    }
                }

                for (int i = 0; i < values.length; i++) {
                    if ((numKeys + i) > 0) {
                        queryBuffer.append(", ");
                    }

                    if (this.isLargeObject(values[i].getAttribute("type"))) {
                        if (values[i].getAttribute("type").equals("ascii")) {
                             queryBuffer.append("empty_clob()");
                        } else {
                             queryBuffer.append("empty_blob()");
                        }
                    } else {
                        queryBuffer.append("?");
                    }
                }

                queryBuffer.append(")");

                query = queryBuffer.toString();

                DatabaseAddAction.addStatements.put(conf, query);
            }
        }

        if ("".equals(query)) return null;

        return query;
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    private final String getSelectLOBQuery (Configuration conf)
    throws ConfigurationException {
        String query = null;

        synchronized (OraAddAction.selectLOBStatements) {
            query = (String) OraAddAction.selectLOBStatements.get(conf);

            // executes only if query is null.
            if (query == null) {
                StringBuffer queryBuffer = new StringBuffer("SELECT ");
                Configuration table = conf.getChild("table");
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");
                int numLobs = 0;

                // Add the values to the query
                for (int i = 0; i < values.length; i++) {
                    if (this.isLargeObject(values[i].getAttribute("type"))) {
                        numLobs++;

                        if (numLobs > 1) {
                            queryBuffer.append(", ");
                        }

                        queryBuffer.append(values[i].getAttribute("dbcol"));
                    }
                }

                if (numLobs < 1) {
                    // if query is set to "", then the Action won't
                    // try to process it again.
                    query = "";
                    OraAddAction.selectLOBStatements.put(conf, query);
                    return null;
                }

                queryBuffer.append(" FROM ").append(table.getAttribute("name"));

                // Process the WHERE clause
                if (keys.length > 0) {
                    queryBuffer.append(" WHERE ");

                    // Add the keys to the query
                    for (int i = 0; i < keys.length; i++) {
                        if (i > 0) {
                            queryBuffer.append(" AND ");
                        }

                        queryBuffer.append(keys[i].getAttribute("dbcol"));
                        queryBuffer.append(" = ?");
                    }
                }

                query = queryBuffer.toString().trim();
                OraAddAction.selectLOBStatements.put(conf, query);
            }
        }

        if ("".equals(query)) return null;

        return query;
    }
}