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
package org.apache.butterfly.serialization;

import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.butterfly.xml.AbstractXMLPipe;
import org.apache.butterfly.xml.xslt.TraxTransformerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Text serializer converts XML into plain text.
 * It omits all XML tags and writes only character events to the output.
 * Internally, text serializer uses XML serializer with {@link OutputKeys#METHOD}
 * set to <code>text</code>.
 *
 * <p>Input document must have at least one element - root element - which
 * should wrap all the text inside it.
 *
 * @version CVS $Id$
 */
public class TextSerializer extends AbstractXMLPipe implements Serializer {

    protected static final Log logger = LogFactory.getLog(TextSerializer.class);
    protected OutputStream output;
    private Map objectModel;
    protected TraxTransformerFactory transformerFactory;

    /**
     * Set to true after first XML element
     */
    private boolean hasRootElement;

    /**
     * Set to true after first XML element
     */
    private boolean hadNoRootElement;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();

    public TextSerializer() {
        this.format.put(OutputKeys.METHOD, "text");
    }

    /**
     * @param transformerFactory The transformerFactory to set.
     */
    public void setTraxTransformerFactory(TraxTransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }
    
    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream output) {
        this.output = output;
        TransformerHandler handler = this.transformerFactory.getTransformerHandler();
        handler.getTransformer().setOutputProperties(format);
        handler.setResult(new StreamResult(this.output));
        this.setContentHandler(handler);
        this.setLexicalHandler(handler);
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
            logger.warn("Encountered text before root element. Creating <text> wrapper element.");
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

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#getEncoding()
     */
    public String getEncoding() {
        return (String) format.get(OutputKeys.ENCODING);
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#getMimeType()
     */
    public String getMimeType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#shouldSetContentLength()
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#setObjectModel(java.util.Map)
     */
    public void setObjectModel(Map objectModel) {
        this.objectModel = objectModel;
    }
    
    public void setEncoding(String encoding) {
        format.put(OutputKeys.ENCODING, encoding);
    }
}
