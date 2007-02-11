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

import java.util.Collection;
import java.util.List;

/**
 * Interface to be implemented by WidgetDefinitions for Widgets which contain other widgets.
 *
 * @author Timothy Larson
 * @version $Id: ContainerDefinition.java,v 1.4 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public interface ContainerDefinition extends WidgetDefinition {

    /**
     * Resolve references to widget definition classes
     */
    public void resolve(List parents, WidgetDefinition parent) throws Exception;

    /**
     * Create a widget from a contained widget definition.
     */
    public void createWidget(Widget parent, String id);

    /**
     * Create widgets from the contained widget definitions.
     */
    public void createWidgets(Widget parent);

    /**
     * Adds a (sub) widget definition to this definition.
     */
    public void addWidgetDefinition(WidgetDefinition definition) throws Exception, DuplicateIdException;

    /**
     * Check if this definition contains the named definition.
     */
    public boolean hasWidget(String id);

    /**
     * Gets a (sub) widget definition from this definition.
     */
    public WidgetDefinition getWidgetDefinition(String id);

    /**
     * Gets the collection of (sub) widget definition from this definition.
     */
    public Collection getWidgetDefinitions();
}
