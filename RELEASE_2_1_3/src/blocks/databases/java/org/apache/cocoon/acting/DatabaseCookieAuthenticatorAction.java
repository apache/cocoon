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

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.language.markup.xsp.XSPCookieHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  This action is used to authenticate user by comparing several cookie values
 *  (username, password) with the values in database. The description of the
 *  process is given via external xml description file simiar to the one used
 *  for all actions derived from AbstractDatabaseAction. <pre>
 * &lt;root&gt;
 *         &lt;connection&gt;personnel&lt;/connection&gt;
 *         &lt;table name="users_table&gt;
 *                 &lt;select dbcol="username" cookie-name="username"
 *                 to-session="username"/&gt;
 *                 &lt;select dbcol="password" cookie-name="password"
 *                 nullable="yes"/&gt;
 *                 &lt;select dbcol="role" to-session="role" type="string"/&gt;
 *                 &lt;select dbcol="skin" to-session="skin" type="string"/&gt;
 *         &lt;/table&gt;
 * &lt;/root&gt;
 * </pre> The values specified via "cookie-name" describe the name of the
 *  cookie, "dbcol" indicates matching database column, "nullable" means that
 *  cookie-name which is null or empty will not be included in the WHERE clause.
 *  This way you can enable accounts with empty passwords, etc. "to-session"
 *  attribute indicates under which name the value obtained from database should
 *  be stored in the session. Of course new session is created when
 *  authorization is successfull. The "type" attribute can be either string,
 *  long or double and alters the type of object stored in session. Additionally
 *  all parameters that are propagated to the session are made available to the
 *  sitemap via {name} expression. If there is no need to touch the session
 *  object, providing just one-time verification, you can specify action
 *  parameter "create-session" to "no" or "false". No values are then propagated
 *  to the sesion and session object is not verified. If you want to append
 *  attributes to the session without creating a new one, specify action
 *  parameter "append-session" to "yes" or "true".
 *
 * @author <a href="mailto:paolo@arsenio.net">Paolo Scaffardi</a>
 * @version CVS $Id: DatabaseCookieAuthenticatorAction.java,v 1.2 2003/03/24 14:33:57 stefano Exp $
 */
public class DatabaseCookieAuthenticatorAction extends AbstractDatabaseAction implements ThreadSafe {

