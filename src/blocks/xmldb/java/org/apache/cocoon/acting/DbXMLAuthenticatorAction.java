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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This action is used to authenticate user by comparing several request
 * fields (username, password) with the values in a DBXML compliant database.
 * The description of the process is given via external xml description file
 * simiar to the one used for all actions derived from AbstractDatabaseAction.
 *
 * <pre>
 * <root>
 *   <connection>
 *     <driver>org.apache.xindice.client.xmldb.DatabaseImpl</driver>
 *     <base>xmldb:xindice:///db/beta</base>
 *   </connection>
 *
 *   <root name="users">
 *      <select element="username" request-param="username" to-session="username"/>
 *      <select element="password" request-param="password" nullable="yes"/>
 *
 *      <select element="role" to-session="role" type="string"/>
 *      <select element="skin" to-session="skin" type="string"/>
 *   </root>
 * </root>
 * </pre>
 *
 * The values specified via "request-param" describe the name of HTTP request
 * parameter, "element" indicates matching document node, "nullable" means
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
 * @author <a href="mailto:czoffoli@littlepenguin.org">Christian Zoffoli</a>
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @since 2002/02/03
 * @version CVS $Id: DbXMLAuthenticatorAction.java,v 1.5 2003/10/29 14:21:14 vgritsenko Exp $
 *
 * based on DatabaseAuthenticatorAction created by Martin Man <Martin.Man@seznam.cz>
 */
public class DbXMLAuthenticatorAction extends AbstractDatabaseAction implements ThreadSafe
{

