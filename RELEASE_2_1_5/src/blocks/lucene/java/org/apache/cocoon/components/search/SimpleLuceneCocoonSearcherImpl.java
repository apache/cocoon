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
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.ClassUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * This class provides searching via lucene.
 *
 * <p>
 *   In order to do searching you need a lucene Directory where the lucene generated
 *   index resides.
 *   Moreover you must know the lucene Analyzer which has been used for
 *   indexing, and which will be used for searching.
 * </p>
 * <p>
 *   Knowing this you can may start searching having a query which is parsable
 *   by an QueryParser, and having the name of the default field to use in
 *   searching.
 * </p>
 * <p>
 *   This class returns an Hit object as its search result.
 * </p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: SimpleLuceneCocoonSearcherImpl.java,v 1.6 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public class SimpleLuceneCocoonSearcherImpl extends AbstractLogEnabled
         implements LuceneCocoonSearcher, Configurable, Serviceable, Disposable, Recyclable
{

    /**
     * Configuration element name of lucene's Analyzer class.
     * <p>
     *   Its value is
     *   <code>analyzer-classname</code>.
     * </p>
     *
     */
    protected final static String ANALYZER_CLASSNAME_CONFIG = "analyzer-classname";
    /**
     * Configuration element default value of lucene's Analyzer class.
     * <p>
     *   Its value is,
     *   <code>org.apache.lucene.analysis.standard.StandardAnalyzer</code>.
     * </p>
     *
     */
    protected final static String ANALYZER_CLASSNAME_DEFAULT = "org.apache.lucene.analysis.standard.StandardAnalyzer";

    /**
     * Configuration element name of default search field.
     * <p>
     *   Its value is
     *   <code>default-seach-field</code>.
     * </p>
     *
     */
    protected final static String DEFAULT_SEARCH_FIELD_CONFIG = "default-search-field";
    /**
     * Configuration element default value of lucene's default search field.
     * <p>
     *   Its value is <code>body</code>.
     * </p>
     *
     */
    protected final static String DEFAULT_SEARCH_FIELD_DEFAULT = "body";

    /**
     * Configuration element name of default-query.
     * <p>
     *   Its value is
     *   <code>default-query</code>.
     * </p>
     *
     */
    protected final static String DEFAULT_QUERY_CONFIG = "default-query";
    /**
     * Configuration element default value of default-query.
     * <p>
     *   Its value is <code>null</code>.
     * </p>
     *
     */
    protected final static String DEFAULT_QUERY_DEFAULT = null;

    /**
     * Configuration element name of query parser class name.
     * <p>
     *   Its value is
     *   <code>queryparser-classname</code>.
     * </p>
     *
     */
    protected final static String QUERYPARSER_CLASSNAME_CONFIG = "queryparser-classname";
    /**
     * Configuration element default value of queryparser-classname.
     * <p>
     *   Its value is
     *   <code>org.apache.lucene.queryParser.QueryParser</code>.
     * </p>
     *
     */
    protected final static String QUERYPARSER_CLASSNAME_DEFAULT = "org.apache.lucene.queryParser.QueryParser";

    /**
     * Configuration element name of lucene's default filesystem default
     * directory.
     * <p>
     *   Its value is <code>directory</code>.
     * </p>
     *
     */
    protected final static String DIRECTORY_CONFIG = "directory";
    /**
     * Configuration element default value of filesystem default directory.
     * <p>
     *   Its value is <code>null</code>.
     * </p>
     *
     */
    protected final static String DIRECTORY_DEFAULT = null;

    /**
     * The service manager instance
     *
     */
    protected ServiceManager manager = null;

    private String analyzerClassnameDefault = ANALYZER_CLASSNAME_DEFAULT;
    private String defaultSearchFieldDefault = DEFAULT_SEARCH_FIELD_DEFAULT;
    private String defaultQueryDefault = DEFAULT_QUERY_DEFAULT;
//    private String queryparserClassnameDefault = QUERYPARSER_CLASSNAME_DEFAULT;
    private String directoryDefault = DIRECTORY_DEFAULT;

    /**
     * The lucene analyzer used for searching
     */
    private Analyzer analyzer;
    /**
     * The lucene directory used for searching
     */
    private Directory directory;
    /**
     * The lucene index searcher used for searching
     */
    private IndexSearcher indexSearcher;

    /**
     * A lucene index reader cache to maximize sharing of
     * lucene index readers
     */
    private IndexReaderCache indexReaderCache;

    /**
     * set an analyzer, overriding the analyzerClassnameDefault.
     *
     * @param  analyzer  The new analyzer value
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }


    /**
     *Sets the directory attribute of the SimpleLuceneCocoonSearcherImpl object
     *
     * @param  directory  The new directory value
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
     *   As an IndexReader might be cached, it is check if the indexReader is
     *   still valid.
     * </p>
     *
     * @return                  IndexReader an up to date indexReader
     * @exception  IOException  is thrown iff it's impossible to create 
     * an IndexReader
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
     * @param  conf                        of this component
     * @exception  ConfigurationException  is thrown iff configuration of 
     *   this component fails
     */
    public void configure(Configuration conf) throws ConfigurationException {
        Configuration child;
        String value;

        child = conf.getChild(ANALYZER_CLASSNAME_CONFIG, false);
        if (child != null) {
            // fix Bugzilla Bug 25277, use child.getValue
            // and in all following blocks
            value = child.getValue(ANALYZER_CLASSNAME_DEFAULT);
            if (value != null) {
                analyzerClassnameDefault = value;
                try {
                    analyzer = (Analyzer) ClassUtils.newInstance(analyzerClassnameDefault);
                } catch (Exception e) {
                    throw new ConfigurationException("Cannot create analyzer of class " +
                            analyzerClassnameDefault, e);
                }
            }
        }

        child = conf.getChild(DEFAULT_SEARCH_FIELD_CONFIG, false);
        if (child != null) {
            value = child.getValue(DEFAULT_SEARCH_FIELD_DEFAULT);
            if (value != null) {
                defaultSearchFieldDefault = value;
            }
        }

        child = conf.getChild(DEFAULT_QUERY_CONFIG, false);
        if (child != null) {
            value = child.getValue(DEFAULT_QUERY_DEFAULT);
            if (value != null) {
                defaultQueryDefault = value;
            }
        }
/*
        child = conf.getChild(QUERYPARSER_CLASSNAME_CONFIG, false);
        if (child != null) {
            value = child.getValue(QUERYPARSER_CLASSNAME_DEFAULT);
            if (value != null) {
                queryparserClassnameDefault = value;
            }
        }
*/
        child = conf.getChild(DIRECTORY_CONFIG, false);
        if (child != null) {
            value = child.getValue(DIRECTORY_DEFAULT);
            if (value != null) {
                directoryDefault = value;
                try {
                    setDirectory(FSDirectory.getDirectory(new File(directoryDefault), false));
                } catch (IOException ioe) {
                    throw new ConfigurationException("Cannot set index directory " + directoryDefault, ioe);
                }
            }
        }
    }


    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param manager  manager of this component
     * @exception  ServiceException  is never thrown
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }


    /**
     * Dispose this component, releasing IndexSearcher, and IndexReaderCache.
     */
    public void dispose() {
        releaseIndexSearcher();
        releaseIndexReaderCache();
    }


    /**
     * Recycle this component, releasing IndexSearcher, and IndexReaderCache.
     */
    public void recycle() {
        releaseIndexSearcher();
        releaseIndexReaderCache();
    }


    /**
     * Search lucene index.
     *
     * @param  query_string             is lucene's query string
     * @param  default_field            the lucene field to run the query
     * @return                          lucene Hits
     * @exception  ProcessingException  iff its not possible do run the query
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
            Query query = QueryParser.parse(query_string, default_field, analyzer);

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
    static class IndexReaderCache 
    {
        private IndexReader indexReader;
        private long lastModified;


        /**
         * Create an IndexReaderCache.
         *
         */
        IndexReaderCache() { }


        /**
         * return cached IndexReader object.
         *
         * @param  directory  lucene index directory
         * @return            The indexReader value
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
         * @param  directory        lucene index directory
         * @return                  boolean  return true if there is a cached IndexReader object,
         *   and its lastModified date is greater equal than the lastModified date
         *   of its lucene Directory.
         * @exception  IOException  Description of Exception
         */
        public boolean indexReaderIsValid(Directory directory) throws IOException {
            return indexReader != null &&
            IndexReader.getCurrentVersion(directory) == lastModified;
        }


        /**
         *  Release all resources, most notably the lucene IndexReader.
         *
         * @exception  Throwable  Description of Exception
         */
        protected void finalize() throws Throwable {
            close();
        }


        /**
         * Create unconditionally a lucene IndexReader.
         *
         * @param  directory        lucene index directory
         * @exception  IOException  Description of Exception
         */
        private void createIndexReader(Directory directory) throws IOException {
            close();
            indexReader = IndexReader.open(directory);
            lastModified = IndexReader.getCurrentVersion(directory);
        }
    }
}

