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

package org.apache.cocoon.acting.modular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Updates a record in a database. The action can update one or more
 * tables, and can update more than one row to a table at a time.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseUpdateAction.java,v 1.2 2003/07/01 11:23:19 haul Exp $
 */
public class DatabaseUpdateAction extends DatabaseAction {

    /**
     * determine which mode to use as default mode
     * here: UPDATE
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
     * Fetch all values for all columns that are needed to do the
     * database operation.
     */
    protected Object[][] getColumnValues( Configuration tableConf, CacheHelper queryData, Map objectModel )
        throws ConfigurationException, ComponentException {

        Object[][] columnValues = new Object[ queryData.columns.length ][];
        for ( int i = 0; i < queryData.columns.length; i++ ){
            columnValues[i] = this.getColumnValue( tableConf, queryData.columns[i], objectModel );
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
        throws ConfigurationException, ComponentException {

        LookUpKey lookUpKey = new LookUpKey( table, modeTypes );
        CacheHelper queryData = null;
        synchronized( this.cachedQueryData ) {
            queryData = (CacheHelper) this.cachedQueryData.get( lookUpKey );
            if (queryData == null) {
                Configuration[] keys = table.getChild("keys").getChildren("key");
                Configuration[] values = table.getChild("values").getChildren("value");

                queryData = new CacheHelper( keys.length, keys.length + values.length );
                fillModes( keys,   true,  defaultModeNames, modeTypes, queryData );
                fillModes( values, false, defaultModeNames, modeTypes, queryData );

                StringBuffer queryBuffer = new StringBuffer("UPDATE ");
                queryBuffer.append(table.getAttribute("name"));

                if (values.length > 0){
                    queryBuffer.append(" SET ");
                    int cols = 0;
                    for (int i = 0; i < queryData.columns.length; i++) {
                        if ( !queryData.columns[i].isKey ) {
                            if ( cols > 0 ) {
                                queryBuffer.append(", ");
                            }   
                            cols++;
                            queryBuffer
                                .append( queryData.columns[i].columnConf.getAttribute( "name" ) )
                                .append( "= ?" );
                        }
                    }
                }
                
                queryBuffer.append(" WHERE ");
                for (int i = 0; i < queryData.columns.length; i++) {
                    if ( queryData.columns[i].isKey ) {
                        if ( i > 0 ) {
                            queryBuffer.append(" AND ");
                        }
                        queryBuffer
                            .append( queryData.columns[i].columnConf.getAttribute( "name" ) )
                            .append( "= ?" );
                    }
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

        // ordering is different for UPDATE than for INSERT: values, keys
        for (int i = 0; i < queryData.columns.length; i++) {
            Column col = queryData.columns[i];
            if ( !col.isKey ) {
                this.setColumn( objectModel, outputMode, results, table, col.columnConf, rowIndex,
                                columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ], statement, currentIndex );
                currentIndex++;
            }
        }
        for (int i = 0; i < queryData.columns.length; i++) {
            Column col = queryData.columns[i];
            if ( col.isKey ) {
                this.setColumn( objectModel, outputMode, results, table, col.columnConf, rowIndex,
                                columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ], statement, currentIndex );
                currentIndex++;
            }
        }
        int rowCount = statement.executeUpdate();
        return rowCount;
    }

}
