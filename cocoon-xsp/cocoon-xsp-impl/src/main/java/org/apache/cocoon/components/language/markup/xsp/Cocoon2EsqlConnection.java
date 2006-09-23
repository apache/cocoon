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

import org.apache.avalon.excalibur.datasource.DataSourceComponent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 * This is the Cocoon2 specific part of an AbstractEsqlConnection.
 *
 * @version $Id$
 */

final public class Cocoon2EsqlConnection extends AbstractEsqlConnection {
    final private DataSourceComponent datasource;
    private Connection connection = null;

    public Cocoon2EsqlConnection() {
        this.datasource = null;
        this.connection = null;
    }

    /**
     * Someone passed the connection
     * @param connection
     */
    public Cocoon2EsqlConnection( Connection connection ) {
        this.datasource = null;
        this.connection = connection;
    }

    /**
     * Get the connection from the pool
     * @param datasource
     */
    public Cocoon2EsqlConnection( DataSourceComponent datasource ) {
        this.datasource = datasource;
    }

    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return(connection);
        }
        else {
            if (datasource != null) {
                // get the connection from the pool
                connection = datasource.getConnection();
            }
            else {
                // open a new connection
                connection = DriverManager.getConnection(getURL(), getProperties());
            }
            if (connection != null) {
                return(connection);
            }
            else {
                throw new SQLException("Could not obtain connection");
            }
        }
    }
}
