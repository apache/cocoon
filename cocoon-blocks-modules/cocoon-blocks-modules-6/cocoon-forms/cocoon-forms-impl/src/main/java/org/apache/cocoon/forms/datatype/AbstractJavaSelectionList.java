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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.excalibur.xml.sax.XMLizable;

/**
 * Abstract implementation of a JavaSelectionList
 */
public abstract class AbstractJavaSelectionList implements JavaSelectionList {

    protected Datatype datatype;

    private HashMap attributes;
    private List items;
    private boolean nullable;
    private boolean rebuild;

    public AbstractJavaSelectionList() {
        this.items = new ArrayList();
        this.rebuild = true;
    }

    public String getAttribute(String name) {
        if (this.attributes == null) {
            return null;
        }
        return (String) this.attributes.get(name);
    }

    public void removeAttribute(String name) {
        if (this.attributes != null) {
            this.attributes.remove(name);
        }
    }

    public void setAttribute(String name, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        this.attributes.put(name, value);
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public Datatype getDatatype() {
        return this.datatype;
    }

    /**
     * Enforce one rebuild on next usage of #generateSaxFragment(ContentHandler, Locale).
     */
    public void markForRebuild() {
        this.rebuild = true;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale)
    throws SAXException {
        if (this.rebuild) {
            try {
                this.items.clear();
                this.rebuild = build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Convertor.FormatCache formatCache = new DefaultFormatCache();
        contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);

        if (this.nullable) {
            AttributesImpl voidAttrs = new AttributesImpl();
            voidAttrs.addCDATAAttribute("value", "");
            contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, voidAttrs);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
        }

        Iterator itemIt = items.iterator();
        while (itemIt.hasNext()) {
            SelectionListItem item = (SelectionListItem) itemIt.next();
            item.generateSaxFragment(contentHandler, locale, formatCache);
        }

        contentHandler.endElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
    }

    /**
     * Build the list of SelectionListItems using #addItem(Object, String).
     * @return <code>true</code> if the list should be rebuild on each usage,
     *         <code>false</code> if it is static.
     */
    protected abstract boolean build() throws Exception;

    /**
     * Adds a new item to this selection list.
     *
     * @param value
     *            a value of the correct type (i.e. the type with which this
     *            selectionlist is associated)
     * @param label
     *            string label, can be null.
     */
    protected void addItem(Object value, String label) {
        this.items.add(new SelectionListItem(value, label));
    }

    /**
     * Adds a new item to this selection list.
     *
     * @param value
     *            a value of the correct type (i.e. the type with which this
     *            selectionlist is associated)
     * @param label
     *            a SAX-fragment such as a
     *            {@link org.apache.cocoon.xml.SaxBuffer}, can be null
     */
    protected void addItem(Object value, XMLizable label) {
        this.items.add(new SelectionListItem(value, label));
    }

    protected List getItems() {
        return this.items;
    }

    private final class SelectionListItem {
        private final Object value;

        private final Object label;

        public SelectionListItem(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        public SelectionListItem(Object value, XMLizable label) {
            this.value = value;
            this.label = label;
        }

        public Object getValue() {
            return value;
        }

        public void generateSaxFragment(ContentHandler contentHandler,
                                        Locale locale, Convertor.FormatCache formatCache)
        throws SAXException {
            String stringValue;
            if (this.value == null) {
                stringValue = "";
            } else {
                stringValue = datatype.getConvertor().convertToString(this.value, locale, formatCache);
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
