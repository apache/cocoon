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

package org.apache.cocoon.acting.modular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;

/**
 * Executes an arbitrary query. The query is associated with a table
 * and selected through the others mode. All keys and values are set
 * in order of appearance, starting with keys, thus the query needs to
 * have as many placeholders for prepared statement parameters. If it
 * is an update query, the number of affected rows is returned to the
 * sitemap.
 *
 *<pre>
 * &lt;table name="example"&gt;
 *   &lt;queries&gt;
 *      &lt;query mode="one"&gt;update example set count=count+1 where id=?&lt;/query&gt;
 *      &lt;query mode="two"&gt;select count, name from example where id=?&lt;/query&gt;
 *   &lt;/queries&gt;
 *   &lt;keys&gt;
 *     &lt;key name="id"/&gt;
 *   &lt;/keys&gt;
 *   &lt;values/&gt;
 * &lt;/table&gt;
 *</pre>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseQueryAction.java,v 1.4 2004/03/05 13:01:52 bdelacretaz Exp $
 */
public class DatabaseQueryAction extends DatabaseAction {

    /**
     * determine which mode to use as default mode
     * here: SELECT
     * highly specific to operation INSERT / UPDATE / DELETE / SELECT
     */
    protected String selectMode ( boolean isAutoIncrement, Map modes ) {
        
        return (String) modes.get( MODE_OTHERS );
    }

    /**
     * determine whether autoincrement columns should be honoured by
     * this operation. This is usually snsible only for INSERTs.
     */
    protected boolean honourAutoIncrement() { return false; }


    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     *
     * @param table the table's configuration object
     * @return the insert query as a string
     */
    protected CacheHelper getQuery( Configuration table, Map modeTypes, Map defaultModeNames )
        throws ConfigurationException, ServiceException {

        LookUpKey lookUpKey = new LookUpKey( table, modeTypes );
        CacheHelper queryData = null;
        synchronized( this.cachedQueryData ) {
            queryData = (CacheHelper) this.cachedQueryData.get( lookUpKey );
            if (queryData == null) {
                Configuration[] queries = table.getChild("queries").getChildren("query");
                Configuration[] keys = table.getChild("keys").getChildren("key");
                Configuration[] values = table.getChild("values").getChildren("value");

                boolean found = false;
                String queryModeName = "";
                String query = "";
                boolean useValues = true;
                for (int i=0; i<queries.length; i++) {
                    queryModeName = queries[i].getAttribute("mode",null);
                    if ( queryModeName.equals(modeTypes.get(MODE_OTHERS)) || "all".equals(queryModeName)) {
                        query = queries[i].getValue();
                        useValues = queries[i].getAttributeAsBoolean("use-values", useValues);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new ConfigurationException("Could not find query mode " +
                                                     modeTypes.get(MODE_OTHERS) + 
                                                     " for table " + table.getAttribute("name",null));
                }

                

                queryData = new CacheHelper( keys.length, keys.length + (useValues ? values.length : 0));
                queryData.queryString = query;
                fillModes( keys  , true , defaultModeNames, modeTypes, queryData );
                if (useValues) fillModes( values, false, defaultModeNames, modeTypes, queryData );
                
                this.cachedQueryData.put( lookUpKey, queryData );
            }
        }

        return queryData;
    }


    /**
     * Fetch all values for all columns that are needed to do the database operation.
     */
    protected Object[][] getColumnValues( Configuration tableConf, CacheHelper queryData, Map objectModel )
        throws ConfigurationException, ServiceException {

        Object[][] columnValues = new Object[ queryData.columns.length ][];
        for ( int i = 0; i < queryData.columns.length; i++ ){
            columnValues[i] = this.getColumnValue( tableConf, queryData.columns[i], objectModel );
        }
        return columnValues;
    }


    /**
     * set all necessary ?s and execute the query
     */
    protected int processRow ( Map objectModel, Connection conn, PreparedStatement statement, String outputMode,
                               Configuration table, CacheHelper queryData, Object[][] columnValues, 
                               int rowIndex, Map results )
        throws SQLException, ConfigurationException, Exception {

        int currentIndex = 1;

        // ordering is different for SELECT just needs keys
        for (int i = 0; i < queryData.columns.length; i++) {
            Column col = queryData.columns[i];
            if ( col.isKey ) {
                this.setColumn(objectModel, outputMode, results, table, col.columnConf, rowIndex,
                               columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ], statement, currentIndex );
                currentIndex++;
            }
        }
        boolean hasResult = statement.execute();
        if (!hasResult) {
            return statement.getUpdateCount();
        } else {
            // retrieve values
            ResultSet resultset = statement.getResultSet();
            ResultSetMetaData metadata = resultset.getMetaData();
            rowIndex = 0;
            while ( resultset.next() ){
                //if ( ! ( rowIndex == -1 && resultset.isLast() ) ) {
                rowIndex++;
                //}
                String tableName = "";
                String columnName = "";
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    Object value = resultset.getObject(i);
                    tableName = metadata.getTableName(i);
                    columnName = metadata.getColumnLabel(i) + "["+rowIndex+"]";
                    if (tableName != "") columnName = tableName + "." + columnName;
                    if (this.getLogger().isDebugEnabled())
                        this.getLogger().debug("retrieving "+columnName+" as "+value);
                    results.put(metadata.getTableName(i)+"."+metadata.getColumnLabel(i),value);
                }
            }
            return rowIndex;
        }
    }


}
