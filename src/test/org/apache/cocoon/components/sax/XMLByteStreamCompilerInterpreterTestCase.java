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
