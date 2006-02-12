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
package org.apache.cocoon.blocks.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Modifiable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.blocks.Block;
import org.apache.cocoon.blocks.BlockConstants;
import org.apache.cocoon.blocks.Blocks;
import org.apache.cocoon.blocks.BlocksContext;
import org.apache.cocoon.blocks.ServiceManagerRegistry;
import org.apache.cocoon.blocks.ServiceManagerRegistryImpl;
import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.core.servlet.CoreUtil;
import org.apache.cocoon.core.servlet.LoggerUtil;
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
        Modifiable { 

    public static String ROLE = BlocksManager.class.getName();
    private BlocksContext blocksContext;
    private URL contextURL;

    private Source wiringFile;
    private HashMap blocks = new HashMap();
    private HashMap mountedBlocks = new HashMap();
    private Logger logger;
    private ClassLoader classLoader;
    private ServiceManagerRegistry serviceManagerRegistry = new ServiceManagerRegistryImpl();

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.blocksContext = new BlocksContext(this.getServletContext(), this);
        String contextURL0 = CoreUtil.getContextURL(this.blocksContext, BlockConstants.WIRING);
        try {
            this.contextURL = new URL(contextURL0);
        } catch (MalformedURLException e) {
            throw new ServletException("Could not parse " + contextURL0, e);
        }
        
        LoggerUtil loggerUtil =
            new LoggerUtil(this.getServletConfig(), BlockConstants.WIRING);
        this.logger = loggerUtil.getCocoonLogger();
        this.getLogger().debug("Initializing the Blocks Manager");
        
        InputSource is = null;
        try {
            this.getLogger().debug("Wiring file: " + this.getServletContext().getResource(BlockConstants.WIRING));
            URLSource urlSource = new URLSource();
            urlSource.init(this.getServletContext().getResource(BlockConstants.WIRING), null);
            this.wiringFile = new DelayedRefreshSourceWrapper(urlSource, 1000);
            is = SourceUtil.getInputSource(this.wiringFile);
        } catch (IOException e) {
            throw new ServletException("Could not open configuration file: " + BlockConstants.WIRING, e);
        } catch (ProcessingException e) {
            throw new ServletException("Could not open configuration file: " + BlockConstants.WIRING, e);                 
        }
        
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration wiring = null;
        try {
            wiring = builder.build(is);
        } catch (Exception e) {
            throw new ServletException("Could not create configuration from file: " + BlockConstants.WIRING, e);                  
        }
        
        ServletConfig blocksConfig =
            new ServletConfigurationWrapper(this.getServletConfig(), this.blocksContext);
        
        Configuration[] blockConfs = wiring.getChildren("block");
                
        // get all wired blocks and add them to the classloader
        List urlList = new ArrayList();        
        for (int i = 0; i < blockConfs.length; i++) {
            Configuration blockConf = blockConfs[i];
            String location = null;
            try {
                location = blockConf.getAttribute("location");
            } catch (ConfigurationException e) {
                throw new ServletException("Couldn't get location from the wiring file");
            }
            URL url;
            try {
                url = this.resolve(location);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new ServletException("Couldn't get location of the classes of the block", e);
            }
            if (url != null) {
                urlList.add(url);
                if(this.logger.isDebugEnabled()) {
                    this.logger.debug("added " + url.toString());
                }
            } else {
                if(this.logger.isDebugEnabled()) {
                    this.logger.debug("didn't add " + location);
                }                
            }
        }
        
        // setup the classloader using the current classloader as parent
        ClassLoader parentClassloader = Thread.currentThread().getContextClassLoader();
        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);        
        URLClassLoader classloader = new URLClassLoader(urls, parentClassloader);
        Thread.currentThread().setContextClassLoader(classloader);
        this.classLoader = Thread.currentThread().getContextClassLoader();
            
        // Create and store all blocks
        for (int i = 0; i < blockConfs.length; i++) {
            Configuration blockConf = blockConfs[i];
            String id = null;
            String location = null;
            try {
                id = blockConf.getAttribute("id");
                location = blockConf.getAttribute("location");
            } catch (ConfigurationException e) {
                throw new ServletException("Couldn't get id or location from the wiring file");
            }
            this.getLogger().debug("Creating " + blockConf.getName() +
                    " id=" + id +
                    " location=" + location);
            BlockManager blockManager = new BlockManager();
            try {
                blockManager.setContextURL(this.resolve(location));
            } catch (MalformedURLException e) {
                throw new ServletException("Could not resolve " + location, e);
            }
            blockManager.setServiceManagerRegistry(this.serviceManagerRegistry);
            try {
                LifecycleHelper.setupComponent(blockManager,
                        this.getLogger(),
                        null,
                        null,
                        blockConf);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            blockManager.init(blocksConfig);
            this.blocks.put(id, blockManager);
            String mountPath = blockConf.getChild("mount").getAttribute("path", null);
            if (mountPath != null) {
                this.mountedBlocks.put(fixPath(mountPath), blockManager);
                this.getLogger().debug("Mounted block " + id + " at " + mountPath);
            }
        }
    }
    
    public void destroy() {
        Iterator blocksIter = this.blocks.values().iterator();
        while (blocksIter.hasNext()) {
            ((Servlet)blocksIter.next()).destroy();
        }
        this.blocks = null;
        this.mountedBlocks = null;
        super.destroy();
    }
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = trimPath(request.getServletPath());
        String pathInfo = trimPath(request.getPathInfo());        
        String uri = servletPath + pathInfo;

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
        
        // set the blocks classloader for this thread
        Thread.currentThread().setContextClassLoader(this.classLoader);        

        RequestDispatcher dispatcher = this.blocksContext.getRequestDispatcher(pathInfo);
        if (dispatcher == null)
            throw new ServletException("No block mounted at " + pathInfo);

        dispatcher.forward(request, response);
    }
    
    private Logger getLogger() {
        return this.logger;
    }

    // Blocks specific methods
    
    public Block getBlock(String blockId) {
        return (Block)this.blocks.get(blockId);
    }
    
    public Block getMountedBlock(String uri) {
        return (Block)this.mountedBlocks.get(uri);
    }
    
    // Modified interface
    
    /**
     * Queries the class to estimate its ergodic period termination.
     *
     * @param date a <code>long</code> value
     * @return a <code>boolean</code> value
     */
    public boolean modifiedSince(long date) {
        return date < this.wiringFile.getLastModified();
    }
        
    /**
     * Resolve a path relative to the servlet context. Paths starting with '/' are
     * supposed to be relative the servlet context to follow the behavior from 
     * ServletContext.getResource. Use "file:" for file system paths instead.
     * @param path
     * @return
     * @throws MalformedURLException
     */
    private URL resolve(String path) throws MalformedURLException {
        if (path.charAt(0) == '/')
            path = path.substring(1);
        System.out.println("BlocksManager.resolve path=" + path +
                " contextURL=" + this.contextURL);

        URL result = new URL(this.contextURL, path);
        
        System.out.println("BlocksManager.resolve to=" + result);
        
        return result;
    }

    /**
     * Utility function to ensure that the parts of the request URI not is null
     * and not ends with /
     * @param path
     * @return the trimmed path
     */
    private static String trimPath(String path) {
        if (path == null)
                return "";
        int length = path.length();
        if (length > 0 && path.charAt(length - 1) == '/')
                path = path.substring(0, length - 1);
        return path;
    }
    
    /**
     * If a block is mounted on "/" it should be registred at "" to get the servlet
     * path right
     * @param path
     * @return fixed path
     */
    private static String fixPath(String path) {
        return "/".equals(path) ? "" : path;
    }
}
