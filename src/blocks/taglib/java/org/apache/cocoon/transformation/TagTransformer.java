/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.map.StaticBucketMap;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Transformer which implements the dynamic Tag functionalty.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: TagTransformer.java,v 1.5 2004/02/06 22:56:46 joerg Exp $
 */
public class TagTransformer
    extends AbstractXMLProducer
    implements Transformer, Serviceable, Configurable, Disposable, Recyclable {

    private int recordingLevel = 0;
    private int skipLevel = 0;
    private String transformerHint;

    private ArrayStack tagStack = new ArrayStack();
    private ArrayStack tagSelectorStack = new ArrayStack();
    private ArrayStack tagTransformerStack = new ArrayStack();
    private ServiceSelector tagNamespaceSelector;
    private ServiceSelector transformerSelector;
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
    private String[] paramArray = new String[1];
    /** Map for caching Tag Introspection */
    private static Map writeMethodMap = new StaticBucketMap();

    /**
     * SAX Event handling
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.characters(ch, start, length);
    }

    /**
     * SAX Event handling
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.comment(ch, start, length);
    }

    /**
     * Avalon Serviceable Interface
     * @param manager The Avalon Service Manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        tagNamespaceSelector = (ServiceSelector) manager.lookup(Tag.ROLE + "Selector");
    }

    /**
     * Avalon Configurable Interface
     */
    public void configure(Configuration conf) throws ConfigurationException {
        transformerHint = conf.getChild("transformer-hint").getValue(null);
        if (transformerHint != null) {
            try {
                transformerSelector = (ServiceSelector) manager.lookup(Transformer.ROLE + "Selector");
            } catch (ServiceException e) {
                String message = "can't lookup transformer";
                getLogger().error(message, e);
                throw new ConfigurationException(message, e);
            }
        }
    }

    /**
     * SAX Event handling
     */
    public void endCDATA() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.endCDATA();
    }

    /**
     * SAX Event handling
     */
    public void endDocument() throws SAXException {
        currentConsumer.endDocument();
        getLogger().debug("endDocument");
    }

    /**
     * SAX Event handling
     */
    public void endDTD() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.endDTD();
    }

    /**
     * SAX Event handling
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        Object saxFragment = null;
        // recording for iteration ?
        if (recordingLevel > 0) {
            if (--recordingLevel > 0) {
                currentConsumer.endElement(namespaceURI, localName, qName);
                return;
            }
            //recording finished
            currentConsumer = currentConsumerBackup;
            saxFragment = xmlSerializer.getSAXFragment();
            manager.release(xmlSerializer);
            xmlSerializer = null;
        }

        if (skipLevel > 0) {
            --skipLevel;

            if (skipLevel > 0) {
                return;
            }
        }

        Tag tag = (Tag) tagStack.pop();
        if (tag != null) {
            ServiceSelector tagSelector = (ServiceSelector)tagSelectorStack.pop();
            try {
                if (saxFragment != null) {
                    //start Iteration
                    IterationTag iterTag = (IterationTag) tag;
                    XMLDeserializer xmlDeserializer = null;
                    try {
                        xmlDeserializer = (XMLDeserializer) manager.lookup(XMLDeserializer.ROLE);
                        xmlDeserializer.setConsumer(this);
                        do {
                            xmlDeserializer.deserialize(saxFragment);
                        } while (iterTag.doAfterBody() != Tag.SKIP_BODY);

                    } catch (ServiceException e) {
                        throw new SAXException("lookup XMLDeserializer failed", e);
                    }
                    finally {
                        if (xmlDeserializer != null)
                            manager.release(xmlDeserializer);
                    }
                }
                tag.doEndTag(namespaceURI, localName, qName);
                currentTag = tag.getParent();

                if (tag == currentConsumer) {
                    // search next XMLConsumer
                    Tag loop = currentTag;
                    for (; loop != null; loop = loop.getParent()) {
                        if (loop instanceof XMLConsumer)
                            break;
                    }
                    if (loop != null) {
                        currentConsumer = (XMLConsumer) loop;
                    } else {
                        currentConsumer = this.xmlConsumer;
                    }
                }
            } finally {
                getLogger().debug("endElement: release Tag");
                tagSelector.release(tag);

                tagNamespaceSelector.release(tagSelector);

                if (transformerSelector != null && tag instanceof XMLProducer) {
                    getLogger().debug("endElement: release transformer");
                    Transformer transformer = (Transformer) tagTransformerStack.pop();
                    transformerSelector.release(transformer);
                }
            }
        } else {
            currentConsumer.endElement(namespaceURI, localName, qName);
        }
    }

    /**
     * SAX Event handling
     */
    public void endEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.endEntity(name);
    }

    /**
     * SAX Event handling
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.endPrefixMapping(prefix);
    }

    /**
     * SAX Event handling
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.ignorableWhitespace(ch, start, length);
    }

    /**
     * SAX Event handling
     */
    public void processingInstruction(String target, String data) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.processingInstruction(target, data);
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        recordingLevel = 0;
        skipLevel = 0;
        resolver = null;
        objectModel = null;
        parameters = null;
        currentTag = null;
        currentConsumer = null;
        currentConsumerBackup = null;

        // can happen if there was a error in the pipeline
        if (xmlSerializer != null) {
            manager.release(xmlSerializer);
            xmlSerializer = null;
        }

        while (!tagStack.isEmpty()) {
            Tag tag = (Tag) tagStack.pop();
            if (tag == null)
                continue;
            ServiceSelector tagSelector = (ServiceSelector)tagSelectorStack.pop();
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

    /*
     * @see XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.currentConsumer = consumer;
        super.setConsumer(consumer);
    }

    /**
     * SAX Event handling
     */
    public void setDocumentLocator(org.xml.sax.Locator locator) {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.setDocumentLocator(locator);
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
     * SAX Event handling
     */
    public void skippedEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.skippedEntity(name);
    }

    /**
     * SAX Event handling
     */
    public void startCDATA() throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.startCDATA();
    }

    /**
     * SAX Event handling
     */
    public void startDocument() throws SAXException {
        currentConsumer.startDocument();
    }

    /**
     * SAX Event handling
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.startDTD(name, publicId, systemId);
    }

    /**
     * SAX Event handling
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        // recording for iteration ?
        if (recordingLevel > 0) {
            ++recordingLevel;
            currentConsumer.startElement(namespaceURI, localName, qName, atts);
            return;
        }
        // If we are skipping the body of a Tag
        if (skipLevel > 0) {
            // Remember to skip one more end element
            skipLevel++;
            // and ignore this start element
            return;
        }

        Tag tag = null;
        if (namespaceURI != null && namespaceURI.length() > 0) {
            ServiceSelector tagSelector = null;
            Transformer tagTransformer = null;
            try {
                tagSelector = (ServiceSelector) tagNamespaceSelector.select(namespaceURI);
                tagSelectorStack.push(tagSelector);

                // namespace matches tag library, lookup tag now.
                tag = (Tag) tagSelector.select(localName);

                // tag found, setup Tag and connect it to pipeline
                tag.setParent(currentTag);
                tag.setup(this.resolver, this.objectModel, this.parameters);

                if (tag instanceof XMLProducer) {
                    if (transformerSelector != null) {
                        // add additional (Tag)Transformer to the output of the Tag
                        tagTransformer = (Transformer) transformerSelector.select(transformerHint);
                        tagTransformerStack.push(tagTransformer);
                        tagTransformer.setup(this.resolver, this.objectModel, null, this.parameters);
                        ((XMLProducer) tag).setConsumer(tagTransformer);
                        tagTransformer.setConsumer(currentConsumer);
                    }
                }
                if (tag instanceof XMLConsumer) {
                    currentConsumer = (XMLConsumer) tag;
                }

                currentTag = tag;

                // Set Tag-Attributes, Attributes are mapped to the coresponding Tag method
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getLocalName(i);
                    String attributeValue = atts.getValue(i);
                    paramArray[0] = attributeValue;
                    try {
                        Method method = getWriteMethod(tag.getClass(), attributeName);
                        method.invoke(tag, paramArray);
                    } catch (Throwable e) {
                        if (getLogger().isInfoEnabled())
                            getLogger().info("startElement(" + localName + "): Attribute " + attributeName + " not set", e);
                    }
                }
            } catch (Exception ignore) {
                // No namespace or tag found, process it as normal element (tag == null)
            }
        }

        tagStack.push(tag);
        if (tag == null) {
            currentConsumer.startElement(namespaceURI, localName, qName, atts);
        } else {
            int eval = tag.doStartTag(namespaceURI, localName, qName, atts);
            switch (eval) {
                case Tag.EVAL_BODY :
                    skipLevel = 0;
                    if (tag instanceof IterationTag) {
                        // start recording for IterationTag
                        try {
                            xmlSerializer = (XMLSerializer) manager.lookup(XMLSerializer.ROLE);
                            currentConsumerBackup = currentConsumer;
                            currentConsumer = xmlSerializer;
                            recordingLevel = 1;
                        } catch (ServiceException e) {
                            throw new SAXException("lookup XMLSerializer failed", e);
                        }
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
    }

    /**
     * SAX Event handling
     */
    public void startEntity(String name) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.startEntity(name);
    }

    /**
     * SAX Event handling
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // If we are skipping the body of a tag, ignore this...
        if (skipLevel > 0)
            return;

        currentConsumer.startPrefixMapping(prefix, uri);
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

    private static Method getWriteMethod(Class type, String propertyName) throws IntrospectionException {
        Map map = getWriteMethodMap(type);
        Method method = (Method) map.get(propertyName);
        if (method == null)
            throw new IntrospectionException("No such property: " + propertyName);
        return method;
    }

    private static Map getWriteMethodMap(Class beanClass) throws IntrospectionException {
        Map map = (Map) writeMethodMap.get(beanClass);
        if (map != null)
            return map;

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
        writeMethodMap.put(beanClass, map);
        return map;
    }
}
