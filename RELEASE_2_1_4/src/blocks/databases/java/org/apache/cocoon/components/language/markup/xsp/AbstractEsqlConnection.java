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

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.sql.Connection;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: AbstractEsqlConnection.java,v 1.6 2003/06/23 14:00:50 tcurdt Exp $
 */
public abstract class AbstractEsqlConnection extends AbstractLogEnabled {

    private String url = null;
    private Properties properties = null;
    private boolean multipleResults = false;

    protected AbstractEsqlConnection() {
    }

    protected abstract Connection getConnection() throws SQLException;


    /**
     * It appears that some commercial DBMSs like Oracle and Informix
     * are broken in that they don't follow the JDBC standard and
     * calls to getUpdateCount after getMoreResults result either in
     * an exception (Informix) or return the same value (i.e. not -1) (Oracle).
     * In addition, this feature is only useful with stored procedures.
     * Hence we disable it per default.
     **/
    public void setMultipleResults(String value) {
        this.multipleResults = ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
    }

    public boolean getMultipleResults() {
        return (this.multipleResults);
    }



    public Properties getProperties() {
        return (properties);
    }

    public void setProperty(final String name, final Object value) {
        if (properties == null) properties = new Properties();
        properties.put(name, value);
    }

    public void setUser(String user) {
        setProperty("user", user);
    }

    public void setPassword(String password) {
        setProperty("password", password);
    }

    public void setAutoCommit(final boolean autocommit) throws SQLException {
        getConnection().setAutoCommit(autocommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return (getConnection().getAutoCommit());
    }

    public String getURL() throws SQLException {
        if (this.url == null) {
            this.url = getConnection().getMetaData().getURL();
        }
        return (this.url);
    }

    public void setURL(final String url) {
        this.url = url;
    }


    public DatabaseMetaData getMetaData() throws SQLException {
        return (getConnection().getMetaData());
    }

    public void commit() throws SQLException {
        getConnection().commit();
    }

    public void rollback() throws SQLException {
        getConnection().rollback();
    }

    public void close() throws SQLException {
        getConnection().close();
    }



    /**
     * Factory method for creating an EsqlQuery object. If type is set to
     * "" or "auto" it will try to find type from the JDBC connection URL.
     * If this does not succeed the generic JDBC type will be assumed.
     * (This type does not work for some databases like mssql though)
     *
     * @param type {sybase|postgresql|mysql|oracle|jdbc}
     * @param queryString
     * @return implementation of the AbstractEsqlQuery
     * @throws SQLException
     */
    public AbstractEsqlQuery createQuery(final String type, final String queryString) throws SQLException {
        AbstractEsqlQuery query;

        Connection connection = getConnection();

        if ("".equals(type) || "auto".equalsIgnoreCase(type)) {
            String database = connection.getMetaData().getDatabaseProductName().toLowerCase();

            if (database.indexOf("postgresql") > -1) {
                query = new PostgresEsqlQuery(connection,queryString);
            }
            else if (database.indexOf("mysql") > -1) {
                query = new MysqlEsqlQuery(connection,queryString);
            }
            else if (database.indexOf("adaptive server anywhere") > -1 ||
                     database.indexOf("microsoft sql server") > -1) {
                query = new SybaseEsqlQuery(connection,queryString);
            }
            else if (database.indexOf("oracle") > -1) {
                query = new OracleEsqlQuery(connection,queryString);
            }
            else if (database.indexOf("pervasive") > -1) {
                query = new PervasiveEsqlQuery(connection,queryString);
            }
            else if (database.indexOf("hsql") > -1 ||
                     database.indexOf("interbase") > -1 ||
                     database.indexOf("access") > -1 ||
                     database.indexOf("sap db") > -1 ||
                     database.indexOf("firebird") > -1 ||
                     database.indexOf("sybase sql server") > -1) {
                query = new JdbcEsqlQuery(getConnection(),queryString);
            }
            else {
                getLogger().warn("Your database [" + String.valueOf(database) + "] is not being recognized yet." +
                                 " Using the generic [jdbc] query as default. " +
                                 " Please report this to cocoon-dev or to tcurdt.at.apache.org directly.");

                query = new JdbcEsqlQuery(getConnection(),queryString);
            }
        }
        else if ("sybase".equalsIgnoreCase(type)) {
            query = new SybaseEsqlQuery(connection,queryString);
        }
        else if ("postgresql".equalsIgnoreCase(type)) {
            query = new PostgresEsqlQuery(connection,queryString);
        }
        else if ("postgresql-old".equalsIgnoreCase(type)) {
            query = new PostgresOldEsqlQuery(connection,queryString);
        }
        else if ("mysql".equalsIgnoreCase(type)) {
            query = new MysqlEsqlQuery(connection,queryString);
        }
        else if ("oracle".equalsIgnoreCase(type)) {
            query = new OracleEsqlQuery(connection,queryString);
        }
        else if ("pervasive".equalsIgnoreCase(type)) {
            query = new PervasiveEsqlQuery(connection,queryString);
        }
        else if ("jdbc".equalsIgnoreCase(type)) {
            query = new JdbcEsqlQuery(connection,queryString);
        }
        else {
            getLogger().error("Unknown database type: " + String.valueOf(type));
            throw new SQLException("Unknown database type: " + String.valueOf(type));
        }
        setupLogger(query);
        return(query);
    }
}

