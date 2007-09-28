/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.datatype.convertor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for the {@link EnumConvertorBuilder} class.
 * 
 * @version $Id$
 */
public class EnumConvertorBuilderTestCase extends TestCase {

    protected DocumentBuilder parser;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newDocumentBuilder();
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
        Source confSource = new ResourceSource("resource://org/apache/cocoon/forms/datatype/convertor/EnumConvertorTestCase.conf.xml");
        Document sample = parser.parse(confSource.getInputStream());
        Element convertorElement = (Element) sample.getElementsByTagNameNS(FormsConstants.DEFINITION_NS, "convertor").item(0);
        String enumClassName = convertorElement.getElementsByTagNameNS(FormsConstants.DEFINITION_NS, "enum").item(0).getFirstChild().getNodeValue();
        PlainEnumConvertorBuilder builder = new PlainEnumConvertorBuilder();
        Convertor convertor = builder.build(convertorElement);
        assertTrue("The returned convertor is not an EnumConvertor",
                convertor instanceof EnumConvertor);
        assertEquals("The convertor does not convert the expected class",
                Class.forName(enumClassName), convertor.getTypeClass());
    }
}
