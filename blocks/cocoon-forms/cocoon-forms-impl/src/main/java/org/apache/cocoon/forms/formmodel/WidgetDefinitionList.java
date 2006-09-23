/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.FormsRuntimeException;
import org.apache.cocoon.util.location.Location;

// TODO: Refine and i18n the exception messages.
/**
 * Helper class for the Definition implementation of widgets containing
 * other widgets.
 *
 * @version $Id$
 */
public class WidgetDefinitionList {

    private List widgetDefinitions = new ArrayList();
    private Map widgetDefinitionsById = new HashMap();
    private WidgetDefinition containerDefinition;
    private boolean wasHere;
    private ListIterator definitionsIt = widgetDefinitions.listIterator();

    /**
     * @param definition the widget definition to which this container delegate belongs
     */
    public WidgetDefinitionList(WidgetDefinition definition) {
        this.containerDefinition = definition;
        wasHere = false;
    }

    public int size() {
        return widgetDefinitions.size();
    }

    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        String id = widgetDefinition.getId();
        // Do not add NewDefinition id's hash.
        if (!(widgetDefinition instanceof NewDefinition)) {
            if (widgetDefinitionsById.containsKey(id)) {
                Location containerLocation = containerDefinition.getLocation();
                Location firstLocation = getWidgetDefinition(id).getLocation();
                throw new DuplicateIdException(
                    "Detected duplicate widget id '" + id + "'.\n" +
                    "Container widget '" + containerDefinition.getId() + "' at " + containerLocation + "\n" +
                    "already contains a widget with the same id at " + firstLocation + ".",
                        widgetDefinition);
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
        return (WidgetDefinition) widgetDefinitionsById.get(id);
    }

    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        if (!wasHere) {
            wasHere = true;
            this.definitionsIt = widgetDefinitions.listIterator();
            parents.add(containerDefinition);
            while (this.definitionsIt.hasNext()) {
                WidgetDefinition widgetDefinition = (WidgetDefinition) this.definitionsIt.next();
                // ClassDefinition's get resolved by NewDefinition rather than here.
                if (!(widgetDefinition instanceof ClassDefinition)) {
                    if (widgetDefinition instanceof NewDefinition) {
                        // Remove NewDefinition in preparation for its referenced class of widget definitions to be added.
                        this.definitionsIt.remove();
                        ((NewDefinition) widgetDefinition).resolve(parents, containerDefinition);
                    } else {
                        if (widgetDefinition instanceof ContainerDefinition) {
                            ((ContainerDefinition) widgetDefinition).resolve(parents, containerDefinition);
                        }
                    }
                }
            }
            parents.remove(parents.size()-1);
            wasHere = false;
        } else {
            // Non-terminating recursion detection
            // Search up parent list in hopes of finding a "Union" or "Repeater" before finding previous "New" for this "Class".
            ListIterator parentsIt = parents.listIterator(parents.size());
            while(parentsIt.hasPrevious()) {
                WidgetDefinition widgetDefinition = (WidgetDefinition)parentsIt.previous();
                if (widgetDefinition instanceof UnionDefinition) {
                    break;
                }
                if (widgetDefinition instanceof RepeaterDefinition) {
                    break;
                }
                if (widgetDefinition == containerDefinition) {
                    Location location = containerDefinition.getLocation();
                    if (parent instanceof FormDefinition) {
                        throw new FormsException("Container: Non-terminating recursion detected in form definition.",
                                                 location);
                    }

                    throw new FormsException("Container: Non-terminating recursion detected in widget definition: " + parent.getId(),
                                             location);
                }
            }
        }
    }

    public void createWidget(Widget parent, String id) {
        WidgetDefinition widgetDefinition = (WidgetDefinition) widgetDefinitionsById.get(id);
        if (widgetDefinition == null) {
            throw new FormsRuntimeException(containerDefinition.getId() + ": WidgetDefinition '" + id + "' does not exist.",
                                            containerDefinition.getLocation());
        }

        Widget widget = widgetDefinition.createInstance();
        if (widget != null) {
            ((ContainerWidget) parent).addChild(widget);
        }
    }

    public void createWidgets(Widget parent) {
        Iterator i = widgetDefinitions.iterator();
        while (i.hasNext()) {
            WidgetDefinition widgetDefinition = (WidgetDefinition) i.next();
            Widget widget = widgetDefinition.createInstance();
            if (widget != null) {
                ((ContainerWidget) parent).addChild(widget);
            }
        }
    }

    public void checkCompleteness() throws IncompletenessException {
        if (!wasHere) {
            wasHere = true;
            // FIXME: is it legal to have no widgets in a container? There are some cases of this in Swan
            // if(size() == 0)
            //     throw new IncompletenessException(this.containerDefinition.getClass().getName() +
            //                                       " requires at least one child widget!", this.containerDefinition);

            // now check children's completeness
            Iterator i = widgetDefinitions.iterator();
            while (i.hasNext()) {
                ((WidgetDefinition) i.next()).checkCompleteness();
            }
            wasHere = false;
        }
    }
}
