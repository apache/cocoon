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
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
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
 * Delete a record from a database.  This Action assumes that all
 * dependant data is either automatically cleaned up by cascading
 * deletes, or that multiple instances of this action are being used
 * in the correct order.  In other words, it removes one record by
 * the keys.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-02-23 22:34:27 $
 */
public class DatabaseDeleteAction extends ComposerAction implements Configurable {
    private ComponentSelector dbselector;
    private String database;

    /**
     * Configure the <code>Action</code> so that we can use the same database
     * for all instances.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        Configuration connElement = conf.getChild("use-connection");

        try {
            this.dbselector = (ComponentSelector) this.manager.lookup(Roles.DB_CONNECTION);
            this.database = connElement.getValue();
        } catch (ComponentManagerException cme) {
            getLogger().error("Could not get the DataSourceComponentSelector", cme);
            throw new ConfigurationException("Could not get the DataSource ComponentSelector", cme);
        }
    }

    /**
     * Delete a record from the database.  This action assumes that
     * the parameter names are the request parameter names and the parameter
     * values are the database column names.  The two notable exceptions
     * are:
     *
     * <ul>
     *   <li>
     *     If a parameter name starts with "key:", then it is treated
     *     as a SQL table key--and the prefix "key:" is stripped from
     *     the name.
     *   </li>
     *   <li>
     *     If a parameter name is "table", then it is the Database table
     *     we are modifying.
     *   </li>
     * </ul>
     */
    public Map act(EntityResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;

        try {
            datasource = (DataSourceComponent) this.dbselector.select(this.database);
            conn = datasource.getConnection();
            HttpRequest request = (HttpRequest) objectModel.get(Constants.REQUEST_OBJECT);

            Iterator lookup = param.getParameterNames();
            ArrayList keys = new ArrayList();
            ArrayList keyNames = new ArrayList();
            ArrayList values = new ArrayList();

            while (lookup.hasNext()) {
                String test = (String) lookup.next();

                if (test.startsWith("key:")) {
                    keys.add(param.getParameter(test, null));
                    keyNames.add(test.substring("key:".length()));
                } else if ("table".equals(test) == false) {
                    values.add(param.getParameter(test, null));
                }
            }

            StringBuffer query = new StringBuffer("DELETE FROM ");
            query.append(param.getParameter("table", null));
            query.append(" WHERE ");

            boolean firstIteration = true;
            Iterator keyIterator = keys.iterator();

            while (keyIterator.hasNext()) {
                if (firstIteration) {
                    firstIteration = false;
                } else {
                    query.append(" AND ");
                }

                query.append((String) keyIterator.next());
                query.append(" = ?");
            }

            PreparedStatement statement = conn.prepareStatement(query.toString());

            keyIterator = keyNames.iterator();
            int i = 1;

            while (keyIterator.hasNext()) {
                statement.setString(i, request.getParameter((String) keyIterator.next()));
                i++;
            }

            statement.execute();
        } catch (Exception e) {
            throw new ProcessingException("Could not delete record", e);
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
}