/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 */

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
import java.util.Vector;
import java.util.Enumeration;
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
import org.apache.cocoon.framework.Cacheable;
import org.apache.cocoon.framework.Director;
import org.apache.cocoon.framework.Monitor;
import org.apache.cocoon.processor.Processor;
import org.apache.cocoon.processor.ProcessorException;
import org.apache.cocoon.Utils;

/**
 * Second version of a DOM2 XInclude parser for cocoon. This revision
 * should support the bulk of the 2000-07-17 version of the XInclude working
 * draft. Notably excluded is inclusion loop checking 
 * (<a href="http://www.w3.org/TR/xinclude#IDw2Bq1">section 3.2.1</a>). Note
 * also that included namespaces may not be handled properly 
 * (<a href="http://www.w3.org/TR/xinclude#ID0mBq1">section 3.2.2</a>) -
 * I'd love feedback on this. Namespaces are simple - but the DOM2 (and SAX2)
 * methods for interacting with them aren't. Finally, note that the order of
 * include element processing as noted in
 * <a href="http://www.w3.org/TR/xinclude#IDwgAq1">section 3.1</a> is not
 * correct - internal xpointer links are not necessarily resolved against
 * the original source document. I haven't figured out a good way to resolve
 * that without cloning the entire source document first, which would be
 * a terrible wasteful of memory.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision: 1.22 $ $Date: 2001-03-08 11:04:08 $ $Author: greenrd $
 */
public class XIncludeProcessor extends AbstractActor implements Processor, Status, Cacheable {

	protected boolean debug = false;

	public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
	public static final String XMLBASE_ATTRIBUTE = "base";
	public static final String XINCLUDE_NAMESPACE_TAG = "xinclude:";
	public static final String XINCLUDE_TAG = "include";

	public static final String XINCLUDE_NAMESPACE_URI = "http://www.w3.org/1999/XML/xinclude";
	public static final String XINCLUDE_HREF_ATTRIBUTE = "href";
	public static final String XINCLUDE_PARSE_ATTRIBUTE = "parse";
	public static final int BUFFER_SIZE = 1024;

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
		Object key = Utils.encode((HttpServletRequest)object, false, false);
		if (monitored_table.containsKey(key)) {
			return monitor.hasChanged(key);
		}
		return false;
	}

        public boolean isCacheable(HttpServletRequest request) {
               return true;
        }

class XIncludeElement {

	Element element;
	Element parent;
	String href;
	String parse;
	Object base;
	String suffix;
	String xpath = null;

	XIncludeElement(Element element, Element parent, String href, String parse, Object base) {
		this.element = element;
		this.parent = parent;
		this.parse = parse;
		this.base = base;
		int index = href.indexOf('#');
		if (index < 0) {
			suffix = "";
			this.href = href;
		} else {
			suffix = href.substring(index+1);
			this.href = href.substring(0,index);
			if (suffix.startsWith("xpointer(") && suffix.endsWith(")")) {
				xpath = suffix.substring(9,suffix.length()-1);
			}
		}
	}

}

class XIncludeProcessorWorker {

	boolean debug;

	XIncludeProcessor processor;

	Document document;

	Object current_xmlbase;

	Stack xmlbase_stack = new Stack();

	Vector xinclude_elements = new Vector();

	Object monitor_key;

	HttpServletRequest request;

	XIncludeProcessorWorker(XIncludeProcessor processor, Document document, Dictionary parameters) throws Exception {
		this.processor = processor;
		debug = processor.debug;
		this.document = document;
		request = (HttpServletRequest)parameters.get("request");
		monitor_key = Utils.encode(request, false, false);
		String basepath = Utils.getBasepath(request,context);
		current_xmlbase = new File(basepath);
	}

