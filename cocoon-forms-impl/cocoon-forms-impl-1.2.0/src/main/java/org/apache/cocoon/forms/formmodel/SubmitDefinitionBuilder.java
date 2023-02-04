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

import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds a <code>&lt;fd:submit&gt;</code> widget. A submit is an action that
 * terminates the current form. It can either require the form to be valid
 * (in which case it will be redisplayed if not valid) or terminate it without
 * validation (e.g. a "cancel" button).
 *
 * <p>The syntax is as follows:
 * <pre>
 *   &lt;fd:submit id="sub-id" command="cmd" validate="false"/&gt;
 * </pre>
 *
 * The "validate" attribute can have the value <code>true</code> or
 * <code>false</code> and determines if the form is to be validated
 * (defaults to true).</p>
 *
 * @version $Id$
 */
public final class SubmitDefinitionBuilder extends ActionDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        SubmitDefinition definition = new SubmitDefinition();
        setupDefinition(widgetElement, definition, context);

        // parse "@validate"
        if (widgetElement.hasAttribute("validate")) {
            definition.setValidateForm(DomHelper.getAttributeAsBoolean(widgetElement, "validate", true));
        }

        definition.makeImmutable();
        return definition;
    }
}
