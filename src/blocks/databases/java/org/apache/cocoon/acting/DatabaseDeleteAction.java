/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Delete a record from a database.  This Action assumes that all
 * dependant data is either automatically cleaned up by cascading
 * deletes, or that multiple instances of this action are being used
 * in the correct order.  In other words, it removes one record by
 * the keys.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: DatabaseDeleteAction.java,v 1.2 2004/03/05 13:01:50 bdelacretaz Exp $
 */
public final class DatabaseDeleteAction extends AbstractDatabaseAction implements ThreadSafe {
    private static final Map deleteStatements = new HashMap();

    /**
     * Delete a record from the database.  This action assumes that
     * the file referenced by the "descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public final Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        int currentIndex = 0;

        // read global parameter settings
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;
        if (this.settings.containsKey("reloadable"))
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        // read local parameter settings
        try {
            Configuration conf =
                this.getConfiguration(param.getParameter("descriptor", (String) this.settings.get("descriptor")), resolver,
                                      param.getParameterAsBoolean("reloadable",reloadable));

            String query = this.getDeleteQuery(conf);
            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request request = ObjectModelHelper.getRequest(objectModel);

            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }

            PreparedStatement statement = conn.prepareStatement(query);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");

            for (int i = 0; i < keys.length; i++) {
                this.setColumn(statement, i + 1, request, keys[i]);
            }

            int rows = statement.executeUpdate();
            conn.commit();
            statement.close();

            if(rows > 0){
                request.setAttribute("rows", Integer.toString(rows));
                return EMPTY_MAP;
            }
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
