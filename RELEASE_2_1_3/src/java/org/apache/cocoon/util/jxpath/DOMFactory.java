/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.util.jxpath;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A <a href="http://jakarta.apache.org/commons/jxpath">JXPath</a> <code>AbstractFactory</code>
 * that creates DOM elements.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id: DOMFactory.java,v 1.1 2003/09/09 14:23:08 sylvain Exp $
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

        addDOMElement((Element) parent, index, name);
        
        return true;
    }

    private void addDOMElement(Element parent, int index, String tag) {
        int pos = tag.indexOf(':');
        String prefix = null;
        if (pos != -1) {
            prefix = tag.substring(0, pos);
        }
        String uri = getNamespaceURI(parent, prefix);
                
        //System.out.println("Found namespace '" + uri + "' for tag " + tag);
        
        Node child = parent.getFirstChild();
        int count = 0;
        while (child != null) {
            if (child.getNodeName().equals(tag)) {
                count++;
            }
            child = child.getNextSibling();
        }

        // Keep inserting new elements until we have index + 1 of them
        while (count <= index) {
            Node newElement = parent.getOwnerDocument().createElementNS(uri, tag);
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
