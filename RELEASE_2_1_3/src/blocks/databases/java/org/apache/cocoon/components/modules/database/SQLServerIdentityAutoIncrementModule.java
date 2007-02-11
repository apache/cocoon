/*

 ============================================================================
				   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
	this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation
	and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
	include  the following  acknowledgment:  "This product includes  software
	developed  by the  Apache Software Foundation  (http://www.apache.org/)."
	Alternately, this  acknowledgment may  appear in the software itself,  if
	and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
	used to  endorse or promote  products derived from  this software without
	prior written permission. For written permission, please contact
	apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
	"Apache" appear  in their name,  without prior written permission  of the
	Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: SQLServerIdentityAutoIncrementModule.java,v 1.1 2003/10/18 18:26:14 haul Exp $
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
