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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * Logging content handler logs all events going through to the logger.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: LoggingContentHandler.java,v 1.2 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class LoggingContentHandler extends AbstractLogEnabled implements ContentHandler {

    /**
     * All debug messages from this handler are prefixed with this id.
     */
    String id;

    /** The current <code>ContentHandler</code>. */
    ContentHandler contentHandler;

    /**
     * Creates new <code>LoggingContentHandler</code> with specified
     * <code>id</code> and destination <code>contentHandler</code>.
     */
    public LoggingContentHandler(String id, ContentHandler contentHandler) {
        this.id = id;
        this.contentHandler = contentHandler;
    }

    public void setDocumentLocator(Locator locator) {
        log("setDocumentLocator", "");
        contentHandler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        log("startDocument", "");
        this.contentHandler.startDocument();
    }

    public void endDocument() throws SAXException {
        log ("endDocument", "");
        this.contentHandler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        log ("startPrefixMapping", "prefix="+prefix+",uri="+uri);
        this.contentHandler.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        log ("endPrefixMapping", "prefix="+prefix);
        this.contentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        log ("startElement", "uri="+uri+",local="+loc+",raw="+raw);
        for (int i = 0; i < a.getLength(); i++) {
            log ("            ", Integer.toString(i + 1)
                 + ". uri=" + a.getURI(i)
                 + ",local=" + a.getLocalName(i)
                 + ",qname=" + a.getQName(i)
                 + ",type=" + a.getType(i)
                 + ",value=" + a.getValue(i));
        }
        this.contentHandler.startElement(uri,loc,raw,a);
    }


    public void endElement(String uri, String loc, String qname) throws SAXException {
        log ("endElement", "uri="+uri+",local="+loc+",qname="+qname);
        this.contentHandler.endElement(uri,loc,qname);
    }

    public void characters(char ch[], int start, int len) throws SAXException {
        log ("characters", new String(ch,start,len));
        this.contentHandler.characters(ch,start,len);
    }

    public void ignorableWhitespace(char ch[], int start, int len) throws SAXException {
        log ("ignorableWhitespace", new String(ch,start,len));
        this.contentHandler.ignorableWhitespace(ch,start,len);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        log ("processingInstruction", "target="+target+",data="+data);
        this.contentHandler.processingInstruction(target,data);
    }

    public void skippedEntity(String name) throws SAXException {
        log ("skippedEntity", "name="+name);
        this.contentHandler.skippedEntity(name);
    }

    private void log(String location, String description) {
        StringBuffer logEntry = new StringBuffer();
        logEntry.append(id);
        logEntry.append("[");
        logEntry.append(location);
        logEntry.append("] ");
        logEntry.append(description);
        logEntry.append("\n");
        getLogger().debug(logEntry.toString());
        // System.out.print(logEntry.toString());
    }
}
