/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.xml.dom;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Testcase for DOMStreamer and DOMBuilder.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: DOMBuilderStreamerTestCase.java,v 1.3 2003/04/23 06:44:50 stephan Exp $
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
