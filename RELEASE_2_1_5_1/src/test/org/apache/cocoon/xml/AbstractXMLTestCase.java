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
package org.apache.cocoon.xml;

import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.cocoon.xml.dom.DOMBuilder;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;


/**
 * general functions for XML related Testcases
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version
 */

public abstract class AbstractXMLTestCase extends XMLTestCase {

    public AbstractXMLTestCase(String s) {
        super(s);
    }

    protected void generateLargeSAX( ContentHandler consumer ) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        final int size = 65000;
        char[] large = new char[size];
        for(int i=0;i<size;i++) {
            large[i] = 'x';
        }

        consumer.startDocument();
        consumer.startElement("", "root", "root", atts);
        consumer.characters(large,0,size);
        consumer.endElement("", "root", "root");
        consumer.endDocument();
    }

    protected void generateSmallSAX( ContentHandler consumer ) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        consumer.startDocument();
        consumer.startElement("", "root", "root", atts);
        consumer.characters("test".toCharArray(),0,4);
        consumer.endElement("", "root", "root");
        consumer.endDocument();
    }

    protected byte[] generateByteArray() throws Exception {
        DOMBuilder in = new DOMBuilder();
        generateSmallSAX(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer t = tFactory.newTransformer();
        Source input = new DOMSource(in.getDocument());
        Result output = new StreamResult(bos);
        t.transform(input, output);
        bos.close();

        return bos.toByteArray();
    }
}

