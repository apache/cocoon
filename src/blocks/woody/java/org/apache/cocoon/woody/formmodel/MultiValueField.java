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

import org.apache.cocoon.woody.datatype.SelectionList;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.util.I18nMessage;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * A MultiValueField is mostly the same as a normal {@link Field}, but can
 * hold multiple values. A MultiValueField should have a Datatype which
 * has a SelectionList, because the user will always select the values
 * from a list. A MultiValueField has no concept of "required", you should
 * instead use the ValueCountValidationRule to check how many items the user
 * has selected.
 *
 * <p>A MultiValueField also has a {@link org.apache.cocoon.woody.datatype.Datatype Datatype}
 * associated with it. In case of MultiValueFields, this Datatype will always be an array
 * type, thus {@link org.apache.cocoon.woody.datatype.Datatype#isArrayType Datatype#isArrayType} will
 * always return true, and this in return has an influence on the kind of validation rules that
 * can be used with the Datatype (see {@link org.apache.cocoon.woody.datatype.Datatype Datatype}
 * description for more information).
 */
public class MultiValueField extends AbstractWidget {
    private SelectionList selectionList;
    private MultiValueFieldDefinition definition;
    private String[] enteredValues;
    private Object[] values;
    private ValidationError validationError;

    public MultiValueField(MultiValueFieldDefinition definition) {
        this.definition = definition;
    }

    public String getId() {
        return definition.getId();
    }

    public void readFromRequest(FormContext formContext) {
        enteredValues = formContext.getRequest().getParameterValues(getFullyQualifiedId());
        validationError = null;
        values = null;

        boolean conversionFailed = false;
        if (enteredValues != null) {
            // Normally, for MultiValueFields, the user selects the values from
            // a SelectionList, and the values in a SelectionList are garanteed to
            // be valid, so the conversion from String to native datatype should
            // never fail. But it could fail if users start messing around with
            // request parameters.
            Object[] tempValues = new Object[enteredValues.length];
            for (int i = 0; i < enteredValues.length; i++) {
                String param = enteredValues[i];
                tempValues[i] = definition.getDatatype().convertFromString(param, formContext.getLocale());
                if (tempValues[i] == null) {
                    conversionFailed = true;
                    break;
                }
            }

            if (!conversionFailed)
                values = tempValues;
            else
                values = null;
        } else {
            values = new Object[0];
        }
    }

    public boolean validate(FormContext formContext) {
        if (values != null)
            validationError = definition.getDatatype().validate(values, new ExpressionContextImpl(this));
        else
            validationError = new ValidationError(new I18nMessage("multivaluefield.conversionfailed", Constants.I18N_CATALOGUE));

        return validationError == null;
    }

    private static final String MULTIVALUEFIELD_EL = "multivaluefield";
    private static final String VALUES_EL = "values";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, MULTIVALUEFIELD_EL, Constants.WI_PREFIX_COLON + MULTIVALUEFIELD_EL, attrs);

        contentHandler.startElement(Constants.WI_NS, VALUES_EL, Constants.WI_PREFIX_COLON + VALUES_EL, Constants.EMPTY_ATTRS);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
                String value = definition.getDatatype().getPlainConvertor().convertToString(values[i], locale, null);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
            }
        } else if (enteredValues != null) {
            for (int i = 0; i < enteredValues.length; i++) {
                contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
                String value = definition.getDatatype().getPlainConvertor().convertToString(enteredValues[i], locale, null);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
            }
        }
        contentHandler.endElement(Constants.WI_NS, VALUES_EL, Constants.WI_PREFIX_COLON + VALUES_EL);

        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);

        // the selection list (a MultiValueField has per definition always a SelectionList)
        if (this.selectionList != null) {
            this.selectionList.generateSaxFragment(contentHandler, locale);
        } else {
            definition.getSelectionList().generateSaxFragment(contentHandler, locale);
        }

        // validation message element
        if (validationError != null) {
            contentHandler.startElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL, Constants.EMPTY_ATTRS);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        contentHandler.endElement(Constants.WI_NS, MULTIVALUEFIELD_EL, Constants.WI_PREFIX_COLON + MULTIVALUEFIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }

    public Object getValue() {
        return values;
    }
    
    public void setValue(Object value) {
        if (value == null) {
            setValues(new Object[0]);
        }
        else if (value.getClass().isArray()) {
            setValues((Object[])values);
        } else {
            throw new RuntimeException("Cannot set value of field \"" + getFullyQualifiedId() + "\" with an object of type " + value.getClass().getName());
        }
    }

    public void setValues(Object[] values) {
        // check that all the objects in the array correspond to the datatype
        for (int i = 0; i < values.length; i++) {
            if (!definition.getDatatype().getTypeClass().isAssignableFrom(values[i].getClass()))
                throw new RuntimeException("Cannot set value of field \"" + getFullyQualifiedId() + "\" with an object of type " + values[i].getClass().getName());
        }
        this.values = values;
    }
    
    /**
     * Set this field's selection list.
     * @param selectionList The new selection list.
     */
    public void setSelectionList(SelectionList selectionList) {
        if (selectionList != null &&
            selectionList.getDatatype() != null &&
            selectionList.getDatatype() != definition.getDatatype()) {

            throw new RuntimeException("Tried to assign a SelectionList that is not associated with this widget's datatype.");
        }
        this.selectionList = selectionList;
    }
    
    /**
     * Read this field's selection list from an external source.
     * All Cocoon-supported protocols can be used. 
     * The format of the XML produced by the source should be the 
     * same as in case of inline specification of the selection list,
     * thus the root element should be a <code>wd:selection-list</code>
     * element.
     * @param uri The URI of the source. 
     */
    public void setSelectionList(String uri) {
        setSelectionList(this.definition.buildSelectionList(uri));
    }
    
    /**
     * Set this field's selection list using values from an in-memory
     * object. The <code>object</code> parameter should point to a collection
     * (Java collection or array, or Javascript array) of objects. Each object
     * belonging to the collection should have a <em>value</em> property and a
     * <em>label</em> property, whose values are used to specify the <code>value</code>
     * attribute and the contents of the <code>wd:label</code> child element
     * of every <code>wd:item</code> in the list.
     * <p>Access to the values of the above mentioned properties is done
     * via <a href="http://jakarta.apache.org/commons/jxpath/users-guide.html">XPath</a> expressions.
     * @param model The collection used as a model for the selection list. 
     * @param valuePath An XPath expression referring to the attribute used
     * to populate the values of the list's items. 
     * @param labelPath An XPath expression referring to the attribute used
     * to populate the labels of the list's items.
     */
    public void setSelectionList(Object model, String valuePath, String labelPath) {
        setSelectionList(this.definition.buildSelectionListFromModel(model, valuePath, labelPath));
    }

    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireValueChangedEvent((ValueChangedEvent)event);
    }
}
