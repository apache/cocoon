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

package org.apache.cocoon.components.modules.database;

import java.lang.Integer;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Abstraction layer to encapsulate different DBMS behaviour for autoincrement columns.
 *
 * Here: <a href="http://www.mysql.com">MYSQL</a> AUTO_INCREMENT columns
 *
 * @author <a href="mailto:phantom@stserv.hcf.jhu.edu">Tim Myers</a>
 * @version CVS $Id: MysqlAutoIncrementModule.java,v 1.3 2004/03/05 13:01:54 bdelacretaz Exp $
 */
public class MysqlAutoIncrementModule implements AutoIncrementModule, ThreadSafe {

    public Object getPostValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                                Connection conn, Statement stmt, Map objectModel )  throws SQLException, ConfigurationException {

        Integer id = null;
        /*
          // if mysql did support callable statements ...  i'm not sure what what would go here, maybe:

          CallableStatement callStmt = conn.prepareCall("? = {CALL LAST_INSERT_ID()}");
          callStmt.registerOutParameter(1, Types.INTEGER);
          ResultSet resultSet = callStmt.executeQuery();
        */

        PreparedStatement pstmt = conn.prepareStatement("SELECT LAST_INSERT_ID()");
        ResultSet resultSet = pstmt.executeQuery();
        while ( resultSet.next() ) {
            id = new Integer(resultSet.getInt(1));
        }
        resultSet.close();

        return id;
    }


    public boolean includeInQuery() { return false; }


    public boolean includeAsValue() { return false; }


    public Object getPreValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                               Connection conn, Map objectModel ) throws SQLException, ConfigurationException {

        return null;
    }

    public String getSubquery( Configuration tableConf, Configuration columnConf, Configuration modeConf )
        throws ConfigurationException {

        return null;
    }
}

