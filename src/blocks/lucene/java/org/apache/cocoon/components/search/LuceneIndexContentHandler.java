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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Parse XML and generate lucene document(s)
 *
 *	can now be configured via SimpleLuceneXMLIndexerImpl
 *  to store specific tags in Lucene, so that you can
 *  display them with hits.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @author <a href="mailto:jeremy@apache.org">Jeremy Quinn</a>
 * @version CVS $Id: LuceneIndexContentHandler.java,v 1.6 2004/03/17 21:21:55 joerg Exp $
 */
public class LuceneIndexContentHandler implements ContentHandler
{
    public static final String LUCENE_URI = "http://apache.org/cocoon/lucene/1.0";

    /**
     * If this attribute is specified on element, values of all attributes
     * are added to the text of the element, and to the document
     * body text
     */
    public static final String LUCENE_ATTR_TO_TEXT_ATTRIBUTE = "text-attr";

    StringBuffer bodyText;
    private List documents;
    private Document bodyDocument;
    private Stack elementStack;
    private HashSet fieldTags;

    /**
     * Constructor for the LuceneIndexContentHandler object
     */
    public LuceneIndexContentHandler() {
        this.bodyText = new StringBuffer();
        this.bodyDocument = new Document();
        this.documents = new ArrayList();
        this.documents.add(this.bodyDocument);
        this.elementStack = new Stack();
        this.fieldTags = new HashSet();
    }

    /**
     * Sets the fieldTags attribute of the LuceneIndexContentHandler object
     *
     * @param  fieldTags  The new fieldTags value
     */
    public void setFieldTags(HashSet fieldTags) { 
    	this.fieldTags = fieldTags;
    }

    /**
     * Sets the documentLocator attribute of the LuceneIndexContentHandler object
     *
     * @param  locator  The new documentLocator value
     */
    public void setDocumentLocator(Locator locator) { }

    public List allDocuments() {
        return documents;
    }

    public Iterator iterator() {
        return documents.iterator();
    }

    public void characters(char[] ch, int start, int length) {
        if (ch.length > 0 && start >= 0 && length > 1) {
            if (elementStack.size() > 0) {
                IndexHelperField tos = (IndexHelperField) elementStack.peek();
                tos.appendText(ch, start, length);
            }
            bodyText.append(' ');
            bodyText.append(ch, start, length);
        }
    }

    public void endDocument() {
        bodyDocument.add(Field.UnStored(LuceneXMLIndexer.BODY_FIELD, bodyText.toString()));
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        IndexHelperField tos = (IndexHelperField) elementStack.pop();
        String lname = tos.getLocalFieldName();
        StringBuffer text = tos.getText();

        // (VG): Atts are never null, see startElement
        Attributes atts = tos.getAttributes();
        boolean attributesToText = atts.getIndex(LUCENE_URI, LUCENE_ATTR_TO_TEXT_ATTRIBUTE) != -1;
        for (int i = 0; i < atts.getLength(); i++) {
            if (LUCENE_URI.equals(atts.getURI(i))) continue;

            String atts_lname = atts.getLocalName(i);
            String atts_value = atts.getValue(i);
            bodyDocument.add(Field.UnStored(lname + "@" + atts_lname, atts_value));
            if (attributesToText) {
                text.append(' ');
                text.append(atts_value);
                bodyText.append(' ');
                bodyText.append(atts_value);
            }
        }

        if (text != null && text.length() > 0) {
        	if (isFieldTag(lname)) {
        		bodyDocument.add(Field.UnIndexed(lname, text.toString()));
        	}
        	bodyDocument.add(Field.UnStored(lname, text.toString()));
        }
    }

    public void endPrefixMapping(String prefix) { }

    public void ignorableWhitespace(char[] ch, int start, int length) { }

    public void processingInstruction(String target, String data) { }

    public void skippedEntity(String name) { }

    public void startDocument() { }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        IndexHelperField ihf = new IndexHelperField(localName, qName, new AttributesImpl(atts));
        elementStack.push(ihf);
    }

    public void startPrefixMapping(String prefix, String uri) { }

    /**
     * check if tag is a candidate for making into a Field
     *
     * @param  tag  local name of the tag we are processing
     * @return      boolean
     */
    private boolean isFieldTag(String tag) {
        // by default do not make field
        if (fieldTags == null) {
            return false;
        }
        Iterator i = fieldTags.iterator();
        while (i.hasNext()) {
            if (tag.equals(i.next())) {
                return true;
            }
        }
        return false;
    }
}
