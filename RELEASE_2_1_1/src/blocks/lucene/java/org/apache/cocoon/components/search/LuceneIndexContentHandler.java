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
 * @version CVS $Id: LuceneIndexContentHandler.java,v 1.3 2003/03/24 14:33:54 stefano Exp $
 */
public class LuceneIndexContentHandler implements ContentHandler
{
    public static final String LUCENE_URI = "http://apache.org/cocoon/lucene/1.0";

    /** If this attribute is specified on element, values of all attributes
     * if this element added to the text of the element, and to the document
     * body text */
    public static final String LUCENE_ATTR_TO_TEXT_ATTRIBUTE = "text-attr";

    StringBuffer bodyText;
    private List documents;
    private Document bodyDocument;
    private Stack elementStack;
    private HashSet fieldTags;

    /**
     *Constructor for the LuceneIndexContentHandler object
     *
     * @since
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
     *Sets the fieldTags attribute of the LuceneIndexContentHandler object
     *
     * @param  fieldTags  The new fieldTags value
     * @since
     */
    public void setFieldTags(HashSet fieldTags) { 
    	this.fieldTags = fieldTags;
    }

    /**
     *Sets the documentLocator attribute of the LuceneIndexContentHandler object
     *
     * @param  locator  The new documentLocator value
     * @since
     */
    public void setDocumentLocator(Locator locator) { }


    /**
     *Description of the Method
     *
     * @return    Description of the Returned Value
     * @since
     */
    public List allDocuments() {
        return documents;
    }


    /**
     *Description of the Method
     *
     * @return    Description of the Returned Value
     * @since
     */
    public Iterator iterator() {
        return documents.iterator();
    }


    /**
     *Description of the Method
     *
     * @param  ch      Description of Parameter
     * @param  start   Description of Parameter
     * @param  length  Description of Parameter
     * @since
     */
    public void characters(char[] ch, int start, int length) {

        if (ch.length > 0 && start >= 0 && length > 1) {
            String text = new String(ch, start, length);
            if (elementStack.size() > 0) {
                IndexHelperField tos = (IndexHelperField) elementStack.peek();
                tos.appendText(text);
            }
            bodyText.append(text);
        }
    }


    /**
     *Description of the Method
     *
     * @since
     */
    public void endDocument() {
        /*
         *  empty
         */
        bodyDocument.add(Field.UnStored(LuceneXMLIndexer.BODY_FIELD, bodyText.toString()));
    }


    /**
     *Description of the Method
     *
     * @param  namespaceURI  Description of Parameter
     * @param  localName     Description of Parameter
     * @param  qName         Description of Parameter
     * @since
     */
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
                text.append(atts_value);
                text.append(' ');
                bodyText.append(atts_value);
                bodyText.append(' ');
            }
        }

        if (text != null && text.length() > 0) {
        
        	if (isFieldTag(lname)) {
        		bodyDocument.add(Field.UnIndexed(lname, text.toString()));
        	}
        	bodyDocument.add(Field.UnStored(lname, text.toString()));
        }
    }


    /**
     *Description of the Method
     *
     * @param  prefix  Description of Parameter
     * @since
     */
    public void endPrefixMapping(String prefix) { }


    /**
     *Description of the Method
     *
     * @param  ch      Description of Parameter
     * @param  start   Description of Parameter
     * @param  length  Description of Parameter
     * @since
     */
    public void ignorableWhitespace(char[] ch, int start, int length) { }


    /**
     *Description of the Method
     *
     * @param  target  Description of Parameter
     * @param  data    Description of Parameter
     * @since
     */
    public void processingInstruction(String target, String data) { }


    /**
     *Description of the Method
     *
     * @param  name  Description of Parameter
     * @since
     */
    public void skippedEntity(String name) { }


    /**
     *Description of the Method
     *
     * @since
     */
    public void startDocument() { }


    /**
     *Description of the Method
     *
     * @param  namespaceURI  Description of Parameter
     * @param  localName     Description of Parameter
     * @param  qName         Description of Parameter
     * @param  atts          Description of Parameter
     * @since
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        IndexHelperField ihf = new IndexHelperField(localName, qName, new AttributesImpl(atts));
        elementStack.push(ihf);
    }


    /**
     *Description of the Method
     *
     * @param  prefix  Description of Parameter
     * @param  uri     Description of Parameter
     * @since
     */
    public void startPrefixMapping(String prefix, String uri) { }

    /**
     * check if tag is a candidate for making into a Field
     *
     * @param  tag  local name of the tag we are processing
     * @return      boolean
     * @since
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
