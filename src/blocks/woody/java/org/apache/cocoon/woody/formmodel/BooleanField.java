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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.xml.AttributesImpl;
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
 */
public class BooleanField extends AbstractWidget {
    // FIXME(SW) : should the initial value be false or null ? This would allow
    // event listeners to be triggered at bind time.
    private Boolean value = Boolean.FALSE;
    private BooleanFieldDefinition definition;

    public BooleanField(BooleanFieldDefinition definition) {
        this.definition = definition;
        setLocation(definition.getLocation());
    }

    public String getId() {
        return definition.getId();
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

    public boolean validate(FormContext formContext) {
        // a boolean field is always valid
        return true;
    }

    private static final String BOOLEAN_FIELD_EL = "booleanfield";
    private static final String VALUE_EL = "value";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl fieldAttrs = new AttributesImpl();
        fieldAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, BOOLEAN_FIELD_EL, Constants.WI_PREFIX_COLON + BOOLEAN_FIELD_EL, fieldAttrs);

        // value element
        contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
        String stringValue = String.valueOf(value != null && value.booleanValue() == true? "true": "false");
        contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
        contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);

        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);

        contentHandler.endElement(Constants.WI_NS, BOOLEAN_FIELD_EL, Constants.WI_PREFIX_COLON + BOOLEAN_FIELD_EL);
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
