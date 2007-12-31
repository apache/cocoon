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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.servlet.CocoonServlet;
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
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class PortletDefinitionRegistryImpl 
extends AbstractLogEnabled
implements PortletDefinitionRegistry, Contextualizable, Initializable, Serviceable, Disposable {

    private static final String WEB_XML = "WEB-INF/web.xml";
    private static final String PORTLET_XML = "WEB-INF/portlet.xml";

    /** The mapping */
    public static final String PORTLET_MAPPING = "resource://org/apache/cocoon/portal/pluto/om/portletdefinitionmapping.xml";

    /** The mapping */    
    public static final String WEBXML_MAPPING = "resource://org/apache/cocoon/portal/pluto/om/servletdefinitionmapping.xml";

    /** The context */
    protected Context context;
    
    /** The service manager */
    protected ServiceManager manager;

    /** The portlet application entity list */
    protected PortletApplicationEntityListImpl portletApplicationEntities = new PortletApplicationEntityListImpl(this);
    
    // Helper lists and hashtables to access the data as fast as possible
    // List containing all portlet applications available in the system
    protected PortletApplicationDefinitionListImpl registry = new PortletApplicationDefinitionListImpl();
    /** All portlet definitions, hashed by ObjectId */
    protected Map portletsKeyObjectId = new HashMap();
    /** Our context name */
    protected String contextName;

    /** The entity resolver */
    protected EntityResolver resolver;
    
    /** The portal service. */
    protected PortalService service;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) 
    throws ServiceException {
        this.manager = manager;
        this.resolver = (EntityResolver) this.manager.lookup(EntityResolver.ROLE);
        this.service = (PortalService) this.manager.lookup(PortalService.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager.release(this.service);
            this.service = null;
            this.manager = null;
        }
        this.context = null;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Initializing PortletDefinitionRegistry");
        }
        ServletConfig servletConfig = (ServletConfig) this.context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        
        ServletContext servletContext = servletConfig.getServletContext();

        SourceResolver resolver = null;

        try {
            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            Mapping mappingPortletXml = new Mapping();
            Mapping mappingWebXml = new Mapping();
            Source source = null;
            try {
                source = resolver.resolveURI(PORTLET_MAPPING);

                mappingPortletXml.loadMapping(SourceUtil.getInputSource(source));
            } finally {
                resolver.release(source);
            }
            try {
                source = resolver.resolveURI(WEBXML_MAPPING);

                mappingWebXml.loadMapping(SourceUtil.getInputSource(source));
            } finally {
                resolver.release(source);
            }

            String baseWMDir = servletContext.getRealPath("");

            if (baseWMDir != null) {
                // BEGIN PATCH for IBM WebSphere
                if (baseWMDir.endsWith(File.separator)) {
                    baseWMDir = baseWMDir.substring(0, baseWMDir.length() - 1);
                }
                // END PATCH for IBM WebSphere
                int lastIndex = baseWMDir.lastIndexOf(File.separatorChar);
                this.contextName = baseWMDir.substring(lastIndex + 1);
                baseWMDir = baseWMDir.substring(0, lastIndex + 1);
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("servletContext.getRealPath('') =" +
                        servletContext.getRealPath(""));
                    this.getLogger().debug("baseWMDir = " + baseWMDir);
                }
                this.load(baseWMDir, mappingPortletXml, mappingWebXml);
            } else {
                if (this.getLogger().isWarnEnabled()) {
                    this.getLogger().warn("Only local portlets are supported when deployed as a war");
                    this.contextName = "local";
                    loadLocal(mappingPortletXml, mappingWebXml);
                }
            }

        } catch (Exception e) {
            this.getLogger().error("Error during initialization of registry.", e);
        } finally {
            this.manager.release(resolver);
        } 

        ((PortletApplicationEntityListCtrl)this.portletApplicationEntities).add("cocoon");
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

    protected void load(String baseWMDir, Mapping portletXMLMapping, Mapping webXMLMapping) 
        throws Exception {
        File f = new File(baseWMDir);
        String[] entries = f.list();
        List entryList = Arrays.asList(entries);
        for (int i=0; i<entries.length; i++) {
            File entry = new File(baseWMDir+entries[i]);
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Searching file: " + entry);
            }
            if (entry.isDirectory()) {
                loadWebApp(baseWMDir, entries[i], portletXMLMapping, webXMLMapping);
            } else if (entry.isFile()) {
                String name = entry.getName();
                int index = name.lastIndexOf(".war");
                if (index > 0 && name.endsWith(".war")) {
                    String webModule = name.substring(0, index);
                    if (!entryList.contains(webModule)) {
                        loadWar(entry, webModule, portletXMLMapping, webXMLMapping);
                    }
                }
            }
        }
    }

    private void loadLocal(Mapping portletXMLMapping, Mapping webXMLMapping) throws Exception {
        ServletConfig config =
            (ServletConfig)this.context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        if (config == null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Unable to locate servlet config");
            }
            return;
        }
        ServletContext servletContext = config.getServletContext();
        URL url = servletContext.getResource("/" + PORTLET_XML);
        if (url != null) {
            InputSource portletSource = new InputSource(url.openStream());
            portletSource.setSystemId(url.toExternalForm());

            url = servletContext.getResource("/" + WEB_XML);
            InputSource webSource = null;
            if (url != null) {
                webSource = new InputSource(url.openStream());
                webSource.setSystemId(url.toExternalForm());
            }
            else {
                webSource = new InputSource();
                webSource.setSystemId("no web.xml!");
            }

            load(portletSource, webSource, this.contextName, portletXMLMapping, webXMLMapping);
        }
    }

    private void loadWar(File warFile, String webModule, Mapping portletXMLMapping,
                         Mapping webXMLMapping) throws Exception {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Searching war " + warFile.getName());
        }
        InputSource portletSource;
        InputSource webSource;
        try {
            ZipFile war = new ZipFile(warFile);
            ZipEntry entry = war.getEntry(PORTLET_XML);
            if (entry != null) {
                portletSource = new InputSource(war.getInputStream(entry));
                portletSource.setSystemId("/" + PORTLET_XML);
                entry = war.getEntry(WEB_XML);
                if (entry != null) {
                    webSource = new InputSource(war.getInputStream(entry));
                    webSource.setSystemId("/" + WEB_XML);
                } else {
                    webSource = new InputSource();
                    webSource.setSystemId("no web.xml!");
                }
                load(portletSource, webSource, webModule, portletXMLMapping, webXMLMapping);
            }
        } catch (Exception e) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Unable to inspect war " + warFile.getName() +". " +
                    e. getMessage());
            }
        }
    }

    private void loadWebApp(String baseDir, String webModule, Mapping portletXMLMapping,
                            Mapping webXMLMapping) throws Exception {
        String directory = baseDir + webModule + File.separatorChar + "WEB-INF" + File.separatorChar;
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Searching in directory: " + directory);
        }

        File portletXml = new File(directory + "portlet.xml");
        File webXml = new File(directory + "web.xml");

        // check for the porlet.xml. If there is no portlet.xml this is not a
        // portlet application web module
        if (portletXml.exists()) { // && (webXml.exists())) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Loading the following Portlet Applications XML files..." +
                    portletXml +
                    ", " +
                    webXml);
            }

            InputSource portletSource = new InputSource(new FileInputStream(portletXml));
            portletSource.setSystemId(portletXml.toURL().toExternalForm());
            InputSource webSource = null;

            if (webXml.exists()) {
                webSource = new InputSource(new FileInputStream(webXml));
                webSource.setSystemId(webXml.toURL().toExternalForm());
            }

            load(portletSource, webSource, webModule, portletXMLMapping, webXMLMapping);
        }
    }

    private void load(InputSource portletXml, InputSource webXml, String webModule,
                      Mapping portletXMLMapping, Mapping webXMLMapping) throws Exception {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Loading the following Portlet Applications XML files..." +
                portletXml.getSystemId() +
                    ", " +
                    webXml.getSystemId());
            }

        Unmarshaller unmarshaller = new Unmarshaller(portletXMLMapping);
        unmarshaller.setIgnoreExtraElements(true);
        unmarshaller.setEntityResolver(this.resolver);
        unmarshaller.setValidation(false);
        PortletApplicationDefinitionImpl portletApp =
            (PortletApplicationDefinitionImpl) unmarshaller.unmarshal(portletXml);

        WebApplicationDefinitionImpl webApp = null;

        if (webXml.getByteStream() != null) {
            unmarshaller = new Unmarshaller(webXMLMapping);
            unmarshaller.setIgnoreExtraElements(true);
            unmarshaller.setEntityResolver(this.resolver);
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
            this.getLogger().info("no web.xml...");

            Vector structure = new Vector();
            structure.add("/" + webModule);
            structure.add(null);
            structure.add(null);

            portletApp.postLoad(structure);

            portletApp.preBuild(structure);

            portletApp.postBuild(structure);

            this.getLogger().debug("portlet.xml loaded");
        }

        this.registry.add(portletApp);

        this.getLogger().debug("Portlet added to registry");

        // fill portletsKeyObjectId
        final Iterator portlets = portletApp.getPortletDefinitionList().iterator();
        while (portlets.hasNext()) {
            final PortletDefinition portlet = (PortletDefinition) portlets.next();
            portletsKeyObjectId.put(portlet.getId(), portlet);

            if (this.contextName.equals(webModule)) {
                ((PortletDefinitionImpl) portlet).setLocalPortlet(true);
            }
            ((PortletDefinitionImpl) portlet).setPortletClassLoader(Thread.currentThread()
                .getContextClassLoader());
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
        return this.service;
    }

}
