/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;

import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.Composable;
import org.apache.avalon.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

/**
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: ContentAggregator.java,v 1.1.2.8 2001-04-24 22:26:50 giacomo Exp $
 */

public class ContentAggregator extends ContentHandlerWrapper
        implements Generator, Cacheable, Composable {
    /** the current sitemap */
    protected Sitemap sitemap;

    /** the root element of the aggregated content */
    protected String rootElement;

    /** the namespace of the root element */
    protected String rootElementNS;

    /** the namespace prefix of the root element */
    protected String rootElementNSPrefix;

    /** the parts */
    protected ArrayList parts = new ArrayList();

    /** The current <code>Environment</code>. */
    protected Environment environment;

    /** The current <code>EntityResolver</code>. */
    protected EntityResolver resolver;

    /** The current <code>Map</code> objectModel. */
    protected Map objectModel;

    /** The current <code>Parameters</code>. */
    protected Parameters parameters;

    /** The source URI associated with the request or <b>null</b>. */
    protected String source;

    /** The <code>XMLConsumer</code> receiving SAX events. */
    protected XMLConsumer xmlConsumer;

    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler;

    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler;

    /** The <code>ComponentManager</code> */
    protected ComponentManager manager;

    /** Holds all collected <code>EventPipeline</code>s */
    private ArrayList partEventPipelines = new ArrayList();

    /** Stacks namespaces during processing */
    private ArrayList currentNS = new ArrayList();

    /** Indicates the position in the stack of the root element of the aggregated content */
    private int rootElementIndex;

    /**
     * Pass the <code>ComponentManager</code> to the <code>composer</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composable</code> uses.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        if (this.manager == null) {
            this.manager = manager;
        }
    }

    /** This object holds the part parts :) */
    private class Part {
        public String uri;
        public String element;
        public String namespace;
        public String prefix;
        boolean stripRootElement;
        public Part (String uri, String element, String namespace, String stripRoot, String prefix) {
            this.uri = uri;
            this.element = element;
            this.namespace = namespace;
            this.prefix = prefix;
            if (stripRoot.equals("yes") || stripRoot.equals("true")) {
                this.stripRootElement = true;
            } else {
                this.stripRootElement = false;
            }
        }
    }

    /**
     * generates the content
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        getLogger().debug("ContentAggregator: generating aggregated content");
        collectParts();
        this.documentHandler.startDocument();
        this.startElem(this.rootElementNS, this.rootElementNSPrefix, this.rootElement);
        try {
            for (int i = 0; i < this.partEventPipelines.size(); i++) {
                Part part = (Part)this.parts.get(i);
                this.rootElementIndex = (part.stripRootElement ? 0 : -1);
                String ns = part.namespace;
                String prefix = part.prefix;
                if (ns.equals("")) {
                    ns = this.getNS();
                    prefix = "";
                }
                if (!part.element.equals("")) {
                    this.startElem(ns, prefix, part.element);
                }
                EventPipeline ep = (EventPipeline)this.partEventPipelines.get(i);
                ((XMLProducer)ep).setConsumer(this);
                try {
                    this.environment.pushURI(part.uri);
                    ep.process(this.environment);
                } catch (Exception e) {
                    getLogger().error("ContentAggregator: cannot process event pipeline for URI " + part.uri, e);
                    throw new ProcessingException ("ContentAggregator: cannot process event pipeline for URI " + part.uri, e);
                } finally {
                    this.manager.release(ep);
                    this.environment.popURI();
                    if (!part.element.equals("")) {
                        this.endElem(prefix, part.element);
                    }
                }
            }
        } finally {
            this.endElem(this.rootElementNSPrefix, this.rootElement);
            this.documentHandler.endDocument();
        }
        getLogger().debug("ContentAggregator: finished aggregating content");
    }

    private void collectParts() throws ProcessingException {
        if (this.partEventPipelines.size() == 0) {
            EventPipeline eventPipeline = null;
            StreamPipeline pipeline = null;
            for (int i = 0; i < this.parts.size(); i++) {
                Part part = (Part)this.parts.get(i);
                getLogger().debug("ContentAggregator: collecting internal resource " + part.uri);
                try {
                    eventPipeline = (EventPipeline)this.manager.lookup(Roles.EVENT_PIPELINE);
                    this.partEventPipelines.add(eventPipeline);
                    pipeline = (StreamPipeline)this.manager.lookup(Roles.STREAM_PIPELINE);
                } catch (ComponentException cme) {
                    getLogger().error("ContentAggregator: could not lookup pipeline components", cme);
                    throw new ProcessingException ("could not lookup pipeline components", cme);
                }
                try {
                    pipeline.setEventPipeline(eventPipeline);
                } catch (Exception cme) {
                    getLogger().error("ContentAggregator: could not set event pipeline on stream pipeline", cme);
                    throw new ProcessingException ("could not set event pipeline on stream pipeline", cme);
                }
                try {
                    this.environment.pushURI(part.uri);
                    this.sitemap.process(this.environment, pipeline, eventPipeline);
                } catch (Exception cme) {
                    getLogger().error("ContentAggregator: could not process pipeline", cme);
                    throw new ProcessingException ("could not process pipeline", cme);
                } finally {
                    this.manager.release(pipeline);
                    this.environment.popURI();
                }
            }
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public long generateKey() {
        return 0;
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        return null;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setSitemap(Sitemap sitemap) {
        this.sitemap = sitemap;
    }

    public void setRootElement(String element, String namespace, String prefix) {
        this.rootElement = element;
        this.rootElementNS = namespace;
        this.rootElementNSPrefix = prefix;
        getLogger().debug("ContentAggregator: root element='" + element + "' ns='" + namespace + "' prefix='" + prefix + "'");
    }

    public void addPart(String uri, String element, String namespace, String stripRootElement, String prefix) {
        this.parts.add(new Part(uri, element, namespace, stripRootElement, prefix));
        getLogger().debug("ContentAggregator: part uri='" + uri + "' element='" + element + "' ns='" + namespace
                        + "' stripRootElement='" + stripRootElement + "' prefix='" + prefix + "'");
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.setContentHandler(consumer);
        this.xmlConsumer = consumer;
        this.contentHandler = consumer;
        this.lexicalHandler = consumer;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
     * Recycle the producer by removing references
     */
    public void recycle () {
        super.recycle();
        this.sitemap = null;
        this.resolver = null;
        this.objectModel = null;
        this.source = null;
        this.parameters = null;
        this.rootElement = null;
        this.rootElementNS = null;
        this.rootElementNSPrefix = null;
        this.parts.clear();
        this.environment = null;
        this.partEventPipelines.clear();
        this.currentNS.clear();
        this.xmlConsumer = null;
        this.contentHandler = null;
        this.lexicalHandler = null;
    }

    /**
     * Set the <code>EntityResolver</code>, object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        this.resolver=resolver;
        this.objectModel=objectModel;
        this.source=src;
        this.parameters=par;
    }

    private String pushNS(String ns) {
        currentNS.add(ns);
        return ns;
    }

    private String popNS() {
        int last = currentNS.size()-1;
        String ns = (String)currentNS.get(last);
        currentNS.remove(last);
        return ns;
    }

    private String getNS() {
        int last = currentNS.size()-1;
        return (String)currentNS.get(last);
    }

    private void startElem(String namespaceURI, String prefix, String name) throws SAXException {
        this.pushNS(namespaceURI);
        AttributesImpl attrs = new AttributesImpl();
        String qname = name;
        if (!namespaceURI.equals("")) {
            this.documentHandler.startPrefixMapping(prefix, namespaceURI);
            if (!prefix.equals("")) {
                attrs.addAttribute("", prefix, "xmlns:" + prefix, "CDATA", namespaceURI);
            } else {
                attrs.addAttribute("", "xmlns", "xmlns", "CDATA", namespaceURI);
            }
        }
        this.documentHandler.startElement(namespaceURI, name, name, attrs);
    }

    private void endElem(String prefix, String name) throws SAXException {
        String ns = this.popNS();
        this.documentHandler.endElement(ns, name, name);
        if (!ns.equals("")) {
            this.documentHandler.endPrefixMapping(prefix);
        }
    }

    /**
     * Ignore start and end document events
     */
    public void startDocument () throws SAXException {
    }

    public void endDocument () throws SAXException {
    }

    public void startElement (String namespaceURI, String localName,
                  String qName, Attributes atts) throws SAXException {
        String ns = namespaceURI;
        if (ns.equals("")) {
            ns = (String)this.getNS();
        }
        this.pushNS(ns);
        if (rootElementIndex != 0) {
            this.documentHandler.startElement(ns, localName, qName, atts);
        } else {
            rootElementIndex = currentNS.size();
            getLogger().debug("ContentAggregator: skipping root element start event " + rootElementIndex);
        }
    }

    public void endElement (String namespaceURI, String localName,
                  String qName) throws SAXException {
        if (rootElementIndex != currentNS.size()) {
            this.documentHandler.endElement((String)this.popNS(), localName, qName);
        } else {
            this.popNS();
            getLogger().debug("ContentAggregator: ignoring root element end event " + rootElementIndex);
        }
    }
}
