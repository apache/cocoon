/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.filters.Filter;
import org.apache.cocoon.producers.Producer;
import org.apache.cocoon.serializers.Serializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class to generate Sitemaps from DOM.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:43 $
 */
public class SitemapFactory {
    /** The current cocoon instance */
    private Cocoon cocoon=null;
    /** The basic configurations */
    private Configurations config=null;
    /** Deny empty construction */
    private SitemapFactory() {}

    /**
     * Construct a SitemapFactory.
     */
    public SitemapFactory(Cocoon cocoon, Configurations config)
    throws ConfigurationException {
        super();
        // Check up some basic parameters.
        if (cocoon==null) throw new ConfigurationException("Null Cocoon");
        this.cocoon=cocoon;
        if (config==null) this.config=new Configurations();
        else this.config=config;
    }

    /**
     * Build a new Sitemap from a DOM Element.
     */
    public Sitemap build(Element elem)
    throws ConfigurationException {
        // Check the configuration element
        if (elem==null)
            throw new ConfigurationException("Null Sitemap element");
        if (!elem.getTagName().equals("sitemap"))
            throw new ConfigurationException("Sitemap element is not <sitemap>");
        
        // Build a new Sitemap
        Sitemap smap=new Sitemap();

        // Build Sitemap partitions
        NodeList list=elem.getChildNodes();
        for (int x=0; x<list.getLength(); x++) {
            Node n=list.item(x);
            Element e=n.getNodeType()==Node.ELEMENT_NODE ? (Element)n : null ;
            if (e==null) continue;
            if (e.getTagName().equals("partition")) buildPartition(smap,e);
        }
        
        // Check if the default partition has been defined
        if (smap.defaultPartition==null)
            throw new ConfigurationException("The default Sitemap partition "+
                                             "has not been defined");
        return(smap);
    }

    /** Build a Partition and set it in the Sitemap */
    private void buildPartition(Sitemap smap, Element e)
    throws ConfigurationException {
        String name=e.getAttribute("name");
        Partition part=new Partition(smap,name);
        if ((name==null)||(name.length()==0)) {
            if (smap.defaultPartition!=null)
                throw new ConfigurationException("Default Partition already "+
                                                 "defined for this Sitemap");
            smap.defaultPartition=part;
        } else if (smap.partitions.put(name,part)!=null) 
            throw new ConfigurationException("Partition "+name+" already "+
                                             "defined for this Sitemap");
        buildProcessors(part,e.getChildNodes());
    }

    /** Build a Processor array and set it in the Partition */
    private void buildProcessors(Partition part, NodeList l)
    throws ConfigurationException {
        // Count the number of <process> in this NodeList
        int num=0;
        for (int x=0; x<l.getLength(); x++) {
            Node n=l.item(x);
            Element e=n.getNodeType()==Node.ELEMENT_NODE ? (Element)n : null ;
            if (e==null) continue;
            if (e.getTagName().equals("process")) num++;
        }

        // Create a Processor array of the right size
        part.processors=new Processor[num];
        num=0;
        for (int x=0; x<l.getLength(); x++) {
            Node n=l.item(x);
            Element e=n.getNodeType()==Node.ELEMENT_NODE ? (Element)n : null ;
            if (e==null) continue;
            if (!e.getTagName().equals("process")) continue;
            // Create the new processor
            String match=e.getAttribute("match");
            String source=e.getAttribute("source");
            part.processors[num]=new Processor(part, match, source);
            NodeList children=e.getChildNodes();

            // Process child nodes of this <process> element
            for (int k=0; k<children.getLength(); k++) {
                Node w=children.item(k);
                Element q=w.getNodeType()==Node.ELEMENT_NODE? (Element)w: null;
                if (q!=null) {
                    String t=q.getTagName();
                    // Set the producer for this processor
                    if (t.equals("producer"))
                        buildProducer(part.processors[num],q);
                    // Set the producer for this processor
                    if (t.equals("serializer"))
                        buildSerializer(part.processors[num],q);
                }
            }
            // Set the filters list for this processor (at least zero)
            buildFilters(part.processors[num],children);

            // Check if the processor was correctly created
            if(part.processors[num].producer==null)
                throw new ConfigurationException("No Producer specified");
            if(part.processors[num].serializer==null)
                throw new ConfigurationException("No Serializer specified");
            num++;
        }
    }

    /** Build a Producer and set it in the Processor */
    private void buildProducer(Processor proc, Element e)
    throws ConfigurationException {
        if (proc.producer!=null)
            throw new ConfigurationException("Multiple Producer specified");
        String name=e.getAttribute("name");
        if (name==null)
            throw new ConfigurationException("No name specified for Producer");
        proc.producer=this.cocoon.getProducer(name);
        if (proc.producer==null)
            throw new ConfigurationException("Cannot find Producer "+name);
        Configurations c=Configurations.createFromNodeList(e.getChildNodes());
        c.merge(this.config);
        proc.producer.configure(c);
    }

    /** Build a Filter array and set it in the Processor */
    private void buildFilters(Processor proc, NodeList l)
    throws ConfigurationException {
        // Count the number of filter in this NodeList
        int count=0;
        for (int x=0; x<l.getLength(); x++) {
            Node n=l.item(x);
            Element e=n.getNodeType()==Node.ELEMENT_NODE ? (Element)n : null ;
            if (e==null) continue;
            if (e.getTagName().equals("filter")) count++;
        }
        // Prepare the filter array
        proc.filters=new Filter[count];
        count=0;
        for (int x=0; x<l.getLength(); x++) {
            Node n=l.item(x);
            Element e=n.getNodeType()==Node.ELEMENT_NODE ? (Element)n : null ;
            if (e==null) continue;
            if (!e.getTagName().equals("filter")) continue;
            // Get the filter name
            String name=e.getAttribute("name");
            if (name==null)
                throw new ConfigurationException("No name specified for Filter");
            // Retrieve the filter instance
            proc.filters[count]=this.cocoon.getFilter(name);
            if (proc.filters[count]==null)
                throw new ConfigurationException("Cannot find Filter "+name);
            // Create configurations and configure the filter
            Configurations c=Configurations.createFromNodeList(e.getChildNodes());
            c.merge(this.config);
            proc.filters[count].configure(c);
            count++;
        }
    }

    /** Build a Serializer and set it in the Processor */
    private void buildSerializer(Processor proc, Element e)
    throws ConfigurationException {
        if (proc.serializer!=null)
            throw new ConfigurationException("Multiple Serializer specified");
        String name=e.getAttribute("name");
        if (name==null)
            throw new ConfigurationException("No name specified for Serializer");
        proc.serializer=this.cocoon.getSerializer(name);
        if (proc.serializer==null)
            throw new ConfigurationException("Cannot find Serializer "+name);
        Configurations c=Configurations.createFromNodeList(e.getChildNodes());
        c.merge(this.config);
        proc.serializer.configure(c);
    }
}
