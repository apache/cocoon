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
package org.apache.cocoon.acting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

/**
 * Adds record in a database. The action can update one or more tables,
 * and can add more than one row to a table at a time. The form descriptor
 * semantics for this are still in a bit of a state of flux. Note
 * that if a secondary table relies on the value of a new primary key in a
 * primary table, the primary key must be created using manual mode.
 *
 * @version $Id$
 */
public class DatabaseAddAction extends AbstractDatabaseAction implements ThreadSafe {
    protected static final Map addStatements = new HashMap();
    private static final Map selectStatements = new HashMap();

    /**
     * Add a record to the database.  This action assumes that
     * the file referenced by the "descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        Map results = new HashMap();

        // read global parameter settings
        boolean reloadable = DESCRIPTOR_RELOADABLE_DEFAULT;
        if (this.settings.containsKey("reloadable"))
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        // read local parameter settings
        try {
            Configuration conf =
                this.getConfiguration(param.getParameter("descriptor", (String) this.settings.get("descriptor")), resolver,
                                      param.getParameterAsBoolean("reloadable",reloadable));

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request request = ObjectModelHelper.getRequest(objectModel);

            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }

            Configuration[] tables = conf.getChildren("table");
            for (int i=0; i<tables.length; i++) {
              Configuration table = tables[i];
              processTable(table,conn,request,results);
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

            //throw new ProcessingException("Could not add record :position = " + currentIndex, e);
            throw new ProcessingException("Could not add record",e);
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

        return Collections.unmodifiableMap(results);
    }

    /**
     * Inserts a row or a set of rows into the given table based on the
     * request parameters
     *
     * @param table the table's configuration
     * @param conn the database connection
     * @param request the request
     */
    void processTable(Configuration table, Connection conn, Request request, Map results) throws SQLException,ConfigurationException,Exception {
      PreparedStatement statement = null;
      try {
        String query = this.getAddQuery(table);
        getLogger().debug("Add query: "+query);
        statement = conn.prepareStatement(query);
        Configuration[] keys = table.getChild("keys").getChildren("key");
        Configuration[] values = table.getChild("values").getChildren("value");
        int currentIndex = 1;
        boolean manyrows = false;
        int wildcardIndex = -1;
        String wildcardParam = null;
        for (int i=0; i<keys.length; i++) {
          wildcardParam = keys[i].getAttribute("param");
          if ((wildcardIndex = wildcardParam.indexOf('*')) != -1) {
            manyrows = true;
            break;
          }
        }
        if (manyrows) {
          /**
           * This table has a column with a wildcard, so we're going
           * to be inserting n rows, where 0 <= n
           */
          String prefix = wildcardParam.substring(0,wildcardIndex);
          String suffix = StringUtils.substring(wildcardParam, wildcardIndex + 1);
          Enumeration names = request.getParameterNames();
          SortedSet matchset = new TreeSet();
          int prefixLength = prefix.length();
          int length = prefixLength + suffix.length();
          while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            if (name.startsWith(prefix) && name.endsWith(suffix)) {
              String wildcard = StringUtils.mid(name, prefixLength, name.length() - length);
              matchset.add(wildcard);
            }
          }
          int rowIndex = 1;
          Iterator iterator = matchset.iterator();
          while (iterator.hasNext()) {
            String wildcard = (String)iterator.next();
            currentIndex = 1;
            for (int j=0; j<keys.length; j++) {
              String myparam = getActualParam(keys[j].getAttribute("param"),wildcard);
              currentIndex += setKey(table,keys[j],conn,statement,currentIndex,request,myparam,results);
            }
            for (int j=0; j<values.length; j++) {
              String myparam = getActualParam(values[j].getAttribute("param"),wildcard);
              this.setColumn(statement,currentIndex,request,values[j],myparam,request.getParameter(myparam),rowIndex);
              currentIndex++;
            }
            statement.execute();
            rowIndex++;
          }
        } else {
          /**
           * This table has no wildcard columns, so we're going to
           * be inserting 1 row.
           */
          for (int i = 0; i < keys.length; i++) {
            currentIndex += setKey(table,keys[i],conn,statement,currentIndex,request,keys[i].getAttribute("param",""),results);
          }
          for (int i = 0; i < values.length; i++, currentIndex++) {
            this.setColumn(statement, currentIndex, request, values[i]);
          }
          statement.execute();
          /** Done processing table **/
        }
      } finally {
        try {
          if (statement != null) {
            statement.close();
          }
        } catch (SQLException e) {}
      }
    }

    /**
     * Sets the key value on the prepared statement. There are four modes:
     *
     * <dl>
     *   <dt>automatic (default)</dt>
     *   <dd>let the database automatically create the key. note this
     *       prohibits the action from storing the key value anywhere.</dd>
     *   <dt>manual</dt>
     *   <dd>create the key value using SELECT(dbcol)+1 from TABLE</dd>
     *   <dt>form</dt>
     *   <dd>look for the key value in the request parameters</dd>
     *   <dt>request-attribute</dt>
     *   <dd>look for the key value in the request attributes</dd>
     * </dl>
     *
     * This method has a couple of side effects. If the mode is manual,
     * the key value is stored in the request object's attributes for use
     * by other inserts. The key is the string "key:TABLENAME:DBCOL".
     * This method also puts the value of manually created keys in the results
     * map. That key is simply the value of the dbcol attribute. Note this
     * stuff is definitely up in the air.
     *
     * @param table the table's configuration object
     * @param key the key's configuration object
     * @param conn the database connection
     * @param statement the insert statement
     * @param currentIndex the position of the key column
     * @param request the request object
     * @param param the actual name of the request parameter
     * @return the number of columns by which to increment the currentIndex
     */
    int setKey(Configuration table, Configuration key, Connection conn, PreparedStatement statement, int currentIndex, Request request, String param, Map results) throws ConfigurationException, SQLException,Exception {
      String mode = key.getAttribute("mode","automatic");
      String keyname = new StringBuffer("key:").append(table.getAttribute("name"))
                           .append(':').append(key.getAttribute("dbcol")).toString();
      if ("manual".equals(mode)) {
        // Set the key value using SELECT MAX(keyname)+1
        String selectQuery = this.getSelectQuery(key);
        PreparedStatement select_statement = conn.prepareStatement(selectQuery);
        ResultSet set = select_statement.executeQuery();
        set.next();
        int value = set.getInt("maxid") + 1;
        statement.setInt(currentIndex, value);
        getLogger().debug("Manually setting key to " + value);
        setRequestAttribute(request,keyname,new Integer(value));
        results.put(key.getAttribute("dbcol"),String.valueOf(value));
        set.close();
        select_statement.close();
      } else if ("form".equals(mode)) {
        // Set the key value from the request
        getLogger().debug("Setting key from form");
        this.setColumn(statement, currentIndex, request, key, param);
      } else if ("request-attribute".equals(mode)) {
        Integer value = (Integer)getRequestAttribute(request,key.getAttribute("request-attribute-name"));
        getLogger().debug("Setting key from request attribute "+value);
        statement.setInt(currentIndex,value.intValue());
      } else {
        getLogger().debug("Automatically setting key");
        // The database automatically creates a key value
        return 0;
      }
      return 1;
    }

    /**
     * Returns the actual name of the parameter. If the name contains
     * no wildcard, the param is returned untouched, otherwise the
     * wildcard value is substituted for the * character. This probably
     * doesn't deserve a method unto itself, but I can imagine wanting
     * to use a more sophisticated matching and substitution algorithm.
     *
     * @param param the name of the parameter, possibly with a wildcard char
     * @param wildcard the wildcard value
     * @return the actual name of the parameter
     */
    String getActualParam(String param, String wildcard) {
      int index;
      if ((index = param.indexOf('*')) != -1) {
        return param.substring(0,index)+wildcard+param.substring(index+1);
      } else {
        return param;
      }
    }

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     *
     * @param table the table's configuration object
     * @return the insert query as a string
     */
    protected String getAddQuery(Configuration table) throws ConfigurationException {
        String query = null;
        synchronized (DatabaseAddAction.addStatements) {
            query = (String) DatabaseAddAction.addStatements.get(table);
            if (query == null) {
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");

                StringBuffer queryBuffer = new StringBuffer("INSERT INTO ");
                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" (");

                int numParams = 0;

                for (int i = 0; i < keys.length; i++) {
                    String mode = keys[i].getAttribute("mode", "automatic");
                    if ("manual".equals(mode) || "form".equals(mode) || "request-attribute".equals(mode)) {
                        if (numParams > 0) {
                            queryBuffer.append(", ");
                        }
                        queryBuffer.append(keys[i].getAttribute("dbcol"));
                        this.setSelectQuery(table.getAttribute("name"), keys[i]);
                        numParams++;
                    }
                }
                queryBuffer.append(buildList(values, numParams));
                numParams += values.length;
                queryBuffer.append(") VALUES (");
                if (numParams > 0) {
                    queryBuffer.append("?");
                    queryBuffer.append(StringUtils.repeat(", ?", numParams - 1));
                }
                queryBuffer.append(")");
                query = queryBuffer.toString();

                DatabaseAddAction.addStatements.put(table, query);
            }
        }
        return query;
    }

    /**
     * Set the String representation of the MaxID lookup statement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     */
    protected final synchronized void setSelectQuery(String tableName, Configuration entry) throws ConfigurationException {
        StringBuffer queryBuffer = new StringBuffer("SELECT max(");
        queryBuffer.append(entry.getAttribute("dbcol"));
        queryBuffer.append(") AS maxid FROM ");
        queryBuffer.append(tableName);

        DatabaseAddAction.selectStatements.put(entry, queryBuffer.toString());
    }

    protected final synchronized String getSelectQuery(Configuration entry) throws ConfigurationException {
        return (String) DatabaseAddAction.selectStatements.get(entry);
    }
}
