/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Map;
import java.util.Date;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import org.xml.sax.EntityResolver;

import org.apache.avalon.Contextualizable;
import org.apache.avalon.Context;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Roles;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpRequest;
import org.apache.cocoon.generation.ImageDirectoryGenerator;
import org.apache.avalon.util.datasource.DataSourceComponent;

/**
 * Upload an Image to a database.  This handles the portion of a
 * multi-part Form handling that has the image to be uploaded.
 * The parameters specify table, column, and field.  As an added
 * bonus, you can also specify where certain image attributes can
 * also be stored in the Database.
 *
 * The assumptions that this Action makes is that there is a new
 * record for each image, and that the image is all that is inserted
 * at this time.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-02-23 21:36:37 $
 */
public class ImageUploadAction extends ComposerAction implements Contextualizable {
    private final static int SIZE = 0;
    private final static int WIDTH = SIZE + 1;
    private final static int HEIGHT = WIDTH + 1;
    private final static int NUM_PARAMS = HEIGHT + 1;

    private final static String[] PARAM_NAMES = new String[NUM_PARAMS];

    static {
        PARAM_NAMES[SIZE] = "image-size";
        PARAM_NAMES[WIDTH] = "image-width";
        PARAM_NAMES[HEIGHT] = "image-height";
    }

    private ComponentSelector dbselector;
    private String database;
    private File workDir;

    /**
     * Configure the <code>Action</code> so that we can use the same database
     * for all instances.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);


        Configuration connElement = conf.getChild("use-connection");

        try {
            this.dbselector = (ComponentSelector) this.manager.lookup(Roles.DB_CONNECTION);
            this.database = connElement.getValue();
        } catch (ComponentManagerException cme) {
            getLogger().error("Could not get the DataSourceComponentSelector", cme);
            throw new ConfigurationException("Could not get the DataSource ComponentSelector", cme);
        }
    }

    /**
     * Uses the passed <code>Context</code> to find the upload directory.
     * NOTE: This might not be necessary any longer.
     *
     * <pre>
     *   &lt;parameter name="table" value="database_table_name"/&gt;
     *   &lt;parameter name="image" value="database_resource_column_name"/&gt;
     *   &lt;parameter name="file-param" value="request_parameter_that_has_image"/&gt;
     * </pre>
     *
     * Please note that if any of those parameters are missing, this
     * <code>Reader</code> cannot function.  There are a number of other
     * parameters that allow you to provide hints for the reader to
     * optimize resource use:
     *
     * <pre>
     *   &lt;parameter name="image-size" value="database_image_size_column_name"/&gt;
     *   &lt;parameter name="image-width" value="database_image_width_column_name"/&gt;
     *   &lt;parameter name="image-height" value="database_image_height_column_name"/&gt;
     *   &lt;parameter name="key" value="database_lookup_key_column_name"/&gt;
     *   &lt;parameter name="last-modified" value="database_timestamp_column_name"/&gt;
     * </pre>
     *
     * Lastly, the <code>key</code> value is derived from the value of
     * the <code>source</code> string.
     *
     * The image attributes are automatically derived from the image itself,
     * but only included in the database if the attributes are set to
     * column names in the table.
     */
    public void contextualize(Context context) {
        this.workDir = new File((File) context.get(Constants.CONTEXT_WORK_DIR), "image-dir" + File.separator);
    }

