/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import org.apache.avalon.Composer;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.util.datasource.DataSourceComponent;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Blob;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Date;

import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This Reader pulls a resource from a database.  It is configured with
 * the Connection to use, parameters specify the table and column
 * to pull the image from, and source specifies the source key information.
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 */
public class DatabaseReader extends AbstractReader implements Composer, Configurable {
    ComponentSelector dbselector;
    String dsn;

    /**
     * Compose the object so that we get the <code>Component</code>s we need from
     * the <code>ComponentManager</code>.
     */
    public void compose(ComponentManager manager) throws ComponentManagerException {
        this.dbselector = (ComponentSelector) manager.lookup(Roles.DB_CONNECTION);
    }

    /**
     * Configure the <code>Reader</code> so that we can use the same database
     * for all instances.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.dsn = conf.getChild("use-connection").getValue();
    }

    /**
     * Generates the resource we need to retrieve verbatim from the
     * database.  Granted, this can be used for any resource from a
     * database, so we may want to get rid of the bias toward images.
     * This reader requires a number of parameters:
     *
     * <pre>
     *   &lt;parameter name="table" value="database_table_name"/&gt;
     *   &lt;parameter name="image" value="database_resource_column_name"/&gt;
     *   &lt;parameter name="key" value="database_lookup_key_column_name"/&gt;
     * </pre>
     *
     * Please note that if any of those parameters are missing, this
     * <code>Reader</code> cannot function.  There are a number of other
     * parameters that allow you to provide hints for the reader to
     * optimize resource use:
     *
     * <pre>
     *   &lt;parameter name="last-modified" value="database_timestamp_column_name"/&gt;
     *   &lt;parameter name="content-type" value="content_mime_type"/&gt;
     *   &lt;parameter name="expires" value="number_of_millis_before_refresh"/&gt;
     * </pre>
     *
     * Lastly, the <code>key</code> value is derived from the value of
     * the <code>source</code> string.
     */
    public void generate() throws ProcessingException, SAXException, IOException {
        DataSourceComponent datasource = null;
        Connection con = null;

        try {
            datasource = (DataSourceComponent) dbselector.select(dsn);
            con = datasource.getConnection();

            if (con.getAutoCommit() == true) {
                con.setAutoCommit(false);
            }

            PreparedStatement statement = con.prepareStatement(getQuery());
            statement.setString(1, this.source);
            ResultSet set = statement.executeQuery();
            if (set.next() == false) throw new ResourceNotFoundException("There is no image with that key");

            HttpServletResponse res = (HttpServletResponse) objectModel.get(Constants.RESPONSE_OBJECT);
            HttpServletRequest req = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);

            if (this.modifiedSince(set, req, res)) {
                Blob object = set.getBlob(1);

                if (object == null) {
                    throw new ResourceNotFoundException("There is no image with that key");
                }

                res.setContentType(this.parameters.getParameter("content-type", ""));
                this.serialize(object, res);
            }

            con.commit();
        } catch (IOException ioe) {
            getLogger().debug("Assuming client reset stream");

            if (con != null) try {con.rollback();} catch (SQLException se) {}
        } catch (Exception e) {
            getLogger().warn("Could not get resource from Database", e);

            if (con != null) try {con.rollback();} catch (SQLException se) {}

            throw new ResourceNotFoundException("DatabaseReader error:", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sqe) {
                    getLogger().warn("Could not close connection", sqe);
                }
            }

            if (datasource != null) this.dbselector.release((Component) datasource);
        }
    }

    /**
     * This method builds the query string used for accessing the database.
     * If the required parameters do not exist, then we cannot build a
     * correct query.
     */
    public String getQuery() throws Exception {
        String table = this.parameters.getParameter("table", null);
        String column = this.parameters.getParameter("image", null);
        String key = this.parameters.getParameter("key", null);

        if (table == null || column == null || key==null) {
            throw new ProcessingException("We are missing a required parameter.  Please include 'table', 'image', and 'key'");
        }

        String date = this.parameters.getParameter("last-modified", null);
        StringBuffer query = new StringBuffer("SELECT ");

        query.append(column);

        if (date != null) {
            query.append(", ").append(date);
        }

        query.append(" FROM ").append(table);
        query.append(" WHERE ").append(key).append(" = ?");

        return query.toString();
    }

    /**
     * Tests whether a resource has been modified or not.  As Blobs and
     * database columns usually do not have intrinsic dates on them (at
     * least easily accessible), we have to have a database column that
     * holds a date for the resource.  Please note, that the database
     * column <strong>must</strong> be a <code>Timestamp</code> column.
     *
     * In the absence of such a column this method <em>always</em>
     * returns <code>true</code>.  This is because databases are much
     * more prone to change than filesystems, and don't have intrinsic
     * timestamps on column updates.
     */
    public boolean modifiedSince(ResultSet set, HttpServletRequest request, HttpServletResponse response)
    throws SQLException {
        String lastModified = this.parameters.getParameter("last-modified", null);

        if (lastModified != null) {
            Timestamp modified = set.getTimestamp(lastModified, null);

            response.setDateHeader("Last-Modified", modified.getTime());

            return modified.getTime() > request.getDateHeader("if-modified-since");
        }

        // if we have nothing to compare to, then we must assume it
        // has been modified
        return true;
    }

    /**
     * This method actually performs the serialization.
     */
    public void serialize(Blob object, HttpServletResponse response)
    throws IOException, SQLException {
        if (object == null) {
            throw new SQLException("The Blob is empty!");
        }

        InputStream is = object.getBinaryStream();

        byte[] bytes = new byte[(int) object.length()];
        is.read(bytes);
        is.close();

        response.setContentLength((int) object.length());
        long expires = parameters.getParameterAsInteger("expires", -1);

        if (expires > 0) {
            response.setDateHeader("Expires", new Date().getTime() + expires);
        }

        response.setHeader("Accept-Ranges", "bytes");
        out.write(bytes);
    }
}
