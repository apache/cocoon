/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.faces.renderkit;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * JSF Response Writer writing SAX events into the XMLConsumer
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class XMLResponseWriter extends ResponseWriter {
    private String contentType;
    private String encoding;
    private XMLConsumer xmlConsumer;

    private boolean closeStart;
    private String name;
    private AttributesImpl attrs;

    private char charHolder[];


    public XMLResponseWriter(XMLConsumer xmlConsumer, String contentType, String encoding) throws FacesException {
        this.contentType = contentType != null ? contentType : "application/xml";
        this.encoding = encoding;
        this.xmlConsumer = xmlConsumer;

        this.attrs = new AttributesImpl();
        this.charHolder = new char[1];
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharacterEncoding() {
        return encoding;
    }

    public void startDocument() throws IOException {
    }

    public void endDocument() throws IOException {
        closeStartIfNecessary();
    }

    public void flush() throws IOException {
        closeStartIfNecessary();
    }

    public void startElement(String name, UIComponent component) throws IOException {
        closeStartIfNecessary();
        this.name = name;
        this.closeStart = true;
    }

    public void endElement(String name) throws IOException {
        closeStartIfNecessary();
        try {
            this.xmlConsumer.endElement("", name, name);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public void writeAttribute(String name, Object value, String componentPropertyName) throws IOException {
        if (value == null) {
            this.attrs.addAttribute("", name, name, "CDATA", "");
        } else if (Boolean.TRUE.equals(value)) {
            this.attrs.addAttribute("", name, name, "CDATA", name);
        } else {
            this.attrs.addAttribute("", name, name, "CDATA", value.toString());
        }
    }

    public void writeURIAttribute(String name, Object value, String componentPropertyName) throws IOException {
        this.attrs.addAttribute("", name, name, "CDATA", value.toString());
    }

    public void writeComment(Object comment) throws IOException {
        closeStartIfNecessary();
        char[] ch = comment.toString().toCharArray();
        try {
            this.xmlConsumer.comment(ch, 0, ch.length);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public void writeText(Object text, String componentPropertyName) throws IOException {
        closeStartIfNecessary();
        char[] ch = text.toString().toCharArray();
        try {
            this.xmlConsumer.characters(ch, 0, ch.length);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public void writeText(char text) throws IOException {
        closeStartIfNecessary();
        charHolder[0] = text;
        try {
            this.xmlConsumer.characters(charHolder, 0, 1);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public void writeText(char text[]) throws IOException {
        closeStartIfNecessary();
        try {
            this.xmlConsumer.characters(text, 0, text.length);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public void writeText(char text[], int off, int len) throws IOException {
        closeStartIfNecessary();
        try {
            this.xmlConsumer.characters(text, off, len);
        } catch (SAXException e) {
            throw new CascadingIOException("SAXException", e);
        }
    }

    public ResponseWriter cloneWithWriter(Writer writer) {
        if (!(writer instanceof XMLResponseWriter)) {
            throw new IllegalArgumentException("Expected XMLResponseWriter got " + writer);
        }
        return new XMLResponseWriter(((XMLResponseWriter) writer).xmlConsumer,
                                     getContentType(),
                                     getCharacterEncoding());
    }

    private void closeStartIfNecessary() throws IOException {
        if (closeStart) {
            try {
                this.xmlConsumer.startElement("", this.name, this.name, this.attrs);
            } catch (SAXException e) {
                throw new CascadingIOException("SAXException", e);
            }
            this.attrs.clear();
            closeStart = false;
        }
    }

    public void close() throws IOException {
        closeStartIfNecessary();
    }

    public void write(char cbuf) throws IOException {
        closeStartIfNecessary();
        writeText(cbuf);
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        closeStartIfNecessary();
        writeText(cbuf);
    }

    public void write(int c) throws IOException {
        closeStartIfNecessary();
        writeText((char) c);
    }

    public void write(String str) throws IOException {
        closeStartIfNecessary();
        writeText(str.toCharArray());
    }

    public void write(String str, int off, int len) throws IOException {
        closeStartIfNecessary();
        writeText(str.toCharArray(), off, len);
    }
}
