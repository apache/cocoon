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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.avalon.util.datasource.DataSourceComponent;

/**
 * Delete a record from a database.  This Action assumes that all
 * dependant data is either automatically cleaned up by cascading
 * deletes, or that multiple instances of this action are being used
 * in the correct order.  In other words, it removes one record by
 * the keys.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-03-30 17:14:09 $
 */
public final class DatabaseDeleteAction extends AbstractDatabaseAction {
    private static final Map deleteStatements = new HashMap();

    /**
     * Delete a record from the database.  This action assumes that
     * the file referenced by the "form-descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public final Map act(EntityResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        int currentIndex = 0;

        try {
            Configuration conf = this.getConfiguration(param.getParameter("form-descriptor", null));
            String query = this.getDeleteQuery(conf);
            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request request = (Request) objectModel.get(Constants.REQUEST_OBJECT);

            if (conn.getAutoCommit() == true) {
                conn.setAutoCommit(false);
            }

            PreparedStatement statement = conn.prepareStatement(query);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");

            for (int i = 0; i < keys.length; i++) {
                this.setColumn(statement, i + 1, request, keys[i]);
            }

            statement.execute();
            conn.commit();
            statement.close();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }

            throw new ProcessingException("Could not delete record :position = " + currentIndex, e);
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
    private final String getDeleteQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseDeleteAction.deleteStatements) {
            query = (String) DatabaseDeleteAction.deleteStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Configuration[] keys = table.getChild("keys").getChildren("key");

                StringBuffer queryBuffer = new StringBuffer("DELETE FROM ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" WHERE ");

                for (int i = 0; i < keys.length; i++) {
                    if (i > 0) {
                        queryBuffer.append(" AND ");
                    }

                    queryBuffer.append((keys[i]).getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }

                query = queryBuffer.toString();

                DatabaseDeleteAction.deleteStatements.put(conf, query);
            }
        }

        return query;
    }
}
