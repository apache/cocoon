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

package org.apache.cocoon.components.language.markup.xsp;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * This EsqlQuery only uses the standard JDBC API approaches.
 * Please note that whether this is good, ok or bad depends
 * on the driver implementation of your database vendor.
 * It should work with all JDBC compliant databases.
 * Unfortunately it seems NOT to work with mssql
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: JdbcEsqlQuery.java,v 1.6 2004/03/05 13:01:53 bdelacretaz Exp $
 */
final public class JdbcEsqlQuery extends AbstractEsqlQuery {

    public JdbcEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private JdbcEsqlQuery(final ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(final ResultSet resultSet) {
        return(new JdbcEsqlQuery(resultSet));
    }

    public PreparedStatement prepareStatement() throws SQLException {
        return (
                setPreparedStatement(
                        getConnection().prepareStatement(
                                getQueryString(),
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY)
                ));
    }

    public CallableStatement prepareCall() throws SQLException {
        return (
                (CallableStatement) setPreparedStatement(
                        getConnection().prepareCall(
                                getQueryString(),
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY
                        )
                )
                );
    }

    /**
     * AFAIK this is the proposed JDBC API way to get the number
     * of results. Unfortunately -at least some- driver implementation
     * are transfering the complete resultset when moving to the end.
     * Which is totally stupid for limit/paging purposes. So we probably
     * better stick with an additional count query from the AbstractEsqlQuery
     */

    public int getRowCount() throws SQLException {
        ResultSet rs = getResultSet();
        synchronized (rs) {
            int currentRow = rs.getRow();
            rs.last();
            int count = rs.getRow();
            if (currentRow > 0) {
                rs.absolute(currentRow);
            }
            else {
                rs.first();
                rs.relative(-1);
            }
            return (count);
        }
    }

    public void getResultRows() throws SQLException {
        final int skip = getSkipRows();
        if (skip > 0) {
            getResultSet().absolute(skip);
        }
        setPosition(skip);
    }

}