	void process() throws Exception {
		Element element = document.getDocumentElement();
		element.appendChild(document.createComment("Processed by XInclude"));
		scan(element,null);
		Enumeration e = xinclude_elements.elements();
		while (e.hasMoreElements()) {
			XIncludeElement xinclude = (XIncludeElement)e.nextElement();
			Object object = processXIncludeElement(xinclude); //current_xmlbase gets changed here
			if (object instanceof Node) {
				Node node = (Node)object;
				xinclude.parent.replaceChild(node,xinclude.element);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					//xmlbase_stack.push(xinclude.base);
					scan((Element)node,xinclude.parent);
					//xmlbase_stack.pop();
				}
			} else if (object instanceof Node[]) {
				Node ary[] = (Node[])object;
				for (int i=0; i<ary.length; i++) {
					xinclude.parent.insertBefore(ary[i],xinclude.element);
				}
				xinclude.parent.removeChild(xinclude.element);
				for (int i=0; i<ary.length; i++) {
					//xmlbase_stack.push(xinclude.base);
					if (ary[i].getNodeType() == Node.ELEMENT_NODE) {
						scan((Element)ary[i],xinclude.parent);
					}
					//xmlbase_stack.pop();
				}

			}
		}
	}

	void scan(Element element, Element parent) throws Exception {
		String name = element.getLocalName();
		String uri = element.getNamespaceURI();
		String prefix = element.getPrefix();
		String value;
		boolean xmlbase_attribute = false;
		if (element.hasAttributeNS(processor.XMLBASE_NAMESPACE_URI,processor.XMLBASE_ATTRIBUTE)) {
			xmlbase_stack.push(current_xmlbase);
			current_xmlbase = new URL(element.getAttributeNS(processor.XMLBASE_NAMESPACE_URI,processor.XMLBASE_ATTRIBUTE));
			xmlbase_attribute = true;
		}
		if (!(scanForXInclude(element,parent))) {
			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					scan((Element)child,element);
				}
			}
		}
		if (xmlbase_attribute) {
			current_xmlbase = xmlbase_stack.pop();
		}
	}

	boolean scanForXInclude(Element element, Element parent) {
          if ((element.getNodeName().equals(processor.XINCLUDE_TAG) || element.getNodeName().equals(processor.XINCLUDE_NAMESPACE_TAG+processor.XINCLUDE_TAG)) && element.hasAttribute(processor.XINCLUDE_NAMESPACE_TAG + processor.XINCLUDE_HREF_ATTRIBUTE) && element.hasAttribute(processor.XINCLUDE_NAMESPACE_TAG + processor.XINCLUDE_PARSE_ATTRIBUTE)) {
            String href = element.getAttribute(processor.XINCLUDE_NAMESPACE_TAG + processor.XINCLUDE_HREF_ATTRIBUTE);
            String parse = element.getAttribute(processor.XINCLUDE_NAMESPACE_TAG + processor.XINCLUDE_PARSE_ATTRIBUTE);
            xinclude_elements.addElement(new XIncludeElement(element,parent,href,parse,current_xmlbase));
            return true;
          }
		if (element.hasAttributeNS(processor.XINCLUDE_NAMESPACE_URI,processor.XINCLUDE_HREF_ATTRIBUTE) && element.hasAttributeNS(processor.XINCLUDE_NAMESPACE_URI,processor.XINCLUDE_PARSE_ATTRIBUTE)) {
			String href = element.getAttributeNS(processor.XINCLUDE_NAMESPACE_URI,processor.XINCLUDE_HREF_ATTRIBUTE);
			String parse = element.getAttributeNS(processor.XINCLUDE_NAMESPACE_URI,processor.XINCLUDE_PARSE_ATTRIBUTE);
			xinclude_elements.addElement(new XIncludeElement(element,parent,href,parse,current_xmlbase));
			return true;
		}
		return false;
	}

	Object processXIncludeElement(XIncludeElement xinclude) throws Exception {
		Object content = null;
		String system_id = null;
		Object local = null;
		try {
			if (xinclude.href.equals("")) {
				if (xinclude.xpath == null) {
					throw new ProcessorException("Invalid xinclude element: "+xinclude+": no href, no valid suffix");
				}
				NodeList list = XPathAPI.selectNodeList(document,xinclude.xpath);
				int length = list.getLength();
				Node ary[] = new Node[length];
				for (int i=0; i<length; i++) {
					ary[i] = list.item(i).cloneNode(true);
				}
				return ary;
			} else if (xinclude.href.charAt(0) == '/') {
				/** local absolute URI, e.g. /foo.xml **/
				local = new File(Utils.getRootpath(request,context),xinclude.href.substring(1));
				system_id = ((File)local).getAbsolutePath();
				content = new FileReader((File)local);
			} else if (xinclude.href.indexOf("://") >= 0) {
				/** absolute URI, e.g. http://example.com/foo.xml **/
				local = new URL(xinclude.href);
				system_id = local.toString();
				content = ((URL)local).getContent();
			} else if (xinclude.base instanceof URL) {
				/** relative URI, relative to XML Base URI **/
				local = new URL((URL)xinclude.base,xinclude.href);
				system_id = local.toString();
				content = ((URL)local).getContent();
			} else if (xinclude.base instanceof File) {
				/** relative URI, relative to XML file in filesystem **/
				local = new File((File)xinclude.base,xinclude.href);
				system_id = ((File)local).getAbsolutePath();
				content = new FileReader((File)local);
			}
			processor.monitored_table.put(monitor_key,"");
			processor.monitor.watch(monitor_key,local);
		} catch (MalformedURLException e) {
			throw new ProcessorException("Invalid xinclude element: "+xinclude+": malformed URL");
		}
		Object result = null;
		if (xinclude.parse.equals("text")) {
			if (content instanceof Reader) {
				Reader reader = (Reader)content;
				int read;
				char ary[] = new char[processor.BUFFER_SIZE];
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
				char ary[] = new char[processor.BUFFER_SIZE];
				StringBuffer sb = new StringBuffer();
				if (reader != null) {
					while ((read = reader.read(ary)) != -1) {
						sb.append(ary,0,read);
					}
					reader.close();
				}
				result = document.createTextNode(sb.toString());
			}
		} else if (xinclude.parse.equals("cdata")) {
			/** i'm not sure what to do here **/
		} else {
			if (local instanceof File) {
				current_xmlbase = new File(((File)local).getParent());
				/**for Java2 we could have current_xmlbase = ((File)local).getParentFile()**/
			} else { /**local instanceof URL**/
				current_xmlbase = new URL((URL)local,".");
			}
			InputSource input;
			/*if (content instanceof Reader) {
				input = new InputSource((Reader)content);
			} else if (content instanceof InputStream) {
				input = new InputSource((InputStream)content);
			} else {
				throw new Exception("Unknown object type: "+content);
			}
			input.setSystemId(system_id); */
			input = new InputSource(system_id);
			Document included_document = null;
			try {
				included_document = parser.parse(input,false);
				stripDocumentTypeNodes(included_document.getDocumentElement());
			} catch (Exception e) {
                                throw new ProcessorException ("Error reading XIncluded entity: " + e);
                        }
			if (xinclude.xpath != null) {
				NodeList list = XPathAPI.selectNodeList(included_document,xinclude.xpath);
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