    /**
     * Process the request and populate the database records with the
     * uploaded image and it's associated attributes.  Please note that
     * there are several required parameters to use this action:
     */
    public Map act(EntityResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = (DataSourceComponent) this.dbselector.select(this.database);
        Connection conn = datasource.getConnection();
        HttpRequest request = (HttpRequest) objectModel.get(Constants.REQUEST_OBJECT);

        String table = param.getParameter("table", null);
        String column = param.getParameter("image", null);
        String keycol = param.getParameter("key", null);
        String lastModified = param.getParameter("last-modified", null);

        if (table == null || column == null) {
            throw new ProcessingException("Cannot insert into a null table/column");
        }

        String fileParam = param.getParameter("file-param", null);

        if (fileParam == null) {
            throw new ProcessingException("Cannot extract a null parameter");
        }

        int[] paramPositions = new int[ImageUploadAction.NUM_PARAMS];
        String[] paramColumns = new String[ImageUploadAction.NUM_PARAMS];

        for (int i = 0; i < ImageUploadAction.NUM_PARAMS; i++) {
            paramColumns[i] = param.getParameter(ImageUploadAction.PARAM_NAMES[i], null);
        }

        String query;

        if (keycol == null || source == null) {
            query = this.setupInsertQuery(table, column, paramColumns, paramPositions, lastModified);
        } else {
            query = this.setupUpdateQuery(table, column, paramColumns, paramPositions, lastModified, keycol, source);
        }

        File image = null;

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            int [] paramValues = new int[ImageUploadAction.NUM_PARAMS];

            image = (File) request.get(fileParam);
            paramValues[ImageUploadAction.SIZE] = (int) image.length();
            int [] dimensions = ImageDirectoryGenerator.getSize(image);
            paramValues[ImageUploadAction.WIDTH] = dimensions[0];
            paramValues[ImageUploadAction.HEIGHT] = dimensions[1];

            statement.setBinaryStream(1, new FileInputStream(image), paramValues[ImageUploadAction.SIZE]);

            int maxIndex = 1;
            for (int i = 0; i < ImageUploadAction.NUM_PARAMS; i++) {
                if (paramPositions[i] > 0) {
                    statement.setInt(paramPositions[i], paramValues[i]);
                    maxIndex = Math.max(paramPositions[i], maxIndex);
                }
            }

            if (lastModified != null) {
                maxIndex++;
                statement.setTimestamp(maxIndex, new Timestamp(new Date().getTime()));
            }

            if (keycol != null && source != null) {
                statement.setString(maxIndex + 1, source);
            }

            statement.execute();
        } catch (Exception e) {
            getLogger().warn("Could not commit file: " + query, e);
        } finally {
            if (image != null) image.delete();

            try {
                conn.close();
            } catch (SQLException sqe) {
                getLogger().warn("Could not close connection", sqe);
            }

            this.dbselector.release(datasource);
        }

        return null;
    }

    /**
     * Sets up the query string to insert the data into a database.
     * The query assumes all information is in one table, and that
     * the indexes are derived automatically.
     */
    String setupInsertQuery(String table, String column, String[] paramNames, int[] paramPositions, String lastModified) {
        StringBuffer query = new StringBuffer("INSERT INTO ");
        query.append(table).append(" (").append(column);

        for (int i = 0; i < ImageUploadAction.NUM_PARAMS; i++) {
            if (paramNames[i] != null) {
                query.append(", ").append(paramNames[i]);
            }
        }

        if (lastModified != null) {
            query.append(", ").append(lastModified);
        }

        query.append(") VALUES (?");

        int index = 2;

        for (int i = 0; i < ImageUploadAction.NUM_PARAMS; i++) {
            if (paramNames[i] != null) {
                query.append(", ?");
                paramPositions[i] = index;
                index++;
            } else {
                paramPositions[i] = -1;
            }
        }

        if (lastModified != null) {
            query.append(", ?");
        }

        query.append(")");

        return query.toString();
    }

    /**
     * Sets up the query to update an existing image.  The query assumes
     * that all the information resides in the same table, and that the
     * key is not a compound key.
     */
    String setupUpdateQuery(String table, String column, String[] paramNames, int[] paramPositions, String lastModified, String keyColumn, String key) {
        StringBuffer query = new StringBuffer("UPDATE ");
        query.append(table).append(" SET ").append(column).append(" = ?");
        int index = 2;

        for (int i = 0; i < ImageUploadAction.NUM_PARAMS; i++) {
            if (paramNames[i] != null) {
                query.append(", ").append(paramNames[i]).append(" = ?");
                paramPositions[i] = index;
                index++;
            } else {
                paramPositions[i] = -1;
            }
        }

        if (lastModified != null) {
            query.append(", ").append(lastModified).append(" = ?");
        }

        query.append(" WHERE ").append(keyColumn).append(" = ?");

        return query.toString();
    }
}