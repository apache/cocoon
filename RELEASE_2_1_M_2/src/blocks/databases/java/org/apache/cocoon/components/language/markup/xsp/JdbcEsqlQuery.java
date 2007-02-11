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

package org.apache.cocoon.components.language.markup.xsp;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * This EsqlQuery only uses the standard JDBC API approaches.
 * Please note that whether this is good, ok or bad depends
 * on the driver implementation of your database vendor.
 * It should work with all JDBC compliant databases.
 * Unfortunately it seems NOT to work with mssql
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: JdbcEsqlQuery.java,v 1.3 2003/03/20 21:51:43 tcurdt Exp $
 */
final public class JdbcEsqlQuery extends AbstractEsqlQuery {

    public JdbcEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private JdbcEsqlQuery(final ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     * @return
     */
    public AbstractEsqlQuery newInstance(final ResultSet resultSet) {
        return(new JdbcEsqlQuery(resultSet));
    }

    public PreparedStatement prepareStatement() throws SQLException {
        return (
                setPreparedStatement(
                        getConnection().prepareStatement(
                                getQueryString(),
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY)
                ));
    }

    public CallableStatement prepareCall() throws SQLException {
        return (
                (CallableStatement) setPreparedStatement(
                        getConnection().prepareCall(
                                getQueryString(),
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY
                        )
                )
                );
    }

    /**
     * AFAIK this is the proposed JDBC API way to get the number
     * of results. Unfortunately -at least some- driver implementation
     * are transfering the complete resultset when moving to the end.
     * Which is totally stupid for limit/paging purposes. So we probably
     * better stick with an additional count query from the AbstractEsqlQuery
     */
    /*
    public int getRowCount() throws SQLException {
        ResultSet rs = getResultSet();
        synchronized (rs) {
            int currentRow = rs.getRow();
            rs.last();
            int count = rs.getRow();
            if (currentRow > 0) {
                rs.absolute(currentRow);
            }
            else {
                rs.first();
                rs.relative(-1);
            }
            return (count);
        }
    }
    */

    public void getResultRows() throws SQLException {
        final int skip = getSkipRows();
        if (skip > 0) {
            getResultSet().absolute(skip);
        }
        setPosition(skip);
    }

}
