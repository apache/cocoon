package org.apache.cocoon.processor.sql;

import java.sql.*;
import java.util.*;

/**
 * Utility methods for this processor.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class ConnectionCreator {

    protected String dburl;
    protected String username;
    protected String password;

    public ConnectionCreator(Properties props) {
        this.dburl = props.getProperty("dburl");
        this.username = props.getProperty("username");
        this.password = props.getProperty("password");
    }

    public Connection getConnection() throws SQLException {
        if (username != null && password != null)
            return DriverManager.getConnection(dburl,username,password);
        else
            return DriverManager.getConnection(dburl);
    }

}
