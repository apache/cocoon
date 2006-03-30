/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.tools.helper.PortalObjects;
import org.apache.cocoon.portal.tools.service.UserRightsService;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class PortalToolManager
    extends AbstractComponent
    implements Parameterizable {

	public static final String ROLE = PortalToolManager.class.getName();

	private Map tools = new HashMap();

	private List i18n = new ArrayList();

	private String rootDir;
	private String confFile;
	private String authFile;
	private static final String pluginDir = "plugins/";
	private static final String pluginConfFile = "tool.xml";
	private static final String i18nDir = "i18n/";

	private Configuration configuration;
	private UserRightsService userRightsService;

    /** The source resolver */
    protected SourceResolver resolver;

	/**
	 * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
	 */
	public void parameterize(Parameters para) throws ParameterException {
		this.rootDir = para.getParameter("root", "/");
		this.confFile = para.getParameter("conf", "conf.xml");
		this.authFile = para.getParameter("auth", "auth.xml");

        Source fSource = null;
		try {
		    fSource = this.resolver.resolveURI(rootDir + confFile);
		    DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder();
		    this.configuration = confBuilder.build(fSource.getInputStream());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            this.resolver.release(fSource);
        }
        fSource = null;
        try {
		    fSource = this.resolver.resolveURI(rootDir + authFile);
		    this.userRightsService = new UserRightsService();
		    this.userRightsService.setLocation(fSource);
		    this.userRightsService.initialize();
			this.init();
		} catch (ProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
        } finally {
            this.resolver.release(fSource);
        }
	}

	/**
	 * Initializes the PortalToolManager. Reads the configuration of all plugins etc.
	 * @throws ProcessingException
	 * @throws IOException
	 */
	public void init() throws ProcessingException, IOException {
        Source toolsDir = null;
        PortalToolBuilder builder = new PortalToolBuilder();
        try {
            toolsDir = this.resolver.resolveURI(rootDir + pluginDir);

            final File td = SourceUtil.getFile(toolsDir);

            if( td == null || !td.isDirectory() ) {
                throw new ProcessingException("PortalToolManager: tool-dir must be a directory: " + toolsDir.getURI());
            }
            final File[] dirs = td.listFiles();
            for(int i = 0; i< dirs.length; i++) {
                final File f = dirs[i];
                if (f.isDirectory()) {
                    String path = f.getAbsolutePath().endsWith(File.separator) ? f.getAbsolutePath() : f.getAbsoluteFile() + File.separator;
                    File conf = new File(path + pluginConfFile);
                    if (conf.exists()) {
                        PortalTool pTool = builder.buildTool(conf, this.rootDir, pluginDir, i18nDir);
                        if(pTool != null) {
	                	 	tools.put(pTool.getId(), pTool);
	                	 	i18n.addAll(pTool.getI18n());
                        }
                    }
                }
            }            
        } finally {
            this.resolver.release(toolsDir);
        }
	}

	/**
	 * Returns a Collection of all Tools
	 */
	public Collection getTools() {
		return tools.values();
	}

	/**
	 * Returns the tool with the id.
	 * @param id Tool-Id
	 */
	public PortalTool getTool(String id) {
		return (PortalTool) tools.get(id);
	}

	/**
	 * Returns a Collection of tools which offers functions
	 */
	public Collection getToolsWithFunctions() {
		ArrayList tmp = new ArrayList();
		for(Iterator it = tools.values().iterator(); it.hasNext();) {
			PortalTool pt;
			if(((pt = (PortalTool) it.next())).getPublicFunctions().size() > 0)
				tmp.add(pt);
		}
		return tmp;
	}

	public List getI18n() {
	    return i18n;
	}

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Returns the Configuration for the plugins
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Saves the configuration
     */
    public synchronized void saveConfiguration() {
        DefaultConfigurationSerializer confSer = new DefaultConfigurationSerializer();
        Source confSource = null;
        try {
	        confSource = this.resolver.resolveURI(rootDir + confFile);
            if (confSource instanceof ModifiableSource) {
                confSer.serialize(((ModifiableSource) confSource).getOutputStream(), configuration);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SourceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            this.resolver.release(confSource);
        }
    }

    public UserRightsService getUserRightsService() {
    	return this.userRightsService;
    }

    /**
     * Returns a value from the auth context 
     * @param key Path (e.g. /foo/bar)
     */
    public String sGet(String key) {
        if (!key.startsWith("/")) {
    		key = "/" + key;
        }
        key = this.getClass().getName() + key;
        return (String)this.portalService.getAttribute(key);
    }

    /**
     * Sets a value in the auth context
     * @param key Path (e.g. /foo/bar)
     * @param value Value
     */
    public void sSet(String key, String value) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        this.portalService.setAttribute(key, value);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        super.dispose();
    }

    public PortalObjects getPortalObjects() {
    	try {
			return new PortalObjects((PortalService) this.manager.lookup(org.apache.cocoon.portal.PortalService.ROLE));
    	} catch (ServiceException e) {
    		return null;
		}
    
    }

    public void releasePortalObjects(PortalObjects pObj) {
    	this.manager.release(pObj.getPortalService());
    }
}
