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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.lang.BooleanUtils;

import java.sql.Connection;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: AbstractEsqlConnection.java,v 1.9 2004/06/04 07:44:39 tcurdt Exp $
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
        this.multipleResults = BooleanUtils.toBoolean(value);
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
                     database.indexOf("informix-online") > -1 ||
                     database.indexOf("sybase sql server") > -1) {
                query = new JdbcEsqlQuery(getConnection(),queryString);
            }
            else {
                getLogger().warn("Your database [" + String.valueOf(database) + "] is not being recognized yet." +
                                 " Using the generic [jdbc] query as default. " +
                                 " Please report this to dev@cocoon.apache.org");

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

