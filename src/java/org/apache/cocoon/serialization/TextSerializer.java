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
package org.apache.cocoon.serialization;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.CascadingIOException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Text serializer converts XML into plain text.
 * It omits all XML tags and writes only character events to the output.
 * Internally, text serializer uses XML serializer with {@link OutputKeys#METHOD}
 * set to <code>text</code>.
 *
 * <p>Input document must have at least one element - root element - which
 * should wrap all the text inside it.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: TextSerializer.java,v 1.6 2004/03/08 14:03:32 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Serializer
 * @x-avalon.lifestyle type=pooled
 */
public class TextSerializer extends AbstractTextSerializer {

    /**
     * Set to true after first XML element
     */
    private boolean hasRootElement;

    /**
     * Set to true after first XML element
     */
    private boolean hadNoRootElement;

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        this.format.put(OutputKeys.METHOD, "text");
    }

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        super.setOutputStream(out);
        try {
            TransformerHandler handler = this.getTransformerHandler();
            handler.getTransformer().setOutputProperties(format);
            handler.setResult(new StreamResult(this.output));
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
       } catch (Exception e) {
            final String message = "Cannot set TextSerializer outputstream";
            throw new CascadingIOException(message, e);
        }
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
            throws SAXException {
        this.hasRootElement = true;
        super.startElement(uri, loc, raw, a);
    }

    public void characters(char c[], int start, int len)
            throws SAXException {
        if (!this.hasRootElement) {
            this.hasRootElement = this.hadNoRootElement = true;
            getLogger().warn("Encountered text before root element. Creating <text> wrapper element.");
            super.startElement("", "text", "text", new AttributesImpl());
        }
        super.characters(c, start, len);
    }

    public void endDocument() throws SAXException {
        if (this.hadNoRootElement) {
            super.endElement("", "text", "text");
        }
        super.endDocument();
    }

    public void recycle() {
        super.recycle();
        this.hasRootElement = false;
        this.hadNoRootElement = false;
    }
}
