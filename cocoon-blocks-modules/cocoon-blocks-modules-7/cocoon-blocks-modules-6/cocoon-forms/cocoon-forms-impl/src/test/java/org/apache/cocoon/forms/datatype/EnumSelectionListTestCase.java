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

package org.apache.cocoon.forms.datatype;

import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.typeimpl.EnumType;
import org.apache.cocoon.xml.dom.DOMBuilder;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;

import org.w3c.dom.Document;

/**
 * Test case for CForms's DynamicSelectionList datatype.
 *
 * @version $Id$
 */
public class EnumSelectionListTestCase extends AbstractSelectionListTestCase {

    /**
     * Test the generateSaxFragment method.
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     */
    public void testGenerateSaxFragment() throws Exception {
        EnumSelectionList list = 
            new EnumSelectionList(Sex.class.getName(), new EnumType(), false);

        DOMBuilder dest = new DOMBuilder();
        dest.startDocument();
        list.generateSaxFragment(dest, Locale.ENGLISH);
        dest.endDocument();
        Document destDocument = dest.getDocument();
        
        Source expectedSource =
            new ResourceSource("resource://org/apache/cocoon/forms/datatype/EnumSelectionListTestCase.dest-no-null.xml");
        Document expected = this.parser.parse(expectedSource.getInputStream());
        // FIXME: Why is the namespace declaration available as attribute on the expected document? (see COCOON-2155)
        expected.getDocumentElement().removeAttribute("xmlns:" + FormsConstants.INSTANCE_PREFIX);
        expected.getDocumentElement().removeAttribute("xmlns:i18n");
        assertEqual("Test if output is what is expected", expected, destDocument);
    }
    
    /**
     * Test the generateSaxFragment method with a nullable selection list
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     */
    public void testGenerateSaxFragmentNullable() throws Exception {
        EnumSelectionList list = 
            new EnumSelectionList(Sex.class.getName(), new EnumType(), true);

        DOMBuilder dest = new DOMBuilder();
        dest.startDocument();
        list.generateSaxFragment(dest, Locale.ENGLISH);
        dest.endDocument();
        Document destDocument = dest.getDocument();

        Source expectedSource =
            new ResourceSource("resource://org/apache/cocoon/forms/datatype/EnumSelectionListTestCase.dest.xml");
        Document expected = this.parser.parse(expectedSource.getInputStream());
        // FIXME: Why is the namespace declaration available as attribute on the expected document? (see COCOON-2155)
        expected.getDocumentElement().removeAttribute("xmlns:" + FormsConstants.INSTANCE_PREFIX);
        expected.getDocumentElement().removeAttribute("xmlns:i18n");
        assertEqual("Test if output is what is expected", expected, destDocument);
    }
    
}
