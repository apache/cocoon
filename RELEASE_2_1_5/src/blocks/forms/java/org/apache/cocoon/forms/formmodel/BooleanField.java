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

import java.util.Locale;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.*;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
 * @version $Id: BooleanField.java,v 1.10 2004/05/07 13:42:10 mpo Exp $
 */
public class BooleanField extends AbstractWidget implements ValueChangedListenerEnabled {
    // FIXME(SW) : should the initial value be false or null ? This would allow
    // event listeners to be triggered at bind time.
    private Boolean value = Boolean.FALSE;
    private final BooleanFieldDefinition definition;
    /** Additional listeners to those defined as part of the widget definition (if any). */
    private ValueChangedListener listener;

    public BooleanField(BooleanFieldDefinition definition) {
        this.definition = definition;
    }

    protected WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void readFromRequest(FormContext formContext) {
        Object oldValue = value;
        String param = formContext.getRequest().getParameter(getRequestParameterName());
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
     * TODO is there a use case for boolean fields having validators?
     */
    public boolean validate() {
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

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // value element
        contentHandler.startElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
        String stringValue = String.valueOf(value != null && value.booleanValue() == true? "true": "false");
        contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
        contentHandler.endElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL);
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
            throw new RuntimeException("Cannot set value of boolean field \"" + getRequestParameterName() + "\" to a non-Boolean value.");
        }
        
        Object oldValue = value;
        value = (Boolean)object;
        if (value != oldValue) {
            getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
        }
    }
    
    /**
     * Adds a ValueChangedListener to this widget instance. Listeners defined
     * on the widget instance will be executed in addtion to any listeners
     * that might have been defined in the widget definition.
     */
    public void addValueChangedListener(ValueChangedListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeValueChangedListener(ValueChangedListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    private void fireValueChangedEvent(ValueChangedEvent event) {
        if (this.listener != null) {
            this.listener.valueChanged(event);
        }
    }

    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireValueChangedEvent((ValueChangedEvent)event);
        fireValueChangedEvent((ValueChangedEvent)event);
    }
}
