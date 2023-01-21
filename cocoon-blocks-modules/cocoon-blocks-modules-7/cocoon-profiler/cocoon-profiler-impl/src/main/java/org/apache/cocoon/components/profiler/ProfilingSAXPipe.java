/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.profiler;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLPipe;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ProfilingSAXPipe implements XMLPipe {

	private static Map pipes = new WeakHashMap();
	
    private XMLConsumer consumer;

    // Data of the profile
    private ProfilerData data;

    // Index of the component
    private int index;

    // Start time
    private long time;
    private boolean inside = false;

    // Time difference
    private long total;

    private XMLByteStreamCompiler serializer;

    /**
     * Setup this XMLPipe.
     *
     * @param index Index of the component.
     * @param data Data of the profile.
     */
    public void setup(int index, ProfilerData data) {
        this.index = index;
        this.data = data;
        this.serializer = new XMLByteStreamCompiler();
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    private void entered() {
    	assert(!this.inside);
    	ProfilingSAXPipe other = (ProfilingSAXPipe) pipes.get(this.data);
    	assert(other != null);
    	if (other != this) other.exited();
    	this.inside = true;
    	this.time = System.currentTimeMillis();
    }
    
    private void exited() {
    	long current = System.currentTimeMillis();
    	if (!this.inside) {
        	ProfilingSAXPipe other = (ProfilingSAXPipe) pipes.get(this.data);
    		if (other != null) {
    			this.time = other.getTime();
    		} else {
    			this.time = current;
    		}
    	}
    	this.total += current - this.time;
    	this.time = current;
    	pipes.put(this.data, this);
    }
    
    public void startDocument() throws SAXException {
    	exited();
        this.serializer.startDocument();
        this.consumer.startDocument();
        entered();
    }

    public void endDocument() throws SAXException {
    	exited();
        this.serializer.endDocument();
        this.consumer.endDocument();
        
        if (this.index != -1)
            this.data.setProcessingTime(this.index, this.total);

        // push the content of the buffer through the next component
        Object fragment = this.serializer.getSAXFragment();

        if (this.index != -1)
            this.data.setSAXFragment(this.index, fragment);
    }

    public void setDocumentLocator(Locator locator) {
    	exited();
        this.serializer.setDocumentLocator(locator);
        this.consumer.setDocumentLocator(locator);
        entered();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    	exited();
        this.serializer.startPrefixMapping(prefix, uri);
        this.consumer.startPrefixMapping(prefix, uri);
        entered();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    	exited();
        this.serializer.endPrefixMapping(prefix);
        this.consumer.endPrefixMapping(prefix);
        entered();
    }

    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
    	exited();
        this.serializer.startElement(uri, loc, raw, a);
        this.consumer.startElement(uri, loc, raw, a);
        entered();
    }

    public void endElement(String uri, String loc, String raw) throws SAXException {
    	exited();
        this.serializer.endElement(uri, loc, raw);
        this.consumer.endElement(uri, loc, raw);
        entered();
    }

    public void characters(char c[], int start, int len) throws SAXException {
    	exited();
        this.serializer.characters(c, start, len);
        this.consumer.characters(c, start, len);
        entered();
    }

    public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
    	exited();
        this.serializer.ignorableWhitespace(c, start, len);
        this.consumer.ignorableWhitespace(c, start, len);
        entered();
    }

    public void processingInstruction(String target, String data) throws SAXException {
    	exited();
        this.serializer.processingInstruction(target, data);
        this.consumer.processingInstruction(target, data);
        entered();
    }

    public void skippedEntity(String name) throws SAXException {
    	exited();
        this.serializer.skippedEntity(name);
        this.consumer.skippedEntity(name);
        entered();
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    	exited();
        this.serializer.startDTD(name, publicId, systemId);
        this.consumer.startDTD(name, publicId, systemId);
        entered();
    }

    public void endDTD() throws SAXException {
    	exited();
        this.serializer.endDTD();
        this.consumer.endDTD();
        entered();
    }

    public void startEntity(String name) throws SAXException {
    	exited();
        this.serializer.startEntity(name);
        this.consumer.startEntity(name);
        entered();
    }

    public void endEntity(String name) throws SAXException {
    	exited();
        this.serializer.endEntity(name);
        this.consumer.endEntity(name);
        entered();
    }

    public void startCDATA() throws SAXException {
    	exited();
        this.serializer.startCDATA();
        this.consumer.startCDATA();
        entered();
    }

    public void endCDATA() throws SAXException {
    	exited();
        this.serializer.endCDATA();
        this.consumer.endCDATA();
        entered();
    }

    public void comment(char ch[], int start, int len) throws SAXException {
    	exited();
        this.serializer.comment(ch, start, len);
        this.consumer.comment(ch, start, len);
        entered();
    }

	public boolean isInside() {
		return inside;
	}

	public long getTime() {
		return time;
	}	

}
