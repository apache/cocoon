/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.config;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.AttributeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This utility class will create a <code>Configuration</code> tree from an
 * XML file parsed with a SAX version 1 or 2 parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-06-09 01:04:44 $
 */
public class SAXConfigurationBuilder implements ContentHandler {

    /** The current Locator. */
    private Locator locator=null;
    /** The stack of ConfigurationImpl object. */
    private Vector stack=new Vector();
    /** The table of namespaces prefix-Namespace mapping. */
    private Hashtable namespaces=new Hashtable();
    /** The name of the root configuration. */
    private Configuration root=null;
    
    /**
     * Construct a new <code>SAXConfigurationBuilder</code> instance.
     */
    public SAXConfigurationBuilder() {
        super();
        // Add the default namespace declaration
        Namespace ns=new Namespace();
        ns.uri="";
        ns.previousUri=ns;
        this.namespaces.put("",ns);
    }

    /**
     * Return the parsed configuration tree.
     */
    public Configuration getConfiguration() {
        return(this.root);
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
        this.locator=locator;
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.stack.clear();
        this.stack.addElement(new ConfigurationImpl(""));
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String p, String uri)
    throws SAXException {
        Namespace ns=new Namespace();
        ns.uri=uri;
        ns.previousUri=(Namespace)this.namespaces.get(p);
        this.namespaces.put(p,ns);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String p)
    throws SAXException {
        Namespace ns=(Namespace)this.namespaces.remove(p);
        if (ns==null) throw new SAXException("Prefix '"+p+"' never declared");
        if (ns.previousUri!=null) this.namespaces.put(p,ns.previousUri);
    }

    /** Return an element/attribute raw name. */
    private String resolve(String uri, String loc, String raw)
    throws SAXException {
        if (raw.length()>0) return(raw);
        if (loc.length()==0) throw new SAXException("No local name found");
        Enumeration e=this.namespaces.keys();
        while (e.hasMoreElements()) {
            String prefix=(String)e.nextElement();
            Namespace ns=(Namespace)this.namespaces.get(prefix);
            if (uri.equals(ns.uri)) {
                if (prefix.length()==0) raw=loc;
                else raw=prefix+":"+loc;
                break;
            }
        }
        if (raw.length()==0) throw new SAXException("Cannot get raw name");
        return(raw);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        String name=this.resolve(uri,loc,raw);
        Hashtable t=new Hashtable();
        // Process the attributes
        for (int x=0; x<a.getLength(); x++) {
            String aname=resolve(a.getURI(x),a.getLocalName(x),a.getQName(x));
            t.put(aname,a.getValue(x));
        }
        this.startElement(name,t);
    }

    /** Receive notification of the beginning of an element. */
    private void startElement(String name, Hashtable t)
    throws SAXException {
        ConfigurationImpl conf=null;
        if (this.locator!=null) {
            String file=this.locator.getSystemId();
            int line=this.locator.getLineNumber();
            conf=new ConfigurationImpl(name,file,line);
        } else conf=new ConfigurationImpl(name);
        // Process the attributes
        Enumeration e=t.keys();
        while (e.hasMoreElements()) {
            String anam=(String)e.nextElement();
            String aval=(String)t.get(anam);
            conf.addAttribute(anam,aval);
        }
        // Check if this is the first root configuration element
        if (this.root==null) this.root=conf;
        // Add this element to the stack
        ConfigurationImpl p=(ConfigurationImpl)this.stack.lastElement();
        p.addConfiguration(conf);
        this.stack.addElement(conf);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        // Keep resolution so that we can implement proper element
        // closing later.
        // String res = this.resolve(uri,loc,raw);
        // NOTE: (PF) Should we check for proper element closing????
        this.stack.setSize(this.stack.size()-1);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len) {
        ConfigurationImpl c=(ConfigurationImpl)this.stack.lastElement();
        c.appendValueData(new String(ch,start,len));
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len) {
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data) {
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name) {
    }

    /** A <code>String</code> chain for namespaces declaration */
    private static class Namespace {
        /** The current namespace URI */
        public String uri=null;
        /** The previous namespace URIs */
        public Namespace previousUri=null;
    }
}
