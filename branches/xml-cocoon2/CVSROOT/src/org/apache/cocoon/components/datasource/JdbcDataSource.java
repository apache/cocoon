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
import org.apache.avalon.Disposable;
import org.apache.avalon.Loggable;
import org.apache.log.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Default implementation for DataSources in Cocoon.  This uses the
 * normal <code>java.sql.Connection</code> object and
 * <code>java.sql.DriverManager</code>.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-01-22 21:56:34 $
 */
public class JdbcDataSource implements DataSourceComponent, Loggable {
    Logger log;
    JdbcConnectionPool pool = null;

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

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
        if (this.pool == null) {
            String dburl = conf.getChild("dburl").getValue();
            Configuration userConf = conf.getChild("user");
            Configuration passwdConf = conf.getChild("password");
            String user = null;
            String passwd = null;

            if (! userConf.getLocation().equals("-")) {
                user = userConf.getValue();
            }

            if (! passwdConf.getLocation().equals("-")) {
                passwd = passwdConf.getValue();
            }

            Configuration controler = conf.getChild("pool-controller");
            int min = controler.getAttributeAsInt("min", 0);
            int max = controler.getAttributeAsInt("max", 3);

            this.pool = new JdbcConnectionPool(dburl, user, passwd, min, max);
            this.pool.setLogger(this.log);
            this.pool.init();
        }
    }

    /** Get the database connection */
    public Connection getConnection()
    throws SQLException {
        Connection conn = null;

        try {
            conn = (Connection) this.pool.get();
        } catch (Exception e) {
            log.error("Could not return Connection", e);
            throw new SQLException(e.getMessage());
        }

        return conn;
    }

    /** Dispose properly of the pool */
    public void dispose() {
        this.pool.dispose();
        this.pool = null;
    }
}
