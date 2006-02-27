/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.servlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.util.ComponentContext;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.servlet.SettingsHelper;

/**
 * This is an utility class to create Settings, Context and Core.
 * It is simplified compared to o.a.c.core.CoreUtil and get its input from
 * a ServletContext instead of from a BootstrapEnvironment
 * 
 * @version $Id$
 * @since 2.2
 */
public class CoreUtil {

    /**
     * Application <code>Context</code> Key for the servlet configuration
     * 
     * @since 2.1.3
     */
    public static final String CONTEXT_SERVLET_CONFIG = "servlet-config";

    public static MutableSettings createSettings(ServletConfig servletConfig) throws ServletException {
            // create settings
            ServletContext servletContext1 = servletConfig.getServletContext();
            
            // create an empty settings objects
            final MutableSettings s = new MutableSettings();
            
            // fill from the environment configuration, like web.xml etc.
            // fill from the servlet parameters
            SettingsHelper.fill(s, servletConfig);
            if (s.getWorkDirectory() == null) {
                final File workDir1 = (File) servletContext1.getAttribute("javax.servlet.context.tempdir");
                s.setWorkDirectory(workDir1.getAbsolutePath());
            }
            if (s.getLoggingConfiguration() == null) {
                s.setLoggingConfiguration("/WEB-INF/log4j.xconf");
            }
            MutableSettings settings = s;
            ServletContext servletContext = servletConfig.getServletContext();
    
            File contextForWriting = null;
            String writeableContextPath = CoreUtil.getWritableContextPath(servletContext);
            if (writeableContextPath != null) {
                contextForWriting = new File(writeableContextPath);
    }
    
            // first init the work-directory for the logger.
            // this is required if we are running inside a war file!
            final String workDirParam = settings.getWorkDirectory();
            File workDir;
            if (workDirParam != null) {
                if (contextForWriting == null) {
                    // No context path : consider work-directory as absolute
                    workDir = new File(workDirParam);
                } else {
                    // Context path exists : is work-directory absolute ?
                    File workDirParamFile = new File(workDirParam);
                    if (workDirParamFile.isAbsolute()) {
                        // Yes : keep it as is
                        workDir = workDirParamFile;
                    } else {
                        // No : consider it relative to context path
                        workDir = new File(contextForWriting, workDirParam);
                    }
                }
            } else {
                workDir = new File("cocoon-files");
            }
            workDir.mkdirs();
            settings.setWorkDirectory(workDir.getAbsolutePath());
    
            // Output some debug info
            servletContext.log("Writeable Context: " + contextForWriting);
            if (workDirParam != null) {
                servletContext.log("Using work-directory " + workDir);
            } else {
                servletContext.log("Using default work-directory " + workDir);
            }
    
            final String uploadDirParam = settings.getUploadDirectory();
            File uploadDir;
            if (uploadDirParam != null) {
                if (contextForWriting == null) {
                    uploadDir = new File(uploadDirParam);
                } else {
                    // Context path exists : is upload-directory absolute ?
                    File uploadDirParamFile = new File(uploadDirParam);
                    if (uploadDirParamFile.isAbsolute()) {
                        // Yes : keep it as is
                        uploadDir = uploadDirParamFile;
                    } else {
                        // No : consider it relative to context path
                        uploadDir = new File(contextForWriting, uploadDirParam);
                    }
                }
                servletContext.log("Using upload-directory " + uploadDir);
            } else {
                uploadDir = new File(workDir, "upload-dir" + File.separator);
                servletContext.log("Using default upload-directory " + uploadDir);
            }
            uploadDir.mkdirs();
            settings.setUploadDirectory(uploadDir.getAbsolutePath());
    
            String cacheDirParam = settings.getCacheDirectory();
            File cacheDir;
            if (cacheDirParam != null) {
                if (contextForWriting == null) {
                    cacheDir = new File(cacheDirParam);
                } else {
                    // Context path exists : is cache-directory absolute ?
                    File cacheDirParamFile = new File(cacheDirParam);
                    if (cacheDirParamFile.isAbsolute()) {
                        // Yes : keep it as is
                        cacheDir = cacheDirParamFile;
                    } else {
                        // No : consider it relative to context path
                        cacheDir = new File(contextForWriting, cacheDirParam);
                    }
                }
                servletContext.log("Using cache-directory " + cacheDir);
            } else {
                cacheDir = new File(workDir, "cache-dir" + File.separator);
                File parent = cacheDir.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                servletContext.log("cache-directory was not set - defaulting to " + cacheDir);
            }
            cacheDir.mkdirs();
            settings.setCacheDirectory(cacheDir.getAbsolutePath());
            
            /*
             * Doesn't work when the CoreUtil is called on both the BlocksManager
             * and the BlockManager level (which will be fixed later) furthermore
             * the configuration file path configuration isn't necessary as we have
             * default positions for the files.
             * 
            String configFileName = settings.getConfiguration();
            final String usedFileName;
    
            if (configFileName == null) {
                servletContext.log("Servlet initialization argument 'configurations' not specified, attempting to use '/WEB-INF/cocoon.xconf'");
                usedFileName = "/WEB-INF/cocoon.xconf";
            } else {
                usedFileName = configFileName;
            }
    
            servletContext.log("Using configuration file: " + usedFileName);
    
            URL result;
            try {
                // test if this is a qualified url
                if (usedFileName.indexOf(':') == -1) {
                    result = servletContext.getResource(usedFileName);
                } else {
                    result = new URL(usedFileName);
                }
            } catch (Exception mue) {
                String msg = "Init parameter 'configurations' is invalid : " + usedFileName;
                servletContext.log(msg, mue);
                throw new ServletException(msg, mue);
            }
    
            if (result == null) {
                File resultFile = new File(usedFileName);
                if (resultFile.isFile()) {
                    try {
                        result = resultFile.getCanonicalFile().toURL();
                    } catch (Exception e) {
                        String msg = "Init parameter 'configurations' is invalid : "
                                + usedFileName;
                        servletContext.log(msg, e);
                        throw new ServletException(msg, e);
                    }
                }
            }
    
            if (result == null) {
                String msg = "Init parameter 'configurations' doesn't name an existing resource : "
                        + usedFileName;
                servletContext.log(msg);
                throw new ServletException(msg);
            }
    
            // update configuration
            final URL u = result;
            settings.setConfiguration(u.toExternalForm());
            */
            
            // settings can't be changed anymore
            settings.makeReadOnly();
            
            return settings;
        }

