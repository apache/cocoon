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
package org.apache.cocoon.components.hsqldb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;

/**
 * This class runs an instance of HSQLDB Server.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ServerImpl.java,v 1.2 2004/02/24 17:56:55 andreas Exp $
 */
public class ServerImpl extends AbstractLogEnabled
    implements Server,
               Parameterizable,
               Contextualizable,
               ThreadSafe,
               Runnable,
               Startable {

    /** Port which HSQLDB server will listen to */
    private String port;

    /** Arguments for running the server */
    private String arguments[] = new String[10];

    /** Check if the server has already been started */
    private boolean started = false;

    /**
     * Initialize the ServerImpl.
     * A few options can be used :
     * <UL>
     *  <LI>port = port where the server is listening</LI>
     *  <LI>silent = display all queries</LI>
     *  <LI>trace = print JDBC trace messages</LI>
     * </UL>
     */
    public void parameterize(Parameters params)  {
        this.getLogger().debug("Parameterize ServerImpl");

        arguments[0] = "-port";
        arguments[1] = this.port = params.getParameter("port", "9002");
        arguments[2] = "-silent";
        arguments[3] = params.getParameter("silent", "true");
        arguments[4] = "-trace";
        arguments[5] = params.getParameter("trace", "false");
        arguments[6] = "-no_system_exit";
        arguments[7] = "true";
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Configure ServerImpl with port: " + arguments[1]
                    + ", silent: " + arguments[3]
                    + ", trace: " +arguments[5]);
        }
    }

    /** Contextualize this class */
    public void contextualize(Context context) throws ContextException {
        org.apache.cocoon.environment.Context ctx =
                (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        // test if we are running inside a WAR file
        final String dbPath = ctx.getRealPath("/WEB-INF/db");
        if (dbPath == null) {
            throw new ContextException("The hsqldb cannot be used inside a WAR file.");
        }

        try {
            arguments[8] = "-database";
            arguments[9] = new File(dbPath).getCanonicalPath();
            arguments[9] += File.separator + "cocoondb";
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("database is " + arguments[9]);
            }
        } catch (MalformedURLException e) {
            getLogger().error("MalformedURLException - Could not get database directory ", e);
        } catch (IOException e) {
            getLogger().error("IOException - Could not get database directory ", e);
        }
    }

    /** Start the server */
    public void start() {
        if (!started) {
            // FIXME (VG): This dirty hack here is till shutdown issue is resolved
            File file = new File(arguments[9] + ".backup");
            if (file.exists() && file.delete()) {
                getLogger().info("HSQLDB backup file has been deleted.");
            }

            Thread server = new Thread(this);
            this.getLogger().debug("Intializing hsqldb server thread");
            server.setPriority(Thread.currentThread().getPriority());
            server.setDaemon(true);
            server.setName("hsqldb server");
            server.start();
        }
    }

    /** Stop the server */
    public void stop() {
        if (started) {
            try {
                getLogger().debug("Shutting down HSQLDB");
                Connection connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:" + this.port, "sa", "");
                Statement statement = connection.createStatement();
                statement.executeQuery("SHUTDOWN");
                try {
                    connection.close();
                } catch (SQLException e) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Shutting down HSQLDB: Ignoring exception: " + e);
                    }
                }
            } catch (Exception e){
                getLogger().error("Error while shutting down HSQLDB", e);
            }
            getLogger().debug("Shutting down HSQLDB: Done");
        }
    }

    public void run() {
        if(!started) {
            started = true;

            try {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("HSQLDB Server arguments are as follows:");
                    for(int i = 0; i < arguments.length; i++) {
                        getLogger().debug(i + " : " + arguments[i]);
                    }
                }

                org.hsqldb.Server.main(arguments);
            } catch(Exception e){
                getLogger().error("Got exception", e);
            } finally {
                started = false;
            }
        }
    }
}
