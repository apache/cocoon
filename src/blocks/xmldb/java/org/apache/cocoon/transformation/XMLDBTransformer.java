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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.TraxErrorHandler;

import org.apache.excalibur.source.SourceValidity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XUpdateQueryService;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

/**
 * This transformer allows to perform resource creation, deletion, and
 * XUpdate command execution in XML:DB. All operations are performed either
 * in <code>base</code> collection, or context collection, which
 * is specified as <code>collection</code> attribute on the <code>query</code>
 * element. Context collection must be specified relative to the base collection.
 *
 * <p>Definition:</p>
 * <pre>
 * &lt;map:transformer name="xmldb" src="org.apache.cocoon.transformation.XMLDBTransformer"&gt;
 *   &lt;!-- Optional driver parameter. Uncomment if you want transformer to register a database.
 *   &lt;driver&gt;org.apache.xindice.client.xmldb.DatabaseImpl&lt;/driver&gt;
 *   --&gt;
 *   &lt;base&gt;xmldb:xindice:///db/collection&lt;/base&gt;
 * &lt;/map:transformer&gt;
 * </pre>
 *
 * <p>Invocation:</p>
 * <pre>
 * &lt;map:transform type="xmldb"&gt;
 *   &lt;map:parameter name="base" value="xmldb:xindice:///db/collection"/&gt;
 * &lt;/map:transform&gt;
 * </pre>
 *
 * <p>Input XML document example:</p>
 * <pre>
 * &lt;page xmlns:xmldb="http://apache.org/cocoon/xmldb/1.0"&gt;
 *   ...
 *   &lt;p&gt;Create XML resource in base collection with specified object ID&lt;/p&gt;
 *   &lt;xmldb:query type="create" oid="xmldb-object-id"&gt;
 *     &lt;page&gt;
 *       XML Object body
 *     &lt;/page&gt;
 *   &lt;/xmldb:query&gt;
 *
 *   &lt;p&gt;Delete XML resource from the base collection with specified object ID&lt;/p&gt;
 *   &lt;xmldb:query type="delete" oid="xmldb-object-id"/&gt;
 *
 *   &lt;p&gt;Update XML resource with specified object ID&lt;/p&gt;
 *   &lt;xmldb:query type="update" oid="xmldb-object-id"&gt;
 *     &lt;xu:modifications version="1.0" xmlns:xu="http://www.xmldb.org/xupdate"&gt;
 *       &lt;xu:remove select="/person/phone[@type = 'home']"/&gt;
 *       &lt;xu:update select="/person/phone[@type = 'work']"&gt;
 *         480-300-3003
 *       &lt;/xu:update&gt;
 *       &lt;/xu:modifications&gt;
 *   &lt;/xmldb:query&gt;
 *
 *   &lt;p&gt;Create collection nested into the base collection&lt;/p&gt;
 *   &lt;xmldb:query type="create" oid="inner/"/&gt;
 *
 *   &lt;p&gt;Create XML resource in context collection with specified object ID&lt;/p&gt;
 *   &lt;xmldb:query type="create" collection="inner" oid="xmldb-object-id"&gt;
 *     &lt;page&gt;
 *       XML Object body
 *     &lt;/page&gt;
 *   &lt;/xmldb:query&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Output XML document example:</p>
 * <pre>
 * &lt;page xmlns:xmldb="http://apache.org/cocoon/xmldb/1.0"&gt;
 *   ...
 *   &lt;xmldb:query type="create" oid="xmldb-object-id" result="success"/&gt;
 *
 *   &lt;xmldb:query type="delete" oid="xmldb-object-id" result="success"/&gt;
 *
 *   &lt;xmldb:query type="update" oid="xmldb-object-id" result="failure"&gt;
 *     Resource xmldb-object-id is not found
 *   &lt;/xmldb:query&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Known bugs and limitations:</p>
 * <ul>
 * <li>No namespaces with Xalan (see AbstractTextSerializer)</li>
 * </ul>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XMLDBTransformer.java,v 1.10 2004/05/03 15:45:43 vgritsenko Exp $
 */
