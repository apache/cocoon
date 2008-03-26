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

import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link org.apache.cocoon.forms.formmodel.UploadDefinition}s.
 * 
 * @version $Id$
 */
public final class UploadDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        UploadDefinition uploadDefinition = new UploadDefinition();
        setupDefinition(widgetElement, uploadDefinition, context);

        setDisplayData(widgetElement, uploadDefinition);
        Iterator i = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (i.hasNext()) {
            uploadDefinition.addValueChangedListener((ValueChangedListener)i.next());
        }

        if (widgetElement.hasAttribute("required")) {
            uploadDefinition.setRequired(DomHelper.getAttributeAsBoolean(widgetElement, "required", false));
        }

        String mimeTypes = DomHelper.getAttribute(widgetElement, "mime-types", null);
        uploadDefinition.addMimeTypes(mimeTypes);

        uploadDefinition.makeImmutable();
        return uploadDefinition;
    }
}
