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

package org.apache.cocoon.processor.ldap;

import org.w3c.dom.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

/**
 * Default Connection Values<br>
 * adapted from Donald Ball's SQLProcessor code.
 * @author <a href="mailto:jmbirchfield@proteus-technologies.com">James Birchfield</a>
 * @version 1.0
 */

public class LDAPDefs {

	protected Hashtable creators = new Hashtable();
	protected Hashtable query_props = new Hashtable();

    protected Properties default_query_props = master_default_query_props;
    protected static Properties master_default_query_props = new Properties();
	static {
        master_default_query_props.put("doc-element","ldapsearch");
        master_default_query_props.put("row-element","searchresult");
        master_default_query_props.put("id-attribute","ID");
        master_default_query_props.put("id-attribute-column","");
        master_default_query_props.put("variable-left-delimiter","{@");
        master_default_query_props.put("variable-right-delimiter","}");
        master_default_query_props.put("session-variable-left-delimiter","{@session.");
        master_default_query_props.put("session-variable-right-delimiter","}");
        master_default_query_props.put(Utils.ERROR_ELEMENT,"ldaperror");
        master_default_query_props.put(Utils.ERROR_MESSAGE_ATTRIBUTE,"message");        master_default_query_props.put(Utils.ERROR_MESSAGE_ELEMENT,"");
        master_default_query_props.put(Utils.ERROR_STACKTRACE_ATTRIBUTE,"");
        master_default_query_props.put(Utils.ERROR_STACKTRACE_ELEMENT,"");
	}


	public LDAPDefs(Document document) throws Exception {
		NodeList ldapdefs = document.getElementsByTagName("ldap-defs");
		Node ldap_defs_ary[] = new Node[ldapdefs.getLength()];
		for (int i=0; i<ldapdefs.getLength(); i++) 
			ldap_defs_ary[i] = ldapdefs.item(i);

		for (int i=0; i<ldap_defs_ary.length; i++) {
			Node ldap_def_node = ldap_defs_ary[i];
			NodeList ldaps = ldap_def_node.getChildNodes();
			for (int j=0; j<ldaps.getLength(); j++) {
				Node node = ldaps.item(j);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element)node;
					String name = element.getNodeName();
					if (name.equals("ldap-server")) 
						processLDAPDef(element);
                    else if (name.equals("ldap-querydefs"))
                        processQueryDef(element);
				}
			}
			ldap_def_node.getParentNode().removeChild(ldap_def_node);
		}
	}

	/**
	 * Process a single ldap definition node
	 */
	protected void processLDAPDef(Element ldap) throws Exception {
		String name = ldap.getAttribute("name");
		if (name == null) return;

		Properties ldap_props = new Properties();

		NodeList ldap_children = ldap.getChildNodes();
		for (int k=0; k<ldap_children.getLength(); k++) {
			Node ldap_parameter = ldap_children.item(k);
			String prop_name = ldap_parameter.getNodeName();
			NodeList ldap_parameter_values = ldap_parameter.getChildNodes();
			StringBuffer value = new StringBuffer();
			for (int l=0; l<ldap_parameter_values.getLength(); l++) {
				Node value_node = ldap_parameter_values.item(l);
				if (value_node.getNodeType() == Node.TEXT_NODE)
					value.append(value_node.getNodeValue());
			}
			ldap_props.put(prop_name,value.toString());
		}
		if (!ldap_props.containsKey("ldap-serverurl")) return;
		creators.put(name,new DirContextCreator(ldap_props));
	}

    protected void processQueryDef(Element querydef) {
        String name = querydef.getAttribute("name");
        if (name == null) return;
        NamedNodeMap attributes = querydef.getAttributes();
        Properties props = new Properties(master_default_query_props);
        for (int i=0; i<attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            props.put(attribute.getNodeName(),attribute.getNodeValue());
        }
        query_props.put(name,props);
        String def = querydef.getAttribute("default");
        if (def != null && (def.equals("y") || def.equals("yes")))
            default_query_props = props;
    }


	public DirContext getDirContext(String name) throws Exception {
		DirContextCreator creator = (DirContextCreator)creators.get(name);
		return creator.getDirContext();
	}

	public Properties getQueryProperties(String name) throws Exception {
        if (name == null) return default_query_props;
		Properties props = (Properties)query_props.get(name);
		return props;
	}

}
