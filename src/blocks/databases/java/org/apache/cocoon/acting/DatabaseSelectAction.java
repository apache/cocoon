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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Select a record from a database. If request parameters are present,
 * their values are used to populate request attributes. Otherwise,
 * values from database are used.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: DatabaseSelectAction.java,v 1.2 2004/03/05 13:01:50 bdelacretaz Exp $
 */
public class DatabaseSelectAction extends AbstractDatabaseAction implements ThreadSafe {

    private static final Map selectStatements = new HashMap();

    /**
     * Select a record from the database.  This action assumes that
     * the file referenced by the "descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
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
                this.getConfiguration(param.getParameter("descriptor", (String) this.settings.get("descriptor")),
                                      resolver,
                                      param.getParameterAsBoolean("reloadable",reloadable));

            Request request = ObjectModelHelper.getRequest(objectModel);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");
            Configuration[] values = conf.getChild("table").getChild("values").getChildren("value");

            PreparedStatement statement = null;
            ResultSet rset = null;
            boolean result = false;

            for (int i = 0; i < keys.length; i++) {
                final String parameter = keys[i].getAttribute("param");
                Object value = request.getParameter(parameter);
                if (value == null || "".equals(value)) {
                    if (statement == null) {
                        final String query = this.getSelectQuery(conf);
                        datasource = this.getDataSource(conf);
                        conn = datasource.getConnection();

                        statement = conn.prepareStatement(query);
                        currentIndex = 1;
                        for (int j = 0; j < keys.length; j++, currentIndex++) {
                            this.setColumn(statement, currentIndex, request, keys[j]);
                        }

                        rset = statement.executeQuery();
                        result = rset.next();
                    }

                    if (result)
                        value = this.getColumn(rset, request, keys[i]);
                }

                if (value != null)
                    request.setAttribute(parameter, value.toString());
            }

            for (int i = 0; i < values.length; i++) {
                final String parameter = values[i].getAttribute("param");
                Object value = request.getParameter(parameter);
                if (value == null || "".equals(value)) {
                    if (statement == null) {
                        final String query = this.getSelectQuery(conf);
                        datasource = this.getDataSource(conf);
                        conn = datasource.getConnection();

                        statement = conn.prepareStatement(query);
                        currentIndex = 1;
                        for (int j = 0; j < keys.length; j++, currentIndex++) {
                            this.setColumn(statement, currentIndex, request, keys[j]);
                        }

                        rset = statement.executeQuery();
                        result = rset.next();
                    }

                    if (result)
                        value = this.getColumn(rset, request, values[i]);
                }

                if (value != null)
                    request.setAttribute(parameter, value.toString());
            }

            if(statement != null)
                statement.close();

            return EMPTY_MAP;
        } catch (Exception e) {
            throw new ProcessingException("Could not prepare statement :position = " + currentIndex, e);
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

        // Result is empty map or exception. No null.
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    protected String getSelectQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseSelectAction.selectStatements) {
            query = (String) DatabaseSelectAction.selectStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Configuration[] keys = table.getChild("keys").getChildren("key");
                Configuration[] values = table.getChild("values").getChildren("value");

                StringBuffer queryBuffer = new StringBuffer("SELECT ");
                int index = 0;
                for (int i = 0; i < keys.length; i++, index++) {
                    if (index > 0) {
                        queryBuffer.append(", ");
                    }
                    queryBuffer.append(keys[i].getAttribute("dbcol"));
                }
                for (int i = 0; i < values.length; i++,index++) {
                    if (index > 0) {
                        queryBuffer.append(", ");
                    }
                    queryBuffer.append(values[i].getAttribute("dbcol"));
                }

                queryBuffer.append(" FROM ");
                queryBuffer.append(table.getAttribute("name"));

                queryBuffer.append(" WHERE ");
                for (int i = 0; i < keys.length; i++) {
                    if (i > 0) {
                        queryBuffer.append(" AND ");
                    }

                    queryBuffer.append(keys[i].getAttribute("dbcol"));
                    queryBuffer.append(" = ?");
                }

                query = queryBuffer.toString();

                DatabaseSelectAction.selectStatements.put(conf, query);
            }
        }

        return query;
    }
}
