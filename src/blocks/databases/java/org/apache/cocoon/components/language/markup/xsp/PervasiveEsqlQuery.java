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
 * Database specific EsqlQuery
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: PervasiveEsqlQuery.java,v 1.5 2004/03/05 13:01:53 bdelacretaz Exp $
 */
final public class PervasiveEsqlQuery extends AbstractEsqlQuery {

    public PervasiveEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private PervasiveEsqlQuery(final ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(final ResultSet resultSet) {
        return(new PervasiveEsqlQuery(resultSet));
    }

    public PreparedStatement prepareStatement() throws SQLException {
        return (
                setPreparedStatement(
                        getConnection().prepareStatement(
                                getQueryString()
                                )
                ));
    }

    public CallableStatement prepareCall() throws SQLException {
        return (
                (CallableStatement) setPreparedStatement(
                        getConnection().prepareCall(
                                getQueryString()
                        )
                )
                );
    }

}
