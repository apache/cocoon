/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ContentHandler;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.apache.avalon.Parameters;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.xpath.XPathAPI;

import org.apache.log.LogKit;
import org.apache.log.Logger;

import javax.xml.transform.TransformerException;

/**
 * My first pass at an XInclude transformation. Currently it should set the base URI
 * from the SAX Locator's system id but allow it to be overridden by xml:base
 * elements as the XInclude spec mandates. It's also got some untested code
 * that should handle inclusion of text includes, but that method isn't called
 * by the SAX event FSM yet.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2000-11-14 21:52:05 $ $Author: dims $
 */
public class XIncludeTransformer extends AbstractTransformer implements Composer {

    protected Logger log = LogKit.getLoggerFor("cocoon");

    protected ComponentManager manager = null;

    public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLBASE_ATTRIBUTE = "base";

    public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/1999/XML/xinclude";
    public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
    public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";

    protected URL base_xmlbase_uri = null;

    /** The current XMLBase URI. We start with an empty "dummy" URL. **/
    protected URL current_xmlbase_uri = null;

    /** This is a stack of xml:base attributes which belong to our ancestors **/
    protected Stack xmlbase_stack = new Stack();

    /** namespace uri of the last element which had an xml:base attribute **/
    protected String last_xmlbase_element_uri = "";

    protected Stack xmlbase_element_uri_stack = new Stack();

    /** name of the last element which had an xml:base attribute **/
    protected String last_xmlbase_element_name = "";

    protected Stack xmlbase_element_name_stack = new Stack();

    public void setup(EntityResolver resolver, Map objectModel,
                      String source, Parameters parameters)
            throws ProcessingException, SAXException, IOException {}
    /*
        try {
            log.debug("SOURCE: "+source);
            base_xmlbase_uri = new URL(source);
            log.debug("SOURCE URI: "+base_xmlbase_uri.toString());
        } catch (MalformedURLException e) {
            throw new ProcessingException(e.getMessage());
        }
    }
    */

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        String value;
        if ((value = attr.getValue(XMLBASE_NAMESPACE_URI,XMLBASE_ATTRIBUTE)) != null) {
            try {
                startXMLBaseAttribute(uri,name,value);
            } catch (MalformedURLException e) {
                throw new SAXException(e);
            }
        }
        if (uri != null && name != null && uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
            String href = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
            String parse = attr.getValue("",XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
            try {
                processXIncludeElement(href, parse);
            } catch (MalformedURLException e) {
                throw new SAXException(e);
            } catch (IOException e) {
                throw new SAXException(e);
            }
            return;
        }
        super.startElement(uri,name,raw,attr);
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (last_xmlbase_element_uri.equals(uri) && last_xmlbase_element_name.equals(name)) {
            endXMLBaseAttribute();
        }
        if (uri != null && name != null && uri.equals(XINCLUDE_NAMESPACE_URI) && name.equals(XINCLUDE_INCLUDE_ELEMENT)) {
            return;
        }
        super.endElement(uri,name,raw);
    }

    public void setDocumentLocator(Locator locator) {
        try {
            base_xmlbase_uri = new URL(locator.getSystemId());
            if (current_xmlbase_uri == null) {
                current_xmlbase_uri = base_xmlbase_uri;
            }
        } catch (MalformedURLException e) {}
        super.setDocumentLocator(locator);
    }

    protected void startXMLBaseAttribute(String uri, String name, String value) throws MalformedURLException {
        if (current_xmlbase_uri != null) {
            xmlbase_stack.push(current_xmlbase_uri);
        }
        current_xmlbase_uri = new URL(value);

        xmlbase_element_uri_stack.push(last_xmlbase_element_uri);
        last_xmlbase_element_uri = uri;

        xmlbase_element_name_stack.push(last_xmlbase_element_name);
        last_xmlbase_element_name = name;
    }

    protected void endXMLBaseAttribute() {
        if (xmlbase_stack.size() > 0) {
            current_xmlbase_uri = (URL)xmlbase_stack.pop();
        } else {
            current_xmlbase_uri = base_xmlbase_uri;
        }
        last_xmlbase_element_uri = (String)xmlbase_element_uri_stack.pop();
        last_xmlbase_element_name = (String)xmlbase_element_name_stack.pop();
    }

