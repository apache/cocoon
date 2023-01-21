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

import org.apache.cocoon.util.Deprecation;
import org.w3c.dom.Element;

/**
 * Builds {StructDefinition}s.
 *
 * @deprecated replaced by {@link GroupDefinitionBuilder}
 * @version $Id$
 */
public class StructDefinitionBuilder extends AbstractContainerDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element element, WidgetDefinitionBuilderContext context)
    throws Exception {
        StructDefinition definition = new StructDefinition();
        setupDefinition(element, definition, context);

        setDisplayData(element, definition);
        setupContainer(element, "widgets", definition, context);

        definition.makeImmutable();
        Deprecation.logger.info("Use of 'fd:struct' is deprecated. Use 'fd:group' instead, at " + definition.getLocation());
        return definition;
    }
}
