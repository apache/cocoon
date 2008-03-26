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
package org.apache.cocoon.portal.pluto.om;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.avalon.AbstractComponent;
import org.apache.cocoon.portal.deployment.DeploymentEvent;
import org.apache.cocoon.portal.deployment.DeploymentException;
import org.apache.cocoon.portal.deployment.DeploymentStatus;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.pluto.deployment.Deployer;
import org.apache.cocoon.portal.pluto.deployment.WebApplicationRewriter;
import org.apache.cocoon.thread.RunnableManager;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.EntityResolver;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletApplicationEntityListCtrl;
import org.apache.pluto.om.portlet.PortletApplicationDefinitionList;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 *
 * @version $Id$
 */
public class PortletDefinitionRegistryImpl
    extends AbstractComponent
    implements PortletDefinitionRegistry, Receiver, Parameterizable, Runnable {

    /** Wait ten seconds before scanning. */
    protected static final int STARTUP_DELAY = 10 * 1000;

    private static final String WEB_XML = "WEB-INF/web.xml";
    private static final String PORTLET_XML = "WEB-INF/portlet.xml";
    private static final String COPLET_XML = "WEB-INF/coplet.xml";

    /** The mapping */
    public static final String PORTLET_MAPPING = "resource://org/apache/cocoon/portal/pluto/om/portletdefinitionmapping.xml";

    /** The mapping */
    public static final String WEBXML_MAPPING = "resource://org/apache/cocoon/portal/pluto/om/servletdefinitionmapping.xml";

    /** The portlet application entity list */
    protected PortletApplicationEntityListImpl portletApplicationEntities = new PortletApplicationEntityListImpl(this);

    // Helper lists and hashtables to access the data as fast as possible
    // List containing all portlet applications available in the system
    protected PortletApplicationDefinitionListImpl registry = new PortletApplicationDefinitionListImpl();

    /** All portlet definitions, hashed by ObjectId */
    protected Map portletsKeyObjectId = new HashMap();

    /** Our context name. */
    protected String contextName;

    /** The entity resolver */
    protected EntityResolver entityResolver;

    /** Path to the webapp directory containing all web apps. This is used to find already
     * deployed portlets and to deploy new portlets. */
    protected String  webAppDir;

    protected String  localAppDir  = "conf/portlets";
    protected boolean stripLoggers = false;

    /** The castor mapping for the portlet.xml. */
    protected Mapping mappingPortletXml = new Mapping();

    /** The castor mapping for the web.xml. */
    protected Mapping mappingWebXml = new Mapping();

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

    /**
     * Default constructor.
     */
    public PortletDefinitionRegistryImpl() {
        // nothing to do
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager)
    throws ServiceException {
        super.service(serviceManager);
        this.entityResolver = (EntityResolver) this.manager.lookup(EntityResolver.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.webAppDir = params.getParameter("webapp-directory", null);
        this.localAppDir = params.getParameter("localapp-directory", this.localAppDir);
        this.stripLoggers = params.getParameterAsBoolean("strip-loggers", this.stripLoggers);
        this.scanOnStartup = params.getParameterAsBoolean("scan-on-startup", this.scanOnStartup);
        this.threadPoolName = params.getParameter("thread-pool-name", this.threadPoolName);
        this.createCoplets = params.getParameterAsBoolean("create-coplets", this.createCoplets);
        this.copletBaseDataName = params.getParameter("coplet-base-data", this.copletBaseDataName);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.entityResolver);
            this.entityResolver = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Initializing Portlet Definition Registry.");
            this.getLogger().info("Local application directory: " + this.localAppDir);
            this.getLogger().info("Strip loggers on deployment: " + this.stripLoggers);
            if ( this.webAppDir != null ) {
                this.getLogger().info("Web application directory: " + this.webAppDir);
            }
            this.getLogger().info("Scan on startup: " + this.scanOnStartup);
        }
        super.initialize();

        this.servletContext = this.portalService.getRequestContext().getServletContext();

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

        // load mapping
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            Source source = null;
            try {
                source = resolver.resolveURI(PORTLET_MAPPING);
                this.mappingPortletXml.loadMapping(SourceUtil.getInputSource(source));
            } finally {
                resolver.release(source);
            }
            try {
                source = resolver.resolveURI(WEBXML_MAPPING);
                this.mappingWebXml.loadMapping(SourceUtil.getInputSource(source));
            } finally {
                resolver.release(source);
            }
        } finally {
            this.manager.release(resolver);
        }

        // now load existing webapps/portlets
        if ( this.scanOnStartup ) {
            RunnableManager runnableManager = null;
            try {
                runnableManager = (RunnableManager)this.manager.lookup(RunnableManager.ROLE);
                runnableManager.execute(this.threadPoolName, this, STARTUP_DELAY);
            } finally {
                this.manager.release(runnableManager);
            }
        }

        ((PortletApplicationEntityListCtrl)this.portletApplicationEntities).add("cocoon");
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
                this.loadLocal();
            } else {
                this.scanWebapps();
            }
        } catch (Exception ignore) {
            this.getLogger().error("Exception during scanning of portlet applications.", ignore);
        }
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry#getPortletApplicationDefinitionList()
     */
    public PortletApplicationDefinitionList getPortletApplicationDefinitionList() {
        return registry;
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry#getPortletDefinition(org.apache.pluto.om.common.ObjectID)
     */
    public PortletDefinition getPortletDefinition(ObjectID id) {
        return (PortletDefinition)portletsKeyObjectId.get(id);
    }

    protected void scanWebapps()
    throws Exception {
        File f = new File(this.webAppDir);
        String[] entries = f.list();
        List entryList = Arrays.asList(entries);
        for (int i=0; i<entries.length; i++) {
            File entry = new File(f, entries[i]);
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Searching file: " + entry);
            }
            try {
                if (entry.isDirectory()) {
                    this.loadWebApp(f.getAbsolutePath(), entries[i]);
                } else if (entry.isFile()) {
                    String name = entry.getName();
                    int index = name.lastIndexOf(".war");
                    if (index > 0 && name.endsWith(".war")) {
                        String webModule = name.substring(0, index);
                        if (!entryList.contains(webModule)) {
                            this.loadWar(entry, webModule);
                        }
                    }
                }
            } catch (DeploymentException de) {
                this.getLogger().error("Error during deployment of portlet application.", de);
            }
        }
    }

    protected void loadLocal()
    throws Exception {
        URL url = this.servletContext.getResource("/" + PORTLET_XML);
        if (url != null) {
            InputSource portletSource = new InputSource(url.openStream());
            portletSource.setSystemId(url.toExternalForm());

            url = this.servletContext.getResource("/" + WEB_XML);
            final InputSource webSource = new InputSource(url.openStream());
            webSource.setSystemId(url.toExternalForm());

            url = this.servletContext.getResource("/" + COPLET_XML);
            InputSource copletSource = null;
            if ( url != null ) {
                copletSource = new InputSource(url.openStream());
                copletSource.setSystemId(url.toExternalForm());
            }
            this.load(portletSource, webSource, copletSource, this.contextName);
        }
    }

    protected void loadWar(File warFile, String webModule)
    throws Exception {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Searching war " + warFile.getName());
        }
        try {
            ZipFile war = new ZipFile(warFile);
            ZipEntry entry = war.getEntry(PORTLET_XML);
            // no portlet.xml -> not a portlet web application
            if (entry != null) {
                final InputSource portletSource = new InputSource(war.getInputStream(entry));
                portletSource.setSystemId("/" + PORTLET_XML);
                entry = war.getEntry(WEB_XML);
                // no web.xml -> not a web application
                if (entry == null) {
                    return;
                }
                final InputSource webSource = new InputSource(war.getInputStream(entry));
                webSource.setSystemId("/" + WEB_XML);

                InputSource copletSource = null;
                entry = war.getEntry(COPLET_XML);
                if ( entry != null ) {
                    copletSource = new InputSource(war.getInputStream(entry));
                    copletSource.setSystemId("/" + COPLET_XML);
                }
                this.load(portletSource, webSource, copletSource, webModule);
            }
        } catch (Exception e) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Unable to inspect war " + warFile.getName() +". " +
                    e. getMessage());
            }
        }
    }

    protected void loadWebApp(String baseDir, String webModule)
    throws Exception {
        final String directory = baseDir + File.separatorChar + webModule + File.separatorChar + "WEB-INF";
        if (this.getLogger().isInfoEnabled()) {
            this.getLogger().info("Searching for portlet application in directory: " + directory);
        }

        // check for the portlet.xml and web.xml. If there is no portlet.xml this is not a
        // portlet application web module. If there is no web.xml this is not a web app.
        final File portletXml = new File(directory + File.separatorChar + "portlet.xml");
        final File webXml = new File(directory + File.separatorChar + "web.xml");
        if (portletXml.exists()&& webXml.exists()) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Loading the following Portlet Applications XML files..." +
                    portletXml +
                    ", " +
                    webXml);
            }

            final InputSource portletSource = new InputSource(new FileInputStream(portletXml));
            portletSource.setSystemId(portletXml.toURL().toExternalForm());

            // web.xml is optional
            InputSource webSource = null;
            if (webXml.exists()) {
                webSource = new InputSource(new FileInputStream(webXml));
                webSource.setSystemId(webXml.toURL().toExternalForm());
            }

            // coplet.xml is optional
            final File copletXml = new File(directory + File.separatorChar + "coplet.xml");
            InputSource copletSource = null;
            if ( copletXml.exists() ) {
                copletSource = new InputSource(new FileInputStream(copletXml));
                copletSource.setSystemId(copletXml.toURL().toExternalForm());
            }

            this.load(portletSource, webSource, copletSource, webModule);
        }
    }

    protected void load(InputSource portletXml,
                        InputSource webXml,
                        InputSource copletXml,
                        String      webModule)
    throws Exception {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Loading the following Portlet Applications XML files..." +
                portletXml.getSystemId() +
                    ", " +
                    webXml.getSystemId());
        }

        Unmarshaller unmarshaller = new Unmarshaller(this.mappingPortletXml);
        unmarshaller.setIgnoreExtraElements(true);
        unmarshaller.setEntityResolver(this.entityResolver);
        unmarshaller.setValidation(false);
        PortletApplicationDefinitionImpl portletApp =
            (PortletApplicationDefinitionImpl) unmarshaller.unmarshal(portletXml);

        WebApplicationDefinitionImpl webApp = null;

        if (webXml.getByteStream() != null) {
            this.getLogger().info("Loading web.xml...");
            unmarshaller = new Unmarshaller(this.mappingWebXml);
            unmarshaller.setIgnoreExtraElements(true);
            unmarshaller.setEntityResolver(this.entityResolver);
            unmarshaller.setValidation(false);
            webApp = (WebApplicationDefinitionImpl) unmarshaller.unmarshal(webXml);

            Vector structure = new Vector();
            structure.add(portletApp);
            structure.add("/" + webModule);

            webApp.postLoad(structure);

            // refill structure with necessary information
            webApp.preBuild(structure);

            webApp.postBuild(structure);
        } else {
            this.getLogger().info("No web.xml...");

            Vector structure = new Vector();
            structure.add("/" + webModule);
            structure.add(null);
            structure.add(null);

            portletApp.postLoad(structure);

            portletApp.preBuild(structure);

            portletApp.postBuild(structure);

        }
        this.getLogger().debug("portlet.xml loaded");

        this.registry.add(portletApp);

        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Portlet application '" + portletApp.getGUID() + "' added to registry.");
        }

        // fill portletsKeyObjectId and
        // register new coplet data for each portlet
        final Iterator portlets = portletApp.getPortletDefinitionList().iterator();
        while (portlets.hasNext()) {
            final PortletDefinition portlet = (PortletDefinition) portlets.next();
            portletsKeyObjectId.put(portlet.getId(), portlet);
            if (this.contextName.equals(webModule)) {
                ((PortletDefinitionImpl) portlet).setLocalPortlet(true);
            } else if ( portlet.getServletDefinition() == null ) {
                throw new DeploymentException("Unable to deploy portlet '" + portlet.getId() +
                          "'. Servlet definition for '"+WebApplicationRewriter.CONTAINER+"' not found in web.xml.");
            }
            ((PortletDefinitionImpl) portlet).setPortletClassLoader(Thread.currentThread()
                .getContextClassLoader());
            if ( this.getLogger().isInfoEnabled() ) {
                this.getLogger().info("Adding portlet '" + portlet.getId() + "'.");
            }
            if ( this.createCoplets ) {
                // TODO - parse coplet.xml if available
                final CopletType cbd = this.portalService.getProfileManager().getCopletType(this.copletBaseDataName);
                // TODO - check portletId for invalid characters!
                final String defId = StringUtils.replaceChars(portlet.getId().toString(), '.', '_');
                final CopletDefinition cd = this.portalService.getCopletFactory().newInstance(cbd, defId);
                cd.setAttribute("portlet", portlet.getId().toString());
                cd.setAttribute("buffer", Boolean.TRUE);
                if ( this.getLogger().isInfoEnabled() ) {
                    this.getLogger().info("Created coplet data: " + cd.getId());
                }
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry#getPortletApplicationEntityList()
     */
    public PortletApplicationEntityList getPortletApplicationEntityList() {
        return this.portletApplicationEntities;
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry#getPortalService()
     */
    public PortalService getPortalService() {
        return this.portalService;
    }

    /**
     * @see Receiver
     */
    public void inform(DeploymentEvent event) {
        String fileName = event.getDeploymentObject().getName();
        if (fileName.endsWith(".war")) {
            try {
                File toFile = new File(this.webAppDir, fileName);
                if ( Deployer.deploy(new URL(event.getDeploymentObject().getUri()).openStream(),
                                     toFile.getAbsolutePath(),
                                     this.stripLoggers,
                                     this.getLogger(), this.manager) ) {
                    // let's wait some seconds to give the web container time to
                    // deploy the new web app
                    Thread.sleep(10 * 1000);
                    final String webModule = fileName.substring(0, fileName.length()-4);
                    this.loadWebApp(this.webAppDir, webModule);
                }
                event.setStatus(DeploymentStatus.STATUS_OKAY);
            } catch (Exception e) {
                this.getLogger().error("Error during deployment of " + event.getDeploymentObject().getName(), e);
                event.setStatus(DeploymentStatus.STATUS_FAILED);
            }
        }
    }
}
