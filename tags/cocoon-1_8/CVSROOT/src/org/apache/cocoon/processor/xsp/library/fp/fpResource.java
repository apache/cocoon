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

package org.apache.cocoon.processor.xsp.library.fp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.w3c.dom.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.processor.xsp.*;
import org.apache.cocoon.xml.util.XPathAPI;
import org.apache.cocoon.framework.XObject;
import org.xml.sax.InputSource;


public class fpResource {

	//RevalidatingDOMParser parser = null;
	protected static SerializerFactory serializer_factory = SerializerFactory.getSerializerFactory(Method.XML);
	Document workDoc = null;
	String name = "";
	Node readNode = null;
	Node writeNode = null;
	Node errorNode = null;
	File workFile = null;
	String workEncoding = "UTF-8";
	HttpServletRequest request;
	boolean changed = false;
	
	String xpath = "";
	public String mode = "";
	
	
	public fpResource (Node errorNode, File workFile, String xpath, String mode, String name) {
		this.errorNode = errorNode;
		this.workFile = workFile;
		this.xpath = xpath;
		this.mode = mode;
		this.name = name;
	}

	public void save(Node cNode, Hashtable errors) {
		OutputFormat format = new OutputFormat(workDoc, workEncoding, true);
		format.setVersion("1.0");
		format.setPreserveSpace(true);
		format.setIndent(1);
		format.setIndenting(true);
		format.setLineWidth(0);
		format.setLineSeparator("\n");
	
		try {
			FileWriter writer = new FileWriter(workFile);
			Serializer serializer = serializer_factory.makeSerializer(writer,format);
			serializer.asDOMSerializer().serialize(workDoc);
			writer.close();
		} catch (Exception ex) {
			fpLibrary.reportError(cNode, cNode, errors, "Cannot save the file: " + workFile);
			ex.printStackTrace();
		}
	}
		
