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
package org.apache.cocoon.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.util.IOUtils;

import org.apache.log.Hierarchy;
import org.apache.log.Priority;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Ant task for running Cocoon.
 *
 * @author    huber@apache.org
 * @version CVS $Id: CocoonTask.java,v 1.6 2004/02/28 04:17:50 antonio Exp $
 */
public class CocoonTask extends Task {

    /**
     *   User-agent header used in the Cocoon processing stage
     */
    protected final static String DEFAULT_USER_AGENT = Constants.COMPLETE_NAME;
    /**
     *   Accept header used in the Cocoon processing stage
     */
    protected final static String DEFAULT_ACCEPT = "text/html, */*";

    /**
     * logkit xconf file, ie logkit.xconf
     */
    private File logkitXconf;
    /**
     * cocoon destination directory
     */
    private File destDir;
    /**
     * cocoon work directory
     */
    private File workDir;
    /**
     * Cocoon context directory
     */
    private File contextDir;
    /**
     * Cocoon config file, ie cocoon.xconf
     */
    private File configFile;
    /**
     *LoggerCategory
     */
    private String logger;
    /**
     *loglevel, ie DEBUG, INFO, WARN, or ERROR
     */
    private String logLevel;

    private String acceptHeader;
    private String agentHeader;
    //private Boolean preCompileOnly;
    private Boolean followLinks;

    /**
     * A list of targets processed by Cocoon
     */
    private List targets;

    private Path cocoonClasspath;


    /**
     * Creates a new instance of CocoonTask
     */
    public CocoonTask() {
        this.logkitXconf = null;

        this.destDir = null;
        File workDirParent = new File(System.getProperty("java.io.tmpdir", "."));
        this.workDir = new File(workDirParent, "work");

        this.contextDir = null;
        this.logLevel = "INFO";
        this.logger = "cocoon";
        this.acceptHeader = DEFAULT_ACCEPT;
        this.agentHeader = DEFAULT_USER_AGENT;
        //this.preCompileOnly = Boolean.FALSE;
        this.followLinks = Boolean.TRUE;
        this.targets = new ArrayList();
    }


    /**
     *   Sets the logkitXconf attribute of the CocoonTask object
     *
     * @param  logkitXconf  The new logkitXconf value
     */
    public void setLogkitXconf(File logkitXconf) {
        this.logkitXconf = logkitXconf;
    }


    /**
     *   Sets the logger attribute of the CocoonTask object
     *
     * @param  logger  The new logger value
     */
    public void setLogger(String logger) {
        this.logger = logger;
    }


    /**
     *   Sets the logLevel attribute of the CocoonTask object
     *
     * @param  logLevelOption  The new logLevel value
     */
    public void setLogLevel(LogLevelOption logLevelOption) {
        this.logLevel = logLevelOption.getValue();
    }


    /**
     *   Sets the acceptHeader attribute of the CocoonTask object
     *
     * @param  acceptHeader  The new acceptHeader value
     */
    public void setAcceptHeader(String acceptHeader) {
        this.acceptHeader = acceptHeader;
    }


    /**
     *   Sets the agentHeader attribute of the CocoonTask object
     *
     * @param  agentHeader  The new agentHeader value
     */
    public void setAgentHeader(String agentHeader) {
        this.agentHeader = agentHeader;
    }


    /**
     *   Sets the precompileOnly attribute of the CocoonTask object
     *
     * @param  preCompileOnly  The new precompileOnly value
     */
    public void setPrecompileOnly(boolean preCompileOnly) {
        //this.preCompileOnly = new Boolean(preCompileOnly);
    }


    /**
     *   Sets the followLinks attribute of the CocoonTask object
     *
     * @param  followLinks  The new followLinks value
     */
    public void setFollowLinks(boolean followLinks) {
        this.followLinks = new Boolean(followLinks);
    }


    /**
     *   Sets the targets attribute of the CocoonTask object
     *
     * @param  targets  The new targets value
     */
    public void setTargets(String targets) {
        this.targets = new ArrayList();

        // split target delimited by DEFAULT_DELIM characters
        final String DEFAULT_DELIM = " ,;";
        final String delimiter = DEFAULT_DELIM;
        StringTokenizer st = new StringTokenizer(targets, delimiter);
        while (st.hasMoreTokens()) {
            String target = st.nextToken();
            this.targets.add(target);
        }
    }


