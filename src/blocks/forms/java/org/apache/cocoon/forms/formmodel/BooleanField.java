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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * A widget to select a boolean value. Usually rendered as a checkbox.
 *
 * <p>You may wonder why we don't use a {@link Field} widget with an associated
 * Boolean Datatype instead. The reason is that many of the features of the Field
 * widget are overkill for a Boolean: validation is unnecessary (if the field is
 * not true it is false), the selectionlist associated with a Datatype also
 * has no purpose here (there would always be only 2 choices: true or false),
 * and the manner in which the request parameter of this widget is interpreted
 * is different (missing or empty request parameter means 'false', rather than null value).
 * 
 * @version $Id: BooleanField.java,v 1.5 2004/04/20 22:19:27 mpo Exp $
 */
public class BooleanField extends AbstractWidget {
    // FIXME(SW) : should the initial value be false or null ? This would allow
    // event listeners to be triggered at bind time.
    private Boolean value = Boolean.FALSE;
    private final BooleanFieldDefinition definition;

    public BooleanField(BooleanFieldDefinition definition) {
        this.definition = definition;
    }

    protected WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void readFromRequest(FormContext formContext) {
        Object oldValue = value;
        String param = formContext.getRequest().getParameter(getFullyQualifiedId());
        if (param != null && param.equalsIgnoreCase("true"))
            value = Boolean.TRUE;
        else
            value = Boolean.FALSE;
        
        if (value != oldValue) {
            getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
        }
    }

    /**
     * Always return <code>true</code> (an action has no validation)
     * 
     * @todo is there a use case for boolean fields having validators?
     */
    public boolean validate(FormContext formContext) {
        // a boolean field is always valid
        return true;
    }


    private static final String BOOLEAN_FIELD_EL = "booleanfield";
    private static final String VALUE_EL = "value";
    
    /**
     * @return "booleanfield"
     */
    public String getXMLElementName() {
        return BOOLEAN_FIELD_EL;
    }

    //TODO: reuse available implementation on superclass
    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl fieldAttrs = new AttributesImpl();
        fieldAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.INSTANCE_NS, BOOLEAN_FIELD_EL, Constants.INSTANCE_PREFIX_COLON + BOOLEAN_FIELD_EL, fieldAttrs);

        // value element
        contentHandler.startElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
        String stringValue = String.valueOf(value != null && value.booleanValue() == true? "true": "false");
        contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
        contentHandler.endElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL);

        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);

        contentHandler.endElement(Constants.INSTANCE_NS, BOOLEAN_FIELD_EL, Constants.INSTANCE_PREFIX_COLON + BOOLEAN_FIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }

    public Object getValue() {
        return value;
    }

    /**
     * Sets value of the field. If value is null, it is considered to be false
     * (see class comment).
     */
    public void setValue(Object object) {
        if (object == null) {
            object = Boolean.FALSE;
        }
        
        if (!(object instanceof Boolean)) {
            throw new RuntimeException("Cannot set value of boolean field \"" + getFullyQualifiedId() + "\" to a non-Boolean value.");
        }
        
        Object oldValue = value;
        value = (Boolean)object;
        if (value != oldValue) {
            getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
        }
    }
    
    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireValueChangedEvent((ValueChangedEvent)event);
    }
}
