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
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Map;

/**
 * An Abstract DOM Transformer, for use when a transformer needs a DOM-based
 * view of the document.
 * Subclass this interface and implement <code>transform(Document doc)</code>.
 * If you need a ComponentManager there is an instance variable
 * <code>manager</code> for use.
 *
 * @author <a href="mailto:rossb@apache.org">Ross Burton</a>
 * @author <a href="mailto:brobertson@mta.ca">Bruce G. Robertson</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: AbstractDOMTransformer.java,v 1.2 2004/03/05 13:02:59 bdelacretaz Exp $
 */
public abstract class AbstractDOMTransformer extends AbstractTransformer
        implements Transformer, DOMBuilder.Listener, Composable, Disposable, Recyclable {

    /**
     *  The SAX entity resolver
     */
    protected SourceResolver resolver;

    /**
     *  The request object model
     */
    protected Map objectModel;

    /**
     *  The URI requested
     */
    protected String source;

    /**
     *  Parameters in the sitemap
     */
    protected Parameters parameters;

    /**
     * A <code>ComponentManager</code> which is available for use.
     */
    protected ComponentManager manager;

    /**
     * The <code>DOMBuilder</code> used to build DOM tree out of
     *incoming SAX events.
     */
    protected DOMBuilder builder;


    public AbstractDOMTransformer() {
        super();
        this.builder = new DOMBuilder(this);
    }

    /**
     * Set the component manager.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     *
     * If you wish to process the parameters, override this method, call
     * <code>super()</code> and then add your code.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {

        this.resolver = resolver;
        this.objectModel = objectModel;
        this.source = src;
        this.parameters = par;
    }

    /**
     * Recycle the component.
     */
    public void recycle() {
        this.resolver = null;
        this.source = null;
        this.objectModel = null;
        this.parameters = null;
        this.builder.recycle();
    }

    /**
     * dispose
     */
    public void dispose() {
        this.builder = null;
        this.manager = null;
        this.builder = null;
    }

    /**
     * This method is called when the Document is finished.
     * @param doc The DOM Document object representing this SAX stream
     * @see org.apache.cocoon.xml.dom.DOMBuilder.Listener
     */
    public void notify(Document doc) throws SAXException {
        // Call the user's transform method
        Document newdoc = transform(doc);

        // Now we stream the resulting DOM tree down the pipe
        DOMStreamer s = new DOMStreamer(contentHandler, lexicalHandler);
        s.stream(newdoc);
    }

    /**
     * Transform the specified DOM, returning a new DOM to stream down the pipeline.
     * @param doc The DOM Document representing the SAX stream
     * @return A DOM Document to stream down the pipeline
     */
    protected abstract Document transform(Document doc);


    //
    // SAX Methods. Send incoming SAX events to the DOMBuilder.
    //

    public void setDocumentLocator(Locator locator) {
        builder.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        builder.startDocument();
    }

    public void endDocument() throws SAXException {
        builder.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        builder.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        builder.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
            throws SAXException {
        builder.startElement(uri, loc, raw, a);
    }

    public void endElement(String uri, String loc, String raw)
            throws SAXException {
        builder.endElement(uri, loc, raw);
    }

    public void characters(char c[], int start, int len)
            throws SAXException {
        builder.characters(c, start, len);
    }

    public void ignorableWhitespace(char c[], int start, int len)
            throws SAXException {
        builder.ignorableWhitespace(c, start, len);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        builder.processingInstruction(target, data);
    }

    public void skippedEntity(String name)
            throws SAXException {
        builder.skippedEntity(name);
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        builder.startDTD(name, publicId, systemId);
    }

    public void endDTD()
            throws SAXException {
        builder.endDTD();
    }

    public void startEntity(String name)
            throws SAXException {
        builder.startEntity(name);
    }

    public void endEntity(String name)
            throws SAXException {
        builder.endEntity(name);
    }

    public void startCDATA()
            throws SAXException {
        builder.startCDATA();
    }

    public void endCDATA()
            throws SAXException {
        builder.endCDATA();
    }

    public void comment(char ch[], int start, int len)
            throws SAXException {
        builder.comment(ch, start, len);
    }
}
