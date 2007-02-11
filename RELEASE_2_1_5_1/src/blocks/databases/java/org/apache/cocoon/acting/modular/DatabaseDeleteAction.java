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
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;

/**
 * Updates a record in a database. The action can update one or more
 * tables, and can update more than one row to a table at a time. 
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseDeleteAction.java,v 1.3 2004/03/05 13:01:52 bdelacretaz Exp $
 */
public class DatabaseDeleteAction extends DatabaseAction {

    /**
     * determine which mode to use as default mode
     * here: DELETE
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
     * Fetch all values for all key columns that are needed to do the
     * database operation.
     */
    protected Object[][] getColumnValues( Configuration tableConf, CacheHelper queryData, Map objectModel )
        throws ConfigurationException, ServiceException {

        Object[][] columnValues = new Object[ queryData.columns.length ][];
        for ( int i = 0; i < queryData.columns.length; i++ ){
            if ( queryData.columns[i].isKey ) {
                columnValues[i] = this.getColumnValue( tableConf, queryData.columns[i], objectModel );
            } else {
                // columnValues[i] = new Object[1]; // this should not be needed
            }
        }
        return columnValues;
    }



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
                Configuration[] keys = table.getChild("keys").getChildren("key");

                queryData = new CacheHelper( keys.length, keys.length );
                fillModes( keys, true, defaultModeNames, modeTypes, queryData );

                StringBuffer queryBuffer = new StringBuffer("DELETE FROM ");
                queryBuffer.append(table.getAttribute("name")).append(" WHERE ");
                for (int i = 0; i < queryData.columns.length; i++) {
                    if ( i > 0 ) {
                        queryBuffer.append(" AND ");
                    }
                    queryBuffer
                        .append( queryData.columns[i].columnConf.getAttribute( "name" ) )
                        .append( "= ?" );
                }

                queryData.queryString = queryBuffer.toString();

                this.cachedQueryData.put( lookUpKey, queryData );
            }
        }

        return queryData;
    }



    /**
     * set all necessary ?s and execute the query
     */
    protected int processRow ( Map objectModel, Connection conn, PreparedStatement statement, String outputMode,
                               Configuration table, CacheHelper queryData, Object[][] columnValues,
                               int rowIndex, Map results )
        throws SQLException, ConfigurationException, Exception {

        int currentIndex = 1;

        // ordering is different for DELETE just needs keys
        for (int i = 0; i < queryData.columns.length; i++) {
            Column col = queryData.columns[i];
            if ( col.isKey ) {
                this.setColumn( objectModel, outputMode, results, table, col.columnConf, rowIndex,
                                columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ], 
                                statement, currentIndex );
                currentIndex++;
            }
        }
        int rowCount = statement.executeUpdate();
        return rowCount;
    }

}
