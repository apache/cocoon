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

import org.apache.cocoon.components.language.markup.xsp.AbstractEsqlQuery;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: MysqlEsqlQuery.java,v 1.5 2004/03/05 13:01:53 bdelacretaz Exp $
 */
final public class MysqlEsqlQuery extends AbstractEsqlQuery {

    public MysqlEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private MysqlEsqlQuery(final ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(ResultSet resultSet) {
        return( new MysqlEsqlQuery(resultSet) );
    }

    public String getQueryString() throws SQLException {
        if (getSkipRows() > 0) {
            if (getMaxRows() > -1) {
                return (new StringBuffer(super.getQueryString())
                        .append(" LIMIT ").append(getSkipRows())
                        .append(",").append(getMaxRows()+1)
                        .toString());
            }
            else {
                throw new SQLException("MySQL does not support a skip of rows only. Please also provide the max amount of rows");
            }
        }
        else {
            if (getMaxRows() > -1) {
                return (new StringBuffer(super.getQueryString())
                        .append(" LIMIT ").append(getMaxRows()+1)
                        .toString());
            }
            else {
                return (super.getQueryString());
            }
        }
    }

    public void getResultRows() throws SQLException {
        setPosition(getSkipRows());
    }
}
