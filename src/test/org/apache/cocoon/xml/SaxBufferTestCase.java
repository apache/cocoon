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
package org.apache.cocoon.xml;

import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.apache.cocoon.xml.dom.DOMBuilder;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;

/**
 * Testcase for SaxBuffer
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 */

public final class SaxBufferTestCase extends AbstractXMLTestCase {
    public SaxBufferTestCase(String s) {
        super(s);
    }

    public void testCompareDOM() throws Exception {
        DOMBuilder in = new DOMBuilder();
        generateSAX(in);

        SaxBuffer sb = new SaxBuffer();
        generateSAX(sb);

        DOMBuilder out = new DOMBuilder();
        sb.toSAX(out);

        assertXMLEqual(in.getDocument(), out.getDocument());
    }

    public void testStressLoop() throws Exception {
        SaxBuffer sb = new SaxBuffer();

        long loop = 50000;

        // simply consume documents
        long start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            generateSAX(sb);
            sb.recycle();
        }
        long stop = System.currentTimeMillis();

        double r = 1000*loop/(stop-start);
        System.out.println("consuming: "+ r + " documents per second");
    }

    public void testCompareToParsing() throws Exception {
        DOMBuilder in = new DOMBuilder();
        generateSAX(in);

        SAXParserFactory pfactory = SAXParserFactory.newInstance();
        SAXParser p = pfactory.newSAXParser();


        SaxBuffer b = new SaxBuffer();
        DefaultHandlerWrapper wrapper = new DefaultHandlerWrapper(b);
        ByteArrayInputStream bis = new ByteArrayInputStream(generateByteArray());

        long loop = 50000;

        long start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            b.recycle();
            bis.reset();
            p.parse(bis,wrapper);
        }
        long stop = System.currentTimeMillis();

        double r = 1000*loop/(stop-start);
        System.out.println("parsed:" + r + " documents per second");


        ContentHandler ch = new DefaultHandler();

        start = System.currentTimeMillis();
        for(int i=0;i<loop;i++) {
            b.toSAX(ch);
        }
        stop = System.currentTimeMillis();

        r = 1000*loop/(stop-start);
        System.out.println("recalling: " + r + " documents per second");
    }


}
