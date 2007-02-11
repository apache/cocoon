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
package org.apache.cocoon.forms.datatype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract implementation of a JavaSelectionList
 */
public abstract class AbstractJavaSelectionList implements JavaSelectionList,
        Serviceable {

    protected Datatype datatype;
    protected ServiceManager manager;

    private HashMap attributes;
    private List items = new ArrayList();
    private boolean nullable;
    private boolean rebuild = true;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#getAttribute(java.lang.String)
     */
    public String getAttribute(String name) {
        if (this.attributes == null) {
            return null;
        }
        return (String) this.attributes.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        if (this.attributes != null) {
            this.attributes.remove(name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#setAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void setAttribute(String name, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        this.attributes.put(name, value);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#isNullable()
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#setDatatype(org.apache.cocoon.forms.datatype.Datatype)
     */
    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.JavaSelectionList#setNullable(boolean)
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.forms.datatype.SelectionList#getDatatype()
     */
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
        if (this.rebuild)
            try {
                this.items.clear();
                this.rebuild = this.build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        Convertor.FormatCache formatCache = new DefaultFormatCache();
        contentHandler.startElement(Constants.INSTANCE_NS, SELECTION_LIST_EL,
                Constants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL,
                XMLUtils.EMPTY_ATTRIBUTES);
        if (nullable) {
            AttributesImpl voidAttrs = new AttributesImpl();
            voidAttrs.addCDATAAttribute("value", "");
            contentHandler.startElement(Constants.INSTANCE_NS, ITEM_EL,
                    Constants.INSTANCE_PREFIX_COLON + ITEM_EL, voidAttrs);
            contentHandler.endElement(Constants.INSTANCE_NS, ITEM_EL,
                    Constants.INSTANCE_PREFIX_COLON + ITEM_EL);
        }
        Iterator itemIt = items.iterator();
        while (itemIt.hasNext()) {
            SelectionListItem item = (SelectionListItem) itemIt.next();
            item.generateSaxFragment(contentHandler, locale, formatCache);
        }
        contentHandler.endElement(Constants.INSTANCE_NS, SELECTION_LIST_EL,
                Constants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
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
     *            a SAX-fragment such as a
     *            {@link org.apache.cocoon.xml.SaxBuffer}, can be null
     */
    protected void addItem(Object value, String label) {
        items.add(new SelectionListItem(value, label));
    }

    protected List getItems() {
        return items;
    }

    private final class SelectionListItem {
        private final Object value;

        private final String label;

        public SelectionListItem(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        public Object getValue() {
            return value;
        }

        public void generateSaxFragment(ContentHandler contentHandler,
                Locale locale, Convertor.FormatCache formatCache)
                throws SAXException {
            AttributesImpl itemAttrs = new AttributesImpl();
            String stringValue;
            if (this.value == null) {
                stringValue = "";
            } else {
                stringValue = datatype.getConvertor().convertToString(value,
                        locale, formatCache);
            }
            itemAttrs.addCDATAAttribute("value", stringValue);
            contentHandler.startElement(Constants.INSTANCE_NS, ITEM_EL,
                    Constants.INSTANCE_PREFIX_COLON + ITEM_EL, itemAttrs);
            contentHandler.startElement(Constants.INSTANCE_NS, LABEL_EL,
                    Constants.INSTANCE_PREFIX_COLON + LABEL_EL,
                    XMLUtils.EMPTY_ATTRIBUTES);
            if (label == null) {
                contentHandler.characters(stringValue.toCharArray(), 0,
                        stringValue.length());
            } else {
                contentHandler.characters(label.toCharArray(), 0, label
                        .length());
            }
            contentHandler.endElement(Constants.INSTANCE_NS, LABEL_EL,
                    Constants.INSTANCE_PREFIX_COLON + LABEL_EL);
            contentHandler.endElement(Constants.INSTANCE_NS, ITEM_EL,
                    Constants.INSTANCE_PREFIX_COLON + ITEM_EL);
        }
    }

}
