package org.apache.cocoon.processor.newsql;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import java.util.Hashtable;

public class IElement {

	protected Element element;
	protected IElement parent;

	public IElement(Element element) {
		this.element = element;
	}

	public IElement(Element element, IElement parent) {
		this.element = element;
		this.parent = parent;
	}

	public String getAttribute(String name) {
		NamedNodeMap attributes = element.getAttributes();
		Node node = attributes.getNamedItem(name);
		if (node != null) {
			return node.getNodeValue();
		}
		if (parent != null) {
			return parent.getAttribute(name);
		}
		return null;
	}

	public int getIntAttribute(String name, int def) {
		String value = getAttribute(name);
		if (value.equals("")) {
			return def;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public void setAttribute(String name, String value) {
		element.setAttribute(name,value);
	}

	public Element getElement() {
		return element;
	}

	public Element getChildElement(String name) {
		NodeList child_nodes = element.getChildNodes();
		int length = child_nodes.getLength();
		for (int i=0; i<length; i++) {
			Node child_node = child_nodes.item(i);
			if (child_node.getNodeType() == Node.ELEMENT_NODE && child_node.getNodeName().equals(name)) {
				return (Element)child_node;
			}
		}
		return null;
	}

	public String getTextChildren() {
		StringBuffer sb = new StringBuffer();
		NodeList child_nodes = element.getChildNodes();
		int length = child_nodes.getLength();
		for (int i=0; i<length; i++) {
			Node child_node = child_nodes.item(i);
			int type = child_node.getNodeType();
			switch(type) {
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
				case Node.ENTITY_NODE:
					sb.append(child_node.getNodeValue());
			}
		}
		return sb.toString();
	}

}
