/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.processor.xinclude;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.util.Stack;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.apache.cocoon.parser.Parser;
import org.apache.cocoon.logger.Logger;
import org.apache.cocoon.xml.util.XPathAPI;
import org.apache.cocoon.framework.Status;
import org.apache.cocoon.framework.AbstractActor;
import org.apache.cocoon.framework.Director;
import org.apache.cocoon.processor.Processor;
import org.apache.cocoon.Utils;

/**
 * First version of a DOM XInclude parser for cocoon. This has been back ported
 * from my XInclude filter for cocoon2.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision: 1.3 $ $Date: 2000-05-09 05:49:49 $ $Author: balld $
 */
public class XIncludeProcessor extends AbstractActor implements Processor, Status {

	protected boolean debug = true;

	public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
	public static final String XMLBASE_ATTRIBUTE = "base";

	public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/1999/XML/xinclude";
	public static final String XINCLUDE_INCLUDE_ELEMENT = "include";
	public static final String XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE = "href";
	public static final String XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE = "parse";

	protected Parser parser;
	protected Logger logger;
	protected Object context;

	public void init(Director director) {
		super.init(director);
		parser = (Parser)director.getActor("parser");
		logger = (Logger)director.getActor("logger");
		context = director.getActor("context");
	}

	public Document process(Document document, Dictionary parameters) throws Exception {
		XIncludeProcessorWorker worker = new XIncludeProcessorWorker(this,document,parameters);
		worker.process();
		return worker.document;
	}

	public String getStatus() {
		return "XInclude Processor";
	}

	/** FIXME - this is obviously the wrong thing to do **/
	public boolean hasChanged(Object object) {
		return true;
	}

class XIncludeProcessorWorker {

	boolean debug;

	XIncludeProcessor processor;

	Document document;

	File base_file = null;
	
	/** The current XMLBase URI. We start with an empty "dummy" URL. **/
	URL current_xmlbase_uri = null;

	/** This is a stack of xml:base attributes which belong to our ancestors **/
	Stack xmlbase_stack = new Stack();

	Hashtable namespace_table = new Hashtable();

	XIncludeProcessorWorker(XIncludeProcessor processor, Document document, Dictionary parameters) throws Exception {
		this.processor = processor;
		debug = processor.debug;
		this.document = document;
		HttpServletRequest request = (HttpServletRequest)parameters.get("request");
		String basename = Utils.getBasename(request,context);
		if (debug) {
			System.err.println("basename: "+basename);
		}
		base_file = new File((new File(basename)).getParent());
	}

	void process() throws Exception {
		Element element = document.getDocumentElement();
		/** FIXME - why doesn't Xerces let us use node.getNamespaceURI()??? **/
		NamedNodeMap attributes = element.getAttributes();
		int length = attributes.getLength();
		for (int i=0; i<length; i++) {
			Attr attr = (Attr)attributes.item(i);
			String name = attr.getName();
			if (name.length() >= 6 && name.substring(0,6).equals("xmlns:")) {
				String prefix = name.substring(6);
				String uri = attr.getValue();
				namespace_table.put(prefix,uri);
			}
		}
		process(element);
	}

