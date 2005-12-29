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
package org.apache.cocoon.blocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.CoreFatalException;
import org.apache.cocoon.core.CoreInitializationException;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.core.PropertyProvider;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.SingleComponentServiceManager;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.servlet.SettingsHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * This is an utility class to create a new Cocoon instance.
 * 
 * TODO - Remove dependencies to LogKit and Log4J
 *
 * @version $Id$
 * @since 2.2
 */
public class CoreUtil {

    /** Parameter map for the context protocol */
    private static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /**
     * Application <code>Context</code> Key for the servlet configuration
     * @since 2.1.3
     */
    public static final String CONTEXT_SERVLET_CONFIG = "servlet-config";

    private ServletConfig config;
	private ServletContext servletContext;

    /** "legacy" support: create an avalon context. */
    private final DefaultContext appContext = new ComponentContext();
    
    private Context environmentContext;

    /** The settings. */
    private MutableSettings settings;

    /** The parent service manager. */
    private ServiceManager parentManager;

    /** The root logger. */
    private Logger log;

    private ClassLoader classloader;

	private File contextForWriting = null;
	private String contextURL;
	// path to a file that is supposed to be present in the servlet context
	// and that is used for calculating the context URI
	private String knownFile;

	public CoreUtil(ServletConfig config) throws ServletException {
		this(config, "WEB-INF/web.xml");
	}
	/**
     * Setup a new instance.
     * @param config
     * @throws ServletException
     */
    public CoreUtil(ServletConfig config, String knownFile) throws ServletException {
		this.config = config;
		this.knownFile = knownFile;
		this.servletContext = this.config.getServletContext();
		this.servletContext.log("Initializing Apache Cocoon " + Constants.VERSION);
		
		String writeableContextPath = this.servletContext.getRealPath("/");
		String path = writeableContextPath;
		if (path == null) {
		    // Try to figure out the path of the root from that of a known file
			this.servletContext.log("Figuring out root from " + this.knownFile);
		    try {
		        path = this.servletContext.getResource("/" + this.knownFile).toString();
				this.servletContext.log("Got " + path);
		    } catch (MalformedURLException me) {
		        throw new ServletException("Unable to get resource '" + this.knownFile + "'.", me);
		    }
		    path = path.substring(0, path.length() - this.knownFile.length());
			this.servletContext.log("And servlet root " + path);
		}
		try {
		    if (path.indexOf(':') > 1) {
		        this.contextURL = path;
		    } else {
		        this.contextURL = new File(path).toURL().toExternalForm();
		    }
		} catch (MalformedURLException me) {
		    // VG: Novell has absolute file names starting with the
		    // volume name which is easily more then one letter.
		    // Examples: sys:/apache/cocoon or sys:\apache\cocoon
		    try {
		        this.contextURL= new File(path).toURL().toExternalForm();
		    } catch (MalformedURLException ignored) {
		        throw new ServletException("Unable to determine servlet context URL.", me);
		    }
		}
		if (writeableContextPath != null) {
			this.contextForWriting = new File(writeableContextPath);
		}
    	this.environmentContext = new HttpContext(config.getServletContext());
    	this.init();
    }
    
