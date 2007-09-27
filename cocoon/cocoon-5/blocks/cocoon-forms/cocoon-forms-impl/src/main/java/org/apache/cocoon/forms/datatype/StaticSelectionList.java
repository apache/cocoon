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
package org.apache.cocoon.forms.datatype;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
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
 * @version $Id$
 */
public class StaticSelectionList implements SelectionList {
    /**
     * The datatype to which this selection list belongs
     */
    private Datatype datatype;

    /**
     * List of SelectionListItems
     */
    private List items;

    public StaticSelectionList(Datatype datatype) {
        this.datatype = datatype;
        this.items = new ArrayList();
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        Convertor.FormatCache formatCache = new DefaultFormatCache();
        contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);

        Iterator itemIt = this.items.iterator();
        while (itemIt.hasNext()) {
            final SelectionListItem item = (SelectionListItem) itemIt.next();
            item.generateSaxFragment(contentHandler, locale, formatCache);
        }

        contentHandler.endElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
    }

    public List getItems() {
        return this.items;
    }

    /**
     * Adds a new item to this selection list.
     * @param value a value of the correct type (i.e. the type with which this selectionlist is associated)
     * @param label a SAX-fragment such as a {@link org.apache.cocoon.xml.SaxBuffer}, can be null
     */
    public void addItem(Object value, XMLizable label) {
        this.items.add(new SelectionListItem(value, label));
    }

    /**
     * Adds a new item to this selection list.
     * @param value a value of the correct type (i.e. the type with which this selectionlist is associated)
     * @param label a String label, can be null
     */
    public void addItem(Object value, String label) {
        this.items.add(new SelectionListItem(value, label));
    }

    /**
     * Adds a new item to this selection list, where the label is set to the toString()
     * result of the value.
     * @param value a value of the correct type (i.e. the type with which this selectionlist is associated)
     */
    public void addItem(Object value) {
        this.items.add(new SelectionListItem(value));
    }

    public final class SelectionListItem {
        private final Object value;
        private final Object label;

        public SelectionListItem(Object value, XMLizable label) {
            this.value = value;
            this.label = label;
        }

        public SelectionListItem(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        public SelectionListItem(Object value) {
            this.value = value;
            this.label = null;
        }

        public Object getValue() {
            return value;
        }

        public void generateSaxFragment(ContentHandler contentHandler, Locale locale, Convertor.FormatCache formatCache)
        throws SAXException {
            String stringValue;
            if (this.value != null) {
                stringValue = getDatatype().getConvertor().convertToString(this.value, locale, formatCache);
            } else {
                // Null value translates into the empty string
                stringValue = "";
            }

            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("value", stringValue);
            contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, attrs);
            contentHandler.startElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES);
            if (this.label == null) {
                contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            } else if (this.label instanceof XMLizable) {
                ((XMLizable) this.label).toSAX(contentHandler);
            } else {
                String stringLabel = (String) this.label;
                contentHandler.characters(stringLabel.toCharArray(), 0, stringLabel.length());
            }
            contentHandler.endElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
        }
    }
}
