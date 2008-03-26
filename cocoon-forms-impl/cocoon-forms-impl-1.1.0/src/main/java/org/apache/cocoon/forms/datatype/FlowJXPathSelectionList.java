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

import java.util.Iterator;
import java.util.Locale;

import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A selection list that takes its values from the flow page data.
 *
 * @see org.apache.cocoon.forms.datatype.FlowJXPathSelectionListBuilder
 * @version $Id$
 */
public class FlowJXPathSelectionList implements SelectionList {

    private ProcessInfoProvider processInfoProvider;
    private String listPath;
    private String valuePath;
    private String labelPath;
    private Datatype datatype;
    private Object model;
    private boolean nullable = false; 
    private String nullText;
    private boolean nullTextIsI18nKey = false;
    private String i18nCatalog;
    private boolean labelIsI18nKey = false;

    public FlowJXPathSelectionList(ProcessInfoProvider processInfoProvider, 
                                   String listPath, 
                                   String valuePath, 
                                   String labelPath, 
                                   Datatype datatype,
                                   String nullText, 
                                   boolean nullTextIsI18nKey, 
                                   String i18nCatalog, 
                                   boolean labelIsI18nKey) {
        this.processInfoProvider = processInfoProvider;
        this.listPath = listPath;
        this.valuePath = valuePath;
        this.labelPath = labelPath;
        this.datatype = datatype;
        this.nullText = nullText;
        this.nullable = (nullText != null);
        this.nullTextIsI18nKey = nullTextIsI18nKey;
        this.i18nCatalog =i18nCatalog;
        this.labelIsI18nKey = labelIsI18nKey;
    }

    /**
     * Builds a dynamic selection list from an in-memory collection.
     * @see org.apache.cocoon.forms.formmodel.Field#setSelectionList(Object model, String valuePath, String labelPath)
     * @param model The collection used as a model for the selection list.
     * @param valuePath An XPath expression referring to the attribute used
     * to populate the values of the list's items.
     * @param labelPath An XPath expression referring to the attribute used
     * to populate the labels of the list's items.
     * @param datatype
     */
    public FlowJXPathSelectionList(Object model, String valuePath, String labelPath, Datatype datatype) {
        this.model = model;
        this.valuePath = valuePath;
        this.labelPath = labelPath;
        this.datatype = datatype;
    }

    public Datatype getDatatype() {
        return this.datatype;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        JXPathContext ctx = null;
        Iterator iter = null;
        if (model == null) {
            Object flowData = FlowHelper.getContextObject(processInfoProvider.getObjectModel());
            if (flowData == null) {
                throw new SAXException("No flow data to produce selection list");
            }

            // Move to the list location
            ctx = JXPathContext.newContext(flowData);

            // Iterate on all elements of the list
            iter = ctx.iteratePointers(this.listPath);
        } else {
            // Move to the list location
            ctx = JXPathContext.newContext(model);

            // Iterate on all elements of the list
            iter = ctx.iteratePointers(".");
        }

        // Start the selection-list
        contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);
        if( this.nullable ) {
            final AttributesImpl voidAttrs = new AttributesImpl(  );
            voidAttrs.addCDATAAttribute( "value", "" );
            contentHandler.startElement( FormsConstants.INSTANCE_NS, ITEM_EL,
                                         FormsConstants.INSTANCE_PREFIX_COLON +
                                         ITEM_EL, voidAttrs );

            if( this.nullText != null ) {
                contentHandler.startElement( FormsConstants.INSTANCE_NS, LABEL_EL,
                                             FormsConstants.INSTANCE_PREFIX_COLON +
                                             LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES );

                if( this.nullTextIsI18nKey ) {
                    if( ( this.i18nCatalog != null ) &&
                        ( this.i18nCatalog.trim(  ).length(  ) > 0 ) ) {
                        new I18nMessage( this.nullText, this.i18nCatalog ).toSAX( contentHandler );
                    } else {
                        new I18nMessage( this.nullText ).toSAX( contentHandler );
                    }
                } else {
                    contentHandler.characters( this.nullText.toCharArray(  ), 0,
                                               this.nullText.length(  ) );
                }

                contentHandler.endElement( FormsConstants.INSTANCE_NS, LABEL_EL,
                                           FormsConstants.INSTANCE_PREFIX_COLON +
                                           LABEL_EL );
            }

            contentHandler.endElement( FormsConstants.INSTANCE_NS, ITEM_EL,
                                       FormsConstants.INSTANCE_PREFIX_COLON +
                                       ITEM_EL );
        }

        while(iter.hasNext()) {
            String stringValue = "";
            Object label = null;

            // Get a context on the current item
            Pointer ptr = (Pointer)iter.next();
            if (ptr.getValue() != null) {
                JXPathContext itemCtx = ctx.getRelativeContext(ptr);

                // Get the value as a string
                Object value = itemCtx.getValue(this.valuePath);

                // List may contain null value, and (per contract with convertors),
                // convertors are not invoked on nulls.
                if (value != null) {
                    stringValue = this.datatype.convertToString(value, locale);
                }

                // Get the label (can be ommitted)
                if (this.labelPath != null) {
                    itemCtx.setLenient(true);
                    label = itemCtx.getValue(this.labelPath);
                }
            }

            // Output this item
            AttributesImpl itemAttrs = new AttributesImpl();
            itemAttrs.addCDATAAttribute("value", stringValue);
            contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, itemAttrs);
            if (label != null) {
                contentHandler.startElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES);
                if (label instanceof XMLizable) {
                    ((XMLizable)label).toSAX(contentHandler);
                }  else if( this.labelIsI18nKey ) {
                    String stringLabel = label.toString();

                    if( ( this.i18nCatalog != null ) &&
                        ( this.i18nCatalog.trim(  ).length(  ) > 0 ) ) {
                        new I18nMessage( stringLabel, this.i18nCatalog ).toSAX( contentHandler );
                    } else {
                        new I18nMessage( stringLabel ).toSAX( contentHandler );
                    }
                } else {
                    String stringLabel = label.toString();
                    contentHandler.characters(stringLabel.toCharArray(), 0, stringLabel.length());
                }
                contentHandler.endElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL);
            }
            contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
        }

        // End the selection-list
        contentHandler.endElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
    }
}
