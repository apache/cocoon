package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

public class RepeaterFilterFieldDefinitionBuilder extends
		FieldDefinitionBuilder {

	public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        RepeaterFilterFieldDefinition definition = new RepeaterFilterFieldDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
	}

	protected void setupDefinition(Element widgetElement,RepeaterFilterFieldDefinition definition) throws Exception {
		super.setupDefinition(widgetElement, definition);
		definition.setRepeaterName(DomHelper.getAttribute(widgetElement, "repeater"));
		definition.setField(DomHelper.getAttribute(widgetElement, "field"));
	}

}
