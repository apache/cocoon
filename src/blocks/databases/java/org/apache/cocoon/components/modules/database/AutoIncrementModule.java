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
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Abstraction layer to encapsulate different DBMS behaviour for key
 * attribute columns.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AutoIncrementModule.java,v 1.4 2004/03/05 13:01:54 bdelacretaz Exp $
 * */
public interface AutoIncrementModule extends Component {

    String ROLE = AutoIncrementModule.class.getName();


    /**
     * Return key attribute value of last inserted row.
     *
     * @param tableConf Table's configuration from resource description.
     * @param columnConf column's configuration from resource description.
     * @param modeConf this mode's configuration from resource description.
     * @param conn Connection
     * @param stmt Statement that was executed to insert the last row.
     * @param objectModel The objectModel
     * @return value representing the last key value value.
     * */
    Object getPostValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                         Connection conn, Statement stmt, Map objectModel ) throws SQLException, ConfigurationException;


    /**
     * Boolean whether the key attribute column needs to be included
     * in the insert query.
     *
     * @return true if the column is needed, false if the column
     * should be skipped.
     * */
    boolean includeInQuery( );


    /**
     * Boolean whether the key attribute needs to be included in the
     * insert query as an attribute value (no subquery).
     *
     * @return true if a value is needed, false if a subquery
     * expression is used or the column is skipped altogether.
     * */
    boolean includeAsValue( );


    /**
     * Provide the value for the key attribute column.
     *
     * If a value for the key value column is needed (i.e. the column
     * is not skipped), this value is computed here.
     *
     * @param tableConf Table's configuration from resource description.
     * @param columnConf column's configuration from resource description.
     * @param modeConf this mode's configuration from resource description.
     * @param conn Connection
     * @param objectModel The objectModel
     * @return exact value for key attribute column
     * */
    Object getPreValue( Configuration tableConf, Configuration columnConf, Configuration modeConf,
                        Connection conn, Map objectModel ) throws SQLException, ConfigurationException;


    /**
     * Provide subquery string for the key attribute column.
     *
     * If a value for the autoincrement column is needed (i.e. the
     * column is not skipped), and the value can be determined through
     * a nested subquery, this function provides the subquery as a
     * string.
     *
     * @return subquery string for autoincrement column.
     */
    String getSubquery( Configuration tableConf, Configuration columnConf, Configuration modeConf ) throws ConfigurationException;

}
