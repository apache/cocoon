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
package org.apache.cocoon.transformation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.search.LuceneCocoonHelper;
import org.apache.cocoon.components.search.LuceneXMLIndexer;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A lucene index creation transformer.
 * <p>FIXME: Write Documentation.</p>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: LuceneIndexTransformer.java,v 1.4 2003/03/19 15:42:17 cziegeler Exp $
 */
public class LuceneIndexTransformer extends AbstractTransformer
    implements Disposable, CacheableProcessingComponent, Recyclable, Configurable, Contextualizable {

    public static final String ANALYZER_CLASSNAME_CONFIG = "analyzer-classname";
    public static final String ANALYZER_CLASSNAME_PARAMETER = "analyzer-classname";
    public static final String ANALYZER_CLASSNAME_DEFAULT = "org.apache.lucene.analysis.standard.StandardAnalyzer";
    public static final String DIRECTORY_CONFIG = "directory";
    public static final String DIRECTORY_PARAMETER = "directory";
    public static final String DIRECTORY_DEFAULT = "index";
    public static final String MERGE_FACTOR_CONFIG = "merge-factor";
    public static final String MERGE_FACTOR_PARAMETER = "merge-factor";
    public static final int MERGE_FACTOR_DEFAULT = 20;

    public static final String LUCENE_URI = "http://apache.org/cocoon/lucene/1.0";
    public static final String LUCENE_QUERY_ELEMENT = "index";
    public static final String LUCENE_QUERY_ANALYZER_ATTRIBUTE = "analyzer";
    public static final String LUCENE_QUERY_DIRECTORY_ATTRIBUTE = "directory";
    public static final String LUCENE_QUERY_CREATE_ATTRIBUTE = "create";
    public static final String LUCENE_QUERY_MERGE_FACTOR_ATTRIBUTE = "merge-factor";
    public static final String LUCENE_DOCUMENT_ELEMENT = "document";
    public static final String LUCENE_DOCUMENT_URL_ATTRIBUTE = "url";
    public static final String LUCENE_ELEMENT_ATTR_TO_TEXT_ATTRIBUTE = "text-attr";
    public static final String LUCENE_ELEMENT_ATTR_STORE_VALUE = "store";

    // Initialization time variables
    protected ComponentManager manager = null;
    protected File workDir = null;

    // Declaration time parameters values
    private String analyzerClassnameDefault;
    private String directoryDefault;
    private int mergeFactorDefault;

    // Invocation time parameters values
    private String analyzerClassname;
    private String directory;
    private int mergeFactor;


    // Runtime variables
    private int processing;
    private IndexWriter writer;
    private StringBuffer bodyText;
    private Document bodyDocument;
    private String bodyDocumentURL;
    private Stack elementStack = new Stack();


    private static String uid(String url) {
        return url.replace('/', '\u0000'); // + "\u0000" + DateField.timeToString(urlConnection.getLastModified());
    }


    public void configure(Configuration conf) throws ConfigurationException {
        this.analyzerClassnameDefault = conf.getChild(ANALYZER_CLASSNAME_CONFIG)
            .getValue(ANALYZER_CLASSNAME_DEFAULT);
        this.mergeFactorDefault = conf.getChild(MERGE_FACTOR_CONFIG)
            .getValueAsInteger(MERGE_FACTOR_DEFAULT);
        this.directoryDefault = conf.getChild(DIRECTORY_CONFIG)
            .getValue(DIRECTORY_DEFAULT);
    }

    /**
     * Setup the transformer.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        // We don't need all this stuff
        this.analyzerClassname = parameters.getParameter(ANALYZER_CLASSNAME_PARAMETER, analyzerClassnameDefault);
        this.directory = parameters.getParameter(DIRECTORY_PARAMETER, directoryDefault);
        this.mergeFactor = parameters.getParameterAsInteger(MERGE_FACTOR_PARAMETER, mergeFactorDefault);
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /**
     * Contextualize this class
     */
    public void contextualize(Context context) throws ContextException {
        this.workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
    }

    public void recycle() {
        this.processing = 0;
        if (this.writer != null) {
            try { this.writer.close(); } catch (IOException ioe) { }
            this.writer = null;
        }
        this.bodyText = null;
        this.bodyDocument = null;
        this.bodyDocumentURL = null;
        this.elementStack.clear();
    }

    public void dispose() {
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
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
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (processing == 0) {
            super.startPrefixMapping(prefix,uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (processing == 0) {
            super.endPrefixMapping(prefix);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {

        if (processing == 0) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_QUERY_ELEMENT.equals(localName)){
                String sCreate = atts.getValue(LUCENE_QUERY_CREATE_ATTRIBUTE);
                boolean bCreate = sCreate != null &&
                    (sCreate.equalsIgnoreCase("yes") || sCreate.equalsIgnoreCase("true"));

                String analyzerClassname =
                    atts.getValue(LUCENE_QUERY_ANALYZER_ATTRIBUTE);
                if (analyzerClassname == null)
                    analyzerClassname = this.analyzerClassname;
                Analyzer analyzer = LuceneCocoonHelper.getAnalyzer(analyzerClassname);

                String sMergeFactor =
                    atts.getValue(LUCENE_QUERY_MERGE_FACTOR_ATTRIBUTE);
                int mergeFactor = this.mergeFactor;
                if (sMergeFactor != null)
                    mergeFactor = Integer.parseInt(sMergeFactor);

                String directoryName =
                    atts.getValue(LUCENE_QUERY_DIRECTORY_ATTRIBUTE);
                if (directoryName == null)
                    directoryName = this.directory;

                // System.out.println("QUERY Create=" + bCreate + ", Directory=" + directoryName + ", Analyzer=" + analyzerClassname);
                try {
                    Directory directory = LuceneCocoonHelper.getDirectory(
                        new File(workDir, directoryName), bCreate);

                    writer = new IndexWriter(directory, analyzer, bCreate);
                    writer.mergeFactor = mergeFactor;
                } catch (IOException e) {
                    throw new SAXException(e);
                }

                processing = 1;
            } else {
                super.startElement(namespaceURI, localName, qName, atts);
            }
        } else if (processing == 1) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_DOCUMENT_ELEMENT.equals(localName)){
                this.bodyDocumentURL = atts.getValue(LUCENE_DOCUMENT_URL_ATTRIBUTE);
                if (this.bodyDocumentURL == null)
                    throw new SAXException("<lucene:document> must have @url attribute");

                // System.out.println("  DOCUMENT URL=" + bodyDocumentURL);
                this.bodyText = new StringBuffer();
                this.bodyDocument = new Document();
                this.elementStack.clear();
                processing = 2;
            } else {
                throw new SAXException("<lucene:query> element can contain only <lucene:document> elements!");
            }
        } else if (processing == 2) {
            elementStack.push(new IndexHelperField(localName, new AttributesImpl(atts)));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {

        if (processing == 1) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_QUERY_ELEMENT.equals(localName)) {
                // End query processing
                // System.out.println("QUERY END!");
                try {
                    this.writer.optimize();
                    this.writer.close();
                    this.writer = null;
                } catch (IOException e) {
                    throw new SAXException(e);
                }

                this.processing = 0;
            } else {
                throw new SAXException("</lucene:query> was expected!");
            }
        } else if (processing == 2) {
            if (LUCENE_URI.equals(namespaceURI) && LUCENE_DOCUMENT_ELEMENT.equals(localName)) {
                // End document processing
                this.bodyDocument.add(Field.UnStored(LuceneXMLIndexer.BODY_FIELD, this.bodyText.toString()));
                System.out.println("    DOCUMENT BODY=" + this.bodyText);
                this.bodyText = null;

                this.bodyDocument.add(Field.UnIndexed(LuceneXMLIndexer.URL_FIELD, this.bodyDocumentURL));
                // store: false, index: true, tokenize: false
                this.bodyDocument.add(new Field(LuceneXMLIndexer.UID_FIELD, uid(this.bodyDocumentURL), false, true, false));
                // System.out.println("    DOCUMENT UID=" + uid(this.bodyDocumentURL));
                this.bodyDocumentURL = null;
                // System.out.println("  DOCUMENT END!");
                try {
                    this.writer.addDocument(this.bodyDocument);
                    this.bodyDocument = null;
                } catch (IOException e) {
                    throw new SAXException(e);
                }

                this.processing = 1;
            } else {
                // End element processing
                IndexHelperField tos = (IndexHelperField) elementStack.pop();
                StringBuffer text = tos.getText();

                Attributes atts = tos.getAttributes();
                boolean attributesToText = atts.getIndex(LUCENE_URI, LUCENE_ELEMENT_ATTR_TO_TEXT_ATTRIBUTE) != -1;
                for (int i = 0; atts != null && i < atts.getLength(); i++) {
                    // Ignore Lucene attributes
                    if (LUCENE_URI.equals(atts.getURI(i)))
                        continue;

                    String atts_lname = atts.getLocalName(i);
                    String atts_value = atts.getValue(i);
                    // System.out.println("        ATTRIBUTE " + localName + "@" + atts_lname + "=" + atts_value);
                    bodyDocument.add(Field.UnStored(localName + "@" + atts_lname, atts_value));
                    if (attributesToText) {
                        text.append(atts_value);
                        text.append(' ');
                        bodyText.append(atts_value);
                        bodyText.append(' ');
                    }
                }

                boolean store = atts.getIndex(LUCENE_URI, LUCENE_ELEMENT_ATTR_STORE_VALUE) != -1;
                if (text != null && text.length() > 0) {
                    // System.out.println("      ELEMENT " + localName + "=" + text);
                    if (store) {
                        bodyDocument.add(Field.Text(localName, text.toString()));
                    } else {
                        bodyDocument.add(Field.UnStored(localName, text.toString()));
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

        if (processing == 2 && ch.length > 0 && start >= 0 && length > 1 && elementStack.size() > 0) {
            String text = new String(ch, start, length);
            ((IndexHelperField) elementStack.peek()).append(text);
            bodyText.append(text);
            bodyText.append(' ');
        } else if (processing == 0) {
            super.characters(ch, start, length);
        }
    }


    class IndexHelperField
    {
        String localName;
        StringBuffer text;
        Attributes attributes;

        IndexHelperField(String localName, Attributes atts) {
            this.localName = localName;
            this.attributes = atts;
            this.text = new StringBuffer();
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public StringBuffer getText() {
            return text;
        }

        public void append(String text) {
            this.text.append(text);
        }

        public void append(char[] str, int offset, int length) {
            this.text.append(str, offset, length);
        }
    }
}
