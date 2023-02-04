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
package org.apache.cocoon.transformation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.search.LuceneCocoonHelper;
import org.apache.cocoon.components.search.LuceneXMLIndexer;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
  * <p style="font-weight: bold;">A lucene index creation transformer.</p>
  * <p>This transformer reads a document with elements in the namespace 
  * <code>http://apache.org/cocoon/lucene/1.0</code>, and creates a new Lucene Index,
  * or updates an existing one.</p>
  * <p>It has several parameters which can be set in the sitemap component configuration or as 
  * parameters to the transformation step in the pipeline, or finally as attributes of the root element
  * in the source XML document. The source document over-rides the transformation parameters, 
  * which in turn over-ride any configuration parameters.</p>
  * <dl>
  * <dt>
  * <dt style="font-weight: bold;">directory</dt>
  * <dd><p>Location of directory where index files are stored. 
  * This path is relative to the Cocoon work directory</p></dd>
  * <dt style="font-weight: bold;">create</dt>
  * <dd><p>This attribute controls whether the index is recreated.  </p>
  *    <ul><li><p>If create = "false" and the index already exists then the index will be updated. 
  *    Any documents which had already been indexed will be removed from the index and reinserted.</p></li>
  *    <li><p>If the index does not exist then it will be created even if <code>create</code>="false".</p></li>
  *    <li><p>If <code>create</code>="true" then any existing index will be destroyed and a new index created. 
  *     If you are rebuilding your entire index then you should set <code>create</code>="true" because the 
  *     indexer doesn't need to remove old documents from the index, so it will be faster.</p></li></ul>
  * </dd>
  * <dt style="font-weight: bold;">max-field-length</dt>
  * <dd><p>Maximum number of terms to index in a field (as far as the index is concerned,
  *    the document will effectively be truncated at this point. The default value, 10k, may not be sufficient for large documents.</p></dd>
  * <dt style="font-weight: bold;">analyzer</dt>
  * <dd><p>Class name of the Lucene text analyzer to use. Typically depends on the language of the text being indexed.
  * See the Lucene documentation for more information.</p></dd>
  * <dt style="font-weight: bold;">merge-factor</dt>
  * <dd><p>Determines how often segment indices are merged. See the Lucene documentation for more information.</p></dd>
  * <dt style="font-weight: bold;">optimize-frequency</dt>
  * <dd><p>Determines how often the lucene index will be optimized. When you have 1000's of documents, optimizing the index
  * can become quite slow (eg. 7 seconds for 9000 small docs, P4).</p>
  *
  * <ul>
  * <li>1: always optimize (default)</li>
  * <li>0: never optimize</li>
  * <li>x: update every x times. You can use any number, it is a random generator which will determine to optimize or not. </li>   
  * </ul>
  * 
  * </dd>
  * </dl>
  * <dl>
  * <dt style="font-weight: bold;">A simple example of the input:</dt>
  * <dd>
  * <pre>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
  * &lt;lucene:index xmlns:lucene="http://apache.org/cocoon/lucene/1.0" 
  *     merge-factor="20" 
  *     create="false" 
  *     directory="index" 
  *     max-field-length="10000"
  *     optimize-frequency="1"
  *     analyzer="org.apache.lucene.analysis.standard.StandardAnalyzer"&gt;
  *     &lt;lucene:document url="a.html"&gt;
  *             &lt;documentTitle lucene:store="true"&gt;Doggerel&lt;/documentTitle&gt;
  *             &lt;body&gt;The quick brown fox jumped over the lazy dog&lt;/body&gt;    
  *     &lt;/lucene:document&gt;
  *     &lt;lucene:document url="b.html"&gt;
  *             &lt;documentTitle lucene:store="true"&gt;Lorem Ipsum&lt;/documentTitle&gt;
  *             &lt;body&gt;Lorem ipsum dolor sit amet, consectetuer adipiscing elit.&lt;/body&gt;
  *             &lt;body&gt;Nunc a mauris blandit ligula scelerisque tristique.&lt;/body&gt;    
  *     &lt;/lucene:document&gt;
  * &lt;/lucene:index&gt;
  * </pre>
  * </dd>
  * </dl>
  *
  * @version $Id$
  */
public class LuceneIndexTransformer extends AbstractTransformer
    implements CacheableProcessingComponent,
               InitializingBean {

    public static final String ANALYZER_CLASSNAME_CONFIG = "analyzer-classname";
    public static final String ANALYZER_CLASSNAME_PARAMETER = "analyzer-classname";
    public static final String ANALYZER_CLASSNAME_DEFAULT = "org.apache.lucene.analysis.standard.StandardAnalyzer";
    public static final String DIRECTORY_CONFIG = "directory";
    public static final String DIRECTORY_PARAMETER = "directory";
    public static final String DIRECTORY_DEFAULT = "index";
    public static final String MERGE_FACTOR_CONFIG = "merge-factor";
    public static final String MERGE_FACTOR_PARAMETER = "merge-factor";
    public static final int MERGE_FACTOR_DEFAULT = 20;

    public static final String OPTIMIZE_FREQUENCY_CONFIG = "optimize-frequency";
    public static final String OPTIMIZE_FREQUENCY_PARAMETER = "optimize-frequency";
    // by default, optimizing will take place on every update (previous behaviour)
    public static final int OPTIMIZE_FREQUENCY_DEFAULT = 1;

    public static final String MAX_FIELD_LENGTH_CONFIG = "max-field-length";
    public static final String MAX_FIELD_LENGTH_PARAMETER = "max-field-length";
    public static final int MAX_FIELD_LENGTH_DEFAULT = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

    public static final String LUCENE_URI = "http://apache.org/cocoon/lucene/1.0";
    public static final String LUCENE_QUERY_ELEMENT = "index";
    public static final String LUCENE_QUERY_ANALYZER_ATTRIBUTE = "analyzer";
    public static final String LUCENE_QUERY_DIRECTORY_ATTRIBUTE = "directory";
    public static final String LUCENE_QUERY_CREATE_ATTRIBUTE = "create";
    public static final String LUCENE_QUERY_MERGE_FACTOR_ATTRIBUTE = "merge-factor";
    public static final String LUCENE_QUERY_MAX_FIELD_LENGTH_ATTRIBUTE = "max-field-length";
    public static final String LUCENE_QUERY_OPTIMIZE_FREQUENCY_CONFIG_ATTRIBUTE = "optimize-frequency";
    public static final String LUCENE_DOCUMENT_ELEMENT = "document";
    public static final String LUCENE_DOCUMENT_URL_ATTRIBUTE = "url";
    public static final String LUCENE_ELEMENT_ATTR_TO_TEXT_ATTRIBUTE = "text-attr";
    public static final String LUCENE_ELEMENT_ATTR_STORE_VALUE = "store";
    public static final String LUCENE_ELAPSED_TIME_ATTRIBUTE = "elapsed-time";
    public static final String CDATA = "CDATA";

    // The 3 states of the state machine
    private static final int STATE_GROUND = 0; // initial or "ground" state
    private static final int STATE_QUERY = 1; // processing a lucene:index (Query) element
    private static final int STATE_DOCUMENT = 2; // processing a lucene:document element

    // Declaration time parameters values (specified in sitemap component config)
    private IndexerConfiguration configureConfiguration;
    // Invocation time parameters values (specified in sitemap transform parameters)
    private IndexerConfiguration setupConfiguration;
    // Parameters specified in the input document
    private IndexerConfiguration queryConfiguration;

    // Runtime variables
    private int processing;
    private boolean createIndex = false;
    private IndexWriter writer;
    private StringBuffer bodyText;
    private Document bodyDocument;
    private String bodyDocumentURL;
    private Stack elementStack = new Stack();
    /**
     * Storage for the document element's attributes until the document has been
     * indexed, so that they can be copied to the output along with a boolean
     * <code>indexed</code> attribute.
     */
    private AttributesImpl documentAttributes;
    private long documentStartTime;

    /**
     * Class name of the Lucene text analyzer to use. Typically depends on the
     * language of the text being indexed. See the Lucene documentation for more
     * information.
     */
    private String analyzer = ANALYZER_CLASSNAME_DEFAULT;

    /**
     * Location of directory where index files are stored. This path is relative
     * to the Cocoon work directory
     */
    private String directory = DIRECTORY_DEFAULT;

    /**
     * Determines how often segment indices are merged. See the Lucene
     * documentation for more information.
     */
    private int mergeFactor = MERGE_FACTOR_DEFAULT;

    /**
     * Maximum number of terms to index in a field (as far as the index is
     * concerned, the document will effectively be truncated at this point. The
     * default value, 10k, may not be sufficient for large documents.
     */
    private int maxFieldLength = MAX_FIELD_LENGTH_DEFAULT;

    /** Determines how often the lucene index will be optimized. */
    private int optimizeFrequency = OPTIMIZE_FREQUENCY_DEFAULT;

    private static String uid(String url) {
        return url.replace('/', '\u0000');
    }

    /**
     * Configure the transformer. The configuration parameters are stored as
     * general defaults, which may be over-ridden by parameters specified as
     * parameters in the sitemap pipeline, or by attributes of the query
     * element(s) in the XML input document.
     */
    public void afterPropertiesSet() throws IllegalArgumentException {
        this.configureConfiguration = new IndexerConfiguration(
                getAnalyzer(),
                getDirectory(),
                getMergeFactor(),
                getMaxFieldLength(),
                getOptimizeFrequency());
    }

    /**
     * Setup the transformer. Called when the pipeline is assembled. The
     * parameters are those specified as child elements of the
     * <code>&lt;map:transform&gt;</code> element in the sitemap. These
     * parameters are optional: If no parameters are specified here then the
     * defaults are supplied by the component configuration. Any parameters
     * specified here may be over-ridden by attributes of the lucene:index
     * element in the input document.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        setupConfiguration = new IndexerConfiguration(
                parameters.getParameter(ANALYZER_CLASSNAME_PARAMETER, configureConfiguration.analyzerClassname),
                parameters.getParameter(DIRECTORY_PARAMETER, configureConfiguration.indexDirectory),
                parameters.getParameterAsInteger(MERGE_FACTOR_PARAMETER, configureConfiguration.indexerMergeFactor),
                parameters.getParameterAsInteger(MAX_FIELD_LENGTH_PARAMETER, configureConfiguration.indexerMaxFieldLength),
                parameters.getParameterAsInteger(OPTIMIZE_FREQUENCY_PARAMETER, configureConfiguration.indexerOptimizeFrequency));
    }

    /**
     * @see org.apache.cocoon.xml.AbstractXMLProducer#recycle()
     */
    public void recycle() {
        this.processing = STATE_GROUND;
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException ioe) {
            }
            this.writer = null;
        }
        this.bodyText = null;
        this.bodyDocument = null;
        this.bodyDocumentURL = null;
        this.elementStack.clear();
        super.recycle();
    }

    /**
     * Generate the unique key. This key must be unique inside the space of this
     * component.
     * 
     * @return The generated key
     */
    public Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     * 
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    public void startDocument() throws SAXException {
        super.startDocument();
    }

    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     * 
     * @param prefix
     *            The Namespace prefix being declared.
     * @param uri
     *            The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (processing == STATE_GROUND) {
            super.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     * 
     * @param prefix
     *            The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (processing == STATE_GROUND) {
            super.endPrefixMapping(prefix);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {

        if (processing == STATE_GROUND) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_QUERY_ELEMENT.equals(localName)) {
                String sCreate = atts.getValue(LUCENE_QUERY_CREATE_ATTRIBUTE);
                createIndex = BooleanUtils.toBoolean(sCreate);

                String analyzerClassname = atts.getValue(LUCENE_QUERY_ANALYZER_ATTRIBUTE);
                String indexDirectory = atts.getValue(LUCENE_QUERY_DIRECTORY_ATTRIBUTE);
                String mergeFactorStr = atts.getValue(LUCENE_QUERY_MERGE_FACTOR_ATTRIBUTE);
                String maxFieldLengthStr = atts.getValue(LUCENE_QUERY_MAX_FIELD_LENGTH_ATTRIBUTE);
                String optimizeFrequencyStr = atts.getValue(LUCENE_QUERY_OPTIMIZE_FREQUENCY_CONFIG_ATTRIBUTE);

                queryConfiguration = new IndexerConfiguration(
                        analyzerClassname != null ? analyzerClassname : setupConfiguration.analyzerClassname,
                        indexDirectory != null ? indexDirectory : setupConfiguration.indexDirectory,
                        mergeFactorStr != null ? Integer.parseInt(mergeFactorStr) : setupConfiguration.indexerMergeFactor,
                        maxFieldLengthStr != null ? Integer.parseInt(maxFieldLengthStr) : setupConfiguration.indexerMaxFieldLength,
                        optimizeFrequencyStr != null ? Integer.parseInt(optimizeFrequencyStr) : setupConfiguration.indexerOptimizeFrequency);

                if (!createIndex) {
                    // Not asked to create the index - but check if this is necessary anyway:
                    try {
                        IndexReader reader = openReader();
                        reader.close();
                    } catch (IOException ioe) {
                        // couldn't open the index - so recreate it
                        createIndex = true;
                    }
                }
                // propagate the lucene:index to the next stage in the pipeline
                super.startElement(namespaceURI, localName, qName, atts);
                processing = STATE_QUERY;
            } else {
                super.startElement(namespaceURI, localName, qName, atts);
            }
        } else if (processing == STATE_QUERY) {
            // processing a lucene:index - expecting a lucene:document
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_DOCUMENT_ELEMENT.equals(localName)) {
                this.bodyDocumentURL = atts.getValue(LUCENE_DOCUMENT_URL_ATTRIBUTE);
                if (this.bodyDocumentURL == null) {
                    throw new SAXException("<lucene:document> must have @url attribute");
                }

                // Remember the time the document indexing began
                this.documentStartTime = System.currentTimeMillis();
                // remember these attributes so they can be passed on to the next stage in the pipeline,
                // when this document element is ended.
                this.documentAttributes = new AttributesImpl(atts);
                this.bodyText = new StringBuffer();
                this.bodyDocument = new Document();
                this.elementStack.clear();
                processing = STATE_DOCUMENT;
            } else {
                throw new SAXException("<lucene:index> element can contain only <lucene:document> elements!");
            }
        } else if (processing == STATE_DOCUMENT) {
            elementStack.push(new IndexHelperField(localName, new AttributesImpl(atts)));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {

        if (processing == STATE_QUERY) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_QUERY_ELEMENT.equals(localName)) {
                if (needToOptimize()) {
                    // End query processing
                    try {
                        if (this.writer == null) {
                            openWriter();
                        }
                        this.writer.optimize();
                        this.writer.close();
                        this.writer = null;
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }
                }
                // propagate the query element to the next stage in the pipeline
                super.endElement(namespaceURI, localName, qName);
                this.processing = STATE_GROUND;
            } else {
                throw new SAXException("</lucene:index> was expected!");
            }
        } else if (processing == STATE_DOCUMENT) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_DOCUMENT_ELEMENT.equals(localName)) {
                // End document processing
                this.bodyDocument.add(new Field(LuceneXMLIndexer.BODY_FIELD, this.bodyText.toString(), Field.Store.NO, Field.Index.TOKENIZED));
                this.bodyText = null;

                this.bodyDocument.add(new Field(LuceneXMLIndexer.URL_FIELD, this.bodyDocumentURL, Field.Store.YES, Field.Index.NO));
                // store: false, index: true, tokenize: false
                this.bodyDocument.add(new Field(LuceneXMLIndexer.UID_FIELD, uid(this.bodyDocumentURL), Field.Store.NO, Field.Index.UN_TOKENIZED));
                try {
                    reindexDocument();
                } catch (IOException e) {
                    throw new SAXException(e);
                }
                this.bodyDocumentURL = null;

                // propagate the lucene:document element to the next stage in the pipeline
                long elapsedTime = System.currentTimeMillis() - this.documentStartTime;

                this.documentAttributes.addAttribute("",
                                                     LUCENE_ELAPSED_TIME_ATTRIBUTE,
                                                     LUCENE_ELAPSED_TIME_ATTRIBUTE,
                                                     CDATA,
                                                     String.valueOf(elapsedTime));
                super.startElement(namespaceURI, localName, qName, this.documentAttributes);
                super.endElement(namespaceURI, localName, qName);
                this.processing = STATE_QUERY;
            } else {
                // End element processing
                IndexHelperField tos = (IndexHelperField) elementStack.pop();
                StringBuffer text = tos.getText();

                Attributes atts = tos.getAttributes();
                boolean attributesToText = atts.getIndex(LUCENE_URI, LUCENE_ELEMENT_ATTR_TO_TEXT_ATTRIBUTE) != -1;
                for (int i = 0; i < atts.getLength(); i++) {
                    // Ignore Lucene attributes
                    if (LUCENE_URI.equals(atts.getURI(i))) {
                        continue;
                    }

                    String atts_lname = atts.getLocalName(i);
                    String atts_value = atts.getValue(i);
                    bodyDocument.add(new Field(localName + "@" + atts_lname, atts_value, Field.Store.NO, Field.Index.TOKENIZED));
                    if (attributesToText) {
                        text.append(atts_value);
                        text.append(' ');
                        bodyText.append(atts_value);
                        bodyText.append(' ');
                    }
                }

                boolean store = atts.getIndex(LUCENE_URI, LUCENE_ELEMENT_ATTR_STORE_VALUE) != -1;
                if (text != null && text.length() > 0) {
                    if (store) {
                        bodyDocument.add(new Field(localName, text.toString(), Field.Store.YES, Field.Index.TOKENIZED));
                    } else {
                        bodyDocument.add(new Field(localName, text.toString(), Field.Store.NO, Field.Index.TOKENIZED));
                    }
                }
            }
        } else {
            // All other tags
            super.endElement(namespaceURI, localName, qName);
        }
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {

        if (processing == STATE_DOCUMENT && ch.length > 0 && start >= 0 && length > 1 && elementStack.size() > 0) {
            String text = new String(ch, start, length);
            ((IndexHelperField) elementStack.peek()).append(text);
            bodyText.append(text);
            bodyText.append(' ');
        } else if (processing == STATE_GROUND) {
            super.characters(ch, start, length);
        }
    }

    private void openWriter() throws IOException {
        final Settings settings = (Settings) WebAppContextUtils.getCurrentWebApplicationContext().getBean(
                "org.apache.cocoon.configuration.Settings");
        final File workDir = new File(settings.getWorkDirectory());
        File indexDirectory = new File(queryConfiguration.indexDirectory);
        if (!indexDirectory.isAbsolute()) {
            indexDirectory = new File(workDir, queryConfiguration.indexDirectory);
        }

        // If the index directory doesn't exist, then always create it.
        boolean indexExists = IndexReader.indexExists(indexDirectory);
        if (!indexExists) {
            createIndex = true;
        }

        // Get the index directory, creating it if necessary
        Directory directory = LuceneCocoonHelper.getDirectory(indexDirectory, createIndex);
        Analyzer analyzer = LuceneCocoonHelper.getAnalyzer(queryConfiguration.analyzerClassname);
        this.writer = new IndexWriter(directory, analyzer, createIndex);
        this.writer.setMergeFactor(queryConfiguration.indexerMergeFactor);
        this.writer.setMaxFieldLength(queryConfiguration.indexerMaxFieldLength);
    }

    private IndexReader openReader() throws IOException {
        final Settings settings = (Settings) WebAppContextUtils.getCurrentWebApplicationContext().getBean(
                "org.apache.cocoon.configuration.Settings");
        final File workDir = new File(settings.getWorkDirectory());
        File indexDirectory = new File(queryConfiguration.indexDirectory);
        if (!indexDirectory.isAbsolute()) {
            indexDirectory = new File(workDir, queryConfiguration.indexDirectory);
        }
        Directory directory = LuceneCocoonHelper.getDirectory(indexDirectory, createIndex);
        IndexReader reader = IndexReader.open(directory);
        return reader;
    }

    private void reindexDocument() throws IOException {
        if (this.createIndex) {
            // The index is being created, so there's no need to delete the doc from an existing index.
            // This means we can keep a single IndexWriter open throughout the process.
            if (this.writer == null) {
                openWriter();
            }
            this.writer.addDocument(this.bodyDocument);
        } else {
            // This is an incremental reindex, so the document should be removed from the index before adding it
            try {
                IndexReader reader = openReader();
                reader.deleteDocuments(new Term(LuceneXMLIndexer.UID_FIELD, uid(this.bodyDocumentURL)));
                reader.close();
            } catch (IOException e) {
                /* ignore */
            }
            openWriter();
            this.writer.addDocument(this.bodyDocument);
            this.writer.close();
            this.writer = null;
        }
        this.bodyDocument = null;
    }

    private static class IndexHelperField {
        String localName;
        StringBuffer text;
        Attributes attributes;

        IndexHelperField(String localName, Attributes atts) {
            this.localName = localName;
            this.attributes = atts;
            this.text = new StringBuffer();
        }

        Attributes getAttributes() {
            return attributes;
        }

        StringBuffer getText() {
            return text;
        }

        void append(String text) {
            this.text.append(text);
        }

        void append(char[] str, int offset, int length) {
            this.text.append(str, offset, length);
        }
    }

    private static class IndexerConfiguration {
        String analyzerClassname;
        String indexDirectory;
        int indexerMergeFactor;
        int indexerMaxFieldLength;
        int indexerOptimizeFrequency;

        IndexerConfiguration(String analyzerClassname,
                             String indexDirectory,
                             int indexerMergeFactor,
                             int indexerMaxFieldLength,
                             int indexerOptimizeFrequency) {
            this.analyzerClassname = analyzerClassname;
            this.indexDirectory = indexDirectory;
            this.indexerMergeFactor = indexerMergeFactor;
            this.indexerMaxFieldLength = indexerMaxFieldLength;
            this.indexerOptimizeFrequency = indexerOptimizeFrequency;
        }
    }

    /**
     * Will check if, based on the configuration (optimize-frequency option),
     * the lucene index should be optimized. It uses a random number generator
     * to determine if it should optimize or not.
     * 
     * This check was added because of large indexes, optimizing becomes quite
     * slow.
     * 
     * From the lucene documentation: The IndexWriter class supports an
     * optimize() method that compacts the index database and speedup queries.
     * You may want to use this method after performing a complete indexing of
     * your document set or after incremental updates of the index. If your
     * incremental update adds documents frequently, you want to perform the
     * optimization only once in a while to avoid the extra overhead of the
     * optimization.
     * 
     * @return true if we should optimize the index
     */
    private boolean needToOptimize() {
        int optimizeFrequency = queryConfiguration.indexerOptimizeFrequency;
        if (optimizeFrequency == 0) {
            return false;
        }
        if (optimizeFrequency == 1) {
            return true;
        }

        // use a random int to determine if we may execute
        int randomInt = 1 + (int) (Math.random() * optimizeFrequency);
        if (randomInt == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the analyzer
     */
    public String getAnalyzer() {
        return analyzer;
    }

    /**
     * @param analyzer
     *            the analyzer to set
     */
    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory
     *            the directory to set
     */
    public void setDirectory(String directory) {
        this.directory = directory;
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
     * @return the maxFieldLength
     */
    public int getMaxFieldLength() {
        return maxFieldLength;
    }

    /**
     * @param maxFieldLength
     *            the maxFieldLength to set
     */
    public void setMaxFieldLength(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
    }

    /**
     * @return the optimizeFrequency
     */
    public int getOptimizeFrequency() {
        return optimizeFrequency;
    }

    /**
     * @param optimizeFrequency
     *            the optimizeFrequency to set
     */
    public void setOptimizeFrequency(int optimizeFrequency) {
        this.optimizeFrequency = optimizeFrequency;
    }
}
