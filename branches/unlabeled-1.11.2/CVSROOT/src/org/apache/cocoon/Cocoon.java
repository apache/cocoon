/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.framework.Modificable;
import org.apache.cocoon.parsers.ParserFactory;
import org.apache.cocoon.dom.DocumentFactory;
import org.apache.cocoon.dom.TreeGenerator;
import org.apache.cocoon.producers.Producer;
import org.apache.cocoon.producers.ProducerFactory;
import org.apache.cocoon.filters.Filter;
import org.apache.cocoon.filters.FilterFactory;
import org.apache.cocoon.serializers.Serializer;
import org.apache.cocoon.serializers.SerializerFactory;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.cocoon.sitemap.SitemapFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The main <b>Cocoon 2.0</b> class.
 * <br>
 * After instantiation, the first method to call is <code>configure(...)</code>
 * wich, deriving parameters from a <code>Configurations</code> object, creates
 * all producer, filter and serializer factories.
 * <br>
 * This class implements the <code>Modificable</code> interface, and the
 * <code>isModifiedSince(...)</code> method will return true if the
 * configuration file changed since the date specified.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>,
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.11.2.1 $ $Date: 2000-02-07 15:35:34 $
 * @since Cocoon 2.0
 */
public class Cocoon implements Configurable, Modificable {
    /** The configuration File */
    private File configurationFile=null;
    /** The configuration File */
    private Configurations configurations=null;
    /** The current DocumentFactory */
    private DocumentFactory documentFactory=null;
    /** The current ParserFactory */
    private ParserFactory parserFactory=null;
    /** The ProducerFactory table */
    private Hashtable producers=null;
    /** The FilterFactory table */
    private Hashtable filters=null;
    /** The SerializerFactory table */
    private Hashtable serializers=null;
    /** The Sitemap */
    private Sitemap sitemap=null;

    /**
     * Instantiate a new Cocoon object.
     */
    public Cocoon() {
        super();
        this.producers=new Hashtable();
        this.filters=new Hashtable();
        this.serializers=new Hashtable();
    }

    /**
     * Check if the configuration file was modified.
     */
    public boolean modifiedSince(long since) {
        long modified=this.configurationFile.lastModified();
        if (modified<=since) return(false);
        else return(true);
    }

