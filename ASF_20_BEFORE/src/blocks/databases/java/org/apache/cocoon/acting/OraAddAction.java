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
package org.apache.cocoon.acting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.ImageProperties;
import org.apache.cocoon.util.ImageUtils;

/**
 * Add a record in a database.  This Action assumes that there is
 * only one table at a time to update.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: OraAddAction.java,v 1.2 2004/03/01 03:50:58 antonio Exp $
 */
public class OraAddAction extends DatabaseAddAction {
    private static final Map selectLOBStatements = new HashMap();

    /**
     * Add a record to the database.  This action assumes that
     * the file referenced by the "descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        int currentIndex = 0;

        try {
            Configuration conf = this.getConfiguration(param.getParameter("descriptor", null));
            String query = this.getAddQuery(conf);

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request request = ObjectModelHelper.getRequest(objectModel);

            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }

            PreparedStatement statement = conn.prepareStatement(query);
            getLogger().info(query);

            Configuration[] keys = conf.getChild("table").getChild("keys").getChildren("key");
            Configuration[] values = conf.getChild("table").getChild("values").getChildren("value");
            currentIndex = 1;

            // Insert the keys into the query
            for (int i = 0; i < keys.length; i++) {
                String mode = keys[i].getAttribute("mode", "automatic");

                if ("manual".equals(mode)) {
                    String selectQuery = this.getSelectQuery(keys[i]);

                    ResultSet set = conn.createStatement().executeQuery(selectQuery);
                    set.next();
                    int value = set.getInt("maxid") + 1;

                    statement.setInt(currentIndex, value);

                    request.setAttribute(keys[i].getAttribute("param"), String.valueOf(value));

                    set.close();
                    set.getStatement().close();
                    currentIndex++;
                } else if ("form".equals(mode)) {
                    String parameter = keys[i].getAttribute("param");
                    request.setAttribute(parameter, request.getParameter(parameter));
                    this.setColumn(statement, currentIndex, request, keys[i]);
                    currentIndex++;
                }
            }

            // insert the values into the query
            for (int i = 0; i < values.length; i++) {
                String type = values[i].getAttribute("type");
                String parameter = values[i].getAttribute("param");

                if (type.equals("image")) {
                    File binaryFile = (File) request.get(parameter);
                    Parameters iparam = new Parameters();

                    iparam.setParameter("image-size", String.valueOf(binaryFile.length()));

                    ImageProperties prop = ImageUtils.getImageProperties(binaryFile);
                    iparam.setParameter("image-width", Integer.toString(prop.width));
                    iparam.setParameter("image-height", Integer.toString(prop.height));

                    synchronized (this.files) {
                        this.files.put(binaryFile, param);
                    }
                }

                if (! this.isLargeObject(type)) {
                    this.setColumn(statement, currentIndex, request, values[i]);
                    currentIndex++;
                }
            }

            statement.execute();
            statement.close();

            query = this.getSelectLOBQuery(conf);

            // Process the large objects if they exist
            if (query != null) {
                PreparedStatement LOBstatement = conn.prepareStatement(query);
                getLogger().info(query);

                if (keys.length > 0) {
                    currentIndex = 1;

                    for (int i = 0; i < keys.length; i++) {
                        this.setColumn(LOBstatement, currentIndex, request, keys[i]);
                        currentIndex++;
                    }
                }

                OracleResultSet set = (OracleResultSet) LOBstatement.executeQuery();

                if (set.next()) {
                    int index = 0;

                    for (int i = 0; i < values.length; i++) {
                        String type = values[i].getAttribute("type", "");
                        if (this.isLargeObject(type)) {
                            Object attr = request.get(values[i].getAttribute("param"));
                            int length = -1;
                            InputStream stream = null;
                            OutputStream output = null;
                            int bufSize = 1024;

                            index++;

                            if (type.equals("ascii")) {
                                CLOB ascii = set.getCLOB(index);

                                if (attr instanceof File) {
                                    File asciiFile = (File) attr;
                                    stream = new BufferedInputStream(new FileInputStream(asciiFile));
                                } else {
                                    String asciiText = (String) attr;
                                    stream = new BufferedInputStream(new ByteArrayInputStream(asciiText.getBytes()));
                                }

                                output = new BufferedOutputStream(ascii.getAsciiOutputStream());
                                bufSize = ascii.getBufferSize();
                            } else {
                                BLOB binary = set.getBLOB(index);
                                File binaryFile = (File) attr;
                                stream = new BufferedInputStream(new FileInputStream(binaryFile));
                                length = (int) binaryFile.length();

                                output = new BufferedOutputStream(binary.getBinaryOutputStream());
                                bufSize = binary.getBufferSize();
                            }

                            byte[] buffer = new byte[bufSize];
                            while ((length = stream.read(buffer)) != -1) {
                                output.write(buffer, 0, length);
                            }

                            stream.close();
                            output.close();
                        }
                    }
                }

                set.close();
                set.getStatement().close();
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    getLogger().debug("There was an error rolling back the transaction", se);
                }
            }

            throw new ProcessingException("Could not add record :position = " + (currentIndex - 1), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqe) {
                    getLogger().warn("There was an error closing the datasource", sqe);
                }
            }

            if (datasource != null) this.dbselector.release(datasource);
        }

        return null;
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    protected final String getAddQuery(Configuration conf) throws ConfigurationException {
        String query = null;

        synchronized (DatabaseAddAction.addStatements) {
            query = (String) DatabaseAddAction.addStatements.get(conf);

            if (query == null) {
                Configuration table = conf.getChild("table");
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");

                StringBuffer queryBuffer = new StringBuffer("INSERT INTO ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" (");

                int numKeys = 0;

                for (int i = 0; i < keys.length; i++) {
                    String mode = keys[i].getAttribute("mode", "automatic");
                    if (numKeys > 0) {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append(keys[i].getAttribute("dbcol"));

                    if ("manual".equals(mode)) {
                        this.setSelectQuery(table.getAttribute("name"), keys[i]);
                    }

                    numKeys++;
                }

                for (int i = 0; i < values.length; i++) {
                    if ((numKeys + i) > 0) {
                        queryBuffer.append(", ");
                    }

                    queryBuffer.append(values[i].getAttribute("dbcol"));
                }

                queryBuffer.append(") VALUES (");

                numKeys = 0;
                ArrayList sequences = new ArrayList();

                for (int i = 0; i < keys.length; i++) {
                    if (numKeys > 0) queryBuffer.append(", ");
                    if ("automatic".equals(keys[i].getAttribute("mode", "automatic"))) {
                        String sequence = keys[i].getAttribute("sequence", "");
                        queryBuffer.append(sequence);

                        if (sequences.contains(sequence)) {
                            queryBuffer.append(".CURRVAL");
                        } else {
                            sequences.add(sequence);
                            queryBuffer.append(".NEXTVAL");
                        }

                        numKeys++;
                    } else {
                        queryBuffer.append("?");
                        numKeys++;
                    }
                }

                for (int i = 0; i < values.length; i++) {
                    if ((numKeys + i) > 0) {
                        queryBuffer.append(", ");
                    }

                    if (this.isLargeObject(values[i].getAttribute("type"))) {
                        if (values[i].getAttribute("type").equals("ascii")) {
                             queryBuffer.append("empty_clob()");
                        } else {
                             queryBuffer.append("empty_blob()");
                        }
                    } else {
                        queryBuffer.append("?");
                    }
                }

                queryBuffer.append(")");

                query = queryBuffer.toString();

                DatabaseAddAction.addStatements.put(conf, query);
            }
        }

        if ("".equals(query)) return null;

        return query;
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    private final String getSelectLOBQuery (Configuration conf)
    throws ConfigurationException {
        String query = null;

        synchronized (OraAddAction.selectLOBStatements) {
            query = (String) OraAddAction.selectLOBStatements.get(conf);

            // executes only if query is null.
            if (query == null) {
                StringBuffer queryBuffer = new StringBuffer("SELECT ");
                Configuration table = conf.getChild("table");
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");
                int numLobs = 0;

                // Add the values to the query
                for (int i = 0; i < values.length; i++) {
                    if (this.isLargeObject(values[i].getAttribute("type"))) {
                        numLobs++;

                        if (numLobs > 1) {
                            queryBuffer.append(", ");
                        }

                        queryBuffer.append(values[i].getAttribute("dbcol"));
                    }
                }

                if (numLobs < 1) {
                    // if query is set to "", then the Action won't
                    // try to process it again.
                    query = "";
                    OraAddAction.selectLOBStatements.put(conf, query);
                    return null;
                }

                queryBuffer.append(" FROM ").append(table.getAttribute("name"));

                // Process the WHERE clause
                if (keys.length > 0) {
                    queryBuffer.append(" WHERE ");

                    // Add the keys to the query
                    for (int i = 0; i < keys.length; i++) {
                        if (i > 0) {
                            queryBuffer.append(" AND ");
                        }

                        queryBuffer.append(keys[i].getAttribute("dbcol"));
                        queryBuffer.append(" = ?");
                    }
                }

                query = queryBuffer.toString().trim();
                OraAddAction.selectLOBStatements.put(conf, query);
            }
        }

        if ("".equals(query)) return null;

        return query;
    }
}
