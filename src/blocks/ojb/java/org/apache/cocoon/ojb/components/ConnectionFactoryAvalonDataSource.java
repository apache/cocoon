/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.ojb.components;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.excalibur.datasource.DataSourceComponent;

import org.apache.ojb.broker.accesslayer.ConnectionFactory;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;

/**
 * OJBConnectionFactory implemenation to bridge into the Avalon DataSource Connection Pooling
 * Component defined in the Cocoon configuration.
 *
 * @author giacomo at apache.org
 * @version $Id$
 */
public class ConnectionFactoryAvalonDataSource
        implements ConnectionFactory {

    /** The <code>ServiceManager</code> to be used */
    private static ServiceManager manager;

    /** The <code>ServiceSelector</code> to be used */
    private static ServiceSelector dbselector;

    /**
     * Initializes this helper class with the <code>ServiceManager</code> to be used.  This method
     * should be called from a Avalon Component configured into Cocoon at startup to supply the
     * needed <code>ServiceManager</code>.
     *
     * @param serviceManager The ServiceManager
     * @throws ServiceException In case we cannot obtain a DataSource
     */
    public static void initialize(final ServiceManager serviceManager)
    throws ServiceException {
        if (ConnectionFactoryAvalonDataSource.manager != null) {
            throw new IllegalStateException("ConnectionFactoryAvalonDataSource is already initialized");
        }

        ConnectionFactoryAvalonDataSource.manager = serviceManager;
        ConnectionFactoryAvalonDataSource.dbselector =
            (ServiceSelector) ConnectionFactoryAvalonDataSource.manager.lookup(DataSourceComponent.ROLE +
                                                                               "Selector");
    }

    /**
     * Signal disposal to this helper class.
     */
    public static void dispose() {
        if (ConnectionFactoryAvalonDataSource.manager != null) {
            ConnectionFactoryAvalonDataSource.manager.release(ConnectionFactoryAvalonDataSource.dbselector);
            ConnectionFactoryAvalonDataSource.dbselector = null;
            ConnectionFactoryAvalonDataSource.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#lookupConnection(org.apache.ojb.broker.metadata.JdbcConnectionDescriptor)
     */
    public Connection lookupConnection(final JdbcConnectionDescriptor conDesc)
    throws LookupException {
        if (ConnectionFactoryAvalonDataSource.manager == null) {
            throw new LookupException("ConnectionFactoryAvalonDataSource is not initialized!");
        }

        try {
            return ((DataSourceComponent) ConnectionFactoryAvalonDataSource.dbselector.select(conDesc.getJcdAlias())).getConnection();
        } catch (final ServiceException e) {
            throw new LookupException("Cannot lookup DataSource named " +
                                      conDesc.getJcdAlias(), e);
        } catch (final SQLException e) {
            throw new LookupException("Cannot get connection from DataSource named " +
                                      conDesc.getDbAlias(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#releaseConnection(org.apache.ojb.broker.metadata.JdbcConnectionDescriptor, java.sql.Connection)
     */
    public void releaseConnection(JdbcConnectionDescriptor conDesc, Connection con) {
        try {
            // The DataSource of this connection will take care of pooling
            con.close();
        } catch (final SQLException e) {
            // This should not happen, but in case
            throw new CascadingRuntimeException("Cannot release SQL Connection to DataSource", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#releaseAllResources()
     */
    public void releaseAllResources() {
        // Nothing to do here
    }
}
