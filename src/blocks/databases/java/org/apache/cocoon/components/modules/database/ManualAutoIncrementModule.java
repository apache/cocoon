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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Abstraction layer to encapsulate different DBMS behaviour for
 * autoincrement columns.
 *
 * Here: manual mode The new value is determined by doing a "select
 * max(column)+1 from table" query. With transactions and correct
 * isolation levels, this should to the trick almost everywhere.
 *
 * Note however, that the above query does not prevent a parallel
 * transaction to try to insert a row with the same ID since it
 * requires only shared locks. C.f. "Phantom Problem"
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: ManualAutoIncrementModule.java,v 1.1 2003/03/09 00:03:09 pier Exp $
 */
public class ManualAutoIncrementModule extends AbstractAutoIncrementModule implements ThreadSafe {

    private Map selectStatements = new HashMap();


    public Object getPostValue( Configuration tableConf, Configuration columnConf, Configuration modenConf,
                                Connection conn, Statement stmt, Map objectModel )
        throws SQLException, ConfigurationException {

        return null;
    }

    public boolean includeInQuery( ) { return true; }


    public boolean includeAsValue( ) { return true; }


    public Object getPreValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                               Connection conn, Map objectModel )
        throws SQLException, ConfigurationException {

        /** Set the key value using SELECT MAX(keyname)+1 **/
        String tableName = tableConf.getAttribute("name","");
        String selectQuery = this.getSelectQuery(tableName, columnConf);
        PreparedStatement select_statement = conn.prepareStatement(selectQuery);
        ResultSet set = select_statement.executeQuery();
        set.next();
        int maxid = set.getInt("maxid");
        set.close();
        select_statement.close();
        if (getLogger().isDebugEnabled())
            getLogger().debug("autoincrementValue " + (maxid+1));
        return new Integer(maxid + 1);
    }


    public String getSubquery( Configuration tableConf, Configuration columnConf, Configuration modeConf )
        throws ConfigurationException {

        return null;
    }


    /**
     * Set the String representation of the MaxID lookup statement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    protected final synchronized void setSelectQuery( String tableName, Configuration entry ) throws ConfigurationException {

        StringBuffer queryBuffer = new StringBuffer("SELECT max(");
        queryBuffer.append(entry.getAttribute("name"));
        queryBuffer.append(") AS maxid FROM ");
        queryBuffer.append(tableName);

        this.selectStatements.put(entry, queryBuffer.toString());
    }


    protected final synchronized String getSelectQuery( String tableName, Configuration entry ) throws ConfigurationException {

        String result = (String) this.selectStatements.get(entry);
        if (result == null) {
            setSelectQuery(tableName, entry);
            result = (String) this.selectStatements.get(entry);
        }
        return result;
    }

}
