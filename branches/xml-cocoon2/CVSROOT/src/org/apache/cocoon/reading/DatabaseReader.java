/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.NOPCacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.util.HashUtil;
import org.xml.sax.SAXException;

/**
 * This Reader pulls a resource from a database.  It is configured with
 * the Connection to use, parameters specify the table and column
 * to pull the image from, and source specifies the source key information.
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 */
public class DatabaseReader extends AbstractReader implements Composable, Configurable, Disposable, Cacheable {
    private ComponentSelector dbselector;
    private String dsn;
    private long lastModified = 0;
    private Blob resource = null;
    private Connection con = null;
    private DataSourceComponent datasource = null;
    private boolean doCommit = false;

    private ComponentManager manager;

    /**
     * Compose the object so that we get the <code>Component</code>s we need from
     * the <code>ComponentManager</code>.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
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
     * Set the <code>EntityResolver</code> the object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        try {
            this.datasource = (DataSourceComponent) dbselector.select(dsn);
            this.con = datasource.getConnection();

            if (this.con.getAutoCommit() == true) {
                this.con.setAutoCommit(false);
            }

            PreparedStatement statement = con.prepareStatement(getQuery());
            statement.setString(1, this.source);
            ResultSet set = statement.executeQuery();
            if (set.next() == false) throw new ResourceNotFoundException("There is no image with that key");

            Response response = (Response) objectModel.get(Constants.RESPONSE_OBJECT);
            Request request = (Request) objectModel.get(Constants.REQUEST_OBJECT);

            if (this.modifiedSince(set, request, response)) {
                this.resource = set.getBlob(1);

                if (this.resource == null) {
                    throw new ResourceNotFoundException("There is no image with that key");
                }
            }

            this.doCommit = true;
        } catch (Exception e) {
            getLogger().warn("Could not get resource from Database", e);

            this.doCommit = false;

            throw new ResourceNotFoundException("DatabaseReader error:", e);
        }
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
    public int generate() throws ProcessingException, SAXException, IOException {
        int contentLength = 0;

        try {
            return this.serialize(object, response);
        } catch (IOException ioe) {
            getLogger().debug("Assuming client reset stream");

            this.doCommit = false;
        } catch (Exception e) {
            getLogger().warn("Could not get resource from Database", e);

            this.doCommit = false;

            throw new ResourceNotFoundException("DatabaseReader error:", e);
        }

        return contentLength; // length is unknown
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
    public boolean modifiedSince(ResultSet set, Request request, Response response)
    throws SQLException {
        String lastModified = this.parameters.getParameter("last-modified", null);

        if (lastModified != null) {
            Timestamp modified = set.getTimestamp(lastModified, null);
            this.lastModified = modified.getTime();

            response.setDateHeader("Last-Modified", this.lastModified);

            return modified.getTime() > request.getDateHeader("if-modified-since");
        }

        // if we have nothing to compare to, then we must assume it
        // has been modified
        return true;
    }

    /**
     * This method actually performs the serialization.
     */
    public int serialize(Response response)
    throws IOException, SQLException {
        if (this.resource == null) {
            throw new SQLException("The Blob is empty!");
        }

        InputStream is = new BufferedInputStream(object.getBinaryStream());

        int contentLength = (int) object.length();
        long expires = parameters.getParameterAsInteger("expires", -1);

        if (expires > 0) {
            response.setDateHeader("Expires", new Date().getTime() + expires);
        }

        response.setHeader("Accept-Ranges", "bytes");

        byte[] buffer = new byte[8192];
        int length = -1;

        while ((length = is.read(buffer)) > -1) {
            out.write(buffer, 0, length);
        }
        is.close();
        out.flush();

        return contentLength;
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public long generateKey() {
        return HashUtil.hash(this.source);
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        if (this.lastModified > 0) {
            return new TimeStampCacheValidity(this.lastModified);
        } else {
            return new NOPCacheValidity();
        }
    }

    public void recycle() {
        super.recycle();
        this.resource = null;
        this.lastModified = 0;

        if (this.con != null) {
            if (this.doCommit) {
                this.con.commit();
            } else {
                this.con.rollback();
            }

            try {
                this.con.close();
            } catch (SQLException se) {
                getLogger().warn("Could not close connection", se);
            }

            this.con = null;
        }

        if (this.datasource != null) {
            this.dbselector.release(this.datasource);
            this.datasource = null;
        }
    }

    /**
     * dispose()
     */
    public void dispose()
    {
        if (this.dbselector != null) this.manager.release((Component)this.dbselector);
    }

    public String getMimeType() {
        return this.parameters.getParameter("content-type", super.getMimeType());
    }

}
