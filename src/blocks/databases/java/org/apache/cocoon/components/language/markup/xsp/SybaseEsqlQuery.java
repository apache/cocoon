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

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Connection;

/**
 * Database specific EsqlQuery
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: SybaseEsqlQuery.java,v 1.4 2003/07/03 07:59:59 cziegeler Exp $
 */
final public class SybaseEsqlQuery extends AbstractEsqlQuery {

    public SybaseEsqlQuery(Connection connection, String query) {
        super(connection, query);
    }

    /**
     * Only newInstance may use this contructor
     * @param resultSet
     */
    private SybaseEsqlQuery(final ResultSet resultSet) {
        super(resultSet);
    }

    /**
     * Create a EsqlQuery of the same type
     * @param resultSet
     */
    public AbstractEsqlQuery newInstance(ResultSet resultSet) {
        return(new SybaseEsqlQuery(resultSet));
    }

    public String getQueryString() throws SQLException {
        if (getMaxRows() > -1) {
            String original = super.getQueryString().trim();
            int command = original.indexOf(' ');
            return (new StringBuffer()
                    .append(original.substring(0,command))
                    .append(" TOP ").append(getMaxRows()+1 + getSkipRows())
                    .append(original.substring(command))
                    .toString());
        }
        else {
            return (super.getQueryString());
        }
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

    public void getResultRows() throws SQLException {
        final int skip = getSkipRows();
        if (skip > 0) {
            getResultSet().absolute(skip);
        }
        setPosition(skip);
    }

}
