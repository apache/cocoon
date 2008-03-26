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
import org.apache.cocoon.xml.dom.DOMBuilder;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.ResourceSource;
import org.apache.excalibur.xmlizer.XMLizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for CForms's DynamicSelectionList datatype.
 * @version $Id$
 */
public class DynamicSelectionListTestCase extends AbstractSelectionListTestCase {

    /**
     * Test the generateSaxFragment method.
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     */
    public void testGenerateSaxFragment() throws Exception {
        Source source = 
            new ResourceSource("resource://org/apache/cocoon/forms/datatype/DynamicSelectionListTestCase.source.xml");
        Document sourceDoc = this.parser.parse(source.getInputStream());
        Element datatypeElement = (Element) sourceDoc.getElementsByTagNameNS(FormsConstants.DEFINITION_NS, "convertor").item(0);
        Datatype datatype = this.datatypeManager.createDatatype(datatypeElement, false);
        final XMLizer xmlizer = (XMLizer)getManager().lookup( XMLizer.ROLE );
        final SourceResolver sourceResolver = (SourceResolver)getManager().lookup( SourceResolver.ROLE );
        DynamicSelectionList list = new DynamicSelectionList(datatype, null, xmlizer, sourceResolver, null);
        
        DOMBuilder dest = new DOMBuilder();
        dest.startDocument();
        list.generateSaxFragment(dest, Locale.ENGLISH, source);
        dest.endDocument();
        Document destDocument = dest.getDocument();
        
        Source expectedSource =
            new ResourceSource("resource://org/apache/cocoon/forms/datatype/DynamicSelectionListTestCase.dest.xml");
        Document expected = this.parser.parse(expectedSource.getInputStream());
        // FIXME: Why is the namespace declaration available as attribute on the expected document? (see COCOON-2155)
        expected.getDocumentElement().removeAttribute("xmlns:" + FormsConstants.INSTANCE_PREFIX);
        assertEqual("Test if output is what is expected", expected, destDocument);
    }

}
