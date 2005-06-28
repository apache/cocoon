/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.i18n;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Implementation of <code>Bundle</code> interface for XML resources. Represents a
 * single XML message bundle.
 *
 * XML format for this resource bundle implementation is the following:
 * <pre>
 * &lt;catalogue xml:lang="en"&gt;
 *   &lt;message key="key1"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   &lt;message key="key2"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   ...
 * &lt;/catalogue&gt;
 * </pre>
 *
 * Value can be any well formed XML snippet and it will be cached by the key specified
 * in the attribute <code>key</code>. Objects returned by this {@link Bundle} implementation
 * are instances of the {@link ParamSaxBuffer} class.
 *
 * @author <a href="mailto:dev@cocoon.apache.org">Apache Cocoon Team</a>
 * @version $Id$
 */
public class XMLResourceBundle extends AbstractLogEnabled
                               implements Bundle, Serviceable {

    /**
     * Namespace for the Bundle markup
     */
    public static final String NS = "http://apache.org/cocoon/i18n/2.0";

    /**
     * XML bundle root element name
     */
    public static final String EL_CATALOGUE = "catalogue";

    /**
     * XML bundle message element name
     */
    public static final String EL_MESSAGE = "message";

    /**
     * XML bundle message element's key attribute name
     */
    public static final String AT_KEY = "key";

    /**
     * Source URI of the bundle
     */
    private String sourceURI;

    /**
     * Bundle validity
     */
    private SourceValidity validity;

    /**
     * Locale of the bundle
     */
    private Locale locale;

    /**
     * Parent of the current bundle
     */
    protected Bundle parent;

    /**
     * Objects stored in the bundle
     */
    protected HashMap values;

    /**
     * Service Manager
     */
    protected ServiceManager manager;

    /**
     * Processes XML bundle file and creates map of values
     */
    private static class SAXContentHandler implements ContentHandler {
        private Map values;
        private int state;
        private String namespace;
        private ParamSaxBuffer buffer;

        public SAXContentHandler(Map values) {
            this.values = values;
        }

        public void setDocumentLocator(Locator arg0) {
            // Ignore
        }

        public void startDocument() throws SAXException {
            // Ignore
        }

        public void endDocument() throws SAXException {
            // Ignore
        }

        public void processingInstruction(String arg0, String arg1) throws SAXException {
            // Ignore
        }

        public void skippedEntity(String arg0) throws SAXException {
            // Ignore
        }

        public void startElement(String ns, String localName, String qName, Attributes atts) throws SAXException {
            switch (this.state) {
                case 0:
                    // <i18n:catalogue>
                    if (!"".equals(ns) && !NS.equals(ns)) {
                        throw new SAXException("Root element <" + EL_CATALOGUE +
                                               "> must be non-namespaced or in i18n namespace.");
                    }
                    if (!EL_CATALOGUE.equals(localName)) {
                        throw new SAXException("Root element must be <" + EL_CATALOGUE + ">.");
                    }
                    this.namespace = ns;
                    this.state++;
                    break;
                case 1:
                    // <i18n:message>
                    if (!EL_MESSAGE.equals(localName)) {
                        throw new SAXException("<" + EL_CATALOGUE + "> must contain <" +
                                               EL_MESSAGE + "> elements only.");
                    }
                    if (!this.namespace.equals(ns)) {
                        throw new SAXException("<" + EL_MESSAGE + "> element must be in '" +
                                               this.namespace + "' namespace.");
                    }
                    String key =  atts.getValue(AT_KEY);
                    if (key == null) {
                        throw new SAXException("<" + EL_MESSAGE + "> must have '" +
                                               AT_KEY + "' attribute.");
                    }
                    this.buffer = new ParamSaxBuffer();
                    this.values.put(key, this.buffer);
                    this.state++;
                    break;
                case 2:
                    this.buffer.startElement(ns, localName, qName, atts);
                    break;
                default:
                    throw new SAXException("Internal error: Invalid state");
            }
        }

        public void endElement(String ns, String localName, String qName) throws SAXException {
            switch (this.state) {
                case 0:
                    break;
                case 1:
                    // </i18n:catalogue>
                    this.state--;
                    break;
                case 2:
                    if (this.namespace.equals(ns) && EL_MESSAGE.equals(localName)) {
                        // </i18n:message>
                        this.buffer = null;
                        this.state--;
                    } else {
                        this.buffer.endElement(ns, localName, qName);
                    }
                    break;
                default:
                    throw new SAXException("Internal error: Invalid state");
            }
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (this.buffer != null) {
                this.buffer.startPrefixMapping(prefix, uri);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            if (this.buffer != null) {
                this.buffer.endPrefixMapping(prefix);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.ignorableWhitespace(ch, start, length);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.characters(ch, start, length);
            }
        }
    }

    /**
     * Compose this instance
     *
     * @param manager The <code>ServiceManager</code> instance
     * @throws ServiceException
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Implements Disposable interface for this class.
     */
    public void dispose() {
        this.manager = null;
    }

    /**
     * Initalize the bundle
     *
     * @param sourceURI source URI of the XML bundle
     * @param locale locale
     * @param parent parent bundle of this bundle
     *
     * @throws IOException if an IO error occurs while reading the file
     * @throws ProcessingException if an error occurs while loading the bundle
     * @throws SAXException if an error occurs while loading the bundle
     */
    public void init(String sourceURI, Locale locale, Bundle parent)
    throws IOException, ProcessingException, SAXException {
        this.sourceURI = sourceURI;
        this.locale = locale;
        this.parent = parent;
        this.values = new HashMap();
        load();
    }

    /**
     * Load the XML bundle, based on the source URI.
     *
     * @exception IOException if an IO error occurs while reading the file
     * @exception ProcessingException if no parser is configured
     * @exception SAXException if an error occurs while parsing the file
     */
    protected void load() throws IOException, ProcessingException, SAXException {
        Source source = null;
        SourceResolver resolver = null;
        try {
            int valid = this.validity == null ? SourceValidity.INVALID : this.validity.isValid();
            if (valid != SourceValidity.VALID) {
                // Saved validity is not valid, get new source and validity
                resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                source = resolver.resolveURI(this.sourceURI);
                SourceValidity sourceValidity = source.getValidity();
                if (valid == SourceValidity.INVALID || this.validity.isValid(sourceValidity) != SourceValidity.VALID) {
                    HashMap values = new HashMap();
                    SourceUtil.toSAX(source, new SAXContentHandler(values));
                    this.validity = sourceValidity;
                    this.values = values;
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Loaded XML bundle: " + this.sourceURI + ", locale: " + this.locale);
                    }
                }
            }
        } catch (ServiceException e) {
            throw new ProcessingException("Can't lookup source resolver", e);
        } catch (MalformedURLException e) {
            throw new SourceNotFoundException("Invalid resource URL: " + this.sourceURI, e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
            this.manager.release(resolver);
        }
    }

    /**
     * Gets the validity of the bundle.
     *
     * @return the validity
     */
    public SourceValidity getValidity() {
        return this.validity;
    }

    /**
     * Gets the locale of the bundle.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Get Object value by key.
     *
     * @param key the key
     * @return the value
     */
    public Object getObject(String key) {
        if (key == null) {
            return null;
        }

        Object value = this.values.get(key);
        if (value == null && this.parent != null) {
            value = this.parent.getObject(key);
        }

        return value;
    }

    /**
     * Get String value by key.
     *
     * @param key the key
     * @return the value
     */
    public String getString(String key) {
        if (key == null) {
            return null;
        }

        Object value = this.values.get(key);
        if (value != null) {
            return value.toString();
        }

        if(this.parent != null) {
            return this.parent.getString(key);
        }

        return null;
    }

    /**
     * Return a set of keys.
     *
     * @return the enumeration of keys
     */
    public Set keySet() {
        return Collections.unmodifiableSet(this.values.keySet());
    }

    /**
     * Reload this bundle if URI's timestamp is newer than ours.
     */
    public void update() {
        try {
            load();
        } catch (Exception e) {
            getLogger().info("Resource update failed. " + this.sourceURI + ", locale: " + this.locale
                             + " Exception: " + e);
        }
    }
}
