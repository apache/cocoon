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
package org.apache.cocoon.blocks.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Map;

import org.apache.cocoon.kernel.composition.Component;
import org.apache.cocoon.kernel.composition.Wire;

/**
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class WirableConnection implements Connection {

    /** <p>Our {@link Connection} instance.</p> */
    private Connection connection = null;

    /** <p>Our {@link Wire} instance.</p> */
    private Wire wire = null;

    /**
     * <p>Create a new {@link WirableConnection} wrapping a specified JDBC/SQL
     * {@link Connection}.</p>
     *
     * @param connection the {@link Connection} to wrap.
     * @throws NullPointerException if the {@link Connection} was <b>null</b>.
     */
    public WirableConnection(Connection connection) {
        if (connection == null) {
            throw new NullPointerException("Null connection");
        }
        this.connection = connection;
    }

    /**
     * <p>Contextualize this {@link Component} instance with the {@link Wire}
     * through which its caller is accessing it.</p>
     *
     * @param wire the {@link Wire} instance associated with this instance.
     */
    public void contextualize(Wire wire) {
        this.wire = wire;
    }

    /**
     * <p>Releases this {@link Connection} object's database and JDBC resources
     * immediately instead of waiting for them to be automatically released.</p>
     *
     * <p>A call to this method will also release or dispose the {@link Wire}
     * associated to this {@link WirableConnection}.</p>
     *
     * @throws SQLException if a database access error occurs.
     */
    public void close()
    throws SQLException {
        try {
            this.connection.close();
            this.wire.release();
        } catch (SQLException exception) {
            this.wire.dispose();
            throw (exception);
        }
    }

    /* ====================================================================== */

    /**
     * <p>Clears all warnings reported for this {@link Connection} object.</p>
     *
     * @throws SQLException if a database access error occurs.
     */
    public void clearWarnings()
    throws SQLException {
        this.connection.clearWarnings();
    }

    /**
     * <p>Makes all changes made since the previous commit/rollback permanent
     * and releases any database locks currently held by this {@link Connection}
     * object.</p>
     *
     * @throws SQLException if a database access error occurs or this
     *                      {@link Connection} object is in auto-commit mode.
     */
    public void commit()
    throws SQLException {
        this.connection.commit();
    }

    /**
     * <p>Creates a {@link Statement} object for sending SQL statements
     * to the database.</p>
     *
     * @return a new default {@link Statement} object.
     * @throws SQLException if a database access error occurs.
     */
    public Statement createStatement()
    throws SQLException {
        return(this.connection.createStatement());
    }

    /**
     * <p>Creates a {@link Statement} object that will generate
     * {@link ResultSet} objects with the given type and concurrency.</p>
     *
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @return a new {@link Statement} object that will generate
     *         {@link ResultSet} objects with the given type and
     *         concurrency.
     * @throws SQLException if a database access error occurs
     *         or the given parameters are not {@link ResultSet} 
     *         constants indicating type and concurrency.
     */
    public Statement createStatement(int type, int conc) 
    throws SQLException {
        return(this.connection.createStatement(type, conc));
    }

    /**
     * <p>Creates a {@link Statement} object that will generate
     * {@link ResultSet} objects with the given type, concurrency,
     * and holdability.</p>
     *
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @param hold one of {@link ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     *             {@link ResultSet.CLOSE_CURSORS_AT_COMMIT}.
     * @return a new {@link Statement} object that will generate
     *         {@link ResultSet} objects with the given type, concurrency, and
     *         holdability.
     * @throws SQLException if a database access error occurs or the given
     *                      parameters are not {@link ResultSet} constants
     *                      indicating type, concurrency, and holdability.
     */
    public Statement createStatement(int type, int conc, int hold)
    throws SQLException {
        return(this.connection.createStatement(type, conc, hold));
    }

    /**
     * <p>Retrieves the current auto-commit mode for this {@link Connection}
     * object.</p>
     *
     * @return the current state of this {@link Connection} object's 
     *         auto-commit mode.
     * @throws SQLException if a database access error occurs.
     */
    public boolean getAutoCommit()
    throws SQLException {
        return(this.connection.getAutoCommit());
    }

    /**
     * <p>Retrieves this {@link Connection} object's current catalog name.</p>
     *
     * @return the current catalog name or <b>null</b> if there is none.
     * @throws SQLException if a database access error occurs.
     */
    public String getCatalog()
    throws SQLException {
        return(this.connection.getCatalog());
    }

    /**
     * <p>Retrieves the current holdability of {@link ResultSet} objects
     * created using this {@link Connection} object.</p>
     *
     * @return one of {@link ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     *         {@link ResultSet.CLOSE_CURSORS_AT_COMMIT}
     * @throws SQLException if a database access occurs.
     */
    public int getHoldability()
    throws SQLException {
        return(this.connection.getHoldability());
    }

    /**
     * <p>Retrieves a {@link DatabaseMetaData} object that contains metadata
     * about the database to which this {@link Connection} object represents a
     * connection.</p>
     *
     * @return a {@link DatabaseMetaData} object for this {@link Connection}
     * @throws SQLException if a database access error occurs.
     */
    public DatabaseMetaData getMetaData()
    throws SQLException {
        return(this.connection.getMetaData());
    }

    /**
     * <p>Retrieves this {@link Connection} object's current transaction
     * isolation level.</p>
     *
     * @return the current transaction isolation level, which will be one
     *         of the following constants:
     *         {@link Connection.TRANSACTION_READ_UNCOMMITTED}, 
     *         {@link Connection.TRANSACTION_READ_COMMITTED},
     *         {@link Connection.TRANSACTION_REPEATABLE_READ}, 
     *         {@link Connection.TRANSACTION_SERIALIZABLE}, or
     *         {@link Connection.TRANSACTION_NONE}.
     * @throws SQLException if a database access error occurs.
     */
    public int getTransactionIsolation()
    throws SQLException {
        return(this.connection.getTransactionIsolation());
    }

    /**
     * <p>Retrieves the {@link Map} object associated with this 
     * {@link Connection} object.</p>
     *
     * @return the {@link java.util.Map} object associated with this object
     * @throws SQLException if a database access error occurs.
     */
    public Map getTypeMap()
    throws SQLException {
        return(this.connection.getTypeMap());
    }

    /**
     * <p>Retrieves the first warning reported by calls on this
     * {@link Connection} object.</p>
     *
     * @return the first {@link SQLWarning} object or <b>null</b>.
     * @throws SQLException if a database access error occurs or this method is
     *                      called on a closed connection.
     */
    public SQLWarning getWarnings()
    throws SQLException {
        return(this.connection.getWarnings());
    }

    /**
     * <p>Retrieves whether this {@link Connection} object has been closed.</p>
     *
     * @return <b>true</b> if this {@link Connection} object
     *         is closed; <b>false</b> if it is still open.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isClosed()
    throws SQLException {
        return(this.connection.isClosed());
    }

    /**
     * <p>Retrieves whether this {@link Connection} object is in read-only
     * mode.</p>
     *
     * @return <b>true</b> if this {@link Connection} object is read-only,
     *         <b>false</b> otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isReadOnly()
    throws SQLException {
        return(this.connection.isReadOnly());
    }

    /**
     * <p>Converts the given SQL statement into native SQL grammar.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' parameter
     *            placeholders.
     * @return the native form of this statement.
     * @throws SQLException if a database access error occurs.
     */
    public String nativeSQL(String sql)
    throws SQLException {
        return(this.connection.nativeSQL(sql));
    }

    /**
     * <p>Creates a {@link CallableStatement} object for calling
     * database stored procedures.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' parameter
     *            placeholders. Typically this  statement is a JDBC function
     *            call escape string.
     * @return a new default {@link CallableStatement} object containing the
     *         pre-compiled SQL statement.
     * @throws SQLException if a database access error occurs.
     */
    public CallableStatement prepareCall(String sql)
    throws SQLException {
        return(this.connection.prepareCall(sql));
    }

    /**
     * <p>Creates a {@link CallableStatement} object that will generate
     * {@link ResultSet} objects with the given type and concurrency.</p>
     *
     * @param sql a {@link String} object that is the SQL statement to be sent
     *            to the database; may contain on or more '?' parameters.
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @return a new {@link PreparedStatement} object containing the
     *         pre-compiled SQL statement that will produce {@link ResultSet}
     *         objects with the given type and concurrency.
     * @throws SQLException if a database access error occurs or the given
     *                      parameters are not {@link ResultSet} constants
     *                      indicating type and concurrency.
     */
    public CallableStatement prepareCall(String sql, int type, int conc)
    throws SQLException {
        return(this.connection.prepareCall(sql, type, conc));
    }

    /**
     * <p>Creates a {@link CallableStatement} object that will generate
     * {@link ResultSet} objects with the given type and concurrency.</p>
     *
     * @param sql a {@link String} object that is the SQL statement to
     *            be sent to the database; may contain on or more '?' parameters
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @param hold one of {@link ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     *             {@link ResultSet.CLOSE_CURSORS_AT_COMMIT}.
     * @return a new {@link CallableStatement} object, containing the
     *         pre-compiled SQL statement, that will generate
     *         {@link ResultSet} objects with the given type,
     *         concurrency, and holdability.
     * @throws SQLException if a database access error occurs
     *            or the given parameters are not {@link ResultSet} 
     *            constants indicating type, concurrency, and holdability.
     */
    public CallableStatement prepareCall(String sql, int type, int conc, 
                                         int hold)
    throws SQLException {
        return(this.connection.prepareCall(sql, type, conc, hold));
    }

    /**
     * <p>Creates a {@link PreparedStatement} object for sending parameterized
     * SQL statements to the database.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' in
     *            parameter placeholders.
     * @return a new default {@link PreparedStatement} object containing the
     *         pre-compiled SQL statement. 
     * @throws SQLException if a database access error occurs.
     */
    public PreparedStatement prepareStatement(String sql)
    throws SQLException {
        return(this.connection.prepareStatement(sql));
    }

    /**
     * <p>Creates a default {@link PreparedStatement} object that has the
     * capability to retrieve auto-generated keys.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' in
     *            parameter placeholders.
     * @param keys one of {@link Statement.RETURN_GENERATED_KEYS} or
     *                    {@link Statement.NO_GENERATED_KEYS}.
     * @return a new {@link PreparedStatement} object, containing the
     *         pre-compiled SQL statement, that will have the capability of
     *         returning auto-generated keys.
     * @throws SQLException if a database access error occurs or the given
     *                      parameter is not a {@link Statement} constant
     *                      indicating whether auto-generated keys should be
     *                      returned.
     */
    public PreparedStatement prepareStatement(String sql, int keys)
    throws SQLException {
        return(this.connection.prepareStatement(sql, keys));
        }

    /**
     * <p>Creates a default {@link PreparedStatement} object capable
     * of returning the auto-generated keys designated by the given array.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' in
     *            parameter placeholders.
     * @param indexes an array of column indexes indicating the columns that
     *                should be returned from the inserted row or rows.
     * @return a new {@link PreparedStatement} object, containing the
     *         pre-compiled statement, that is capable of returning the
     *         auto-generated keys designated by the given array of column
     *         indexes.
     * @throws SQLException if a database access error occurs
     *
     */
    public PreparedStatement prepareStatement(String sql, int indexes[])
    throws SQLException {
        return(this.connection.prepareStatement(sql, indexes));
    }

    /**
     * <p>Creates a {@link PreparedStatement} object that will generate
     * {@link ResultSet} objects with the given type and concurrency.</p>
     *
     * @param sql a {@link String} object that is the SQL statement to be sent
     *            to the database, may contain one or more '?' in parameters.
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @return a new {@link PreparedStatement} object containing the
     *         pre-compiled SQL statement that will produce {@link ResultSet}
     *         objects with the given type and concurrency.
     * @throws SQLException if a database access error occurs or the given
     *                      parameters are not {@link ResultSet} constants
     *                      indicating type and concurrency.
     */
    public PreparedStatement prepareStatement(String sql, int type, 
                                              int conc)
    throws SQLException {
        return(this.connection.prepareStatement(sql, type, conc));
    }

    /**
     * <p>Creates a {@link PreparedStatement} object that will generate
     * {@link ResultSet} objects with the given type, concurrency,
     * and holdability.</p>
     *
     * @param sql a {@link String} object that is the SQL statement to
     *            be sent to the database, may contain one or more '?' in
     *            parameters.
     * @param type one of {@link ResultSet.TYPE_FORWARD_ONLY},
     *             {@link ResultSet.TYPE_SCROLL_INSENSITIVE}, or
     *             {@link ResultSet.TYPE_SCROLL_SENSITIVE}.
     * @param conc one of {@link ResultSet.CONCUR_READ_ONLY} or
     *             {@link ResultSet.CONCUR_UPDATABLE}.
     * @param hold one of {@link ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     *             {@link ResultSet.CLOSE_CURSORS_AT_COMMIT}.
     * @return a new {@link PreparedStatement} object, containing the
     *         pre-compiled SQL statement, that will generate
     *         {@link ResultSet} objects with the given type,
     *         concurrency, and holdability.
     * @throws SQLException if a database access error occurs or the given
     *                      parameters are not {@link ResultSet} constants
     *                      indicating type, concurrency, and holdability.
     */
    public PreparedStatement prepareStatement(String sql, int type, int conc,
                                              int hold)
    throws SQLException {
        return(this.connection.prepareStatement(sql, type, conc, hold));
    }

    /**
     * <p>Creates a default {@link PreparedStatement} object capable
     * of returning the auto-generated keys designated by the given array.</p>
     *
     * @param sql an SQL statement that may contain one or more '?' in
     *            parameter placeholders.
     * @param name an array of column names indicating the columns that should
     *             be returned from the inserted row or rows.
     * @return a new {@link PreparedStatement} object, containing the
     *         pre-compiled statement, that is capable of returning the
     *         auto-generated keys designated by the given array of column
     *         names
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement prepareStatement(String sql, String names[])
    throws SQLException {
        return(this.connection.prepareStatement(sql, names));
    }

    /**
     * <p>Removes the given {@link Savepoint} object from the current 
     * transaction.</p>
     *
     * @param savepoint the {@link Savepoint} object to be removed.
     * @throws SQLException if a database access error occurs or the given
     *                      {@link Savepoint} object is not a valid savepoint.
     */
    public void releaseSavepoint(Savepoint savepoint)
    throws SQLException {
        this.connection.releaseSavepoint(savepoint);
    }

    /**
     * <p>Undoes all changes made in the current transaction and releases any
     * database locks currently held by this {@link Connection} object.</p>
     *
     * @throws SQLException if a database access error occurs or this
     *                      {@link Connection} object is in auto-commit mode.
     */
    public void rollback()
    throws SQLException {
        this.connection.rollback();
    }

    /**
     * <p>Undoes all changes made after the given {@link Savepoint} object
     * was set.</p>
     *
     * @param savepoint the {@link Savepoint} object to roll back to.
     * @throws SQLException if a database access error occurs, the
     *                      {@link Savepoint} object is no longer valid, or
     *                      this {@link Connection} object is currently in
     *                      auto-commit mode.
     */
    public void rollback(Savepoint savepoint)
    throws SQLException {
        this.connection.rollback(savepoint);
    }

    /**
     * <p>Sets this connection's auto-commit mode to the given state.</p>
     *
     * @param autoCommit <b>true</b> to enable auto-commit mode, <b>false</b>
     *                   to disable it.
     * @throws SQLException if a database access error occurs.
     */
    public void setAutoCommit(boolean autoCommit)
    throws SQLException {
        this.connection.setAutoCommit(autoCommit);
    }

    /**
     * <p>Sets the given catalog name in order to select a subspace of this
     * {@link Connection} object's database in which to work.</p>
     *
     * @param catalog the name of a catalog in which to work.
     * @throws SQLException if a database access error occurs.
     */
    public void setCatalog(String catalog)
    throws SQLException {
        this.connection.setCatalog(catalog);
    }

    /**
     * <p>Changes the holdability of {@link ResultSet} objects created using
     * this {@link Connection} object to the given holdability.</p>
     *
     * @param holdability a {@link ResultSet} holdability constant, one of
     *                    {@link ResultSet.HOLD_CURSORS_OVER_COMMIT} or
     *                    {@link ResultSet.CLOSE_CURSORS_AT_COMMIT}.
     * @throws SQLException if a database access occurs, the given parameter
     *                       is not a {@link ResultSet} constant indicating
     *                       holdability, or the given holdability is not
     *                       supported.
     */
    public void setHoldability(int holdability)
    throws SQLException {
        this.connection.setHoldability(holdability);
    }

    /**
     * <p>Puts this connection in read-only mode as a hint to the driver to
     * enable database optimizations.</p>
     *
     * @param mode <b>true</b> enables read-only mode, <b>false</b> disables it.
     * @throws SQLException if a database access error occurs or this method is
     *                      called during a transaction.
     */
    public void setReadOnly(boolean mode)
    throws SQLException {
        this.connection.setReadOnly(mode);
    }

    /**
     * <p>Creates an unnamed savepoint in the current transaction and returns
     * the new {@link Savepoint} object that represents it.</p>
     *
     * @return the new {@link Savepoint} object.
     * @throws SQLException if a database access error occurs or this
     *                      {@link Connection} object is currently in
     *                      auto-commit mode.
     */
    public Savepoint setSavepoint()
    throws SQLException {
        return(this.connection.setSavepoint());
    }

    /**
     * <p>Creates a savepoint with the given name in the current transaction
     * and returns the new {@link Savepoint} object that represents it.</p>
     *
     * @param name a {@link String} containing the name of the savepoint.
     * @return the new {@link Savepoint} object.
     * @throws SQLException if a database access error occurs or this
     *                      {@link Connection} object is currently in
     *                      auto-commit mode.
     */
    public Savepoint setSavepoint(String name)
    throws SQLException {
        return(this.connection.setSavepoint(name));
    }

    /**
     * <p>Attempts to change the transaction isolation level for this
     * {@link Connection} object to the one given.</p>
     *
     * @param level one of the following {@link Connection} constants:
     *        {@link Connection.TRANSACTION_READ_UNCOMMITTED},
     *        {@link Connection.TRANSACTION_READ_COMMITTED},
     *        {@link Connection.TRANSACTION_REPEATABLE_READ}, or
     *        {@link Connection.TRANSACTION_SERIALIZABLE}.
     * @throws SQLException if a database access error occurs or the given
     *                      parameter is not one of the {@link Connection}
     *                      constants.
     */
    public void setTransactionIsolation(int level)
    throws SQLException {
        this.connection.setTransactionIsolation(level);
    }

    /**
     * <p>Installs the given {@link TypeMap} object as the type map for this
     * {@link Connection} object.</p>
     *
     * @param map the {@link java.util.Map} object to install as a replacement
     *            for this {@link Connection} object's default type map.
     * @throws SQLException if a database access error occurs or the given
     *                      parameter is not a {@link java.util.Map} object.
     */
    public void setTypeMap(Map map)
    throws SQLException {
        this.connection.setTypeMap(map);
    }
}
