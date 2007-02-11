package org.apache.cocoon.woody.formmodel;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Helper class for the Definition implementation of widgets containing
 * other widgets.
 */
public class ContainerDefinitionDelegate {
    private List widgetDefinitions = new ArrayList();
    private Map widgetDefinitionsById = new HashMap();
    private WidgetDefinition definition;

    /**
     * @param definition the widget definition to which this container delegate belongs
     */
    public ContainerDefinitionDelegate(WidgetDefinition definition) {
        this.definition = definition;
    }

    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        if (widgetDefinitionsById.containsKey(widgetDefinition.getId()))
            throw new DuplicateIdException("Duplicate widget id detected: widget " + definition.getId() + " already contains a widget with id \"" + widgetDefinition.getId() + "\"");
        widgetDefinitions.add(widgetDefinition);
        widgetDefinitionsById.put(widgetDefinition.getId(), widgetDefinition);
    }

    public List getWidgetDefinitions() {
        return widgetDefinitions;
    }

    public boolean hasWidget(String id) {
        return widgetDefinitionsById.containsKey(id);
    }
}
