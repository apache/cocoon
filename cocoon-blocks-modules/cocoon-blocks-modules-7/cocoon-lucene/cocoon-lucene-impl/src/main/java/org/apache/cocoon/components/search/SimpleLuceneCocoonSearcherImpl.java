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

import java.io.File;
import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class provides searching via lucene.
 * 
 * <p>
 * In order to do searching you need a lucene Directory where the lucene
 * generated index resides. Moreover you must know the lucene Analyzer which has
 * been used for indexing, and which will be used for searching.
 * </p>
 * <p>
 * Knowing this you can may start searching having a query which is parsable by
 * an QueryParser, and having the name of the default field to use in searching.
 * </p>
 * <p>
 * This class returns an Hit object as its search result.
 * </p>
 * 
 * @version $Id: SimpleLuceneCocoonSearcherImpl.java 449162 2006-09-23 05:14:05Z
 *          crossley $
 */
public class SimpleLuceneCocoonSearcherImpl extends AbstractLogEnabled implements InitializingBean,
        LuceneCocoonSearcher {

    /**
     * Configuration element default value of lucene's default search field.
     * <p>
     * Its value is <code>body</code>.
     * </p>
     * 
     */
    protected final static String DEFAULT_SEARCH_FIELD_DEFAULT = "body";

    /**
     * Configuration element default value of default-query.
     * <p>
     * Its value is <code>null</code>.
     * </p>
     * 
     */
    protected final static String DEFAULT_QUERY_DEFAULT = null;

    /**
     * Configuration element default value of queryparser-classname.
     * <p>
     * Its value is <code>org.apache.lucene.queryParser.QueryParser</code>.
     * </p>
     * 
     */
    protected final static String QUERYPARSER_CLASSNAME_DEFAULT = "org.apache.lucene.queryParser.QueryParser";

    /**
     * Configuration element default value of filesystem default directory.
     * <p>
     * Its value is <code>null</code>.
     * </p>
     * 
     */
    protected final static String DIRECTORY_DEFAULT = null;

    /**
     * The lucene analyzer used for searching
     */
    private Analyzer analyzer = new StandardAnalyzer();

    private String defaultSearchFieldDefault = DEFAULT_SEARCH_FIELD_DEFAULT;
    private String defaultQueryDefault = DEFAULT_QUERY_DEFAULT;
    private String defaultQueryparser = QUERYPARSER_CLASSNAME_DEFAULT;
    private String defaultDirectory = DIRECTORY_DEFAULT;

    /**
     * The lucene directory used for searching
     */
    private Directory directory;

    /**
     * The lucene index searcher used for searching
     */
    private IndexSearcher indexSearcher;

    /**
     * A lucene index reader cache to maximize sharing of lucene index readers
     */
    private IndexReaderCache indexReaderCache;

    /**
     * Sets the directory attribute of the SimpleLuceneCocoonSearcherImpl object
     * 
     * @param directory
     *            The new directory value
     */
    public void setDirectory(Directory directory) {
        this.directory = directory;
        if (indexReaderCache != null) {
            indexReaderCache.close();
            indexReaderCache = null;
        }
    }

    /**
     * Get an IndexReader.
     * <p>
     * As an IndexReader might be cached, it is check if the indexReader is
     * still valid.
     * </p>
     * 
     * @return IndexReader an up to date indexReader
     * @exception IOException
     *                is thrown iff it's impossible to create an IndexReader
     */
    public IndexReader getReader() throws IOException {
        if (indexReaderCache == null) {
            indexReaderCache = new IndexReaderCache();
        }
        return indexReaderCache.getIndexReader(directory);
    }

    /**
     * configure this component
     * 
     * @param conf
     *            of this component
     * @exception IllegalArgumentException
     *                is thrown iff configuration of this bean fails
     */
    public void afterPropertiesSet() throws IllegalArgumentException {
        try {
            setDirectory(FSDirectory.getDirectory(new File(getDefaultDirectory()), false));
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Cannot set index directory "
                    + getDefaultDirectory() + ": " + ioe.getMessage());
        }
    }

    /**
     * Dispose this component, releasing IndexSearcher, and IndexReaderCache.
     */
    public void dispose() {
        releaseIndexSearcher();
        releaseIndexReaderCache();
    }

    /**
     * Search lucene index.
     * 
     * @param query_string
     *            is lucene's query string
     * @param default_field
     *            the lucene field to run the query
     * @return lucene Hits
     * @exception ProcessingException
     *                iff its not possible do run the query
     */
    public Hits search(String query_string, String default_field) throws ProcessingException {
        Hits hits = null;

        if (query_string == null) {
            query_string = defaultQueryDefault;
        }
        if (default_field == null) {
            default_field = defaultSearchFieldDefault;
        }

        try {
            final QueryParser parser = new QueryParser(default_field, analyzer);
            Query query = parser.parse(query_string);

            // release index searcher for each new search
            releaseIndexSearcher();

            IndexSearcher indexSearcher = new IndexSearcher(getReader());
            hits = indexSearcher.search(query);
            // do not close indexSearcher now, as using hits needs an
            // opened indexSearcher indexSearcher.close();
        } catch (ParseException pe) {
            throw new ProcessingException("Cannot parse query " + query_string, pe);
        } catch (IOException ioe) {
            throw new ProcessingException("Cannot access hits", ioe);
        }
        return hits;
    }

    /**
     * Search lucene index. This method is designed to be used by other
     * components, or Flowscripts
     * 
     * @param query
     *            the lucene Query
     * @return lucene Hits
     * @exception ProcessingException
     *                if its not possible do run the query
     */
    public Hits search(Query query) throws ProcessingException {
        Hits hits = null;
        try {
            // release index searcher for each new search
            releaseIndexSearcher();

            IndexSearcher indexSearcher = new IndexSearcher(getReader());
            hits = indexSearcher.search(query);
            // do not close indexSearcher now, as using hits needs an
            // opened indexSearcher indexSearcher.close();
        } catch (IOException ioe) {
            throw new ProcessingException("Cannot access hits", ioe);
        }
        return hits;
    }

    /**
     * Release the index searcher.
     * 
     */
    private void releaseIndexSearcher() {
        if (indexSearcher != null) {
            try {
                indexSearcher.close();
            } catch (IOException ioe) {
                // ignore it
            }
            indexSearcher = null;
        }
    }

    /**
     * Release the IndexReaderCache
     * 
     */
    private void releaseIndexReaderCache() {
        if (indexReaderCache != null) {
            indexReaderCache = null;
        }
    }

    /**
     * This class should help to minimise usage of IndexReaders.
     * 
     */
    static class IndexReaderCache {
        private IndexReader indexReader;
        private long lastModified;

        /**
         * Create an IndexReaderCache.
         * 
         */
        IndexReaderCache() {
        }

        /**
         * return cached IndexReader object.
         * 
         * @param directory
         *            lucene index directory
         * @return The indexReader value
         */
        public IndexReader getIndexReader(Directory directory) throws IOException {
            if (indexReader == null) {
                createIndexReader(directory);
            } else {
                if (!indexReaderIsValid(directory)) {
                    createIndexReader(directory);
                }
            }
            return indexReader;
        }

        /**
         * Close an opened lucene IndexReader
         * 
         */
        public void close() {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (IOException ioe) {
                    // ignore it
                }
                indexReader = null;
            }
        }

        /**
         * Check if cached IndexReader is up to date.
         * 
         * @param directory
         *            lucene index directory
         * @return boolean return true if there is a cached IndexReader object,
         *         and its lastModified date is greater equal than the
         *         lastModified date of its lucene Directory.
         * @exception IOException
         *                Description of Exception
         */
        public boolean indexReaderIsValid(Directory directory) throws IOException {
            return indexReader != null && IndexReader.getCurrentVersion(directory) == lastModified;
        }

        /**
         * Release all resources, most notably the lucene IndexReader.
         * 
         * @exception Throwable
         *                Description of Exception
         */
        protected void finalize() throws Throwable {
            close();
        }

        /**
         * Create unconditionally a lucene IndexReader.
         * 
         * @param directory
         *            lucene index directory
         * @exception IOException
         *                Description of Exception
         */
        private void createIndexReader(Directory directory) throws IOException {
            close();
            indexReader = IndexReader.open(directory);
            lastModified = IndexReader.getCurrentVersion(directory);
        }
    }

    /**
     * @return the defaultSearchFieldDefault
     */
    public String getDefaultSearchFieldDefault() {
        return defaultSearchFieldDefault;
    }

    /**
     * @param defaultSearchFieldDefault
     *            the defaultSearchFieldDefault to set
     */
    public void setDefaultSearchFieldDefault(String defaultSearchFieldDefault) {
        this.defaultSearchFieldDefault = defaultSearchFieldDefault;
    }

    /**
     * @return the defaultQueryDefault
     */
    public String getDefaultQueryDefault() {
        return defaultQueryDefault;
    }

    /**
     * @param defaultQueryDefault
     *            the defaultQueryDefault to set
     */
    public void setDefaultQueryDefault(String defaultQueryDefault) {
        this.defaultQueryDefault = defaultQueryDefault;
    }

    /**
     * @return the defaultQueryparser
     */
    public String getDefaultQueryparser() {
        return defaultQueryparser;
    }

    /**
     * @param defaultQueryparser
     *            the defaultQueryparser to set
     */
    public void setDefaultQueryparser(String defaultQueryparser) {
        this.defaultQueryparser = defaultQueryparser;
    }

    /**
     * @return the defaultDirectory
     */
    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    /**
     * @param defaultDirectory
     *            the defaultDirectory to set
     */
    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    /**
     * @return the analyzer
     */
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * @param analyzer
     *            the analyzer to set
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

}
