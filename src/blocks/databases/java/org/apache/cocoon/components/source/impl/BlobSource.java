/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;

import org.apache.cocoon.CascadingIOException;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

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
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: BlobSource.java,v 1.1 2003/06/23 17:53:42 cziegeler Exp $
 */
public class BlobSource extends AbstractLogEnabled implements Source, Composable {

    /** The component manager instance */
    private ComponentManager manager = null;

    /**
     * The system ID for this source
     */
    private String systemId;

    private String datasourceName;

    private String tableName;

    private String columnName;

    private String condition;

    private final static String URL_PREFIX = "blob:/";
    private final static int URL_PREFIX_LEN = URL_PREFIX.length();

    /**
     * Create a file source from a 'blob:' url and a component manager.
     * <p>The url is of the form "blob:/datasource/table/column[condition]
     */
    public BlobSource(String url) throws MalformedURLException {

        this.systemId = url;

        // Parse the url
        int start = URL_PREFIX_LEN;
        int end;

        // Datasource
        end = url.indexOf('/', start);
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
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager)  {
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
    public InputStream getInputStream() throws IOException, SourceException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Opening stream for datasource " + this.datasourceName +
                ", table " + this.tableName + ", column " + this.columnName +
                (this.condition == null ? ", no condition" : ", condition " + this.condition)
            );
        }

        Connection cnx = null;
        Statement stmt = null;

        try {
            cnx = getConnection();
            stmt = cnx.createStatement();

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
            rs.next();

            int colType = rs.getMetaData().getColumnType(1);

            switch(colType) {
                case Types.BLOB :
                    Blob blob = rs.getBlob(1);
                    return new JDBCInputStream(blob.getBinaryStream(), cnx);
                //break;

                case Types.CLOB :
                    Clob clob = rs.getClob(1);
                    return new JDBCInputStream(clob.getAsciiStream(), cnx);
                //break;

                default :
                    String value = rs.getString(1);
                    stmt.close();
                    rs.close();
                    cnx.close();
                    return new ByteArrayInputStream(value.getBytes());
            }
        } catch(SQLException sqle) {
            String msg = "Cannot retrieve content from " + this.systemId;
            getLogger().error(msg, sqle);

            try {
                if (cnx != null) {
                    cnx.close();
                }
            } catch(SQLException sqle2) {
                // PITA
                throw new SourceException("Cannot close connection", sqle2);
            }

            // IOException would be more adequate, but ProcessingException is cascaded...
            throw new SourceException(msg, sqle);
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

    class EmptyIterator implements Iterator {
        public boolean hasNext() { return false; }
        public Object next() { return null; }
        public void remove() {}
    }

    private Connection getConnection() throws SourceException {

        ComponentSelector selector = null;
        DataSourceComponent datasource = null;

        try {
            try {
                selector = (ComponentSelector)this.manager.lookup(DataSourceComponent.ROLE + "Selector");

                datasource = (DataSourceComponent)selector.select(this.datasourceName);

            } catch(Exception e) {
                String msg = "Cannot get datasource '" + this.datasourceName + "'";
                getLogger().error(msg);
                throw new SourceException(msg, e);
            }

            try {
                return datasource.getConnection();
            } catch(Exception e) {
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

        Connection cnx;

        private final void closeCnx() throws IOException {
            if (this.cnx != null) {
                try {
                    Connection tmp = cnx;
                    cnx = null;
                    tmp.close();
                } catch(Exception e) {
                    String msg = "Error closing the connection for " + BlobSource.this.systemId;
                    BlobSource.this.getLogger().warn(msg, e);
                    throw new CascadingIOException(msg + " : " + e.getMessage(), e);
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