    /**
     *  Main invocation routine.
     *
     * @param  redirector     Description of Parameter
     * @param  resolver       Description of Parameter
     * @param  objectModel    Description of Parameter
     * @param  src            Description of Parameter
     * @param  parameters     Description of Parameter
     * @return                Description of the Returned Value
     * @exception  Exception  Description of Exception
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src,
            Parameters parameters)
        throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        // read global parameter settings
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;

        if (this.settings.containsKey("reloadable")) {
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        }

        // read local settings
        try {
            Configuration conf = this.getConfiguration(
                    parameters.getParameter("descriptor", (String) this.settings.get("descriptor")),
                    resolver,
                    parameters.getParameterAsBoolean("reloadable", reloadable));
            boolean cs = true;
            boolean as = false;
            String create_session = parameters.getParameter("create-session",
                    (String) this.settings.get("create-session"));
            String
                    append_session = parameters.getParameter("append-session",
                    (String) this.settings.get("append-session"));

            if (create_session != null &&
                    ("no".equals(create_session.trim()) || "false".equals(create_session.trim()))) {
                cs = false;
            }
            if (append_session != null &&
                    ("yes".equals(append_session.trim()) || "true".equals(append_session.trim()))) {
                as = true;
            }

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request req = ObjectModelHelper.getRequest(objectModel);

            /*
             *  check request validity
             */
            if (req == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("DBCOOKIEAUTH: no request object");
                }
                return null;
            }

            String query = this.getAuthQuery(objectModel, conf, req);
            if (query == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("DBCOOKIEAUTH: have not got query");
                }
                req.setAttribute("message", "The authenticator is misconfigured");
                return null;
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("DBCOOKIEAUTH: query is: " + query);
            }
            st = conn.createStatement();
            rs = st.executeQuery(query);

            if (rs.next()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("DBCOOKIEAUTH: authorized successfully");
                }
                Session session = null;

                if (cs) {
                    session = req.getSession(false);
                    if (session != null) {
                        if (as == false) {
                            session.invalidate();
                            session = req.getSession(true);
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("DBCOOKIEAUTH: session invalidated");
                            }
                        }
                    } else {
                        session = req.getSession(true);
                    }

                    if (session == null) {
                        return null;
                    }

                    if (getLogger().isDebugEnabled()) {
                        if (as) {
                            getLogger().debug("DBCOOKIEAUTH: appending to session");
                        } else {
                            getLogger().debug("DBCOOKIEAUTH: session created");
                        }
                    }
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("DBCOOKIEAUTH: leaving session untouched");
                    }
                }

                HashMap actionMap = this.propagateParameters(conf, rs,
                        session);
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return Collections.unmodifiableMap(actionMap);
            }
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }

            req.setAttribute("message", "The username or password were incorrect, please check your CAPS LOCK key and try again.");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("DBCOOKIEAUTH: no results for query");
            }
        } catch (Exception e) {
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) {
                        conn.rollback();
                    }
                } catch (Exception se) {
                    /*
                     *  ignore
                     */
                }
            }
            getLogger().error("Exception: ", e);
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    /*
                     *  ignore
                     */
                }
            }
        }
        return null;
    }


    /**
     *  Gets the authQuery attribute of the DatabaseCookieAuthenticatorAction
     *  object
     *
     * @param  objectModel  Description of Parameter
     * @param  conf         Description of Parameter
     * @param  req          Description of Parameter
     * @return              The authQuery value
     */
    private String getAuthQuery(Map objectModel, Configuration conf, Request req) {
        boolean first_constraint = true;
        StringBuffer queryBuffer = new StringBuffer("SELECT ");
        StringBuffer queryBufferEnd = new StringBuffer("");
        String dbcol;
        String cookie_name;
        String cookie_value;
        String nullstr;
        boolean nullable = false;
        Configuration table = conf.getChild("table");
        Configuration[] select = table.getChildren("select");
        try {
            for (int i = 0; i < select.length; i++) {
                if (i != 0) {
                    queryBuffer.append(", ");
                }
                dbcol = select[i].getAttribute("dbcol");
                queryBuffer.append(dbcol);
                try {
                    cookie_name = select[i].getAttribute("cookie-name");
                    if (cookie_name == null ||
                            cookie_name.trim().equals("")) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                try {
                    nullstr = select[i].getAttribute("nullable");
                    if (nullstr != null) {
                        nullstr = nullstr.trim();
                    }
                    if ("yes".equals(nullstr) || "true".equals(nullstr)) {
                        nullable = true;
                    }
                } catch (Exception e1) {
                }
                /*
                 *  if there is a cookie name,
                 *  but not the value, we exit immediately do
                 *  that authorization fails authomatically
                 */
                cookie_value = XSPCookieHelper.getCookie(objectModel, cookie_name, -1).getValue();

                if (cookie_value == null || cookie_value.trim().equals("")) {
                    // value is null
                    if (!nullable) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("DBCOOKIEAUTH: cookie-name "
                                        + cookie_name + " does not exist");
                        }
                        return null;
                    }
                } else {
                    if (!first_constraint) {
                        queryBufferEnd.append(" AND ");
                    }
                    queryBufferEnd.append(dbcol + "='" + cookie_value + "'");
                    first_constraint = false;
                }
            }
            queryBuffer.append(" FROM ");
            queryBuffer.append(table.getAttribute("name"));
            if (!queryBufferEnd.toString().trim().equals("")) {
                queryBuffer.append(" WHERE ").append(queryBufferEnd);
            }
            return queryBuffer.toString();
        } catch (Exception e) {
            getLogger().error("Exception: ",e);
            return null;
        }
    }


    /**
     *  Description of the Method
     *
     * @param  conf     Description of Parameter
     * @param  rs       Description of Parameter
     * @param  session  Description of Parameter
     * @return          Description of the Returned Value
     */
    private HashMap propagateParameters(Configuration conf, ResultSet rs,
            Session session) {
        Configuration table = conf.getChild("table");
        Configuration[] select = table.getChildren("select");
        String session_param;
        String type;
        HashMap map = new HashMap();
        try {
            for (int i = 0; i < select.length; i++) {
                try {
                    session_param = select[i].getAttribute("to-session");
                    if (session_param != null &&
                            !session_param.trim().equals("")) {
                        String s = rs.getString(i + 1);
                        /*
                         *  propagate to session
                         */
                        try {
                            type = select[i].getAttribute("type");
                        } catch (Exception e) {
                            type = null;
                        }
                        if (type == null || "".equals(type.trim())) {
                            type = "string";
                        }
                        Object o = null;
                        if ("string".equals(type)) {
                            o = s;
                        } else if ("long".equals(type)) {
                            Long l = Long.decode(s);
                            o = l;
                        } else if ("double".equals(type)) {
                            Double d = Double.valueOf(s);
                            o = d;
                        }
                        if (session != null) {
                            session.setAttribute(session_param, o);
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("DBCOOKIEAUTH: propagating param "
                                            + session_param + "=" + s);
                            }
                        }
                        map.put(session_param, o);
                    }
                } catch (Exception e) {
                }
            }
            return map;
        } catch (Exception e) {
            getLogger().error("Exception: ", e);
        }
        return null;
    }
}

