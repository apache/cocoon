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
package org.apache.butterfly.transformation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.butterfly.test.SitemapComponentTestCase;
import org.apache.butterfly.xml.WhitespaceFilter;
import org.apache.butterfly.xml.dom.DOMBuilder;
import org.apache.butterfly.xml.dom.DOMStreamer;
import org.apache.butterfly.xml.xslt.TraxTransformerFactory;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Description of TraxTransformerTestCase.
 * 
 * @version CVS $Id: TraxTransformerTestCase.java,v 1.3 2004/07/26 22:44:18 ugo Exp $
 */
public class TraxTransformerTestCase extends SitemapComponentTestCase {
    /**
     * @param name
     */
    public TraxTransformerTestCase(String name) {
        super(name);
    }

    public void testTransformation() throws IOException, SAXException, ParserConfigurationException {
        XMLUnit.setIgnoreWhitespace(true);
        TraxTransformerFactory factory = 
                (TraxTransformerFactory) getBean("traxTransformerFactory");
        TraxTransformer transformer = factory.getTransformer("testdata/traxtest-style.xsl");
        DOMBuilder builder = new DOMBuilder();
        transformer.setConsumer(new WhitespaceFilter(builder));
        DOMStreamer streamer = new DOMStreamer(transformer);
        Document control = XMLUnit.buildControlDocument(new InputSource("testdata/traxtest-result.xml"));
        Document input = XMLUnit.buildTestDocument(new InputSource("testdata/traxtest-input.xml"));
        streamer.stream(input);
        this.assertXMLEqual("Output from transformer does not match control file.",
                control, builder.getDocument());
    }
}
