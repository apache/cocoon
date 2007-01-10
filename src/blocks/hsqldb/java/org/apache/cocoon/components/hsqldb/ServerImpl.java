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
package org.apache.cocoon.components.hsqldb;

import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.thread.RunnableManager;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;

/**
 * This class runs an instance of the HSQLDB HSQL protocol network database server.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class ServerImpl extends AbstractLogEnabled
                        implements Server, Parameterizable, Contextualizable,
                                   ThreadSafe, Runnable, Serviceable, Startable {

    private static final boolean DEFAULT_TRACE = false;
    private static final boolean DEFAULT_SILENT = true;
    private static final int DEFAULT_PORT = 9002;
    private static final String CONTEXT_PROTOCOL = "context:/";
    private static final String DEFAULT_DB_NAME = "cocoondb";
    private static final String DEFAULT_DB_PATH = "context://WEB-INF/db";

    /** Cocoon context **/
    private org.apache.cocoon.environment.Context cocoonContext;

    /** The HSQLDB HSQL protocol network database server instance **/
    private org.hsqldb.Server hsqlServer = new org.hsqldb.Server();

    /** The threadpool name to be used for daemon thread */
    private String m_daemonThreadPoolName = "daemon";

    /** The {@link ServiceManager} instance */
    private ServiceManager m_serviceManager;

    /** Contextualize this class */
    public void contextualize(Context context) throws ContextException {
        cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * Initialize the ServerImpl.
     * Posible options:
     * <ul>
     *  <li>port = port where the server is listening</li>
     *  <li>silent = false => display all queries</li>
     *  <li>trace = print JDBC trace messages</li>
     *  <li>name = name of the HSQL-DB</li>
     *  <li>path = path to the database - context-protocol is resolved</li>
     * </ul>
     */
    public void parameterize(Parameters params) throws ParameterException {
        hsqlServer.setLogWriter(null); /* Remove console log */
        hsqlServer.setErrWriter(null); /* Remove console log */
        hsqlServer.setPort(params.getParameterAsInteger("port", DEFAULT_PORT));
        hsqlServer.setSilent(params.getParameterAsBoolean("silent", DEFAULT_SILENT));
        hsqlServer.setTrace(params.getParameterAsBoolean("trace", DEFAULT_TRACE));
        hsqlServer.setNoSystemExit(true);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configure HSQLDB with port: " + hsqlServer.getPort() +
                              ", silent: " + hsqlServer.isSilent() +
                              ", trace: " + hsqlServer.isTrace());
        }

        m_daemonThreadPoolName = params.getParameter("thread-pool-name", m_daemonThreadPoolName);

        final String dbCfgPath = params.getParameter("path", DEFAULT_DB_PATH);
        String dbPath = dbCfgPath;
        // Test if we are running inside a WAR file
        if(dbPath.startsWith(ServerImpl.CONTEXT_PROTOCOL)) {
            dbPath = this.cocoonContext.getRealPath(dbPath.substring(ServerImpl.CONTEXT_PROTOCOL.length()));
        }
        if (dbPath == null) {
            throw new ParameterException("The hsqldb cannot be used inside an unexpanded WAR file. " +
                                         "Real path for <" + dbCfgPath + "> is null.");
        }

        String dbName = params.getParameter("name", DEFAULT_DB_NAME);
        try {
            hsqlServer.setDatabasePath(0, new File(dbPath).getCanonicalPath() + File.separator + dbName);
            hsqlServer.setDatabaseName(0, dbName);
        } catch (IOException e) {
            throw new ParameterException("Could not get database directory <" + dbPath + ">", e);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Database path is <" + hsqlServer.getDatabasePath(0, true) + ">");
        }
    }

    /**
     * @param serviceManager The <@link ServiceManager} instance
     * @throws ServiceException In case we cannot find a service needed
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        m_serviceManager = serviceManager;
    }

    /** Start the server */
    public void start() {
        RunnableManager runnableManager = null;
        try {
            runnableManager = (RunnableManager) m_serviceManager.lookup(RunnableManager.ROLE);
            runnableManager.execute(m_daemonThreadPoolName, this);
        } catch(final ServiceException e) {
            throw new CascadingRuntimeException("Cannot get RunnableManager", e);
        } finally {
            if (null != runnableManager) {
                m_serviceManager.release(runnableManager);
            }
        }
    }

    /** Stop the server */
    public void stop() {
        getLogger().debug("Shutting down HSQLDB");
        //AG: Temporally workaround for http://issues.apache.org/jira/browse/COCOON-1862
        // A newer version of hsqldb or SAP NetWeaver may not need the next line
        DatabaseManager.closeDatabases(Database.CLOSEMODE_COMPACT);
        hsqlServer.stop();
        getLogger().debug("Shutting down HSQLDB: Done");
    }

    /** Run the server */
    public void run() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Starting " + hsqlServer.getProductName() + " " + hsqlServer.getProductVersion() + " with parameters:");
        }
        this.hsqlServer.start();
    }
}
