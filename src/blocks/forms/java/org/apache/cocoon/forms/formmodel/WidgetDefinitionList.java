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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

// TODO: Refine and i18n the exception messages.
/**
 * Helper class for the Definition implementation of widgets containing
 * other widgets.
 * 
 * @version $Id: WidgetDefinitionList.java,v 1.1 2004/04/21 20:30:49 mpo Exp $
 */
public class WidgetDefinitionList {
    private List widgetDefinitions = new ArrayList();
    private Map widgetDefinitionsById = new HashMap();
    private WidgetDefinition definition;
    private boolean resolving;
    private ListIterator definitionsIt = widgetDefinitions.listIterator();

    /**
     * @param definition the widget definition to which this container delegate belongs
     */
    public WidgetDefinitionList(WidgetDefinition definition) {
        this.definition = definition;
        resolving = false;
    }

    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        String id = widgetDefinition.getId();
        // Do not add NewDefinition id's hash.
        if (!(widgetDefinition instanceof NewDefinition)) {
            if (widgetDefinitionsById.containsKey(id)) {
                String duplicateLocation = widgetDefinition.getLocation();
                String containerLocation = definition.getLocation();
                String firstLocation = getWidgetDefinition(id).getLocation();
                throw new DuplicateIdException(
                    "Duplicate widget id \"" + id + "\" detected at " + duplicateLocation + ".\n" +
                    "Container widget \"" + definition.getId() + "\" at " + containerLocation + "\n" +
                    "already contains a widget with id \"" + id + "\" at " + firstLocation + ".");
            }
            widgetDefinitionsById.put(widgetDefinition.getId(), widgetDefinition);
        }
        this.definitionsIt.add(widgetDefinition);
    }

    public List getWidgetDefinitions() {
        return widgetDefinitions;
    }

    public boolean hasWidget(String id) {
        return widgetDefinitionsById.containsKey(id);
    }

    public WidgetDefinition getWidgetDefinition(String id) {
        return (WidgetDefinition)widgetDefinitionsById.get(id);
    }

    public boolean isResolving() {
        return resolving;
    }

    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        if (!resolving) {
            resolving = true;
            this.definitionsIt = widgetDefinitions.listIterator();
            parents.add(definition);
            while (this.definitionsIt.hasNext()) {
                WidgetDefinition widgetDefinition = (WidgetDefinition)this.definitionsIt.next();
                // ClassDefinition's get resolved by NewDefinition rather than here.
                if (!(widgetDefinition instanceof ClassDefinition)) {
                    if (widgetDefinition instanceof NewDefinition) {
                        // Remove NewDefinition in preparation for its referenced class of widget definitions to be added.
                        this.definitionsIt.remove();
                        ((NewDefinition)widgetDefinition).resolve(parents, definition);
                    } else {
                        if (widgetDefinition instanceof ContainerDefinition)
                            ((ContainerDefinition)widgetDefinition).resolve(parents, definition);
                    }
                }
            }
            parents.remove(parents.size()-1);
            resolving = false;
        } else {
            // Non-terminating recursion detection
            if (resolving == true) {
               // Search up parent list in hopes of finding a "Union" before finding previous "New" for this "Class".
                ListIterator parentsIt = parents.listIterator(parents.size());
                while(parentsIt.hasPrevious()) {
                    WidgetDefinition widgetDefinition = (WidgetDefinition)parentsIt.previous();
                    if (widgetDefinition instanceof UnionDefinition) break;
                    if (widgetDefinition == definition) {
                        String location = definition.getLocation();
                        if (parent instanceof FormDefinition) {
                            throw new Exception("Container: Non-terminating recursion detected in form definition (" + location + ")");
                        } else {
                            throw new Exception("Container: Non-terminating recursion detected in widget definition: "
                                + parent.getId() + " (" + location + ")");
                        }
                    }
                }
            }
        }
    }
 
    public void createWidget(Widget parent, String id) {
        WidgetDefinition widgetDefinition = (WidgetDefinition)widgetDefinitionsById.get(id);
        if (widgetDefinition == null) {
            throw new RuntimeException(definition.getId() + ": WidgetDefinition \"" + id +
                    "\" does not exist (" + definition.getLocation() + ")");
        }
        Widget widget = widgetDefinition.createInstance();
        if (widget != null)
            ((ContainerWidget)parent).addWidget(widget);
    }

    public void createWidgets(Widget parent) {
        Iterator definitionsIt = widgetDefinitions.iterator();
        while (definitionsIt.hasNext()) {
            WidgetDefinition widgetDefinition = (WidgetDefinition)definitionsIt.next();
            Widget widget = widgetDefinition.createInstance();
            if (widget != null)
                ((ContainerWidget)parent).addWidget(widget);
        }
    }
}
