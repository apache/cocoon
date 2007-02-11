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
package org.apache.cocoon.components.sax;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ContentHandler;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.AbstractXMLTestCase;
import org.apache.cocoon.xml.DefaultHandlerWrapper;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;

/**
 * Testcase for XMLByteStreamCompiler and Interpreter
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version
 */

public final class XMLByteStreamCompilerInterpreterTestCase extends AbstractXMLTestCase {
    public XMLByteStreamCompilerInterpreterTestCase(String s) {
        super(s);
    }

    public void testCompareDOM() throws Exception {
        // reference
        DOMBuilder in = new DOMBuilder();
        generateLargeSAX(in);

        // capture events
        XMLByteStreamCompiler xmlc = new XMLByteStreamCompiler();
        generateLargeSAX(xmlc);

        // recall events and build a DOM from it
        XMLByteStreamInterpreter xmli = new XMLByteStreamInterpreter();
        DOMBuilder out = new DOMBuilder();
        xmli.setConsumer(out);
        xmli.deserialize(xmlc.getSAXFragment());

        // compare DOMs
        assertXMLEqual(in.getDocument(), out.getDocument());
    }

    public void testCompareByteArray() throws Exception {
        // capture events
        XMLByteStreamCompiler sa = new XMLByteStreamCompiler();
        generateLargeSAX(sa);

        // serialize events
        byte[] aa = (byte[]) sa.getSAXFragment();

        // deserialize and capture
        XMLByteStreamCompiler sb = new XMLByteStreamCompiler();
        XMLByteStreamInterpreter xmli = new XMLByteStreamInterpreter();
        xmli.setConsumer(sb);
        xmli.deserialize(aa);

        // serialize again
        byte[] ab = (byte[]) sb.getSAXFragment();

        assertTrue(aa.length == ab.length);

        for (int i=0;i<aa.length;i++) {
            assertEquals(aa[i],ab[i]);
        }
    }

    public void testStressLoop() throws Exception {
        XMLByteStreamCompiler xmlc = new XMLByteStreamCompiler();

        long loop = 10000;

        // simply consume documents
        long start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            generateSmallSAX(xmlc);
            xmlc.recycle();
        }
        long stop = System.currentTimeMillis();

        double r = 1000*loop/(stop-start);
        System.out.println("consuming: "+ r + " documents per second");
    }

    public void testCompareToParsing() throws Exception {
        DOMBuilder in = new DOMBuilder();
        generateSmallSAX(in);

        SAXParserFactory pfactory = SAXParserFactory.newInstance();
        SAXParser p = pfactory.newSAXParser();

        XMLByteStreamCompiler xmlc = new XMLByteStreamCompiler();
        DefaultHandlerWrapper wrapper = new DefaultHandlerWrapper(xmlc);

        ByteArrayInputStream bis = new ByteArrayInputStream(generateByteArray());

        long loop = 10000;

        // parse documents
        long start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            xmlc.recycle();
            bis.reset();
            p.parse(bis,wrapper);
        }
        long stop = System.currentTimeMillis();

        double r = 1000*loop/(stop-start);
        System.out.println("parsed: " + r + " documents per second");


        XMLByteStreamInterpreter xmli = new XMLByteStreamInterpreter();
        ContentHandler ch = new DefaultHandler();

        // recall documents
        start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            xmli.setContentHandler(ch);
            xmli.deserialize(xmlc.getSAXFragment());
        }
        stop = System.currentTimeMillis();

        r = 1000*loop/(stop-start);
        System.out.println("recalling: " + r + " documents per second");
    }
}
