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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.crawler.CocoonCrawler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * A lucene indexer.
 *
 * <p>
 *  XML documents are indexed using lucene.
 *  Links to XML documents are supplied by
 *  a crawler, requesting links of documents by specifying a cocoon-view, and
 *  HTTP protocol.
 * </p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: SimpleLuceneCocoonIndexerImpl.java,v 1.10 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public class SimpleLuceneCocoonIndexerImpl extends AbstractLogEnabled
         implements LuceneCocoonIndexer, Configurable, Serviceable, Disposable
{

    /**
     * configuration tagname for specifying the analyzer class
     */
    public final static String ANALYZER_CLASSNAME_CONFIG = "analyzer-classname";
    
    /**
     * configuration default analyzer class
     */
    public final static String ANALYZER_CLASSNAME_DEFAULT = "org.apache.lucene.analysis.standard.StandardAnalyzer";

    /**
     * configuration tagname for specifying lucene's index directory
     */
    public final static String DIRECTORY_CONFIG = "directory";
    
    /**
     * configuration default directory, ie. no default.
     */
    public final static String DIRECTORY_DEFAULT = null;

    /**
     * configuration tagname for specifying lucene's merge factor.
     */
    public final static String MERGE_FACTOR_CONFIG = "merge-factor";

    /**
     * configuration default value for
     * <a href="http://www.mail-archive.com/lucene-user@jakarta.apache.org/msg00373.html">lucene's merge factor</a>.
     */
    public final static int MERGE_FACTOR_DEFAULT = 10;

    /**
     * The service manager for looking up components used.
     */
    protected ServiceManager manager = null;

    protected Analyzer analyzer;
//    private String analyzerClassnameDefault = ANALYZER_CLASSNAME_DEFAULT;
    private int mergeFactor = MERGE_FACTOR_DEFAULT;


    /**
     *Sets the analyzer attribute of the SimpleLuceneCocoonIndexerImpl object
     *
     * @param  analyzer  The new analyzer value
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }


    /**
     * Configure this component.
     *
     * @param  conf                        is the configuration
     * @exception  ConfigurationException  is thrown if configuring fails
     */
    public void configure(Configuration conf) throws ConfigurationException {
        Configuration child;

/*        child = conf.getChild(ANALYZER_CLASSNAME_CONFIG, false);
        if (child != null) {
            // fix Bugzilla Bug 25277, use child.getValue
            // and in all following blocks
            String value = child.getValue(ANALYZER_CLASSNAME_DEFAULT);
            if (value != null) {
                analyzerClassnameDefault = value;
            }
        }
*/
        child = conf.getChild(MERGE_FACTOR_CONFIG, false);
        if (child != null) {
            // fix Bugzilla Bug 25277, use child instead of conf
            int int_value = child.getValueAsInteger(MERGE_FACTOR_DEFAULT);
            mergeFactor = int_value;
        }
    }


    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param  manager                 used by this component
     * @exception  ServiceException  is never thrown
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }


    /**
     * Dispose this component.
     */
    public void dispose() { }


    /**
     * index content of base_url, index content of links from base_url.
     *
     * @param  index                    the lucene store to write the index to
     * @param  create                   if true create, or overwrite existing index, else
     *   update existing index.
     * @param  base_url                 index content of base_url, and crawl through all its
     *   links recursivly.
     * @exception  ProcessingException  is thrown if indexing fails
     */
    public void index(Directory index, boolean create, URL base_url)
             throws ProcessingException {

        IndexWriter writer = null;
        LuceneXMLIndexer lxi = null;
        CocoonCrawler cocoonCrawler = null;

        try {
            lxi = (LuceneXMLIndexer) manager.lookup(LuceneXMLIndexer.ROLE);

            writer = new IndexWriter(index, analyzer, create);
            writer.mergeFactor = this.mergeFactor;

            cocoonCrawler = (CocoonCrawler) manager.lookup(CocoonCrawler.ROLE);
            cocoonCrawler.crawl(base_url);

            Iterator cocoonCrawlerIterator = cocoonCrawler.iterator();
            while (cocoonCrawlerIterator.hasNext()) {
                URL crawl_url = (URL) cocoonCrawlerIterator.next();
                // result of fix Bugzilla Bug 25270, in SimpleCocoonCrawlerImpl
                // check if crawl_url is null
                if (crawl_url == null) {
                    continue;
                } else if (!crawl_url.getHost().equals(base_url.getHost()) ||
                        crawl_url.getPort() != base_url.getPort()) {

                    // skip urls using different host, or port than host,
                    // or port of base url
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Skipping crawling URL " + crawl_url.toString() +
                            " as base_url is " + base_url.toString());
                    }
                    continue;
                }

                // build lucene documents from the content of the crawl_url
                Iterator i = lxi.build(crawl_url).iterator();

                // add all built lucene documents
                while (i.hasNext()) {
                    writer.addDocument((Document) i.next());
                }
            }
            // optimize it
            writer.optimize();
        } catch (IOException ioe) {
            throw new ProcessingException("IOException in index()", ioe);
        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup service in index()", se);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe) {
                }
                writer = null;
            }

            if (lxi != null) {
                manager.release(lxi);
                lxi = null;
            }
            if (cocoonCrawler != null) {
                manager.release(cocoonCrawler);
                cocoonCrawler = null;
            }
        }
    }


    /**
     * A document iterator deleting "old" documents form the index.
     * 
     * TODO: use this class before indexing, in non-creating mode.
     */
    static class DocumentDeletableIterator {
        private IndexReader reader;
        // existing index
        private TermEnum uidIter;

        // document id iterator


        /**
         *Constructor for the DocumentDeletableIterator object
         *
         * @param  directory        Description of Parameter
         * @exception  IOException  Description of Exception
         */
        public DocumentDeletableIterator(Directory directory) throws IOException {
            reader = IndexReader.open(directory);
            // open existing index
            uidIter = reader.terms(new Term("uid", ""));
            // init uid iterator
        }


        /**
         *Description of the Method
         *
         * @exception  IOException  Description of Exception
         */
        public void deleteAllStaleDocuments() throws IOException {
            while (uidIter.term() != null && uidIter.term().field() == "uid") {
                reader.delete(uidIter.term());
                uidIter.next();
            }
        }


        /**
         *Description of the Method
         *
         * @param  uid              Description of Parameter
         * @exception  IOException  Description of Exception
         */
        public void deleteModifiedDocuments(String uid) throws IOException {
            while (documentHasBeenModified(uidIter.term(), uid)) {
                reader.delete(uidIter.term());
                uidIter.next();
            }
            if (documentHasNotBeenModified(uidIter.term(), uid)) {
                uidIter.next();
            }
        }


        /**
         *Description of the Method
         *
         * @exception  Throwable  Description of Exception
         */
        protected void finalize() throws Throwable {
            super.finalize();
            if (uidIter != null) {
                uidIter.close();
                // close uid iterator
                uidIter = null;
            }
            if (reader != null) {
                reader.close();
                // close existing index
                reader = null;
            }
        }


        /**
         *Description of the Method
         *
         * @param  term  Description of Parameter
         * @return       Description of the Returned Value
         */
        boolean documentIsDeletable(Term term) {
            return term != null && term.field() == "uid";
        }


        /**
         *Description of the Method
         *
         * @param  term  Description of Parameter
         * @param  uid   Description of Parameter
         * @return       Description of the Returned Value
         */
        boolean documentHasBeenModified(Term term, String uid) {
            return documentIsDeletable(term) &&
                    term.text().compareTo(uid) < 0;
        }


        /**
         *Description of the Method
         *
         * @param  term  Description of Parameter
         * @param  uid   Description of Parameter
         * @return       Description of the Returned Value
         */
        boolean documentHasNotBeenModified(Term term, String uid) {
            return documentIsDeletable(term) &&
                    term.text().compareTo(uid) == 0;
        }
    }
}

