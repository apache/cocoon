/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.servlet;

import org.apache.avalon.excalibur.logger.DefaultLogKitManager;
import org.apache.avalon.excalibur.logger.LogKitManager;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.request.RequestFactory;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.StringUtils;
import org.apache.cocoon.util.log.CocoonLogFormatter;
import org.apache.excalibur.instrument.InstrumentManager;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.log.ContextMap;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.output.ServletOutputLogTarget;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: CocoonServlet.java,v 1.2 2003/03/16 14:25:31 stefano Exp $
 */
public class CocoonServlet extends HttpServlet {

    protected static final String PROCESSED_BY = "Processed by "
        + Constants.COMPLETE_NAME + " in ";

    protected Logger log;
    protected LogKitManager logKitManager;

    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;

    /** The time the cocoon instance was created */
    protected long creationTime = 0;

    /** The <code>Cocoon</code> instance */
    protected Cocoon cocoon;

    protected Exception exception;

    protected DefaultContext appContext = new DefaultContext();

    /** Allow reloading of cocoon by specifying the cocoon-reload parameter with a request */
    protected boolean allowReload;

    /** Allow adding processing time to the response */
    protected boolean showTime;
    protected boolean hiddenShowTime;

    private static final boolean SAVE_UPLOADS_TO_DISK = true;
    private static final int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    private int maxUploadSize;
    private boolean autoSaveUploads;
    private boolean allowOverwrite;
    private boolean silentlyRename;

    private File uploadDir;
    private File workDir;
    private File cacheDir;
    private String containerEncoding;
    private String defaultFormEncoding;

    protected ServletContext servletContext;

    /** The classloader that will be set as the context classloader if init-classloader is true */
    protected ClassLoader classLoader = this.getClass().getClassLoader();
    protected boolean initClassLoader = false;

    private String parentComponentManagerClass;

    protected String forceLoadParameter;
    protected String forceSystemProperty;

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    private boolean manageExceptions;

    /**
     * Flag to enable avalon excalibur instrumentation of Cocoon.
     */
    private boolean enableInstrumentation;

    /**
     * The <code>InstrumentManager</code> instance
     */
    private DefaultInstrumentManager instrumentManager;

    /**
     * This is the path to the servlet context (or the result
     * of calling getRealPath('/') on the ServletContext.
     * Note, that this can be null.
     */
    protected String servletContextPath;

    /**
     * This is the url to the servlet context directory
     */
    protected URL servletContextURL;


    /**
     * The requestFactory to use for incoming HTTP requests.
     */
    protected RequestFactory requestFactory;

    /**
     * Initialize this <code>CocoonServlet</code> instance.  You will
     * notice that I have broken the init into sub methods to make it
     * easier to maintain (BL).  The context is passed to a couple of
     * the subroutines.  This is also because it is better to explicitly
     * pass variables than implicitely.  It is both more maintainable,
     * and more elegant.
     *
     * @param conf The ServletConfig object from the servlet engine.
     *
     * @throws ServletException
     */
    public void init(ServletConfig conf)
    throws ServletException {

        super.init(conf);

        final String initClassLoaderParam = conf.getInitParameter("init-classloader");
        this.initClassLoader = "true".equalsIgnoreCase(initClassLoaderParam) ||
                               "yes".equalsIgnoreCase(initClassLoaderParam);

        if (this.initClassLoader) {
            // Force context classloader so that JAXP can work correctly
            // (see javax.xml.parsers.FactoryFinder.findClassLoader())
            try {
                Thread.currentThread().setContextClassLoader(this.classLoader);
            } catch (Exception e){}
        }

        String value;

        // FIXME (VG): We shouldn't have to specify these. Need to override
        // jaxp implementation of weblogic before initializing logger.
        // This piece of code is also required in the Cocoon class.
        value = System.getProperty("javax.xml.parsers.SAXParserFactory");
        if (value != null && value.startsWith("weblogic")) {
            System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        }

        this.servletContext = conf.getServletContext();
        this.appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, new HttpContext(this.servletContext));
        this.servletContextPath = this.servletContext.getRealPath("/");

        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = conf.getInitParameter("work-directory");
        if ((workDirParam != null) && (!workDirParam.trim().equals(""))) {
            if (this.servletContextPath == null) {
                // No context path : consider work-directory as absolute
                this.workDir = new File(workDirParam);
            } else {
                // Context path exists : is work-directory absolute ?
                File workDirParamFile = new File(workDirParam);
                if (workDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    this.workDir = workDirParamFile;
                } else {
                    // No : consider it relative to context path
                    this.workDir = new File(servletContextPath , workDirParam);
                }
            }
        } else {
            this.workDir = (File) this.servletContext.getAttribute("javax.servlet.context.tempdir");
            this.workDir = new File(workDir, "cocoon-files");
        }
        this.workDir.mkdirs();

