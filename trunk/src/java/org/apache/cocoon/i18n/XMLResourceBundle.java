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
package org.apache.cocoon.i18n;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of <code>Bundle</code> interface for XML resources. Represents a single XML message bundle.
 *
 * @author <a href="mailto:mengelhart@earthtrip.com">Mike Engelhart</a>
 * @author <a href="mailto:neeme@one.lv">Neeme Praks</a>
 * @author <a href="mailto:oleg@one.lv">Oleg Podolsky</a>
 * @author <a href="mailto:mattam@netcourrier.com">Matthieu Sozeau</a>
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @version CVS $Id: XMLResourceBundle.java,v 1.6 2004/03/08 14:02:47 cziegeler Exp $
 */
public class XMLResourceBundle extends ResourceBundle
                               implements Bundle {

    /** DOM factory */
    protected static final DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();

    /** Logger */
    protected Logger logger;

    /** Cache for storing string values for existing XPaths */
    private Hashtable cache = new Hashtable();

    /** Cache for storing non-existing XPaths */
    private Map cacheNotFound = new HashMap();

    /** Bundle name */
    private String name = "";

    /** DOM-tree containing the bundle content */
    private Document doc;

    /** Locale of the bundle */
    private Locale locale;

    /** Parent of the current bundle */
    protected XMLResourceBundle parent = null;

    /** Component Manager */
    protected ServiceManager manager = null;

    /** XPath Processor */
    private XPathProcessor processor = null;


    /**
     * Compose this instance
     *
     * @param manager The <code>ComponentManager</code> instance
     * @throws ServiceException if XPath processor is not found
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.processor = (XPathProcessor) this.manager.lookup(XPathProcessor.ROLE);
    }

    /**
     * Implements Disposable interface for this class.
     */
    public void dispose() {
        this.manager.release(this.processor);
        this.processor = null;
    }

    /**
     * Implements LogEnabled interface for this class.
     *
     * @param logger the logger object
     */
    public void enableLogging(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Initalize the bundle
     *
     * @param name name of the bundle
     * @param fileName name of the XML source file
     * @param locale locale
     * @param parent parent bundle of this bundle
     * @param cacheAtStartup cache all the keys when constructing?
     *
     * @throws IOException if an IO error occurs while reading the file
     * @throws ParserConfigurationException if no parser is configured
     * @throws SAXException if an error occurs while parsing the file
     */
    public void init(String name, String fileName, Locale locale, XMLResourceBundle parent, boolean cacheAtStartup)
    throws IOException, ParserConfigurationException, SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("Constructing XMLResourceBundle: " + name + ", locale: " + locale);
        }

        this.name = name;
        this.doc = loadResourceBundle(fileName);
        this.locale = locale;
        this.parent = parent;

        if (cacheAtStartup) {
            Node root = doc.getDocumentElement();
            cacheAll(root, "/" + root.getNodeName());
        }
    }

    /**
     * Load the DOM tree, based on the file name.
     *
     * @param fileName name of the XML source file
     *
     * @return the DOM tree
     *
     * @exception IOException if an IO error occurs while reading the file
     * @exception ParserConfigurationException if no parser is configured
     * @exception SAXException if an error occurs while parsing the file
     */
    protected synchronized Document loadResourceBundle(String fileName)
    throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Source source = null;
        SourceResolver resolver = null;

        try {
            resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(fileName);
            return builder.parse(new InputSource(source.getInputStream()));
        } catch (Exception e) {
            logger.warn("XMLResourceBundle: Non excalibur-source " + fileName, e);
        } finally {
            resolver.release(source);
            manager.release(resolver);
        }

        // Fallback try
        return builder.parse(fileName);
    }

    /**
     * Gets the name of the bundle.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the source DOM tree of the bundle.
     *
     * @return the DOM tree
     */
    public Document getDocument() {
        return this.doc;
    }

    /**
     * Gets the locale of the bundle.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Does the &quot;key-not-found-cache&quot; contain such key?
     *
     * @param key the key to the value to be returned
     *
     * @return true if contains, false otherwise
     */
    private boolean cacheNotFoundContains(String key) {
        return cacheNotFound.containsKey(key);
    }

    /**
     * Cache the key and value in &quot;key-cache&quot;.
     *
     * @param key the key
     * @param value the value
     */
    private void cacheKey(String key, Node value) {
        cache.put(key, value);
    }

    /**
     * Cache the key in &quot;key-not-found-cache&quot;.
     *
     * @param key the key
     */
    private void cacheNotFoundKey(String key) {
        cacheNotFound.put(key, "");
    }

    /**
     * Gets the value by the key from the &quot;key-cache&quot;.
     *
     * @param key the key
     * @return the value
     */
    private Node getFromCache(String key) {
        Object value = cache.get(key);
        return (Node)value;
    }

    /**
     * Steps through the bundle tree and stores all text element values in bundle's cache. Also stores attributes for
     * all element nodes.
     *
     * @param parent parent node, must be an element
     * @param pathToParent XPath to the parent node
     */
    private void cacheAll(Node parent, String pathToParent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Caching all messages");
        }

        NodeList children = parent.getChildNodes();
        int childnum = children.getLength();

        for (int i = 0; i < childnum; i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                StringBuffer pathToChild = new StringBuffer(pathToParent).append('/').append(child.getNodeName());

                NamedNodeMap attrs = child.getAttributes();
                if (attrs != null) {
                    int attrnum = attrs.getLength();

                    for (int j = 0; j < attrnum; j++) {
                        Node temp = attrs.item(j);

                        if (!temp.getNodeName().equalsIgnoreCase("xml:lang")) {
                            pathToChild.append("[@").append(temp.getNodeName()).append("='").append(temp.getNodeValue())
                                       .append("']");
                        }
                    }
                }

                cacheKey(pathToChild.toString(), child);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("What we've cached: " + child.toString());
            }
        }
    }

    /**
     * Get value by key.
     *
     * @param key the key
     * @return the value
     */
    private Object _getObject(String key) {
        if (key == null) {
            return null;
        }

        Node value = getFromCache(key);
        if (value == null && !cacheNotFoundContains(key)) {
            if (doc != null) {
                value = (Node)_getObject(this.doc.getDocumentElement(), key);
            }

            if (value == null) {
                if (this.parent != null) {
                    value = (Node)this.parent._getObject(key);
                }
            }

            if (value != null) {
                cacheKey(key, value);
            } else {
                cacheNotFoundKey(key);
            }
        }

        return value;
    }

    /**
     * Get value by key from a concrete node.
     *
     * @param node the node
     * @param key the key
     *
     * @return the value
     */
    private Object _getObject(Node node, String key) {
        return _getNode(node, key);
    }

    /**
     * Get the node with the supplied XPath key, starting from concrete root node.
     *
     * @param rootNode the root node
     * @param key the key
     *
     * @return the node
     */
    private Node _getNode(Node rootNode, String key) {
        try {
            return this.processor.selectSingleNode(rootNode, key);
        } catch (Exception e) {
            logger.error("Error while locating resource with key: " + key, e);
        }

        return null;
    }

    /**
     * Return an Object by key. Implementation of the ResourceBundle abstract method.
     *
     * @param key the key
     *
     * @return the object
     *
     * @throws MissingResourceException on error
     */
    protected Object handleGetObject(String key)
    throws MissingResourceException {
        return _getObject(key);
    }

    /**
     * Return an enumeration of the keys. Implementation of the ResourceBundle abstract method.
     *
     * @return the enumeration of keys
     */
    public Enumeration getKeys() {
        return cache.keys();
    }
}
