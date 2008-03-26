/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.databases.bridge.spring.avalon;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Wrapper bean for {@link DataSource} that implements {@link DataSourceComponent} additionally in order
 * to provide access to DataSources for Avalon components.
 *
 */
public class SpringToAvalonDataSourceWrapper implements DataSource, DataSourceComponent {
    
    private DataSource wrappedBean;

    /**
     * @return the wrappedBean
     */
    public DataSource getWrappedBean() {
        return wrappedBean;
    }

    /**
     * @param wrappedBean the wrappedBean to set
     */
    public void setWrappedBean(DataSource wrappedBean) {
        this.wrappedBean = wrappedBean;
    }

    /**
     * @return
     * @throws SQLException
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return wrappedBean.getConnection();
    }

    /**
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException {
        return wrappedBean.getConnection(username, password);
    }

    /**
     * @return
     * @throws SQLException
     * @see javax.sql.CommonDataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        return wrappedBean.getLoginTimeout();
    }

    /**
     * @return
     * @throws SQLException
     * @see javax.sql.CommonDataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        return wrappedBean.getLogWriter();
    }

    /**
     * @param seconds
     * @throws SQLException
     * @see javax.sql.CommonDataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        wrappedBean.setLoginTimeout(seconds);
    }

    /**
     * @param out
     * @throws SQLException
     * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        wrappedBean.setLogWriter(out);
    }
    
    /**
     * @param iface
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    public boolean isWrapperFor(Class iface) throws SQLException {
        throw new UnsupportedOperationException("This operation is not supported because we need to stay compatible " +
                                                "with Java 1.4 where isWrapperFor() is not defined");
    }

    /**
     * @param iface
     * @return
     * @throws SQLException
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    public Object unwrap(Class iface) throws SQLException {
        //I hope that nothing will call this method (GK)
        throw new UnsupportedOperationException("This operation is not supported because we need to stay compatible " +
        		                                "with Java 1.4 where unwrap() is not defined");
    }

    public void configure(Configuration arg0) throws ConfigurationException {
        //do nothing
    }

}
