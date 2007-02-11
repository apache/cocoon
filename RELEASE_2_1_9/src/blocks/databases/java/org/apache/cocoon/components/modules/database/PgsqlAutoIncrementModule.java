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
 * Here: <a href="http://www.postgres.org">PostgreSQL</a>
 * sequences. The default sequence name is constructed from the table
 * name, a "_", the column name, and the suffix "_seq". To use a
 * different sequence name, set an attribute "sequence" for the
 * modeConf e.g. &lt;mode name="auto" type="auto" sequence="my_sequence"/&gt;.
 *
 * @author <a href="mailto:pmhahn@titan.lahn.de">Philipp Hahn</a>
 * @version CVS $Id: PgsqlAutoIncrementModule.java,v 1.4 2004/03/05 13:01:54 bdelacretaz Exp $
 */
public class PgsqlAutoIncrementModule implements AutoIncrementModule, ThreadSafe {

    public Object getPostValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                                Connection conn, Statement stmt, Map objectModel )  throws SQLException, ConfigurationException {

        Integer id = null;
        /*
          // Postgres does support callable statements ...  i'm not sure what what would go here, maybe:

          CallableStatement callStmt = conn.prepareCall("? = {CALL LAST_INSERT_ID()}");
          callStmt.registerOutParameter(1, Types.INTEGER);
          ResultSet resultSet = callStmt.executeQuery();
        */

        String sequence = modeConf.getAttribute("sequence",null);

        StringBuffer queryBuffer = new StringBuffer("SELECT currval('");
        if (sequence != null) {
            queryBuffer.append(sequence);
        } else {
            queryBuffer.append(tableConf.getAttribute("name",""));
            queryBuffer.append('_');
            queryBuffer.append(columnConf.getAttribute("name"));
            queryBuffer.append("_seq");
        }
        queryBuffer.append("')");
        
        PreparedStatement pstmt = conn.prepareStatement(queryBuffer.toString());
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
