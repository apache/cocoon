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
package org.apache.cocoon.reading;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.xml.sax.SAXException;

/**
 * This Reader pulls a resource from a database.  It is configured with
 * the Connection to use, parameters specify the table and column
 * to pull the image from, and source specifies the source key information.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: DatabaseReader.java,v 1.5 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class DatabaseReader extends ServiceableReader
    implements Configurable, Disposable, CacheableProcessingComponent
{

    private ServiceSelector dbselector;
    private String dsn;
    private long lastModified = System.currentTimeMillis();
    private InputStream resource = null; // because HSQL doesn't yet implement getBlob()
    private Connection con = null;
    private DataSourceComponent datasource = null;
    private String mimeType = null;
    private int typeColumn = 0;
    private boolean doCommit = false;
    private boolean defaultCache = true;

    public void service(final ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.dbselector = (ServiceSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
    }

    /**
     * Configure the <code>Reader</code> so that we can use the same database
     * for all instances.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.dsn = conf.getChild("use-connection").getValue();
        this.defaultCache = conf.getChild("invalidate").getValue("never").equals("always");
    }

    /**
     * Set the <code>SourceResolver</code> the object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        try {
            this.datasource = (DataSourceComponent) dbselector.select(dsn);
            this.con = datasource.getConnection();

            if (this.con.getAutoCommit()) {
                this.con.setAutoCommit(false);
            }

            PreparedStatement statement = con.prepareStatement(getQuery());
            statement.setString(1, this.source);
            ResultSet set = statement.executeQuery();
            if (!set.next()) throw new ResourceNotFoundException("There is no resource with that key");

            Response response = ObjectModelHelper.getResponse(objectModel);
            Request request = ObjectModelHelper.getRequest(objectModel);

            if (this.modifiedSince(set, request, response)) {
                this.resource = set.getBinaryStream(1);
                if (this.typeColumn != 0) {
                    this.mimeType = set.getString(this.typeColumn);
                }

                if (this.resource == null) {
                    throw new ResourceNotFoundException("There is no resource with that key");
                }
            }

            this.doCommit = true;
        } catch (Exception e) {
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
     *   &lt;parameter name="type-column" value="database_content_mime_type_column"/&gt;
     *   &lt;parameter name="expires" value="number_of_millis_before_refresh"/&gt;
     *   &lt;parameter name="where" value="alternate_key = 'foo'"/&gt;
     *   &lt;parameter name="order-by" value="alternate_key DESC"/&gt;
     * </pre>
     *
     * Lastly, the <code>key</code> value is derived from the value of
     * the <code>source</code> string.
     */
    public void generate() throws ProcessingException, SAXException, IOException {
        try {
            Response response = ObjectModelHelper.getResponse(objectModel);
            this.serialize(response);
        } catch (IOException ioe) {
            getLogger().warn("Assuming client reset stream");

            this.doCommit = false;
        } catch (Exception e) {
            this.doCommit = false;

            throw new ResourceNotFoundException("DatabaseReader error:", e);
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
        String where = this.parameters.getParameter("where", null);
        String orderBy = this.parameters.getParameter("order-by", null);
        String typeColumn = this.parameters.getParameter("type-column", null);
        int columnNo = 1;

        if (table == null || column == null || key==null) {
            throw new ProcessingException("We are missing a required parameter.  Please include 'table', 'image', and 'key'");
        }

        String date = this.parameters.getParameter("last-modified", null);
        StringBuffer query = new StringBuffer("SELECT ");

        query.append(column);
        columnNo++;

        if (date != null) {
            query.append(", ").append(date);
            columnNo++;
        }

        if (null != orderBy) {
            query.append(", ");

            if (orderBy.endsWith(" DESC")) {
                query.append(orderBy.substring(0, orderBy.length() - 5));
            } else {
                query.append(orderBy);
            }
            columnNo++;
        }

        if (null != typeColumn) {
            query.append(", ").append(typeColumn);
            this.typeColumn = columnNo;
            columnNo++;
        }

        query.append(" FROM ").append(table);
        query.append(" WHERE ").append(key).append(" = ?");

        if (null != where) {
            query.append(" AND ").append(where);
        }

        if (null != orderBy) {
            query.append(" ORDER BY ").append(orderBy);
        }

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
            if (null != modified) {
                this.lastModified = modified.getTime();
            } else {
                // assume it has never been modified
            }

            response.setDateHeader("Last-Modified", this.lastModified);

            return this.lastModified > request.getDateHeader("if-modified-since");
        }

        // if we have nothing to compare to, then we must assume it
        // has been modified
        return true;
    }

    /**
     * This method actually performs the serialization.
     */
    public void serialize(Response response)
    throws IOException, SQLException {
        if (this.resource == null) {
            throw new SQLException("The Blob is empty!");
        }

        InputStream is = new BufferedInputStream(this.resource);

        long expires = parameters.getParameterAsInteger("expires", -1);

        if (expires > 0) {
            response.setDateHeader("Expires", System.currentTimeMillis() + expires);
        }

        response.setHeader("Accept-Ranges", "bytes");

        byte[] buffer = new byte[8192];
        int length = -1;

        while ((length = is.read(buffer)) > -1) {
            out.write(buffer, 0, length);
        }
        is.close();
        out.flush();
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return this.source;
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        if (this.lastModified > 0) {
            return new TimeStampValidity(this.lastModified);
        } else {
            if (this.defaultCache) {
                return NOPValidity.SHARED_INSTANCE;
            } else {
                return null;
            }
        }
    }

    public void recycle() {
        super.recycle();
        this.resource = null;
        this.lastModified = 0;
        this.mimeType = null;
        this.typeColumn = 0;

        if (this.con != null) {
            try {
                if (this.doCommit) {
                    this.con.commit();
                } else {
                    this.con.rollback();
                }
            } catch (SQLException se) {
                getLogger().warn("Could not commit or rollback connection", se);
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
        this.manager.release(this.dbselector);
    }

    public String getMimeType() {
        return (this.mimeType != null ? 
                this.mimeType : 
                this.parameters.getParameter("content-type", super.getMimeType()));
    }

}
