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
package org.apache.butterfly.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.butterfly.test.SitemapComponentTestCase;
import org.apache.butterfly.xml.dom.DOMStreamer;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Description of XMLSerializerTestCase.
 * 
 * @version CVS $Id: XMLSerializerTestCase.java,v 1.2 2004/07/24 20:31:57 ugo Exp $
 */
public class XMLSerializerTestCase extends SitemapComponentTestCase {

    /**
     * @param arg0
     */
    public XMLSerializerTestCase(String name) {
        super(name);
    }

    public void testSerialization() throws IOException, SAXException, ParserConfigurationException {
        XMLSerializer serializer = (XMLSerializer) getBean("xmlSerializer");
        DOMStreamer streamer = new DOMStreamer(serializer);
        Document input = XMLUnit.buildControlDocument(new InputSource("testdata/traxtest-input.xml"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serializer.setOutputStream(output);
        streamer.stream(input);
        Document test = XMLUnit.buildTestDocument(new InputSource(new StringReader(output.toString())));
        this.assertXMLEqual("Output from serializer does not match input file.",
                input, test);
    }
}
