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
import java.net.URL;
import java.util.Iterator;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.crawler.CocoonCrawler;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;

/**
 * A lucene indexer.
 * 
 * <p>
 * XML documents are indexed using lucene. Links to XML documents are supplied
 * by a crawler, requesting links of documents by specifying a cocoon-view, and
 * HTTP protocol.
 * </p>
 * 
 * @version $Id: SimpleLuceneCocoonIndexerImpl.java 449162 2006-09-23 05:14:05Z
 *          crossley $
 */
public class SimpleLuceneCocoonIndexerImpl extends AbstractLogEnabled implements LuceneCocoonIndexer {

    /**
     * configuration default value for <a
     * href="http://www.mail-archive.com/lucene-user@jakarta.apache.org/msg00373.html">lucene's
     * merge factor</a>.
     */
    public final static int MERGE_FACTOR_DEFAULT = 10;

    /** The used lucene analyzer */
    protected Analyzer analyzer = new StandardAnalyzer();

    // private String analyzerClassnameDefault = ANALYZER_CLASSNAME_DEFAULT;

    /** The Lucene Merge Factor */
    private int mergeFactor = MERGE_FACTOR_DEFAULT;

    /** The Lucene XML Indexer */
    private LuceneXMLIndexer luceneXMLIndexer;

    /** The crawler */
    private CocoonCrawler cocoonCrawler;

    /**
     * Sets the analyzer attribute of the SimpleLuceneCocoonIndexerImpl object
     * 
     * @param analyzer
     *            The new analyzer value
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * index content of base_url, index content of links from base_url.
     * 
     * @param index
     *            the lucene store to write the index to
     * @param create
     *            if true create, or overwrite existing index, else update
     *            existing index.
     * @param base_url
     *            index content of base_url, and crawl through all its links
     *            recursivly.
     * @exception ProcessingException
     *                is thrown if indexing fails
     */
    public void index(Directory index, boolean create, URL base_url) throws ProcessingException {

        IndexWriter writer = null;

        try {
            writer = new IndexWriter(index, analyzer, create);
            writer.setMergeFactor(this.mergeFactor);

            getCocoonCrawler().crawl(base_url);

            Iterator cocoonCrawlerIterator = getCocoonCrawler().iterator();
            while (cocoonCrawlerIterator.hasNext()) {
                URL crawl_url = (URL) cocoonCrawlerIterator.next();
                // result of fix Bugzilla Bug 25270, in SimpleCocoonCrawlerImpl
                // check if crawl_url is null
                if (crawl_url == null) {
                    continue;
                } else if (!crawl_url.getHost().equals(base_url.getHost()) || crawl_url.getPort() != base_url.getPort()) {

                    // skip urls using different host, or port than host,
                    // or port of base url
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(
                                "Skipping crawling URL " + crawl_url.toString() + " as base_url is "
                                        + base_url.toString());
                    }
                    continue;
                }

                // build lucene documents from the content of the crawl_url
                Iterator i = getLuceneXMLIndexer().build(crawl_url).iterator();

                // add all built lucene documents
                while (i.hasNext()) {
                    writer.addDocument((Document) i.next());
                }
            }
            // optimize it
            writer.optimize();
        } catch (IOException ioe) {
            throw new ProcessingException("IOException in index()", ioe);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe) {
                }
                writer = null;
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
         * Constructor for the DocumentDeletableIterator object
         * 
         * @param directory
         *            Description of Parameter
         * @exception IOException
         *                Description of Exception
         */
        public DocumentDeletableIterator(Directory directory) throws IOException {
            reader = IndexReader.open(directory);
            // open existing index
            uidIter = reader.terms(new Term("uid", ""));
            // init uid iterator
        }

        /**
         * Description of the Method
         * 
         * @exception IOException
         *                Description of Exception
         */
        public void deleteAllStaleDocuments() throws IOException {
            while (uidIter.term() != null && uidIter.term().field().equals("uid")) {
                reader.deleteDocuments(uidIter.term());
                uidIter.next();
            }
        }

        /**
         * Description of the Method
         * 
         * @param uid
         *            Description of Parameter
         * @exception IOException
         *                Description of Exception
         */
        public void deleteModifiedDocuments(String uid) throws IOException {
            while (documentHasBeenModified(uidIter.term(), uid)) {
                reader.deleteDocuments(uidIter.term());
                uidIter.next();
            }
            if (documentHasNotBeenModified(uidIter.term(), uid)) {
                uidIter.next();
            }
        }

        /**
         * Description of the Method
         * 
         * @exception Throwable
         *                Description of Exception
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
         * Description of the Method
         * 
         * @param term
         *            Description of Parameter
         * @return Description of the Returned Value
         */
        boolean documentIsDeletable(Term term) {
            return term != null && term.field().equals("uid");
        }

        /**
         * Description of the Method
         * 
         * @param term
         *            Description of Parameter
         * @param uid
         *            Description of Parameter
         * @return Description of the Returned Value
         */
        boolean documentHasBeenModified(Term term, String uid) {
            return documentIsDeletable(term) && term.text().compareTo(uid) < 0;
        }

        /**
         * Description of the Method
         * 
         * @param term
         *            Description of Parameter
         * @param uid
         *            Description of Parameter
         * @return Description of the Returned Value
         */
        boolean documentHasNotBeenModified(Term term, String uid) {
            return documentIsDeletable(term) && term.text().compareTo(uid) == 0;
        }
    }

    /**
     * @return the mergeFactor
     */
    public int getMergeFactor() {
        return mergeFactor;
    }

    /**
     * @param mergeFactor
     *            the mergeFactor to set
     */
    public void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    /**
     * @return the luceneXMLIndexer
     */
    public LuceneXMLIndexer getLuceneXMLIndexer() {
        return luceneXMLIndexer;
    }

    /**
     * @param luceneXMLIndexer
     *            the luceneXMLIndexer to set
     */
    public void setLuceneXMLIndexer(LuceneXMLIndexer luceneXMLIndexer) {
        this.luceneXMLIndexer = luceneXMLIndexer;
    }

    /**
     * @return the cocoonCrawler
     */
    public CocoonCrawler getCocoonCrawler() {
        return cocoonCrawler;
    }

    /**
     * @param cocoonCrawler
     *            the cocoonCrawler to set
     */
    public void setCocoonCrawler(CocoonCrawler cocoonCrawler) {
        this.cocoonCrawler = cocoonCrawler;
    }
}
