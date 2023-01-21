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
package org.apache.cocoon.portal.pluto.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.cocoon.thread.RunnableManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.pluto.spi.optional.PortletRegistryService;


/**
 *
 * @version $Id$
 */
public class PortletDefinitionRegistryImpl
    extends AbstractBean
    implements Receiver, Runnable {

    protected PortletRegistryService portletRegistry;

    /** Wait ten seconds before scanning. */
    protected static final int STARTUP_DELAY = 10 * 1000;

    private static final String COPLET_XML = "WEB-INF/coplet.xml";

    /** Our context name. */
    protected String contextName;

    /** Path to the webapp directory containing all web apps. This is used to find already
     * deployed portlets and to deploy new portlets. */
    protected String  webAppDir;

    protected String  localAppDir  = "conf/portlets";

    /** Should we scan the webapps directory on startup? */
    protected boolean scanOnStartup = true;

    /** Create coplets. */
    protected boolean createCoplets = true;

    /** The name of the coplet base data for portlets. */
    protected String copletBaseDataName = "Portlet";

    /** The threadpool name to be used for daemon thread. */
    protected String threadPoolName = "daemon";

    /** The servlet context. */
    protected ServletContext servletContext;

    protected RunnableManager runnableManager;

    /**
     * Default constructor.
     */
    public PortletDefinitionRegistryImpl() {
        // nothing to do
    }

    public void setWebAppDir(String webAppDir) {
        this.webAppDir = webAppDir;
    }

    public void setLocalAppDir(String localAppDir) {
        this.localAppDir = localAppDir;
    }

    public void setScanOnStartup(boolean scanOnStartup) {
        this.scanOnStartup = scanOnStartup;
    }

    public void setCreateCoplets(boolean createCoplets) {
        this.createCoplets = createCoplets;
    }

    public void setCopletBaseDataName(String copletBaseDataName) {
        this.copletBaseDataName = copletBaseDataName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public void setRunnableManager(RunnableManager runnableManager) {
        this.runnableManager = runnableManager;
    }

    /**
     * Initialize.
     */
    public void init() throws Exception {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Initializing Portlet Definition Registry.");
            this.getLogger().info("Local application directory: " + this.localAppDir);
            if ( this.webAppDir != null ) {
                this.getLogger().info("Web application directory: " + this.webAppDir);
            }
            this.getLogger().info("Scan on startup: " + this.scanOnStartup);
        }
        this.servletContext = this.portalService.getProcessInfoProvider().getServletContext();

        // get our context path
        String baseWMDir = this.servletContext.getRealPath("");
        if (baseWMDir != null) {
            // BEGIN PATCH for IBM WebSphere
            if (baseWMDir.endsWith(File.separator)) {
                baseWMDir = baseWMDir.substring(0, baseWMDir.length() - 1);
            }
            // END PATCH for IBM WebSphere
            int lastIndex = baseWMDir.lastIndexOf(File.separatorChar);
            this.contextName = baseWMDir.substring(lastIndex + 1);
            baseWMDir = baseWMDir.substring(0, lastIndex);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("servletContext.getRealPath('') =" + this.servletContext.getRealPath(""));
                this.getLogger().debug("baseWMDir = " + baseWMDir);
            }
        }
        if ( this.webAppDir == null ) {
            this.webAppDir = baseWMDir;
        }

        // now check directories
        File webAppDirFile = new File(this.webAppDir);

        if (webAppDirFile.exists() && webAppDirFile.isDirectory()) {
            try {
                this.webAppDir = webAppDirFile.getCanonicalPath();
            } catch (IOException e) {
                // ignore
            }
        } else {
            throw new FileNotFoundException("The depoyment directory for portlet applications \""
                                            + webAppDirFile.getAbsolutePath() + "\" does not exist.");
        }

        File localAppDirFile = new File(this.localAppDir);
        if (!localAppDirFile.exists()) {
            localAppDirFile.mkdirs();
        } else if (!localAppDirFile.isDirectory()) {
            throw new FileNotFoundException("Invalid depoyment directory for local portlet applications: \""
                                            + localAppDirFile.getAbsolutePath());
        }
        try {
            this.localAppDir = localAppDirFile.getCanonicalPath();
        } catch (IOException e) {
            // ignore
        }

        // now load existing webapps/portlets
        if ( this.scanOnStartup ) {
            this.runnableManager.execute(this.threadPoolName, this, STARTUP_DELAY);
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if ( this.webAppDir == null ) {
                if (this.getLogger().isWarnEnabled()) {
                    this.getLogger().warn("Only local portlets are supported when deployed as a war "
                                        + "and 'webapp-directory' is not configured.");
                }
                this.contextName = "local";
                //this.loadLocal();
            } else {
                //this.scanWebapps();
            }
        } catch (Exception ignore) {
            this.getLogger().error("Exception during scanning of portlet applications.", ignore);
        }
    }
}
