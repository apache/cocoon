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

package org.apache.cocoon.transformation;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

/**
 * A simple testcase for AbstractDOMTransformer.
 *
 * @version CVS $Id: XAbstractDOMTransformerTestCase.java,v 1.1 2004/05/07 14:56:44 ugo Exp $
 */
public class XAbstractDOMTransformerTestCase extends TestCase {

    /**
     * Constructor.
     * @param name
     */
    public XAbstractDOMTransformerTestCase(String name) {
        super(name);
    }

    /**
     * Test if sending two consecutive "characters" events to the transformer
     * doesn't lose one of them (cfr. bug #26219).
     */
    public void testJoiningCharacters() throws Exception {
        /*
         * Simple transformer that produces a document with a root with a single
         * text node whose value is given by the concatenation of the values
         * of the children of the root element of the original document.
         */
        AbstractDOMTransformer adt = new AbstractDOMTransformer() {
            protected Document transform(Document doc) {
                try {
                    Document newdoc = DocumentBuilderFactory
                            .newInstance().newDocumentBuilder().newDocument();
                    Element root = newdoc.createElement("out");
                    newdoc.appendChild(root);
                    NodeList children = doc.getDocumentElement().getChildNodes();
                    StringBuffer value = new StringBuffer();
                    for (int i = 0 ; i < children.getLength() ; ++i) {
                        value.append(children.item(i).getNodeValue());
                    }
                    root.appendChild(newdoc.createTextNode(value.toString()));
                    return newdoc;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        DOMBuilder builder = new DOMBuilder();
        adt.setConsumer(builder);
        Attributes attrs = new AttributesImpl();
        char c1[] = "ABC".toCharArray();
        char c2[] = "DEF".toCharArray();
        adt.startDocument();
        adt.startElement("", "in", "in", attrs);
        adt.characters(c1, 0, 3);
        adt.characters(c2, 0, 3);
        adt.endElement("", "in", "in");
        adt.endDocument();
        assertEquals("Content of root element not what expected", "ABCDEF", 
                builder.getDocument().getDocumentElement().getFirstChild().getNodeValue());
    }
}
