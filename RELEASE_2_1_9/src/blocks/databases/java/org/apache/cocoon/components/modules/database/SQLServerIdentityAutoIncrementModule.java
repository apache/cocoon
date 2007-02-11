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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Encapsulate MS SQLServer behaviour for autoincrement columns.
 *
 *
 * @author <a href="mailto:andrzej@chaeron.com">Andrzej Jan Taramina</a>
 * @version CVS $Id: SQLServerIdentityAutoIncrementModule.java,v 1.2 2004/03/05 13:01:54 bdelacretaz Exp $
 */
public class SQLServerIdentityAutoIncrementModule implements AutoIncrementModule, ThreadSafe {

	public Object getPostValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
								Connection conn, Statement stmt, Map objectModel )  throws SQLException, ConfigurationException {

		Integer id = null;
		/*
		  // if SQLServer did support callable statements ...

		  CallableStatement callStmt = conn.prepareCall("? = {select @@IDENTITY}");
		  callStmt.registerOutParameter(1, Types.INTEGER);
		  ResultSet resultSet = callStmt.executeQuery();
		*/

		PreparedStatement pstmt = conn.prepareStatement("select @@IDENTITY");
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