    /**
     * Configure this Cocoon instance.
     * <br>
     * Valid configuration parameters are:
     * <ul>
     *   <li><b>configurationFile</b> <i>(string)</i> The uri of the Cocoon
     *       XML configuration file <i>(default=no default)</i>.
     *   <li><b>rootPath</b> <i>(string)</i> The root path for Cocoon operation
     *       <i>(default=directory or configurationFile)</i>.
     *   <li><b>defaultParserFactory</b> <i>(string)</i> The full class name
     *       of the default ParserFactory.
     *      <i>(default=org.apache.cocoon.parsers.XercesFactory)</i>.
     *   <li><b>defaultDocumentFactory</b> <i>(string)</i> The full class name
     *       of the default DocumentFactory.
     *      <i>(default=org.apache.cocoon.parsers.XercesFactory)</i>.
     * </ul>
     * Those and all other specified parameters are merged with those specified
     * at root level in the configuration file and passed to all factories.
     */
    public void configure(Configurations conf)
    throws ConfigurationException {
        //////////////////////////////////////////////////////////////////////
        // Check the supplied configuration file parameter
        String c=conf.getParameter("configurationFile");
        if (c==null) throw this.newException("Configuration file unspecified");

        // Check if the configuration file can be accessed and is a file
        try {
            this.configurationFile=new File(c).getCanonicalFile();
            c=this.configurationFile.getPath();
        } catch (IOException e) {
            throw this.newException("Cannot access config file '"+c+"'",e);
        }
        if (!this.configurationFile.isFile())
            throw this.newException("Configuration file '"+c+"' doesnt exist");

        // In case the rootPath parameter was not specifed set it
        if (conf.getParameter("rootPath")==null)
            conf.setParameter("rootPath",this.configurationFile.getParent());

        //////////////////////////////////////////////////////////////////////
        // Check if the defaultParserFactory parameter was specified
        String d=conf.getParameter("defaultDocumentFactory",
                                   "org.apache.cocoon.parsers.XercesFactory");
        try {
            this.documentFactory=(DocumentFactory)this.getClassInstance(d);
        } catch (ClassCastException e) {
            throw this.newException("Class '"+d+"' doesn't implement "+
                                    "'org.apache.cocoon.dom.DocumentFactory'");
        }

        //////////////////////////////////////////////////////////////////////
        // Check if the defaultParserFactory property was specified
        String p=conf.getParameter("defaultParserFactory",
                                   "org.apache.cocoon.parsers.XercesFactory");
        try {
            this.parserFactory=(ParserFactory)this.getClassInstance(p);
        } catch (ClassCastException e) {
            throw this.newException("Class '"+p+"' doesn't implement "+
                                    "'org.apache.cocoon.parser.ParserFactory'");
        }

        //////////////////////////////////////////////////////////////////////
        // Load configuration file
        TreeGenerator tg=new TreeGenerator(this.documentFactory);
        try {
            this.parserFactory.getXMLProducer(new InputSource(c)).produce(tg);
        } catch (IOException e) {
            throw this.newException("IOException catched parsing '"+c+"'", e);
        } catch (SAXException e) {
            throw this.newException("SAXException catched parsing '"+c+"'", e);
        }

        //////////////////////////////////////////////////////////////////////
        // Do a prelimiar analisys of the configuration document
        Document doc=tg.document();
        Element elem=doc.getDocumentElement();
        if ((!elem.getTagName().equals("cocoon"))||
            (!elem.getAttribute("version").equals("2.0")))
            throw this.newException("Configuration file '"+c+"' does not "+
                                    "start with <cocoon version=\"2.0\">");
        // Setup preliminary configuration parameter found in document
        NodeList l=elem.getChildNodes();
        this.configurations=conf.merge(Configurations.createFromNodeList(l));

        //////////////////////////////////////////////////////////////////////
        // Setup the factories and the sitemap
        boolean done=false;
        for (int x=0; x<l.getLength(); x++) {
            if (l.item(x).getNodeType()!=Node.ELEMENT_NODE) continue;
            Element e=(Element)l.item(x);
            if (e.getTagName().equals("sitemap"))
                this.sitemap=new SitemapFactory(this,conf).build(e);
            if (!e.getTagName().equals("configuration")) continue;
            if (done)
                throw this.newException("Multiple <configuration> tags found "+
                                        "in configuration file '"+c+"'");
            this.setupFactories(e);
            done=true;
        }

    }

    /**
     * Get the configured DocumentFactory.
     */
    public DocumentFactory getDocumentFactory() {
        return(this.documentFactory);
    }

    /**
     * Get the configured ParserFactory.
     */
    public ParserFactory getParserFactory() {
        return(this.parserFactory);
    }

    /**
     * Get the instance of a Producer specified by its name.
     */
    public Producer getProducer(String name)
    throws ConfigurationException {
        ProducerFactory f=(ProducerFactory)this.producers.get(name);
        if(f==null) return(null);
        Producer p=f.getProducer();
        p.setCocoonInstance(this);        
        return(p);
    }

    /**
     * Get the instance of a Filter specified by its name.
     */
    public Filter getFilter(String name)
    throws ConfigurationException {
        FilterFactory f=(FilterFactory)this.filters.get(name);
        if(f==null) return(null);
        Filter x=f.getFilter();
        x.setCocoonInstance(this);        
        return(x);
    }

    /**
     * Get the instance of a Serializer specified by its name.
     */
    public Serializer getSerializer(String name)
    throws ConfigurationException {
        SerializerFactory f=(SerializerFactory)this.serializers.get(name);
        if(f==null) return(null);
        Serializer s=f.getSerializer();
        s.setCocoonInstance(this);        
        return(s);
    }

    public boolean handle(Job job, OutputStream out)
    throws IOException, SAXException {
        return(this.sitemap.handle(job,out));
    }

