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

import java.util.Iterator;
import java.util.Locale;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.excalibur.xml.sax.XMLizable;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A selection list that takes its values from the flow page data.
 *
 * @see org.apache.cocoon.woody.datatype.FlowJXPathSelectionListBuilder
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: FlowJXPathSelectionList.java,v 1.1 2004/03/09 10:34:00 reinhard Exp $
 */
public class FlowJXPathSelectionList implements SelectionList {

    private Context context;
    private String listPath;
    private String valuePath;
    private String labelPath;
    private Datatype datatype;
    private Object model;

    public FlowJXPathSelectionList(Context context, String listPath, String valuePath, String labelPath, Datatype datatype) {
        this.context = context;
        this.listPath = listPath;
        this.valuePath = valuePath;
        this.labelPath = labelPath;
        this.datatype = datatype;
    }

    /**
     * Builds a dynamic selection list from an in-memory collection.
     * @see org.apache.cocoon.woody.formmodel.Field#setSelectionList(Object model, String valuePath, String labelPath)
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
            Object flowData = FlowHelper.getContextObject(ContextHelper.getObjectModel(this.context));
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
        contentHandler.startElement(Constants.FI_NS, SELECTION_LIST_EL, Constants.FI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);

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
                itemCtx.setLenient(true);
                label = itemCtx.getValue(this.labelPath);
                if (label == null) {
                    label = stringValue;
                }
            }

            // Output this item
            AttributesImpl itemAttrs = new AttributesImpl();
            itemAttrs.addCDATAAttribute("value", stringValue);
            contentHandler.startElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL, itemAttrs);
            if (label != null) {
                contentHandler.startElement(Constants.FI_NS, LABEL_EL, Constants.FI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
                if (label instanceof XMLizable) {
                    ((XMLizable)label).toSAX(contentHandler);
                } else {
                    String stringLabel = label.toString();
                    contentHandler.characters(stringLabel.toCharArray(), 0, stringLabel.length());
                }
                contentHandler.endElement(Constants.FI_NS, LABEL_EL, Constants.FI_PREFIX_COLON + LABEL_EL);
            }
            contentHandler.endElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL);
        }

        // End the selection-list
        contentHandler.endElement(Constants.FI_NS, SELECTION_LIST_EL, Constants.FI_PREFIX_COLON + SELECTION_LIST_EL);
    }
}
