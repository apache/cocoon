/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Enumeration;

import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.Modifiable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.SAXConfigurationBuilder;

import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.Manager;
import org.apache.cocoon.util.ClassUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.4.2.30 $ $Date: 2000-10-12 16:43:11 $
 */
public class Cocoon
  implements Component, Configurable, ComponentManager, Modifiable, Processor, Constants {

    /** The table of role-class */
    private HashMap components = new HashMap();
    
    /** The table of role-configuration */
    private HashMap configurations = new HashMap();
    
    /** The configuration file */ 
    private File configurationFile; 
    
    /** The sitemap file */ 
    private String sitemapFileName; 
    
    /** The configuration tree */
    private Configuration configuration;
    
    /** The sitemap manager */
    private Manager sitemapManager;
    
    /** The root uri/path */
    private URL root;

    /** The classpath (null if not available) */
    private String classpath;

    /** The working directory (null if not available) */
    private String workDir;
            
    /**
     * Create a new <code>Cocoon</code> instance.
     */
    protected Cocoon() throws ConfigurationException {
        // Set the system properties needed by Xalan2.
        setSystemProperties();

        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        String parser = System.getProperty(PARSER_PROPERTY, DEFAULT_PARSER);
        try {
            this.components.put("parser", ClassUtils.loadClass(parser));
        } catch (Exception e) {
            throw new ConfigurationException("Error creating parser (" + parser + ")", null);
        }
        String processor = System.getProperty(PROCESSOR_PROPERTY, DEFAULT_PROCESSOR);
        try {
			trax.Processor.setPlatformDefaultProcessor(processor);
            this.components.put("processor", ClassUtils.loadClass(parser));
        } catch (Exception e) {
            throw new ConfigurationException("Error creating processor (" + processor + ")", null);
        }
    }
    
    /**
     * Create a new <code>Cocoon</code> object, parsing configuration from
     * the specified file.
     */
    public Cocoon(File configurationFile, String classpath, String workDir)
    throws SAXException, IOException, ConfigurationException {
        this();

        this.classpath = classpath;
        
        this.workDir = workDir;
                
        this.configurationFile = configurationFile;
        if (!configurationFile.isFile()) {
            throw new FileNotFoundException(configurationFile.toString());
        }
            
        Parser p = (Parser) this.getComponent("parser");
        SAXConfigurationBuilder b = new SAXConfigurationBuilder();
        p.setContentHandler(b);
        String path = this.configurationFile.getPath();
        InputSource is = new InputSource(new FileReader(path));
        is.setSystemId(path);
        p.parse(is);
        this.setConfiguration(b.getConfiguration());
        this.root = this.configurationFile.getParentFile().toURL();
    }

    /**
     * Set the cocoon root.
     * @param root The new Cocoon root.
     */
    public void setRoot(URL root) {
        this.root = root;
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
    public String getWorkDir() {
        return this.workDir;
    }

    /**
     * Configure this <code>Cocoon</code> instance.
     */
    public void setConfiguration(Configuration conf)
    throws ConfigurationException {
    
        this.configuration = conf;
        
        if (!"cocoon".equals(conf.getName())) {
            throw new ConfigurationException("Invalid configuration file", conf);
        }
        if (!CONF_VERSION.equals(conf.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '" 
                + CONF_VERSION + "'.", conf);
        }
            
        // Set components
        Enumeration e = conf.getConfigurations("component");
        while (e.hasMoreElements()) {
            Configuration co = (Configuration) e.nextElement();
            String role = co.getAttribute("role");
            String className = co.getAttribute("class");
            try {
                this.components.put(role, ClassUtils.loadClass(className));
                this.configurations.put(role, co);
            } catch (Exception ex) {
                // if the component is not found, there is no need to stop at this point.
            }
        }

        // Create the sitemap
        Configuration sconf = conf.getConfiguration("sitemap");
        if (sconf == null) {
            throw new ConfigurationException("No sitemap configuration", conf);
        }
        this.sitemapManager = new Manager();
        this.sitemapManager.setComponentManager(this);
        this.sitemapManager.setConfiguration(conf);
        this.sitemapFileName = sconf.getAttribute("file");
        if (this.sitemapFileName == null) {
            throw new ConfigurationException("No sitemap file name", conf);
        }
    }

    /**
     * Get the <code>Component</code> associated with the given role.
     */
    public Component getComponent(String role)
    throws ComponentNotFoundException, ComponentNotAccessibleException {

        if (role == null) throw new ComponentNotFoundException("Role cannot be null");
        if (role.equals("cocoon")) return this;
        
        Class c = (Class) this.components.get(role);
        if (c == null) {
            throw new ComponentNotFoundException("Can't find component " + role);
        }
        
        try {
            Component comp = (Component) c.newInstance();
            if (comp instanceof Composer)
                ((Composer)comp).setComponentManager(this);
            if (comp instanceof Configurable) {
                Configuration conf = (Configuration) this.configurations.get(role);
                if (conf != null) ((Configurable) comp).setConfiguration(conf);
            }
            return comp;
        } catch (Exception e) {
            throw new ComponentNotAccessibleException("Can't access class '" +
                        c.getName() + "' with role '" + role + "' due to a " +
                        e.getClass().getName() + "[" + e.getMessage() + "]", e);
        }
    }
    
    /**
     * Queries the class to estimate its ergodic period termination.
     */
    public boolean modifiedSince(long date) {
        return(date < this.configurationFile.lastModified());
    }

    /**
     * Process the given <code>Environment</code> to produce the output.
     */
    public boolean process(Environment environment) 
    throws Exception {
        String file = new URL(environment.resolveEntity(null, this.sitemapFileName).getSystemId()).getFile();
        return this.sitemapManager.invoke(environment, "", file, true);
    }

  /**
   * Sets required system properties .
   */	
    protected void setSystemProperties()
	{
	  java.util.Properties props = new java.util.Properties();

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
