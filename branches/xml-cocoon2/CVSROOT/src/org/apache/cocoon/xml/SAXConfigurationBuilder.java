/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.cocoon.xml;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.apache.avalon.Configuration;
import org.apache.avalon.DefaultConfiguration;

/**
 * This utility class will create a <code>Configuration</code> tree from an
 * XML file parsed with a SAX version 2 parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-17 14:38:46 $
 */
public class SAXConfigurationBuilder implements ContentHandler {

    /** The current Locator. */
    private Locator locator=null;
    /** The stack of DefaultConfiguration object. */
    private Stack stack=new Stack();
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
        Namespace ns = new Namespace();
        ns.uri = "";
        ns.previousUri = ns;
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
        this.root = null;
        this.locator = null;
        this.stack.clear();
        this.namespaces.clear();
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
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        Namespace ns = new Namespace();
        ns.uri = uri;
        ns.previousUri = (Namespace)this.namespaces.get(prefix);
        this.namespaces.put(prefix, ns);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        Namespace ns=(Namespace)this.namespaces.remove(prefix);

        if (ns == null) {
            throw new SAXException("Prefix '" + prefix + "' never declared");
        }

        if (ns.previousUri != null) {
            this.namespaces.put(prefix, ns.previousUri);
        }
    }

    /** Return an element/attribute raw name. */
    private String resolve(String uri, String loc, String raw) throws SAXException {
        String name = raw;
        Enumeration e = this.namespaces.keys();
        String prefix = null;
        Namespace ns = null;

        if (name.length()>0) {
            return(name);
        }

        if (loc.length()==0) {
            throw new SAXException("No local name found");
        }

        while (e.hasMoreElements()) {
            prefix = (String)e.nextElement();
            ns = (Namespace)this.namespaces.get(prefix);
            if (uri.equals(ns.uri)) {
                if (prefix.length() == 0) {
                    name = loc;
                } else {
                    name = new StringBuffer(prefix).append(":").append(loc).toString();
                }

                break;
            }
        }

        if (name.length()==0) {
            throw new SAXException("Cannot get raw name");
        }

        return(name);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        String name=this.resolve(uri,loc,raw);
        DefaultConfiguration conf=null;
        int numAttributes = a.getLength();

        conf = new DefaultConfiguration(name);

        // Process the attributes
        for (int i = 0; i < numAttributes; i++) {
            conf.addAttribute(this.resolve(a.getURI(i), a.getLocalName(i), a.getQName(i)),
                              a.getValue(i));
        }

        // Check if this is the first root configuration element
        if (this.root==null) {
            this.root=conf;
        }

        // Add this element to the stack
        if (!stack.empty()) {
            DefaultConfiguration p=(DefaultConfiguration)this.stack.peek();
            p.addChild(conf);
        }

        stack.push(conf);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        String res = this.resolve(uri,loc,raw);
        String test = ((DefaultConfiguration)this.stack.pop()).getName();

        if (!res.equals(test)) {
            throw new SAXException("Unequal tags");
        }
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char[] ch, int start, int len) {
        DefaultConfiguration c = (DefaultConfiguration)this.stack.peek();
        c.appendValueData(new String(ch,start,len).trim());
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char[] ch, int start, int len) {
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
    private final static class Namespace {
        /** The current namespace URI */
        protected String uri=null;
        /** The previous namespace URIs */
        protected Namespace previousUri=null;
    }
}
