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

import org.apache.cocoon.forms.formmodel.library.Library;

/**
 * Holds context information for the building phase.
 *
 * @version $Id$
 */
public class WidgetDefinitionBuilderContext {

    private final Library library;
    private WidgetDefinition superDefinition;


    public WidgetDefinitionBuilderContext(Library library) {
        this.library = library;
    }

    /**
     * Create context which inherits library from another context.
     *
     * @param other context to borrow library from
     */
    public WidgetDefinitionBuilderContext(WidgetDefinitionBuilderContext other) {
        this.library = other.library;
    }

    public Library getLocalLibrary() {
        return library;
    }

    public WidgetDefinition getSuperDefinition() {
        return superDefinition;
    }

    public void setSuperDefinition(WidgetDefinition def) {
        superDefinition = def;
    }
}
