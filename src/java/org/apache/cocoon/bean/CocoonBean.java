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
package org.apache.cocoon.bean;

import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.bean.helpers.DelayedOutputStream;
import org.apache.cocoon.components.notification.SimpleNotifyingBean;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.components.notification.DefaultNotifyingBuilder;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;

import org.apache.avalon.excalibur.logger.DefaultLogKitManager;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.log.Priority;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;

/**
 * The Cocoon Bean simplifies usage of the Cocoon object. Allows to create, configure Cocoon
 * instance and process requests, one by one or multiple with link traversal.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: CocoonBean.java,v 1.8 2003/06/04 14:19:09 upayavira Exp $
 */
public class CocoonBean {

    protected static final String DEFAULT_USER_AGENT = Constants.COMPLETE_NAME;
    protected static final String DEFAULT_ACCEPT = "text/html, */*";

    // User Supplied Parameters
    private String contextDir = Constants.DEFAULT_CONTEXT_DIR;
    private String configFile = null;

    private String brokenLinkReportFile = null;
    private String brokenLinkReportType = "text";
    private boolean brokenLinkGenerate = false;
    private String brokenLinkExtension = "";

    private String workDir = Constants.DEFAULT_WORK_DIR;
    private String logKit = null;
    private String logger = null;
    private String userAgent = DEFAULT_USER_AGENT;
    private String accept = DEFAULT_ACCEPT;
    private String defaultFilename = Constants.INDEX_URI;
    private boolean followLinks = true;
    private boolean precompileOnly = false;
    private boolean confirmExtension = true;
    private List classList = new ArrayList();
    private List brokenLinks = null;
    private List targets = new ArrayList();

    // Objects used alongside User Supplied Parameters
    private File context;
    private File work;
    private File conf;

    // Internal Objects
    private CommandLineContext cliContext;
    private Cocoon cocoon;
    private String destDir;
    private static Logger log;
    private Map attributes;
    private HashMap empty;
    private Map allProcessedLinks;
    private Map allTranslatedLinks;
    private boolean initialized;
    private boolean verbose;
    SourceResolver sourceResolver;
    //
    // INITIALISATION METHOD
    //

    public CocoonBean() {
    }

