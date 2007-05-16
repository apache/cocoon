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
package org.apache.cocoon.ojb.components;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.ojb.broker.accesslayer.ConnectionFactory;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;

/**
 * OJB ConnectionFactory implemenation to bridge into the Avalon DataSource
 * connection pooling component defined in the Cocoon configuration.
 *
 * <p>This class has two faces to it:
 * <dl>
 * <dt>Avalon Component</dt>
 * <dd>Instance of the class created and managed by Avalon container.
 * When instance is initialized, it looks up datasource components
 * service selector.</dd>
 * <dt>OJB Managed Class</dt>
 * <dd>Instances of the class are created and managed by OJB, as defined
 * in the OJB <code>repository.xml</code> file. Each OJB managed instance
 * of the class will have access to the datasource components service
 * selector initialized by Avalon managed instance of the class.</dd>
 * </dl>
 *
 * It is important that Avalon component is initialized before any access
 * to OJB API is made.</p>
 *
 * @version $Id$
 */
public class ConnectionFactoryImpl implements Component, ThreadSafe, Serviceable, Disposable,
                                              ConnectionFactory {

    // Preloadable to ensure that static attributes are properly setup at startup.

    /** The <code>ServiceManager</code> to be used */
    private static ServiceManager manager;

    /** The <code>ServiceSelector</code> to be used */
    private static ServiceSelector datasources;

    /** The <code>JdbcConnectionDescriptor</code> */
    private JdbcConnectionDescriptor conDesc;

    /**
     * Default constructor
     */
    public ConnectionFactoryImpl() {
    }

    /**
     * OJB 1.1 constructor
     */
    public ConnectionFactoryImpl(JdbcConnectionDescriptor conDesc) {
        this.conDesc = conDesc;
    }

    public void service(ServiceManager manager) throws ServiceException {
        ConnectionFactoryImpl.manager = manager;
        ConnectionFactoryImpl.datasources = (ServiceSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
    }

    public void dispose() {
        if (ConnectionFactoryImpl.manager != null) {
            ConnectionFactoryImpl.manager.release(ConnectionFactoryImpl.datasources);
            ConnectionFactoryImpl.datasources = null;
            ConnectionFactoryImpl.manager = null;
        }
    }

    //
    // OJB 1.1 ConnectionFactory Implementation
    //

    public Connection lookupConnection()
    throws LookupException {
        return lookupConnection(this.conDesc);
    }

    public void releaseConnection(Connection connection) {
        releaseConnection(this.conDesc, connection);
    }

    public int getActiveConnections() {
        return 0;
    }

    public int getIdleConnections() {
        return 0;
    }

    //
    // OJB 1.0 ConnectionFactory Implementation
    //

    public Connection lookupConnection(final JdbcConnectionDescriptor conDesc)
    throws LookupException {
        if (ConnectionFactoryImpl.manager == null) {
            throw new LookupException("ConnectionFactoryImpl is not initialized! Please check your cocoon.xconf");
        }

        try {
            return ((DataSourceComponent) ConnectionFactoryImpl.datasources.select(conDesc.getJcdAlias())).getConnection();
        } catch (final ServiceException e) {
            throw new LookupException("Cannot lookup DataSource " +
                                      conDesc.getJcdAlias(), e);
        } catch (final SQLException e) {
            throw new LookupException("Cannot get connection from DataSource " +
                                      conDesc.getJcdAlias(), e);
        }
    }

    public void releaseConnection(JdbcConnectionDescriptor conDesc, Connection connection) {
        try {
            // The DataSource of this connection will take care of pooling
            connection.close();
        } catch (final SQLException e) {
            // This should not happen, but in case
            throw new CascadingRuntimeException("Cannot release SQL Connection to DataSource", e);
        }
    }

    public void releaseAllResources() {
        // Nothing to do here
    }
}
