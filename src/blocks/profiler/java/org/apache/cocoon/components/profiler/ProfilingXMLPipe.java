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
 * @version CVS $Id: ProfilingXMLPipe.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
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
