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
package org.apache.cocoon.woody.datatype;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.xml.sax.XMLizable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * An implementation of a SelectionList. Create instances of this class by using
 * the {@link SelectionListBuilder}. This implementation is called "Static" because
 * the items in the list are build once from its source, and then list items are
 * cached as part of this object. In contrast, the {@link DynamicSelectionList}
 * will retrieve its content from its source each time it's needed.
 * 
 * @version $Id: StaticSelectionList.java,v 1.9 2004/02/11 09:53:43 antonio Exp $
 */
public class StaticSelectionList implements SelectionList {
    /** The datatype to which this selection list belongs */
    private Datatype datatype;
    private List items = new ArrayList();

    public StaticSelectionList(Datatype datatype) {
        this.datatype = datatype;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        Convertor.FormatCache formatCache = new DefaultFormatCache();
        contentHandler.startElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);
        Iterator itemIt = items.iterator();
        while (itemIt.hasNext()) {
            SelectionListItem item = (SelectionListItem)itemIt.next();
            item.generateSaxFragment(contentHandler, locale, formatCache);
        }
        contentHandler.endElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL);
    }

    public List getItems() {
        return items;
    }

    /**
     * Adds a new item to this selection list.
     * @param value a value of the correct type (i.e. the type with which this selectionlist is associated)
     * @param label a SAX-fragment such as a {@link org.apache.cocoon.xml.SaxBuffer}, can be null
     */
    public void addItem(Object value, XMLizable label) {
        items.add(new SelectionListItem(value, label));
    }

    public final class SelectionListItem {
        private final Object value;
        private final XMLizable label;

        public SelectionListItem(Object value, XMLizable label) {
            this.value = value;
            this.label = label;
        }

        public Object getValue() {
            return value;
        }

        public void generateSaxFragment(ContentHandler contentHandler, Locale locale, Convertor.FormatCache formatCache)
                throws SAXException
        {
            AttributesImpl itemAttrs = new AttributesImpl();
            String stringValue;
            if (this.value == null) {
                // Null value translates into the empty string
                stringValue = "";
            } else {
                stringValue = datatype.getConvertor().convertToString(value, locale, formatCache);
            }
            itemAttrs.addCDATAAttribute("value", stringValue);
            contentHandler.startElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL, itemAttrs);
            contentHandler.startElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
            if (label != null) {
                label.toSAX(contentHandler);
            } else {
                contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            }
            contentHandler.endElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL);
            contentHandler.endElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL);
        }
    }
}