    /** Create a new instance for a specified class */
    private Object getClassInstance(String c)
    throws ConfigurationException {
        try {
            return(Class.forName(c).newInstance());
        } catch (ClassNotFoundException e) {
            throw this.newException("Can't find class '"+c+"'");
        } catch (IllegalAccessException e) {
            throw this.newException("Can't access class '"+c+"'");
        } catch (InstantiationException e) {
            throw this.newException("Can't instantiate class '"+c+"'");
        } catch (Exception e) {
            throw this.newException("Error trying to load and/or instantiate '"+
                                    c+"'");
        }
    }

    /** Load a factory instance and configures it */
    private Configurable configureFactory(String c, Configurations conf)
    throws ConfigurationException {
        try {
            Configurable instance=(Configurable)this.getClassInstance(c);
            instance.configure(conf);
            return(instance);
        } catch (ClassCastException e) {
            throw this.newException("Class '"+c+"' doesn't implement "+
                                    "'org.apache.cocoon.Configurable'");
        }
    }

    /** Setup factories from a DOM Element */
    private void setupFactories(Element elem)
    throws ConfigurationException {
        NodeList list=elem.getChildNodes();
        for (int x=0; x<list.getLength(); x++) {
            // Retrieve the element child of <configuration>
            if (list.item(x).getNodeType()!=Node.ELEMENT_NODE) continue;
            Element e=(Element)list.item(x);
            // Get the name and the factory class
            String type=e.getTagName();
            String name=e.getAttribute("name");
            String f=e.getAttribute("class");
            // Prepare configurations
            NodeList children=e.getChildNodes();
            Configurations conf=Configurations.createFromNodeList(children);
            conf.merge(this.configurations);
            // Get the Configurable already configured
            Configurable instance=this.configureFactory(f,conf);

            ///////////////////////////////////////////////////////////////////
            // Check if we were specified to instantiate a producer factory
            if (type.equals("producerFactory")) try {
                if (name==null)
                    throw this.newException("No name specified for "+
                                            "producerFactory '"+f+"'");
                this.producers.put(name,(ProducerFactory)instance);
            } catch (ClassCastException ex) {
                throw this.newException("Factory '"+f+"' does not implement "+
                              "'org.apache.cocoon.producers.ProducerFactory'");

            ///////////////////////////////////////////////////////////////////
            // Check if we were specified to instantiate a filter factory
            } else if (type.equals("filterFactory")) try {
                if (name==null)
                    throw this.newException("No name specified for "+
                                            "filterFactory '"+f+"'");
                this.filters.put(name,(FilterFactory)instance);
            } catch (ClassCastException ex) {
                throw this.newException("Factory '"+f+"' does not implement "+
                                "'org.apache.cocoon.producers.FilterFactory'");

            ///////////////////////////////////////////////////////////////////
            // Check if we were specified to instantiate a serializer factory
            } else if (type.equals("serializerFactory")) try {
                if (name==null)
                    throw this.newException("No name specified for "+
                                            "serializerFactory '"+f+"'");
                this.serializers.put(name,(SerializerFactory)instance);
            } catch (ClassCastException ex) {
                throw this.newException("Factory '"+f+"' does not implement "+
                            "'org.apache.cocoon.producers.SerializerFactory'");

            ///////////////////////////////////////////////////////////////////
            // Check if we were specified to instantiate a parser factory
            } else if (type.equals("parserFactory")) try {
                this.parserFactory=(ParserFactory)instance;
            } catch (ClassCastException ex) {
                throw this.newException("Factory '"+f+"' does not implement "+
                                  "'org.apache.cocoon.parsers.ParserFactory'");

            ///////////////////////////////////////////////////////////////////
            // Check if we were specified to instantiate a document factory
            } else if (type.equals("documentFactory")) try {
                this.documentFactory=(DocumentFactory)instance;
            } catch (ClassCastException ex) {
                throw this.newException("Factory '"+f+"' does not implement "+
                                    "'org.apache.cocoon.dom.DocumentFactory'");
            }
        }
    }

    /** Create a ConfigurationException */
    private ConfigurationException newException(String msg) {
        return(this.newException(msg,null));
    }

    /** Create a ConfigurationException */
    private ConfigurationException newException(String msg, Exception exc) {
        return(new ConfigurationException(msg,exc,this.getClass()));
    }
}
