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
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.arch.Component;
import org.apache.arch.ComponentManager;
import org.apache.arch.ComponentNotFoundException;
import org.apache.arch.ComponentNotAccessibleException;
import org.apache.arch.Modifiable;
import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;
import org.apache.arch.config.SAXConfigurationBuilder;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.serializers.Serializer;
import org.apache.cocoon.sitemap.Sitemap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.4.2.2 $ $Date: 2000-02-27 01:25:35 $
 */
public class Cocoon
implements Component, Configurable, ComponentManager, Modifiable, Processor {

    /** The table of role-class */
    private Hashtable components=new Hashtable();
    /** The table of role-configuration */
    private Hashtable configurations=new Hashtable();
    /** The configuration file */
    private File configurationFile=null;
    /** The configuration tree */
    private Configuration configuration=null;
    /** The sitemap */
    private Sitemap sitemap=null;

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
        p.parse(new InputSource(this.configurationFile.getName()));
        this.setConfiguration(b.getConfiguration());
    }

    /**
     * Get the <code>Component</code> associated with the given role.
     */
    public Component getComponent(String role)
    throws ComponentNotFoundException, ComponentNotAccessibleException {
        if (role==null) throw new ComponentNotFoundException("Null role");
        if (role.equals("cocoon")) return(this);
        Class c=(Class)this.components.get(role);
        if (c==null) throw new ComponentNotFoundException("Can't find "+role);
        try {
            Component comp=(Component)c.newInstance();
            if (comp instanceof Configurable) {
                Configuration conf=(Configuration)this.configurations.get(role);
                if (conf!=null) ((Configurable)comp).setConfiguration(conf);
            }
            return(comp);
        } catch (Exception e) {
            throw new ComponentNotAccessibleException("Can't access "+role, e);
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
        // Set generators, filters and serializers
        String buf[]={"generator","filter","serializer"};
        for (int x=0; x<buf.length; x++) {
            Enumeration e=conf.getConfigurations(buf[x]);
            while (e.hasMoreElements()) {
                Configuration co=(Configuration)e.nextElement();
                String na=co.getAttribute("name");
                String cl=co.getAttribute("class");
                String ro=buf[x]+":"+na;
                System.out.println("Adding component: "+ro);
                this.components.put(ro,this.getClass(cl,co));
                this.configurations.put(ro,co);
            }
        }
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
        this.sitemap=new Sitemap();
        this.sitemap.setComponentManager(this);
        this.sitemap.setConfiguration(sconf);
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     */
    public boolean modifiedSince(long date) {
        return(date<this.configurationFile.lastModified());
    }

    /**
     * Process the given <code>Request</code> producing the output to the
     * specified <code>Response</code> and <code>OutputStream</code>.
     */
    public boolean process(Request req, Response res, OutputStream out)
    throws SAXException, IOException, ProcessingException {
        return(this.sitemap.process(req,res,out));
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

    public static void main(String argv[]) {
        try {
            Cocoon c=new Cocoon(argv[0]);
            Parser p=(Parser)c.getComponent("parser");
            Serializer s=(Serializer)c.getComponent("serializer");
            s.setOutputStream(System.out);
            p.setConsumer(s);
            p.parse(new InputSource(argv[0]));
        } catch (ConfigurationException e) {
            System.out.print(e.getClass().getName()+":\n  ");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.print(e.getClass().getName()+":\n  ");
            e.printStackTrace(System.out);
        } catch (SAXException e) {
            System.out.print(e.getClass().getName()+":\n  ");
            Exception e2=e.getException();
            if (e2==null) e.printStackTrace(System.out);
            else {
                System.out.print(e2.getClass().getName()+":\n  ");
                e2.printStackTrace(System.out);
            }
        }
    }
}
