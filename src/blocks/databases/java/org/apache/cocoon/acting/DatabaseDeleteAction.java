/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: DatabaseDeleteAction.java,v 1.1 2003/03/09 00:03:03 pier Exp $
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
