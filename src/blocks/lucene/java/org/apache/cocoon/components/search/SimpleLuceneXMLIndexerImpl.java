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
 * @version CVS $Id: SimpleLuceneXMLIndexerImpl.java,v 1.6 2004/02/06 22:45:58 joerg Exp $
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

