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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This is base class for all EsqlQueries
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: AbstractEsqlQuery.java,v 1.5 2003/08/04 13:42:15 haul Exp $
 */
public abstract class AbstractEsqlQuery extends AbstractLogEnabled {
    private int maxRows = -1;
    private int skipRows = 0;
    private int rowCount = -1;
    private int position = -1;
    private String query = null;
    private Connection connection = null;
    private ResultSetMetaData resultSetMetaData = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private boolean hasResultSet = false;
    private boolean keepgoing = true;

    private int queryResultsCount = 0;
    private int updateResultsCount = 0;
    private int updateCount = -2;

    private ArrayList groups = null;
    private int groupLevel = -1;
    private int changeLevel = -1;


    /**
     * Constructor
     *
     * @param connection
     * @param query - The SQL query string
     */
    protected AbstractEsqlQuery(Connection connection, String query) {
        this.connection = connection;
        this.query = query;
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    protected AbstractEsqlQuery(final ResultSet resultSet) {
        this.connection = null;
        this.query = null;
        this.resultSet = resultSet;
        this.hasResultSet = (resultSet != null);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public abstract AbstractEsqlQuery newInstance(final ResultSet resultSet);


    /**
     * Return the query string ("select * from bla")
     *
     * NOTE: Might want to be overridden by indiviual EsqlQuery implementations
     * e.g. for database specific LIMIT features. Be aware that there a two different
     * limit approaches:
     * <pre>
     * retrieve                   query
     * time                       time
     * +---------+                ...........
     * |JDBC     |                :         :
     * |ResultSet|                :         :
     * |.........|-+              :_________:_
     * |         | | skip/max+1   |JDBC     | | skip/max+1
     * |         | | window       |ResultSet| | window
     * |.........| |              |_________| |
     * |         |-+              :         :_|
     * |         |                :         :
     * +---------+                :.........:
     *  </pre>
     * With the "retrieve time" limit the JDBC ResultSet includes ALL of the rows of the
     * query.
     * With the "query time" limit only a small window of rows are in the actuall JDBC
     * ResultSet. In order to know whether there are more rows available (without an additional
     * query) we need to have at least one more row in the JDBC ResultSet. So we ask for getMaxRows()+1
     *
     * @throws SQLException
     */
    public String getQueryString() throws SQLException {
        return (query);
    }

    /**
     * NOTE: Might want to be overridden by indiviual EsqlQuery implementations
     *
     * @throws SQLException
     */
    public PreparedStatement prepareStatement() throws SQLException {
        preparedStatement = connection.prepareStatement(getQueryString());
        return (preparedStatement);
    }

    /**
     * NOTE: Might want to be overridden by indiviual EsqlQuery implementations
     *
     * @throws SQLException
     */
    public CallableStatement prepareCall() throws SQLException {
        preparedStatement = connection.prepareCall(getQueryString());
        return ((CallableStatement) preparedStatement);
    }


    /**
     * Gets the total number of rows of a the query WITHOUT the
     * limits of skip/max rows.
     *
     * NOTE: Might want to be overridden by indiviual EsqlQuery implementations
     *
     * @return total number of rows
     * @throws SQLException
     */
    public int getRowCount() throws SQLException {
        if (rowCount < 0) {
            String lowerQuery = query.toLowerCase();
            int from = lowerQuery.indexOf(" from ");

            int groupby = lowerQuery.indexOf(" group by ");
            int orderby = lowerQuery.indexOf(" order by ");

            int min = Math.min(groupby, orderby);

            String countQuery;
            if (min > -1) {
                countQuery = "select count(*)" + String.valueOf(query).substring(from, min);
            }
            else {
                countQuery = "select count(*)" + String.valueOf(query).substring(from);
            }

            if (getLogger().isDebugEnabled()) getLogger().debug("executing [" + String.valueOf(query) + "]");

            ResultSet rs = preparedStatement.executeQuery(countQuery);
            try {
                if (rs.first()) {
                    rowCount = rs.getInt(1);
                    if (getLogger().isDebugEnabled()) getLogger().debug("count = " + rowCount);
                }
            }
            finally {
                rs.close();
            }
        }

        return (rowCount);
    }

    /**
     * Move to the first row.
     *
     * NOTE: Might want to be overridden by indiviual EsqlQuery implementations
     *
     * @throws SQLException
     */
    public void getResultRows() throws SQLException {
        if (skipRows > 0) {
            while (resultSet.next()) {
                position++;
                if (position >= skipRows) {
                    break;
                }
            }
        }
    }

    /**
     * Clean up all database resources used by the query. In particular,
     * close result sets and statements. 
     *
     */
    public void cleanUp() {
        this.resultSetMetaData = null;
        if (this.resultSet != null){
            try {
                this.resultSet.close();
                this.resultSet = null;
            } catch (SQLException e) {
                // should never happen! (only cause: access error)
            }
        }
        if (this.preparedStatement != null){
            try {
                this.preparedStatement.close();
                this.preparedStatement = null;
            } catch (SQLException e) {
                // should never happen! (only cause: access error)
            }
        }
    }

    /* ************** FINAL methods *********************** */

    protected final void setPosition(int p) {
        position = p;
    }

    protected final PreparedStatement setPreparedStatement(final PreparedStatement ps) {
        preparedStatement = ps;
        return (preparedStatement);
    }

    public final Connection getConnection() {
        return (connection);
    }

    public final int getSkipRows() {
        return (skipRows);
    }

    public final void setSkipRows(int i) {
        skipRows = i;
    }

    public final int getMaxRows() {
        return (maxRows);
    }

    public final void setMaxRows(int i) {
        maxRows = i;
    }

    public final ResultSetMetaData getResultSetMetaData() {
        return (resultSetMetaData);
    }

    public final PreparedStatement getPreparedStatement() {
        return (preparedStatement);
    }

    public final CallableStatement getCallableStatement() {
        return ((CallableStatement) preparedStatement);
    }

    public final ResultSet getResultSet() {
        return (resultSet);
    }

    public final boolean nextRow() throws SQLException {
        position++;
        return (resultSet.next());
    }

    public final int getCurrentRow() {
        return (position);
    }


    public final boolean execute(int resultSetFromObject) throws SQLException {
        if (preparedStatement != null) {
            hasResultSet = preparedStatement.execute();
            if (hasResultSet) {
                resultSet = (ResultSet) ((CallableStatement) preparedStatement).getObject(resultSetFromObject);
                queryResultsCount++;
                return (true);
            }
            else {
                updateResultsCount++;
                updateCount = preparedStatement.getUpdateCount();
                return (updateCount > -1);
            }
        }
        else {
            return (false);
        }
    }

    public final boolean execute() throws SQLException {
        if (preparedStatement != null) {
            hasResultSet = preparedStatement.execute();
            if (hasResultSet) {
                resultSet = preparedStatement.getResultSet();
                resultSetMetaData = resultSet.getMetaData();
                queryResultsCount++;
                return (true);
            }
            else {
                updateResultsCount++;
                updateCount = preparedStatement.getUpdateCount();
                return (updateCount > -1);
            }
        }
        else {
            return (false);
        }
    }

    public final boolean executeQuery() throws SQLException {
        if (preparedStatement != null) {
            resultSet = preparedStatement.executeQuery();
            if (resultSet != null) {
                resultSetMetaData = resultSet.getMetaData();
                queryResultsCount++;
                return (true);
            }
            else {
                return (false);
            }
        }
        else {
            return (false);
        }
    }

    /**
     * Try to get the next ResultSet
     *
     * @return whether there is one or not
     * @throws SQLException
     */
    public final boolean getMoreResults() throws SQLException {
        if (preparedStatement != null) {
            hasResultSet = preparedStatement.getMoreResults();
            if (hasResultSet) {
                resultSetMetaData = resultSet.getMetaData();
                queryResultsCount++;
                return (true);
            }
            else {
                updateResultsCount++;
                updateCount = preparedStatement.getUpdateCount();
                return (updateCount > -1);
            }
        }
        else {
            return (false);
        }
    }


    public final boolean hasResultSet() {
        return (hasResultSet);
    }


    /**
     * Returns the how many rows where updated on last update
     */
    public final int getUpdateCount() {
        return (updateCount);
    }

    /**
     * Returns the number of query results
     */
    public final int getQueryResultsCount() {
        return (queryResultsCount);
    }

    /**
     * Returns the number of update results
     */
    public final int getUpdateResultsCount() {
        return (updateResultsCount);
    }


    public final boolean keepGoing() {
        return (keepgoing);
    }

    public final void setKeepGoing(boolean still) {
        keepgoing = still;
    }

    /* ************************ GROUPING ************************ */

    public final void incGroupLevel() {
        groupLevel++;
    }

    public final void decGroupLevel() {
        groupLevel--;
    }

    public final boolean groupLevelExists() {
        return (groups != null && groups.size() >= groupLevel + 1 && groups.get(groupLevel) != null);
    }

    public final void setGroupingVar(String key) throws SQLException {
        if (groups == null) groups = new ArrayList(groupLevel);
        groups.ensureCapacity(groupLevel);
        groups.add(groupLevel, new EsqlGroup(key, getResultSet().getObject(key))
        );
    }

    public final boolean hasGroupingVarChanged() throws SQLException {
        if (changeLevel != -1) {
            if (changeLevel < groupLevel) {
                return (true);
            }
            else {
                changeLevel = -1;
                return (true);
            }
        }
        else {
            boolean result = false;
            // need to check the complete hierarchy of nested groups for changes
            for (int i = 0; i <= groupLevel; i++) {
                Object tmp = getResultSet().getObject(((EsqlGroup) groups.get(i)).var);
                if (!tmp.equals(((EsqlGroup) groups.get(i)).value)) {
                    ((EsqlGroup) groups.get(i)).value = tmp;
                    result = true;
                    if (changeLevel == -1 && groupLevel != i)
                        changeLevel = i;
                }
            }
            return (result);
        }
    }

    final class EsqlGroup {
        public String var = null;
        public Object value = null;

        EsqlGroup(String var, Object value) {
            this.var = var;
            this.value = value;
        }
    }

}
