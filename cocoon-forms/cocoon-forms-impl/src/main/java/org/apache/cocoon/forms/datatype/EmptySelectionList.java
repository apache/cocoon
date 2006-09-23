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

import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * 
 * @version $Id$
 */
public class EmptySelectionList implements SelectionList {
    private String text;
    private boolean i18n;
    
    public EmptySelectionList(String text) {
        this.text = text;
        this.i18n = false;
    }
    
    public EmptySelectionList(String text, boolean i18n) {
        this.text = text;
        this.i18n = i18n;
    }

    public Datatype getDatatype() {
        // Cannot return anything meaningful
        return null;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Start wi:selection list
        contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);

        // Start wi:item
        AttributesImpl itemAttrs = new AttributesImpl();
        itemAttrs.addCDATAAttribute("value", "");
        contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, itemAttrs);

        // Start wi:label
        contentHandler.startElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES);
        if (this.text != null) {

            if (i18n) {
                contentHandler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);
        
                contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, XMLUtils.EMPTY_ATTRIBUTES);
                contentHandler.characters(this.text.toCharArray(), 0, this.text.length());
                contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);
    
                contentHandler.endPrefixMapping("i18n");
            } else {
                contentHandler.characters(this.text.toCharArray(), 0, this.text.length());
            }
        }
        
        // End wi:label
        contentHandler.endElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL);
        
        // End wi:item
        contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
        
        // End wi:selection-list
        contentHandler.endElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
    }
}
