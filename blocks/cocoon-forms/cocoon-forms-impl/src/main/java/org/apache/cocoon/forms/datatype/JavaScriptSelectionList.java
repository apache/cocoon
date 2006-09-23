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
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.LocatedRuntimeException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A {@link FilterableSelectionList} based on a JavaScript snippet.
 * 
 * @since 2.1.9
 * @version $Id$
 */
public class JavaScriptSelectionList implements FilterableSelectionList, Locatable {

    private Context context;
    private Datatype type;
    private Function function;
    private Location location = null;
    private boolean labelIsI18nKey = false;
    private String i18nCatalog = null;
    
    public JavaScriptSelectionList(Context context, Datatype type, Function function, String catalogue, Location location) {
        this.context = context;
        this.type = type;
        this.function = function;
        this.location = location;
        if (catalogue != null) {
            this.labelIsI18nKey = true;
            if (catalogue.length() > 0) {
                this.i18nCatalog = catalogue;
            }
        }
    }
    
    public Location getLocation() {
        return this.location;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        generateSaxFragment(contentHandler, locale, null);
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale, String filter) throws SAXException {
        Map objectModel = ContextHelper.getObjectModel(this.context);
        Object result;
        try {
            result = JavaScriptHelper.callFunction(function, null /*this*/, new Object[] {filter}, objectModel);
        } catch (JavaScriptException e) {
            throw new LocatedRuntimeException("Error building JS selection list", e, getLocation());
        }
        
        // Start the selection-list
        contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);

        NativeArray array = (NativeArray)result;
        for (int i = 0; i < array.jsGet_length(); i++) {
            Scriptable item = (Scriptable)array.get(i, array);
            
            Object value = item.get("value", item);
            String stringValue = value == null ? "" : this.type.convertToString(value, locale);
            Object label = item.get("label", item);
            if (label == Scriptable.NOT_FOUND)
                label = null;
            
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

    public Datatype getDatatype() {
        return this.type;
    }
}
