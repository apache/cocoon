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
package org.apache.cocoon.components.cron;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.quartz.utils.ConnectionProvider;

/**
 * Quartz database connection provider that uses the 
 * Excalibur DataSourceComponent service.
 */
public class DataSourceComponentConnectionProvider implements ConnectionProvider {

    private ServiceManager m_manager;
    private DataSourceComponent m_ds;

    public DataSourceComponentConnectionProvider(String dsName, ServiceManager manager) throws ConfigurationException {
        m_manager = manager;
        try {
            m_manager.lookup(DataSourceComponent.ROLE + "/" + dsName);            
        }
        catch (ServiceException e) {
            throw new ConfigurationException("No datasource available by that name: " + dsName);
        }
    }

    /*
     * @see org.quartz.utils.ConnectionProvider#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return m_ds.getConnection();
    }

    /*
     * @see org.quartz.utils.ConnectionProvider#shutdown()
     */
    public void shutdown() throws SQLException {
        if (m_ds != null) {
            m_manager.release(m_ds);
        }
        m_ds = null;
        m_manager = null;
    }

}
