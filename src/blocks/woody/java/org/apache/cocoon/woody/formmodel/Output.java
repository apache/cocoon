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
