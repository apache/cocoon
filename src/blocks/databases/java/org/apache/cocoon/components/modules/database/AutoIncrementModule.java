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
 * @version CVS $Id: AutoIncrementModule.java,v 1.1 2003/03/09 00:03:08 pier Exp $
 * */
public interface AutoIncrementModule extends Component {

    String ROLE = AutoIncrementModule.class.getName();


    /**
     * Return key attribute value of last inserted row.
     *
     * @param name a String that specifies what the caller thinks
     * would identify a set of parameters. This is mainly a fallback
     * if no modeConf is present.

     * @param tableConf Table's configuration from resource description.
     * @param columnConf column's configuration from resource description.
     * @param mdoeConf this mode's configuration from resource description.
     * @param conn Connection
     * @param stmt Statement that was executed to insert the last row.
     * @param request The request object
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
     * @param mdoeConf this mode's configuration from resource description.
     * @param conn Connection
     * @param request The request object
     * @param idx In case of multiple rows to be inserted, index to the desired row
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