	void process(Element element) throws Exception {
		/**
			String name = element.getTagName();
			String uri = element.getNamespaceURI();
			String prefix = element.getPrefix();
		**/
		/** FIXME - why doesn't Xerces let us use node.getNamespaceURI()??/ **/
		String name = element.getTagName();
		String uri = "";
		int index;
		if ((index = name.indexOf(':')) >= 0) {
			String prefix = name.substring(0,index);
			name = name.substring(index+1);
			uri = (String)namespace_table.get(prefix);
		}
		if (debug) {
			System.err.println("Processing element: "+element);
			System.err.println("Name: "+name);
			System.err.println("URI: "+uri);
		}
		String value;
		boolean xmlbase_attribute = false;
		if ((value = element.getAttributeNS(processor.XMLBASE_NAMESPACE_URI,processor.XMLBASE_ATTRIBUTE)) != null) {
			startXMLBaseAttribute(value);
			xmlbase_attribute = true;
		}
		if (uri != null && uri.equals(processor.XINCLUDE_NAMESPACE_URI) && name.equals(processor.XINCLUDE_INCLUDE_ELEMENT)) {
			String href = element.getAttribute(processor.XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
			String parse = element.getAttribute(processor.XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
			processXIncludeElement(element, href, parse);
		}
		NodeList child_nodes = element.getElementsByTagName("*");
		int length = child_nodes.getLength();
		Element ary[] = new Element[length];
		for (int i=0; i<length; i++) {
			ary[i] = (Element)child_nodes.item(i);
		}
		for (int i=0; i<length; i++) {
			process(ary[i]);
		}
		if (xmlbase_attribute) {
			endXMLBaseAttribute();
		}
	}

	void startXMLBaseAttribute(String value) throws MalformedURLException {
		if (current_xmlbase_uri != null) {
			xmlbase_stack.push(current_xmlbase_uri);
		}
		current_xmlbase_uri = new URL(value);
	}

	void endXMLBaseAttribute() {
		current_xmlbase_uri = (URL)xmlbase_stack.pop();
	}

	void processXIncludeElement(Element element, String href, String parse) throws Exception {
		if (debug) { System.err.println("Processing XInclude element: href="+href+", parse="+parse); }
		String suffix;
		int index = href.indexOf('#');
		if (index < 0) {
			suffix = "";
		} else {
			suffix = href.substring(index+1);
			if (debug) { System.err.println("Suffix: "+suffix); }
			href = href.substring(0,index);
		}
		Object object;
		if (current_xmlbase_uri != null) {
			URL url = new URL(current_xmlbase_uri,href);
			if (debug) { System.err.println("URL: "+url); }
			object = url.getContent();
		} else {
			File file = new File(base_file,href);
			if (debug) { System.err.println("File: "+file); }
			object = new FileReader(file);
		}
		if (debug) { System.err.println("Object: "+object); }
		DocumentFragment result_fragment = document.createDocumentFragment();
		if (parse.equals("text")) {
			if (debug) { System.err.println("Parse type is text"); }
			if (object instanceof Reader) {
				Reader reader = (Reader)object;
				int read;
				char ary[] = new char[1024];
				StringBuffer sb = new StringBuffer();
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						sb.append(ary,0,read);
					}
					reader.close();
				}
				result_fragment.appendChild(document.createTextNode(sb.toString()));
			} else if (object instanceof InputStream) {
				InputStream input = (InputStream)object;
				InputStreamReader reader = new InputStreamReader(input);
				int read;
				char ary[] = new char[1024];
				StringBuffer sb = new StringBuffer();
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						sb.append(ary,0,read);
					}
					reader.close();
				}
				result_fragment.appendChild(document.createTextNode(sb.toString()));
			}
		} else if (parse.equals("xml")) {
			if (debug) { System.err.println("Parse type is XML"); }
			InputSource input;
			if (object instanceof Reader) {
				input = new InputSource((Reader)object);
			} else if (object instanceof InputStream) {
				input = new InputSource((InputStream)object);
			} else {
				throw new Exception("Unknown object type: "+object);
			}
			Document included_document = null;
			try {
				included_document = parser.parse(input,false);
			} catch (Exception e) {}
			if (suffix.startsWith("xptr(") && suffix.endsWith(")")) {
				String xpath = suffix.substring(5,suffix.length()-1);
				if (debug) { System.err.println("XPath is "+xpath); }
				NodeList list = XPathAPI.selectNodeList(included_document,xpath);
				int length = list.getLength();
				if (debug) { System.err.println("Found "+length+" nodes"); }
				for (int i=0; i<length; i++) {
					result_fragment.appendChild(document.importNode(list.item(i),true));
				}
			} else {
				result_fragment.appendChild(document.importNode(included_document.getDocumentElement(),true));
			}
		}
		Node parent_node = element.getParentNode();
		parent_node.replaceChild(result_fragment,element);
	}

}

}
