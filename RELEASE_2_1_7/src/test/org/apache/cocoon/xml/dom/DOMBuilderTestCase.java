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
package org.apache.cocoon.xml.dom;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * JUnit Testcase for {@link DOMBuilder}.
 * 
 * @version CVS $Id: DOMBuilderTestCase.java,v 1.3 2004/05/18 12:36:52 ugo Exp $
 */
public class DOMBuilderTestCase extends TestCase {

    /**
     * Constructor.
     * @param name
     */
    public DOMBuilderTestCase(String name) {
        super(name);
    }

    /**
     * Test if two consecutive "characters" events result in two text nodes
     * whose concatenation is equal to the concatenation
     * of the two strings (cfr. bug #26219).
     * 
     * @throws SAXException
     */
    public void testMultipleCharactersEvents() throws SAXException {
        DOMBuilder builder = new DOMBuilder();
        Attributes attrs = new AttributesImpl();
        char c1[] = "ABC".toCharArray();
        char c2[] = "DEF".toCharArray();
        builder.startDocument();
        builder.startElement("", "test", "test", attrs);
        builder.characters(c1, 0, 3);
        builder.characters(c2, 0, 3);
        builder.endElement("", "test", "test");
        builder.endDocument();
        Document dom = builder.getDocument();
        StringBuffer value = new StringBuffer();
        for (int i = 0 ; i < dom.getDocumentElement().getChildNodes().getLength() ; ++i) {
            value.append(dom.getDocumentElement().getChildNodes().item(i).getNodeValue());
        }
        assertEquals("Content of root element not what expected",
                "ABCDEF", value.toString()); 
    }
}
