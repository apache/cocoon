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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.cocoon.thread.RunnableManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;

/**
 * This class runs an instance of the HSQLDB HSQL protocol network database server.
 *
 * @version $Id$
 */
public class ServerImpl
       implements Runnable {

    private static final boolean DEFAULT_TRACE = false;
    private static final boolean DEFAULT_SILENT = true;
    private static final int DEFAULT_PORT = 9002;
    private static final String CONTEXT_PROTOCOL = "context:/";
    private static final String DEFAULT_DB_NAME = "cocoondb";
    private static final String DEFAULT_DB_PATH = "context://WEB-INF/db";

    /** The HSQLDB HSQL protocol network database server instance **/
    private org.hsqldb.Server hsqlServer = new org.hsqldb.Server();

    /** The threadpool name to be used for daemon thread */
    private String daemonThreadPoolName = "daemon";

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    /** The servlet context. */
    private ServletContext servletContext;

    /** The runnable manager. */
    private RunnableManager runnableManager;

    private boolean trace = DEFAULT_TRACE;
    private boolean silent = DEFAULT_SILENT;
    private int port = DEFAULT_PORT;

    private Properties databases;

    public ServerImpl() {
        hsqlServer.setLogWriter(null); /* Remove console log */
        hsqlServer.setErrWriter(null); /* Remove console log */
        hsqlServer.setNoSystemExit(true);
        this.databases = new Properties();
        this.databases.setProperty(DEFAULT_DB_NAME, DEFAULT_DB_PATH);
    }

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    public org.hsqldb.Server getServer() {
        return this.hsqlServer;
    }

    public void setThreadPoolName(String name) {
        this.daemonThreadPoolName = name;
    }

    public void setDatabases(Properties p) {
        this.databases = p;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setServletContext(ServletContext c) {
        this.servletContext = c;
    }

    public void setRunnableManager(RunnableManager runnableManager) {
        this.runnableManager = runnableManager;
    }

    /**
     * Initialize the ServerImpl.
     */
    public void init() {
        if ( this.databases == null || this.databases.size() == 0 ) {
            this.getLogger().warn("HSQLDB Server is configured, but no databases are configured!");
            this.getLogger().warn("HSQLDB Server not started.");
            return;
        }
        this.hsqlServer.setSilent(this.silent);
        this.hsqlServer.setTrace(this.trace);
        this.hsqlServer.setPort(this.port);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configure HSQLDB with port: " + hsqlServer.getPort() +
                              ", silent: " + hsqlServer.isSilent() +
                              ", trace: " + hsqlServer.isTrace());
        }

        final Iterator i = this.databases.entrySet().iterator();
        int index = 0;
        while ( i.hasNext() ) {
            final Map.Entry current = (Map.Entry)i.next();
            final String name = current.getKey().toString();
            final String dbCfgPath = current.getValue().toString();
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Configuring database " + name + " with path " + dbCfgPath);
            }
            String dbPath = dbCfgPath;
            // is the context protocol used?
            if (dbPath.startsWith(ServerImpl.CONTEXT_PROTOCOL)) {
                // Test if we are running inside a WAR file
                dbPath = this.servletContext.getRealPath(dbPath.substring(ServerImpl.CONTEXT_PROTOCOL.length()));
                if (dbPath == null) {
                    throw new IllegalArgumentException("The hsqldb cannot be used inside an unexpanded WAR file. " +
                                                 "Real path for <" + dbCfgPath + "> is null.");
                }
            } else {
                // is this a file url
                if ( dbPath.startsWith("file:") ) {
                    dbPath = dbPath.substring(5);
                }
                // we test if the path points to a directory in the file system
                final File directory = new File(dbPath);
                if ( !directory.exists() ) {
                    throw new IllegalArgumentException("Path for hsqldb database does not exist: " + dbPath);
                }
                if ( !directory.isDirectory() ) {
                    throw new IllegalArgumentException("Path for hsqldb database does not point to a directory: " + dbPath);
                }
            }

            try {
                hsqlServer.setDatabasePath(index, new File(dbPath).getCanonicalPath() + File.separator + name);
                hsqlServer.setDatabaseName(index, name);
            } catch (IOException e) {
                throw new RuntimeException("Could not get database directory <" + dbPath + ">", e);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Database path for " + name + " is <" + hsqlServer.getDatabasePath(index, true) + ">, index " + index);
            }
            index++;
        }
        this.start();
    }

    /**
     * Destroy the server and release everything.
     */
    public void destroy() {
        this.stop();
    }

    /**
     * Start the server.
     */
    protected void start() {
        this.runnableManager.execute(this.daemonThreadPoolName, this);
    }

    /**
     * Stop the server.
     */
    protected void stop() {
        if ( this.getLogger().isDebugEnabled() ) {
            getLogger().debug("Shutting down HSQLDB");
        }
        // AG: Temporally workaround for http://issues.apache.org/jira/browse/COCOON-1862
        // A newer version of hsqldb or SAP NetWeaver may not need the next line
        DatabaseManager.closeDatabases(Database.CLOSEMODE_COMPACT);
        this.hsqlServer.stop();
        if ( this.getLogger().isDebugEnabled() ) {
            getLogger().debug("Shutting down HSQLDB: Done");
        }
    }

    /** Run the server */
    public void run() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Starting " + hsqlServer.getProductName() + " " + hsqlServer.getProductVersion() + " with parameters:");
        }
        this.hsqlServer.start();
    }
}
