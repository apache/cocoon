// $Id: DatabaseAuthenticatorAction.java,v 1.1.2.9 2001-04-30 14:16:57 bloritsch Exp $
package org.apache.cocoon.acting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.log.Logger;
import org.xml.sax.EntityResolver;

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
 *                 &lt;select dbcol="password" request-param="password"/&gt;
 *                 &lt;select dbcol="role" to-session="role" type="string"/&gt;
 *                 &lt;select dbcol="skin" to-session="skin" type="string"/&gt;
 *         &lt;/table&gt;
 * &lt;/root&gt;
 * </pre>
 * The values specified via "request-param" describe the name of HTTP request
 * parameter, "dbcol" indicates matching database column and finally
 * "to-session" attribute indicates under which name the value obtained from
 * database should stored in the session. Of course new session is created
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
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-04-30 14:16:57 $
 */
public class DatabaseAuthenticatorAction extends AbstractDatabaseAction
{
    /**
     * Main invocation routine.
     */
    public Map act (EntityResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        DataSourceComponent datasource = null;
        Connection conn = null;

        try {
            Configuration conf = this.getConfiguration (
                    parameters.getParameter ("descriptor", null));
            boolean cs = true;
            String create_session = parameters.getParameter ("create-session", null);
            if (create_session != null &&
                    ("no".equals (create_session.trim ()) || "false".equals (create_session.trim ()))) {
                cs = false;
            }

            datasource = this.getDataSource(conf);
            conn = datasource.getConnection();
            Request req = (Request) objectModel.get(Constants.REQUEST_OBJECT);

            /* check request validity */
            if (req == null) {
                getLogger ().debug ("DBAUTH: no request object");
                return null;
            }


            String query = this.getAuthQuery (conf, req);
            if (query == null) {
                getLogger ().debug ("DBAUTH: have not got query");
                return null;
            }

            try {
                getLogger ().debug ("DBAUTH: quuery is: " + query);
                Statement st = conn.createStatement ();
                ResultSet rs = st.executeQuery (query);
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
                    rs.close ();
                    st.close ();
                    return Collections.unmodifiableMap (actionMap);
                }
                getLogger ().debug ("DBAUTH: no results for query");
            } catch (Exception e) {
                getLogger ().debug ("exception: ", e);
                return null;
            }
        } catch (Exception e) {
            getLogger().debug ("exception: ", e);
        }
        return null;
    }

    private String getAuthQuery (Configuration conf, Request req) {
        boolean first_constraint = true;
        StringBuffer queryBuffer = new StringBuffer ("SELECT ");
        StringBuffer queryBufferEnd = new StringBuffer ("");
        String dbcol, request_param, request_value;
        Configuration table = conf.getChild ("table");
        Configuration[] select = table.getChildren ("select");
        try {
            for (int i = 0; i < select.length; i ++) {
                if (i != 0)
                    queryBuffer.append (", ");
                dbcol = select[i].getAttribute ("dbcol");
                queryBuffer.append (dbcol);
                try {
                    request_param = select[i].getAttribute ("request-param");
                    if (request_param == null ||
                            request_param.trim().equals ("")) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                /* if there is a request parameter name,
                 * but not the value, we exit immediately do
                 * that authorization fails authomatically */
                request_value = req.getParameter (
                        request_param);
                if (request_value == null || request_value.trim().equals ("")) {
                    getLogger ().debug ("DBAUTH: request-param "
                            + request_param + " does not exist");
                    return null;
                }
                if (!first_constraint)
                    queryBufferEnd.append (" AND ");
                queryBufferEnd.append (dbcol + "='" + request_value + "'");
                first_constraint = false;
            }
            queryBuffer.append (" FROM ");
            queryBuffer.append (table.getAttribute ("name"));
            if (!queryBufferEnd.toString ().trim ().equals (""))
                queryBuffer.append (" WHERE ").append (queryBufferEnd);
            return queryBuffer.toString ();
        } catch (Exception e) {
            getLogger ().debug ("DBAUTH: got exception: " + e);
            return null;
        }
    }

    private HashMap propagateParameters (Configuration conf, ResultSet rs,
            Session session) {
        Configuration table = conf.getChild ("table");
        Configuration[] select = table.getChildren ("select");
        String dbcol, session_param, type;
        HashMap map = new HashMap();
        try {
            for (int i = 0; i < select.length; i ++) {
                dbcol = select[i].getAttribute ("dbcol");
                try {
                    session_param = select[i].getAttribute ("to-session");
                    if (session_param != null &&
                            !session_param.trim().equals ("")) {
                        String s = rs.getString (i + 1);
                        /* propagate to session */
                        try {
                            type = select[i].getAttribute ("type");
                        } catch (Exception e) {
                            type = null;
                        }
                        if (type == null || "".equals (type.trim ())) {
                            type = "string";
                        }
                        Object o = null;
                        if ("string".equals (type)) {
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
                }
            }
            return map;
        } catch (Exception e) {
            getLogger().debug("exception: ", e);
        }
        return null;
    }
}

// $Id: DatabaseAuthenticatorAction.java,v 1.1.2.9 2001-04-30 14:16:57 bloritsch Exp $
// vim: set et ts=4 sw=4:
