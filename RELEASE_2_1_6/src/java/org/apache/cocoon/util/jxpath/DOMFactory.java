/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.util.jxpath;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A <a href="http://jakarta.apache.org/commons/jxpath">JXPath</a> <code>AbstractFactory</code>
 * that creates DOM elements.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id: DOMFactory.java,v 1.3 2004/03/17 09:29:15 cziegeler Exp $
 */

public class DOMFactory extends AbstractFactory {

    /**
     * Return <b>false</b> if this factory cannot create the requested object.
     */
    public boolean createObject(
        JXPathContext context,
        Pointer pointer,
        Object parent,
        String name,
        int index) 
    {
         //FIXME: JXPath automatically creates attributes if the element already exists,
         //but does not call this method if the element does not exit 

        addDOMElement((Node) parent, index, name);
        
        return true;
    }

    private void addDOMElement(Node parent, int index, String tag) {
        int pos = tag.indexOf(':');
        String prefix = null;
        if (pos != -1) {
            prefix = tag.substring(0, pos);
        }
        String uri = null;
        
        Node child = parent.getFirstChild();
        int count = 0;
        while (child != null) {
            if (child.getNodeName().equals(tag)) {
                count++;
            }
            child = child.getNextSibling();
        }

        Document doc = parent.getOwnerDocument();
        
        if (doc != null) {
            uri = getNamespaceURI((Element)parent, prefix);
        } else {
            if (parent instanceof Document) {
                doc = (Document)parent;
                if (prefix != null) {
                    throw new RuntimeException("Cannot map non-null prefix " +
                        "when creating a document element");    
                }
            } else { // Shouldn't happen (must be a DocumentType object)
                throw new RuntimeException("Node of class " +
                    parent.getClass().getName() + " has null owner document " +
                    "but is not a Document"); 
            }

        }

        // Keep inserting new elements until we have index + 1 of them
        while (count <= index) {
            Node newElement = doc.createElementNS(uri, tag);
            parent.appendChild(newElement);
            count++;
        }
    }
    
    public String getNamespaceURI(Element element, String prefix) {
        Node tmp = element;
        String nsAttr = prefix == null ? "xmlns" : "xmlns:" + prefix;
        
        while (tmp != null && tmp.getNodeType() == Node.ELEMENT_NODE) {
            element = (Element)tmp;
            
            // First test element prefixes
            if (prefix == null) {
                if (element.getPrefix() == null) {
                    return element.getNamespaceURI();
                }
            } else if(prefix.equals(element.getPrefix())) {
                return element.getNamespaceURI();
            }
            
            String namespace = ((Element)tmp).getAttribute(nsAttr);
            if (namespace != null) {
                //System.out.println("Found attribute '" + nsAttr + "'='" + namespace + "' on element " + tmp.getNodeName());
                return namespace;
            }
            tmp = tmp.getParentNode();
        }
        return null;
    }

    public boolean declareVariable(JXPathContext context, String name) {
        return false;
    }
}
