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
package org.apache.cocoon.serialization;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.TraxErrorHandler;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version $Id$
 */
public abstract class AbstractTextSerializer extends AbstractSerializer
                                             implements Configurable, Serviceable, CacheableProcessingComponent {
    
    /**
     * Cache for avoiding unnecessary checks of namespaces abilities.
     * It associates a Boolean to the transformer class name.
     */
    private static final Map needsNamespaceCache = new HashMap();

    /**
     * The trax <code>TransformerFactory</code> used by this serializer.
     */
    private SAXTransformerFactory tfactory;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();

    /**
     * The pipe that adds namespaces as xmlns attributes.
     */
    private NamespaceAsAttributes namespacePipe;

    /**
     * The caching key
     */
    private String cachingKey;

    private String transformerFactoryClass;
    
    private String defaultEncoding = null;

    /**
     * Set the properties used for transformer handler used by the serializer
     * see {@link OutputKeys} for possible settings.
     * 
     * @param format
     */
    public void setFormat(Properties format) {
        this.format = format;
    }

    /**
     * Set the default encoding. This will be overided if the encoding is set
     * in the format properties. This is mainly useful together with Spring
     * bean inheritance.
     * 
     * @param defaultEncoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Initialize logger, caching key, transformer handler and namespace pipe
     *
     * @throws Exception 
     */
    public void init() throws Exception {
        if (!this.format.containsKey(OutputKeys.ENCODING) && this.defaultEncoding != null) {
            this.format.put(OutputKeys.ENCODING, this.defaultEncoding);
        }
        this.cachingKey = createCachingKey(format);
        initTransformerFactory();
        initNamespacePipe();
    }

    /**
     * Optionally set the transformer factory used for creating the transformer
     * handler that is used for serialization. Otherwise the standard transformer
     * factory is used ({@link TransformerFactory}).
     * 
     * @param transformerFactoryClass the name of the class
     */
    public void setTransformerFactory(String transformerFactoryClass) {
        this.transformerFactoryClass = transformerFactoryClass;
    }

    /**
     * Interpose namespace pipe if needed.
     */
    public void setConsumer(XMLConsumer consumer) {
        if (this.namespacePipe == null) {
            super.setConsumer(consumer);
        } else {
            this.namespacePipe.setConsumer(consumer);
            super.setConsumer(this.namespacePipe);
        }
    }

    /**
     * Interpose namespace pipe if needed.
     */
    public void setContentHandler(ContentHandler handler) {
        if (this.namespacePipe == null) {
            super.setContentHandler(handler);
        } else {
            this.namespacePipe.setContentHandler(handler);
            super.setContentHandler(this.namespacePipe);
        }
    }

    /**
     * Interpose namespace pipe if needed.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        if (this.namespacePipe == null) {
            super.setLexicalHandler(handler);
        } else {
            this.namespacePipe.setLexicalHandler(handler);
            super.setLexicalHandler(this.namespacePipe);
        }
    }

    /**
     * Helper for TransformerFactory.
     */
    protected SAXTransformerFactory getTransformerFactory() {
        return tfactory;
    }

    /**
     * Helper for TransformerHandler.
     */
    protected TransformerHandler getTransformerHandler() throws TransformerException {
        return this.getTransformerFactory().newTransformerHandler();
    }

//    /**
//     * Set the {@link OutputStream} where the requested resource should
//     * be serialized.
//     */
//    public void setOutputStream(OutputStream out) throws IOException {
//        /*
//         * Add a level of buffering to the output stream. Xalan serializes
//         * every character individually. In conjunction with chunked
//         * transfer encoding this would otherwise lead to a whopping 6-fold
//         * increase of data on the wire.
//         */
//        //  if (outputBufferSize > 0) {
//        //      super.setOutputStream(
//        //        new BufferedOutputStream(out, outputBufferSize));
//        //  } else {
//        super.setOutputStream(out);
//        //  }
//    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     * 
     * @deprecated use property injection instead
     */
    public void service(ServiceManager manager) throws ServiceException {
        final Settings settings = (Settings)manager.lookup(Settings.ROLE);
        String defaultEncoding  = settings.getFormEncoding();
        if (defaultEncoding != null) {
            this.format.setProperty(OutputKeys.ENCODING, defaultEncoding);
        }
        manager.release(settings);
    }

    /**
     * Set the configurations for this serializer.
     * 
     * @deprecated use property injection instead
     */
    public void configure(Configuration conf) throws ConfigurationException {
        // configure buffer size
        //   Configuration bsc = conf.getChild("buffer-size", false);
        //   if(null != bsc)
        //    outputBufferSize = bsc.getValueAsInteger(DEFAULT_BUFFER_SIZE);

        // configure xalan
        String cdataSectionElements = conf.getChild("cdata-section-elements").getValue(null);
        String dtPublic = conf.getChild("doctype-public").getValue(null);
        String dtSystem = conf.getChild("doctype-system").getValue(null);
        String encoding = conf.getChild("encoding").getValue(null);
        String indent = conf.getChild("indent").getValue(null);
        String mediaType = conf.getChild("media-type").getValue(null);
        String method = conf.getChild("method").getValue(null);
        String omitXMLDeclaration = conf.getChild("omit-xml-declaration").getValue(null);
        String standAlone = conf.getChild("standalone").getValue(null);
        String version = conf.getChild("version").getValue(null);

        if (cdataSectionElements != null) {
            format.put(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
        }
        if (dtPublic != null) {
            format.put(OutputKeys.DOCTYPE_PUBLIC, dtPublic);
        }
        if (dtSystem != null) {
            format.put(OutputKeys.DOCTYPE_SYSTEM, dtSystem);
        }
        if (encoding != null) {
            format.put(OutputKeys.ENCODING, encoding);
        }
        if (indent != null) {
            format.put(OutputKeys.INDENT, indent);
        }
        if (mediaType != null) {
            format.put(OutputKeys.MEDIA_TYPE, mediaType);
        }
        if (method != null) {
            format.put(OutputKeys.METHOD, method);
        }
        if (omitXMLDeclaration != null) {
            format.put(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration);
        }
        if (standAlone != null) {
            format.put(OutputKeys.STANDALONE, standAlone);
        }
        if (version != null) {
            format.put(OutputKeys.VERSION, version);
        }

        this.cachingKey = createCachingKey(format);

        this.transformerFactoryClass = conf.getChild("transformer-factory").getValue(null);
        this.initTransformerFactory();

        this.initNamespacePipe();

    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        if (this.namespacePipe != null) {
            this.namespacePipe.recycle();
        }
    }

    private void initTransformerFactory() throws ConfigurationException, TransformerFactoryConfigurationError {
        if (transformerFactoryClass != null) {
            try {
                this.tfactory = (SAXTransformerFactory) ClassUtils.newInstance(transformerFactoryClass);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using transformer factory " + transformerFactoryClass);
                }
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load transformer factory " + transformerFactoryClass, e);
            }
        } else {
            // Standard TrAX behaviour
            this.tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        }
        tfactory.setErrorListener(new TraxErrorHandler(getLogger()));
    }

    private void initNamespacePipe() {
        // Check if we need namespace as attributes.
        try {
            if (needsNamespacesAsAttributes()) {
                // Setup a correction pipe
                this.namespacePipe = new NamespaceAsAttributes();
            }
        } catch (Exception e) {
            getLogger().warn("Cannot know if transformer needs namespaces attributes - assuming NO.", e);
        }
    }

    /**
     * Create the caching key from the formating properties used by the
     * transformer handler that is used for serialization.
     * @param format 
     *
     */
    protected static String createCachingKey(Properties format) {
        final StringBuffer buffer = new StringBuffer();
        String value = null;
        
        // Use lookup of the property values instead of just iterating through the
        // enumeration of them to give the caching key a  deterministic order of its parts

        if ((value = format.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS)) != null) {
            buffer.append(";cdata-section-elements=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.DOCTYPE_PUBLIC)) != null) {
            buffer.append(";doctype-public=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.DOCTYPE_SYSTEM)) != null) {
            buffer.append(";doctype-system=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.ENCODING)) != null) {
            buffer.append(";encoding=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.INDENT)) != null) {
            buffer.append(";indent=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.MEDIA_TYPE)) != null) {
            buffer.append(";media-type=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.METHOD)) != null) {
            buffer.append(";method=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.OMIT_XML_DECLARATION)) != null) {
            buffer.append(";omit-xml-declaration=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.STANDALONE)) != null) {
            buffer.append(";standalone=").append(value);
        }
        if ((value = format.getProperty(OutputKeys.VERSION)) != null) {
            buffer.append(";version=").append(value);
        }

        if ( buffer.length() > 0 )
            return buffer.toString();
        else
            return "1";
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public java.io.Serializable getKey() {
        return this.cachingKey;
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
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Checks if the used Trax implementation correctly handles namespaces set using
     * <code>startPrefixMapping()</code>, but wants them also as 'xmlns:' attributes.
     * <p>
     * The check consists in sending SAX events representing a minimal namespaced document
     * with namespaces defined only with calls to <code>startPrefixMapping</code> (no
     * xmlns:xxx attributes) and check if they are present in the resulting text.
     */
    protected boolean needsNamespacesAsAttributes() throws Exception {

        SAXTransformerFactory factory = getTransformerFactory();

        Boolean cacheValue = (Boolean) needsNamespaceCache.get(factory.getClass().getName());
        if (cacheValue != null) {
            return cacheValue.booleanValue();
        } else {
            // Serialize a minimal document to check how namespaces are handled.
            StringWriter writer = new StringWriter();

            String uri = "namespaceuri";
            String prefix = "nsp";
            String check = "xmlns:" + prefix + "='" + uri + "'";

            TransformerHandler handler = this.getTransformerHandler();

            handler.getTransformer().setOutputProperties(format);
            handler.setResult(new StreamResult(writer));

            // Output a single element
            handler.startDocument();
            handler.startPrefixMapping(prefix, uri);
            handler.startElement(uri, "element", "element", XMLUtils.EMPTY_ATTRIBUTES);
            handler.endElement(uri, "element", "element");
            handler.endPrefixMapping(prefix);
            handler.endDocument();

            String text = writer.toString();

            // Check if the namespace is there (replace " by ' to be sure of what we search in)
            boolean needsIt = (text.replace('"', '\'').indexOf(check) == -1);

            String msg = needsIt ? " needs namespace attributes (will be slower)." : " handles correctly namespaces.";

            getLogger().debug("Trax handler " + handler.getClass().getName() + msg);

            needsNamespaceCache.put(factory.getClass().getName(), BooleanUtils.toBooleanObject(needsIt));

            return needsIt;
        }
    }

    //--------------------------------------------------------------------------------------------

    /**
     * A pipe that ensures that all namespace prefixes are also present as
     * 'xmlns:' attributes. This used to circumvent Xalan's serialization behaviour
     * which is to ignore namespaces if they're not present as 'xmlns:xxx' attributes.
     */
    public static class NamespaceAsAttributes extends AbstractXMLPipe {

        /**
         * The prefixes of startPrefixMapping() declarations for the coming element.
         */
        private List prefixList = new ArrayList();

        /**
         * The URIs of startPrefixMapping() declarations for the coming element.
         */
        private List uriList = new ArrayList();

        /**
         * Maps of URI<->prefix mappings. Used to work around a bug in the Xalan
         * serializer.
         */
        private Map uriToPrefixMap = new HashMap();
        private Map prefixToUriMap = new HashMap();

        /**
         * True if there has been some startPrefixMapping() for the coming element.
         */
        private boolean hasMappings = false;

        public void startDocument() throws SAXException {
            // Cleanup
            this.uriToPrefixMap.clear();
            this.prefixToUriMap.clear();
            clearMappings();
            super.startDocument();
        }

        /**
         * Track mappings to be able to add <code>xmlns:</code> attributes
         * in <code>startElement()</code>.
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // Store the mappings to reconstitute xmlns:attributes
            // except prefixes starting with "xml": these are reserved
            // VG: (uri != null) fixes NPE in startElement
            if (uri != null && !prefix.startsWith("xml")) {
                this.hasMappings = true;
                this.prefixList.add(prefix);
                this.uriList.add(uri);

                // append the prefix colon now, in order to save concatenations later, but
                // only for non-empty prefixes.
                if (prefix.length() > 0) {
                    this.uriToPrefixMap.put(uri, prefix + ":");
                } else {
                    this.uriToPrefixMap.put(uri, prefix);
                }

                this.prefixToUriMap.put(prefix, uri);
            }
            super.startPrefixMapping(prefix, uri);
        }

        /**
         * Ensure all namespace declarations are present as <code>xmlns:</code> attributes
         * and add those needed before calling superclass. This is a workaround for a Xalan bug
         * (at least in version 2.0.1) : <code>org.apache.xalan.serialize.SerializerToXML</code>
         * ignores <code>start/endPrefixMapping()</code>.
         */
        public void startElement(String eltUri, String eltLocalName, String eltQName, Attributes attrs)
                throws SAXException {

            // try to restore the qName. The map already contains the colon
            if (null != eltUri && eltUri.length() != 0 && this.uriToPrefixMap.containsKey(eltUri)) {
                eltQName = this.uriToPrefixMap.get(eltUri) + eltLocalName;
            }
            if (this.hasMappings) {
                // Add xmlns* attributes where needed

                // New Attributes if we have to add some.
                AttributesImpl newAttrs = null;

                int mappingCount = this.prefixList.size();
                int attrCount = attrs.getLength();

                for (int mapping = 0; mapping < mappingCount; mapping++) {

                    // Build infos for this namespace
                    String uri = (String) this.uriList.get(mapping);
                    String prefix = (String) this.prefixList.get(mapping);
                    String qName = prefix.equals("") ? "xmlns" : ("xmlns:" + prefix);

                    // Search for the corresponding xmlns* attribute
                    boolean found = false;
                    for (int attr = 0; attr < attrCount; attr++) {
                        if (qName.equals(attrs.getQName(attr))) {
                            // Check if mapping and attribute URI match
                            if (!uri.equals(attrs.getValue(attr))) {
                                getLogger().error("URI in prefix mapping and attribute do not match : '"
                                                  + uri + "' - '" + attrs.getURI(attr) + "'");
                                throw new SAXException("URI in prefix mapping and attribute do not match");
                            }
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        // Need to add this namespace
                        if (newAttrs == null) {
                            // Need to test if attrs is empty or we go into an infinite loop...
                            // Well know SAX bug which I spent 3 hours to remind of :-(
                            if (attrCount == 0) {
                                newAttrs = new AttributesImpl();
                            } else {
                                newAttrs = new AttributesImpl(attrs);
                            }
                        }

                        if (prefix.equals("")) {
                            newAttrs.addAttribute(Constants.XML_NAMESPACE_URI, "xmlns", "xmlns", "CDATA", uri);
                        } else {
                            newAttrs.addAttribute(Constants.XML_NAMESPACE_URI, prefix, qName, "CDATA", uri);
                        }
                    }
                } // end for mapping

                // Cleanup for the next element
                clearMappings();

                // Start element with new attributes, if any
                super.startElement(eltUri, eltLocalName, eltQName, newAttrs == null ? attrs : newAttrs);
            } else {
                // Normal job
                super.startElement(eltUri, eltLocalName, eltQName, attrs);
            }
        }


        /**
         * Receive notification of the end of an element.
         * Try to restore the element qName.
         */
        public void endElement(String eltUri, String eltLocalName, String eltQName) throws SAXException {
            // try to restore the qName. The map already contains the colon
            if (null != eltUri && eltUri.length() != 0 && this.uriToPrefixMap.containsKey(eltUri)) {
                eltQName = this.uriToPrefixMap.get(eltUri) + eltLocalName;
            }
            super.endElement(eltUri, eltLocalName, eltQName);
        }

        /**
         * End the scope of a prefix-URI mapping:
         * remove entry from mapping tables.
         */
        public void endPrefixMapping(String prefix) throws SAXException {
            // remove mappings for xalan-bug-workaround.
            // Unfortunately, we're not passed the uri, but the prefix here,
            // so we need to maintain maps in both directions.
            if (this.prefixToUriMap.containsKey(prefix)) {
                this.uriToPrefixMap.remove(this.prefixToUriMap.get(prefix));
                this.prefixToUriMap.remove(prefix);
            }

            if (hasMappings) {
                // most of the time, start/endPrefixMapping calls have an element event between them,
                // which will clear the hasMapping flag and so this code will only be executed in the
                // rather rare occasion when there are start/endPrefixMapping calls with no element
                // event in between. If we wouldn't remove the items from the prefixList and uriList here,
                // the namespace would be incorrectly declared on the next element following the
                // endPrefixMapping call.
                int pos = prefixList.lastIndexOf(prefix);
                if (pos != -1) {
                    prefixList.remove(pos);
                    uriList.remove(pos);
                }
            }

            super.endPrefixMapping(prefix);
        }

        /**
         *
         */
        public void endDocument() throws SAXException {
            // Cleanup
            this.uriToPrefixMap.clear();
            this.prefixToUriMap.clear();
            clearMappings();
            super.endDocument();
        }

        private void clearMappings() {
            this.hasMappings = false;
            this.prefixList.clear();
            this.uriList.clear();
        }
    }
}
