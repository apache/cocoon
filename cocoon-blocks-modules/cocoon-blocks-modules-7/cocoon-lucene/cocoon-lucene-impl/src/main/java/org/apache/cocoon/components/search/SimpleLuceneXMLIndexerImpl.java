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
package org.apache.cocoon.components.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A simple class building lucene documents from xml content.
 * 
 * <p>
 * It has two parameters that effect the way it works:
 * </p>
 * <p>
 * <tt>&lt;storeFields/&gt;</tt> Sets which tags in your content are stored
 * in Lucene as fields, during the indexing process. Allows them to be output
 * with search hits.
 * </p>
 * <p>
 * <tt>&lt;contentViewQuery/&gt;</tt> Sets the view the indexer will request
 * for indexing content.
 * </p>
 * <p>
 * Example configuration (goes in cocoon-lucene.xml)
 * 
 * <pre><tt>
 * &lt;bean name=&quot;org.apache.cocoon.components.search.LuceneXMLIndexer&quot; class=&quot;org.apache.cocoon.components.search.SimpleLuceneXMLIndexerImpl&quot;&gt;
 *   &lt;property name=&quot;parser&quot; ref=&quot;org.apache.cocoon.core.xml.SAXParser&quot; /&gt;
 *   &lt;!-- Config element name specifying query-string appendend for requesting links of an URL. --&gt;
 *   &lt;property name="contentViewQuery&quot; value=&quot;cocoon-view=content&quot; /&gt;
 *   &lt;!-- Optional config element name specifying the tags to be added as Stored, Untokenised, Unindexed Fields. --&gt;
 *   &lt;property name=&quot;storeFields&quot;&gt;
 *      &lt;storeFields&gt;
 *        &lt;set&gt;
 *          &lt;value&gt;title&lt;/value&gt;
 *          &lt;value&gt;summary&lt;/value&gt;
 *        &lt;/set&gt;
 *       &lt;/storeFields&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * 
 * </tt></pre>
 * 
 * </p>
 * 
 * @version $Id: SimpleLuceneXMLIndexerImpl.java 449162 2006-09-23 05:14:05Z
 *          crossley $
 */
public class SimpleLuceneXMLIndexerImpl extends AbstractLogEnabled implements LuceneXMLIndexer, InitializingBean {

    /**
     * append this string to the url in order to get the content view of the url
     * 
     * @since
     */
    final String CONTENT_VIEW_QUERY_DEFAULT = "cocoon-view=content";

    /**
     * set of allowed content types
     * 
     * @since
     */
    final HashSet allowedContentType;

    private String contentViewQuery = CONTENT_VIEW_QUERY_DEFAULT;
    private HashSet storeFields;
    private SAXParser parser;

    /**
     * @since
     */
    public SimpleLuceneXMLIndexerImpl() {
        allowedContentType = new HashSet();
        allowedContentType.add("text/xml");
        allowedContentType.add("text/xhtml");
        storeFields = new HashSet();
    }

    /**
     * afterPropertiesSet
     * 
     * @exception IllegalArgumentException
     */
    public void afterPropertiesSet() throws IllegalArgumentException {
        if (getLogger().isDebugEnabled()) {
            if (getStoreFields() != null) {
                final Iterator iter = getStoreFields().iterator();
                while (iter.hasNext()) {
                    getLogger().debug("add field: " + (String) iter.next());
                }
            } else {
                getLogger().debug("Do not add any fields");
            }
            getLogger().debug("content view: " + this.getContentViewQuery());
        }
        if (parser == null) {
            throw new IllegalArgumentException("Cannot lookup xml parser!");
        }
    }

    /**
     * Build lucenen documents from a URL
     * 
     * @param url
     *            the content of this url gets indexed.
     * @exception ProcessingException
     *                Description of Exception
     * @since
     */
    public List build(URL url) throws ProcessingException {

        try {
            URL contentURL = new URL(url, url.getFile() + ((url.getFile().indexOf("?") == -1) ? "?" : "&")
                    + contentViewQuery);
            URLConnection contentURLConnection = contentURL.openConnection();
            if (contentURLConnection == null) {
                throw new ProcessingException("Can not open connection to URL " + contentURL + " (null connection)");
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
                luceneIndexContentHandler.setFieldTags(storeFields);
                indexDocument(contentURLConnection, luceneIndexContentHandler);
                //
                // document is parsed
                //
                Iterator it = luceneIndexContentHandler.iterator();
                while (it.hasNext()) {
                    Document d = (Document) it.next();
                    d.add(new Field(URL_FIELD, url.toString(), Field.Store.YES, Field.Index.NO));
                    // store ... false, index ... true, token ... false
                    d.add(new Field(UID_FIELD, uid(contentURLConnection), Field.Store.NO, Field.Index.UN_TOKENIZED));
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
     * @param contentURLConnection
     *            the xml content which should get indexed.
     * @param luceneIndexContentHandler
     *            ContentHandler for generating a lucene Document from XML
     *            content.
     * @exception ProcessingException
     *                Description of Exception
     * @since
     */
    private void indexDocument(URLConnection contentURLConnection, LuceneIndexContentHandler luceneIndexContentHandler)
            throws ProcessingException {

        InputStream is = null;
        InputSource in = null;

        try {
            is = contentURLConnection.getInputStream();
            in = new InputSource(is);

            // reader.setErrorHandler(new CocoonErrorHandler());
            parser.parse(in, luceneIndexContentHandler);
            //
            // document is parsed
            //
        } catch (IOException ioe) {
            throw new ProcessingException("Cannot read!", ioe);
        } catch (SAXException saxe) {
            throw new ProcessingException("Cannot parse!", saxe);
        }
    }

    /**
     * return a unique uid of a url connection
     * 
     * @param urlConnection
     *            Description of Parameter
     * @return String unique uid of a urlConnection
     * @since
     */
    private String uid(URLConnection urlConnection) {
        // Append path and date into a string in such a way that lexicographic
        // sorting gives the same results as a walk of the file hierarchy. Thus
        // null (\u0000) is used both to separate directory components and to
        // separate the path from the date.
        return urlConnection.toString().replace('/', '\u0000') + "\u0000"
                + DateField.timeToString(urlConnection.getLastModified());
    }

    /**
     * @return the contentViewQuery
     */
    public String getContentViewQuery() {
        return contentViewQuery;
    }

    /**
     * @param contentViewQuery
     *            the contentViewQuery to set
     */
    public void setContentViewQuery(String contentViewQuery) {
        this.contentViewQuery = contentViewQuery;
    }

    /**
     * @return the storeFields
     */
    public HashSet getStoreFields() {
        return storeFields;
    }

    /**
     * @param storeFields
     *            the dtoreFields to set
     */
    public void setStoreFields(HashSet storeFields) {
        this.storeFields = storeFields;
    }

    /**
     * @return the parser
     */
    public SAXParser getParser() {
        return parser;
    }

    /**
     * @param parser
     *            the parser to set
     */
    public void setParser(SAXParser parser) {
        this.parser = parser;
    }
}
