/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.EntityResolver;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.avalon.util.datasource.DataSourceComponent;

/**
 * Add a record in a database.  This Action assumes that there is
 * only one table at a time to update.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-03-09 16:10:27 $
 */
public class DatabaseAddAction extends AbstractDatabaseAction {
    private static final Map addStatements = new HashMap();
    private static final Map selectStatements = new HashMap();

    /**
     * Delete a record from the database.  This action assumes that
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

            Iterator keys = conf.getChild("table").getChild("keys").getChildren("key");
            Iterator values = conf.getChild("table").getChild("values").getChildren("value");
            currentIndex = 1;

            while (keys.hasNext()) {
                Configuration key = (Configuration) keys.next();
                if ("manual".equals(key.getAttribute("mode", "automatic"))) {
                    String selectQuery = this.getSelectQuery(key);

                    ResultSet set = conn.createStatement().executeQuery(selectQuery);
                    set.next();
                    int value = set.getInt("maxid") + 1;

                    statement.setInt(currentIndex, value);

                    set.close();
                    set.getStatement().close();
                    currentIndex++;
                }
            }

            for (int i = currentIndex; values.hasNext(); i++) {
                Configuration itemConf = (Configuration) values.next();
                this.setColumn(statement, i, request, itemConf);
                currentIndex = i;
            }

            statement.execute();
            conn.commit();
            statement.close();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }

            throw new ProcessingException("Could not add record :position = " + currentIndex, e);
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
    private final String getAddQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseAddAction.addStatements) {
            query = (String) DatabaseAddAction.addStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Iterator values = table.getChild("values").getChildren("value");
                Iterator keys = table.getChild("keys").getChildren("key");

                StringBuffer queryBuffer = new StringBuffer("INSERT INTO ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" (");

                boolean firstIteration = true;
                int numKeys = 0;

                while (keys.hasNext()) {
                    Configuration key = (Configuration) keys.next();
                    if ("manual".equals(key.getAttribute("mode", "automatic"))) {
                        if (firstIteration) {
                            firstIteration = false;
                        } else {
                            queryBuffer.append(", ");
                        }

                        queryBuffer.append(key.getAttribute("dbcol"));
                        this.setSelectQuery(table.getAttribute("name"), key);
                        numKeys++;
                    }
                }

                int numValues = 0;

                while (values.hasNext()) {
                    if (firstIteration) {
                        firstIteration = false;
                    } else {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append(((Configuration) values.next()).getAttribute("dbcol"));
                    numValues++;
                }

                queryBuffer.append(") VALUES (");

                int numParams = numValues + numKeys;

                for (int i = 0; i < numParams; i++) {
                    if (i > 0) {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append("?");
                }

                queryBuffer.append(")");

                query = queryBuffer.toString();

                DatabaseAddAction.addStatements.put(conf, query);
            }
        }

        return query;
    }

    /**
     * Set the String representation of the MaxID lookup statement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    private final synchronized void setSelectQuery(String tableName, Configuration entry) throws ConfigurationException {
        StringBuffer queryBuffer = new StringBuffer("SELECT max(");
        queryBuffer.append(entry.getAttribute("dbcol"));
        queryBuffer.append(") AS maxid FROM ");
        queryBuffer.append(tableName);

        DatabaseAddAction.selectStatements.put(entry, queryBuffer.toString());
    }

    private final synchronized String getSelectQuery(Configuration entry) throws ConfigurationException {
        return (String) DatabaseAddAction.selectStatements.get(entry);
    }
}