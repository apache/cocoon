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

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: AbstractEsqlConnection.java,v 1.2 2003/03/11 17:44:20 vgritsenko Exp $
 */
public abstract class AbstractEsqlConnection extends AbstractLogEnabled implements Connection {

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



    public String getURL() throws SQLException {
        if (this.url == null) {
            this.url = getConnection().getMetaData().getURL();
        }
        return (this.url);
    }

    public void setURL(final String url) {
        this.url = url;
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
        if ("".equals(type) || "auto".equalsIgnoreCase(type)) {
            String url = getURL();

            if (url.startsWith("jdbc:postgresql:")) {
                query = new PostgresEsqlQuery(this,queryString);
            }
            else if (url.startsWith("jdbc:mysql:")) {
                query = new MysqlEsqlQuery(this,queryString);
            }
            else if (url.startsWith("jdbc:sybase:")) {
                query = new SybaseEsqlQuery(this,queryString);
            }
            else if (url.startsWith("jdbc:oracle:")) {
                query = new OracleEsqlQuery(this,queryString);
            }
            else if (url.startsWith("jdbc:pervasive:")) {
                query = new PervasiveEsqlQuery(this,queryString);
            }
            else {
                getLogger().warn("Cannot guess database type from jdbc url: " + String.valueOf(url) +" - Defaulting to JDBC");
                query = new JdbcEsqlQuery(this,queryString);
            }
        }
        else if ("sybase".equalsIgnoreCase(type)) {
            query = new SybaseEsqlQuery(this,queryString);
        }
        else if ("postgresql".equalsIgnoreCase(type)) {
            query = new PostgresEsqlQuery(this,queryString);
        }
        else if ("postgresql-old".equalsIgnoreCase(type)) {
            query = new PostgresOldEsqlQuery(this,queryString);
        }
        else if ("mysql".equalsIgnoreCase(type)) {
            query = new MysqlEsqlQuery(this,queryString);
        }
        else if ("oracle".equalsIgnoreCase(type)) {
            query = new OracleEsqlQuery(this,queryString);
        }
        else if ("pervasive".equalsIgnoreCase(type)) {
            query = new PervasiveEsqlQuery(this,queryString);
        }
        else if ("jdbc".equalsIgnoreCase(type)) {
            query = new JdbcEsqlQuery(this,queryString);
        }
        else {
            getLogger().error("Unknown database type: " + String.valueOf(type));
            throw new SQLException("Unknown database type: " + String.valueOf(type));
        }
        setupLogger(query);
        return(query);
    }



    /* just wrap methods below */

    public java.sql.Statement createStatement() throws SQLException {
        return (getConnection().createStatement());
    }

    public java.sql.Statement createStatement(int i1, int i2) throws SQLException {
        return (getConnection().createStatement(i1, i2));
    }

    public java.sql.PreparedStatement prepareStatement(String s) throws SQLException {
        return (getConnection().prepareStatement(s));
    }

    public java.sql.PreparedStatement prepareStatement(String s, int i1, int i2) throws SQLException {
        return (getConnection().prepareStatement(s, i1, i2));
    }


    public void close() throws SQLException {
        getConnection().close();
    }

    public void commit() throws SQLException {
        getConnection().commit();
    }

    public void rollback() throws SQLException {
        getConnection().rollback();
    }

    public boolean getAutoCommit() throws SQLException {
        return (getConnection().getAutoCommit());
    }

    public void setAutoCommit(boolean autocommit) throws SQLException {
        getConnection().setAutoCommit(autocommit);
    }

    public void setTransactionIsolation(int i) throws SQLException {
        getConnection().setTransactionIsolation(i);
    }

    public int getTransactionIsolation() throws SQLException {
        return (getConnection().getTransactionIsolation());
    }

    public String getCatalog() throws SQLException {
        return (getConnection().getCatalog());
    }

    public java.sql.SQLWarning getWarnings() throws SQLException {
        return (getConnection().getWarnings());
    }

    public java.util.Map getTypeMap() throws SQLException {
        return (getConnection().getTypeMap());
    }

    public boolean isClosed() throws SQLException {
        return (getConnection().isClosed());
    }

    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        return (getConnection().getMetaData());
    }

    public void setCatalog(String s) throws SQLException {
        getConnection().setCatalog(s);
    }

    public void setTypeMap(java.util.Map m) throws SQLException {
        getConnection().setTypeMap(m);
    }

    public void setReadOnly(boolean b) throws SQLException {
        getConnection().setReadOnly(b);
    }

    public void clearWarnings() throws SQLException {
        getConnection().clearWarnings();
    }

    public boolean isReadOnly() throws SQLException {
        return (getConnection().isReadOnly());
    }

    public String nativeSQL(String s) throws SQLException {
        return (getConnection().nativeSQL(s));
    }

    public java.sql.CallableStatement prepareCall(String s) throws SQLException {
        return (getConnection().prepareCall(s));
    }

    public java.sql.CallableStatement prepareCall(String s, int i1, int i2) throws SQLException {
        return (getConnection().prepareCall(s, i1, i2));
    }

}

