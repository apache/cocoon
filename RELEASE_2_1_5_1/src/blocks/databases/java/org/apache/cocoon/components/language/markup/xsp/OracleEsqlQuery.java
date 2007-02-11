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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: OracleEsqlQuery.java,v 1.5 2004/03/05 13:01:53 bdelacretaz Exp $
 */
final public class OracleEsqlQuery extends AbstractEsqlQuery {

    public OracleEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private OracleEsqlQuery(ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(final ResultSet resultSet) {
        return(new OracleEsqlQuery(resultSet));
    }

    public String getQueryString() throws SQLException {
        if (getSkipRows() > 0) {
            if (getMaxRows() > -1) {
                return (new StringBuffer("select * from (select a.*, rownum rnum from (")
                        .append(super.getQueryString())
                        .append(") a where rownum <= ")
                        .append(getSkipRows() + getMaxRows())
                        .append(") where rnum > ")
                        .append(getSkipRows())
                        .toString());
            }
            else {
		return (new StringBuffer("select * from (select a.*, rownum rnum from (")
                        .append(super.getQueryString())
			.append(") a ")
                        .append(") where rnum > ")
                        .append(getSkipRows())
                        .toString());
	    }
        }
        else {
            if (getMaxRows() > -1) {
                return (new StringBuffer("select * from (select a.*, rownum from (")
                        .append(super.getQueryString())
			.append(") a where rownum <= ")
                        .append(getMaxRows())
                        .append(")").toString());
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
