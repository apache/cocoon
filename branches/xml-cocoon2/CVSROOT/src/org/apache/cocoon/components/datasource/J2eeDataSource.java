/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.datasource;

import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.log.LogKit;
import org.apache.log.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The J2EE implementation for DataSources in Cocoon.  This uses the
 * <code>javax.sql.DataSource</code> object and assumes that the
 * J2EE container pools the datasources properly.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-01-10 22:07:03 $
 */
public class J2eeDataSource implements DataSourceComponent {
    final static String JDBC_NAME = "java:comp/env/jdbc/";
    Logger log = LogKit.getLoggerFor("cocoon");
    DataSource ds = null;

    /**
     *  Configure and set up DB connection.  Here we set the connection
     *  information needed to create the Connection objects.  It must
     *  be called only once.
     *
     * @param conf The Configuration object needed to describe the
     *             connection.
     *
     * @throws ConfigurationException
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        if (this.ds == null) {
            try {
                Context initContext = new InitialContext();

                ds = (DataSource) initContext.lookup("java:comp/env/jdbc/" + conf.getChild("dbname").getValue());
            } catch (NamingException ne) {
                log.error("Problem with JNDI lookup of datasource", ne);
                throw new ConfigurationException("Could not use JNDI to find datasource", ne);
            }
        }
    }

    /** Get the database connection */
    public Connection getConnection()
    throws SQLException {

        if (ds == null) {
            throw new SQLException("Can not access DataSource object");
        }

        return ds.getConnection();
    }
}
