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
package org.apache.cocoon.acting;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This action is used to authenticate user by comparing several request
 * fields (username, password) with the values in database. The description of
 * the process is given via external xml description file simiar to the one
 * used for all actions derived from AbstractDatabaseAction.
 * <pre>
 * &lt;root&gt;
 *         &lt;connection&gt;personnel&lt;/connection&gt;
 *         &lt;table name="users_table&gt;
 *                 &lt;select dbcol="username" request-param="username"
 *                 to-session="username"/&gt;
 *                 &lt;select dbcol="password" request-param="password"
 *                 nullable="yes"/&gt;
 *                 &lt;select dbcol="role" to-session="role" type="string"/&gt;
 *                 &lt;select dbcol="skin" to-session="skin" type="string"/&gt;
 *         &lt;/table&gt;
 * &lt;/root&gt;
 * </pre>
 * The values specified via "request-param" describe the name of HTTP request
 * parameter, "dbcol" indicates matching database column, "nullable" means
 * that request-param which is null or empty will not be included in the WHERE
 * clause. This way you can enable accounts with empty passwords, etc.
 * "to-session" attribute indicates under which name the value obtained from
 * database should be stored in the session. Of course new session is created
 * when authorization is successfull. The "type" attribute can be either
 * string, long or double and alters the type of object stored in session.
 * Additionally all parameters that are
 * propagated to the session are made available to the sitemap via {name}
 * expression.
 *
 * If there is no need to touch the session object, providing just one-time
 * verification, you can specify action parameter "create-session" to "no" or
 * "false". No values are then propagated to the sesion and session object is
 * not verified.
 *
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @version CVS $Id: DatabaseAuthenticatorAction.java,v 1.7 2004/03/30 05:50:48 antonio Exp $
 */
public class DatabaseAuthenticatorAction extends AbstractDatabaseAction implements ThreadSafe
{
    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        // read global parameter settings
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;

        if (this.settings.containsKey("reloadable")) {
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        }

        // read local settings
        try {
            Configuration conf = this.getConfiguration (
                    parameters.getParameter ("descriptor", (String) this.settings.get("descriptor")),
            resolver,
            parameters.getParameterAsBoolean("reloadable",reloadable));
            boolean cs = true;
            String create_session = parameters.getParameter ("create-session",
                                 (String) this.settings.get("create-session"));
            if (create_session != null) {
                cs = BooleanUtils.toBoolean(create_session.trim());
             }

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request req = ObjectModelHelper.getRequest(objectModel);

            /* check request validity */
            if (req == null) {
                getLogger ().debug ("DBAUTH: no request object");
                return null;
            }

            st = this.getAuthQuery (conn, conf, req);
            if (st == null) {
                getLogger ().debug ("DBAUTH: have not got query");
                req.setAttribute("message", "The authenticator is misconfigured");
                return null;
            }

            rs = st.executeQuery ();

            if (rs.next ()) {
                getLogger ().debug ("DBAUTH: authorized successfully");
                Session session = null;

                if (cs) {
                    session = req.getSession (false);
                    if (session != null)
                        session.invalidate ();
                    session = req.getSession (true);
                    if (session == null)
                        return null;
                    getLogger ().debug ("DBAUTH: session created");
                } else {
                    getLogger ().debug ("DBAUTH: leaving session untouched");
                }

                HashMap actionMap = this.propagateParameters (conf, rs,
                        session);
                if(!conn.getAutoCommit()) {
                    conn.commit();
                }
                return Collections.unmodifiableMap (actionMap);
            }
            if(!conn.getAutoCommit()) {
                conn.rollback();
            }

            req.setAttribute("message", "The username or password were incorrect, please check your CAPS LOCK key and try again.");
            getLogger ().debug ("DBAUTH: no results for query");
        } catch (Exception e) {
            if (conn != null) {
                try {
                    if(!conn.getAutoCommit()) {
                        conn.rollback();
                    }
                } catch (Exception se) {/* ignore */}
            }
            getLogger().debug ("exception: ", e);
            return null;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {/* ignore */}
            }
        }
        return null;
    }

    private PreparedStatement getAuthQuery(Connection conn, Configuration conf, Request req) {
        StringBuffer queryBuffer = new StringBuffer("SELECT ");
        StringBuffer queryBufferEnd = new StringBuffer("");
        Configuration table = conf.getChild("table");
        Configuration[] columns = table.getChildren("select");
        try {
            Object[] constraintValues = new Object[columns.length];
            int constraints = 0;
            for (int i = 0; i < columns.length; i++) {
                String dbcol = columns[i].getAttribute("dbcol");
                boolean nullable = false;
                if (i > 0) {
                    queryBuffer.append (", ");
                }
                queryBuffer.append(dbcol);

                String requestParameter = columns[i].getAttribute("request-param", null);
                if (StringUtils.isNotBlank(requestParameter)) {
                    String nullstr = columns[i].getAttribute("nullable", null);
                    if (nullstr != null) {
                        nullable = BooleanUtils.toBoolean(nullstr.trim());
                    }
                    String constraintValue = req.getParameter(requestParameter);

                    // if there is a request parameter name,
                    // but not the value, we exit immediately do
                    // that authorization fails authomatically
                    if (StringUtils.isBlank(constraintValue) && !nullable) {
                        getLogger().debug("DBAUTH: request-param " + requestParameter + " does not exist");
                        return null;
                    }
                    if (constraints > 0) {
                        queryBufferEnd.append(" AND ");
                    }
                    queryBufferEnd.append(dbcol).append("= ?");
                    constraintValues[constraints++] = constraintValue;
                }
            }

            queryBuffer.append(" FROM ");
            queryBuffer.append(table.getAttribute("name"));
            if (StringUtils.isNotBlank(queryBufferEnd.toString())) {
                queryBuffer.append(" WHERE ").append(queryBufferEnd);
            }

            getLogger().debug("DBAUTH: query " + queryBuffer);

            PreparedStatement st = conn.prepareStatement(queryBuffer.toString());

            for (int i = 0; i < constraints; i++) {
                getLogger().debug("DBAUTH: parameter " + (i+1) + " = [" + String.valueOf(constraintValues[i]) + "]");
                st.setObject(i+1,constraintValues[i]);
            }
            return st;
        }
        catch (Exception e) {
            getLogger().debug("DBAUTH: got exception: " + e);
        }
        return null;
    }

    private HashMap propagateParameters (Configuration conf, ResultSet rs,
            Session session) {
        Configuration table = conf.getChild ("table");
        Configuration[] select = table.getChildren ("select");
        String session_param, type;
        HashMap map = new HashMap();
        try {
            for (int i = 0; i < select.length; i ++) {
                try {
                    session_param = select[i].getAttribute ("to-session");
                    if (StringUtils.isNotBlank(session_param)) {
                        Object o = null;
                        String s = rs.getString (i + 1);
                        /* propagate to session */
                            type = select[i].getAttribute("type", "");
                        if (StringUtils.isBlank(type) || "string".equals (type)) {
                            o = s;
                        } else if ("long".equals (type)) {
                            Long l = Long.decode (s);
                            o = l;
                        } else if ("double".equals (type)) {
                            Double d = Double.valueOf (s);
                            o = d;
                        }
                        if (session != null) {
                            session.setAttribute (session_param, o);
                            getLogger ().debug ("DBAUTH: propagating param "
                                    + session_param + "=" + s);
                        }
                        map.put (session_param, o);
                    }
                } catch (Exception e) {
                    // Empty
                }
            }
            return map;
        } catch (Exception e) {
            getLogger().debug("exception: ", e);
        }
        return null;
    }
}