	private void init(Hashtable errors) {
		if (notLoaded()) {
			Node workNode = null;
			Node parent = null;
			NodeList nodes = null;
			//System.out.println("about to call LoadDocument");
			workDoc = loadDocument(workFile, errors);
			//System.out.println("LoadDocument called ");
			if (workDoc == null) {
			//System.out.println("Document was null");
			}
			try {
				//System.out.println("about to call XPathAPI.selectNodeList [" + xpath + "]");
				nodes = XPathAPI.selectNodeList(workDoc.getDocumentElement(), xpath);
				//System.out.println("XPathAPI.selectNodeList called");
			} catch (Exception ex) {
				fpLibrary.reportError(errorNode, errorNode, errors, "Problem using the supplied XPath expression: " + xpath + " : " + ex.getMessage());
				ex.printStackTrace();
			}
			if (nodes != null && nodes.getLength() == 0) {
				fpLibrary.reportError(errorNode, errorNode, errors, "(init) No nodes match the given XPath expression: " + xpath);
			}
			//System.out.println("OK");
			try {
				readNode = nodes.item(0);
				writeNode = readNode;
				parent = readNode.getParentNode();
				//System.out.println("Starting mode, Got :" + parent.getNodeName() + "/" + readNode.getNodeName());
				//System.out.println("there are nodes here :" + String.valueOf(readNode.hasChildNodes()));
				if (mode.equalsIgnoreCase("remove")) {
					parent.removeChild(readNode);
					changed = true;
				} else if (mode.equalsIgnoreCase("insert-before")) {
					workNode = workDoc.createElement(readNode.getNodeName());
					parent.insertBefore(workDoc.createTextNode("\r\t"), readNode);
					parent.insertBefore(workNode, readNode);
					writeNode = workNode;
				} else if (mode.equalsIgnoreCase("insert-after")) {
					workNode = workDoc.createElement(readNode.getNodeName());
					Node brother = parent.getNextSibling();
					if (brother == null) {
						parent.appendChild(workDoc.createTextNode("\r\t"));
						parent.appendChild(workNode);
					} else {
						parent.insertBefore(workDoc.createTextNode("\r\t"), brother);
						parent.insertBefore(workNode, brother);
					}
					writeNode = workNode;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//System.out.println("Ending mode, Got :" + parent.getNodeName() + "/" + readNode.getNodeName());
			//System.out.println("there are nodes here :" + String.valueOf(readNode.hasChildNodes()));
			}
	}

	public Object readAsNode(Node cNode, Hashtable errors, String select) {
		init(errors);
		NodeList nodes = null;
		((Element)cNode).setAttribute("fp-type", "text/xml");
		((Element)cNode).setAttribute("fp-action", "read");
		((Element)cNode).setAttribute("fp-select", select);
		((Element)cNode).setAttribute("fp-from", name);
		try {
			try {
				nodes = XPathAPI.selectNodeList(readNode, select, null);
			} catch (Exception ex) {
				fpLibrary.reportError(cNode, readNode, errors, "XPath expression error on:" + select + " : " + ex.getMessage());
				ex.printStackTrace();
				return null;
			}
			if (nodes == null || nodes.getLength() == 0) {
				fpLibrary.reportError(cNode, readNode, errors, "No nodes match the given XPath expression:" + select);
				return null;
			} 
				//e.appendChild((Element)XSPUtil.cloneNode(nodes.item(0), outDoc));
				//e.appendChild((Element)XSPUtil.cloneNode((Node)nodes, outDoc));
				//for (int i = 0; i < nodes.getLength(); i ++) {
				//	e.appendChild((Element)XSPUtil.cloneNode(nodes.item(i), outDoc));
				//}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		return fpLibrary.NodeList2Array(nodes);
	}

	public Object readAsString(Node cNode, Hashtable errors, String select) {
		init(errors);
		NodeList nodes = null;
		StringBuffer temp = new StringBuffer("");
		((Element)cNode).setAttribute("fp-type", "text/plain");
		((Element)cNode).setAttribute("fp-action", "read");
		((Element)cNode).setAttribute("fp-select", select);
		((Element)cNode).setAttribute("fp-from", name);
		try {
			try {
				nodes = XPathAPI.selectNodeList(readNode, select, null);
				//System.out.println("Found " + nodes.getLength() + " nodes");
			} catch (Exception ex) {
				fpLibrary.reportError(cNode, readNode, errors, "XPath expression error on:" + select + " : " + ex.getMessage());
				ex.printStackTrace();
			}
			if (nodes == null || nodes.getLength() == 0) {
				fpLibrary.reportError(cNode, readNode, errors, "No nodes match the given XPath expression:" + select);
			} else {
				//System.out.println("extracting TextNodes from: " + nodes.getLength());
				// for each of our results
				for (int i = 0; i < nodes.getLength(); i ++) {
					Node n = nodes.item(i);
					NodeList nl = n.getChildNodes();
					if (nodes.getLength() == 0) { return n.getNodeValue(); }// if this is a TextNode, get just it
					for (int ix = 0; ix < nl.getLength(); ix++) 
						if (nl.item(ix).getNodeType() == Node.TEXT_NODE) { // only get the Text Nodes
							//System.out.println("extracting: [" + nl.item(ix).getNodeValue() + "]");
							//temp.append(fpLibrary.getAsMarkup((Node)nodes.item(i)));
							temp.append(nl.item(ix).getNodeValue());
						}
					}
				}
				/*
				 = fpLibrary.getAsMarkup((Node)nodes);
				System.out.println("got : [" + temp + "]");*/
				//e.appendChild(outDoc.createTextNode(temp.toString()));

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return temp.toString();
	}

	public Object writeAsTextNode(Node cNode, Hashtable errors, String select, Object value, String customMode) {
		init(errors);
		//Object result = null;
		((Element)cNode).setAttribute("fp-type", "text/plain");
		((Element)cNode).setAttribute("fp-action", "write");
		((Element)cNode).setAttribute("fp-select", select);
		((Element)cNode).setAttribute("fp-to", name);
		try {
			Object fileNode = sureNode(select); // construct path to node
			if (fileNode instanceof Node[]) {
				int len = ((Node[])fileNode).length;
				if ( len > 1 ) {  // FIXME ?? is there an alternative to this?
					fpLibrary.reportError(cNode, readNode, errors, "Cannot write to the multiple Nodes returned by this XPath expression:" + select);
					return null;
				} else if ( len == 1 ) {
					fileNode = (Node)((Node[])fileNode)[0];
				}
			}
			NodeList kids = ((Node)fileNode).getChildNodes();
			for (int i = kids.getLength()-1; i>-1	 ; i --) { // strip out all existing TextNodes from fileNode
				if (kids.item(i).getNodeType() == Node.TEXT_NODE) {
					((Element)fileNode).removeChild(kids.item(i));
				}
			}
			if (value instanceof Node[]) { // from embedded fp:read tag having multiple hits with it's XPath
				Node[] v = (Node[])value;
				for (int i = 0; i < v.length ; i ++) { // adopt all TextNode elements
					if (v[i].getNodeType() == Node.TEXT_NODE  ) {
						((Element)fileNode).appendChild(workDoc.createTextNode(v[i].getNodeValue())); // send to file
					}
				}
			//result = fileNode; // send to form
			} else if (value instanceof Element) { // from embedded fp:tag
				Node v = (Node)value;
				kids = v.getChildNodes();
				if (kids.getLength() == 0) { // it's a Text Node (?!?)
					((Element)fileNode).appendChild(workDoc.createTextNode(v.getNodeValue()));
				} else {
					for (int i = 0; i < kids.getLength() ; i ++) {
						if (kids.item(i).getNodeType() == Node.TEXT_NODE) {
							((Element)fileNode).appendChild(workDoc.createTextNode(kids.item(i).getNodeValue())); // clone the TextNode kids to file
						}
					}
				}
				//result = fileNode; // send to form
			} else { // make a TextNode of the String value
				Node tn = workDoc.createTextNode((String)value);
				((Element)fileNode).appendChild(tn); // send to file
				//result = tn;
			}
			// removed till Xerces 1.1.2 is working
			/*Node errorNode = parser.validate(workNode);
			if (errorNode != null) {
				fpLibrary.reportError(currentNode, errorNode, "Cannot Validate from: " + errorNode.getNodeName() + " with XPath: " + select);
				errors = true;
			} else {
				changed = true;
			}*/
			changed = true;
					
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";  // no longer returning write data
	}

	public Object writeAsXMLNode(Node cNode, Hashtable errors, String select, Object value, String customMode) {
		init(errors);
		//Object result = null;
		((Element)cNode).setAttribute("fp-type", "text/xml");
		((Element)cNode).setAttribute("fp-action", "write");
		((Element)cNode).setAttribute("fp-select", select);
		((Element)cNode).setAttribute("fp-to", name);
		try {
			Object fileNode = sureNode(select);
			if (fileNode instanceof Node[]) {
				int len = ((Node[])fileNode).length;
				if ( len > 1 ) {
					fpLibrary.reportError(cNode, readNode, errors, "Cannot write to the multiple Nodes returned by this XPath expression:" + select);
					return value;
				} else if ( len == 1 ) {
					fileNode = (Node)((Node[])fileNode)[0];
				}
			}
			Node newNode = workDoc.createElement(((Node)fileNode).getNodeName()); // node to insert to file
			if (value instanceof Node[]) { // from embedded fp:read tag having multiple hits with it's XPath
				Node[] v = (Node[])value;
				for (int i = 0; i < v.length ; i ++) { // adopt all Node elements
					newNode.appendChild(XSPUtil.cloneNode(v[i], workDoc)); // send to file
				}
			} else if (value instanceof Element) { // from embedded fp:tag
				Node v = (Node)value;
				NodeList kids = v.getChildNodes();
				for (int i = 0; i < kids.getLength() ; i ++) { // adopt all childNodes
					newNode.appendChild(XSPUtil.cloneNode(kids.item(i), workDoc)); // send to file
				}
			} else {
				// make a TextNode of the parsed String value
				newNode = parseStringInto(workDoc, cNode, String.valueOf(value), errors); // send to file
			}
			if (newNode != null) {
				((Node)fileNode).getParentNode().replaceChild(newNode, (Node)fileNode);// replace copy in file
				//result = newNode; // send it to form
			}
			// removed till Xerces 1.1.2 is working
			/*Node errorNode = parser.validate(fileNode);
			if (errorNode != null) {
				fpLibrary.reportError(cNode, errorNode, errors, "Cannot Validate from: " + errorNode.getNodeName() + " with XPath: " + select);
				errors = true;
			}*/			
			changed = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";  // no longer returning write data
	}

	/* get the Element we need to change, building any required on the way */
	public Object sureNode(String select) {
		NodeList nodes = null;
		boolean found = false;
		Element e = null;
		//System.out.println("sureNode select:" + select + " from:" + writeNode.getNodeName());
		try {
			nodes = XPathAPI.selectNodeList(writeNode, select);
		} catch (Exception ex) {
			//fpLibrary.reportError(currentNode, rootNode, "Cannot construct XPath: " + select + " : " + ex.getMessage());
			//errors = true;
		}
		/*if (nodes != null) {
			System.out.println("Selected Nodes:" + nodes.getLength());
		} else {
			System.out.println("Selected Nodes:0");
		}*/
		if (nodes != null && nodes.getLength() > 0) {
			// I've got hits, return an Array of Nodes
			return fpLibrary.NodeList2Array(nodes);
		} else {
			/* make it */
			//System.out.println("Making Nodes");
			StringTokenizer st = new StringTokenizer(select,"/");
			Node workNode = writeNode;
			NodeList kids;
			String part = "";
			int len;
			while (st.hasMoreTokens()) {
				part = st.nextToken();
				//System.out.println("Looking for:" + part);
				kids = workNode.getChildNodes();
				found = false;
				len = kids.getLength();
				//System.out.println("Children:" + len);
				for (int i = 0; i < len ; i++) {
					//System.out.println("OK 6");
					Node n = kids.item(i);
					//System.out.println("Looking at:"+ n.getNodeName());
					if (n.getNodeName().equals(part)) {
						workNode = n;
						//System.out.println("found = true");
						found = true;
						break;
					}
				}
			}
			if (found == false) {
				//System.out.println("createElement");
				//System.out.println((workDoc!=null) ? "OK": "SHIT!");
				part = (part=="")?select:part;
				e = workDoc.createElement(part);
				//System.out.println("appendChild");
				workNode.appendChild(workDoc.createTextNode("\n\t"));
				workNode.appendChild(e);
			}
		}
		//System.out.println("Done");
			return e;
		}

	public Node parseStringInto(Document doc, Node cNode, String val, Hashtable errors) {
		DOMParser p = new DOMParser();
		try {
			//System.out.println("Writing:" + val);
			p.parse(new InputSource(new StringReader(val)));
		} catch (Exception ex) {
			fpLibrary.reportError(cNode, errorNode, errors, "Cannot Parse: "  + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		return XSPUtil.cloneNode(p.getDocument().getDocumentElement(), doc);
	}

	public Document loadDocument(File f, Hashtable errors) {
		// removed till Xerces 1.1.2 is working
		//RevalidatingDOMParser parser = new RevalidatingDOMParser();
		DOMParser parser = new DOMParser();
		try {
			//System.out.println("about to parse Document");
			InputSource is = new InputSource("file:" + f.toString());
			String enc = is.getEncoding();
			if (enc != "") {
				workEncoding = enc;
			}
			parser.parse(is);
			//System.out.println("parsed Document");
		} catch (Exception ex) {
			//System.out.println("about to call reportError");
			fpLibrary.reportError(errorNode, errorNode, errors, "Cannot Parse: " + f.toString() + " : " + ex.getMessage());
			//System.out.println("reportError called");
			ex.printStackTrace();
			return null;
		}
		return parser.getDocument();
	}

	public boolean notLoaded() {
		return (readNode == null);
	}
	public boolean hasChanged() {
		return (changed);
	}
}
