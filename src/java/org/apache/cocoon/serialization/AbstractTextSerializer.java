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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.TraxErrorHandler;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public abstract class AbstractTextSerializer extends AbstractSerializer
        implements Configurable, CacheableProcessingComponent, Contextualizable {

    /**
     * The trax <code>TransformerFactory</code> used by this serializer.
     */
    private SAXTransformerFactory tfactory = null;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();

    /**
     * The default output buffer size.
     */
    //private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The output buffer size to use.
     */
    //private int outputBufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * Cache for avoiding unnecessary checks of namespaces abilities.
     * It associates a Boolean to the transformer class name.
     */
    private static Map needsNamespaceCache = new HashMap();

    /**
     * The pipe that adds namespaces as xmlns attributes.
     */
    private NamespaceAsAttributes namespacePipe;

    /** The caching key */
    private String cachingKey = "1";
    

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

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        /*
         * Add a level of buffering to the output stream. Xalan serializes
         * every character individually. In conjunction with chunked
         * transfer encoding this would otherwise lead to a whopping 6-fold
         * increase of data on the wire.
         */
        //  if (outputBufferSize > 0) {
        //      super.setOutputStream(
        //        new BufferedOutputStream(out, outputBufferSize));
        //  } else {
        super.setOutputStream(out);
        //  }
    }

    /**
     * Uses the context to retrieve a default encoding for the serializers.
     */
    public void contextualize(Context context) throws ContextException {
        String defaultEncoding  = (String)context.get(Constants.CONTEXT_DEFAULT_ENCODING);
        if (defaultEncoding != null) {
            this.format.setProperty(OutputKeys.ENCODING, defaultEncoding);
        }
    }

    /**
     * Set the configurations for this serializer.
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

        final StringBuffer buffer = new StringBuffer();
        
        if (cdataSectionElements != null) {
            format.put(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
            buffer.append(";cdata-section-elements=").append(cdataSectionElements);
        }
        if (dtPublic != null) {
            format.put(OutputKeys.DOCTYPE_PUBLIC, dtPublic);
            buffer.append(";doctype-public=").append(dtPublic);
        }
        if (dtSystem != null) {
            format.put(OutputKeys.DOCTYPE_SYSTEM, dtSystem);
            buffer.append(";doctype-system=").append(dtSystem);
        }
        if (encoding != null) {
            format.put(OutputKeys.ENCODING, encoding);
            buffer.append(";encoding=").append(encoding);
        }
        if (indent != null) {
            format.put(OutputKeys.INDENT, indent);
            buffer.append(";indent=").append(indent);
        }
        if (mediaType != null) {
            format.put(OutputKeys.MEDIA_TYPE, mediaType);
            buffer.append(";media-type=").append(mediaType);
        }
        if (method != null) {
            format.put(OutputKeys.METHOD, method);
            buffer.append(";method=").append(method);
        }
        if (omitXMLDeclaration != null) {
            format.put(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration);
            buffer.append(";omit-xml-declaration=").append(omitXMLDeclaration);
        }
        if (standAlone != null) {
            format.put(OutputKeys.STANDALONE, standAlone);
            buffer.append(";standalone=").append(standAlone);
        }
        if (version != null) {
            format.put(OutputKeys.VERSION, version);
            buffer.append(";version=").append(version);
        }
        
        if ( buffer.length() > 0 ) {
            this.cachingKey = buffer.toString();
        }
        
        Configuration tFactoryConf = conf.getChild("transformer-factory", false);
        if (tFactoryConf != null) {
            String tFactoryClass = tFactoryConf.getValue();
            try {
                this.tfactory = (SAXTransformerFactory) ClassUtils.newInstance(tFactoryClass);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using transformer factory " + tFactoryClass);
                }
            } catch (Exception e) {
                throw new ConfigurationException("Cannot load transformer factory " + tFactoryClass, e);
            }
        } else {
            // Standard TrAX behaviour
            this.tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        }

        tfactory.setErrorListener(new TraxErrorHandler(getLogger()));

        // Check if we need namespace as attributes.
        try {
            if (needsNamespacesAsAttributes()) {
                // Setup a correction pipe
                this.namespacePipe = new NamespaceAsAttributes();
                this.namespacePipe.enableLogging(getLogger());
            }
        } catch (Exception e) {
            getLogger().warn("Cannot know if transformer needs namespaces attributes - assuming NO.", e);
        }
    }

    public void recycle() {
        super.recycle();

        if (this.namespacePipe != null) {
            this.namespacePipe.recycle();
        }
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
            handler.startElement(uri, "element", "", new AttributesImpl());
            handler.endPrefixMapping(prefix);
            handler.endDocument();

            String text = writer.toString();

            // Check if the namespace is there (replace " by ' to be sure of what we search in)
            boolean needsIt = (text.replace('"', '\'').indexOf(check) == -1);

            String msg = needsIt ? " needs namespace attributes (will be slower)." : " handles correctly namespaces.";

            getLogger().debug("Trax handler " + handler.getClass().getName() + msg);

            needsNamespaceCache.put(factory.getClass().getName(), new Boolean(needsIt));

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

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        super.endDocument();

        //   if (this.output != null) {
        //       try {
        //           this.output.flush();
        //       } catch (IOException ignored) {
        //       }
        //   }
    }

}
