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
import org.apache.cocoon.util.ClassUtils;
import org.apache.log.LogKit;
import org.apache.log.Logger;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * The Default implementation for DataSources in Cocoon.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-01-05 23:08:34 $
 */
public class JdbcDataSource implements DataSourceComponent {
    String driver;
    String dburl;
    String user;
    String passwd;
    Logger log = LogKit.getLoggerFor("cocoon");
    Connection dbConnection = null;

    /** Configure and set up DB connection */
    public void configure(Configuration conf)
    throws ConfigurationException {
        this.driver = conf.getChild("driver").getValue();
        this.dburl = conf.getChild("dburl").getValue();
        this.user = conf.getChild("user").getValue();
        this.passwd = conf.getChild("password").getValue();

        try {
            ClassUtils.newInstance(driver);
        } catch (Exception e) {
            log.error("Could not load Database Driver", e);
            throw new ConfigurationException("Could not load Database Driver",e);
        }

        try {
	        if (user.equals("")) {
	            this.dbConnection = DriverManager.getConnection(dburl);
	        } else {
	            this.dbConnection = DriverManager.getConnection(dburl, user, passwd);
	        }
        } catch (Exception e) {
            log.error("Could not connect to Database", e);
            throw new ConfigurationException("Could not connect to Database", e);
        }
    }

    /** Get the database connection */
    public Connection getConnection() {
        return this.dbConnection;
    }
}
