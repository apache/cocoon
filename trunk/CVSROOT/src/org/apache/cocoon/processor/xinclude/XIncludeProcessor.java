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
import org.apache.cocoon.framework.Monitor;
import org.apache.cocoon.processor.Processor;
import org.apache.cocoon.processor.ProcessorException;
import org.apache.cocoon.Utils;

/**
 * First version of a DOM XInclude parser for cocoon. This has been back ported
 * from my XInclude filter for cocoon2.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision: 1.10 $ $Date: 2000-07-06 18:25:49 $ $Author: balld $
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

	protected Monitor monitor;
	protected Hashtable monitored_table;

	public void init(Director director) {
		super.init(director);
		parser = (Parser)director.getActor("parser");
		logger = (Logger)director.getActor("logger");
		context = director.getActor("context");
		monitor = new Monitor(10);
		monitored_table = new Hashtable();
	}

	public Document process(Document document, Dictionary parameters) throws Exception {
		XIncludeProcessorWorker worker = new XIncludeProcessorWorker(this,document,parameters);
		worker.process();
		return worker.document;
	}

	public String getStatus() {
		return "XInclude Processor";
	}

	public boolean hasChanged(Object object) {
		/** I would have thought that the monitor would return false if the
		    key has no resources being monitored, but it doesn't. I think
			that might should change, but we'll work around it for now. **/
		Object key = Utils.encode((HttpServletRequest)object);
		if (monitored_table.containsKey(key)) {
			return monitor.hasChanged(key);
		}
		return false;
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

	Object monitor_key;

	HttpServletRequest request;

	XIncludeProcessorWorker(XIncludeProcessor processor, Document document, Dictionary parameters) throws Exception {
		this.processor = processor;
		debug = processor.debug;
		this.document = document;
		request = (HttpServletRequest)parameters.get("request");
		monitor_key = Utils.encode(request);
		String basename = Utils.getBasename(request,context);
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
		process(element,null);
	}

	void process(Element element, Element parent) throws Exception {
		String name = element.getLocalName();
		String uri = element.getNamespaceURI();
		String prefix = element.getPrefix();
		String value;
		boolean xmlbase_attribute = false;
		if ((value = element.getAttributeNS(processor.XMLBASE_NAMESPACE_URI,processor.XMLBASE_ATTRIBUTE)) != null) {
			startXMLBaseAttribute(value);
			xmlbase_attribute = true;
		}
		if (uri != null && uri.equals(processor.XINCLUDE_NAMESPACE_URI) && name.equals(processor.XINCLUDE_INCLUDE_ELEMENT)) {
			String href = element.getAttribute(processor.XINCLUDE_INCLUDE_ELEMENT_HREF_ATTRIBUTE);
			String parse = element.getAttribute(processor.XINCLUDE_INCLUDE_ELEMENT_PARSE_ATTRIBUTE);
			Object object = processXIncludeElement(element, href, parse);
			if (object instanceof Node) {
				Node node = (Node)object;
				parent.replaceChild(node,element);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element)node;
				} else {
					return;
				}
			} else if (object instanceof Node[]) {
				Node ary[] = (Node[])object;
				for (int i=0; i<ary.length; i++) {
					parent.insertBefore(ary[i],element);
				}
				parent.removeChild(element);
				for (int i=0; i<ary.length ;i++) {
					if (ary[i].getNodeType() == Node.ELEMENT_NODE) {
						process((Element)ary[i],parent);
					}
				}
				return;
			}
		}
		NodeList child_nodes = element.getChildNodes();
		int length = child_nodes.getLength();
		Node ary[] = new Node[length];
		for (int i=0; i<length; i++) {
			ary[i] = child_nodes.item(i);
		}
		for (int i=0; i<length; i++) {
			if (ary[i].getNodeType() == Node.ELEMENT_NODE) {
				process((Element)ary[i],element);
			}
		}
		if (xmlbase_attribute) {
			endXMLBaseAttribute();
		}
	}

	void startXMLBaseAttribute(String value) throws MalformedURLException {
		if (current_xmlbase_uri != null) {
			xmlbase_stack.push(current_xmlbase_uri);
		}
		System.err.println("URL IS "+value);
		current_xmlbase_uri = new URL(value);
	}

	void endXMLBaseAttribute() {
		current_xmlbase_uri = (URL)xmlbase_stack.pop();
	}

	Object processXIncludeElement(Element element, String href, String parse) throws Exception {
		String suffix;
		int index = href.indexOf('#');
		if (index < 0) {
			suffix = "";
		} else {
			suffix = href.substring(index+1);
			href = href.substring(0,index);
		}
		Object content;
		String system_id;
		Object local;
		try {
			if (href.charAt(0) == '/') {
				local = new File(Utils.getRootpath(request,context)+href);
				system_id = ((File)local).getAbsolutePath();
				content = new FileReader((File)local);
			} else if (href.indexOf("://") >= 0) {
				local = new URL(href);
				system_id = local.toString();
				content = ((URL)local).getContent();
			} else if (current_xmlbase_uri != null) {
				local = new URL(current_xmlbase_uri,href);
				system_id = local.toString();
				content = ((URL)local).getContent();
			} else {
				local = new File(Utils.getBasepath(request,context)+href);
				system_id = local.toString();
				content = new FileReader((File)local);
			}
			processor.monitored_table.put(monitor_key,"");
			processor.monitor.watch(monitor_key,local);
		} catch (MalformedURLException e) {
			throw new ProcessorException("Could not include document: "+href+" is a malformed URL.");
		}
		Object result = null;
		if (parse.equals("text")) {
			if (content instanceof Reader) {
				Reader reader = (Reader)content;
				int read;
				char ary[] = new char[1024];
				StringBuffer sb = new StringBuffer();
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						sb.append(ary,0,read);
					}
					reader.close();
				}
				result = document.createTextNode(sb.toString());
			} else if (content instanceof InputStream) {
				InputStream input = (InputStream)content;
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
				result = document.createTextNode(sb.toString());
			}
		} else if (parse.equals("xml")) {
			InputSource input;
			if (content instanceof Reader) {
				input = new InputSource((Reader)content);
			} else if (content instanceof InputStream) {
				input = new InputSource((InputStream)content);
			} else {
				throw new Exception("Unknown object type: "+content);
			}
			input.setSystemId(system_id);
			Document included_document = null;
			try {
				included_document = parser.parse(input,false);
				stripDocumentTypeNodes(included_document.getDocumentElement());
			} catch (Exception e) {}
			if (suffix.startsWith("xpointer(") && suffix.endsWith(")")) {
				String xpath = suffix.substring(9,suffix.length()-1);
				NodeList list = XPathAPI.selectNodeList(included_document,xpath);
				int length = list.getLength();
				Node ary[] = new Node[length];
				for (int i=0; i<length; i++) {
					ary[i] = document.importNode(list.item(i),true);
				}
				result = ary;
			} else {
				result = document.importNode(included_document.getDocumentElement(),true);
			}
		}
		return result;
	}

	void stripDocumentTypeNodes(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			Node next = child.getNextSibling();
			if (child.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
				node.removeChild(child);
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				stripDocumentTypeNodes(child);
			}
			child = next;
		}
	}

}

}
