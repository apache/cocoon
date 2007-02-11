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
package org.apache.cocoon.woody.datatype.convertor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.cocoon.woody.Constants;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for the {@link EnumConvertorBuilder} class.
 * 
 * @version CVS $Id: EnumConvertorBuilderTestCase.java,v 1.5 2004/03/09 13:54:18 reinhard Exp $
 */
public class EnumConvertorBuilderTestCase extends TestCase {

    protected DocumentBuilder parser;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newDocumentBuilder();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
    }

    public EnumConvertorBuilderTestCase(String name) {
        super(name);
    }

    /**
     * Test the {@link EnumConvertorBuilder#build(org.w3c.dom.Element)
     * build} method.
     * @throws Exception
     */
    public void testBuild() throws Exception {
        Source confSource = new ResourceSource("resource://org/apache/cocoon/woody/datatype/convertor/EnumConvertorTestCase.conf.xml");
        Document sample = parser.parse(confSource.getInputStream());
        Element convertorElement = (Element) sample.getElementsByTagNameNS(Constants.WD_NS, "convertor").item(0);
        String enumClassName = convertorElement.getElementsByTagNameNS(Constants.WD_NS, "enum").item(0).getFirstChild().getNodeValue();
        EnumConvertorBuilder builder = new EnumConvertorBuilder();
        Convertor convertor = builder.build(convertorElement);
        assertTrue("The returned convertor is not an EnumConvertor",
                convertor instanceof EnumConvertor);
        assertEquals("The convertor does not convert the expected class",
                Class.forName(enumClassName), convertor.getTypeClass());
    }
}
