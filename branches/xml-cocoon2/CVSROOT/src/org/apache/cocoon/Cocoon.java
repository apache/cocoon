/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.Modifiable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.SAXConfigurationHandler;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Initializable;

import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.Manager;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.DefaultComponentManager;

import org.apache.avalon.AbstractLoggable;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.4.2.52 $ $Date: 2001-02-15 18:17:05 $
 */
public class Cocoon extends AbstractLoggable
  implements Component, Configurable, ComponentManager, Modifiable, Processor, Constants, Contextualizable {

    /** The application context */
    private Context context;

    /** The table of role-class */
    private HashMap components = new HashMap();

    /** The table of role-configuration */
    private HashMap configurations = new HashMap();

    /** The configuration file */
    private URL configurationFile;

    /** The sitemap file */
    private String sitemapFileName;

    /** The configuration tree */
    private Configuration configuration;

    /** The sitemap manager */
    private Manager sitemapManager;

    /** The classpath (null if not available) */
    private String classpath;

    /** The working directory (null if not available) */
    private File workDir;

    /** The component manager. */
    private DefaultComponentManager componentManager;

    /**
     * Create a new <code>Cocoon</code> instance.
     */
    public Cocoon() throws ConfigurationException {
        // Set the system properties needed by Xalan2.
        setSystemProperties();
    }

    public void contextualize(Context context) {
        if (this.context == null) {
            this.context = context;

            this.classpath = (String) context.get(Constants.CONTEXT_CLASSPATH);
            this.workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
            this.configurationFile = (URL) context.get(Constants.CONTEXT_CONFIG_URL);
        }
    }

    public void init() throws Exception {
        this.componentManager = new DefaultComponentManager();
        this.componentManager.contextualize(this.context);
        this.componentManager.setLogger(getLogger());

        getLogger().debug("New Cocoon object.");

        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        String parser = System.getProperty(PARSER_PROPERTY, DEFAULT_PARSER);
        getLogger().debug("Using parser: " + parser);

        try {
            this.componentManager.addComponent(Roles.PARSER, ClassUtils.loadClass(parser),null);
        } catch ( Exception e ) {
            getLogger().error("Could not load parser, Cocoon object not created.", e);
            throw new ConfigurationException("Could not load parser " + parser, e);
        }
        this.componentManager.addComponentInstance(Roles.COCOON, this);

        getLogger().debug("Classpath = " + classpath);

        getLogger().debug("Work directory = " + workDir.getCanonicalPath());

        Parser p = (Parser) this.lookup(Roles.PARSER);
        SAXConfigurationHandler b = new SAXConfigurationHandler();
        InputSource is = new InputSource(this.configurationFile.openStream());

        p.setContentHandler(b);
        is.setSystemId(this.configurationFile.toExternalForm());
        p.parse(is);

        this.configure(b.getConfiguration());
    }

    /**
     * Get the local classpath
     * @return the classpath available to this instance or null if not available.
     */
    public String getClasspath() {
        return this.classpath;
    }

    /**
     * Get the local workpath
     * @return the workpath available to this instance or null if not available.
     */
    public File getWorkDir() {
        return this.workDir;
    }

    /**
     * Configure this <code>Cocoon</code> instance.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {

        this.configuration = conf;

        getLogger().debug("Root configuration: " + conf.getName());
        if (!"cocoon".equals(conf.getName())) {
            throw new ConfigurationException("Invalid configuration file\n" + conf.toString());
        }

        getLogger().debug("Configuration version: " + conf.getAttribute("version"));
        if (!CONF_VERSION.equals(conf.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '"
                + CONF_VERSION + "'.");
        }

        getLogger().debug("Setting up components...");
        this.componentManager.configure(conf);

        getLogger().debug("Setting up the sitemap.");
        // Create the sitemap
        Configuration sconf = conf.getChild("sitemap");

        this.sitemapManager = new Manager(null);
        this.sitemapManager.setLogger(getLogger());
        this.sitemapManager.compose(this);
        this.sitemapManager.configure(conf);
        this.sitemapFileName = sconf.getAttribute("file");
        if (this.sitemapFileName == null) {
            getLogger().error("No sitemap file name");
            throw new ConfigurationException("No sitemap file name\n" + conf.toString());
        }

        getLogger().debug("Sitemap location = " + this.sitemapFileName);
    }

    /**
     * Get the <code>Component</code> associated with the given role.
     */
    public Component lookup(String role)
    throws ComponentManagerException {
        return this.componentManager.lookup(role);
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     */
    public boolean modifiedSince(long date) {
        boolean answer;

        try {
            answer = date < this.configurationFile.openConnection().getLastModified();
        } catch (IOException ioe) {
            getLogger().warn("Problem checking the date on the Configuration File.", ioe);
            answer = false;
        }

        return answer;
    }

    /**
     * Process the given <code>Environment</code> to produce the output.
     */
    public boolean process(Environment environment)
    throws Exception {
        return this.sitemapManager.invoke(environment, "", this.sitemapFileName, true);
    }

  /**
   * Sets required system properties .
   */
    protected void setSystemProperties()
    {
      java.util.Properties props = new java.util.Properties();

      // FIXME We shouldn't have to specify the SAXParser...
      // This is needed by Xalan2, it is used by org.xml.sax.helpers.XMLReaderFactory
      // to locate the SAX2 driver.
      props.put("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");

      java.util.Properties systemProps = System.getProperties();
      Enumeration propEnum = props.propertyNames();
      while(propEnum.hasMoreElements())
      {
        String prop = (String)propEnum.nextElement();
        if(!systemProps.containsKey(prop))
          systemProps.put(prop, props.getProperty(prop));
      }
      System.setProperties(systemProps);
    }
}
