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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * An Output widget can be used to show a non-editable value to the user.
 * An Output widget is associated with a certain
 * {@link org.apache.cocoon.woody.datatype.Datatype Datatype}.
 *
 * <p>An Output widget is always valid and never required.
 * 
 * @version $Id: Output.java,v 1.9 2004/03/05 13:02:32 bdelacretaz Exp $
 */
public class Output extends AbstractWidget implements DataWidget {
    private OutputDefinition definition;
    private Object value;

    public OutputDefinition getOutputDefinition() {
        return definition;
    }

    public Datatype getDatatype() {
        return definition.getDatatype();
    }

    protected Output(OutputDefinition definition) {
        this.definition = definition;
        setLocation(definition.getLocation());
    }

    public String getId() {
        return definition.getId();
    }

    public void readFromRequest(FormContext formContext) {
        // do nothing
    }

    public boolean validate(FormContext formContext) {
        return true;
    }

    private static final String OUTPUT_EL = "output";
    private static final String VALUE_EL = "value";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl outputAttrs = new AttributesImpl();
        outputAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, OUTPUT_EL, Constants.WI_PREFIX_COLON + OUTPUT_EL, outputAttrs);

        // the value
        if (value != null) {
            contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
            String stringValue;
            stringValue = definition.getDatatype().convertToString(value, locale);
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
        }

        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);

        contentHandler.endElement(Constants.WI_NS, OUTPUT_EL, Constants.WI_PREFIX_COLON + OUTPUT_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
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
