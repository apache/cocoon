/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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

package org.apache.cocoon.processor.ixml;

import java.util.Dictionary;
import org.w3c.dom.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.processor.*;

/**
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1 $ $Date: 1999-12-23 07:23:18 $
 */

public class IXMLProcessor extends AbstractActor implements Processor, Status {

	protected static String namespace =
	"http://xml.apache.org/cocoon/namespaces/ixml";

	protected static String EXTENDS_ELEMENT = "extends";
	protected static String INHERITS_ELEMENTS_ATTRIBUTE = "inherits-elements";
	protected static String INHERITS_ATTRIBUTES_ATTRIBUTE = "inherits-attributes";
	protected static String NAME_ATTRIBUTE = "name";
	protected static String ID_ATTRIBUTE = "id";
	protected static String ID_ATTRIBUTE_ATTRIBUTE = "id-attribute";
	protected static String TRUE_VALUE = "true";

	protected static String default_prefix = "ixml";
	protected static boolean default_inherits_attributes = true;
	protected static boolean default_inherits_elements = false;

    public Document process(Document document, Dictionary parameters) throws Exception {
		/**
		NodeList elements = document.getElementsByTagName("*");
		for (int i=0; i<elements.getLength(); i++) {
			Element node = (Element)elements.item(i);
			System.err.println("NODE: "+node.getNodeName());
			if (node.getPrefix() != null) {
				System.err.println("PREFIX: "+node.getPrefix());
			}
			if (node.getNamespaceURI() != null) {
				System.err.println("NAMESPACE: "+node.getNamespaceURI());
			}
		}
		**/
		// FIXME - this is stupid, we should be using getElementsByTagNameNS
		// but it doesn't seem to be working properly in Xerces.
		NodeList extenders =
		document.getElementsByTagName(default_prefix+':'+EXTENDS_ELEMENT);
		int length = extenders.getLength();
		Element ary[] = new Element[length];
		for (int i=0; i<length; i++) {
			ary[i] = (Element)extenders.item(i);
		}
		for (int i=0; i<length; i++) {
			Element extender = ary[i];
			Element logical_parent = getLogicalParent(extender);
			Element physical_parent = (Element)extender.getParentNode();
			if (logical_parent != null) {
				Attr id_attribute_attr =
				extender.getAttributeNode(ID_ATTRIBUTE_ATTRIBUTE);
				String id_attribute;
				if (id_attribute_attr == null) {
					id_attribute = "id";
				} else {
					id_attribute = id_attribute_attr.getValue();
				}
				Attr inherits_attributes_attr =
				extender.getAttributeNode(INHERITS_ATTRIBUTES_ATTRIBUTE);
				boolean inherits_attributes;
				if (inherits_attributes_attr == null) {
					inherits_attributes = default_inherits_attributes;
				} else if
				(inherits_attributes_attr.getValue().equals(TRUE_VALUE)) {
					inherits_attributes = true;
				} else {
					inherits_attributes = false;
				}
				Attr inherits_elements_attr =
				extender.getAttributeNode(INHERITS_ELEMENTS_ATTRIBUTE);
				boolean inherits_elements;
				if (inherits_elements_attr == null) {
					inherits_elements = default_inherits_elements;
				} else if 
				(inherits_elements_attr.getValue().equals(TRUE_VALUE)) {
					inherits_elements = true;
				} else {
					inherits_elements = false;
				}
				if (inherits_attributes) {
					NamedNodeMap attributes = logical_parent.getAttributes();
					int attributes_length = attributes.getLength();
					for (int j=0; j<attributes_length; j++) {
						Attr attr = (Attr)attributes.item(j);
						if (!attr.getName().equals(id_attribute)) {
							physical_parent.setAttribute(attr.getName(),attr.getValue());
						}
					}
				}
				if (inherits_elements) {
					NodeList children = logical_parent.getChildNodes();
					int children_length = children.getLength();
					for (int j=0; j<children_length; j++) {
						Node child = children.item(j);
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							Node clone = child.cloneNode(true);
							physical_parent.insertBefore(clone,extender);
						}
					}
				}
			}
			physical_parent.removeChild(extender);
		}
		return document;
	}

	public static Element getLogicalParent(Element element) {
		Document document = element.getOwnerDocument();
		Attr name_attr = element.getAttributeNode(NAME_ATTRIBUTE);
		if (name_attr == null) {
			//Illegal extends element
			return null;
		}
		String name = name_attr.getValue();
		NodeList parents = document.getElementsByTagName(name);
		int length = parents.getLength();
		if (length <= 0) {
			//No matching parents node found
			return null;
		}
		Attr id_attr = element.getAttributeNode(ID_ATTRIBUTE);
		if (id_attr == null) {
			//Illegal extends element
			return null;
		}
		String id = id_attr.getValue();
		Attr id_attribute_attr =
		element.getAttributeNode(ID_ATTRIBUTE_ATTRIBUTE);
		String id_attribute;
		if (id_attribute_attr == null) {
			id_attribute = "id";
		} else {
			id_attribute = id_attribute_attr.getValue();
		}
		for (int i=0; i<length; i++) {
			Element parent = (Element)parents.item(i);
			id_attr = parent.getAttributeNode(id_attribute);
			if (id_attr != null && id.equals(id_attr.getValue())) {
				return parent;
			}
		}
		return null;
	}

    public boolean hasChanged(Object context) {
        return true;
    }

    public String getStatus() {
        return "IXML Processor";
    }

}
