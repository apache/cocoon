package org.apache.cocoon.processor.newsql;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Hashtable;

public class IDocument {

	protected Document document;
	protected static String ID_ATTRIBUTE = "id";

	public IDocument(Document document) {
		this.document = document;
	}

	public IElement getIElement(String name) {
		return getIElement(name,null);
	}

	/**
	 * We should cache this somehow, but then the programmer must remember
	 * to remove the node from the IDocument as well as the regular document.
	 * probably, IDocument oughta completely wrap Document.
	 */
	public IElement getIElement(String name, String id) {
		NodeList elements = document.getElementsByTagName(name);
		int length = elements.getLength();
		if (length == 0) {
			return null;
		}
		Element ary[] = new Element[length];
		Element element = null;
		for (int i=0; i<length; i++) {
			ary[i] = (Element)elements.item(i);
		}
		if (id == null) {
			element = ary[0];
		} else {
			for (int i=0; i<length; i++) {
				String id_value = ary[i].getAttribute(ID_ATTRIBUTE);
				if (id_value != null && id_value.equals(id)) {
					element = ary[i];
				}
			}
		}
		if (element == null) {
			return null;
		}
		String extends_name = element.getAttribute("extends");
		if (extends_name == null || extends_name.equals("")) {
			return new IElement(element);
		}
		int index = extends_name.indexOf('.');
		if (index < 0) {
			return new IElement(element);
		}
		String child_name = extends_name.substring(0,index);
		String child_id = extends_name.substring(index+1);
		IElement parent = this.getIElement(child_name,child_id);
		return new IElement(element,parent);
	}

}