  /**
  * Main invocation routine.
  */
  public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
        Parameters parameters) throws Exception {

    ResourceSet rs = null;

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
          parameters.getParameterAsBoolean("reloadable",
          reloadable));


        boolean cs = true;
        String create_session = parameters.getParameter ("create-session", (String) this.settings.get("create-session"));

        if (create_session != null && ("no".equals (create_session.trim ()) || "false".equals (create_session.trim ()))) {
          cs = false;
        }

        Request req = ObjectModelHelper.getRequest(objectModel);

        /* check request validity */
        if (req == null) {
          getLogger ().debug ("DBXMLAUTH: no request object");
          return null;
        }

        rs = this.Authenticate( conf, req );

        if (rs != null )
        {
          getLogger ().debug ("DBXMLAUTH: authorized successfully");
          Session session = null;

          if (cs) {
            session = req.getSession (false);
            if (session != null)
                session.invalidate ();
            session = req.getSession (true);
            if (session == null)
                return null;
            getLogger ().debug ("DBXMLAUTH: session created");
          } else {
            getLogger ().debug ("DBXMLAUTH: leaving session untouched");
          }

          HashMap actionMap = this.propagateParameters (conf, rs, session);
          return Collections.unmodifiableMap (actionMap);
        } else {
          //getLogger ().debug ("DBXMLAUTH: error ResourceSet is null");
        }

        req.setAttribute("message", "The username or password were incorrect, please check your CAPS LOCK key and try again.");
        getLogger ().debug ("DBXMLAUTH: no results for query");

    } catch (Exception e) {

        getLogger().debug ("exception: ", e);
        return null;
    }

    return null;
  }


  private String getAuthQuery ( Configuration conf, Request req )
  {

    StringBuffer queryBuffer = new StringBuffer ("//");
    StringBuffer queryBufferEnd = new StringBuffer ("");

    String dbcol, request_param, request_value, nullstr;
    boolean nullable = false;

    Configuration table = conf.getChild ("root");
    Configuration[] select = table.getChildren ("select");

    try {

        queryBuffer.append (table.getAttribute ("name"));

        for (int i = 0; i < select.length; i ++)
        {

          dbcol = "[" + select[i].getAttribute ("element");

          try {
            request_param = select[i].getAttribute ("request-param");
            if (request_param == null ||
                  request_param.trim().equals ("")) {
                continue;
            }
          } catch (Exception e) {
            continue;
          }

          try {
            nullstr = select[i].getAttribute ("nullable");

            if (nullstr != null) nullstr = nullstr.trim ();

            if ("yes".equals (nullstr) || "true".equals (nullstr)) {
                nullable = true;
            }

          } catch (Exception e1) {
          }

          /* if there is a request parameter name,
          * but not the value, we exit immediately do
          * that authorization fails authomatically */
          request_value = req.getParameter (request_param);

          if (request_value == null || request_value.trim().equals ("")) {
            // value is null
            if (!nullable) {
                getLogger ().debug ("DBXMLAUTH: request-param " + request_param + " does not exist");
                return null;
            }
          } else {
            queryBufferEnd.append (dbcol).append("='").append(request_value).append("']");
          }
        }

        if (!queryBufferEnd.toString ().trim ().equals (""))
          queryBuffer.append (queryBufferEnd);

        return queryBuffer.toString ();
    } catch (Exception e) {
        getLogger ().debug ("DBXMLAUTH: got exception: " + e);
        return null;
    }
  }


  private ResourceSet Authenticate( Configuration conf, Request req) throws Exception, XMLDBException {

     ResourceSet rs = null;

    String query = this.getAuthQuery (conf, req);
    if (query == null) {
      getLogger ().debug ("DBXMLAUTH: have not got query");
      req.setAttribute("message", "The authenticator is misconfigured");
      return null;
    }
    getLogger ().debug ("DBXMLAUTH: query is: " + query);


    Collection col = CreateConnection( conf );

    if ( col != null )
    {
      if ( col.isOpen() )
      {

        try
        {
          XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");

          rs = service.query(query);
          ResourceIterator results = rs.getIterator();

          if (results.hasMoreResources() == false)
          {
              getLogger ().debug ("DBXMLAUTH: auth failed");
              return null;
          } else {
            getLogger ().debug ("DBXMLAUTH: auth OK");
            return rs;
          }

        } catch (XMLDBException e) {

          getLogger ().debug ("DBXMLAUTH: got exception: " + e);
          return null;

        } finally {

          // close col
          if (col != null) {
            try {
              col.close();
            } catch (Exception e) { /* ignore */ }
          }
          getLogger ().debug ("DBXMLAUTH: collection closed");

        }

      } else {
        getLogger ().debug ("DBXMLAUTH: error: collection closed !!");
      }

    } else {
      getLogger ().debug ("DBXMLAUTH: couldn't open a connection with DB");

    }

    return null;
  }


  private Collection CreateConnection( Configuration conf ) throws Exception, XMLDBException {

    Collection col = null;

    Configuration conn = conf.getChild ("connection");

    try {

      Class c = Class.forName( conn.getChild("driver").getValue() );

      Database database = (Database) c.newInstance();
      DatabaseManager.registerDatabase(database);

      col = DatabaseManager.getCollection( conn.getChild("base").getValue() );

    } catch (XMLDBException e) {
      getLogger ().debug ("DBXMLAUTH: Exception occured " + e.errorCode);
    }

    return col;
  }

  private HashMap propagateParameters (Configuration conf,  ResourceSet resultSet, Session session) {

      Configuration table = conf.getChild ("root");
      Configuration[] select = table.getChildren ("select");
      String session_param, type;
      HashMap map = new HashMap();

      XObject xo;
      Node originalnode = null;

      try {

        ResourceIterator results = resultSet.getIterator();

        // Create an XObject to be used in Xpath query
        xo = new XObject();

        // Retrieve the next node
        XMLResource resource = (XMLResource) results.nextResource();

        originalnode = resource.getContentAsDOM();

      }
      catch (Exception e) {
        getLogger ().debug ("DBXMLAUTH: error creating XObject ");
      }


      try {
          for (int i = 0; i < select.length; i ++) {
            try {
              session_param = select[i].getAttribute ("to-session");
              if (session_param != null && !session_param.trim().equals (""))
              {

                  String s = "";

                  try {
                    // Use Xalan xpath parser to extract data
                    xo = XPathAPI.eval(originalnode, "/" + table.getAttribute ("name") + "/" + select[i].getAttribute ("element") );
                    s = xo.toString();
                  }
                  catch (Exception e) {
                  }

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
                    getLogger ().debug ("DBXMLAUTH: propagating param " + session_param + "=" + s);
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
