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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.ClassUtils;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @cocoon.sitemap.component.name tee
 * @cocoon.sitemap.component.logger sitemap.transformer.tee
 * @cocoon.sitemap.component.pooling.max  16
 *
 * The Teetransformer serializes SAX events as-is to the {@link org.apache.excalibur.source.ModifiableSource}
 * specified by its <code>src</code> parameter.
 * It does not in any way change the events.
 * <p/>
 * This transformer works just like the unix "tee" command and is useful for debugging
 * received XML streams.
 * <p/>
 * Usage:<br>
 * <pre>
 * &lt;map:transform type="tee" src="url"/&gt;
 * </pre>
 *
 * @version $Id$
 */
public class TeeTransformer extends AbstractSAXTransformer {

    /** the serializer */
    private TransformerHandler serializer;

    /** the transformer factory to use */
    private SAXTransformerFactory transformerFactory;

    /** the resolver */
    private SourceResolver resolver;
    
    private OutputStream os;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver,
     *      java.util.Map, java.lang.String,
     *      org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);

        Source source = null;
        try {
            this.resolver = resolver;
            source = this.resolver.resolveURI(src);
            String systemId = source.getURI();
            if (!(source instanceof ModifiableSource)) {
                throw new ProcessingException("Source '" + systemId + "' is not writeable.");
            }
            this.serializer = this.transformerFactory.newTransformerHandler();
            os = ((ModifiableSource) source).getOutputStream();
            this.serializer.setResult(new StreamResult(os));
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        } catch (TransformerConfigurationException e) {
            throw new ProcessingException(e);
        } catch (TransformerFactoryConfigurationError error) {
            throw new ProcessingException(error.getException());
        } finally {
            if (source != null) {
                this.resolver.release(source);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        String tFactoryClass = configuration.getChild("transformer-factory").getValue(null);
        if (tFactoryClass != null) {
            try {
                this.transformerFactory = (SAXTransformerFactory) ClassUtils
                    .newInstance(tFactoryClass);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using transformer factory " + tFactoryClass);
                }
            } catch (Exception e) {
                throw new ConfigurationException(
                    "Cannot load transformer factory " + tFactoryClass, e);
            }
        } else {
            this.transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

        }
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
        super.contentHandler.setDocumentLocator(locator);
        this.serializer.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument() throws SAXException {
        super.contentHandler.startDocument();
        this.serializer.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {
        super.contentHandler.endDocument();
        this.serializer.endDocument();
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                throw new CascadingRuntimeException("Error closing output stream.", e);
            }
        }
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.contentHandler.startPrefixMapping(prefix, uri);
        this.serializer.startPrefixMapping(prefix, uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        super.contentHandler.endPrefixMapping(prefix);
        this.serializer.endPrefixMapping(prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        super.contentHandler.startElement(uri, loc, raw, a);
        this.serializer.startElement(uri, loc, raw, a);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw) throws SAXException {
        super.contentHandler.endElement(uri, loc, raw);
        this.serializer.endElement(uri, loc, raw);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len) throws SAXException {
        super.contentHandler.characters(ch, start, len);
        this.serializer.characters(ch, start, len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len) throws SAXException {
        super.contentHandler.ignorableWhitespace(ch, start, len);
        this.serializer.ignorableWhitespace(ch, start, len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data) throws SAXException {
        super.contentHandler.processingInstruction(target, data);
        this.serializer.processingInstruction(target, data);
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name) throws SAXException {
        super.contentHandler.skippedEntity(name);
        this.serializer.skippedEntity(name);
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        super.lexicalHandler.startDTD(name, publicId, systemId);
        this.serializer.startDTD(name, publicId, systemId);
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD() throws SAXException {
        super.lexicalHandler.endDTD();
        this.serializer.endDTD();
    }

    /**
     * Report the beginning of an entity.
     */
    public void startEntity(String name) throws SAXException {
        super.lexicalHandler.startEntity(name);
        this.serializer.startEntity(name);
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name) throws SAXException {
        super.lexicalHandler.endEntity(name);
        this.serializer.endEntity(name);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA() throws SAXException {
        super.lexicalHandler.startCDATA();
        this.serializer.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() throws SAXException {
        super.lexicalHandler.endCDATA();
        this.serializer.endCDATA();
    }

    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char ch[], int start, int len) throws SAXException {
        super.lexicalHandler.comment(ch, start, len);
        this.serializer.comment(ch, start, len);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        this.serializer = null;
    }
}
