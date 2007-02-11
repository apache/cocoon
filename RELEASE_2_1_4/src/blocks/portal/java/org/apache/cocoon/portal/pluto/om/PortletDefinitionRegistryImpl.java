/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.pluto.om;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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
 * @version CVS $Id: PortletDefinitionRegistryImpl.java,v 1.3 2004/02/07 18:31:16 cziegeler Exp $
 */
public class PortletDefinitionRegistryImpl 
extends AbstractLogEnabled
implements PortletDefinitionRegistry, Contextualizable, Initializable, Serviceable, Disposable {

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
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) 
    throws ServiceException {
        this.manager = manager;
        this.resolver = (EntityResolver) this.manager.lookup(EntityResolver.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.resolver != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        this.manager = null;
        this.context = null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.getLogger().debug("Initializing PortletDefinitionRegistry");
        ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        
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
            int lastIndex = baseWMDir.lastIndexOf(File.separatorChar);
            this.contextName = baseWMDir.substring(lastIndex+1);
            baseWMDir = baseWMDir.substring(0, lastIndex+1);
            this.load(baseWMDir,mappingPortletXml, mappingWebXml);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.manager.release(resolver);
        } 

        ((PortletApplicationEntityListCtrl)this.portletApplicationEntities).add("cocoon");
    }

    public PortletApplicationDefinitionList getPortletApplicationDefinitionList() {
        return registry;
    }

    public PortletDefinition getPortletDefinition(ObjectID id) {
        return (PortletDefinition)portletsKeyObjectId.get(id);
    }

    protected void load(String baseWMDir, Mapping portletXMLMapping, Mapping webXMLMapping) 
    throws Exception {
        File f = new File(baseWMDir);
        String[] entries = f.list();
        for (int i=0; i<entries.length; i++)
        {
            File entry = new File(baseWMDir+entries[i]);
            if (entry.isDirectory()) {
                load(baseWMDir, entries[i], portletXMLMapping, webXMLMapping);
            }
        }
    }

    protected void load(String baseDir, 
                        String webModule, 
                        Mapping portletXMLMapping, 
                        Mapping webXMLMapping) 
    throws Exception {
        String directory = baseDir+webModule+File.separatorChar+"WEB-INF"+File.separatorChar;

        File portletXml = new File(directory+"portlet.xml");
        File webXml = new File(directory+"web.xml");

        // check for the porlet.xml. If there is no portlet.xml this is not a
        // portlet application web module
        if (portletXml.exists()) { // && (webXml.exists())) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Loading the following Portlet Applications XML files..."+portletXml+", "+webXml);
            }

            InputSource source = new InputSource(new FileInputStream(portletXml));
            source.setSystemId(portletXml.toURL().toExternalForm());
            
            Unmarshaller unmarshaller = new Unmarshaller(portletXMLMapping);
			unmarshaller.setIgnoreExtraElements(true);
            unmarshaller.setEntityResolver(this.resolver);
            unmarshaller.setValidation(false);
            PortletApplicationDefinitionImpl portletApp = 
                (PortletApplicationDefinitionImpl)unmarshaller.unmarshal( source );

            WebApplicationDefinitionImpl webApp = null;

            if (webXml.exists()) {
                source = new InputSource(new FileInputStream(webXml));
                source.setSystemId(webXml.toURL().toExternalForm());

                unmarshaller = new Unmarshaller(webXMLMapping);
				unmarshaller.setIgnoreExtraElements(true);
                unmarshaller.setEntityResolver(this.resolver);
                unmarshaller.setValidation(false);
                webApp = 
                    (WebApplicationDefinitionImpl)unmarshaller.unmarshal( source );

                Vector structure = new Vector();
                structure.add(portletApp);
                structure.add("/"+webModule);

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
            }

            this.registry.add( portletApp );

            // fill portletsKeyObjectId
            final Iterator portlets = portletApp.getPortletDefinitionList().iterator();
            while (portlets.hasNext()) {
                final PortletDefinition portlet = (PortletDefinition)portlets.next();
                portletsKeyObjectId.put(portlet.getId(), portlet);
                
                if (this.contextName.equals(webModule)) {
                    ((PortletDefinitionImpl)portlet).setLocalPortlet(true);
                }

            }
        }

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry#getPortletApplicationEntityList()
     */
    public PortletApplicationEntityList getPortletApplicationEntityList() {
        return this.portletApplicationEntities;
    }
    
}
