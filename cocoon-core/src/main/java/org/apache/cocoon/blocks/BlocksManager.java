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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Modifiable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.URLSource;
import org.xml.sax.InputSource;

/**
 * @version $Id$
 */
public class BlocksManager
	extends
		HttpServlet
    implements
    	Blocks,
        Modifiable
	{ 

    public static String ROLE = BlocksManager.class.getName();
    private ServletConfig servletConfig;
    private ServletContext servletContext;
    private ServiceManager serviceManager;
    private Context context;
    private org.apache.cocoon.environment.Context environmentContext;
    private Settings settings;
    private String contextURL;
    private String containerEncoding;

    private String wiringFileName = "/" + Constants.WIRING;
    private Source wiringFile;
    private HashMap blocks = new HashMap();
    private TreeMap mountedBlocks = new TreeMap(new InverseLexicographicalOrder());
    
    private Processor processor;
    private Logger logger;    

    public void init(ServletConfig servletConfig) throws ServletException {
    	super.init(servletConfig);
        this.containerEncoding = servletConfig.getInitParameter("container-encoding");
        if (this.containerEncoding == null) {
        	this.containerEncoding = "ISO-8859-1";
        }
    	this.servletConfig = servletConfig;
    	this.servletContext = servletConfig.getServletContext();
    	CoreUtil coreUtil = new CoreUtil(servletConfig, Constants.WIRING);
		Core core = coreUtil.getCore();
		this.settings = coreUtil.getSettings();
		this.environmentContext = core.getEnvironmentContext();
		this.context = core.getContext();
		this.contextURL = coreUtil.getContextURL();
		this.serviceManager = coreUtil.getServiceManager();
		LoggerUtil loggerUtil = new LoggerUtil(servletConfig, this.context, this.settings);
		this.logger = loggerUtil.getCocoonLogger();
		this.getLogger().debug("Initializing the Blocks Manager");
		
		InputSource is = null;
		try {
			this.getLogger().debug("Wiring file: " + this.servletContext.getResource(this.wiringFileName));
			URLSource urlSource = new URLSource();
			urlSource.init(this.servletContext.getResource(this.wiringFileName), null);
			this.wiringFile = new DelayedRefreshSourceWrapper(urlSource, 1000);
			is = SourceUtil.getInputSource(this.wiringFile);
		} catch (IOException e) {
			throw new ServletException("Could not open configuration file: " + this.wiringFileName, e);
		} catch (ProcessingException e) {
			throw new ServletException("Could not open configuration file: " + this.wiringFileName, e);			
		}
				
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		Configuration wiring = null;
		try {
			wiring = builder.build(is);
		} catch (Exception e) {
			throw new ServletException("Could not create configuration from file: " + this.wiringFileName, e);			
		}
		
		Configuration[] blockConfs = wiring.getChildren("block");
		
		try {
		// Create and store all blocks
		for (int i = 0; i < blockConfs.length; i++) {
			Configuration blockConf = blockConfs[i];
			this.getLogger().debug("Creating " + blockConf.getName() +
					" id=" + blockConf.getAttribute("id") +
					" location=" + blockConf.getAttribute("location"));
			BlockManager blockManager = new BlockManager();
			blockManager.setBlocks(this);
			LifecycleHelper.setupComponent(blockManager,
					this.getLogger(),
					this.context,
					this.serviceManager,
					blockConf);
			this.blocks.put(blockConf.getAttribute("id"), blockManager);
			String mountPath = blockConf.getChild("mount").getAttribute("path", null);
			if (mountPath != null) {
				this.mountedBlocks.put(mountPath, blockManager);
				this.getLogger().debug("Mounted block " + blockConf.getAttribute("id") +
						" at " + mountPath);
			}
		}
		} catch (Exception e) {
			throw new ServletException(e);
		}
		this.createProcessor();
    }
    
    public void destroy() {
        Iterator blocksIter = this.blocks.entrySet().iterator();
        while (blocksIter.hasNext()) {
            LifecycleHelper.dispose(blocksIter.next());
        }
        if (this.serviceManager != null) {
            this.serviceManager = null;            
        }
        this.blocks = null;
        this.mountedBlocks = null;
    }
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpEnvironment env;

        // We got it... Process the request
        String uri = request.getServletPath();
        if (uri == null) {
            uri = "";
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            // VG: WebLogic fix: Both uri and pathInfo starts with '/'
            // This problem exists only in WL6.1sp2, not in WL6.0sp2 or WL7.0b.
            if (uri.length() > 0 && uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            uri += pathInfo;
        }

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String prefix = request.getRequestURI();
            if (prefix == null) {
                prefix = "";
            }

            response.sendRedirect(response.encodeRedirectURL(prefix + "/"));
            return;
        }

        if (uri.charAt(0) == '/') {
        	uri = uri.substring(1);
        }

        String formEncoding = request.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.settings.getFormEncoding();
        }
        env = new HttpEnvironment(uri,
                                  this.contextURL,
                                  request,
                                  response,
                                  this.servletContext,
                                  this.environmentContext,
                                  this.containerEncoding,
                                  formEncoding);
        env.enableLogging(getLogger());
		
        try {
	        this.processor.process(env);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
		env.commitResponse();
	}

	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	private Logger getLogger() {
    	return this.logger;
    }

    private void createProcessor() {
        this.processor = new BlockDispatcherProcessor(this);
        ((BlockDispatcherProcessor)this.processor).enableLogging(this.getLogger());
    }
    
    public Block getBlock(String blockId) {
        return (Block)this.blocks.get(blockId);
    }
    
    /**
     * The block with the largest mount point that is a prefix of the URI is
     * chosen. The implementation could be made much more efficient.
     * @param uri
     */
    public Block getMountedBlock(String uri) {
        Block block = null;
        // All mount points that are before or equal to the URI in
        // lexicographical order. This includes all prefixes.
        Map possiblePrefixes = this.mountedBlocks.tailMap(uri);
        Iterator possiblePrefixesIt = possiblePrefixes.entrySet().iterator();
        // Find the largest prefix to the uri
        while (possiblePrefixesIt.hasNext()) {
            Map.Entry entry = (Map.Entry) possiblePrefixesIt.next();
            String mountPoint = (String)entry.getKey();
            if (uri.startsWith(mountPoint)) {
                block = (BlockManager)entry.getValue();
                break;
            }
        }
        return block;
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     *
     * @param date a <code>long</code> value
     * @return a <code>boolean</code> value
     */
    public boolean modifiedSince(long date) {
        return date < this.wiringFile.getLastModified();
    }

    private static class InverseLexicographicalOrder implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((String)o2).compareTo((String)o1);
        }
    }
}