    public void initialize() throws Exception {
        // @todo@ when does the logger get initialised? uv
        // @todo@ these should log then throw exceptions back to the caller, not use system.exit()
        if (destDir.equals("")) {
            String error = "Careful, you must specify a destination dir when using the -d/--destDir argument";
            log.fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (contextDir.equals("")) {
            String error = "Careful, you must specify a configuration file when using the -c/--contextDir argument";
            log.fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (workDir.equals("")) {
            String error = "Careful, you must specify a destination dir when using the -w/--workDir argument";
            log.fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (targets.size() == 0 && !precompileOnly) {
            String error = "Please, specify at least one starting URI.";
            log.fatalError(error);
            System.out.println(error);
            System.exit(1);
        }
        setLogLevel("ERROR");
        this.context = getDir(this.contextDir, "context");
        this.work = getDir(workDir, "working");

        this.conf = getConfigurationFile(this.context, this.configFile);

        try {
            DefaultContext appContext = new DefaultContext();
            appContext.put(Constants.CONTEXT_CLASS_LOADER, CocoonBean.class.getClassLoader());
            cliContext = new CommandLineContext(contextDir);
            cliContext.enableLogging(new LogKitLogger(log));
            appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, cliContext);
            DefaultLogKitManager logKitManager = null;
            if (logKit != null) {
                final FileInputStream fis = new FileInputStream(logKit);
                final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                final Configuration logKitConf = builder.build(fis);
                logKitManager = new DefaultLogKitManager(Hierarchy.getDefaultHierarchy());
                logKitManager.setLogger(log);
                final DefaultContext subcontext = new DefaultContext(appContext);
                subcontext.put("context-root", contextDir);
                logKitManager.contextualize(subcontext);
                logKitManager.configure(logKitConf);
                if (logger != null) {
                    log = logKitManager.getLogger(logger);
                } else {
                    log = logKitManager.getLogger("cocoon");
                }
            } else {
                logKitManager = new DefaultLogKitManager(Hierarchy.getDefaultHierarchy());
                logKitManager.setLogger(log);
            }
            appContext.put(Constants.CONTEXT_CLASSPATH, getClassPath(contextDir));
            appContext.put(Constants.CONTEXT_WORK_DIR, work);
            appContext.put(Constants.CONTEXT_UPLOAD_DIR, contextDir + "upload-dir");
            File cacheDir = getDir(workDir + File.separator + "cache-dir", "cache");
            appContext.put(Constants.CONTEXT_CACHE_DIR, cacheDir);
            appContext.put(Constants.CONTEXT_CONFIG_URL, conf.toURL());

            loadClasses(classList);

            cocoon = new Cocoon();
            cocoon.enableLogging(new LogKitLogger(log));
            cocoon.contextualize(appContext);
            cocoon.setLogKitManager(logKitManager);
            cocoon.initialize();

            if (brokenLinkReportFile!=null && !(new File(brokenLinkReportFile).exists())) {
              log.error("Broken Link Report File does not exist: " + brokenLinkReportFile);
            }

            if (brokenLinkReportFile!=null) {
                brokenLinks= new ArrayList();
            }
            this.sourceResolver = (SourceResolver)cocoon.getComponentManager().lookup(SourceResolver.ROLE);

        } catch (Exception e) {
            log.fatalError("Exception caught", e);
            throw e;
        }
        initialized = true;
    }

    /**
     * Look around for the configuration file.
     *
     * @param dir a <code>File</code> where to look for configuration files
     * @return a <code>File</code> representing the configuration
     * @exception IOException if an error occurs
     */
    private static File getConfigurationFile(File dir, String configFile) throws IOException {
        File conf;
        if (configFile == null) {
            conf = tryConfigurationFile(dir + File.separator + Constants.DEFAULT_CONF_FILE);
            if (conf == null) {
                conf = tryConfigurationFile(dir + File.separator + "WEB-INF" + File.separator + Constants.DEFAULT_CONF_FILE);
            }
            if (conf == null) {
                conf = tryConfigurationFile(System.getProperty("user.dir") + File.separator + Constants.DEFAULT_CONF_FILE);
            }
            if (conf == null) {
                conf = tryConfigurationFile("/usr/local/etc/" + Constants.DEFAULT_CONF_FILE);
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
    private static File tryConfigurationFile(String filename) throws IOException {
        File conf;
        if (log.isDebugEnabled()) {
            log.debug("Trying configuration file at: " + filename);
        }
        conf = new File(filename);
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
                throw new IOException("Error creating " + type + " directory '" + d + "'");
            }
        }

        if (!d.isDirectory()) {
            log.error("'" + d + "' is not a directory.");
            throw new IOException("'" + d + "' is not a directory.");
        }

        if (!(d.canRead() && d.canWrite())) {
            log.error("Directory '" + d + "' is not readable/writable");
            throw new IOException("Directory '" + d + "' is not readable/writable");
        }

        return d;
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    protected void loadClasses(List classList) {
        if (classList != null) {
            for (Iterator i = classList.iterator();i.hasNext();) {
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
        CocoonBean.log = Hierarchy.getDefaultHierarchy().getLoggerFor("");
    }

    /**
     * Set logger category as default logger for the Cocoon engine
     * @param logger logger category
     */
    public void setLogger(String logger) {
        this.logger = logger;
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

    public void setDestDir(String destDir) {
        this.destDir = destDir;
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

    public void setDefaultFilename(String filename) {
        defaultFilename = filename;
    }

    public void setFollowLinks(boolean follow) {
        followLinks = follow;
    }

    public void setConfirmExtensions(boolean confirmExtension) {
        this.confirmExtension = confirmExtension;
    }

    public void setBrokenLinkReportFile(String filename){
        this.brokenLinkReportFile = filename;
    }

    public void setBrokenLinkReportType(String brokenLinkReportType) {
        this.brokenLinkReportType = brokenLinkReportType;
    }

    public void setBrokenLinkGenerate(boolean brokenLinkGenerate) {
        this.brokenLinkGenerate = brokenLinkGenerate;
    }

    public void setBrokenLinkExtension(String brokenLinkExtension) {
        this.brokenLinkExtension = brokenLinkExtension;
    }

    public void setPrecompileOnly(boolean precompileOnly){
         this.precompileOnly = precompileOnly;
    }

    public void setVerbose(boolean verbose){
         this.verbose = verbose;
    }

    public void addLoadedClass(String className) {
        this.classList.add(className);
    }

    public void addLoadedClasses(List classList) {
        this.classList.addAll(classList);
    }

    public void addTarget(String sourceURI) {
        targets.add(new Target(sourceURI, destDir));
    }
    public void addTarget(String type, String root, String sourceURI, String destURI){
        targets.add(new Target(type, root, sourceURI, destURI));
    }

    public void addTarget(String type, String sourceURI, String destURI){
        targets.add(new Target(type, sourceURI, destURI));
    }

    public void addTarget(String sourceURI, String destURI){
        targets.add(new Target(sourceURI, destURI));
    }
    public void addTargets(List uris) {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            Target target = new Target((String)i.next(), destDir);
            targets.add(target);
        }
    }
    /**
     * Process single URI into given output stream.
     *
     * @param uri to process
     * @param outputStream to write generated contents into
     */
    public void processURI(String uri, OutputStream outputStream) throws Exception {

        if (!initialized) {
            initialize();
        }
        log.info("Processing URI: " + uri);

        // Get parameters, deparameterized URI and path from URI
        final TreeMap parameters = new TreeMap();
        final String deparameterizedURI = NetUtils.deparameterize(uri, parameters);
        final String suri = NetUtils.parameterize(deparameterizedURI, parameters);
        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        // Get file name from URI (without path)
        String pageURI = deparameterizedURI;
        if (pageURI.indexOf("/") != -1) {
            pageURI = pageURI.substring(pageURI.lastIndexOf("/") + 1);
            if (pageURI.length() == 0) {
                pageURI = "./";
            }
        }

        String filename;
        if (confirmExtension) {
            filename = (String)allTranslatedLinks.get(suri);
            if (filename == null) {
                filename = mangle(suri);
                final String type = getType(deparameterizedURI, parameters);
                final String ext = NetUtils.getExtension(filename);
                final String defaultExt = MIMEUtils.getDefaultExtension(type);
                if ((ext == null) || (!ext.equals(defaultExt))) {
                    filename += defaultExt;
                }
            }
        } else {
            filename = suri;
        }
        if ("".equals(filename)) {
            outputStream.close();
            return;
        }

        int status = getPage(deparameterizedURI, parameters, null, null, outputStream);

        if (status >=400) {
           throw new ProcessingException("Resource not found: "+status);
        }
    }

    /**
     * Warms up the engine by accessing the root.
     * @exception Exception if an error occurs
     */
    public void warmup() throws Exception {
        //log.info(" [Cocoon might need to compile the sitemaps, this might take a while]");
        cocoon.generateSitemap(new LinkSamplingEnvironment("/", context, attributes, null, cliContext,
                                                           new LogKitLogger(log)));
    }

    public void dispose () {
        if (initialized) {
            cocoon.dispose();
            initialized = false;
            cocoon = null;
            log.debug("Disposed");
        }
    }

    /**
     * Process the URI list and process them all independently.
     * @exception Exception if an error occurs
     */
    public void process() throws Exception {

        if (!initialized){
            initialize();
        }
        attributes = new HashMap();
        empty = new HashMap();
        allProcessedLinks = new HashMap();
        allTranslatedLinks = new HashMap();

        int nCount = 0;
        Target target;

        HashMap targetMap = new java.util.HashMap();
        Iterator i = targets.iterator();
        while (i.hasNext()) {
            target = (Target)i.next();
            if (!targetMap.containsKey(target.getHashCode())) {
                targetMap.put(target.getHashCode(), target);
            }
        }

        while (targetMap.size() > 0) {
            String hashCode = (String)(new ArrayList(targetMap.keySet()).get(0));
            target = (Target)targetMap.get(hashCode);
            try {
                if (allProcessedLinks.get(hashCode) == null) {
                    if (precompileOnly) {
                        this.processXSP(target.getSourceURI());
                    } else if (this.followLinks) {
                        i = processTarget(target).iterator();
                        while (i.hasNext()) {
                            target = (Target)i.next();
                            if (!targetMap.containsKey(target.getHashCode())) {
                                targetMap.put(target.getHashCode(), target);
                            }
                        }
                    } else {
                        processTarget(target);
                    }
                }
            } catch (ResourceNotFoundException rnfe) {
                printBroken (target.getSourceURI(), rnfe.getMessage());
            }

            targetMap.remove(target.getHashCode());
            nCount++;

            if (log.isInfoEnabled()) {
                log.info("  Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
                log.info("  Processed, Translated & Left: " + allProcessedLinks.size() + ", "  + allTranslatedLinks.size() + ", " + targetMap.size());
            }
        }

        if (nCount == 0) {
            recursivelyPrecompile(context, context);
        }
        outputBrokenLinks();

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
    private void processXSP(String uri) throws Exception {
        String markupLanguage = "xsp";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, attributes, null, cliContext,
                                                      new LogKitLogger(log));
        cocoon.precompile(uri, env, markupLanguage, programmingLanguage);
    }

    /**
     * Process a single XMAP file
     *
     * @param uri a <code>String</code> pointing to an xmap URI
     * @exception Exception if an error occurs
     */
    private void processXMAP(String uri) throws Exception {
        String markupLanguage = "sitemap";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, attributes, null, cliContext,
                                                      new LogKitLogger(log));
        cocoon.precompile(uri, env, markupLanguage, programmingLanguage);
    }

    /**
     * Processes the given Target and return all links.
     *
     * If links are to be followed, and extensions checked then the algorithm is as
     * follows:
     * <ul>
     *  <li>file name for the URI is generated. URI MIME type is checked for
     *      consistency with the URI and, if the extension is inconsistent
     *      or absent, the file name is changed</li>
     *  <li>the link view of the given URI is called and the file names for linked
     *      resources are generated and stored.</li>
     *  <li>for each link, absolute file name is translated to relative path.</li>
     *  <li>after the complete list of links is translated, the link-translating
     *      view of the resource is called to obtain a link-translated version
     *      of the resource with the given link map</li>
     *  <li>list of absolute URI is returned, for every URI which is not yet
     *      present in list of all translated URIs</li>
     * </ul>
     *
     * If links are to be followed, but extensions are not checked, then the
     * algorithm will be:
     * <ul>
     *   <li>The content for the page is generated</li>
     *   <li>Whilst generating, all links are gathered by the LinkGatherer</li>
     *   <li>Gathered links are added to the unprocessed links list, and
     *       processing continues until all processing is complete
     *   </li>
     * </ul>
     * @param target a <code>Target</code> target to process
     * @return a <code>Collection</code> containing all links found, as
     * Target objects.
     * @exception Exception if an error occurs
     */
    private Collection processTarget(Target target) throws Exception {
        System.out.print(" * ");

        String uri = target.getSourceURI();

        // Get parameters, deparameterized URI and path from URI
        final TreeMap parameters = new TreeMap();
        final String deparameterizedURI = NetUtils.deparameterize(uri, parameters);
        final String path = NetUtils.getPath(uri);
        final String suri = NetUtils.parameterize(deparameterizedURI, parameters);
        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);

        // Get file name from URI (without path)
        String pageURI = deparameterizedURI;
        if (pageURI.indexOf("/") != -1) {
            pageURI = pageURI.substring(pageURI.lastIndexOf("/") + 1);
            if (pageURI.length() == 0) {
                pageURI = "./";
            }
        }

        String filename;
        if (confirmExtension) {
            filename = (String)allTranslatedLinks.get(suri);
            if (filename == null) {
                filename = mangle(suri);
                final String type = getType(deparameterizedURI, parameters);
                final String ext = NetUtils.getExtension(filename);
                final String defaultExt = MIMEUtils.getDefaultExtension(type);
                if (defaultExt != null) {
                    if ((ext == null) || (!ext.equals(defaultExt))) {
                        filename += defaultExt;
                    }
                }
                allTranslatedLinks.put(suri, filename);
            }
        } else {
            filename = suri;
        }
        // Store processed URI list to avoid eternal loop
        allProcessedLinks.put(target.getHashCode(),target);

        if ("".equals(filename)) {
            return new ArrayList();
        }

        // Process links
        final List absoluteLinks = new ArrayList();
        final HashMap translatedLinks = new HashMap();
        List gatheredLinks = new ArrayList();

        if (followLinks && confirmExtension) {
            final Iterator i = this.getLinks(deparameterizedURI, parameters).iterator();

            while (i.hasNext()) {
                String link = (String) i.next();
                // Fix relative links starting with "?"
                String relativeLink = link;
                if (relativeLink.startsWith("?")) {
                    relativeLink = pageURI + relativeLink;
                }

                String absoluteLink = NetUtils.normalize(NetUtils.absolutize(path, relativeLink));
                {
                    final TreeMap p = new TreeMap();
                    absoluteLink = NetUtils.parameterize(NetUtils.deparameterize(absoluteLink, p), p);
                }
                String translatedAbsoluteLink = (String)allTranslatedLinks.get(absoluteLink);
                if (translatedAbsoluteLink == null) {
                    try {
                        translatedAbsoluteLink = this.translateURI(absoluteLink);
                        log.info("  Link translated: " + absoluteLink);
                        allTranslatedLinks.put(absoluteLink, translatedAbsoluteLink);
                        absoluteLinks.add(absoluteLink);
                    } catch (ProcessingException pe) {
                        printBroken(absoluteLink, pe.getMessage());
                    }
                }

                // I have to add also broken links to the absolute links
                // to be able to generate the "broken link" page
                absoluteLinks.add(absoluteLink);
                final String translatedRelativeLink = NetUtils.relativize(path, translatedAbsoluteLink);
                translatedLinks.put(link, translatedRelativeLink);
            }

            printInfo("["+translatedLinks.size()+"] ");
        }

        try {
            // Process URI
            DelayedOutputStream output = new DelayedOutputStream();
            try{
                int status = getPage(deparameterizedURI, parameters, confirmExtension ? translatedLinks : null, gatheredLinks, output);

                if (status >=400) {
                    throw new ProcessingException("Resource not found: "+status);
                }

                if (followLinks && !confirmExtension) {
                    for (Iterator it=gatheredLinks.iterator();it.hasNext();){
                        String link = (String) it.next();
                        if (link.startsWith("?")) {
                            link = pageURI + link;
                        }
                        String absoluteLink = NetUtils.normalize(NetUtils.absolutize(path, link));
                        {
                            final TreeMap p = new TreeMap();
                            absoluteLink = NetUtils.parameterize(NetUtils.deparameterize(absoluteLink, p), p);
                        }
                        absoluteLinks.add(absoluteLink);
                    }
                    printInfo("["+gatheredLinks.size()+"] ");
                }

                printlnInfo(uri); // (can also output type returned by getPage)
            } catch(ProcessingException pe) {
                output.close();
                output = null;
                if (brokenLinkGenerate) {
                    String brokenFile = NetUtils.decodePath(filename);
                    if (brokenLinkExtension!=null){
                        brokenFile = brokenFile+brokenLinkExtension;
                    }
                    resourceUnavailable(target, uri);
                }
                printBroken(filename, DefaultNotifyingBuilder.getRootCause(pe).getMessage());
            } finally {
                if (output != null) {

                    OutputStream stream = target.getOutputStream(filename);

                    output.setFileOutputStream(stream);
                    output.flush();
                }
                try {
                    if (output != null)
                        output.close();
                } catch(IOException ioex) {
                    log.warn(ioex.toString());
                }
            }
        } catch (Exception rnfe) {
            System.out.println("XXX" + rnfe);
            log.warn("Could not process URI: " + deparameterizedURI);
            if (verbose) System.out.println("Could not process URI: " + deparameterizedURI);
        }

         List targets = new ArrayList();
         for (Iterator i = absoluteLinks.iterator() ;i.hasNext();) {
             String link = (String)i.next();
             Target derivedTarget = target.getDerivedTarget(link);
             if (derivedTarget != null) {
                 targets.add(target.getDerivedTarget(link));
             }
         }
         return targets;
    }

    /**
     * Translate an URI into a file name.
     *
     * @param uri a <code>String</code> value to map
     * @return a <code>String</code> vlaue for the file
     * @exception Exception if an error occurs
     */
    private String translateURI(String uri) throws Exception {
        if (null == uri || "".equals(uri)) {
            log.warn("translate empty uri");
            if (verbose) System.out.println("translate empty uri");
            return "";
        }
        HashMap parameters = new HashMap();
        parameters.put("user-agent", userAgent);
        parameters.put("accept", accept);
        String deparameterizedURI = NetUtils.deparameterize(uri, parameters);

        String filename = mangle(uri);
        if (confirmExtension) {
            String type = getType(deparameterizedURI, parameters);
            String ext = NetUtils.getExtension(filename);
            String defaultExt = MIMEUtils.getDefaultExtension(type);
            if (defaultExt != null) {
                if ((ext == null) || (!ext.equals(defaultExt))) {
                    filename += defaultExt;
                }
            }
        }

        return filename;
    }

    /**
      * Print the message of a broken link.
      *
      * @param url the <code>URL</code> of the broken link
      * @param cause of the broken link
      */
     private void printBroken(String url, String cause) {
         int screenWidth = 67;
         int causeWidth = screenWidth - 6;

         printlnInfo("");
         printlnInfo("-> [broken page] " + url +" <- ");
         printlnInfo("");
         printInfo("     ");

         int causeLength = cause.length(), currentStart = -causeWidth, currentEnd = 0;
         do {
             currentStart += causeWidth;
             currentEnd   += causeWidth;

             if (currentEnd>causeLength) {
                 currentEnd=causeLength;
             }

             printlnInfo(cause.substring(currentStart, currentEnd));
             printInfo("     ");
         } while(currentEnd < causeLength);

         printlnInfo("");

         if (null != this.brokenLinks) {
             brokenLinks.add(url);
         }
     }

     /**
      * Print an info message.
      *
      * @param message the message to print
      */
     private void printlnInfo (String message) {
         log.info(message);
         if (verbose) System.out.println(message);
     }

     /**
      * Print an info message.
      *
      * @param message the message to print
      */
     private void printInfo (String message) {
         log.info(message);
         if (verbose) System.out.print(message);
     }

    /**
     * Generate a <code>resourceUnavailable</code> message.
     *
     * @param target being unavailable
     * @exception IOException if an error occurs
     */
    private void resourceUnavailable(Target target, String uri) throws IOException, ProcessingException {
        SimpleNotifyingBean n = new SimpleNotifyingBean(this);
        n.setType("resource-not-found");
        n.setTitle("Resource not Found");
        n.setSource("Cocoon commandline (Main.java)");
        n.setMessage("Page Not Available.");
        n.setDescription("The requested resource couldn't be found.");
        n.addExtraDescription(Notifying.EXTRA_REQUESTURI, uri);
        n.addExtraDescription("missing-file", uri);

        PrintStream out = new PrintStream(target.getOutputStream(uri));
        Notifier.notify(n, out, "text/html");
        out.flush();
        out.close();
    }

    private void outputBrokenLinks() {
        if (brokenLinkReportFile==null){
            return;
        } else if ("text".equalsIgnoreCase(brokenLinkReportType)) {
            outputBrokenLinksAsText();
        } else if ("xml".equalsIgnoreCase(brokenLinkReportType)) {
            outputBrokenLinksAsXML();
        }
    }
    private void outputBrokenLinksAsText(){
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(new File(brokenLinkReportFile)), true);
            for (Iterator i=brokenLinks.iterator();i.hasNext();){
                writer.println((String)i.next());
            }
            writer.close();
        } catch (IOException ioe) {
            log.error("File does not exist: " + brokenLinkReportFile);
        }
    }
    private void outputBrokenLinksAsXML(){
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(new File(brokenLinkReportFile)), true);
            writer.println("<broken-links>");
            for (Iterator i=brokenLinks.iterator();i.hasNext();){
                writer.println("  <link>" + (String)i.next()+"</link>");
            }
            writer.println("</broken-links>");
            writer.close();
        } catch (IOException ioe) {
            log.error("File does not exist: " + brokenLinkReportFile);
        }
    }
    /**
     * Mangle a URI.
     *
     * @param uri a URI to mangle
     * @return a mangled URI
     */
    private String mangle(String uri) {
        if (log.isDebugEnabled()) {
            log.debug("mangle(\"" + uri + "\")");
        }
        if (uri.charAt(uri.length() - 1) == '/') {
            uri += defaultFilename;
        }
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        uri = uri.replace(':', '_');
        if (log.isDebugEnabled()) {
            log.debug(uri);
        }
        return uri;
    }

    /**
     * Samples an URI for its links.
     *
     * @param deparameterizedURI a <code>String</code> value of an URI to start sampling from
     * @param parameters a <code>Map</code> value containing request parameters
     * @return a <code>Collection</code> of links
     * @exception Exception if an error occurs
     */
    protected Collection getLinks(String deparameterizedURI, Map parameters) throws Exception {
        LinkSamplingEnvironment env = new LinkSamplingEnvironment(deparameterizedURI,
                                                                  context,
                                                                  attributes,
                                                                  parameters,
                                                                  cliContext,
                                                                  new LogKitLogger(log));
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
    protected int getPage(String deparameterizedURI, Map parameters, Map links, List gatheredLinks, OutputStream stream) throws Exception {
        FileSavingEnvironment env = new FileSavingEnvironment(deparameterizedURI,
                                                              context,
                                                              attributes,
                                                              parameters,
                                                              links,
                                                              gatheredLinks,
                                                              cliContext,
                                                              stream,
                                                              new LogKitLogger(log));

        // Here Cocoon can throw an exception if there are errors in processing the page
        cocoon.process(env);

        // if we get here, the page was created :-)
        return env.getStatus();
    }

    /** Class <code>NullOutputStream</code> here. */
    static class NullOutputStream extends OutputStream
    {
        public void write(int b) throws IOException { }
        public void write(byte b[]) throws IOException { }
        public void write(byte b[], int off, int len) throws IOException { }
    }

    /**
     * Analyze the type of content for an URI.
     *
     * @param deparameterizedURI a <code>String</code> value to analyze
     * @param parameters a <code>Map</code> value for the request
     * @return a <code>String</code> value denoting the type of content
     * @exception Exception if an error occurs
     */
    protected String getType(String deparameterizedURI, Map parameters) throws Exception {
        FileSavingEnvironment env = new FileSavingEnvironment(deparameterizedURI,
                                                              context,
                                                              attributes,
                                                              parameters,
                                                              empty,
                                                              null,
                                                              cliContext,
                                                              new NullOutputStream(),
                                                              new LogKitLogger(log));
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
       try{
            cocoon.process(env);
       } catch(ProcessingException pe) {
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
        File root = new File(context + "/WEB-INF/lib");

        buildClassPath.append(classDir);

        if (root.isDirectory()) {
            File[] libraries = root.listFiles();
            Arrays.sort(libraries);
            for (int i = 0; i < libraries.length; i++) {
                buildClassPath.append(File.pathSeparatorChar)
                              .append(IOUtils.getFullFilename(libraries[i]));
            }
        }

        buildClassPath.append(File.pathSeparatorChar)
                      .append(System.getProperty("java.class.path"));

//        buildClassPath.append(File.pathSeparatorChar)
//                      .append(getExtraClassPath(context));

        if (log.isDebugEnabled()) {
            log.debug("Context classpath: " + buildClassPath.toString());
        }
        return buildClassPath.toString();
     }

    public class Target {
        private static final String DEFAULT_TYPE = "default";
        private static final String REPLACE_TYPE = "replace";
        private static final String APPEND_TYPE = "append";
        private static final String INSERT_TYPE = "insert";
        private String type;
        private String root;
        private String sourceURI;
        private String destURI;

        public Target(String type, String root, String sourceURI, String destURI){
            this.type = type;
            this.root = root;
            this.sourceURI = NetUtils.normalize(sourceURI);
            this.destURI = destURI;
        }

        public Target(String type, String sourceURI, String destURI){
            this(type, "", sourceURI, destURI);
        }

        public Target(String sourceURI, String destURI){
            this(DEFAULT_TYPE, "", sourceURI, destURI);
        }

        public Target getDerivedTarget(String newURI) throws ProcessingException {
            if (!newURI.startsWith(root)) {
                return null;
            }
            newURI = newURI.substring(root.length());
            return new Target(this.type, this.root, newURI, this.destURI);
        }

        public String getFinalURI(String actualSourceURI) throws ProcessingException {
            if (!actualSourceURI.startsWith(root)) {
                throw new ProcessingException("Derived target does not share same root: " + actualSourceURI);
            }
            actualSourceURI = actualSourceURI.substring(root.length());

            if (DEFAULT_TYPE.equals(this.type)){
                return this.getFinalURIWithDefault(actualSourceURI);
            }
            else if (REPLACE_TYPE.equals(this.type)){
                return this.getFinalURIWithReplace(actualSourceURI);
            }
            else if (APPEND_TYPE.equals(this.type)){
                return this.getFinalURIWithAppend(actualSourceURI);
            }
            else if (INSERT_TYPE.equals(this.type)){
                return this.getFinalURIWithInsert(actualSourceURI);
            } else {
                throw new ProcessingException("Unknown mapper type: " + this.type);
            }
        }
        private String getFinalURIWithDefault(String actualSourceURI){
            return destDir + "/" + actualSourceURI;
        }

        private String getFinalURIWithAppend(String actualSourceURI){
            return destURI + actualSourceURI;
        }

        private String getFinalURIWithReplace(String actualSourceURI){
            return destURI;
        }

        private String getFinalURIWithInsert(String actualSourceURI) throws ProcessingException {
            int starPos = destURI.indexOf("*");
            if (starPos == -1) {
                throw new ProcessingException("Missing * in replace mapper uri");
            } else if (starPos == destURI.length()-1) {
               return destURI.substring(0,starPos) + actualSourceURI;
            } else {
                return destURI.substring(0,starPos) + actualSourceURI + destURI.substring(starPos+1);
            }
        }

        public String getSourceURI() {
            return root + sourceURI;
        }
        // @todo@ this is misusing the 'hashCode' name - hashCodes should be integer it seems, uv
        public String getHashCode() {
            return type + "|" + root +"|" + sourceURI + "|" + destURI;
        }
        public OutputStream getOutputStream(String filename) throws IOException, ProcessingException {
            final String finalDestinationURI = this.getFinalURI(filename);
            Source src = (Source) sourceResolver.resolveURI(finalDestinationURI);
            if (!(src instanceof ModifiableSource)) {
                throw new ProcessingException("Source is not Modifiable: "+finalDestinationURI);
            }
            ModifiableSource outputSource = (ModifiableSource) src;
            return outputSource.getOutputStream();
        }
    }
}
