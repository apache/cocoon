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
import java.util.Enumeration;
import java.util.Hashtable;

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
import org.apache.cocoon.sitemap.SitemapManager;
import org.apache.cocoon.util.ClassUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.4.2.21 $ $Date: 2000-08-31 15:58:54 $
 */
public class Cocoon
  implements Component, Configurable, ComponentManager, Modifiable, Processor {

    /* Cocoon Default Strings */
    public static final String NAME               = "@name@";
    public static final String VERSION            = "@version@";
    public static final String CONF_VERSION       = "2.0";
    public static final String YEAR               = "@year@";
    public static final String RELOAD_PARAM       = "cocoon-reload";
    public static final String SHOWTIME_PARAM     = "cocoon-showtime";
    public static final String VIEW_PARAM         = "cocoon-view";
    public static final String TEMPDIR_PROPERTY   = "org.apache.cocoon.properties.tempdir";
    public static final String DEFAULT_CONF_FILE  = "cocoon.xconf";
    public static final String DEFAULT_DEST_DIR   = "./site";
    public static final String DEFAULT_TEMP_DIR   = "./work";
    public static final String LINK_CONTENT_TYPE  = "x-application/x-cocoon-links";
    public static final String LINK_VIEW          = "links";
    public static final String LINK_CRAWLING_ROLE = "static";
    public static final String PARSER_PROPERTY    = "org.apache.cocoon.components.parser.Parser";
    public static final String DEFAULT_PARSER     = "org.apache.cocoon.components.parser.XercesParser";
    
    /** The table of role-class */
    private Hashtable components = new Hashtable();
    
    /** The table of role-configuration */
    private Hashtable configurations = new Hashtable();
    
    /** The configuration file */ 
    private File configurationFile = null; 
    
    /** The sitemap file */ 
    private String sitemapFileName = null; 
    
    /** The configuration tree */
    private Configuration configuration = null;
    
    /** The sitemap manager */
    private SitemapManager sitemapManager = null;
    
    /** The root uri/path */
    private URL root = null;
        
    /**
     * Create a new <code>Cocoon</code> instance.
     */
    protected Cocoon() throws ConfigurationException {
        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        String parser = System.getProperty(PARSER_PROPERTY, DEFAULT_PARSER);
        try {
            this.components.put("parser", ClassUtils.getClass(parser));
        } catch (Exception e) {
            throw new ConfigurationException("Error creating parser (" + parser + ")", null);
        }
    }
    
    /**
     * Create a new <code>Cocoon</code> object, parsing configuration from
     * the specified file.
     */
    public Cocoon(String configurationFile)
    throws SAXException, IOException, ConfigurationException {
        this(new File(configurationFile).getCanonicalFile());
    }
    
    /**
     * Create a new <code>Cocoon</code> object, parsing configuration from
     * the specified file.
     */
    public Cocoon(File configurationFile)
    throws SAXException, IOException, ConfigurationException {
        this();
        
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
                this.components.put(role, ClassUtils.getClass(className));
                this.configurations.put(role, co);
            } catch (Exception ex) {
                throw new ConfigurationException("Cannot load class " + className + ": " + ex.getMessage(), co);
            }
        }

        // Create the sitemap
        Configuration sconf = conf.getConfiguration("sitemap");
        if (sconf == null) {
            throw new ConfigurationException("No sitemap configuration", conf);
        }
        this.sitemapManager = new SitemapManager();
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
}
