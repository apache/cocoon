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

import java.util.Iterator;

import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.formmodel.library.LibraryManager;
import org.apache.cocoon.util.location.LocationAttributes;
import org.w3c.dom.Element;

/**
 * Builds {@link FormDefinition}s.
 *
 * @version $Id$
 */
public final class FormDefinitionBuilder extends AbstractContainerDefinitionBuilder {

    protected LibraryManager libraryManager;

    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        throw new UnsupportedOperationException("Please use the other signature without WidgetDefinitionBuilderContext!");
    }

    public WidgetDefinition buildWidgetDefinition(Element formElement) throws Exception {
        FormDefinition formDefinition = new FormDefinition(libraryManager);
        WidgetDefinitionBuilderContext context = new WidgetDefinitionBuilderContext(formDefinition.getLocalLibrary());

        // set local URI
        formDefinition.getLocalLibrary().setSourceURI(LocationAttributes.getURI(formElement));

        Iterator i = buildEventListeners(formElement, "on-processing-phase", ProcessingPhaseListener.class).iterator();
        while (i.hasNext()) {
            formDefinition.addProcessingPhaseListener((ProcessingPhaseListener) i.next());
        }

        setupDefinition(formElement, formDefinition, context);
        setDisplayData(formElement, formDefinition);
        setupContainer(formElement, "widgets", formDefinition, context);

        formDefinition.resolve();
        formDefinition.makeImmutable();

        return formDefinition;
    }
}