    /**
     *   Sets the destDir attribute of the CocoonTask object
     *
     * @param  destDir  The new destDir value
     */
    public void setDestDir(File destDir) {
        this.destDir = getDir(destDir.toString(), true, "dest-dir");
    }


    /**
     *   Sets the workDir attribute of the CocoonTask object
     *
     * @param  workDir  The new workDir value
     */
    public void setWorkDir(File workDir) {
        this.workDir = getDir(workDir.toString(), true, "work-dir");
    }


    /**
     *   Sets the contextDir attribute of the CocoonTask object
     *
     * @param  contextDir  The new contextDir value
     */
    public void setContextDir(File contextDir) {
        this.contextDir = getDir(contextDir.toString(), false, "context-dir");
    }


    /**
     *   Sets the configFile attribute of the CocoonTask object
     *
     * @param  configFile  The new configFile value
     */
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }


    /**
     * Adds a reference to a CLASSPATH defined elsewhere.
     *
     * @param  r  The new classpathRef value
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }


    /**
     * Creates a nested classpath element.
     *
     * @return        Path created
     * @deprecated    no need for creating an additional classloader
     */
    public Path createClasspath() {
        if (cocoonClasspath == null) {
            cocoonClasspath = new Path(project);
        }
        return cocoonClasspath.createPath();
    }



    // MatchingTask implementation
    /**
     *   Execute the ant task launching Cocoon
     *
     * @exception  BuildException  thrown if Cocoon processing fails
     */
    public void execute() throws BuildException {

        // try check some well known places for configFile, and logkitXconf
        if (contextDir != null) {
            if (configFile == null) {
                configFile = getContextDirFile(contextDir, "cocoon.xconf");
            }
            if (logkitXconf == null) {
                logkitXconf = getContextDirFile(contextDir, "logkit.xconf");
            }
        }

        // Check validity of members
        checkValidity();

        DefaultContext rootCtx = new DefaultContext();
        DefaultConfiguration configuration = new DefaultConfiguration("root", "");
        Cocoon cocoon = null;
        CocoonFactory cocoonFactory = null;

        try {
            // fill rootCtx
            rootCtx.put("dest-dir", destDir);
            rootCtx.put("context-root", this.contextDir);
            rootCtx.put(Constants.CONTEXT_WORK_DIR, this.workDir);
            rootCtx.put(Constants.CONTEXT_CONFIG_URL, configFile.toURL());

            ClassLoader classLoader = null;
            if (this.cocoonClasspath != null) {
                //
                // I think there is no real need for creating an additional
                // AntClassLoader
                // CocoonTask was already loaded via an AntClassLoader by Ant
                //
                AntClassLoader antClassLoader = new AntClassLoader(this.project, this.cocoonClasspath, false);

                log("Using Class Loader having classpath " + String.valueOf(this.cocoonClasspath), Project.MSG_INFO);
                classLoader = antClassLoader;
            } else {
                classLoader = this.getClass().getClassLoader();
            }
            rootCtx.put(Constants.CONTEXT_CLASS_LOADER, classLoader);

            // set classloader explicitly
            // this is very important otherwise ClassUtils.loadClass(), et al.
            // will use the system classloader for loading classes, and resources
            // but only this class (the CocoonTask) was loaded via an
            // AntClassLoader
            Thread.currentThread().setContextClassLoader(classLoader);

            // build a configuration from the ant attributes....
            // add configuration elements
            DefaultConfiguration child;
            if (logkitXconf != null) {
                child = new DefaultConfiguration("logkit", "");
                child.setValue(logkitXconf.toString());
                configuration.addChild(child);
            }
            child = new DefaultConfiguration("log-level", "");
            child.setValue(this.logLevel);
            configuration.addChild(child);

            child = new DefaultConfiguration("follow-links", "");
            child.setValue(this.followLinks.toString());
            configuration.addChild(child);

            DefaultConfiguration headers = new DefaultConfiguration("headers", "");
            child = new DefaultConfiguration("parameter", "");
            child.setAttribute("name", "accept");
            child.setAttribute("value", this.acceptHeader);
            headers.addChild(child);

            child = new DefaultConfiguration("parameter", "");
            child.setAttribute("name", "user-agent");
            child.setAttribute("value", this.agentHeader);
            headers.addChild(child);

            configuration.addChild(headers);

            // create a Cocoon instance
            cocoonFactory = new CocoonFactory();
            cocoonFactory.enableLogging(new ConsoleLogger());
            cocoonFactory.contextualize(rootCtx);
            cocoonFactory.configure(configuration);

        } catch (Exception e) {
            String message = "Cannot create cocoon factory";
            throw new BuildException(message, e);
        }

        try {
            cocoon = cocoonFactory.createCocoon();
        } catch (Exception e) {
            String message = "Cannot create cocoon object";
            throw new BuildException(message, e);
        }

        // loop over all targets
        try {
            Set uniqueTargets = new HashSet();
            uniqueTargets.addAll(targets);
            CocoonProcessorDelegate cpd = cocoonFactory.createCocoonProcessorDelegate(cocoon, configuration);
            cpd.processAllUris(uniqueTargets);
            //cpd.dumpVisitedLinks();
        } catch (Exception e) {
            String message = "Cannot process Uri(s) by Cocoon";
            throw new BuildException(message, e);
        } finally {

            cocoonFactory.disposeCocoon(cocoon);
        }
    }


    /**
     *   Gets the contextDirFile attribute of the CocoonTask object.
     *   Try to locate a file name relative to Cocoon's context directory.
     *   Check ${contextDir}/WEB-INF, ${contextDir}/ locations if
     *   there is a file named name.
     *
     * @param  contextDir  Cocoon's context directory
     * @param  name        a pure file name
     * @return             File full path of an existing file name, or null
     */
    protected File getContextDirFile(File contextDir, String name) {

        // 1 try ${contextDir}/WEB-INF/${name}
        final File fullName1 = new File(contextDir, "WEB-INF/" + name);
        if (fullName1.exists() && fullName1.canRead()) {
            return fullName1;
        }

        // 2 try ${contextDir}/${name}
        final File fullName2 = new File(contextDir, name);
        if (fullName2.exists() && fullName2.canRead()) {
            return fullName2;
        }
        String message = "Cannot find, or access file " + String.valueOf(name) + " " +
                "neither " + String.valueOf(fullName1) + ", " +
                "nor " + String.valueOf(fullName2);
        log(message, Project.MSG_INFO);
        return null;
    }


    /**
     * Get a <code>File</code> representing a directory.
     * Create, and check existance, read- writeability of a directory.
     *
     * @param  dir                 a <code>String</code> with a directory name
     * @param  type                a <code>String</code> describing the type of directory
     * @param  create              true if directory should be created
     * @return                     a <code>File</code> value
     * @exception  BuildException  throw if checks fails
     */
    protected File getDir(String dir, boolean create, String type) throws BuildException {

        log("Getting handle to " + type + " directory '" + dir + "'", Project.MSG_INFO);

        File d = new File(dir);
        if (!d.exists()) {
            if (create && !d.mkdirs()) {
                String message = "Error creating " + type + " directory '" + d + "'";
                throw new BuildException(message);
            }
        }
        if (!d.isDirectory()) {
            String message = "'" + d + "' is not a directory.";
            throw new BuildException(message);
        }

        if (!(d.canRead() && d.canWrite())) {
            String message = "Directory '" + d + "' is not readable/writable";
            throw new BuildException(message);
        }
        return d;
    }


    /**
     *   Check if all parameters for running Cocoon are set properly
     *
     * @exception  BuildException  is thrown if at least one parameter is invalid
     */
    protected void checkValidity() throws BuildException {
        if (destDir == null) {
            throw new BuildException("Set attribute destDir!");
        } else {
            destDir = getDir(destDir.toString(), true, "dest-dir");
        }

        if (workDir == null) {
            throw new BuildException("Set attribute workDir!");
        } else {
            workDir = getDir(workDir.toString(), true, "work-dir");
        }
        if (contextDir == null) {
            throw new BuildException("Set attribute contextDir!");
        } else {
            contextDir = getDir(contextDir.toString(), false, "contex-dir");
        }

        if (configFile == null) {
            throw new BuildException("Set attribute configFile!");
        }
        if (logger == null) {
            throw new BuildException("Set attribute logger!");
        }
        if (logLevel == null) {
            throw new BuildException("Set attribute logLevel!");
        }
        if (acceptHeader == null) {
            throw new BuildException("Set attribute acceptHeader!");
        }
        if (agentHeader == null) {
            throw new BuildException("Set attribute agentHeader!");
        }

        if (!destDir.exists() || !destDir.isDirectory()) {
            throw new BuildException("Attribute destDir directory " + String.valueOf(destDir) +
                    " does not exists, or is not a directory!");
        }
        if (!contextDir.exists() || !contextDir.isDirectory()) {
            throw new BuildException("Attribute contextDir directory " + String.valueOf(contextDir) +
                    " does not exists, or is not a directory!");
        }
        if (!workDir.exists() || !workDir.isDirectory()) {
            throw new BuildException("Attribute worktDir directory " + String.valueOf(workDir) +
                    " does not exists, or is not a directory!");
        }
    }


    /**
     * A factory creating Cocoon objects.
     * This class encapsulates creation, disposing of Cocoon objects, and
     * creating of classes for using Cocoon in some task, subtask execution
     *
     * @author    huber@apache.org
     */
    public static class CocoonFactory extends AbstractLogEnabled
             implements Contextualizable, Configurable {

        private LogKitLoggerManager logKitLoggerManager;
        private Logger logger;
        private DefaultContext ctx;


        /**
         * Constructor for the CocoonFactory object
         */
        public CocoonFactory() { }


        /**
         * contextualize the CocoonFactory
         *
         * Expecting at least following context entries
         *
         * <ul>
         *  <li>context-root</li>
         *  <li>CONTEXT_CLASSPATH</li>
         *  <li>CONTEXT_CLASS_LOADER</li>
         *  <li>CONTEXT_ENVIRONMENT_CONTEXT</li>
         *  <li>CONTEXT_WORK_DIR</li>
         *  <li>CONTEXT_CONFIG_URL</li>
         * </ul>
         *
         * @param  context               parent context
         * @exception  ContextException  thrown if parent context fails to provide
         *   mandadory context entries
         */
        public void contextualize(Context context) throws ContextException {
            this.ctx = new DefaultContext(context);

            File contextDir = (File) this.ctx.get("context-root");
            File workDir = (File) this.ctx.get(Constants.CONTEXT_WORK_DIR);

            CommandLineContext clContext = new CommandLineContext(contextDir.toString());
            clContext.enableLogging(getLogger());
            this.ctx.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, clContext);
            this.ctx.put(Constants.CONTEXT_CLASSPATH, getClassPath(contextDir));

            this.ctx.put(Constants.CONTEXT_UPLOAD_DIR, new File(contextDir, "upload-dir"));
            this.ctx.put(Constants.CONTEXT_CACHE_DIR, new File(workDir, "cache-dir"));
        }


        /**
         *   Configure the Cocoon factory
         *
         * @param  configuration               Cocoon factory configuration
         * @exception  ConfigurationException  thrown if configuration fails
         */
        public void configure(Configuration configuration) throws ConfigurationException {
            Configuration child;

            // configure logLevel
            String logLevel = "WARN";
            child = configuration.getChild("log-level", false);
            if (child != null) {
                logLevel = child.getValue();
            } else {
                logLevel = "WARN";
            }

            // configure the logKitLoggerManager,
            // either by using a logkit.xconf file or by a logger
            final Priority priority = Priority.getPriorityForName(logLevel);
            Hierarchy.getDefaultHierarchy().setDefaultPriority(priority);
            this.logger = new LogKitLogger(Hierarchy.getDefaultHierarchy().getLoggerFor(""));

            child = configuration.getChild("logKit", false);
            if (child != null) {
                String logKit = child.getValue();
                String logKitLogCategory = child.getAttribute("category", "cocoon");

                if (logKit != null) {
                    try {
                        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                        final Configuration logKitConf = builder.buildFromFile(logKit);
                        this.logKitLoggerManager = new LogKitLoggerManager(Hierarchy.getDefaultHierarchy());

                        final DefaultContext subcontext = new DefaultContext(this.ctx);
                        File contextDir = (File) this.ctx.get("context-root");
                        subcontext.put("context-root", contextDir);
                        this.logKitLoggerManager.contextualize(subcontext);
                        this.logKitLoggerManager.configure(logKitConf);
                        logger = this.logKitLoggerManager.getLoggerForCategory(logKitLogCategory);
                    } catch (Exception e) {
                        getLogger().error("Cannot initialize log-kit-manager from logkit-xconf " + String.valueOf(logKit));
                        // clean logKitLoggerManager, try init it without the logkit-xconf
                        this.logKitLoggerManager = null;
                    }
                }
            }

            if (this.logKitLoggerManager == null) {
                this.logKitLoggerManager = new LogKitLoggerManager(Hierarchy.getDefaultHierarchy());
                this.logKitLoggerManager.enableLogging(logger);
            }
        }


        /**
         * create a new Cocoon instance
         *
         * @return                             Cocoon the Cocoon instance
         * @exception  ContextException        thrown if configuring of Cocoon instance fails
         * @exception  ConfigurationException  thrown if contextualizing of Cocoon instance fails
         * @exception  Exception               thrown if initializing of Cocoon instance fails
         */
        public Cocoon createCocoon() throws Exception, ContextException, ConfigurationException {
            Cocoon cocoon = new Cocoon();
            cocoon.enableLogging(logger);
            cocoon.contextualize(this.ctx);
            cocoon.setLoggerManager(logKitLoggerManager);
            cocoon.initialize();
            return cocoon;
        }


        /**
         *  Dispose a cocoon instance.
         *  Don't forget to invoke this method if you have retrieved a Cocoon
         *  instance via <code>createCocoon()</code>.
         *
         * @param  cocoon  the Cocoon instance
         */
        public void disposeCocoon(Cocoon cocoon) {
            if (cocoon != null) {
                cocoon.dispose();
            }
        }


        /**
         *   Create a CocoonProcessorDelegate for performing some Cocoon relevant
         *   operations.
         *
         * @param  cocoon         Cocoon instance
         * @param  configuration  of the CocoonProcessorDelegate
         * @return                CocoonProcessorDelegate instance
         * @exception  Exception  thrown if contextualizing, configuring, or creating
         *   of CocoonProcessorDelegate instance fails.
         */
        public CocoonProcessorDelegate createCocoonProcessorDelegate(Cocoon cocoon, Configuration configuration) throws Exception {
            CocoonProcessorDelegate cpd = new CocoonProcessorDelegate(cocoon);
            cpd.enableLogging(logger);
            cpd.contextualize(this.ctx);
            cpd.configure(configuration);
            cpd.initialize();
            return cpd;
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
         * @param  contextDir  Description of Parameter
         * @return             a <code>String</code> value
         */
        protected String getClassPath(final File contextDir) {
            StringBuffer buildClassPath = new StringBuffer();
            File classDir = new File(contextDir, "WEB-INF/classes");
            File root = new File(contextDir, "WEB-INF/lib");

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

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Context classpath: " + buildClassPath.toString());
            }

            return buildClassPath.toString();
        }
    }


    /**
     * Enumerated attribute with the values "DEBUG", "INFO", "WARN", "ERROR",
     * and "FATAL_ERROR"
     *
     * @author    huber@apache.org
     */
    public static class LogLevelOption extends EnumeratedAttribute {
        /**
         *   Gets the values attribute of the LogLevelOption object
         *
         * @return    The values value
         */
        public String[] getValues() {
            final String[] values = {
                    "DEBUG", "INFO", "WARN", "ERROR", "FATAL_ERROR"
                    };
            return values;
        }
    }

}