    public static DefaultContext createContext(ServletConfig servletConfig,
            Settings settings, String knownFile) throws ServletException {
        DefaultContext appContext = new ComponentContext();
        CoreUtil.addSourceResolverContext(appContext, servletConfig, knownFile);
        CoreUtil.addSettingsContext(appContext, settings);
        CoreUtil.addCoreContext(appContext, new Core(appContext));
        return appContext;
    }

    public static String getWritableContextPath(ServletContext servletContext) {
        return servletContext.getRealPath("/");
    }

    /**
     * @param servletContext
     * @throws ServletException
     */
    public static String getContextURL(ServletContext servletContext, String knownFile)
    throws ServletException {
        String path = CoreUtil.getWritableContextPath(servletContext);
        String contextURL;
        if (path == null) {
            // Try to figure out the path of the root from that of a known file
            servletContext.log("Figuring out root from " + knownFile);
            URL url;
            try {
                url = servletContext.getResource(knownFile); 
            } catch (MalformedURLException me) {
                throw new ServletException("Unable to get resource '"
                        + knownFile + "'.", me);
            }
            if (url == null)
                throw new ServletException("Couldn't find " + knownFile);
            path = url.toString();
            servletContext.log("Got " + path);

            path = path.substring(0, path.length() - (knownFile.length() - 1));
            servletContext.log("And servlet root " + path);
        }
        try {
            if (path.indexOf(':') > 1) {
                contextURL = path;
            } else {
                contextURL = new File(path).toURL().toExternalForm();
            }
        } catch (MalformedURLException me) {
            // VG: Novell has absolute file names starting with the
            // volume name which is easily more then one letter.
            // Examples: sys:/apache/cocoon or sys:\apache\cocoon
            try {
                contextURL = new File(path).toURL().toExternalForm();
            } catch (MalformedURLException ignored) {
                throw new ServletException(
                        "Unable to determine servlet context URL.", me);
            }
        }
        return contextURL;
    }
    
    /*
     * set up the appContext with some values to make
     * the simple source resolver work
     */
    private static void addSourceResolverContext(DefaultContext appContext,
            ServletConfig servletConfig,
            String knownFile) throws ServletException {
        
        ServletContext servletContext = servletConfig.getServletContext();
        String contextURL = CoreUtil.getContextURL(servletContext, knownFile);
        servletContext.log("Context URL: " + contextURL);

        // add root url
        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(contextURL));
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }

        Context environmentContext = new HttpContext(servletConfig.getServletContext());
        // add environment context
        appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, environmentContext);

        // now add environment specific information
        appContext.put(CONTEXT_SERVLET_CONFIG, servletConfig);        
    }

    private static void addSettingsContext(DefaultContext appContext, Settings settings) {
        appContext.put(Constants.CONTEXT_WORK_DIR, new File(settings.getWorkDirectory()));

        appContext.put(Constants.CONTEXT_UPLOAD_DIR, new File(settings.getUploadDirectory()));

        appContext.put(Constants.CONTEXT_CACHE_DIR, new File(settings.getCacheDirectory()));

        //appContext.put(Constants.CONTEXT_CONFIG_URL, settings.getConfiguration());

        // set encoding
        appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, settings.getFormEncoding());

        // set class loader
        appContext.put(Constants.CONTEXT_CLASS_LOADER, null);

        // FIXME - for now we just set an empty string as this information is
        // looked up
        // by other components
        appContext.put(Constants.CONTEXT_CLASSPATH, "");        
    }

    private static void addCoreContext(DefaultContext appContext, Core core) {
        // put the core into the context - this is for internal use only
        // The Cocoon container fetches the Core object using the context.
        appContext.put(Core.ROLE, core);
    }
}
