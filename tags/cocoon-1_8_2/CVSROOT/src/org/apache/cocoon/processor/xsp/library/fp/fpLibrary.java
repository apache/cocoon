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
import java.security.*;

import org.w3c.dom.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.processor.xsp.*;
import org.apache.cocoon.framework.XObject;

public class fpLibrary {


	public static void getErrors(Node cNode, Hashtable errors) {
		Enumeration e = errors.elements();
		while (e.hasMoreElements()) {
			cNode.appendChild(((fpError)e.nextElement()).getErrorNode(cNode));
		}
		
	}

	public static void reportError(Node cNode, Node eNode, Hashtable errors, String message ) {
		String label = String.valueOf(errors.size() + 1);
		//System.out.println("error:"+ label);
		errors.put(label, new fpError(cNode, eNode, message, label));
		((Element)cNode).setAttribute("fp-error", "fp-" + label);
	}

	public static Node[] NodeList2Array(NodeList nodes) {
		int len = nodes.getLength();
		Node n[]  = new Node[len];
		if (len > 0) {
			for(int i = 0; i < len; i++) {
				n[i] = nodes.item(i);
			}
			return n;
		}
		return null;
	}

	public static void handleFileResource(Node cNode, HttpServletRequest request, ServletContext context, Hashtable resources,  Hashtable errors, String key, String file, String xpath, String mode) {
		//try {//
		File f = null;
		try {
			f = new File(XSPUtil.relativeFilename(file, request, context));
		} catch (Exception e) {
			reportError(cNode, cNode, errors, "Resource Error: Could not locate file: " + f.toString() + " : " + e.getMessage());
		}
		fpResource res = new fpResource(cNode, f, xpath, mode, key);
		resources.put(key, res);
		/*} catch(Exception ex) {
			System.out.println(ex.getMessage());
		}*/
			//System.out.println("request [" + request.getParameter("body") + "]");
		}
 
	public static Object handleRead(Node cNode, Hashtable resources, Hashtable errors, String select, String key, String type) {
		Object e = null;
		fpResource res = (fpResource)resources.get(key);
		if (res == null) {
			reportError(cNode, cNode, errors, "Resource not defined: " + key);
			return e;
		}
		if (type.equalsIgnoreCase("node")) {
			e = res.readAsNode(cNode, errors, select);
		} else {
			e = res.readAsString(cNode, errors, select);
		}
		return e;
	}

	public static Object handleWrite(Node cNode, Hashtable resources, Hashtable errors, String select, String key, Object value, String type, String mode) {
		Object e = null;
		fpResource res = (fpResource)resources.get(key);
		if (res == null) {
			reportError(cNode, cNode, errors, "Resource not defined: " + key);
			return e;
		}
		if (type.equalsIgnoreCase("node".trim())) {
			e = res.writeAsXMLNode(cNode, errors, select, value, mode);
		} else {
			e = res.writeAsTextNode(cNode, errors, select, value, mode);
		}
		return e;
	}
	
	public static String getAsMarkup(Node node) {
		if (!node.hasChildNodes()) {
			//System.out.println("getAsMarkup:" + node.getNodeValue());
			return node.getNodeValue();
		} else {
			StringBuffer buffer = new StringBuffer();
			NodeList n = node.getChildNodes();
			int childCount = n.getLength();
			for (int i = 0; i < childCount; i++) {
				doMarkup(n.item(i), buffer);
			}
			//System.out.println("getAsMarkup:" + buffer.toString());
			return buffer.toString();
		}
	}

		public static void saveResources(Node cNode, Hashtable resources, Hashtable errors) {
			Enumeration e = resources.elements();
			while (e.hasMoreElements()) {
				fpResource res = (fpResource)e.nextElement();
				if (res.hasChanged()) {
					res.save(cNode, errors);
				}
			}
		}

	protected static void doMarkup(Node node, StringBuffer buffer) {
		switch (node.getNodeType()) {
			case Node.CDATA_SECTION_NODE:
				buffer.append("<![CDATA[\n");
				buffer.append(node.getNodeValue());
				buffer.append("]]>\n");
				break;
			case Node.DOCUMENT_NODE:
			case Node.DOCUMENT_FRAGMENT_NODE: {
				NodeList nodeList = node.getChildNodes();
				int childCount = nodeList.getLength();
				for (int i = 0; i < childCount; i++) {
					doMarkup(nodeList.item(i), buffer);
				}
				break;
			}
			case Node.ELEMENT_NODE: {
				Element element = (Element) node;
				buffer.append("<" + element.getTagName());
				NamedNodeMap attributes = element.getAttributes();
				int attributeCount = attributes.getLength();
				for (int i = 0; i < attributeCount; i++) {
					Attr attribute = (Attr) attributes.item(i);
					buffer.append(" ");
					buffer.append(attribute.getName());
					buffer.append("=\"");
					buffer.append(attribute.getValue());
					buffer.append("\"");
				}
				NodeList nodeList = element.getChildNodes();
				int childCount = nodeList.getLength();
				if (childCount == 0) {
					buffer.append("/>\n");
				} else {
					buffer.append(">");
					for (int i = 0; i < childCount; i++) {
						doMarkup(nodeList.item(i), buffer);
					}
					buffer.append("</");
					buffer.append(element.getTagName());
					buffer.append(">");
				}
				break;
			}
			case Node.COMMENT_NODE:
				buffer.append("<!-- ");
				buffer.append(node.getNodeValue());
				buffer.append(" -->\n");
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				ProcessingInstruction pi = (ProcessingInstruction) node;
				buffer.append("<?");
				buffer.append(pi.getTarget());
				buffer.append(" ");
				buffer.append(pi.getData());
				buffer.append("?>\n");
				break;
			case Node.TEXT_NODE:
				buffer.append(node.getNodeValue());
				// buffer.append("\n");
				break;
			default:
				break;
		}
	}

}