        initLogger();
        String path = this.servletContextPath;
        if (log.isDebugEnabled()) {
            log.debug("getRealPath for /: " + path);
        }
        if (path == null) {
            // Try to figure out the path of the root from that of WEB-INF
            try {
                path = this.servletContext.getResource("/WEB-INF").toString();
            } catch (MalformedURLException me) {
                throw new ServletException("Unable to get resource 'WEB-INF'.", me);
            }
            if (log.isDebugEnabled()) {
                log.debug("getResource for /WEB-INF: " + path);
            }
            path = path.substring(0,path.length() - "WEB-INF".length());
            if (log.isDebugEnabled()) {
                log.debug("Path for Root: " + path);
            }
        }

        try {
            if (path.indexOf(':') > 1) {
                this.servletContextURL = new URL(path);
            } else {
                this.servletContextURL = new File(path).toURL();
            }
        } catch (MalformedURLException me) {
            // VG: Novell has absolute file names starting with the
            // volume name which is easily more then one letter.
            // Examples: sys:/apache/cocoon or sys:\apache\cocoon
            try {
                this.servletContextURL = new File(path).toURL();
            } catch (MalformedURLException ignored) {
                throw new ServletException("Unable to determine servlet context URL.", me);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("URL for Root: " + this.servletContextURL);
        }

        this.forceLoadParameter = conf.getInitParameter("load-class");
        if (conf.getInitParameter("load-class") == null) {
            if (log.isDebugEnabled()) {
                log.debug("load-class was not set - defaulting to false?");
            }
        }

        this.forceSystemProperty = conf.getInitParameter("force-property");

        // add work directory
        if ((workDirParam != null) && (!workDirParam.trim().equals(""))) {
            if (log.isDebugEnabled()) {
                log.debug("Using work-directory " + this.workDir);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("work-directory was not set - defaulting to " + this.workDir);
            }
        }
        this.appContext.put(Constants.CONTEXT_WORK_DIR, workDir);

        final String uploadDirParam = conf.getInitParameter("upload-directory");
        if ((uploadDirParam != null) && (!uploadDirParam.trim().equals(""))) {
            if (this.servletContextPath == null) {
                this.uploadDir = new File(uploadDirParam);
            } else {
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    this.uploadDir = uploadDirParamFile;
                } else {
                    // No : consider it relative to context path
                    this.uploadDir = new File(servletContextPath , uploadDirParam);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Using upload-directory " + this.uploadDir);
            }
        } else {
            this.uploadDir = new File(workDir, "upload-dir" + File.separator);
            if (log.isDebugEnabled()) {
                log.debug("upload-directory was not set - defaulting to " + this.uploadDir);
            }
        }
        this.uploadDir.mkdirs();
        this.appContext.put(Constants.CONTEXT_UPLOAD_DIR, this.uploadDir);

        value = conf.getInitParameter("autosave-uploads");
        if (value != null) {
            this.autoSaveUploads = ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
        } else {
            this.autoSaveUploads = SAVE_UPLOADS_TO_DISK;
            if (log.isDebugEnabled()) {
               log.debug("autosave-uploads was not set - defaulting to " + this.autoSaveUploads);
            }
        }

        String overwriteParam = conf.getInitParameter("overwrite-uploads");
        // accepted values are deny|allow|rename - rename is default.
        if (overwriteParam == null) {
            if (log.isDebugEnabled()) {
               log.debug("overwrite-uploads was not set - defaulting to rename");
            }
        }
        if ("deny".equalsIgnoreCase(overwriteParam)) {
            this.allowOverwrite = false;
            this.silentlyRename = false;
        } else if ("allow".equalsIgnoreCase(overwriteParam)) {
           this.allowOverwrite = true;
           this.silentlyRename = false; // ignored in this case
        } else {
           // either rename is specified or unsupported value - default to rename.
           this.allowOverwrite = false;
           this.silentlyRename = true;
        }

        this.maxUploadSize = MAX_UPLOAD_SIZE;
        String maxSizeParam = conf.getInitParameter("upload-max-size");
        if ((maxSizeParam != null) && (!maxSizeParam.trim().equals(""))) {
            this.maxUploadSize = Integer.parseInt(maxSizeParam);
        }

        String cacheDirParam = conf.getInitParameter("cache-directory");
        if ((cacheDirParam != null) && (!cacheDirParam.trim().equals(""))) {
            if (this.servletContextPath == null) {
                this.cacheDir = new File(cacheDirParam);
            } else {
                // Context path exists : is cache-directory absolute ?
                File cacheDirParamFile = new File(cacheDirParam);
                if (cacheDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    this.cacheDir = cacheDirParamFile;
                } else {
                    // No : consider it relative to context path
                    this.cacheDir = new File(servletContextPath , cacheDirParam);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Using cache-directory " + this.cacheDir);
            }
        } else {
            this.cacheDir = IOUtils.createFile(workDir, "cache-dir" + File.separator);
            if (log.isDebugEnabled()) {
                log.debug("cache-directory was not set - defaulting to " + this.cacheDir);
            }
        }
        this.cacheDir.mkdirs();
        this.appContext.put(Constants.CONTEXT_CACHE_DIR, this.cacheDir);

        this.appContext.put(Constants.CONTEXT_CONFIG_URL,
                            getConfigFile(conf.getInitParameter("configurations")));
        if (conf.getInitParameter("configurations") == null) {
            if (log.isDebugEnabled()) {
                log.debug("configurations was not set - defaulting to... ?");
            }
        }

        // get allow reload parameter, default is true
        value = conf.getInitParameter("allow-reload");
        this.allowReload = (value == null || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("allow-reload was not set - defaulting to true");
            }
        }

        value = conf.getInitParameter("show-time");
        this.showTime = "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
            || (this.hiddenShowTime = "hide".equals(value));
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("show-time was not set - defaulting to false");
            }
        }

        parentComponentManagerClass = conf.getInitParameter("parent-component-manager");
        if (parentComponentManagerClass == null) {
            if (log.isDebugEnabled()) {
                log.debug("parent-component-manager was not set - defaulting to null.");
            }
        }

        value = conf.getInitParameter("request-factory");
        if (value == null) {
            value = "org.apache.cocoon.components.request.MultipartRequestFactoryImpl";
            if (log.isDebugEnabled()) {
                log.debug("request-factory was not set - defaulting to " + value);
            }
        }
        this.requestFactory = RequestFactory.getRequestFactory(value);

        this.containerEncoding = conf.getInitParameter("container-encoding");
        if (containerEncoding == null) {
            containerEncoding = "ISO-8859-1";
            if (log.isDebugEnabled()) {
                log.debug("container-encoding was not set - defaulting to ISO-8859-1.");
            }
        }

        this.defaultFormEncoding = conf.getInitParameter("form-encoding");
        if (defaultFormEncoding == null) {
            if (log.isDebugEnabled()) {
                log.debug("form-encoding was not set - defaulting to null.");
            }
        }

        value = conf.getInitParameter("manage-exceptions");
        this.manageExceptions = (value == null || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("Parameter manageExceptions was not set - defaulting to true.");
            }
        }

        value = conf.getInitParameter("enable-instrumentation");
        this.enableInstrumentation =
            "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("enable-instrumentation was not set - defaulting to false.");
            }
        }

        this.createCocoon();
    }

    /**
     * Dispose Cocoon when servlet is destroyed
     */
    public void destroy()
    {
        if (this.initClassLoader) {
            try {
                Thread.currentThread().setContextClassLoader(this.classLoader);
            } catch (Exception e){}
        }

        if (this.cocoon != null) {
            if (log.isDebugEnabled()) {
                log.debug("Servlet destroyed - disposing Cocoon");
            }
            this.disposeCocoon();
        }

        if (this.enableInstrumentation) {
            this.instrumentManager.dispose();
        }
    }

    /**
     * Adds an URL to the classloader. Does nothing here, but is
     * overriden in {@link ParanoidCocoonServlet}.
     */
    protected void addClassLoaderURL(URL URL) {
        // Nothing
    }

    /**
     * Adds a directory to the classloader. Does nothing here, but is
     * overriden in {@link ParanoidCocoonServlet}.
     */
    protected void addClassLoaderDirectory(String dir) {
        // Nothing
    }

    /**
     * This builds the important ClassPath used by this Servlet.  It
     * does so in a Servlet Engine neutral way.  It uses the
     * <code>ServletContext</code>'s <code>getRealPath</code> method
     * to get the Servlet 2.2 identified classes and lib directories.
     * It iterates in alphabetical order through every file in the
     * lib directory and adds it to the classpath.
     *
     * Also, we add the files to the ClassLoader for the Cocoon system.
     * In order to protect ourselves from skitzofrantic classloaders,
     * we need to work with a known one.
     *
     * We need to get this to work properly when Cocoon is in a war.
     *
     * @throws ServletException
     */
     protected String getClassPath() throws ServletException {
        StringBuffer buildClassPath = new StringBuffer();

        File root = null;
        if (servletContextPath != null) {
            // Old method.  There *MUST* be a better method than this...

            String classDir = this.servletContext.getRealPath("/WEB-INF/classes");
            String libDir = this.servletContext.getRealPath("/WEB-INF/lib");

            if (libDir != null) {
                root = new File(libDir);
            }

            if (classDir != null) {
                buildClassPath.append(classDir);

                addClassLoaderDirectory(classDir);
            }
        } else {
            // New(ish) method for war'd deployments
            URL classDirURL = null;
            URL libDirURL = null;

            try {
                classDirURL = this.servletContext.getResource("/WEB-INF/classes");
            } catch (MalformedURLException me) {
                if (log.isWarnEnabled()) {
                    this.log.warn("Unable to add WEB-INF/classes to the classpath", me);
                }
            }

            try {
                libDirURL = this.servletContext.getResource("/WEB-INF/lib");
            } catch (MalformedURLException me) {
                if (log.isWarnEnabled()) {
                    this.log.warn("Unable to add WEB-INF/lib to the classpath", me);
                }
            }

            if (libDirURL != null && libDirURL.toExternalForm().startsWith("file:")) {
                root = new File(libDirURL.toExternalForm().substring("file:".length()));
            }

            if (classDirURL != null) {
                buildClassPath.append(classDirURL.toExternalForm());

                addClassLoaderURL(classDirURL);
            }
        }

        // Unable to find lib directory. Going the hard way.
        if (root == null) {
            root = extractLibraries();
        }

        if (root != null && root.isDirectory()) {
            File[] libraries = root.listFiles();
            Arrays.sort(libraries);
            for (int i = 0; i < libraries.length; i++) {
                String fullName = IOUtils.getFullFilename(libraries[i]);
                buildClassPath.append(File.pathSeparatorChar).append(fullName);

                addClassLoaderDirectory(fullName);
            }
        }

        buildClassPath.append(File.pathSeparatorChar)
                      .append(System.getProperty("java.class.path"));

        buildClassPath.append(File.pathSeparatorChar)
                      .append(getExtraClassPath());
        return buildClassPath.toString();
     }

    private File extractLibraries() {
        try {
            URL manifestURL = this.servletContext.getResource("/META-INF/MANIFEST.MF");
            if (manifestURL == null) {
                this.log.fatalError("Unable to get Manifest");
                return null;
            }

            Manifest mf = new Manifest(manifestURL.openStream());
            Attributes attr = mf.getMainAttributes();
            String libValue = attr.getValue("Cocoon-Libs");
            if (libValue == null) {
                this.log.fatalError("Unable to get 'Cocoon-Libs' attribute from the Manifest");
                return null;
            }

            List libList = new ArrayList();
            for(StringTokenizer st = new StringTokenizer(libValue, " "); st.hasMoreTokens();) {
                libList.add(st.nextToken());
            }

            File root = new File(this.workDir, "lib");
            root.mkdirs();

            File[] oldLibs = root.listFiles();
            for (int i = 0; i < oldLibs.length; i++) {
                String oldLib = oldLibs[i].getName();
                if (!libList.contains(oldLib)) {
                    this.log.debug("Removing old library " + oldLibs[i]);
                    oldLibs[i].delete();
                }
            }

            this.log.warn("Extracting libraries into " + root);
            byte[] buffer = new byte[65536];
            for (Iterator i = libList.iterator(); i.hasNext();) {
                String libName = (String)i.next();

                long lastModified = -1;
                try {
                    lastModified = Long.parseLong(attr.getValue("Cocoon-Lib-" + libName.replace('.', '_')));
                } catch (Exception e) {
                    this.log.debug("Failed to parse lastModified: " + attr.getValue("Cocoon-Lib-" + libName.replace('.', '_')));
                }

                File lib = new File(root, libName);
                if (lib.exists() && lib.lastModified() != lastModified) {
                    this.log.debug("Removing modified library " + lib);
                    lib.delete();
                }

                InputStream is = this.servletContext.getResourceAsStream("/WEB-INF/lib/" + libName);
                if (is == null) {
                    this.log.warn("Skipping " + libName);
                } else {
                    this.log.debug("Extracting " + libName);
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(lib);
                        int count;
                        while((count = is.read(buffer)) > 0) {
                            os.write(buffer, 0, count);
                        }
                    } finally {
                        if (is != null) is.close();
                        if (os != null) os.close();
                    }
                }

                if (lastModified != -1) {
                    lib.setLastModified(lastModified);
                }
            }

            return root;
        } catch (IOException e) {
            this.log.fatalError("Exception while processing Manifest file", e);
            return null;
        }
    }


    /**
     * Retreives the "extra-classpath" attribute, that needs to be
     * added to the class path.
     *
     * @throws ServletException
     */
     protected String getExtraClassPath()
     throws ServletException {
         String extraClassPath = this.getInitParameter("extra-classpath");
         if ((extraClassPath != null) && !extraClassPath.trim().equals("")) {
             StringBuffer sb = new StringBuffer();
             StringTokenizer st = new StringTokenizer(extraClassPath, System.getProperty("path.separator"), false);
             int i = 0;
             while (st.hasMoreTokens()) {
                 String s = st.nextToken();
                 if (i++ > 0) {
                     sb.append(File.pathSeparatorChar);
                 }
                 if ((s.charAt(0) == File.separatorChar) ||
                     (s.charAt(1) == ':')) {
                     if (log.isDebugEnabled()) {
                         log.debug ("extraClassPath is absolute: " + s);
                     }
                     sb.append(s);

                     addClassLoaderDirectory(s);
                 } else {
                     if (s.indexOf("${") != -1) {
                         String path = StringUtils.replaceToken(s);
                         sb.append(path);
                         if (log.isDebugEnabled()) {
                             log.debug ("extraClassPath is not absolute replacing using token: [" + s + "] : " + path);
                         }
                         addClassLoaderDirectory(path);
                     } else {
                         String path = null;
                         if (this.servletContextPath != null) {
                             path = this.servletContextPath + s;
                             if (log.isDebugEnabled()) {
                                 log.debug ("extraClassPath is not absolute pre-pending context path: " + path);
                             }
                         } else {
                             path = this.workDir.toString() + s;
                             if (log.isDebugEnabled()) {
                                 log.debug ("extraClassPath is not absolute pre-pending work-directory: " + path);
                             }
                         }
                         sb.append(path);
                         addClassLoaderDirectory(path);
                     }
                 }
             }
             return sb.toString();
         }
         return "";
     }

    /**
     * Set up the log level and path.  The default log level is
     * Priority.ERROR, although it can be overwritten by the parameter
     * "log-level".  The log system goes to both a file and the Servlet
     * container's log system.  Only messages that are Priority.ERROR
     * and above go to the servlet context.  The log messages can
     * be as restrictive (Priority.FATAL_ERROR and above) or as liberal
     * (Priority.DEBUG and above) as you want that get routed to the
     * file.
     */
    private void initLogger() {
        String logLevel = getInitParameter("log-level");
        if (logLevel == null) {
            logLevel = "INFO";
        }

        final String accesslogger = getInitParameter("servlet-logger");

        final Priority logPriority = Priority.getPriorityForName(logLevel.trim());

        final ServletOutputLogTarget servTarget = new ServletOutputLogTarget(this.servletContext);

        final CocoonLogFormatter formatter = new CocoonLogFormatter();
        formatter.setFormat( "%7.7{priority} %{time}   [%8.8{category}] " +
                             "(%{uri}) %{thread}/%{class:short}: %{message}\\n%{throwable}" );

        servTarget.setFormatter(formatter);
        Hierarchy.getDefaultHierarchy().setDefaultLogTarget(servTarget);
        Hierarchy.getDefaultHierarchy().setDefaultPriority(logPriority);
        final Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("");
        final DefaultLogKitManager logKitManager = new DefaultLogKitManager(Hierarchy.getDefaultHierarchy());
        logKitManager.setLogger(logger);
        final DefaultContext subcontext = new DefaultContext(this.appContext);
        subcontext.put("servlet-context", this.servletContext);
        if (this.servletContextPath == null) {
            File logSCDir = new File(this.workDir, "log");
            logSCDir.mkdirs();
            if (logger.isWarnEnabled()) {
                logger.warn("Setting servlet-context for LogKit to " + logSCDir);
            }
            subcontext.put("context-root", logSCDir.toString());
        } else {
            subcontext.put("context-root", this.servletContextPath);
        }

        try {
            logKitManager.contextualize(subcontext);
            this.logKitManager = logKitManager;

            //Configure the logkit management
            String logkitConfig = getInitParameter("logkit-config");
            if (logkitConfig == null) {
                logkitConfig = "/WEB-INF/logkit.xconf";
            }

            InputStream is = this.servletContext.getResourceAsStream(logkitConfig);
            if (is == null) is = new FileInputStream(logkitConfig);
            final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            final Configuration conf = builder.build(is);
            logKitManager.configure(conf);
        } catch (Exception e) {
            Hierarchy.getDefaultHierarchy().log("Could not set up Cocoon Logger, will use screen instead", e);
        }

        if (accesslogger != null) {
            this.log = logKitManager.getLogger(accesslogger);
        } else {
            this.log = logKitManager.getLogger("cocoon");
        }

    }

    /**
     * Set the ConfigFile for the Cocoon object.
     *
     * @param configFileName The file location for the cocoon.xconf
     *
     * @throws ServletException
     */
    private URL getConfigFile(final String configFileName)
    throws ServletException {
        final String usedFileName;

        if (configFileName == null) {
            if (log.isWarnEnabled()) {
                log.warn("Servlet initialization argument 'configurations' not specified, attempting to use '/WEB-INF/cocoon.xconf'");
            }
            usedFileName = "/WEB-INF/cocoon.xconf";
        } else {
            usedFileName = configFileName;
        }

        if (log.isDebugEnabled()) {
            log.debug("Using configuration file: " + usedFileName);
        }

        URL result;
        try {
            result = this.servletContext.getResource(usedFileName);
        } catch (Exception mue) {
            String msg = "Init parameter 'configurations' is invalid : " + usedFileName;
            log.error(msg, mue);
            throw new ServletException(msg, mue);
        }

        if (result == null) {
            File resultFile = new File(usedFileName);
            if (resultFile.isFile()) try {
                result = resultFile.getCanonicalFile().toURL();
            } catch (Exception e) {
                String msg = "Init parameter 'configurations' is invalid : " + usedFileName;
                log.error(msg, e);
                throw new ServletException(msg, e);
            }
        }

        if (result == null) {
            String msg = "Init parameter 'configuration' doesn't name an existing resource : " + usedFileName;
            log.error(msg);
            throw new ServletException(msg);
        }
        return result;
    }

    /**
     * Handle the "force-load" parameter.  This overcomes limits in
     * many classpath issues.  One of the more notorious ones is a
     * bug in WebSphere that does not load the URL handler for the
     * "classloader://" protocol.  In order to overcome that bug,
     * set "force-load" to "com.ibm.servlet.classloader.Handler".
     *
     * If you need to force more than one class to load, then
     * separate each entry with whitespace, a comma, or a semi-colon.
     * Cocoon will strip any whitespace from the entry.
     */
    private void forceLoad() {
        if (this.forceLoadParameter != null) {
            StringTokenizer fqcnTokenizer = new StringTokenizer(forceLoadParameter, " \t\r\n\f;,", false);

            while (fqcnTokenizer.hasMoreTokens()) {
                final String fqcn = fqcnTokenizer.nextToken().trim();

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Trying to load class: " + fqcn);
                    }
                    ClassUtils.loadClass(fqcn).newInstance();
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not force-load class: " + fqcn, e);
                    }
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
        }
    }

    /**
     * Handle the "force-property" parameter.
     *
     * If you need to force more than one property to load, then
     * separate each entry with whitespace, a comma, or a semi-colon.
     * Cocoon will strip any whitespace from the entry.
     */
    private void forceProperty() {
        if (this.forceSystemProperty != null) {
            StringTokenizer tokenizer = new StringTokenizer(forceSystemProperty, " \t\r\n\f;,", false);

            java.util.Properties systemProps = System.getProperties();
            while (tokenizer.hasMoreTokens()) {
                final String property = tokenizer.nextToken().trim();
                if (property.indexOf('=') == -1) {
                    continue;
                }
                try {
                    String key = property.substring(0,property.indexOf('='));
                    String value = property.substring(property.indexOf('=') + 1);
                    if (value.indexOf("${") != -1) {
                         value = StringUtils.replaceToken(value);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("setting " + key + "=" + value);
                    }
                    systemProps.setProperty(key,value);
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not set property: " + property, e);
                    }
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
            System.setProperties(systemProps);
        }
    }

    /**
     * Process the specified <code>HttpServletRequest</code> producing output
     * on the specified <code>HttpServletResponse</code>.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

        /* HACK for reducing class loader problems.                                     */
        /* example: xalan extensions fail if someone adds xalan jars in tomcat3.2.1/lib */
        if (this.initClassLoader) {
            try {
                Thread.currentThread().setContextClassLoader(this.classLoader);
            } catch (Exception e){}
        }

        // This is more scalable
        long start = System.currentTimeMillis();
        res.addHeader("X-Cocoon-Version", Constants.VERSION);
        HttpServletRequest request = requestFactory.getServletRequest(req,
                                         this.autoSaveUploads,
                                         this.uploadDir,
                                         this.allowOverwrite,
                                         this.silentlyRename,
                                         this.maxUploadSize);

        getCocoon(request.getPathInfo(), request.getParameter(Constants.RELOAD_PARAM));

        // Check if cocoon was initialized
        if (this.cocoon == null) {
            manageException(request, res, null, null,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Initialization Problem",
                            null /* "Cocoon was not initialized" */,
                            null /* "Cocoon was not initialized, cannot process request" */,
                            this.exception);
            return;
        }

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

            res.sendRedirect(res.encodeRedirectURL(prefix + "/"));
            return;
        }

        String contentType = null;
        ContextMap ctxMap = null;
        
        Environment env; 
        try{
            if (uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            env = getEnvironment(URLDecoder.decode(uri), request, res);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Problem with Cocoon servlet", e);
            }

            manageException(request, res, null, uri,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Problem in creating the Environment", null, null, e);
            return;
        }
            
        try {
            try {
                // Initialize a fresh log context containing the object model: it
                // will be used by the CocoonLogFormatter
                ctxMap = ContextMap.getCurrentContext();
                // Add thread name (default content for empty context)
                String threadName = Thread.currentThread().getName();
                ctxMap.set("threadName", threadName);
                // Add the object model
                ctxMap.set("objectModel", env.getObjectModel());
                // Add a unique request id (threadName + currentTime
                ctxMap.set("request-id", threadName + System.currentTimeMillis());

                if (this.cocoon.process(env)) {
                    contentType = env.getContentType();
                } else {
                    // We reach this when there is nothing in the processing change that matches
                    // the request. For example, no matcher matches.
                    log.fatalError("The Cocoon engine failed to process the request.");
                    manageException(request, res, env, uri,
                                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "Request Processing Failed",
                                    "Cocoon engine failed in process the request",
                                    "The processing engine failed to process the request. This could be due to lack of matching or bugs in the pipeline engine.",
                                    null);
                    return;
                }
            } catch (ResourceNotFoundException rse) {
                if (log.isWarnEnabled()) {
                    log.warn("The resource was not found", rse);
                }

                manageException(request, res, env, uri,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Resource Not Found",
                                "Resource Not Found",
                                "The requested resource \"" + request.getRequestURI() + "\" could not be found",
                                rse);
                return;
            } catch (ConnectionResetException cre) {
                if (log.isDebugEnabled()) {
                    log.debug("The connection was reset", cre);
                } else if (log.isWarnEnabled()) {
                    log.warn("The connection was reset.");
                }

            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Internal Cocoon Problem", e);
                }

                manageException(request, res, env, uri,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Internal Server Error", null, null, e);
                return;
            }

            long end = System.currentTimeMillis();
            String timeString = processTime(end - start);
            if (log.isInfoEnabled()) {
                log.info("'" + uri + "' " + timeString);
            }

            if (contentType != null && contentType.equals("text/html")) {
                String showTime = request.getParameter(Constants.SHOWTIME_PARAM);
                boolean show = this.showTime;
                if (showTime != null) {
                    show = !showTime.equalsIgnoreCase("no");
                }
                if (show) {
                    boolean hide = this.hiddenShowTime;
                    if (showTime != null) {
                        hide = showTime.equalsIgnoreCase("hide");
                    }
                    ServletOutputStream out = res.getOutputStream();
                    out.print((hide) ? "<!-- " : "<p>");
                    out.print(timeString);
                    out.println((hide) ? " -->" : "</p>");
                }
            }
        } finally {
            if (ctxMap != null) ctxMap.clear();
            try {
                ServletOutputStream out = res.getOutputStream();
                out.flush();
                out.close();
            } catch(Exception e) {
                log.error("Cocoon got an Exception while trying to close stream.", e);
            }
        }
    }

    protected void manageException(HttpServletRequest req, HttpServletResponse res, Environment env,
                                   String uri, int errorStatus,
                                   String title, String message, String description,
                                   Exception e)
            throws IOException {
        if (this.manageExceptions) {
            if (env != null) {
                env.tryResetResponse();
            } else {
                res.reset();
            }

			String type = Notifying.FATAL_NOTIFICATION;
			HashMap extraDescriptions = null;
			
			if (errorStatus == HttpServletResponse.SC_NOT_FOUND) {
				type = "resource-not-found";
				// Do not show the exception stacktrace for such common errors.
				e = null;
			} else {
				extraDescriptions = new HashMap(2);
				extraDescriptions.put(Notifying.EXTRA_REQUESTURI, req.getRequestURI());
				if (uri != null) {
					 extraDescriptions.put("Request URI", uri);
				}

				// Do not show exception stack trace when log level is WARN or above. Show only message.
				if (!log.isInfoEnabled()) {
					Throwable t = DefaultNotifyingBuilder.getRootCause(e);
					if (t != null) extraDescriptions.put(Notifying.EXTRA_CAUSE, t.getMessage());
					e = null;
				}
			}

			Notifying n = new DefaultNotifyingBuilder().build(this,
															   e,
															   type,
															   title,
															   "Cocoon Servlet",
															   message,
															   description,
															   extraDescriptions);
			
			res.setContentType("text/html");
			res.setStatus(errorStatus);
			Notifier.notify(n, res.getOutputStream(), "text/html");
        } else {
            res.sendError(errorStatus, title);
            res.flushBuffer();
        }
    }

    /**
     * Create the environment for the request
     */
    protected Environment getEnvironment(String uri,
                                         HttpServletRequest req,
                                         HttpServletResponse res)
    throws Exception {
        HttpEnvironment env;

        String formEncoding = req.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.defaultFormEncoding;
        }
        env = new HttpEnvironment(uri,
                                  this.servletContextURL,
                                  req,
                                  res,
                                  this.servletContext,
                                  (HttpContext) this.appContext.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT),
                                  this.containerEncoding,
                                  formEncoding,
                                  this.requestFactory);
        env.enableLogging(new LogKitLogger(this.log));
        return env;
    }

    /**
     * Instatiates the parent component manager, as specified in the
     * parent-component-manager init parameter.
     *
     * If none is specified, the method returns <code>null</code>.
     *
     * @return the parent component manager, or <code>null</code>.
     */
    protected synchronized ComponentManager getParentComponentManager() {
        ComponentManager parentComponentManager = null;
        if (parentComponentManagerClass != null) {
            try {
                String initParam = null;
                int dividerPos = parentComponentManagerClass.indexOf('/');
                if (dividerPos != -1) {
                    initParam = parentComponentManagerClass.substring(dividerPos + 1);
                    parentComponentManagerClass = parentComponentManagerClass.substring(0, dividerPos);
                }

                Class pcm = ClassUtils.loadClass(parentComponentManagerClass);
                Constructor pcmc = pcm.getConstructor(new Class[]{String.class});
                parentComponentManager = (ComponentManager) pcmc.newInstance(new Object[]{initParam});

                if (parentComponentManager instanceof LogEnabled) {
                    ((LogEnabled) parentComponentManager).enableLogging(new LogKitLogger(log));
                }
                if (parentComponentManager instanceof Contextualizable) {
                    ((Contextualizable) parentComponentManager).contextualize(this.appContext);
                }
                if (parentComponentManager instanceof Initializable) {
                    ((Initializable) parentComponentManager).initialize();
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Could not initialize parent component manager.", e);
                }
            }
        }
        return parentComponentManager;
    }



    /**
     * Creates the Cocoon object and handles exception handling.
     */
    private synchronized void createCocoon()
    throws ServletException {

        /* HACK for reducing class loader problems.                                     */
        /* example: xalan extensions fail if someone adds xalan jars in tomcat3.2.1/lib */
        if (this.initClassLoader) {
            try {
                Thread.currentThread().setContextClassLoader(this.classLoader);
            } catch (Exception e){}
        }

        updateEnvironment();
        forceLoad();
        forceProperty();

        try {
            URL configFile = (URL) this.appContext.get(Constants.CONTEXT_CONFIG_URL);
            if (log.isInfoEnabled()) {
                log.info("Reloading from: " + configFile.toExternalForm());
            }
            Cocoon c = (Cocoon) ClassUtils.newInstance("org.apache.cocoon.Cocoon");
            final String rootlogger = getInitParameter("cocoon-logger");
            if (rootlogger != null) {
                c.enableLogging(new LogKitLogger(this.logKitManager.getLogger(rootlogger)));
            } else {
                c.enableLogging(new LogKitLogger(log));
            }
            c.contextualize(this.appContext);
            c.compose(getParentComponentManager());
            c.setLogKitManager(this.logKitManager);
            if (this.enableInstrumentation) {
                c.setInstrumentManager(getInstrumentManager());
            }
            c.initialize();
            this.creationTime = System.currentTimeMillis();

            disposeCocoon();
            this.cocoon = c;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Exception reloading", e);
            }
            this.exception = e;
            disposeCocoon();
        }
    }

    /**
     * Method to update the environment before Cocoon instances are created.
     *
     * This is also useful if you wish to customize any of the 'protected'
     * variables from this class before a Cocoon instance is built in a derivative
     * of this class (eg. Cocoon Context).
     */
    protected void updateEnvironment() throws ServletException {
        this.appContext.put(Constants.CONTEXT_CLASS_LOADER, classLoader);
        this.appContext.put(Constants.CONTEXT_CLASSPATH, getClassPath());
    }

    /**
     * Helper method to obtain an <code>InstrumentManager</code> instance
     *
     * @return an <code>InstrumentManager</code> instance
     */
    private InstrumentManager getInstrumentManager()
        throws Exception
    {
        String imConfig = getInitParameter("instrumentation-config");
        if (imConfig == null) {
            throw new ServletException("Please define the init-param 'instrumentation-config' in your web.xml");
        }

        final InputStream is = this.servletContext.getResourceAsStream(imConfig);
        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        final Configuration conf = builder.build(is);

        // Get the logger for the instrument manager
        Logger imLogger =
            this.logKitManager.getLogger(conf.getAttribute( "logger", "core.instrument" ));

        // Set up the Instrument Manager
        DefaultInstrumentManager instrumentManager = new DefaultInstrumentManager();
        instrumentManager.enableLogging(new LogKitLogger(imLogger));
        instrumentManager.configure(conf);
        instrumentManager.initialize();

        if (log.isDebugEnabled()) {
            log.debug("Instrument manager created " + instrumentManager);
        }

        this.instrumentManager = instrumentManager;
        return instrumentManager;
    }

    private String processTime(long time) {
        StringBuffer out = new StringBuffer(PROCESSED_BY);
        if (time <= SECOND) {
            out.append(time);
            out.append(" milliseconds.");
        } else if (time <= MINUTE) {
            out.append(time / SECOND);
            out.append(" seconds.");
        } else if (time <= HOUR) {
            out.append(time / MINUTE);
            out.append(" minutes.");
        } else {
            out.append(time / HOUR);
            out.append(" hours.");
        }
        return out.toString();
    }

    /**
     * Gets the current cocoon object.  Reload cocoon if configuration
     * changed or we are reloading.
     */
    private void getCocoon(final String pathInfo, final String reloadParam)
    throws ServletException {
        if (this.allowReload) {
            boolean reload = false;

            if (this.cocoon != null) {
                if (this.cocoon.modifiedSince(this.creationTime)) {
                    if (log.isInfoEnabled()) {
                        log.info("Configuration changed reload attempt");
                    }
                    reload = true;
                } else if (pathInfo == null && reloadParam != null) {
                    if (log.isInfoEnabled()) {
                        log.info("Forced reload attempt");
                    }
                    reload = true;
                }
            } else if (pathInfo == null && reloadParam != null) {
                if (log.isInfoEnabled()) {
                    log.info("Invalid configurations reload");
                }
                reload = true;
            }

            if (reload) {
                initLogger();
                createCocoon();
            }
        }
    }

    /**
     * Destroy Cocoon
     */
    private final void disposeCocoon()
    {
        if (this.cocoon != null) {
            this.cocoon.dispose();
            this.cocoon = null;
        }
    }
}
