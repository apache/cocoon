/*
 * Copyright (c) 1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software and design ideas developed by the Java 
 *    Apache Project (http://java.apache.org/)."
 *
 * 4. The names "Cocoon", "Cocoon Servlet" and "Java Apache Project" must 
 *    not be used to endorse or promote products derived from this software 
 *    without prior written permission.
 *
 * 5. Products derived from this software may not be called "Cocoon"
 *    nor may "Cocoon" and "Java Apache Project" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software and design ideas developed by the Java 
 *    Apache Project (http://java.apache.org/)."
 *           
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *           
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Project. For more information
 * on the Java Apache Project please see <http://java.apache.org/>.
 */
 
package org.apache.cocoon.producer;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.parser.*;
import org.w3c.dom.*;
import gnu.regexp.*;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version $Revision: 1.3 $ $Date: 1999-12-16 11:45:07 $
 */

public class ProducerFromMap extends ProducerFromFile {

	public Document getDocument(HttpServletRequest request) throws Exception {
		String path = this.getBasename(request);
		File file = new File(path);
		File sitemap_file = null;
		String sitemap_path = path;
		while (true) {
			String parent = file.getParent();
			sitemap_file = new File(parent,".sitemap.xml");
			int index = sitemap_path.lastIndexOf('/');
			if (index >= 0) 
				sitemap_path = sitemap_path.substring(0,sitemap_path.lastIndexOf('/'));
			else 
				sitemap_path = "";
			if (sitemap_file.exists()) break;
			sitemap_file = null;
			file = new File(parent);
		}
		if (sitemap_file == null) return super.getDocument(request);
		Parser parser = (Parser)director.getActor("parser");
		Document sitemap = parser.parse(new InputSource(new FileReader(sitemap_file)));
		String relative_url = path.substring(sitemap_path.length());
		Element document_element = sitemap.getDocumentElement();
		NodeList children = document_element.getChildNodes();
		Node ary[] = new Node[children.getLength()];
		for (int i=0; i<children.getLength(); i++) ary[i] = children.item(i);
		for (int i=0; i<ary.length; i++) {
			Node child = ary[i];
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element)child;
			if (!element.getNodeName().equals("url")) continue;
			RE regexp = new RE(element.getAttribute("path"));
			if (regexp.isMatch(relative_url))
				return getDocument(parser,sitemap,element,sitemap_path,relative_url);
		}
		return super.getDocument(request);
	}

	public Document getDocument(Parser parser, Document sitemap, Element url_element, String path, String relative_url) throws Exception {
		Document results = null;
		NodeList children = url_element.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element)child;
			if (!element.getNodeName().equals("source")) continue;
			String mount = element.getAttribute("mount");
			if (mount == null) mount = ""; // For broken parsers
			String filename = element.getAttribute("file");
			if (filename == null) filename = ""; // For broken parsers
			if (filename.equals("")) filename = relative_url.substring(1);
			File file = new File(filename);
			if (!file.isAbsolute()) 
				file = new File(path,filename);
			Document document = parser.parse(new InputSource(new FileReader(file)));
			if (mount.equals("")) results = document;
			else {
				Element mount_element = results.createElement(mount);
				NodeList mount_children = document.getDocumentElement().getChildNodes();
				Node mount_ary[] = new Node[mount_children.getLength()];
				/**
				 * Why this is necessary I have no idea, but if I don't
				 * copy to an array first, only every other node gets copied
				 */
				for (int j=0; j<mount_ary.length; j++) 
					mount_ary[j] = mount_children.item(j);
				for (int j=0; j<mount_ary.length; j++) 
					mount_element.appendChild(mount_ary[j]);
				results.getDocumentElement().appendChild(mount_element);
			}
		}
		return results;
	}
    
	public String getStatus() {
		return "Map Producer";
	}
}