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
package org.apache.cocoon.components.profiler;

import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLPipe;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This SAX connector measures time taken by each Sitemap component. This
 * class use the XMLSerializer/Interpreter to buffer the output, and to
 * seperate the measurement of the time. The SAX fragments were also stored
 * into the ProfilerData.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @version CVS $Id: ProfilingXMLPipe.java,v 1.2 2003/03/20 15:04:14 stephan Exp $
 */
public class ProfilingXMLPipe implements XMLPipe {

    private XMLConsumer consumer;

    // Data of the profile
    private ProfilerData data;

    // Index of the component
    private int index;

    // Start time
    private long time;

    // Time difference
    private long total;

    private XMLDeserializer deserializer;
    private XMLSerializer serializer;

    /**
     * Setup this XMLPipe.
     *
     * @param index Index of the component.
     * @param data Data of the profile.
     */
    public void setup(int index, ProfilerData data) {
        this.index = index;
        this.data = data;

        // FIXME Retrieve components from the CM
        this.deserializer = new XMLByteStreamInterpreter();
        this.serializer = new XMLByteStreamCompiler();
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public void startDocument() throws SAXException {
        this.time = System.currentTimeMillis(); // Startup time

        this.serializer.startDocument();
    }

    public void endDocument() throws SAXException {
        this.total = System.currentTimeMillis() - this.time;

        this.serializer.endDocument();
        if (this.index != -1)
            this.data.setProcessingTime(this.index, this.total);

        // push the content of the buffer through the next component
        Object fragment = this.serializer.getSAXFragment();

        if (this.index != -1)
            this.data.setSAXFragment(this.index, fragment);

        this.deserializer.setConsumer(this.consumer);

        this.time = System.currentTimeMillis(); // Startup time
        this.deserializer.deserialize(fragment);
        this.total = System.currentTimeMillis() - this.time;

        if ((this.index != -1) && (this.index==(this.data.getCount()-2)))
            this.data.setProcessingTime(this.index+1, this.total);
    }

    public void setDocumentLocator(Locator locator) {
        this.serializer.setDocumentLocator(locator);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.serializer.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.serializer.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        this.serializer.startElement(uri, loc, raw, a);
    }

    public void endElement(String uri, String loc, String raw) throws SAXException {
        this.serializer.endElement(uri, loc, raw);
    }

    public void characters(char c[], int start, int len) throws SAXException {
        this.serializer.characters(c, start, len);
    }

    public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
        this.serializer.ignorableWhitespace(c, start, len);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.serializer.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        this.serializer.skippedEntity(name);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.serializer.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        this.serializer.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        this.serializer.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        this.serializer.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        this.serializer.startCDATA();
    }

    public void endCDATA() throws SAXException {
        this.serializer.endCDATA();
    }

    public void comment(char ch[], int start, int len) throws SAXException {
        this.serializer.comment(ch, start, len);
    }
}
