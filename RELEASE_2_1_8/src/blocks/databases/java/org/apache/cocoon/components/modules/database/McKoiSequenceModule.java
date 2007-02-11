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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Abstraction layer to encapsulate different DBMS behaviour for autoincrement columns.
 *
 * Here: <a href="http://www.mckoi.org">McKoi</a> sequences. The default
 * sequence name is constructed from the table name, a "_", the column name, and
 * the suffix "_seq". To use a different sequence name, set an attribute
 * "sequence" for the modeConf e.g. &lt;mode name="auto" type="auto"
 * sequence="my_sequence"/&gt;.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: McKoiSequenceModule.java,v 1.3 2004/03/05 13:01:54 bdelacretaz Exp $
 */
public class McKoiSequenceModule implements AutoIncrementModule, ThreadSafe {

    public Object getPostValue(
        Configuration tableConf,
        Configuration columnConf,
        Configuration modeConf,
        Connection conn,
        Statement stmt,
        Map objectModel)
        throws SQLException, ConfigurationException {
        return null;
    }

    public boolean includeInQuery() {
        return true;
    }

    public boolean includeAsValue() {
        return false;
    }

    public Object getPreValue(
        Configuration tableConf,
        Configuration columnConf,
        Configuration modeConf,
        Connection conn,
        Map objectModel)
        throws SQLException, ConfigurationException {

        return null;
    }

    public String getSubquery(
        Configuration tableConf,
        Configuration columnConf,
        Configuration modeConf)
        throws ConfigurationException {

        String sequence = modeConf.getAttribute("sequence", null);
        StringBuffer queryBuffer = new StringBuffer();
        queryBuffer.append(" NEXTVAL('");
        if (sequence != null) {
            queryBuffer.append(sequence);
        } else {
            queryBuffer.append(tableConf.getAttribute("name", ""));
            queryBuffer.append('_');
            queryBuffer.append(columnConf.getAttribute("name"));
            queryBuffer.append("_seq");
        }
        queryBuffer.append("')");

        return queryBuffer.toString();
    }
}
