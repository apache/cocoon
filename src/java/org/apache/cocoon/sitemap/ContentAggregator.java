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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This generator implements the sitemap content aggregation.
 * It combines several parts into one big XML document which is streamed
 * into the pipeline.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ContentAggregator.java,v 1.7 2004/03/28 20:51:24 antonio Exp $
 */
public class ContentAggregator
extends ContentHandlerWrapper
implements Generator, CacheableProcessingComponent, Serviceable {

    /** the root element of the aggregated content */
    protected Element rootElement;

    /** the parts */
    protected ArrayList parts = new ArrayList();

    /** Empty Attributes */
    private AttributesImpl emptyAttrs = new AttributesImpl();

    /** Indicates the position in the stack of the root element of the aggregated content */
    private int rootElementIndex;

    /** The element used for the current part */
    protected Element currentElement;

    /** The SourceResolver */
    protected SourceResolver resolver;

    /** The component manager */
    protected ServiceManager manager;
    
    /** This object holds the part parts :) */
    protected final class Part {
        public String uri;
        public Element element;
        public Source source;
        boolean stripRootElement;

        public Part(String uri, Element element, String stripRoot) {
            this.uri = uri;
            this.element = element;
            this.stripRootElement = BooleanUtils.toBoolean(stripRoot);
        }
    }

    /** This object holds an element definition */
    protected final class Element {
        public String namespace;
        public String prefix;
        public String name;

        public Element(String name, String namespace, String prefix) {
            this.namespace = namespace;
            this.prefix = prefix;
            this.name = name;
        }
    }

    /**
     * generates the content
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Generating aggregated content");
        }
        this.contentHandler.startDocument();
        this.startElem(this.rootElement);
        try {
            for (int i = 0; i < this.parts.size(); i++) {
                final Part part = (Part)this.parts.get(i);
                this.rootElementIndex = (part.stripRootElement ? -1 : 0);
                if (part.element != null) {
                    this.currentElement = part.element;
                    this.startElem(part.element);
                } else {
                    this.currentElement = this.rootElement;
                }

                try {
                    SourceUtil.parse(this.manager, part.source, this);
                } finally {
                    if (part.element != null) {
                        this.endElem(part.element);
                    }
                }
            }
        } finally {
            this.endElem(this.rootElement);
            this.contentHandler.endDocument();
        }
        getLogger().debug("Finished aggregating content");
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CA(")
                  .append(this.rootElement.prefix).append(':')
                  .append(this.rootElement.name).append('<')
                  .append(this.rootElement.namespace).append(">)");
            Source current;
            for (int i = 0; i < this.parts.size(); i++) {
                final Part part = (Part)this.parts.get(i);
                current = part.source;
                if (part.element == null) {
                    buffer.append("P=")
                          .append(part.stripRootElement).append(':')
                          .append(current.getURI()).append(';');
                } else {
                    buffer.append("P=")
                          .append(part.element.prefix).append(':')
                          .append(part.element.name)
                          .append('<').append(part.element.namespace).append(">:")
                          .append(part.stripRootElement).append(':')
                          .append(current.getURI()).append(';');
                }
            }
            return buffer.toString();
        } catch (Exception e) {
            getLogger().error("Could not generateKey", e);
            return null;
        }
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        try {
            AggregatedValidity v = new AggregatedValidity();
            Source current;
            for (int i = 0; i < this.parts.size(); i++) {
                current = ((Part)this.parts.get(i)).source;
                SourceValidity sv = current.getValidity();
                if (sv == null) {
                    return null;
                } else {
                    v.add(sv);
                }
            }
            return v;
        } catch (Exception e) {
            getLogger().error("Could not getValidity", e);
            return null;
        }
    }

    /**
     * Set the root element. Please make sure that the parameters are not null!
     */
    public void setRootElement(String element, String namespace, String prefix) {
        this.rootElement = new Element(element,
                                       namespace,
                                       prefix);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Root element='" + element +
                              "' ns='" + namespace + "' prefix='" + prefix + "'");
        }
    }

    /**
     * Add a part. Please make sure that the parameters are not null!
     */
    public void addPart(String uri,
                        String element,
                        String namespace,
                        String stripRootElement,
                        String prefix) {
        Element elem = null;
        if (!element.equals("")) {
            if (namespace.equals("")) {
                elem = new Element(element,
                                   this.rootElement.namespace,
                                   this.rootElement.prefix);
            } else {
                elem = new Element(element,
                                   namespace,
                                   prefix);
            }
        }
        this.parts.add(new Part(uri,
                                elem,
                                stripRootElement));
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Part uri='" + uri +
                              "' element='" + element + "' ns='" + namespace +
                              "' stripRootElement='" + stripRootElement + "' prefix='" + prefix + "'");
        }
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.setContentHandler(consumer);
        this.setLexicalHandler(consumer);
    }

    /**
     * Recycle the producer by removing references
     */
    public void recycle() {
        super.recycle();
        this.rootElement = null;
        for(int i=0; i<this.parts.size();i++) {
            final Part current = (Part)this.parts.get(i);
            if (current.source != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Releasing " + String.valueOf(current.source));
                }
                this.resolver.release(current.source);
            }
        }
        this.parts.clear();
        this.currentElement = null;
        this.resolver = null;
    }

    /**
     * Set the <code>SourceResolver</code>, object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;
        // get the Source for each part
        try {
            for(int i=0; i<this.parts.size();i++) {
                final Part current = (Part)this.parts.get(i);
                current.source = resolver.resolveURI(current.uri);
            }
        } catch (SourceException se) {
            throw SourceUtil.handle("Unable to resolve source.", se);
        }
    }

    /**
     * Private method generating startElement event for the aggregated parts
     * and the root element
     */
    private void startElem(Element element)
    throws SAXException {
        final String qname = (element.prefix.equals("")) ? element.name : element.prefix + ':' + element.name;
        if (!element.namespace.equals("")) {
            this.contentHandler.startPrefixMapping(element.prefix, element.namespace);
        }
        this.contentHandler.startElement(element.namespace, element.name, qname, this.emptyAttrs);
    }

    /**
     * Private method generating endElement event for the aggregated parts
     * and the root element
     */
    private void endElem(Element element) throws SAXException {
        final String qname = (element.prefix.equals("")) ? element.name : element.prefix + ':' + element.name;
        this.contentHandler.endElement(element.namespace, element.name, qname);
        if (!element.namespace.equals("")) {
            this.contentHandler.endPrefixMapping(element.prefix);
        }
    }

    /**
     * Ignore start and end document events
     */
    public void startDocument() throws SAXException {
    }

    /**
     * Ignore start and end document events
     */
    public void endDocument() throws SAXException {
    }

    /**
     * Override startElement() event to add namespace and prefix
     */
    public void startElement(String namespaceURI, String localName, String raw, Attributes atts)
    throws SAXException {
        this.rootElementIndex++;
        if (this.rootElementIndex == 0) {
            getLogger().debug("Skipping root element start event.");
            return;
        }
        if (namespaceURI == null || namespaceURI.equals("")) {
            final String qname = this.currentElement.prefix.equals("") ? localName : this.currentElement.prefix + ':' + localName;
            this.contentHandler.startElement(this.currentElement.namespace, localName, qname, atts);
        } else {
            this.contentHandler.startElement(namespaceURI, localName, raw, atts);
        }
    }

    /**
     * Override startElement() event to add namespace and prefix
     */
    public void endElement(String namespaceURI, String localName, String raw) throws SAXException {
        this.rootElementIndex--;
        if (this.rootElementIndex == -1) {
            getLogger().debug("Skipping root element end event.");
            return;
        }
        if (namespaceURI == null || namespaceURI.equals("")) {
            final String qname = this.currentElement.prefix.equals("") ? localName : this.currentElement.prefix + ':' + localName;
            this.contentHandler.endElement(this.currentElement.namespace, localName, qname);
        } else {
            this.contentHandler.endElement(namespaceURI, localName, raw);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }
}
