/*-- $Id: LdapProcessor.java,v 1.2 2000-02-13 18:29:31 stefano Exp $ -- 

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
 
package org.apache.cocoon.processor.ldap;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.processor.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

/**
 * A processor that performs Ldap queries<br>
 *
 * XML file format: <p><pre>
 *&lt;?xml version="1.0"?&gt;
 *&lt;?xml-stylesheet href="ldap.xsl" type="text/xsl"?&gt;
 *
 *&lt;?cocoon-process type="ldap"?&gt;
 *&lt;?cocoon-process type="xslt"?&gt;
 *
 *&lt;page&gt;
 *
 * &lt;ldap-defs&gt;
 *  &lt;ldap-server name="searchlight"&gt;
 *   &lt;initializer>com.sun.jndi.ldap.LdapCtxFactory&lt;/initializer&gt;
 *   &lt;ldap-serverurl>ldap://dir.skyway.nsa&lt;/ldap-serverurl&gt;
 *  &lt;/ldap-server&gt;
 * &lt;/ldap-defs&gt;
 *
 * &lt;ldap-query server="searchlight" ldap-searchbase="o=Proteus Technologies, c=US"&gt;
 *    uid=jmbirch
 * &lt;/ldap-query&gt;
 *
 *&lt;/page&gt;
 *</pre>
 *
 *
 * adapted from Donald Ball's SQLProcessor code.
 * @author <a href="mailto:jmbirchfield@proteus-technologies.com">James Birchfield</a>
 * @version 1.0
 */

public class LdapProcessor extends AbstractActor implements Processor, Status {
		
	public Document process(Document doc, Dictionary parameters) throws Exception {
		LdapDefs ldefs = new LdapDefs(doc);
        NodeList query_nodes = doc.getElementsByTagName("ldap-query");
        Node query_nodes_ary[] = new Node[query_nodes.getLength()];

        for (int i=0; i<query_nodes.getLength(); i++) {
            query_nodes_ary[i] = query_nodes.item(i);
        }

        for (int i=0; i<query_nodes_ary.length; i++) {
			Node query_node = query_nodes_ary[i];
			if (query_node.getNodeType() != Node.ELEMENT_NODE) continue;
			Element query_element = (Element)query_node;
            String defs = query_element.getAttribute("defs");
			Properties query_props = ldefs.getQueryProperties(defs);
			NamedNodeMap query_attributes = query_element.getAttributes();
			for (int j=0; j<query_attributes.getLength(); j++) {
				Node query_attribute = query_attributes.item(j);
				query_props.put(query_attribute.getNodeName(),query_attribute.getNodeValue());
			} 
			LdapContext ctx = ldefs.getLdapContext(query_props.getProperty("server"));    
			processQuery(doc,parameters,query_element,query_props,ctx);
		}
		return doc;
	}
	
	protected void processQuery(Document doc, Dictionary parameters, Element query_element, Properties query_props, LdapContext ctx) throws Exception {

        String doc_element_name = query_props.getProperty("doc-element");
        String row_element_name = query_props.getProperty("row-element");
        String id_attribute_name = query_props.getProperty("id-attribute");
		
		Node results_node = doc.createElement(doc_element_name);

		String ldap_searchbase = query_props.getProperty("ldap-searchbase");
		ldap_searchbase = (ldap_searchbase == null)?"":ldap_searchbase;
		
		NodeList query_text_nodes = query_element.getChildNodes();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<query_text_nodes.getLength(); i++) {
			Node query_text_node = query_text_nodes.item(i);
			if (query_text_node.getNodeType() == Node.TEXT_NODE) {
				buf.append(query_text_node.getNodeValue());
			}
		}

		
		try {
			String query = LdapQueryCreator.getQuery(buf.toString(), query_props, parameters);
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration ldapresults = ctx.search(ldap_searchbase, query, constraints);
			
			Node searchNode = results_node;
			Element searchElement = null;
			
			while (ldapresults != null && ldapresults.hasMore()) {
				SearchResult si = (SearchResult)ldapresults.next();
				
				searchElement = doc.createElement(row_element_name);
				searchNode = searchElement;
				searchElement.setAttribute(id_attribute_name, si.getName());
				
				Attributes attrs = si.getAttributes();
				if (attrs != null) {
					NamingEnumeration ae = attrs.getAll();
					while(ae.hasMoreElements()) {
						Attribute attr = (Attribute)ae.next();
						String attrId = attr.getID();
						Enumeration vals = attr.getAll();
						while(vals.hasMoreElements()) {
							Element attrElement = doc.createElement(attrId);
							attrElement.appendChild(doc.createTextNode((String)vals.nextElement()));
							searchNode.appendChild(attrElement);
							                
						}
					}
				}
				results_node.appendChild(searchNode);
			}
			query_element.getParentNode().replaceChild(results_node, query_element);
		}
		catch(Exception e) {
			System.err.println("Exception: " + e);
		}
		
	}
		
	public boolean hasChanged(Object o) {
		return true;
	}
	public String getStatus() {
		return "Ldap Processor";
	}
}
