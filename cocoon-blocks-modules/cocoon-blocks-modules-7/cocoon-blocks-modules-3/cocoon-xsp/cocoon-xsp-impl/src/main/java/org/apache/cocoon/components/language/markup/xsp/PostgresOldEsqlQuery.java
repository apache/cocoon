/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * @version $Id$
 */
final public class PostgresOldEsqlQuery extends AbstractEsqlQuery {

    public PostgresOldEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private PostgresOldEsqlQuery(ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(final ResultSet resultSet) {
        return(new PostgresOldEsqlQuery(resultSet));
    }

    public String getQueryString() throws SQLException {
        if (getSkipRows() > 0) {
            if (getMaxRows() > -1) {
                return (new StringBuffer(super.getQueryString())
                        .append(" LIMIT ").append(getMaxRows()+1)
                        .append(",").append(getSkipRows())
                        .toString());
            }
            else {
                return (new StringBuffer(super.getQueryString())
                        .append(" OFFSET ").append(getSkipRows())
                        .toString());
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
