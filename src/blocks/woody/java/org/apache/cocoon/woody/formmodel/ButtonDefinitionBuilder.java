package org.apache.cocoon.woody.formmodel;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;

/**
 * The ButtonDefinitionBuilder has been replaced by {@link ActionDefinitionBuilder}. This implementation
 * is only left here to give a warning to users.
 */
public class ButtonDefinitionBuilder implements WidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        throw new Exception("The button widget has been renamed to action. Please update your form definition files. Found at " + DomHelper.getLocation(widgetElement));
    }
}
