package org.apache.cocoon.woody.formmodel;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.Datatype;

/**
 * Builds {@link OutputDefinition}s.
 */
public class OutputDefinitionBuilder extends AbstractDatatypeWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        OutputDefinition definition = new OutputDefinition();
        setLocation(widgetElement, definition);
        setId(widgetElement, definition);

        Element datatypeElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "datatype");
        if (datatypeElement == null)
            throw new Exception("A nested datatype element is required for the widget specified at " + DomHelper.getLocation(widgetElement));

        Datatype datatype = datatypeManager.createDatatype(datatypeElement, false);
        definition.setDatatype(datatype);

        setDisplayData(widgetElement, definition);

        return definition;
    }
}
