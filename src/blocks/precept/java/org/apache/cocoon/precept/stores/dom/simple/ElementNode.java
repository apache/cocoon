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
package org.apache.cocoon.precept.stores.dom.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.precept.Constraint;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @version CVS $Id: ElementNode.java,v 1.3 2003/11/20 16:39:31 joerg Exp $
 */
public class ElementNode extends Node {

    private final static Attributes NOATTR = new AttributesImpl();

    private ArrayList childs;
    private ArrayList attributes;
    private HashMap attributeIndex;


    public ElementNode(String name, Collection constraints) {
        super(name, constraints);
    }

    public void addAttribute(Node node) {
        if (attributes == null) attributes = new ArrayList();
        if (attributeIndex == null) attributeIndex = new HashMap();
        attributes.add(node);
        attributeIndex.put(node.getValue(), node);
    }

    //public Node getAttribute( String name ) {
    //}
    public List getAttributes() {
        return (attributes);
    }


    public void addChild(Node node) {
        if (childs == null) childs = new ArrayList();
        childs.add(node);
    }

    public List getChilds() {
        return (childs);
    }

    //public List getChilds( String name ) {
    //}
    public void toStringBuffer(StringBuffer sb, ElementNode e, int depth) {
        StringBuffer ident = new StringBuffer();
        for (int i = 0; i < depth * 3; i++) ident.append(" ");

        sb.append("\n").append(ident).append("<").append(e.getName());


        Collection attributes = e.getAttributes();
        if (attributes != null) {
            for (Iterator it = attributes.iterator(); it.hasNext();) {
                AttributeNode attr = (AttributeNode) it.next();
                attr.toStringBuffer(sb, depth);
            }
        }

        sb.append(">").append("\n").append(ident).append(" ");

        sb.append(String.valueOf(e.getValue()));

        Collection childs = e.getChilds();
        if (childs != null) {
            for (Iterator it = childs.iterator(); it.hasNext();) {
                ElementNode child = (ElementNode) it.next();
                toStringBuffer(sb, child, depth + 1);
            }
        }
        sb.append("\n").append(ident);
        sb.append("</").append(e.getName()).append(">");
    }


    public void toSAX(ContentHandler handler, ElementNode e, boolean withConstraints) throws SAXException {

        handler.startElement("", e.getName(), e.getName(), NOATTR);

        if (e.getValue() != null) handler.characters(e.getValue().toString().toCharArray(), 0, e.getValue().length());

        if (withConstraints) {
            Collection constraints = e.getConstraints();
            if (constraints != null) {
                for (Iterator it = constraints.iterator(); it.hasNext();) {
                    Constraint constraint = (Constraint) it.next();

                    handler.startElement("", "constraint", "constraint", NOATTR);
                    String s = String.valueOf(constraint.getId()) +
                            " of type " + String.valueOf(constraint.getType()) +
                            " is " + constraint.isSatisfiedBy(e.getValue(), null);
                    handler.characters(s.toString().toCharArray(), 0, s.length());
                    handler.endElement("", "constraint", "constraint");
                }
            }
        }

        Collection childs = e.getChilds();
        if (childs != null) {
            for (Iterator it = childs.iterator(); it.hasNext();) {
                ElementNode child = (ElementNode) it.next();
                toSAX(handler, child, withConstraints);
            }
        }
        handler.endElement("", getName(), e.getName());
    }
}
