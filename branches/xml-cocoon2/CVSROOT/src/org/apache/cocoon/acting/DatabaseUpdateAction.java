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
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.Parameters;

import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.avalon.util.datasource.DataSourceComponent;

/**
 * Update a record in a database.  This Action assumes that there is
 * only one table at a time to update.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.15 $ $Date: 2001-03-19 21:20:12 $
 */
public class DatabaseUpdateAction extends AbstractDatabaseAction {
    private static final Map updateStatements = new HashMap();

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
            String query = this.getUpdateQuery(conf);
            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            HttpRequest request = (HttpRequest) objectModel.get(Constants.REQUEST_OBJECT);

            if (conn.getAutoCommit() == true) {
                conn.setAutoCommit(false);
            }

            PreparedStatement statement = conn.prepareStatement(query);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");
            Configuration[] values = conf.getChild("table").getChild("values").getChildren("value");
            currentIndex = 1;

            for (int i = 0; i < values.length; i++, currentIndex++) {
                this.setColumn(statement, currentIndex, request, values[i]);
            }

            for (int i = 0; i < keys.length; i++, currentIndex++) {
                this.setColumn(statement, currentIndex, request, keys[i]);
            }

            statement.execute();
            conn.commit();
            statement.close();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }

            throw new ProcessingException("Could not update record :position = " + currentIndex, e);
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
    protected String getUpdateQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseUpdateAction.updateStatements) {
            query = (String) DatabaseUpdateAction.updateStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Configuration[] keys = table.getChild("keys").getChildren("key");
                Configuration[] values = table.getChild("values").getChildren("value");

                StringBuffer queryBuffer = new StringBuffer("UPDATE ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" SET ");

                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                         queryBuffer.append(", ");
                    }

                    queryBuffer.append(values[i].getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }

                queryBuffer.append(" WHERE ");

                for (int i = 0; i < keys.length; i++) {
                    if (i > 0) {
                        queryBuffer.append(" AND ");
                    }

                    queryBuffer.append(keys[i].getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }

                query = queryBuffer.toString();

                DatabaseUpdateAction.updateStatements.put(conf, query);
            }
        }

        return query;
    }
}
