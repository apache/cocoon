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
 */
public class ContainerDefinitionDelegate {
    private List widgetDefinitions = new ArrayList();
    private Map widgetDefinitionsById = new HashMap();
    private WidgetDefinition definition;
    private boolean resolving;
    private ListIterator definitionsIt = widgetDefinitions.listIterator();

    /**
     * @param definition the widget definition to which this container delegate belongs
     */
    public ContainerDefinitionDelegate(WidgetDefinition definition) {
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
