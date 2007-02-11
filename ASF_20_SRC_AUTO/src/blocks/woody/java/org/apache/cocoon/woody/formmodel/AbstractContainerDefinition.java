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
 * The {@link AbstractContainerDefinition} corresponding to an {@link AbstractContainerWidget}.
 *
 * @author Timothy Larson
 * @version $Id: AbstractContainerDefinition.java,v 1.4 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public abstract class AbstractContainerDefinition
        extends AbstractWidgetDefinition implements ContainerDefinition {
    protected ContainerDefinitionDelegate definitions;

    public AbstractContainerDefinition() {
        definitions = new ContainerDefinitionDelegate(this);
    }

    public void createWidget(Widget parent, String id) {
        definitions.createWidget(parent, id);
    }

    public void createWidgets(Widget parent) {
        definitions.createWidgets(parent);
    }

    public void addWidgetDefinition(WidgetDefinition definition) throws Exception, DuplicateIdException {
        definition.setParent(this);
        definitions.addWidgetDefinition(definition);
    }

    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        definitions.resolve(parents, parent);
    }

    public boolean hasWidget(String id) {
        return definitions.hasWidget(id);
    }

    public WidgetDefinition getWidgetDefinition(String id) {
        return definitions.getWidgetDefinition(id);
    }

    public Collection getWidgetDefinitions() {
        return definitions.getWidgetDefinitions();
    }
}
