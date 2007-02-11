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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * This class implements the xmldb:// pseudo-protocol and allows to get XML
 * content from an XML:DB enabled XML database.
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XMLDBSource.java,v 1.13 2004/03/05 13:02:36 bdelacretaz Exp $
 */
public class XMLDBSource extends AbstractLogEnabled
    implements Source, ModifiableSource, XMLizable {

    //
    // Static Strings used for XML Collection representation
    //

    /** Source namespace */
    public static final String URI = "http://apache.org/cocoon/xmldb/1.0";

    /** Source prefix */
    public static final String PREFIX = "xmldb";

    /** Root element <code>&lt;collections&gt;</code> */
    protected static final String COLLECTIONS  = "collections";
    /** Root element <code>&lt;xmldb:collections&gt;</code> (raw name) */
    protected static final String QCOLLECTIONS  = PREFIX + ":" + COLLECTIONS;
    /** Attribute <code>resources</code> on the root element indicates count of resources in the collection */
    protected static final String RESOURCE_COUNT_ATTR = "resources";
    /** Attribute <code>collections</code> on the root element indicates count of collections in the collection */
    protected static final String COLLECTION_COUNT_ATTR  = "collections";
    protected static final String COLLECTION_BASE_ATTR  = "base";

    /** Element <code>&lt;collection&gt;</code> */
    protected static final String COLLECTION  = "collection";
    /** Element <code>&lt;xmldb:collection&gt;</code> (raw name) */
    protected static final String QCOLLECTION  = PREFIX + ":" + COLLECTION;

    /** Element <code>&lt;resource&gt;</code> */
    protected static final String RESOURCE  = "resource";
    /** Element <code>&lt;resource&gt;</code> (raw name) */
    protected static final String QRESOURCE  = PREFIX + ":" + RESOURCE;
    /** Attribute <code>name</code> on the collection/resource element */
    protected static final String NAME_ATTR  = "name";

    /** Root element <code>&lt;results&gt;</code> */
    protected static final String RESULTSET = "results";
    /** Root element <code>&lt;xmldb:results&gt;</code> (raw name) */
    protected static final String QRESULTSET = PREFIX + ":" + RESULTSET;
    protected static final String QUERY_ATTR = "query";
    protected static final String RESULTS_COUNT_ATTR = "resources";

    /** Element <code>&lt;result&gt;</code> */
    protected static final String RESULT = "result";
    /** Element <code>&lt;xmldb:result&gt;</code> (raw name) */
    protected static final String QRESULT = PREFIX + ":" + RESULT;
    protected static final String RESULT_DOCID_ATTR = "docid";
    protected static final String RESULT_ID_ATTR = "id";

    protected static final String CDATA  = "CDATA";

    //
    // Instance variables
    //

    /** The requested URL */
    protected String url;

    /** The supplied user */
    protected String user;

    /** The supplied password */
    protected String password;

    /** The part of URL after # sign */
    protected String query;

    /** The System ID */
    protected String systemId;

    /** ServiceManager */
    protected ServiceManager manager;

    /** XMLDBOutputStream for writing to Modifiable resource */
    protected XMLDBOutputStream os;
    /**
     * The constructor.
     *
     * @param logger the Logger instance.
     * @param credential username and password
     * @param url the URL being queried.
     * @param manager component manager
     */
    public XMLDBSource(Logger logger,
                       SourceCredential credential,
                       String url,
                       ServiceManager manager) {
        enableLogging(logger);
        this.manager = manager;

        this.user = credential.getPrincipal();
        this.password = credential.getPassword();

        // Parse URL
        int start = url.indexOf('#');
        if (start != -1) {
            this.url = url.substring(0, start);
            this.query = url.substring(start + 1);
        } else {
            this.url = url;
        }
        this.os = null;
    }

    /**
     * Stream SAX events to a given ContentHandler. If the requested
     * resource is a collection, build an XML view of it.
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        try {
            if (url.endsWith("/")) {
                this.collectionToSAX(handler);
            } else {
                this.resourceToSAX(handler);
            }
        } catch (ProcessingException pe) {
            throw new SAXException("ProcessingException", pe);
        }
    }

    private void resourceToSAX(ContentHandler handler)
    throws SAXException, ProcessingException {

        final String col = url.substring(0, url.lastIndexOf('/'));
        final String res = url.substring(url.lastIndexOf('/') + 1);

        Collection collection = null;
        try {
            collection = DatabaseManager.getCollection(col, user, password);
            if (collection == null) {
                throw new ResourceNotFoundException("Document " + url + " not found");
            }

            XMLResource xmlResource = (XMLResource) collection.getResource(res);
            if (xmlResource == null) {
                throw new ResourceNotFoundException("Document " + url + " not found");
            }

            if (query != null) {
                // Query resource
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Querying resource " + res + " from collection " + url + "; query= " + this.query);
                }

                queryToSAX(handler, collection, res);
            } else {
                // Return entire resource
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Obtaining resource " + res + " from collection " + col);
                }

                xmlResource.getContentAsSAX(handler);
            }
        } catch (XMLDBException xde) {
            String error = "Unable to fetch content. Error "
                           + xde.errorCode + ": " + xde.getMessage();
            throw new SAXException(error, xde);
        } finally {
            if (collection != null) {
                try {
                    collection.close();
                } catch (XMLDBException ignored) {
                }
            }
        }
    }

    private void collectionToSAX(ContentHandler handler)
    throws SAXException, ProcessingException {

        AttributesImpl attributes = new AttributesImpl();

        Collection collection = null;
        try {
            collection = DatabaseManager.getCollection(url, user, password);
            if (collection == null) {
                throw new ResourceNotFoundException("Collection " + url +
                                                    " not found");
            }

            if (query != null) {
                // Query collection
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Querying collection " + url + "; query= " + this.query);
                }

                queryToSAX(handler, collection, null);
            } else {
                // List collection
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Listing collection " + url);
                }

                final String nresources = Integer.toString(collection.getResourceCount());
                attributes.addAttribute("", RESOURCE_COUNT_ATTR,
                                        RESOURCE_COUNT_ATTR, "CDATA", nresources);
                final String ncollections = Integer.toString(collection.getChildCollectionCount());
                attributes.addAttribute("", COLLECTION_COUNT_ATTR,
                                        COLLECTION_COUNT_ATTR, "CDATA", ncollections);
                attributes.addAttribute("", COLLECTION_BASE_ATTR,
                                        COLLECTION_BASE_ATTR, "CDATA", url);

                handler.startDocument();
                handler.startPrefixMapping(PREFIX, URI);
                handler.startElement(URI, COLLECTIONS, QCOLLECTIONS, attributes);

                // Print child collections
                String[] collections = collection.listChildCollections();
                for (int i = 0; i < collections.length; i++) {
                    attributes.clear();
                    attributes.addAttribute("", NAME_ATTR, NAME_ATTR, CDATA, collections[i]);
                    handler.startElement(URI, COLLECTION, QCOLLECTION, attributes);
                    handler.endElement(URI, COLLECTION, QCOLLECTION);
                }

                // Print child resources
                String[] resources = collection.listResources();
                for (int i = 0; i < resources.length; i++) {
                    attributes.clear();
                    attributes.addAttribute("", NAME_ATTR, NAME_ATTR, CDATA, resources[i]);
                    handler.startElement(URI, RESOURCE, QRESOURCE, attributes);
                    handler.endElement(URI, RESOURCE, QRESOURCE);
                }

                handler.endElement(URI, COLLECTIONS, QCOLLECTIONS);
                handler.endPrefixMapping(PREFIX);
                handler.endDocument();
            }
        } catch (XMLDBException xde) {
            String error = "Collection listing failed. Error " + xde.errorCode + ": " + xde.getMessage();
            throw new SAXException(error, xde);
        } finally {
            if (collection != null) {
                try {
                    collection.close();
                } catch (XMLDBException ignored) {
                }
            }
        }
    }

    private void queryToSAX(ContentHandler handler, Collection collection, String resource)
    throws SAXException {

        AttributesImpl attributes = new AttributesImpl();

        try {
            XPathQueryService service =
                    (XPathQueryService) collection.getService("XPathQueryService", "1.0");
            ResourceSet resultSet = (resource == null) ?
                    service.query(query) : service.queryResource(resource, query);

            attributes.addAttribute("", QUERY_ATTR, QUERY_ATTR, "CDATA", query);
            attributes.addAttribute("", RESULTS_COUNT_ATTR,
                                    RESULTS_COUNT_ATTR, "CDATA", Long.toString(resultSet.getSize()));

            handler.startDocument();
            handler.startPrefixMapping(PREFIX, URI);
            handler.startElement(URI, RESULTSET, QRESULTSET, attributes);

            IncludeXMLConsumer includeHandler = new IncludeXMLConsumer(handler);

            // Print search results
            ResourceIterator results = resultSet.getIterator();
            while (results.hasMoreResources()) {
                XMLResource result = (XMLResource)results.nextResource();

                final String id = result.getId();
                final String documentId = result.getDocumentId();

                attributes.clear();
                if (id != null) {
                    attributes.addAttribute("", RESULT_ID_ATTR, RESULT_ID_ATTR,
                                            CDATA, id);
                }
                if (documentId != null) {
                    attributes.addAttribute("", RESULT_DOCID_ATTR, RESULT_DOCID_ATTR,
                                            CDATA, documentId);
                }

                handler.startElement(URI, RESULT, QRESULT, attributes);
                result.getContentAsSAX(includeHandler);
                handler.endElement(URI, RESULT, QRESULT);
            }

            handler.endElement(URI, RESULTSET, QRESULTSET);
            handler.endPrefixMapping(PREFIX);
            handler.endDocument();
        } catch (XMLDBException xde) {
            String error = "Query failed. Error " + xde.errorCode + ": " + xde.getMessage();
            throw new SAXException(error, xde);
        }
    }

    public void recycle() {
        this.url = null;
        this.user = null;
        this.password = null;
        this.query = null;
    }

    public String getURI() {
        return url;
    }

    public long getContentLength() {
        return -1;
    }

    public long getLastModified() {
        return 0;
    }

    public boolean exists() {
        final String col = url.substring(0, url.lastIndexOf('/'));
        final String res = url.substring(url.lastIndexOf('/') + 1);
        boolean result = true;

        /* Ignore the query: we're just testing if the document exists. */
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Testing existence of resource `" + res + "' from collection `" + url + "'; query (ignored) = `" + this.query + "'");
        }

        Collection collection = null;
        try {
            collection = DatabaseManager.getCollection(col, user, password);
            if (collection == null) {
                result = false;
            } else {
                XMLResource xmlResource = (XMLResource) collection.getResource(res);
                if (xmlResource == null) {
                    result = false;
                }
            }
        } catch (XMLDBException xde) {
            result = false;
        } finally {
            if (collection != null) {
                try {
                    collection.close();
                } catch (XMLDBException ignored) {
                }
            }
        }

        return result;
    }

    public String getMimeType() {
        return null;
    }

    public String getScheme() {
        return url.substring(url.indexOf('/') - 1);
    }

    public SourceValidity getValidity() {
        return null;
    }

    public void refresh() {
    }

    /**
     * Get an InputSource for the given URL. Shamelessly stolen
     * from SitemapSource.
     *
     */
    public InputStream getInputStream()
    throws IOException {

        ComponentSelector serializerSelector = null;
        Serializer serializer = null;
        // this.manager does not have Serializer
        ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();
        try {
            serializerSelector = (ComponentSelector) manager.lookup(Serializer.ROLE + "Selector");
            serializer = (Serializer)serializerSelector.select("xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.setOutputStream(os);

            toSAX(serializer);

            return new ByteArrayInputStream(os.toByteArray());
//        } catch (ServiceException e) {
//            throw new CascadingIOException("Could not lookup pipeline components", e);
        } catch (ComponentException e) {
            throw new CascadingIOException("Could not lookup pipeline components", e);
        } catch (Exception e) {
            throw new CascadingIOException("Exception during processing of " + getURI(), e);
        } finally {
            if (serializer != null) {
                serializerSelector.release(serializer);
            }
            if (serializerSelector != null) {
                manager.release(serializerSelector);
            }
        }
    }

    /**
     * Return an {@link OutputStream} to write to.
     */
    public OutputStream getOutputStream() throws IOException, MalformedURLException {
        if (query != null) {
            throw new MalformedURLException("Cannot modify a resource that includes an XPATH expression");
        }
        this.os = new XMLDBOutputStream();
        return this.os;
    }

    private void writeOutputStream(String content) throws SourceException {
        String name = null;
        String base = null;

        try {
            if (this.url.endsWith("/")) {
                name = "";
                base = this.url.substring(0, this.url.length() - 1);
            } else {
                base = this.url.substring(0, this.url.lastIndexOf("/"));
                name = this.url.substring(this.url.lastIndexOf("/")+1);
            }
            Collection collection = DatabaseManager.getCollection(base);

            if (name.equals("")) {
                name = collection.createId();
                this.url += name;
            }
            Resource resource = collection.createResource(name, "XMLResource");

            resource.setContent(content);
            collection.storeResource(resource);

            getLogger().debug("Written to resource " + name);
        } catch (XMLDBException e) {
            String message = "Failed to create resource " + name + ": " + e.errorCode;
            getLogger().debug(message, e);
            throw new SourceException(message);
        }
    }

    /**
     * Delete the source
     */
    public void delete() throws SourceException {
        String base = null;
        String name = null;
        if (this.url.endsWith("/")) {
            try {
                // Cut trailing '/'
                String k = this.url.substring(0, this.url.length() - 1);

                base = k.substring(0, k.lastIndexOf("/"));
                name = k.substring(k.lastIndexOf("/")+1);

                Collection collection = DatabaseManager.getCollection(base);

                CollectionManagementService service =
                        (CollectionManagementService) collection.getService("CollectionManagementService", "1.0");
                service.removeCollection(name);
            } catch (XMLDBException e) {
                String message = "Failed to remove collection " + name + ": " + e.errorCode;
                getLogger().error(message, e);
                throw new SourceException(message);
            }
        } else {
            try {
                base = this.url.substring(0, this.url.lastIndexOf("/"));
                name = this.url.substring(this.url.lastIndexOf("/")+1);

                Collection collection = DatabaseManager.getCollection(base);

                Resource resource = collection.getResource(name);
                if (resource == null) {
                    String message = "Resource " + name + " does not exist";
                    getLogger().debug(message);
                    throw new SourceException(message);
                } else {
                    collection.removeResource(resource);
                    getLogger().debug("Removed resource: "+ name);
                }
            } catch (XMLDBException e) {
                String message = "Failed to delete resource " + name + ": " + e.errorCode;
                getLogger().debug(message, e);
                throw new SourceException(message);
            }
        }
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        return !this.os.isClosed();
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     *
     * <p>After cancelling, the stream should no longer be used.</p>
     */
    public void cancel(OutputStream stream) throws IOException {
        this.os.cancel();
        this.os = null;
    }

    public class XMLDBOutputStream extends OutputStream {

        private ByteArrayOutputStream baos;
        private boolean isClosed;
        public XMLDBOutputStream() {
            baos = new ByteArrayOutputStream();
            isClosed = false;
        }

        public void write(int b) throws IOException {
            baos.write(b);
        }

        public void write(byte b[]) throws IOException {
            baos.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            baos.write(b, off, len);
        }

        public void close() throws IOException, SourceException {
            if (!isClosed) {
                writeOutputStream(baos.toString());
                baos.close();
                this.isClosed = true;
            }
        }

        public void flush() throws IOException {
        }

        public int size() {
            return baos.size();
        }

        public boolean isClosed() {
            return this.isClosed;
        }
        public void cancel() {
            this.isClosed = true;
        }
    }
}
