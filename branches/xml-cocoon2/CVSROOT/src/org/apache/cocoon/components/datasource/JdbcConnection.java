/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.util.Map;

import org.apache.avalon.Recyclable;
import org.apache.avalon.util.pool.Pool;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/**
 * The Connection object used in conjunction with the JdbcDataSource
 * object.
 *
 * TODO: Implement a configurable closed end Pool, where the Connection
 * acts like JDBC PooledConnections work.  That means we can limit the
 * total number of Connection objects that are created.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-01-08 20:25:43 $
 */
public class JdbcConnection implements Connection, Recyclable {
    private Connection conn;
    private Pool pool;
    private Logger log = LogKit.getLoggerFor("cocoon");

    public JdbcConnection(Connection realConn, Pool parent) {
        this.conn = realConn;
        this.pool = parent;
    }
    public Statement createStatement() throws SQLException {
        return this.conn.createStatement();
    }
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.conn.prepareStatement(sql);
    }
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.conn.prepareCall(sql);
    }
    public String nativeSQL(String sql) throws SQLException {
        return this.conn.nativeSQL(sql);
    }
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.conn.setAutoCommit(autoCommit);
    }
    public boolean getAutoCommit() throws SQLException {
        return this.conn.getAutoCommit();
    }
    public void commit() throws SQLException {
        this.conn.commit();
    }
    public void rollback() throws SQLException {
        this.conn.rollback();
    }
    public void close() throws SQLException {
        this.pool.put(this);
    }
    public void recycle() {
        try {
           this.conn.close();
        } catch (SQLException se) {
           log.warn("Could not close connection", se);
        }
    }
    public boolean isClosed() throws SQLException {
        return this.conn.isClosed();
    }
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.conn.getMetaData();
    }
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.conn.setReadOnly(readOnly);
    }
    public boolean isReadOnly() throws SQLException {
        return this.conn.isReadOnly();
    }
    public void setCatalog(String catalog) throws SQLException {
        this.conn.setCatalog(catalog);
    }
    public String getCatalog() throws SQLException {
        return this.conn.getCatalog();
    }
    public void setTransactionIsolation(int level) throws SQLException {
        this.conn.setTransactionIsolation(level);
    }
    public int getTransactionIsolation() throws SQLException {
        return this.conn.getTransactionIsolation();
    }
    public SQLWarning getWarnings() throws SQLException {
        return this.conn.getWarnings();
    }
    public void clearWarnings() throws SQLException {
        this.conn.clearWarnings();
    }
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.conn.createStatement(resultSetType, resultSetConcurrency);
    }
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    public Map getTypeMap() throws SQLException {
        return this.conn.getTypeMap();
    }
    public void setTypeMap(Map map) throws SQLException {
        this.conn.setTypeMap(map);
    }
}