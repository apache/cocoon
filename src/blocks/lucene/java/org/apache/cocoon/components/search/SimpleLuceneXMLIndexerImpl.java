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
package org.apache.cocoon.components.search;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.Tokenizer;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * A simple class building lucene documents from xml content.
 *
 * <p>It has two parameters that effect the way it works:</p>
 * <p>
 *   <tt>&lt;store-fields/&gt;</tt> 
 *   Sets which tags in your content are stored in Lucene as fields, 
 *   during the indexing process. Allows them to be output with search hits.
 * </p><p>
 *   <tt>&lt;content-view-query/&gt;</tt>
 *   Sets the view the indexer will request for indexing content.
 * </p><p>
 *   Example configuration (goes in cocoon.xconf)
 *   <pre><tt>
 *     &lt;lucene-xml-indexer logger="core.search.lucene"&gt;
 *       &lt;store-fields&gt;title, summary&lt;/store-fields&gt;
 *       &lt;content-view-query&gt;cocoon-view=search&lt;/content-view-query&gt;
 *     &lt;/lucene-xml-indexer&gt;
 *   </tt></pre></p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @author <a href="mailto:jeremy@apache.org">Jeremy Quinn</a>
 * @version CVS $Id: SimpleLuceneXMLIndexerImpl.java,v 1.7 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public class SimpleLuceneXMLIndexerImpl extends AbstractLogEnabled
         implements LuceneXMLIndexer, Configurable, Serviceable, ThreadSafe {

    /**
     * The service manager instance
     *
     * @since
     */
    protected ServiceManager manager = null;

    /**
     * Config element name specifying query-string appendend for requesting links
     * of an URL.
     * <p>
     *  Its value is <code>link-view-query</code>.
     * </p>
     *
     * @since
     */
    public final static String CONTENT_VIEW_QUERY_CONFIG = "content-view-query";

    /**
     * append this string to the url in order to get the
     * content view of the url
     *
     * @since
     */
    
    final String CONTENT_VIEW_QUERY_DEFAULT = "cocoon-view=content";

    /**
     * Config element name specifying the tags to be added as Stored, Untokenised, Unindexed Fields.
     * <p>
     *  Its value is <code>field-tags</code>.
     * </p>
     *
     * @since
     */
    public final static String FIELDTAGS_CONFIG = "store-fields";

    /**
     * set of allowed content types
     *
     * @since
     */
    final HashSet allowedContentType;


    /**
     * @since
     */
    public SimpleLuceneXMLIndexerImpl() {
        allowedContentType = new HashSet();
        allowedContentType.add("text/xml");
        allowedContentType.add("text/xhtml");
        fieldTags = new HashSet();
    }
    
    
    private String contentViewQuery = CONTENT_VIEW_QUERY_DEFAULT;
    private HashSet fieldTags;


    /**
     * configure
     *
     * @param  configuration
     * @exception  ConfigurationException
     * @since
     */
    public void configure(Configuration configuration) throws ConfigurationException { 
    
        Configuration[] children;
        children = configuration.getChildren(FIELDTAGS_CONFIG);
        if (children != null && children.length > 0) {
            fieldTags = new HashSet();
            for (int i = 0; i < children.length; i++) {
                String pattern = children[i].getValue();
 								Tokenizer t = new Tokenizer(pattern, ", ");
								while (t.hasMoreTokens()) {
										String tokenized_pattern = t.nextToken();
										if (!tokenized_pattern.equals("")) {
											this.fieldTags.add(tokenized_pattern);
											if (getLogger().isDebugEnabled()) {
													getLogger().debug("add field: " + tokenized_pattern);
											}
										}
								}
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Do not add any fields");
            }
        }
        this.contentViewQuery = configuration.getChild(CONTENT_VIEW_QUERY_CONFIG, true).getValue(CONTENT_VIEW_QUERY_DEFAULT);
				if (getLogger().isDebugEnabled()) {
						getLogger().debug("content view: " + this.contentViewQuery);
				}
    }


    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param  manager                 Description of Parameter
     * @exception  ServiceException  Description of Exception
     * @since
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }


    /**
     * Build lucenen documents from a URL
     *
     * @param  url                      the content of this url gets indexed.
     * @exception  ProcessingException  Description of Exception
     * @since
     */
    public List build(URL url)
             throws ProcessingException {

        try {
            URL contentURL = new URL(url, url.getFile()
                + ((url.getFile().indexOf("?") == -1) ? "?" : "&")
                + contentViewQuery);
            URLConnection contentURLConnection = contentURL.openConnection();
            if (contentURLConnection == null) {
                throw new ProcessingException("Can not open connection to URL "
                        + contentURL + " (null connection)");
            }

            String contentType = contentURLConnection.getContentType();
            if (contentType == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Ignoring " + contentURL + " (no content type)");
                }

                return Collections.EMPTY_LIST;
            }

            int index = contentType.indexOf(';');
            if (index != -1) {
                contentType = contentType.substring(0, index);
            }

            if (allowedContentType.contains(contentType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Indexing " + contentURL + " (" + contentType + ")");
                }

                LuceneIndexContentHandler luceneIndexContentHandler = new LuceneIndexContentHandler();
                luceneIndexContentHandler.setFieldTags(fieldTags);
                indexDocument(contentURLConnection, luceneIndexContentHandler);
                //
                // document is parsed
                //
                Iterator it = luceneIndexContentHandler.iterator();
                while (it.hasNext()) {
                    Document d = (Document) it.next();
                    d.add(Field.UnIndexed(URL_FIELD, url.toString()));
                    // store ... false, index ... true, token ... false
                    d.add(new Field(UID_FIELD, uid(contentURLConnection), false, true, false));
                }

                return luceneIndexContentHandler.allDocuments();
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Ignoring " + contentURL + " (" + contentType + ")");
                }

                return Collections.EMPTY_LIST;
            }
        } catch (IOException ioe) {
            throw new ProcessingException("Cannot read URL " + url, ioe);
        }
    }


    /**
     * index input stream producing lucene Documents
     *
     * @param  contentURLConnection       the xml content which should get indexed.
     * @param  luceneIndexContentHandler  ContentHandler for generating
     *   a lucene Document from XML content.
     * @exception  ProcessingException    Description of Exception
     * @since
     */
    private void indexDocument(URLConnection contentURLConnection,
            LuceneIndexContentHandler luceneIndexContentHandler)
             throws ProcessingException {

        InputStream is = null;
        InputSource in = null;
        SAXParser parser = null;

        try {
            is = contentURLConnection.getInputStream();
            in = new InputSource(is);

            // get an XML parser
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            //reader.setErrorHandler(new CocoonErrorHandler());
            parser.parse(in, luceneIndexContentHandler);
            //
            // document is parsed
            //
        } catch (IOException ioe) {
            throw new ProcessingException("Cannot read!", ioe);
        } catch (SAXException saxe) {
            throw new ProcessingException("Cannot parse!", saxe);
        } catch (ServiceException se) {
            throw new ProcessingException("Cannot lookup xml parser!", se);
        } finally {
            if (parser != null) {
                this.manager.release(parser);
            }
        }
    }


    /**
     * return a unique uid of a url connection
     *
     * @param  urlConnection  Description of Parameter
     * @return                String unique uid of a urlConnection
     * @since
     */
    private String uid(URLConnection urlConnection) {
        // Append path and date into a string in such a way that lexicographic
        // sorting gives the same results as a walk of the file hierarchy.  Thus
        // null (\u0000) is used both to separate directory components and to
        // separate the path from the date.
        return urlConnection.toString().replace('/', '\u0000') +
                "\u0000" +
                DateField.timeToString(urlConnection.getLastModified());
    }
}

