/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.logger.LogKitLoggerManager;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.NetUtils;

import org.apache.log.Hierarchy;
import org.apache.log.Priority;

/**
 * The Cocoon Wrapper simplifies usage of the Cocoon object. Allows to create, 
 * configure Cocoon instance and process single requests.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: CocoonWrapper.java,v 1.11 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public class CocoonWrapper {

    protected static final String DEFAULT_USER_AGENT = Constants.COMPLETE_NAME;
    protected static final String DEFAULT_ACCEPT = "text/html, */*";

    // User Supplied Parameters
    private String contextDir = Constants.DEFAULT_CONTEXT_DIR;
    private String configFile = null;

    private String workDir = Constants.DEFAULT_WORK_DIR;
    private String logKit = null;
    protected String logger = null;
    private String userAgent = DEFAULT_USER_AGENT;
    private String accept = DEFAULT_ACCEPT;
    private List classList = new ArrayList();
 
    // Objects used alongside User Supplied Parameters
    private File context;
    private File work;
    private File conf;

    // Internal Objects
    private CommandLineContext cliContext;
    private Cocoon cocoon;
    protected static Logger log;
    private Map attributes = new HashMap();
    private HashMap empty = new HashMap();

    private boolean initialized = false;

    //
    // INITIALISATION METHOD
    //
    public void initialize() throws Exception {
        // @todo@ when does the logger get initialised? uv
        // @todo@ these should log then throw exceptions back to the caller, not use system.exit()
        setLogLevel("ERROR");

        this.context = getDir(this.contextDir, "context");
        this.work = getDir(workDir, "working");

        this.conf = getConfigurationFile(this.context, this.configFile);

        try {
            DefaultContext appContext = new DefaultContext();
            appContext.put(
                Constants.CONTEXT_CLASS_LOADER,
                CocoonWrapper.class.getClassLoader());
            cliContext = new CommandLineContext(contextDir);
            cliContext.enableLogging(log);
            appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, cliContext);
            LogKitLoggerManager logKitLoggerManager =
                    new LogKitLoggerManager(Hierarchy.getDefaultHierarchy());
            logKitLoggerManager.enableLogging(log);

            if (this.logKit != null) {
                final FileInputStream fis = new FileInputStream(logKit);
                final DefaultConfigurationBuilder builder =
                    new DefaultConfigurationBuilder();
                final Configuration logKitConf = builder.build(fis);
                final DefaultContext subcontext = new DefaultContext(appContext);
                subcontext.put("context-root", contextDir);
                logKitLoggerManager.contextualize(subcontext);
                logKitLoggerManager.configure(logKitConf);
                if (logger != null) {
                    log = logKitLoggerManager.getLoggerForCategory(logger);
                } else {
                    log = logKitLoggerManager.getLoggerForCategory("cocoon");
                }
            }

            appContext.put(Constants.CONTEXT_CLASSPATH, getClassPath(contextDir));
            appContext.put(Constants.CONTEXT_WORK_DIR, work);
            appContext.put(Constants.CONTEXT_UPLOAD_DIR, contextDir + "upload-dir");
            File cacheDir = getDir(workDir + File.separator + "cache-dir", "cache");
            appContext.put(Constants.CONTEXT_CACHE_DIR, cacheDir);
            appContext.put(Constants.CONTEXT_CONFIG_URL, conf.toURL());
            appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, "ISO-8859-1");
            
            loadClasses(classList);

            cocoon = new Cocoon();
            ContainerUtil.enableLogging(cocoon, log);
            ContainerUtil.contextualize(cocoon, appContext);
            cocoon.setLoggerManager(logKitLoggerManager);
            ContainerUtil.initialize(cocoon);

        } catch (Exception e) {
            log.fatalError("Exception caught", e);
            throw e;
        }
        initialized = true;
    }
    
    protected ExcaliburComponentManager getComponentManager() {
        return cocoon.getComponentManager();
    }

    /**
     * Look around for the configuration file.
     *
     * @param dir a <code>File</code> where to look for configuration files
     * @return a <code>File</code> representing the configuration
     * @exception IOException if an error occurs
     */
    private static File getConfigurationFile(File dir, String configFile)
        throws IOException {
        File conf;
        if (configFile == null) {
            conf = tryConfigurationFile(dir + File.separator + Constants.DEFAULT_CONF_FILE);
            if (conf == null) {
                conf = tryConfigurationFile(dir
                            + File.separator
                            + "WEB-INF"
                            + File.separator
                            + Constants.DEFAULT_CONF_FILE);
            }
            if (conf == null) {
                conf =  tryConfigurationFile(
                        System.getProperty("user.dir")
                            + File.separator
                            + Constants.DEFAULT_CONF_FILE);
            }
            if (conf == null) {
                conf = tryConfigurationFile(
                        "/usr/local/etc/" + Constants.DEFAULT_CONF_FILE);
            }
        } else {
            conf = new File(configFile);
            if (!conf.exists()) {
                conf = new File(dir, configFile);
            }
        }
        if (conf == null) {
            log.error("Could not find the configuration file.");
            throw new FileNotFoundException("The configuration file could not be found.");
        }
        return conf;
    }

    /**
     * Try loading the configuration file from a single location
     */
    private static File tryConfigurationFile(String filename) {
        if (log.isDebugEnabled()) {
            log.debug("Trying configuration file at: " + filename);
        }
        File conf = new File(filename);
        if (conf.canRead()) {
            return conf;
        } else {
            return null;
        }
    }

    /**
     * Get a <code>File</code> representing a directory.
     *
     * @param dir a <code>String</code> with a directory name
     * @param type a <code>String</code> describing the type of directory
     * @return a <code>File</code> value
     * @exception IOException if an error occurs
     */
    private File getDir(String dir, String type) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting handle to " + type + " directory '" + dir + "'");
        }
        File d = new File(dir);

        if (!d.exists()) {
            if (!d.mkdirs()) {
                log.error("Error creating " + type + " directory '" + d + "'");
                throw new IOException(
                    "Error creating " + type + " directory '" + d + "'");
            }
        }

        if (!d.isDirectory()) {
            log.error("'" + d + "' is not a directory.");
            throw new IOException("'" + d + "' is not a directory.");
        }

        if (!(d.canRead() && d.canWrite())) {
            log.error("Directory '" + d + "' is not readable/writable");
            throw new IOException(
                "Directory '" + d + "' is not readable/writable");
        }

        return d;
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    protected void loadClasses(List classList) {
        if (classList != null) {
            for (Iterator i = classList.iterator(); i.hasNext();) {
                String className = (String) i.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Trying to load class: " + className);
                    }
                    ClassUtils.loadClass(className).newInstance();
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not force-load class: " + className, e);
                    }
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
        }
    }

    //
    // GETTERS AND SETTERS FOR CONFIGURATION PROPERTIES
    //

    /**
     * Set LogKit configuration file name
     * @param logKit LogKit configuration file
     */
    public void setLogKit(String logKit) {
        this.logKit = logKit;
    }

    /**
     * Set log level. Default is DEBUG.
     * @param logLevel log level
     */
    public void setLogLevel(String logLevel) {
        final Priority priority = Priority.getPriorityForName(logLevel);
        Hierarchy.getDefaultHierarchy().setDefaultPriority(priority);
        CocoonWrapper.log = new LogKitLogger(Hierarchy.getDefaultHierarchy().getLoggerFor(""));
    }

    /**
     * Set logger category as default logger for the Cocoon engine
     * @param logger logger category
     */
    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLoggerName() {
        return logger;
    }
    
    /**
     * Set context directory
     * @param contextDir context directory
     */
    public void setContextDir(String contextDir) {
        this.contextDir = contextDir;
    }

    /**
     * Set working directory
     * @param workDir working directory
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setAgentOptions(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setAcceptOptions(String accept) {
        this.accept = accept;
    }

    public void addLoadedClass(String className) {
        this.classList.add(className);
    }

    public void addLoadedClasses(List classList) {
        this.classList.addAll(classList);
    }
    /**
     * Process single URI into given output stream.
     *
     * @param uri to process
     * @param outputStream to write generated contents into
     */
    public void processURI(String uri, OutputStream outputStream)
        throws Exception {

        if (!initialized) {
            initialize();
        }
        log.info("Processing URI: " + uri);

        // Get parameters, deparameterized URI and path from URI
        final TreeMap parameters = new TreeMap();
        final String deparameterizedURI =
            NetUtils.deparameterize(uri, parameters);
        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        int status =
            getPage(deparameterizedURI, 0L, parameters, null, null, outputStream);

        if (status >= 400) {
            throw new ProcessingException("Resource not found: " + status);
        }
    }

    public void dispose() {
        if (this.initialized) {
            this.initialized = false;
            ContainerUtil.dispose(this.cocoon);
            this.cocoon = null;
            if (log.isDebugEnabled()) {
                log.debug("Disposed");
            }
        }
    }

    /**
     * Allow subclasses to recursively precompile XSPs.
     */
    protected void precompile() {
        recursivelyPrecompile(context, context);
    }
    
    /**
     * Recurse the directory hierarchy and process the XSP's.
     * @param contextDir a <code>File</code> value for the context directory
     * @param file a <code>File</code> value for a single XSP file or a directory to scan recursively
     */
    private void recursivelyPrecompile(File contextDir, File file) {
        if (file.isDirectory()) {
            String entries[] = file.list();
            for (int i = 0; i < entries.length; i++) {
                recursivelyPrecompile(contextDir, new File(file, entries[i]));
            }
        } else if (file.getName().toLowerCase().endsWith(".xmap")) {
            try {
                this.processXMAP(IOUtils.getContextFilePath(contextDir.getCanonicalPath(),file.getCanonicalPath()));
            } catch (Exception e){
                //Ignore for now.
            }
        } else if (file.getName().toLowerCase().endsWith(".xsp")) {
            try {
                this.processXSP(IOUtils.getContextFilePath(contextDir.getCanonicalPath(),file.getCanonicalPath()));
            } catch (Exception e){
                //Ignore for now.
            }
        }
    }

    /**
     * Process a single XSP file
     *
     * @param uri a <code>String</code> pointing to an xsp URI
     * @exception Exception if an error occurs
     */
    protected void processXSP(String uri) throws Exception {
        String markupLanguage = "xsp";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, attributes,
                                                      null, cliContext, log);
        cocoon.precompile(uri, env, markupLanguage, programmingLanguage);
    }

    /**
     * Process a single XMAP file
     *
     * @param uri a <code>String</code> pointing to an xmap URI
     * @exception Exception if an error occurs
     */
    protected void processXMAP(String uri) throws Exception {
        String markupLanguage = "sitemap";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, attributes,
                                                      null, cliContext, log);
        cocoon.precompile(uri, env, markupLanguage, programmingLanguage);
    }


    /**
     * Samples an URI for its links.
     *
     * @param deparameterizedURI a <code>String</code> value of an URI to start sampling from
     * @param parameters a <code>Map</code> value containing request parameters
     * @return a <code>Collection</code> of links
     * @exception Exception if an error occurs
     */
    protected Collection getLinks(String deparameterizedURI, Map parameters)
        throws Exception {

        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        LinkSamplingEnvironment env =
            new LinkSamplingEnvironment(deparameterizedURI, context, attributes,
                                        parameters, cliContext, log);
        processLenient(env);
        return env.getLinks();
    }

    /**
     * Processes an URI for its content.
     *
     * @param deparameterizedURI a <code>String</code> value of an URI to start sampling from
     * @param parameters a <code>Map</code> value containing request parameters
     * @param links a <code>Map</code> value
     * @param stream an <code>OutputStream</code> to write the content to
     * @return a <code>String</code> value for the content
     * @exception Exception if an error occurs
     */
    protected int getPage(String deparameterizedURI,
                          long lastModified,
                          Map parameters,
                          Map links,
                          List gatheredLinks,
                          OutputStream stream)
    throws Exception {

        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        FileSavingEnvironment env =
            new FileSavingEnvironment(deparameterizedURI, lastModified, context,
                                      attributes, parameters, links,
                                      gatheredLinks, cliContext, stream, log);

        // Here Cocoon can throw an exception if there are errors in processing the page
        cocoon.process(env);

        // if we get here, the page was created :-)
        int status = env.getStatus();
        if (!env.isModified()) {
            status = -1;
        }
        return status;
    }

    /** Class <code>NullOutputStream</code> here. */
    static class NullOutputStream extends OutputStream {
        public void write(int b) throws IOException {
        }
        public void write(byte b[]) throws IOException {
        }
        public void write(byte b[], int off, int len) throws IOException {
        }
    }

    /**
     * Analyze the type of content for an URI.
     *
     * @param deparameterizedURI a <code>String</code> value to analyze
     * @param parameters a <code>Map</code> value for the request
     * @return a <code>String</code> value denoting the type of content
     * @exception Exception if an error occurs
     */
    protected String getType(String deparameterizedURI, Map parameters)
        throws Exception {
        
        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        FileSavingEnvironment env =
            new FileSavingEnvironment(deparameterizedURI, context, attributes,
                                      parameters, empty, null, cliContext,
                                      new NullOutputStream(), log);
        processLenient(env);
        return env.getContentType();
    }

    /**
     * Try to process something but don't throw a ProcessingException.
     *
     * @param env the <code>Environment</code> to process
     * @return boolean true if no error were cast, false otherwise
     * @exception Exception if an error occurs, except RNFE
     */
    private boolean processLenient(Environment env) throws Exception {
        try {
            this.cocoon.process(env);
        } catch (ProcessingException pe) {
            return false;
        }
        return true;
    }

    /**
     * This builds the important ClassPath used by this class.  It
     * does so in a neutral way.
     * It iterates in alphabetical order through every file in the
     * lib directory and adds it to the classpath.
     *
     * Also, we add the files to the ClassLoader for the Cocoon system.
     * In order to protect ourselves from skitzofrantic classloaders,
     * we need to work with a known one.
     *
     * @param context  The context path
     * @return a <code>String</code> value
     */
    protected static String getClassPath(final String context) {
        StringBuffer buildClassPath = new StringBuffer();

        String classDir = context + "/WEB-INF/classes";
        buildClassPath.append(classDir);

        File root = new File(context + "/WEB-INF/lib");
        if (root.isDirectory()) {
            File[] libraries = root.listFiles();
            Arrays.sort(libraries);
            for (int i = 0; i < libraries.length; i++) {
                if (libraries[i].getAbsolutePath().endsWith(".jar")) {
                    buildClassPath.append(File.pathSeparatorChar).append(
                        IOUtils.getFullFilename(libraries[i]));
                }
            }
        }

        buildClassPath.append(File.pathSeparatorChar).append(
            System.getProperty("java.class.path"));

        // Extra class path is necessary for non-classloader-aware java compilers to compile XSPs
        //        buildClassPath.append(File.pathSeparatorChar)
        //                      .append(getExtraClassPath(context));

        if (log.isDebugEnabled()) {
            log.debug("Context classpath: " + buildClassPath);
        }
        return buildClassPath.toString();
    }
}
