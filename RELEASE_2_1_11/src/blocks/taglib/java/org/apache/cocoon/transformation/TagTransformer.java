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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.taglib.IterationTag;
import org.apache.cocoon.taglib.Tag;
import org.apache.cocoon.taglib.BodyTag;
import org.apache.cocoon.taglib.BodyContent;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.xml.SaxBuffer;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.map.StaticBucketMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Transformer which implements the taglib functionalty.
 *
 * <p>Transformer processes incoming SAX events and for each element it tries to
 * find {@link Tag} component with matching namespace and tag name.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id$
 */
public class TagTransformer
        extends AbstractXMLProducer
        implements Transformer, Serviceable, Configurable, Disposable, Recyclable {

    private int recordingLevel;
    private int skipLevel;

    private String transformerHint;
    private ServiceSelector transformerSelector;

    private final ArrayStack tagStack = new ArrayStack();
    private final ArrayStack tagSelectorStack = new ArrayStack();
    private final ArrayStack tagTransformerStack = new ArrayStack();

    private ServiceSelector tagNamespaceSelector;
    private Tag currentTag;

    /** current SAX Event Consumer  */
    private XMLConsumer currentConsumer;

    /** backup of currentConsumer while recording */
    private XMLConsumer currentConsumerBackup;

    private XMLSerializer xmlSerializer;

    /** The SourceResolver for this request */
    private SourceResolver resolver;

    /** The current objectModel of the environment */
    private Map objectModel;

    /** The parameters specified in the sitemap */
    private Parameters parameters;

    /** The Avalon ServiceManager */
    private ServiceManager manager;


    /** Array for dynamic calling of Tag set property methods */
    private final String[] paramArray = new String[1];

    /** Map for caching Tag Introspection */
    private static Map TAG_PROPERTIES_MAP = new StaticBucketMap();

    //
    // Component Lifecycle Methods
    //

    /**
     * Avalon Serviceable Interface
     * @param manager The Avalon Service Manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.tagNamespaceSelector = (ServiceSelector) manager.lookup(Tag.ROLE + "Selector");
    }

    /**
     * Avalon Configurable Interface
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.transformerHint = conf.getChild("transformer-hint").getValue(null);
        if (this.transformerHint != null) {
            try {
                this.transformerSelector = (ServiceSelector) manager.lookup(Transformer.ROLE + "Selector");
            } catch (ServiceException e) {
                String message = "Can't lookup transformer selector";
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(message, e);
                }
                throw new ConfigurationException(message, e);
            }
        }
    }

    /**
     * Set the <code>EntityResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters)
    throws IOException, SAXException {
        this.resolver = resolver;
        this.objectModel = objectModel;
        this.parameters = parameters;
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        this.recordingLevel = 0;
        this.skipLevel = 0;
        this.resolver = null;
        this.objectModel = null;
        this.parameters = null;
        this.currentTag = null;
        this.currentConsumer = null;
        this.currentConsumerBackup = null;

        // can happen if there was a error in the pipeline
        if (xmlSerializer != null) {
            manager.release(xmlSerializer);
            xmlSerializer = null;
        }

        while (!tagStack.isEmpty()) {
            Tag tag = (Tag) tagStack.pop();
            if (tag == null)
                continue;
            ComponentSelector tagSelector = (ComponentSelector)tagSelectorStack.pop();
            tagSelector.release(tag);

            tagNamespaceSelector.release(tagSelector);
        }

        while (!tagTransformerStack.isEmpty()) {
            Transformer transformer = (Transformer) tagTransformerStack.pop();
            transformerSelector.release(transformer);
        }

        if (!tagSelectorStack.isEmpty()) {
            getLogger().fatalError("recycle: internal Error, tagSelectorStack not empty");
            tagSelectorStack.clear();
        }

        super.recycle();
    }

    /**
     *  Dispose this component.
     */
    public void dispose() {
        this.manager.release(tagNamespaceSelector);
        tagNamespaceSelector = null;
        if (transformerSelector != null) {
            this.manager.release(transformerSelector);
            transformerSelector = null;
        }
    }

    /*
     * @see XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.currentConsumer = consumer;
        super.setConsumer(consumer);
    }


    //
    // SAX Events Methods
    //

    public void setDocumentLocator(org.xml.sax.Locator locator) {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        this.currentConsumer.startDocument();
    }

    public void endDocument() throws SAXException {
        this.currentConsumer.endDocument();
    }

    public void processingInstruction(String target, String data) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.processingInstruction(target, data);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.endDTD();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.endPrefixMapping(prefix);
    }

    public void startCDATA() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.startCDATA();
    }

    public void endCDATA() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.endCDATA();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        // Are we recording for iteration ?
        if (this.recordingLevel > 0) {
            this.recordingLevel ++;
            this.currentConsumer.startElement(namespaceURI, localName, qName, atts);
            return;
        }

        // If we are skipping the body of a Tag
        if (this.skipLevel > 0) {
            // Remember to skip one more end element
            this.skipLevel ++;
            // and ignore this start element
            return;
        }

        Tag tag = null;
        if (namespaceURI != null && namespaceURI.length() > 0) {
            // Try to find Tag corresponding to this element
            ComponentSelector tagSelector = null;
            try {
                tagSelector = (ComponentSelector) tagNamespaceSelector.select(namespaceURI);
                tagSelectorStack.push(tagSelector);

                // namespace matches tag library, lookup tag now.
                tag = (Tag) tagSelector.select(localName);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("startElement: Got tag " + qName);
                }

                setupTag(tag, qName, atts);
            } catch (SAXException e) {
                throw e;
            } catch (Exception ignore) {
                // No namespace or tag found, process it as normal element (tag == null)
            }
        }

        tagStack.push(tag);
        if (tag == null) {
            currentConsumer.startElement(namespaceURI, localName, qName, atts);
            return;
        }

        // Execute Tag
        int eval = tag.doStartTag(namespaceURI, localName, qName, atts);
        switch (eval) {
            case Tag.EVAL_BODY :
                skipLevel = 0;
                if (tag instanceof IterationTag) {
                    // start recording for IterationTag
                    startRecording();
                }
                break;

            case Tag.SKIP_BODY :
                skipLevel = 1;
                break;

            default :
                String tagName = tag.getClass().getName();
                getLogger().warn("Bad return value from doStartTag(" + tagName + "): " + eval);
                break;
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        Object saxFragment = null;

        // Are we recording?
        if (recordingLevel > 0) {
            if (--recordingLevel > 0) {
                currentConsumer.endElement(namespaceURI, localName, qName);
                return;
            }
            // Recording finished
            saxFragment = endRecording();
        }

        if (skipLevel > 0) {
            if (--skipLevel > 0) {
                return;
            }
        }

        Tag tag = (Tag) tagStack.pop();
        if (tag != null) {
            ComponentSelector tagSelector = (ComponentSelector)tagSelectorStack.pop();
            try {
                if (saxFragment != null) {
                    // Start Iteration
                    IterationTag iterTag = (IterationTag) tag;
                    XMLDeserializer xmlDeserializer = null;
                    try {
                        xmlDeserializer = (XMLDeserializer) manager.lookup(XMLDeserializer.ROLE);
                        xmlDeserializer.setConsumer(this);

                        // BodyTag Support
                        XMLConsumer backup = this.currentConsumer;
                        if (tag instanceof BodyTag) {
                            SaxBuffer content = new SaxBuffer();
                            this.currentConsumer = content;
                            ((BodyTag)tag).setBodyContent(new BodyContent(content, backup));
                            ((BodyTag)tag).doInitBody();
                        }

                        do {
                            xmlDeserializer.deserialize(saxFragment);
                        } while (iterTag.doAfterBody() != Tag.SKIP_BODY);

                        // BodyTag Support
                        if (tag instanceof BodyTag) {
                            this.currentConsumer = backup;
                        }

                    } catch (ServiceException e) {
                        throw new SAXException("Can't obtain XMLDeserializer", e);
                    } finally {
                        if (xmlDeserializer != null) {
                            manager.release(xmlDeserializer);
                        }
                    }
                }
                tag.doEndTag(namespaceURI, localName, qName);
                currentTag = tag.getParent();

                if (tag == this.currentConsumer) {
                    popConsumer();
                }
            } finally {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("endElement: Release tag " + qName);
                }

                tagSelector.release(tag);
                tagNamespaceSelector.release(tagSelector);

                if (transformerSelector != null && tag instanceof XMLProducer) {
                    getLogger().debug("endElement: Release transformer");
                    Transformer transformer = (Transformer) tagTransformerStack.pop();
                    transformerSelector.release(transformer);
                }
            }
        } else {
            this.currentConsumer.endElement(namespaceURI, localName, qName);
        }
    }

    public void startEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.endEntity(name);
    }

    public void skippedEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.skippedEntity(name);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.characters(ch, start, length);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.comment(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (this.skipLevel > 0) {
            return;
        }

        this.currentConsumer.ignorableWhitespace(ch, start, length);
    }


    //
    // Internal Implementation Methods
    //

    private void setupTag(Tag tag, String name, Attributes atts) throws SAXException {
        // Set Tag Parent
        tag.setParent(this.currentTag);

        // Set Tag XML Consumer
        if (tag instanceof XMLProducer) {
            XMLConsumer tagConsumer;
            if (transformerSelector != null) {
                Transformer tagTransformer = null;
                try {
                    // Add additional (Tag)Transformer to the output of the Tag
                    tagTransformer = (Transformer) transformerSelector.select(transformerHint);
                    tagTransformerStack.push(tagTransformer);
                    tagTransformer.setConsumer(currentConsumer);
                    tagTransformer.setup(this.resolver, this.objectModel, null, this.parameters);
                } catch (SAXException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SAXException("Failed to setup tag transformer " + transformerHint, e);
                }
                tagConsumer = tagTransformer;
            } else {
                tagConsumer = this.currentConsumer;
            }

            ((XMLProducer) tag).setConsumer(tagConsumer);
        }

        // Setup Tag
        try {
            tag.setup(this.resolver, this.objectModel, this.parameters);
        } catch (IOException e) {
            throw new SAXException("Could not set up tag " + name, e);
        }

        if (tag instanceof XMLConsumer) {
            this.currentConsumer = (XMLConsumer) tag;
        }
        this.currentTag = tag;

        // Set Tag-Attributes, Attributes are mapped to the coresponding Tag method
        for (int i = 0; i < atts.getLength(); i++) {
            String attributeName = atts.getLocalName(i);
            String attributeValue = atts.getValue(i);
            this.paramArray[0] = attributeValue;
            try {
                Method method = getWriteMethod(tag.getClass(), attributeName);
                method.invoke(tag, this.paramArray);
            } catch (Throwable e) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Tag " + name + " attribute " + attributeName + " not set", e);
                }
            }
        }
    }

    /**
     * Start recording for the iterator tag.
     */
    private void startRecording() throws SAXException {
        try {
            this.xmlSerializer = (XMLSerializer) manager.lookup(XMLSerializer.ROLE);
        } catch (ServiceException e) {
            throw new SAXException("Can't lookup XMLSerializer", e);
        }

        this.currentConsumerBackup = this.currentConsumer;
        this.currentConsumer = this.xmlSerializer;
        this.recordingLevel = 1;
    }

    /**
     * End recording for the iterator tag and returns recorded XML fragment.
     */
    private Object endRecording() {
        // Restore XML Consumer
        this.currentConsumer = this.currentConsumerBackup;
        this.currentConsumerBackup = null;

        // Get XML Fragment
        Object saxFragment = this.xmlSerializer.getSAXFragment();

        // Release Serializer
        this.manager.release(this.xmlSerializer);
        this.xmlSerializer = null;

        return saxFragment;
    }

    /**
     * Find previous XML consumer when processing of current consumer
     * is complete.
     */
    private void popConsumer() {
        Tag loop = this.currentTag;
        for (; loop != null; loop = loop.getParent()) {
            if (loop instanceof XMLConsumer) {
                this.currentConsumer = (XMLConsumer) loop;
                return;
            }
        }

        this.currentConsumer = this.xmlConsumer;
    }

    private static Method getWriteMethod(Class type, String propertyName) throws IntrospectionException {
        Map map = getWriteMethodMap(type);
        Method method = (Method) map.get(propertyName);
        if (method == null) {
            throw new IntrospectionException("No such property: " + propertyName);
        }
        return method;
    }

    private static Map getWriteMethodMap(Class beanClass) throws IntrospectionException {
        Map map = (Map) TAG_PROPERTIES_MAP.get(beanClass);
        if (map != null) {
            return map;
        }

        BeanInfo info = Introspector.getBeanInfo(beanClass);
        if (info != null) {
            PropertyDescriptor pds[] = info.getPropertyDescriptors();
            map = new HashMap(pds.length * 4 / 3, 1);
            for (int i = 0; i < pds.length; i++) {
                PropertyDescriptor pd = pds[i];
                String name = pd.getName();
                Method method = pd.getWriteMethod();
                Class type = pd.getPropertyType();
                if (type != String.class) // only String properties
                    continue;
                map.put(name, method);
            }
        }
        TAG_PROPERTIES_MAP.put(beanClass, map);
        return map;
    }
}
