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
package org.apache.cocoon.i18n;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.validity.DelayedValidity;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.ParamSaxBuffer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Implementation of <code>Bundle</code> interface for XML resources. Represents a
 * single XML message bundle.
 *
 * <p>
 * XML format for this resource bundle implementation is the following:
 * <pre>
 * &lt;catalogue xml:lang="en"&gt;
 *   &lt;message key="key1"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   &lt;message key="key2"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   ...
 * &lt;/catalogue&gt;
 * </pre>
 *
 * <p>
 * Value can be any well formed XML snippet and it will be cached by the key specified
 * in the attribute <code>key</code>. Objects returned by this {@link Bundle} implementation
 * are instances of the {@link ParamSaxBuffer} class.
 *
 * <p>
 * If value for a key is not present in this bundle, parent bundle will be queried.
 *
 * @version $Id$
 */
public class XMLResourceBundle extends AbstractLogEnabled
                               implements Bundle {

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
    protected Map values;


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
                    if (!"".equals(ns) && !I18nUtils.matchesI18nNamespace(ns)) {
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
     * Construct a bundle.
     * @param sourceURI source URI of the XML bundle
     * @param locale locale
     * @param parent parent bundle of this bundle
     */
    public XMLResourceBundle(String sourceURI, Locale locale, Bundle parent) {
        this.sourceURI = sourceURI;
        this.locale = locale;
        this.parent = parent;
        this.values = Collections.EMPTY_MAP;
    }

    /**
     * (Re)Loads the XML bundle if necessary, based on the source URI.
     * @return true if reloaded successfully
     */
    protected boolean reload(SourceResolver resolver, long interval) {
        Source newSource = null;
        Map newValues;

        try {
            int valid = this.validity == null ? SourceValidity.INVALID : this.validity.isValid();
            if (valid != SourceValidity.VALID) {
                // Saved validity is not valid, get new source and validity
                newSource = resolver.resolveURI(this.sourceURI);
                SourceValidity newValidity = newSource.getValidity();

                if (valid == SourceValidity.INVALID || this.validity.isValid(newValidity) != SourceValidity.VALID) {
                    newValues = new HashMap();
                    SourceUtil.toSAX(newSource, new SAXContentHandler(newValues));
                    synchronized (this) {
                        // Update source validity and values
                        if (interval > 0 && newValidity != null) {
                            this.validity = new DelayedValidity(interval, newValidity);
                        } else {
                            this.validity = newValidity;
                        }
                        this.values = newValues;
                    }
                }
            }

            // Success
            return true;

        } catch (MalformedURLException e) {
            getLogger().error("Bundle <" + this.sourceURI + "> not loaded: Invalid URI", e);
            newValues = Collections.EMPTY_MAP;

        } catch (ResourceNotFoundException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().info("Bundle <" + sourceURI + "> not loaded: Source URI not found", e);
            } else if (getLogger().isInfoEnabled()) {
                getLogger().info("Bundle <" + sourceURI + "> not loaded: Source URI not found");
            }
            newValues = Collections.EMPTY_MAP;

        } catch (SourceNotFoundException e) {
            // Nominal case where a bundle doesn't exist
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Bundle <" + sourceURI + "> not loaded: Source URI not found");
            }
            newValues = Collections.EMPTY_MAP;

        } catch (CascadingIOException e) {
            // Nominal case where a bundle doesn't exist
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Bundle <" + sourceURI + "> not loaded: Source URI not found");
            }
            newValues = Collections.EMPTY_MAP;

        } catch (SAXException e) {
            getLogger().error("Bundle <" + sourceURI + "> not loaded: Invalid XML", e);
            // Keep existing loaded values
            newValues = this.values;

        } catch (Exception e) {
            getLogger().error("Bundle <" + sourceURI + "> not loaded: Exception", e);
            // Keep existing loaded values
            newValues = this.values;

        } finally {
            if (newSource != null) {
                resolver.release(newSource);
            }
        }

        synchronized (this) {
            // Use expires validity to delay next reloading.
            if (interval > 0) {
                this.validity = new ExpiresValidity(interval);
            } else {
                this.validity = null;
            }
            this.values = newValues;
        }

        // Failure
        return false;
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
     * Gets the source URI of the bundle.
     *
     * @return the source URI
     */
    public String getSourceURI() {
        return this.sourceURI;
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
     * Get an instance of the {@link ParamSaxBuffer} associated with the key.
     *
     * @param key the key
     * @return the value, or null if no value associated with the key.
     */
    public Object getObject(String key) {
        if (key == null) {
            return null;
        }

        Object value = this.values.get(key);
        if (value != null) {
            return value;
        }

        if (this.parent != null) {
            return this.parent.getObject(key);
        }

        return null;
    }

    /**
     * Get a string representation of the value object by key.
     *
     * @param key the key
     * @return the string value, or null if no value associated with the key.
     */
    public String getString(String key) {
        if (key == null) {
            return null;
        }

        Object value = this.values.get(key);
        if (value != null) {
            return value.toString();
        }

        if (this.parent != null) {
            return this.parent.getString(key);
        }

        return null;
    }
}
