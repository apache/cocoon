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
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * An Output widget can be used to show a non-editable value to the user.
 * An Output widget is associated with a certain
 * {@link org.apache.cocoon.forms.datatype.Datatype Datatype}.
 *
 * <p>An Output widget is always valid and never required.
 * 
 * @version $Id: Output.java,v 1.7 2004/04/22 14:26:48 mpo Exp $
 */
public class Output extends AbstractWidget implements DataWidget {
    
    private final OutputDefinition definition;
    private Object value;

    public OutputDefinition getOutputDefinition() {
        return definition;
    }

    public Datatype getDatatype() {
        return definition.getDatatype();
    }

    protected Output(OutputDefinition definition) {
        this.definition = definition;
    }

    protected WidgetDefinition getDefinition() {
        return definition;
    }

    public void readFromRequest(FormContext formContext) {
        // do nothing
    }

    public boolean validate(FormContext formContext) {
        return true;
    }

    private static final String OUTPUT_EL = "output";
    private static final String VALUE_EL = "value";


    /**
     * @return "output"
     */
    public String getXMLElementName() {
        return OUTPUT_EL;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // the value
        if (value != null) {
            contentHandler.startElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            String stringValue;
            stringValue = definition.getDatatype().convertToString(value, locale);
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object object) {
        if (object != null && !definition.getDatatype().getTypeClass().isAssignableFrom(object.getClass())) {
            throw new RuntimeException("Tried to set value of output widget \""
                                       + getFullyQualifiedId()
                                       + "\" with an object of an incorrect type: "
                                       + "expected " + definition.getDatatype().getTypeClass()
                                       + ", received " + object.getClass() + ".");
        }
        value = object;
    }
}