    protected void processXIncludeElement(String href, String parse) throws SAXException,MalformedURLException,IOException {
        log.debug("Processing XInclude element: href="+href+", parse="+parse);
        URL url;
        String suffix;
        int index = href.indexOf('#');
        if (index < 0) {
            url = new URL(current_xmlbase_uri,href);
            suffix = "";
        } else {
            url = new URL(current_xmlbase_uri,href.substring(0,index));
            suffix = href.substring(index+1);
        }
        log.debug("URL: "+url+"\nSuffix: "+suffix);
        Object object = url.getContent();
        log.debug("Object: "+object);
        if (parse.equals("text")) {
            log.debug("Parse type is text");
            if (object instanceof Reader) {
                Reader reader = (Reader)object;
                int read;
                char ary[] = new char[1024];
                if (reader != null) {
                    while ((read = reader.read(ary)) != -1) {
                        super.characters(ary,0,read);
                    }
                    reader.close();
                }
            } else if (object instanceof InputStream) {
                InputStream input = (InputStream)object;
                InputStreamReader reader = new InputStreamReader(input);
                int read;
                char ary[] = new char[1024];
                if (reader != null) {
                    while ((read = reader.read(ary)) != -1) {
                        super.characters(ary,0,read);
                    }
                    reader.close();
                }
            }
        } else if (parse.equals("xml")) {
            log.debug("Parse type is XML");
	        Parser parser = null;
	        try {
	            log.debug("Looking up " + Roles.PARSER);
                parser = (Parser)manager.lookup(Roles.PARSER);
	        } catch (Exception e) {
	            log.error("Could not find component", e);
    		    return;
	        }
            InputSource input;
            if (object instanceof Reader) {
                input = new InputSource((Reader)object);
            } else if (object instanceof InputStream) {
                input = new InputSource((InputStream)object);
            } else {
                throw new SAXException("Unknown object type: "+object);
            }
            if (suffix.startsWith("xpointer(") && suffix.endsWith(")")) {
                String xpath = suffix.substring(9,suffix.length()-1);
                log.debug("XPath is "+xpath);
                DOMBuilder builder = new DOMBuilder(parser);
                parser.setContentHandler(builder);
                parser.setLexicalHandler(builder);
                parser.parse(input);
                Document document = builder.getDocument();
                try {
                    NodeList list = XPathAPI.selectNodeList(document,xpath);
                    DOMStreamer streamer = new DOMStreamer(super.contentHandler,super.lexicalHandler);
                    int length = list.getLength();
                    for (int i=0; i<length; i++) {
                        streamer.stream(list.item(i));
                    }
                } catch (TransformerException e){
	                log.error("TransformerException", e);
    		        return;
                }
            } else {
                XIncludeContentHandler xinclude_handler = new XIncludeContentHandler(super.contentHandler,super.lexicalHandler);
                parser.setContentHandler(xinclude_handler);
                parser.setLexicalHandler(xinclude_handler);
                parser.parse(input);
            }
        }
    }

    class XIncludeContentHandler implements ContentHandler, LexicalHandler {

        ContentHandler content_handler;
        LexicalHandler lexical_handler;
        boolean debug;

        XIncludeContentHandler(ContentHandler content_handler, LexicalHandler lexical_handler) {
            this.content_handler = content_handler;
            this.lexical_handler = lexical_handler;
        }

        public void setDocumentLocator(Locator locator) {
            content_handler.setDocumentLocator(locator);
        }

        public void startDocument() {
            log.debug("Internal start document received");
            /** We don't pass start document on to the "real" handler **/
        }

        public void endDocument() {
            log.debug("Internal end document received");
            /** We don't pass end document on to the "real" handler **/
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
            content_handler.startPrefixMapping(prefix,uri);
        }

        public void endPrefixMapping(String prefix)
            throws SAXException {
            content_handler.endPrefixMapping(prefix);
        }

        public void startElement(String namespace, String name, String raw,
            Attributes attr) throws SAXException {
            log.debug("Internal element received: "+name);
            content_handler.startElement(namespace,name,raw,attr);
        }

        public void endElement(String namespace, String name, String raw)
            throws SAXException {
            content_handler.endElement(namespace,name,raw);
        }

        public void characters(char ary[], int start, int length)
            throws SAXException {
            content_handler.characters(ary,start,length);
        }

        public void ignorableWhitespace(char ary[], int start, int length)
            throws SAXException {
            content_handler.ignorableWhitespace(ary,start,length);
        }

        public void processingInstruction(String target, String data)
            throws SAXException {
            content_handler.processingInstruction(target,data);
        }

        public void skippedEntity(String name)
            throws SAXException {
            content_handler.skippedEntity(name);
        }

        public void startDTD(String name, String public_id, String system_id)
            throws SAXException {
            lexical_handler.startDTD(name,public_id,system_id);
        }

        public void endDTD() throws SAXException {
            lexical_handler.endDTD();
        }

        public void startEntity(String name) throws SAXException {
            lexical_handler.startEntity(name);
        }

        public void endEntity(String name) throws SAXException {
            lexical_handler.endEntity(name);
        }

        public void startCDATA() throws SAXException {
            lexical_handler.startCDATA();
        }

        public void endCDATA() throws SAXException {
            lexical_handler.endCDATA();
        }

        public void comment(char ary[], int start, int length)
            throws SAXException {
            lexical_handler.comment(ary,start,length);
        }
    }
}
