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

import java.util.Iterator;
import java.util.Locale;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.woody.Constants;
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
 * @version CVS $Id: FlowJXPathSelectionList.java,v 1.5 2004/01/29 03:18:05 vgritsenko Exp $
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
        contentHandler.startElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);

        while(iter.hasNext()) {
            String stringValue = "";
            Object label = null;

            // Get a context on the current item
            Pointer ptr = (Pointer)iter.next();
            if (ptr.getValue() != null) {
                JXPathContext itemCtx = ctx.getRelativeContext(ptr);

                // Get the value as a string
                Object value = itemCtx.getValue(this.valuePath);
                stringValue = this.datatype.convertToString(value, locale);

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
            contentHandler.startElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL, itemAttrs);
            if (label != null) {
                contentHandler.startElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
                if (label instanceof XMLizable) {
                    ((XMLizable)label).toSAX(contentHandler);
                } else {
                    String stringLabel = label.toString();
                    contentHandler.characters(stringLabel.toCharArray(), 0, stringLabel.length());
                }
                contentHandler.endElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL);
            }
            contentHandler.endElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL);
        }

        // End the selection-list
        contentHandler.endElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL);
    }
}
