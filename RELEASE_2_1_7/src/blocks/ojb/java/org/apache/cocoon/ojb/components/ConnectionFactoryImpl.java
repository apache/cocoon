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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * OJB ConnectionFactory implemenation to bridge into the Avalon DataSource
 * connection pooling component defined in the Cocoon configuration.
 *
 * @author giacomo at apache.org
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class ConnectionFactoryImpl implements Component, ThreadSafe, Serviceable, Disposable,
                                              ConnectionFactory {

    /** The <code>ServiceManager</code> to be used */
    private static ServiceManager manager;

    /** The <code>ServiceSelector</code> to be used */
    private static ServiceSelector datasources;

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

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#lookupConnection(org.apache.ojb.broker.metadata.JdbcConnectionDescriptor)
     */
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
