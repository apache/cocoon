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
 * @version CVS $Id: ElementNode.java,v 1.4 2004/03/05 13:02:20 bdelacretaz Exp $
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
