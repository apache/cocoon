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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.collections.ArrayStack;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AbstractXMLPipe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class acts as a proxy to a dynamically loaded<code>Generator</code>
 * delegating actual SAX event generation.
 * <p>
 * It has a single configuration item :
 * <code>&lt;autocomplete-documents&gttrue|false&lt;autocomplete-documents&gt;</code>
 * (default is <code>false</code>).
 * <p>
 * This tells the generator to automatically close all elements that weren't properly closed
 * by the XSP, such as when a <code>return</code> statement is used to prematurely end
 * processing. Activating this feature <em>sensibly increases CPU-usage</em> and should
 * therefore be used only if really needed (it's better to have clean XSP pages that don't
 * break abruptly generation flow).
 *
 * @version $Id$
 */
public class ServerPagesGenerator extends ServletGenerator
                                  implements CacheableProcessingComponent, Configurable {
    
    /**
     * The sitemap-defined server pages program generator
     */
    protected ProgramGenerator programGenerator;

    protected AbstractServerPage generator;

    /** The source */
    private Source inputSource;

    private CompletionPipe completionPipe;

    /**
     * Set the global service manager. This method sets the sitemap-defined
     * program generator
     *
     * @param manager The global service manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.programGenerator = (ProgramGenerator) manager.lookup(ProgramGenerator.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        boolean autoComplete = config.getChild("autocomplete-documents").getValueAsBoolean(false);
        if (autoComplete) {
            this.completionPipe = new CompletionPipe();
        }

        this.markupLanguage = config.getChild("markup-language").getValue(DEFAULT_MARKUP_LANGUAGE);
        this.programmingLanguage = config.getChild("programming-language").getValue(DEFAULT_PROGRAMMING_LANGUAGE);
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>null</code> if the component
     *         is currently not cacheable.
     */
    public Serializable getKey() {
        Object key = generator.getKey();
        if (key == null) {
            return this.inputSource.getURI();
        }
        return this.inputSource.getURI() + '-' + key;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        // VG: Input source's systemID is part of the key,
        // and need not be included into the validity.
        return generator.getValidity();
    }

    /**
     * The loaded generator's <code>MarkupLanguage</code>
     */
    protected String markupLanguage;

    /**
     * The loaded generator's <code>ProgrammingLanguage</code>
     */
    protected String programmingLanguage;

    /**
     * The default <code>MarkupLanguage</code>
     */
    public final static String DEFAULT_MARKUP_LANGUAGE = "xsp";

    /**
     * The default <code>ProgrammingLanguage</code>
     */
    public final static String DEFAULT_PROGRAMMING_LANGUAGE = "java";

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, par);

        String markupLanguage = this.parameters.getParameter(
                "markup-language", this.markupLanguage);
        String programmingLanguage = this.parameters.getParameter(
                "programming-language", this.programmingLanguage);

        try {
            this.inputSource = this.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
            //throw SourceUtil.handle(se);
        }

        try {
            this.generator = (AbstractServerPage) programGenerator.load(this.manager,
                    this.inputSource, markupLanguage, programmingLanguage, this.resolver);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            getLogger().warn("setup()", e);
            throw new ProcessingException(e.getMessage(), e);
        } catch (NoClassDefFoundError e) {
            // VG: Usually indicates that page invoked with the wrong case.
            // I.e., it was compiled as "my.xsp" and inoked as "My.xsp",
            // results in different class name and an error.
            getLogger().warn("Failed to load class: " + e);
            throw new ResourceNotFoundException(e.getMessage());
        }

        generator.setup(super.resolver, super.objectModel, super.source, super.parameters);
    }

    /**
     * Generate XML data. This method loads a server pages generator associated
     * with its (file) input source and delegates SAX event generator to it
     * taking care of "closing" any event left open by the loaded generator as a
     * result of its possible "premature" return (a common situation in server
     * pages)
     *
     * @exception IOException IO Error
     * @exception SAXException SAX event generation error
     * @exception ProcessingException Error during load/execution
     */
    public void generate() throws IOException, SAXException, ProcessingException {

        if (this.completionPipe != null) {
            generator.setConsumer(this.completionPipe);
            if (this.xmlConsumer != null) {
                this.completionPipe.setConsumer(this.xmlConsumer);
            } else {
                this.completionPipe.setContentHandler(this.contentHandler);
                this.completionPipe.setLexicalHandler(this.lexicalHandler);
            }
        } else {
            if (this.xmlConsumer != null) {
                generator.setConsumer(this.xmlConsumer);
            } else {
                generator.setContentHandler(this.contentHandler);
                generator.setLexicalHandler(this.lexicalHandler);
            }
        }

        // Fixes BUG#4062: Set document locator which is used by XIncludeTransformer
        org.xml.sax.helpers.LocatorImpl locator = new org.xml.sax.helpers.LocatorImpl();
        locator.setSystemId(this.inputSource.getURI());
        this.contentHandler.setDocumentLocator(locator);

        // Log exception and ensure that generator is released.
        try {
            generator.generate();
        } catch (IOException e) {
            getLogger().debug("IOException in generate()", e);
            throw e;
        } catch (SAXException e) {
            getLogger().debug("SAXException in generate()", e);
            throw e;
        } catch (ProcessingException e) {
            getLogger().debug("ProcessingException in generate()", e);
            throw e;
        } catch (Exception e) {
            getLogger().debug("Exception in generate()", e);
            throw new ProcessingException("Exception in ServerPagesGenerator.generate()", e);
        } finally {
            if (generator != null) {
                programGenerator.release(generator);
            }
            generator = null;
        }

        if (this.completionPipe != null) {
            this.completionPipe.flushEvents();
        }
    }

    /**
     * Recycle the generator by removing references
     */
    public void recycle() {
        if (this.generator != null) {
            this.programGenerator.release(this.generator);
            this.generator = null;
        }
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        if (this.completionPipe != null) {
            this.completionPipe.recycle();
            this.completionPipe = null;
        }
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.programGenerator);
            this.programGenerator = null;
        }
        super.dispose();
    }

    /* Completion pipe */

    // int values for event types
    private final static int DOCUMENT = 0;
    private final static int ELEMENT = 1;
    private final static int PREFIX_MAPPING = 2;
    private final static int CDATA = 3;
    private final static int DTD = 4;
    private final static int ENTITY = 5;

    // Integer equivalents to push on the stack
    private final static Integer DOCUMENT_OBJ = new Integer(DOCUMENT);
    private final static Integer ELEMENT_OBJ = new Integer(ELEMENT);
    private final static Integer PREFIX_MAPPING_OBJ = new Integer(PREFIX_MAPPING);
    private final static Integer CDATA_OBJ = new Integer(CDATA);
    private final static Integer DTD_OBJ = new Integer(DTD);
    private final static Integer ENTITY_OBJ = new Integer(ENTITY);

    public class CompletionPipe extends AbstractXMLPipe {

        /**
         * The SAX event stack. Used for "completing" pendind SAX events left "open"
         * by prematurely returning server pages generators
         */
        protected ArrayStack eventStack = new ArrayStack();

        /**
         * Receive notification of the beginning of a document.
         */
        public void startDocument() throws SAXException {
            super.startDocument();
            this.eventStack.push(DOCUMENT_OBJ);
        }

        /**
         * Receive notification of the end of a document.
         */
        public void endDocument() throws SAXException {
            this.eventStack.pop();
            super.endDocument();
        }

        /**
         * Receive notification of the beginning of an element.
         */
        public void startElement(String namespaceURI, String localName, String rawName, Attributes atts)
                throws SAXException {
            super.startElement(namespaceURI, localName, rawName, atts);
            this.eventStack.push(rawName);
            this.eventStack.push(localName);
            this.eventStack.push(namespaceURI);
            this.eventStack.push(ELEMENT_OBJ);
        }

        /**
         * Receive notification of the end of an element.
         */
        public void endElement(String namespaceURI, String localName, String rawName)
                throws SAXException {
            this.eventStack.pop(); // ELEMENT_OBJ
            this.eventStack.pop(); // namespaceURI
            this.eventStack.pop(); // localName
            this.eventStack.pop(); // rawName
            super.endElement(namespaceURI, localName, rawName);
        }

        /**
         * Begin the scope of a prefix-URI Namespace mapping.
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            super.startPrefixMapping(prefix, uri);
            this.eventStack.push(prefix);
            this.eventStack.push(PREFIX_MAPPING_OBJ);
        }

        /**
         * End the scope of a prefix-URI mapping.
         */
        public void endPrefixMapping(String prefix) throws SAXException {
            this.eventStack.pop(); // PREFIX_MAPPING_OBJ
            this.eventStack.pop(); // prefix
            super.endPrefixMapping(prefix);
        }

        public void startCDATA() throws SAXException {
            super.startCDATA();
            this.eventStack.push(CDATA_OBJ);
        }

        public void endCDATA() throws SAXException {
            this.eventStack.pop();
            super.endCDATA();
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
            super.startDTD(name, publicId, systemId);
            this.eventStack.push(DTD_OBJ);
        }

        public void endDTD() throws SAXException {
            this.eventStack.pop();
            super.endDTD();
        }

        public void startEntity(String name) throws SAXException {
            super.startEntity(name);
            this.eventStack.push(name);
            this.eventStack.push(ENTITY_OBJ);
        }

        public void endEntity(String name) throws SAXException {
            this.eventStack.pop(); // ENTITY_OBJ
            this.eventStack.pop(); // name
            super.endEntity(name);
        }

        public void flushEvents() throws SAXException {

            if (this.getLogger().isWarnEnabled()) {
                if (this.eventStack.size() > 0) {
                    this.getLogger().warn("Premature end of document generated by " + inputSource.getURI());
                }
            }

            // End any started events in case of premature return
            while (this.eventStack.size() != 0) {
                int event = ((Integer) eventStack.pop()).intValue();

                switch (event) {
                    case DOCUMENT:
                        super.endDocument();
                        break;

                    case ELEMENT:
                        String namespaceURI = (String) eventStack.pop();
                        String localName = (String) eventStack.pop();
                        String rawName = (String) eventStack.pop();
                        super.endElement(namespaceURI, localName, rawName);
                        break;

                    case PREFIX_MAPPING:
                        super.endPrefixMapping((String) eventStack.pop());
                        break;

                    case CDATA:
                        super.endCDATA();
                        break;

                    case DTD:
                        super.endDTD();
                        break;

                    case ENTITY:
                        super.endEntity((String) eventStack.pop());
                        break;
                }
            }
        }

        public void recycle() {
            this.eventStack.clear();
            super.recycle();
        }
    }
}
