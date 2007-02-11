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
package org.apache.cocoon.components.hsqldb;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.thread.RunnableManager;

/**
 * This class runs an instance of HSQLDB Server.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class ServerImpl extends AbstractLogEnabled
    implements Server,
               Parameterizable,
               Contextualizable,
               ThreadSafe,
               Runnable,
               Serviceable,
               Startable {

    /** Port which HSQLDB server will listen to */
    private String port;

    /** Arguments for running the server */
    private String arguments[] = new String[10];

    /** The threadpool name to be used for daemon thread */
    private String m_daemonThreadPoolName = "daemon";

    /** Check if the server has already been started */
    private boolean started = false;

    /** The {@link ServiceManager} instance */
    private ServiceManager m_serviceManager;
    
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
        m_daemonThreadPoolName = params.getParameter( "thread-pool-name", m_daemonThreadPoolName );
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
    /**
     * @param serviceManager The <@link ServiceManager} instance
     * @throws ServiceException In case we cannot find a service needed
     */
    public void service( ServiceManager serviceManager )
    throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    /** Start the server */
    public void start() {
        if (!started) {
            // FIXME (VG): This dirty hack here is till shutdown issue is resolved
            File file = new File(arguments[9] + ".backup");
            if (file.exists() && file.delete()) {
                getLogger().info("HSQLDB backup file has been deleted.");
            }

            RunnableManager runnableManager = null;
            try
            {
            this.getLogger().debug("Intializing hsqldb server thread");
                runnableManager = (RunnableManager)m_serviceManager.lookup(RunnableManager.ROLE);
                runnableManager.execute( m_daemonThreadPoolName, this );
            }
            catch( final ServiceException se )
            {
                throw new CascadingRuntimeException( "Cannot get RunnableManager", se );
            }
            finally
            {
                if( null != runnableManager )
                {
                    m_serviceManager.release( runnableManager );
                }
            }
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
