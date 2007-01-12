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
package org.apache.cocoon.databases.ibatis;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.cocoon.components.CocoonComponentManager;

import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;

/**
 * This is an implementation if iBatis DataSourceFactory allowing to use
 * a reference to an excalibur data source in the iBatis configuration.
 * The configuration in the iBatis sqlMapConfig looks like this:
 * &lt;dataSource type="org.apache.cocoon.databases.ibatis.ExcaliburDataSourceFactory"&gt;
 *   &lt;property name="connection" value="Name of the Excalibur data source from the cocoon.xconf"/&gt;
 * &lt;/dataSource&gt;
 *
 * @version $Id$
 * @since 2.1.10
 */
public class ExcaliburDataSourceFactory implements DataSourceFactory {

    protected DataSourceComponent datasource;

    public DataSource getDataSource() {
        return new DataSourceWrapper(this.datasource);
    }

    /**
     * @see com.ibatis.sqlmap.engine.datasource.DataSourceFactory#initialize(java.util.Map)
     */
    public void initialize(Map values) {
        final String connection = (String)values.get("connection");
        if ( connection == null ) {
            throw new RuntimeException("Connection configuration is missing for " + this.getClass().getName() + "." +
                    " Have a look at the iBatis sqlMapConfig and check the 'connection' property for the data source.");
        }
        // get the component manager
        final ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();
        if ( manager == null ) {
            throw new RuntimeException("CocoonComponentManager is not available for " + this.getClass().getName() + "." +
            " Make sure that you're initializing iBatis during an active request and not on startup.");            
        }
        try {
            final ComponentSelector selector = (ComponentSelector)manager.lookup(DataSourceComponent.ROLE + "Selector");
            try {
                this.datasource = (DataSourceComponent)selector.select(connection);
            } catch (ComponentException e) {
                throw new CascadingRuntimeException("Unable to lookup data source with name " + connection + "." +
                        " Check the cocoon.xconf and the iBatis sqlMapConfig.", e);                
            }
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup datasource selector.", e);
        }
    }

    protected static final class DataSourceWrapper implements DataSource {

        protected final DataSourceComponent datasource;
        protected PrintWriter writer = new PrintWriter(System.out);
        protected int timeout = 0;

        public DataSourceWrapper(DataSourceComponent d) {
            this.datasource = d;
        }

        public Connection getConnection() throws SQLException {
            return this.datasource.getConnection();
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        public int getLoginTimeout() throws SQLException {
            return this.timeout;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return this.writer;
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            this.timeout = seconds;
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            this.writer = out;
        }       

        /**
         * Required by JDK1.6.
         */
        public Object unwrap(Class iface) throws SQLException {
            return null;
        }

        /**
         * Required by JDK1.6.
         */
        public boolean isWrapperFor(Class iface) throws SQLException {
            return false;
        }
    }
}