public class XMLDBTransformer extends AbstractTransformer
        implements CacheableProcessingComponent, Configurable, Initializable {

    private static String XMLDB_URI = "http://apache.org/cocoon/xmldb/1.0";
    private static String XMLDB_QUERY_ELEMENT = "query";
    private static String XMLDB_QUERY_TYPE_ATTRIBUTE = "type";
    private static String XMLDB_QUERY_CONTEXT_ATTRIBUTE = "collection";
    private static String XMLDB_QUERY_OID_ATTRIBUTE = "oid";
    private static String XMLDB_QUERY_RESULT_ATTRIBUTE = "result";

    /** The trax <code>TransformerFactory</code> used by this transformer. */
    private SAXTransformerFactory tfactory = null;
    private Properties format = new Properties();

    /** The map of namespace prefixes. */
    private Map prefixMap = new HashMap();

    /** XML:DB driver class name (optional) */
    private String driver = null;

    /** Default collection name. */
    private String default_base;

    /** Current collection name. */
    private String local_base;

    /** Current collection name. */
    private String xbase;

    /** Current collection. */
    private Collection collection;

    /** Operation. One of: create, delete, update. */
    private String operation;

    /** Document ID. Can be null if update or insert is performed on collection. */
    private String key;

    /** Result of current operation. Success or failure. */
    private String result;

    /** Message in case current operation failed. */
    private String message;

    private StringWriter queryWriter;
    private TransformerHandler queryHandler;

    /** True when inside &lt;query&gt; element. */
    private boolean processing;


    public XMLDBTransformer() {
        format.put(OutputKeys.ENCODING, "utf-8");
        format.put(OutputKeys.INDENT, "no");
        format.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.driver = configuration.getChild("driver").getValue(null);
        if (driver == null) {
            getLogger().debug("Driver parameter is missing. Transformer will not initialize database.");
        }

        this.default_base = configuration.getChild("base").getValue(null);
    }

    /**
     * Initializes XML:DB database instance if driver class was configured.
     */
    public void initialize() throws Exception {
        if (driver != null) {
            Class c = Class.forName(driver);
            Database database = (Database)c.newInstance();
            DatabaseManager.registerDatabase(database);
        }
    }

    /** Setup the transformer. */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        this.local_base = par.getParameter("base", this.default_base);
        if (this.local_base == null) {
            throw new ProcessingException("Required base parameter is missing. Syntax is: xmldb:xindice:///db/collection");
        }

        try {
            this.collection = DatabaseManager.getCollection(this.local_base);
        } catch (XMLDBException e) {
            throw new ProcessingException("Could not get collection " + this.local_base + ": " + e.errorCode, e);
        }

        if (this.collection == null) {
            throw new ResourceNotFoundException("Collection " + this.local_base + " does not exist");
        }
    }

    /**
     * Helper for TransformerFactory.
     */
    protected SAXTransformerFactory getTransformerFactory() {
        if (tfactory == null)  {
            tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            tfactory.setErrorListener(new TraxErrorHandler(getLogger()));
        }
        return tfactory;
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>null</code> if the component
     *              is currently not cacheable.
     */
    public Serializable getKey() {
        return null;
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
        return null;
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!processing) {
            super.startPrefixMapping(prefix,uri);
            prefixMap.put(prefix,uri);
        } else if (this.queryHandler != null) {
            this.queryHandler.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (!processing) {
            super.endPrefixMapping(prefix);
            prefixMap.remove(prefix);
        } else if (this.queryHandler != null){
            this.queryHandler.endPrefixMapping(prefix);
        }
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (!processing) {
            if (XMLDB_URI.equals(uri) && XMLDB_QUERY_ELEMENT.equals(loc)){

                this.operation = a.getValue(XMLDB_QUERY_TYPE_ATTRIBUTE);
                if (!"create".equals(operation) && !"delete".equals(operation) && !"update".equals(operation)) {
                    throw new SAXException("Supported operation types are: create, delete, update");
                }

                this.key = a.getValue(XMLDB_QUERY_OID_ATTRIBUTE);
                if ("delete".equals(operation) && this.key == null) {
                    throw new SAXException("Object ID attribute is missing on query element");
                }

                this.xbase = a.getValue(XMLDB_QUERY_CONTEXT_ATTRIBUTE);

                // Start processing
                result = "failure";
                message = null;
                processing = true;

                if ("create".equals(operation) && this.key != null && this.key.endsWith("/")) {
                } else if (!"delete".equals(operation)) {
                    // Prepare SAX query writer
                    queryWriter = new StringWriter(256);
                    try {
                        this.queryHandler = getTransformerFactory().newTransformerHandler();
                        this.queryHandler.setResult(new StreamResult(queryWriter));
                        this.queryHandler.getTransformer().setOutputProperties(format);
                    } catch (TransformerConfigurationException e) {
                        throw new SAXException("Failed to get transformer handler", e);
                    }

                    // Start query document
                    this.queryHandler.startDocument();
                    Iterator i = prefixMap.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry entry = (Map.Entry)i.next();
                        this.queryHandler.startPrefixMapping((String)entry.getKey(), (String)entry.getValue());
                    }
                }
            } else {
                super.startElement(uri, loc, raw, a);
            }
        } else if (this.queryHandler != null) {
            this.queryHandler.startElement(uri, loc, raw, a);
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (!processing) {
            super.endElement(uri,loc,raw);
        } else {
            if (XMLDB_URI.equals(uri) && XMLDB_QUERY_ELEMENT.equals(loc)) {
                processing = false;

                String document = null;
                if (this.queryHandler != null) {
                    // Finish building query. Remove existing prefix mappings.
                    Iterator i = prefixMap.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry entry = (Map.Entry) i.next();
                        this.queryHandler.endPrefixMapping((String)entry.getKey());
                    }
                    this.queryHandler.endDocument();
                    document = this.queryWriter.toString();
                }

                // Perform operation
                Collection collection = null;
                try {
                    // Obtain collection for the current operation
                    collection = (xbase != null)? DatabaseManager.getCollection(local_base + "/" + xbase) : this.collection;

                    if ("create".equals(operation)) {
                        if (key != null && key.endsWith("/")) {
                            try {
                                // Cut trailing '/'
                                String k = this.key.substring(0, this.key.length() - 1);
                                CollectionManagementService service =
                                        (CollectionManagementService) collection.getService("CollectionManagementService", "1.0");
                                service.createCollection(k);
                                result = "success";
                            } catch (XMLDBException e) {
                                message = "Failed to create collection " + this.key + ": " + e.errorCode;
                                getLogger().error(message, e);
                            }
                        } else {
                            try {
                                if (key == null) {
                                    key = collection.createId();
                                }
                                // Support of binary objects can be added. Content can be obtained using Source.
                                Resource resource = collection.createResource(key, "XMLResource");
                                resource.setContent(document);
                                collection.storeResource(resource);
                                result = "success";
                                key = resource.getId();
                            } catch (XMLDBException e) {
                                message = "Failed to create resource " + key + ": " + e.errorCode;
                                getLogger().debug(message, e);
                            }
                        }
                    } else if ("delete".equals(operation)) {
                        try {
                            Resource resource = collection.getResource(this.key);
                            if (resource == null) {
                                message = "Resource " + this.key + " does not exist";
                                getLogger().debug(message);
                            } else {
                                collection.removeResource(resource);
                                result = "success";
                            }
                        } catch (XMLDBException e) {
                            message = "Failed to delete resource " + key + ": " + e.errorCode;
                            getLogger().debug(message, e);
                        }
                    } else if ("update".equals(operation)) {
                        try {
                            XUpdateQueryService service =
                                    (XUpdateQueryService) collection.getService("XUpdateQueryService", "1.0");
                            long count = (this.key == null)?
                                    service.update(document) : service.updateResource(this.key, document);
                            message = count + " entries updated.";
                            result = "success";
                        } catch (XMLDBException e) {
                            message = "Failed to update resource " + key + ": " + e.errorCode;
                            getLogger().debug(message, e);
                        }
                    }
                } catch (XMLDBException e) {
                    message = "Failed to get context collection for the query (base: " + local_base + ", context: " + xbase + "): " + e.errorCode;
                    getLogger().debug(message, e);
                } finally {
                    if (xbase != null) {
                        try {
                            collection.close();
                        } catch (XMLDBException ignored) {
                        }
                    }
                }


                // Report result
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute(null, XMLDB_QUERY_OID_ATTRIBUTE,
                        XMLDB_QUERY_OID_ATTRIBUTE, "CDATA", this.key);
                attrs.addAttribute(null, XMLDB_QUERY_TYPE_ATTRIBUTE,
                        XMLDB_QUERY_TYPE_ATTRIBUTE, "CDATA", this.operation);
                attrs.addAttribute(null, XMLDB_QUERY_RESULT_ATTRIBUTE,
                        XMLDB_QUERY_RESULT_ATTRIBUTE, "CDATA", result);
                super.startElement(uri, loc, raw, attrs);
                if (message != null) {
                    super.characters(message.toCharArray(), 0, message.length());
                }
                super.endElement(uri, loc, raw);
            } else if (this.queryHandler != null) {
                this.queryHandler.endElement(uri, loc, raw);
            }
        }
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len) throws SAXException {
        if (!processing) {
            super.characters(c,start,len);
        } else if (this.queryHandler != null) {
            this.queryHandler.characters(c,start,len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
        if (!processing) {
            super.ignorableWhitespace(c,start,len);
        } else if (this.queryHandler != null) {
            this.queryHandler.ignorableWhitespace(c,start,len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data) throws SAXException {
        if (!processing) {
            super.processingInstruction(target,data);
        } else if (this.queryHandler != null) {
            this.queryHandler.processingInstruction(target,data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name) throws SAXException {
        if (!processing) {
            super.skippedEntity(name);
        } else if (this.queryHandler != null) {
            this.queryHandler.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        if (!processing) {
            super.startDTD(name, publicId, systemId);
        } else {
            throw new SAXException(
                "Recieved startDTD after beginning SVG extraction process."
            );
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD() throws SAXException {
        if (!processing) {
            super.endDTD();
        } else {
            throw new SAXException("Recieved endDTD after xmldb element.");
        }
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name) throws SAXException {
        if (!processing) {
            super.startEntity(name);
        } else if (this.queryHandler != null) {
            this.queryHandler.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name) throws SAXException {
        if (!processing) {
            super.endEntity(name);
        } else if (this.queryHandler != null) {
            this.queryHandler.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA() throws SAXException {
        if (!processing) {
            super.startCDATA();
        } else if (this.queryHandler != null) {
            this.queryHandler.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() throws SAXException {
        if (!processing) {
            super.endCDATA();
        } else if (this.queryHandler != null) {
            this.queryHandler.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len) throws SAXException {
        if (!processing) {
            super.comment(ch, start, len);
        } else if (this.queryHandler != null) {
            this.queryHandler.comment(ch, start, len);
        }
    }

    public void recycle() {
        this.prefixMap.clear();
        this.queryHandler = null;
        this.queryWriter = null;

        try {
            if (collection != null) {
                collection.close();
            }
        } catch (XMLDBException e) {
            getLogger().error("Failed to close collection " + this.local_base + ". Error " + e.errorCode, e);
        }
        collection = null;
        super.recycle();
    }
}
