/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.generation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.butterfly.test.SitemapComponentTestCase;
import org.apache.butterfly.xml.WhitespaceFilter;
import org.apache.butterfly.xml.dom.DOMBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Description of FileGeneratorTestCase.
 * 
 * @version CVS $Id: FileGeneratorTestCase.java,v 1.2 2004/07/24 20:31:57 ugo Exp $
 */
public class FileGeneratorTestCase extends SitemapComponentTestCase {

    /**
     * @param name
     */
    public FileGeneratorTestCase(String name) {
        super(name);
    }
    
    public void testSimpleXMLFile() throws IOException, SAXException, ParserConfigurationException {
        XMLUnit.setIgnoreWhitespace(true);
        FileGenerator generator = (FileGenerator) getBean("fileGenerator");
        generator.setInputSource("testdata/test1.xml");
        DOMBuilder builder = new DOMBuilder();
        generator.setConsumer(new WhitespaceFilter(builder));
        generator.generate();
        this.assertXMLEqual("Output from generator does not match input file.",
                XMLUnit.buildControlDocument(new InputSource("testdata/test1.xml")),
                builder.getDocument());
    }
}
