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
 * Update a record in a database.  This Action assumes that there is
 * only one table at a time to update.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2001-02-26 22:22:08 $
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

        try {
            Configuration conf = this.getConfiguration(param.getParameter("form-descriptor", null));
            String query = this.getUpdateQuery(conf);
            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            HttpRequest request = (HttpRequest) objectModel.get(Constants.REQUEST_OBJECT);

            PreparedStatement statement = conn.prepareStatement(query);

            Iterator keys = conf.getChild("table").getChild("keys").getChildren("key");
            Iterator values = conf.getChild("table").getChild("values").getChildren("value");
            int currentIndex = 1;

            for (int i = currentIndex; values.hasNext(); i++) {
                Configuration itemConf = (Configuration) values.next();
                String parameter = itemConf.getAttribute("param");
                Object value = request.get(parameter);
                this.setColumn(statement, i, value, itemConf);
                currentIndex = i;
            }

            for (int i = currentIndex; keys.hasNext(); i++) {
                Configuration itemConf = (Configuration) keys.next();
                String parameter = itemConf.getAttribute("param");
                Object value = request.get(parameter);
                this.setColumn(statement, i, value, itemConf);
            }

            statement.execute();
        } catch (Exception e) {
            throw new ProcessingException("Could not updaterecord", e);
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
    private final String getUpdateQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseUpdateAction.updateStatements) {
            query = (String) DatabaseUpdateAction.updateStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Iterator keys = table.getChild("keys").getChildren("key");
                Iterator values = table.getChild("values").getChildren("value");

                StringBuffer queryBuffer = new StringBuffer("UPDATE ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" SET ");

                boolean firstIteration = true;

                while (values.hasNext()) {
                    if (firstIteration) {
                        firstIteration = false;
                    } else {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append(((Configuration) values.next()).getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }

                queryBuffer.append(" WHERE ");
                firstIteration = true;

                while (keys.hasNext()) {
                    if (firstIteration) {
                        firstIteration = false;
                    } else {
                        queryBuffer.append(" AND ");
                    }

                    queryBuffer.append(((Configuration) keys.next()).getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }
            }

            DatabaseUpdateAction.updateStatements.put(conf, query);
        }

        return query;
    }
}