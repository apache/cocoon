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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements the xmldb:// pseudo-protocol and allows to get XML
 * content from an XML:DB enabled XML database.
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XMLDBSource.java,v 1.8 2003/12/05 00:29:02 vgritsenko Exp $
 */
public class XMLDBSource extends AbstractLogEnabled
    implements Source, XMLizable {

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
        try {
            serializerSelector = (ComponentSelector) this.manager.lookup(Serializer.ROLE + "Selector");
            serializer = (Serializer)serializerSelector.select("xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.setOutputStream(os);

            toSAX(serializer);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (ServiceException e) {
            throw new CascadingIOException("Could not lookup pipeline components", e);
        } catch (ComponentException e) {
            throw new CascadingIOException("Could not lookup pipeline components", e);
        } catch (Exception e) {
            throw new CascadingIOException("Exception during processing of " + getURI(), e);
        } finally {
            if (serializer != null) {
                serializerSelector.release(serializer);
            }
            if (serializerSelector != null) {
                this.manager.release(serializerSelector);
            }
        }
    }
}
