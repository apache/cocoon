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

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Testcase for DOMStreamer and DOMBuilder.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: DOMBuilderStreamerTestCase.java,v 1.7 2004/03/05 13:03:04 bdelacretaz Exp $
 */
public class DOMBuilderStreamerTestCase extends XMLTestCase {

    public DOMBuilderStreamerTestCase(String name) {
        super(name);
    }

    public void testBuilderWithOneElement() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.endElement("", "root", "root");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root/>");
        assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithMoreElements() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.startElement("", "node", "node", atts);
        builder.endElement("", "node", "node");
        builder.startElement("", "node", "node", atts);
        builder.endElement("", "node", "node");
        builder.endElement("", "root", "root");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root><node/><node/></root>");
        assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithText() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.characters("abcd".toCharArray(), 0, 4);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root>abcd</root>");
        assertXMLEqual(document, builder.getDocument());
    }

    /*public void testBuilderWithNS()  throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startPrefixMapping("", "http://xml.apache.org");
        builder.startElement("", "root", "root", atts);
        builder.endElement("", "node", "node");
        builder.endPrefixMapping("");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root xmlns=\"http://xml.apache.org\"/>");
        assertXMLEqual(document, builder.getDocument());
    }*/

    /*public void testBuilderWithPrefix()  throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startPrefixMapping("bla", "http://xml.apache.org");
        builder.startElement("http://xml.apache.org", "root", "bla:root", atts);
        builder.endElement("http://xml.apache.org", "root", "bla:root");
        builder.endPrefixMapping("bla");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<bla:root xmlns:bla=\"http://xml.apache.org\"/>");
        assertXMLEqual(document, builder.getDocument());
    }*/

    /*public void testBuilderWithNSError()  throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();

        try {
            builder.startDocument();
            builder.startPrefixMapping("bla", "http://xml.apache.org");
            atts.addAttribute( "", "bla", "xmlns:bla", "CDATA", "http://xml.apache.org");
            builder.startElement("http://xml.apache.org", "root", "bla:root", atts);
            builder.endElement("http://xml.apache.org", "root", "bla:root");
            builder.endPrefixMapping("bla");
            builder.endDocument();

            fail("DOMBuilder should throw exception because of permitted attribute");
        } catch (Exception e) {
            // nothing
        }
    }*/

    public void testBuilderWithComments() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement("", "root", "root", atts);
        builder.comment("abcd".toCharArray(), 0, 4);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<root><!--abcd--></root>");

        assertXMLEqual(document, builder.getDocument());
    }

    public void testBuilderWithCommentWithinDocType() throws Exception {
        AttributesImpl atts = new AttributesImpl();

        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startDTD("skinconfig", null, null);
        builder.comment("abcd".toCharArray(), 0, 4);
        builder.endDTD();
        builder.startElement("", "root", "root", atts);
        builder.endElement("", "root", "node");
        builder.endDocument();

        Document document = XMLUnit.buildControlDocument("<!DOCTYPE skinconfig [<!--abcd-->]><root></root>");

        print(document);
        print(builder.getDocument());

        assertXMLEqual(document, builder.getDocument());
    }

    public final void print(Document document) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try
        {
          javax.xml.transform.Transformer serializer = factory.newTransformer();
          serializer.transform(new DOMSource(document), new StreamResult(System.out));
          System.out.println();
        }
        catch (TransformerException te)
        {
          te.printStackTrace();
        }
    }


    public void testTestFacility() throws Exception {
        Document document = XMLUnit.getControlParser().newDocument();
        Element elemA = document.createElement("root");
        document.appendChild(elemA);

        Document oneElementDocument = XMLUnit.buildControlDocument("<root/>");
        assertXMLEqual(oneElementDocument, document);

        document = XMLUnit.getControlParser().newDocument();
        elemA = document.createElement("node");
        document.appendChild(elemA);

        oneElementDocument = XMLUnit.buildControlDocument("<root/>");
        assertXMLNotEqual(oneElementDocument, document);
    }

    public void testStreamer() throws Exception {

        Document document = XMLUnit.getControlParser().newDocument();
        Element elemA = document.createElement("root");
        document.appendChild(elemA);

        Element elemB = document.createElement("node");
        elemA.appendChild(elemB);
        
        elemB = document.createElement("node");
        elemA.appendChild(elemB);

        DOMBuilder builder = new DOMBuilder();
        DOMStreamer streamer = new DOMStreamer(builder);

        streamer.stream(document);

        document = builder.getDocument();

        Document moreElementDocument = XMLUnit.buildControlDocument("<root><node/><node/></root>");
        assertXMLEqual(moreElementDocument, document);
    }

    /*public void testStreamerWithNS() throws Exception {

        Document document = XMLUnit.getControlParser().newDocument();
        Element elemA = document.createElementNS("http://xml.apache.org", "root");
        document.appendChild(elemA);

        Element elemB = document.createElementNS("http://xml.apache.org", "node");
        elemA.appendChild(elemB);

        elemB = document.createElementNS("http://xml.apache.org", "node");
        elemA.appendChild(elemB);

        DOMBuilder builder = new DOMBuilder();
        DOMStreamer streamer = new DOMStreamer(builder);

        streamer.stream(document);
    
        document = builder.getDocument();
    
        Document moreElementDocument = XMLUnit.buildControlDocument("<root xmlns=\"http://xml.apache.org\"><node/><node/></root>");
        assertXMLEqual(moreElementDocument, document);
    }*/
}
