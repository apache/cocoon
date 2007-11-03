/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A <code>Source</code> that takes its content in a single JDBC column. Any
 * kind of column can be used (clob, blob, varchar, etc), but "Blob" means
 * that the whole content is contained in a single column.
 * <p>The URL syntax is "blob:/datasource/table/column[cond]", where :
 * <ul>
 * <li>"datasource" is the jdbc datasource to use (defined in cocoon.xonf)
 * <li>"table" is the database table,
 * <li>"column" is (you can guess, now :) the column in "table",
 * <li>"cond" is the boolean condition used to select a particular record in
 * the table.
 * </ul>
 * <p>For example, "<code>blob:/personel/people/photo[userid='foo']</code>"
 * will fetch the first column returned by the statement "<code>SELECT photo
 * from people where userid='foo'</code>" in the datasource "<code>personel</code>"
 *
 * @version $Id$
 */
public class BlobSource extends AbstractLogEnabled
                        implements Source, Serviceable {

    private static final String URL_PREFIX = "blob:/";
    private static final int URL_PREFIX_LEN = URL_PREFIX.length();

    /** The ServiceManager instance */
    private ServiceManager manager;

    /** The system ID for this source */
    private String systemId;

    private String datasourceName;

    private String tableName;

    private String columnName;

    private String condition;

    /**
     * Create a file source from a 'blob:' url and a component manager.
     * <p>The url is of the form "blob:/datasource/table/column[condition]
     */
    public BlobSource(String url) throws MalformedURLException {
        this.systemId = url;

        // Parse the url
        int start = URL_PREFIX_LEN;

        // Datasource
        int end = url.indexOf('/', start);
        if (end == -1) {
            throw new MalformedURLException("Malformed blob source (cannot find datasource) : " + url);
        }

        this.datasourceName = url.substring(start, end);

        // Table
        start = end + 1;
        end = url.indexOf('/', start);
        if (end == -1) {
            throw new MalformedURLException("Malformed blob source (cannot find table name) : " + url);
        }

        this.tableName = url.substring(start, end);

        // Column
        start = end + 1;
        end = url.indexOf('[', start);
        if (end == -1) {
            this.columnName = url.substring(start);
        } else {
            this.columnName = url.substring(start, end);

            // Condition
            start = end + 1;
            end = url.length() - 1;
            if (url.charAt(end) != ']') {
                throw new MalformedURLException("Malformed url for a blob source (cannot find condition) : " + url);
            } else {
                this.condition = url.substring(start, end);
            }
        }
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager)  {
        this.manager = manager;
    }

    /**
     * Return the protocol
     */
    public String getScheme() {
        return URL_PREFIX;
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.systemId;
    }

    /**
     * Get the input stream for this source.
     */
    public InputStream getInputStream() throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Opening stream for datasource " + this.datasourceName +
                ", table " + this.tableName + ", column " + this.columnName +
                (this.condition == null ? ", no condition" : ", condition " + this.condition)
            );
        }

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            StringBuffer selectBuf = new StringBuffer("SELECT ").append(this.columnName).
                append(" FROM ").append(this.tableName);

            if (this.condition != null) {
                selectBuf.append(" WHERE ").append(this.condition);
            }

            String select = selectBuf.toString();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing statement " + select);
            }

            ResultSet rs = stmt.executeQuery(select);
            if (!rs.next()) {
                throw new SourceNotFoundException("Source not found.");
            }

            int colType = rs.getMetaData().getColumnType(1);
            switch(colType) {
                case Types.BLOB :
                    Blob blob = rs.getBlob(1);
                    if (blob != null) {
                        return new JDBCInputStream(blob.getBinaryStream(), conn);
                    }
                    break;

                case Types.CLOB :
                    Clob clob = rs.getClob(1);
                    if (clob != null) {
                        return new JDBCInputStream(clob.getAsciiStream(), conn);
                    }
                    break;

                default :
                    String value = rs.getString(1);
                    if (value != null) {
                        return new ByteArrayInputStream(value.getBytes());
                    }
            }

            return new ByteArrayInputStream(new byte[0]);
        } catch (SQLException e) {
            String msg = "Cannot retrieve content from " + this.systemId;
            getLogger().error(msg, e);
            // IOException would be more adequate, but SourceException is cascaded...
            throw new SourceException(msg, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) { /* ignored */ }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) { /* ignored */ }
        }
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return null;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {
    }

    /**
     *
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        // FIXME
        return true;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be <code>null</code>.
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
        return -1;
    }

    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return 0;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return new EmptyIterator();
    }

    static class EmptyIterator implements Iterator {
        public boolean hasNext() { return false; }
        public Object next() { throw new NoSuchElementException(); }
        public void remove() {}
    }

    private Connection getConnection() throws SourceException {

        ServiceSelector selector = null;
        DataSourceComponent datasource = null;

        try {
            try {
                selector = (ServiceSelector)this.manager.lookup(DataSourceComponent.ROLE + "Selector");

                datasource = (DataSourceComponent)selector.select(this.datasourceName);

            } catch (Exception e) {
                String msg = "Cannot get datasource '" + this.datasourceName + "'";
                getLogger().error(msg);
                throw new SourceException(msg, e);
            }

            try {
                return datasource.getConnection();
            } catch (Exception e) {
                String msg = "Cannot get connection for datasource '" + this .datasourceName + "'";
                getLogger().error(msg);
                throw new SourceException(msg, e);
            }

        } finally {
            if (datasource != null) {
                selector.release(datasource);
            }
        }
    }

    /**
     * An OutputStream that will close the connection that created it on
     * close.
     */
    private class JDBCInputStream extends FilterInputStream {

        private Connection cnx;

        private void closeCnx() throws IOException {
            if (this.cnx != null) {
                try {
                    cnx.close();
                } catch (Exception e) {
                    String msg = "Error closing the connection for " + BlobSource.this.getURI();
                    BlobSource.this.getLogger().warn(msg, e);
                    throw new CascadingIOException(msg + " : " + e.getMessage(), e);
                } finally {
                    cnx = null;
                }
            }
        }

        public JDBCInputStream(InputStream stream, Connection cnx) {
            super(stream);
            this.cnx = cnx;
        }

        public int read() throws IOException {
            try {
                int result = in.read();
                if (result == -1) {
                    closeCnx();
                }
                return result;
            } catch(IOException e) {
                closeCnx();
                throw e;
            }
        }

        public int read(byte[] b) throws IOException {
            try {
                int result = in.read(b);
                if (result == -1) {
                    closeCnx();
                }
                return result;
            } catch(IOException e) {
                closeCnx();
                throw e;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            try {
                int result = in.read(b, off, len);
                if (result == -1) {
                    closeCnx();
                }
                return result;
            } catch(IOException e) {
                closeCnx();
                throw e;
            }
        }

        public void close() throws IOException {
            super.close();
            closeCnx();
        }
    }
}
