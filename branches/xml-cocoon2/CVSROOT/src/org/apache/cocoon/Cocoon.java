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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.4.2.15 $ $Date: 2000-07-27 21:48:31 $
 */
public class Cocoon
implements Component, Configurable, ComponentManager, Modifiable, 
           Processor {

    /** The table of role-class */
    private Hashtable components=new Hashtable();
    /** The table of role-configuration */
    private Hashtable configurations=new Hashtable();
    /** The configuration file */ 
    private File configurationFile=null; 
    /** The sitemap file */ 
    private String sitemapFileName=null; 
    /** The configuration tree */
    private Configuration configuration=null;
    /** The sitemap manager */
    private SitemapManager sitemapManager=null;
    /** The root uri/path */
    private URL root=null;
        
    /**
     * Create a new <code>Cocoon</code> instance.
     */
    protected Cocoon()
    throws ConfigurationException {
        super();
        // Setup the default parser, for parsing configuration.
        // If one need to use a different parser, set the given system property
        String parser=null;
        parser=System.getProperty("org.apache.cocoon.components.parser.Parser",
                            "org.apache.cocoon.components.parser.XercesParser");
        this.components.put("parser",this.getClass(parser,null));
    }

    /**
     * Create a new <code>Cocoon</code> object, parsing configuration from
     * the specified file.
     */
    public Cocoon(String configuration)
    throws SAXException, IOException, ConfigurationException {
        this();
        this.configurationFile=new File(configuration).getCanonicalFile();
        if (!this.configurationFile.isFile())
            throw new FileNotFoundException(configuration);
        Parser p=(Parser)this.getComponent("parser");
        SAXConfigurationBuilder b=new SAXConfigurationBuilder();
        p.setContentHandler(b);
        String path = this.configurationFile.getPath();
        InputSource is = new InputSource(new FileReader(path));
        is.setSystemId(path);
        p.parse(is);
        this.setConfiguration(b.getConfiguration());
        this.root=this.configurationFile.getParentFile().toURL();
    }

    /**
     * Set the cocoon root.
     * @param root The new Cocoon root.
     */
    public void setRoot(URL root) {
        this.root = root;
    }
 
    /**
     * Get the <code>Component</code> associated with the given role.
     */
    public Component getComponent(String role)
    throws ComponentNotFoundException, ComponentNotAccessibleException {
        if (role==null) throw new ComponentNotFoundException("Null role");
        if (role.equals("cocoon")) return (this);
        Class c=(Class)this.components.get(role);
        if (c==null)
            throw new ComponentNotFoundException("Can't find component "+role);
        try {
            Component comp=(Component)c.newInstance();
            if (comp instanceof Composer)
                ((Composer)comp).setComponentManager(this);
            if (comp instanceof Configurable) {
                Configuration conf=(Configuration)this.configurations.get(role);
                if (conf!=null) ((Configurable)comp).setConfiguration(conf);
            }
            return(comp);
        } catch (Exception e) {
            throw new ComponentNotAccessibleException("Can't access class "+
                        c.getName()+" with role "+role+" due to a "+
                        e.getClass().getName()+"("+e.getMessage()+")",e);
        }
    }

    /**
     * Configure this <code>Cocoon</code> instance.
     */
    public void setConfiguration(Configuration conf)
     throws ConfigurationException {
        this.configuration=conf;
        if (!conf.getName().equals("cocoon"))
            throw new ConfigurationException("Invalid configuration file",conf);
        if (!conf.getAttribute("version").equals("2.0"))
            throw new ConfigurationException("Invalid version",conf);
        // Set components
        Enumeration e=conf.getConfigurations("component");
        while (e.hasMoreElements()) {
            Configuration co=(Configuration)e.nextElement();
            String ro=co.getAttribute("role");
            String cl=co.getAttribute("class");
            System.out.println("Adding component: "+ro);
            this.components.put(ro,this.getClass(cl,co));
            this.configurations.put(ro,co);
        }
        // Create the sitemap
        Configuration sconf=conf.getConfiguration("sitemap");
        if (sconf==null)
            throw new ConfigurationException("No sitemap configuration",conf);
        this.sitemapManager=new SitemapManager();
        this.sitemapManager.setComponentManager(this);
        this.sitemapManager.setConfiguration(conf);
        this.sitemapFileName = sconf.getAttribute("file");
        System.out.println("The root sitemap is located at \""+this.sitemapFileName+"\"");
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     */
    public boolean modifiedSince(long date) {
        return(date<this.configurationFile.lastModified() 
            || sitemapManager.hasChanged());
    }

    /**
     * Process the given <code>Environment</code> producing the output to the
     * specified <code>OutputStream</code>.
     */
    public boolean process(Environment environment, OutputStream out) 
    throws Exception  {
        String s = environment.resolveEntity(this.sitemapFileName).getSystemId();
        if (s.startsWith("file:")) {
            if (s.startsWith("file://")) 
                s = s.substring(7);
            else
                s = s.substring(5);
        }
        return this.sitemapManager.invoke (environment, "",
                          s, true, out);
    }

    /** Get a new class */
    private Class getClass(String className, Configuration conf)
    throws ConfigurationException {
        // This is better than Class.forName() because components should be
        // loaded by the same classloader that loaded Cocoon (they should
        // be in the same jar file, or directory.
        try {
            return(this.getClass().getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cannot load class "+className,
                conf);
        }
    }
}
