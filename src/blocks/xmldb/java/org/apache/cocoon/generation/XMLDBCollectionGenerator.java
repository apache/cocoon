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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

import java.io.IOException;
import java.util.Map;

/**
 * This class implements generation of a XML:DB collection
 * contents as a directory listing.
 *
 * <pre>
 * &lt;driver&gt;
 *   (a valid DB:XML compliant driver)
 * &lt;/driver&gt;
 * &lt;base&gt;
 *   xmldb:yourdriver://host/an/optional/path/to/be/prepended
 * &lt;/base&gt;
 * </pre>
 *
 * NOTE: the driver can be any DB:XML compliant driver (although this
 * component has been tested only with
 * <a href="http://www.dbxml.org">dbXML</a>, and the trailing
 * slash in the base tag is important!
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id: XMLDBCollectionGenerator.java,v 1.5 2004/05/24 12:37:52 cziegeler Exp $
 * @deprecated Use the XML:DB pseudo protocol instead.
 */
public class XMLDBCollectionGenerator extends ServiceableGenerator
        implements CacheableProcessingComponent, Configurable, Initializable {

    protected static final String URI =
            "http://apache.org/cocoon/xmldb/1.0";
    protected static final String PREFIX = "collection";
    protected static final String RESOURCE_COUNT_ATTR = "resources";
    protected static final String COLLECTION_COUNT_ATTR  = "collections";
    protected static final String COLLECTION  = "collection";
    protected static final String QCOLLECTION  = PREFIX + ":collection";
    protected static final String RESOURCE  = "resource";
    protected static final String QRESOURCE  = PREFIX + ":resource";

    protected String driver;
    protected String base;
    protected String col;
    protected Database database;
    protected Collection collection;
    protected final AttributesImpl attributes = new AttributesImpl();

    /**
     * Recycle the component, keep only the configuration variables
     * and the database instance for reuse.
     */
    public void recycle() {
        super.recycle();
        this.col = null;
        this.collection = null;
    }

   /**
    * Configure the component. This class is expecting a configuration
    * like the following one:
    * <pre>
    * &lt;driver&gt;org.dbxml.client.xmldb.DatabaseImpl&lt;/driver&gt;
    * &lt;base&gt;xmldb:dbxml:///db/&lt;/base&gt;
    * </pre>
    * NOTE: the driver can be any DB:XML compliant driver (although this
    * component has been tested only with
    * <a href="http://www.dbxml.org">dbXML</a>, and the trailing
    * slash in the base tag is important!
    *
    * @exception ConfigurationException (configuration invalid or missing)
    */
   public void configure(Configuration conf) throws ConfigurationException {
       this.driver = conf.getChild("driver").getValue();
       this.base = conf.getChild("base").getValue();
   }

   /**
    * Initialize the component getting a database instance.
    *
    * @exception Exception if an error occurs
    */
   public void initialize() throws Exception {
       try {
           Class c = Class.forName(driver);
           database = (Database)c.newInstance();
           DatabaseManager.registerDatabase(database);
       } catch (XMLDBException xde) {
           getLogger().error("Unable to connect to the XML:DB database");
           throw new ProcessingException("Unable to connect to the XML DB"
                                         + xde.getMessage());
       } catch (Exception e) {
           getLogger().error("There was a problem setting up the connection");
           getLogger().error("Make sure that your driver is available");
           throw new ProcessingException("Problem setting up the connection: "
                                         + e.getMessage());
       }
   }

    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
            throws ProcessingException, SAXException,IOException {
        super.setup(resolver, objectModel, src, par);
    }

    /**
     * The component isn't cached (yet)
     */
    public SourceValidity getValidity() {
        return null;
    }

    /**
     * The component isn't cached (yet)
     */
    public java.io.Serializable getKey() {
        return null;
    }

    /**
     * Parse the requested URI, connect to the XML:DB database
     * and fetch the requested resource.
     *
     * @exception ProcessingException something unexpected happened with the DB
     */
    public void generate()
            throws IOException, SAXException, ProcessingException {
        //String col = "/";

        //if (source.indexOf('/') != -1)
        col = source;

        try {
            collection = DatabaseManager.getCollection(base + col);
            if (collection == null) {
                throw new ResourceNotFoundException("Collection " + col +
                                                    " not found");
            }

            collectionToSAX(collection);
            collection.close();
        } catch (XMLDBException xde) {
            throw new ProcessingException("Unable to fetch content '"
                                          + source + "':" + xde.getMessage());
        } catch (NullPointerException npe) {
            getLogger().error("The XML:DB driver raised an exception");
            getLogger().error("probably the document was not found");
            throw new ProcessingException("Null pointer exception while " +
                                          "retrieving document : " + npe.getMessage());
        }
    }

    /**
     * Output SAX events listing the collection.
     *
     * @exception SAXException
     */
    public void collectionToSAX(Collection collection)
            throws SAXException {

        String ncollections;
        String nresources;
        String[] resources;
        String[] collections;

        try {
            ncollections = Integer.toString(collection.getChildCollectionCount());
            nresources = Integer.toString(collection.getResourceCount());

            attributes.clear();
            attributes.addAttribute("", RESOURCE_COUNT_ATTR,
                                    RESOURCE_COUNT_ATTR, "CDATA", nresources);
            attributes.addAttribute("", COLLECTION_COUNT_ATTR,
                                    COLLECTION_COUNT_ATTR, "CDATA", ncollections);

            collections = collection.listChildCollections();
            resources = collection.listResources();

            this.xmlConsumer.startDocument();
            this.xmlConsumer.startPrefixMapping(PREFIX, URI);

            this.xmlConsumer.startElement(URI, "collections",
                                          "collection:collections", attributes);

            // Print child collections

            for (int i = 0; i < collections.length; i++) {
                attributes.clear();
                attributes.addAttribute("", "name", "name", "CDATA", collections[i]);
                this.xmlConsumer.startElement(URI, COLLECTION,
                                              QCOLLECTION, attributes);
                this.xmlConsumer.endElement(URI, COLLECTION, COLLECTION);
            }

            // Print child resources

            for (int i = 0; i < resources.length; i++) {
                attributes.clear();
                attributes.addAttribute("", "name", "name", "CDATA", resources[i]);
                this.xmlConsumer.startElement(URI, RESOURCE,
                                              QRESOURCE, attributes);
                this.xmlConsumer.endElement(URI, RESOURCE, RESOURCE);
            }

            this.xmlConsumer.endElement(URI, "collections",
                                        "collection:collections");

            this.xmlConsumer.endPrefixMapping(PREFIX);
            this.xmlConsumer.endDocument();
        } catch (XMLDBException xde) {
            getLogger().warn("Collection listing failed: " + xde.getMessage());
            throw new SAXException("Collection listing failed: " + xde.getMessage());
        }
    }
}