    private void init() throws ServletException {
        // first let's set up the appContext with some values to make
        // the simple source resolver work

        // add root url
        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL,
                           new URL(this.contextURL));
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }

        // add environment context
        this.appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT,
                            this.environmentContext);

        // now add environment specific information
        this.appContext.put(CONTEXT_SERVLET_CONFIG, this.config);

        // create settings
        this.settings = this.createSettings();

        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = this.settings.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            if (this.contextForWriting == null) {
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
                    workDir = new File(this.contextForWriting, workDirParam);
                }
            }
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        this.appContext.put(Constants.CONTEXT_WORK_DIR, workDir);
        this.settings.setWorkDirectory(workDir.getAbsolutePath());

        // Output some debug info
        this.servletContext.log("Context URL: " + this.contextURL);
        this.servletContext.log("Writeable Context: " + this.contextForWriting);
        if (workDirParam != null) {
        	this.servletContext.log("Using work-directory " + workDir);
        } else {
        	this.servletContext.log("Using default work-directory " + workDir);
        }

        final String uploadDirParam = this.settings.getUploadDirectory();
        File uploadDir;
        if (uploadDirParam != null) {
            if (this.contextForWriting == null) {
                uploadDir = new File(uploadDirParam);
            } else {
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    uploadDir = uploadDirParamFile;
                } else {
                    // No : consider it relative to context path
                    uploadDir = new File(this.contextForWriting, uploadDirParam);
                }
            }
            this.servletContext.log("Using upload-directory " + uploadDir);
        } else {
            uploadDir = new File(workDir, "upload-dir" + File.separator);
            this.servletContext.log("Using default upload-directory " + uploadDir);
        }
        uploadDir.mkdirs();
        appContext.put(Constants.CONTEXT_UPLOAD_DIR, uploadDir);
        this.settings.setUploadDirectory(uploadDir.getAbsolutePath());

        String cacheDirParam = this.settings.getCacheDirectory();
        File cacheDir;
        if (cacheDirParam != null) {
            if (this.contextForWriting == null) {
                cacheDir = new File(cacheDirParam);
            } else {
                // Context path exists : is cache-directory absolute ?
                File cacheDirParamFile = new File(cacheDirParam);
                if (cacheDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    cacheDir = cacheDirParamFile;
                } else {
                    // No : consider it relative to context path
                    cacheDir = new File(this.contextForWriting, cacheDirParam);
                }
            }
            this.servletContext.log("Using cache-directory " + cacheDir);
        } else {
            cacheDir = new File(workDir, "cache-dir" + File.separator);
            File parent = cacheDir.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            this.servletContext.log("cache-directory was not set - defaulting to " + cacheDir);
        }
        cacheDir.mkdirs();
        appContext.put(Constants.CONTEXT_CACHE_DIR, cacheDir);
        this.settings.setCacheDirectory(cacheDir.getAbsolutePath());
		String configFileName = this.settings.getConfiguration();
		final String usedFileName;
		
		if (configFileName == null) {
			this.servletContext.log("Servlet initialization argument 'configurations' not specified, attempting to use '/WEB-INF/cocoon.xconf'");
			usedFileName = "/WEB-INF/cocoon.xconf";
		} else {
			usedFileName = configFileName;
		}
		
		this.servletContext.log("Using configuration file: " + usedFileName);
		
		URL result;
		try {
			// test if this is a qualified url
			if (usedFileName.indexOf(':') == -1) {
				result = this.config.getServletContext().getResource(usedFileName);
			} else {
				result = new URL(usedFileName);
			}
		} catch (Exception mue) {
			String msg = "Init parameter 'configurations' is invalid : " + usedFileName;
			this.servletContext.log(msg, mue);
			throw new ServletException(msg, mue);
		}
		
		if (result == null) {
			File resultFile = new File(usedFileName);
			if (resultFile.isFile()) {
				try {
					result = resultFile.getCanonicalFile().toURL();
				} catch (Exception e) {
					String msg = "Init parameter 'configurations' is invalid : " + usedFileName;
					this.servletContext.log(msg, e);
					throw new ServletException(msg, e);
				}
			}
		}
		
		if (result == null) {
			String msg = "Init parameter 'configurations' doesn't name an existing resource : " + usedFileName;
			this.servletContext.log(msg);
			throw new ServletException(msg);
		}

        // update configuration
        final URL u = result;
        this.settings.setConfiguration(u.toExternalForm());
        this.appContext.put(Constants.CONTEXT_CONFIG_URL, u);

        // set encoding
        this.appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, settings.getFormEncoding());

        // set class loader
        this.appContext.put(Constants.CONTEXT_CLASS_LOADER, this.classloader);

        // create the Core object
        final Core core = this.createCore();

        // create parent service manager
        this.parentManager = this.getParentServiceManager(core);

        // settings can't be changed anymore
        settings.makeReadOnly();

        // put the core into the context - this is for internal use only
        // The Cocoon container fetches the Core object using the context.
        this.appContext.put(Core.ROLE, core);

        // FIXME - for now we just set an empty string as this information is looked up
        //         by other components
        this.appContext.put(Constants.CONTEXT_CLASSPATH, "");
    }

    public Core getCore() {
        try {
            return (Core)this.parentManager.lookup(Core.ROLE);
        } catch (ServiceException neverIgnore) {
            // this should never happen!
            throw new CoreFatalException("Fatal exception: no Cocoon core available.", neverIgnore);
        }
    }

    /**
     * Create a new core instance.
     * This method can be overwritten in sub classes.
     * @return A new core object.
     */
    private Core createCore() {
        final Core c = new Core(this.settings, this.appContext);
        return c;
    }

    /**
     * Return the settings object.
     */
    public Settings getSettings() {
        return this.settings;
    }

    public ServiceManager getServiceManager() {
    	return this.parentManager;
    }
    
    /**
     * The root context path for the servlet
     * @return context URL
     */
    public String getContextURL() {
    	return this.contextURL;
    }
    /**
     * Instatiates the parent service manager, as specified in the
     * parent-service-manager init parameter.
     *
     * If none is specified, the method returns <code>null</code>.
     *
     * @return the parent service manager, or <code>null</code>.
     */
    private ServiceManager getParentServiceManager(Core core) {
        String parentServiceManagerClass = this.settings.getParentServiceManagerClassName();
        String parentServiceManagerInitParam = null;
        if (parentServiceManagerClass != null) {
            int dividerPos = parentServiceManagerClass.indexOf('/');
            if (dividerPos != -1) {
                parentServiceManagerInitParam = parentServiceManagerInitParam.substring(dividerPos + 1);
                parentServiceManagerClass = parentServiceManagerClass.substring(0, dividerPos);
            }
        }

        ServiceManager parentServiceManager = null;
        if (parentServiceManagerClass != null) {
            try {
                Class pcm = ClassUtils.loadClass(parentServiceManagerClass);
                Constructor pcmc = pcm.getConstructor(new Class[]{String.class});
                parentServiceManager = (ServiceManager) pcmc.newInstance(new Object[]{parentServiceManagerInitParam});

                ContainerUtil.enableLogging(parentServiceManager, this.log);
                ContainerUtil.contextualize(parentServiceManager, this.appContext);
                ContainerUtil.initialize(parentServiceManager);
            } catch (Exception e) {
                if (this.log.isErrorEnabled()) {
                    this.log.error("Could not initialize parent component manager.", e);
                }
            }
        }
        return new SingleComponentServiceManager(parentServiceManager, core, Core.ROLE);
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) context://WEB-INF/properties/*.properties
     *    Default values for the core and each block - the order in which the files are read is not guaranteed.
     * 2) context://WEB-INF/properties/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 3) Property providers (ToBeDocumented)
     * 4) The environment (CLI, Servlet etc.) adds own properties (e.g. from web.xml)
     * 5) Additional property file specified by the "org.apache.cocoon.settings" system property or
     *    if the property is not found, the file ".cocoon/settings.properties" is tried to be read from
     *    the user directory.
     * 6) System properties
     *
     * @return A new Settings object
     */
    private MutableSettings createSettings() {
        // get the running mode
        final String mode = System.getProperty(Settings.PROPERTY_RUNNING_MODE, Settings.DEFAULT_RUNNING_MODE);
        this.config.getServletContext().log("Running in mode: " + mode);

        // create an empty settings objects
        final MutableSettings s = new MutableSettings();

        // we need our own resolver
        final SourceResolver resolver = this.createSourceResolver(new LoggerWrapper(this.config.getServletContext()));

        // now read all properties from the properties directory
        this.readProperties("context://WEB-INF/properties", s, resolver);
        // read all properties from the mode dependent directory
        this.readProperties("context://WEB-INF/properties/" + mode, s, resolver);

        // Next look for custom property providers
        Iterator i = s.getPropertyProviders().iterator();
        while ( i.hasNext() ) {
            final String className = (String)i.next();
            try {
                PropertyProvider provider = (PropertyProvider)ClassUtils.newInstance(className);
                s.fill(provider.getProperties());
            } catch (Exception ignore) {
                this.config.getServletContext().log("Unable to get property provider for class " + className, ignore);
                this.config.getServletContext().log("Continuing initialization.");            
            }
        }
        // fill from the environment configuration, like web.xml etc.
        // fill from the servlet parameters
		SettingsHelper.fill(s, this.config);
		if ( s.getWorkDirectory() == null ) {
			final File workDir = (File)this.config.getServletContext().getAttribute("javax.servlet.context.tempdir");
			s.setWorkDirectory(workDir.getAbsolutePath());
		}
		if ( s.getLoggingConfiguration() == null ) {
			s.setLoggingConfiguration("/WEB-INF/logkit.xconf");
		}

        // read additional properties file
        String additionalPropertyFile = s.getProperty(Settings.PROPERTY_USER_SETTINGS, 
                                                      System.getProperty(Settings.PROPERTY_USER_SETTINGS));
        // if there is no property defining the addition file, we try it in the home directory
        if ( additionalPropertyFile == null ) {
            additionalPropertyFile = System.getProperty("user.home") + File.separator + ".cocoon/settings.properties";
            final File testFile = new File(additionalPropertyFile);
            if ( !testFile.exists() ) {
                additionalPropertyFile = null;
            }
        }
        if ( additionalPropertyFile != null ) {
            this.config.getServletContext().log("Reading user settings from '" + additionalPropertyFile + "'");
            final Properties p = new Properties();
            try {
                FileInputStream fis = new FileInputStream(additionalPropertyFile);
                p.load(fis);
                fis.close();
            } catch (IOException ignore) {
                this.config.getServletContext().log("Unable to read '" + additionalPropertyFile + "'.", ignore);
                this.config.getServletContext().log("Continuing initialization.");
            }
        }
        // now overwrite with system properties
        s.fill(System.getProperties());

        return s;
    }

    /**
     * Read all property files from the given directory and apply them to the settings.
     */
    private void readProperties(String directoryName,
                                  MutableSettings s,
                                  SourceResolver resolver) {
        Source directory = null;
        try {
            directory = resolver.resolveURI(directoryName, null, CONTEXT_PARAMETERS);
            if (directory.exists() && directory instanceof TraversableSource) {
                final Iterator c = ((TraversableSource) directory).getChildren().iterator();
                while (c.hasNext()) {
                    final Source src = (Source) c.next();
                    if ( src.getURI().endsWith(".properties") ) {
                        final InputStream propsIS = src.getInputStream();
                        this.config.getServletContext().log("Reading settings from '" + src.getURI() + "'.");
                        final Properties p = new Properties();
                        p.load(propsIS);
                        propsIS.close();
                        s.fill(p);
                    }
                }
            }
        } catch (IOException ignore) {
            this.config.getServletContext().log("Unable to read from directory 'WEB-INF/properties'.", ignore);
            this.config.getServletContext().log("Continuing initialization.");            
        } finally {
            resolver.release(directory);
        }
    }

    /**
     * Create a simple source resolver.
     */
    private SourceResolver createSourceResolver(Logger logger) {
        // Create our own resolver
        final SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(this.appContext);
        } catch (ContextException ce) {
            throw new CoreInitializationException(
                    "Cannot setup source resolver.", ce);
        }
        return resolver;        
    }

    private static final class LoggerWrapper implements Logger {
        private final ServletContext context;

        public LoggerWrapper(ServletContext context) {
            this.context = context;
        }

        private void text(String arg0, Throwable arg1) {
            if ( arg1 != null ) {
                this.context.log(arg0, arg1);
            } else {
                this.context.log(arg0);
            }
        }

        public void debug(String arg0, Throwable arg1) {
            // we ignore debug
        }

        public void debug(String arg0) {
            // we ignore debug
        }

        public void error(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        public void error(String arg0) {
            this.text(arg0, null);
        }

        public void fatalError(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        public void fatalError(String arg0) {
            this.text(arg0, null);
        }

        public Logger getChildLogger(String arg0) {
            return this;
        }

        public void info(String arg0, Throwable arg1) {
            // we ignore info
        }

        public void info(String arg0) {
            // we ignore info
        }

        public boolean isDebugEnabled() {
            return false;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        public boolean isFatalErrorEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return false;
        }

        public boolean isWarnEnabled() {
            return false;
        }

        public void warn(String arg0, Throwable arg1) {
            // we ignore warn
        }

        public void warn(String arg0) {
            // we ignore warn
        }
    }
}
