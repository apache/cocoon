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

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        String mimeTypes = DomHelper.getAttribute(widgetElement, "mime-types", null);
        
        UploadDefinition uploadDefinition = new UploadDefinition();
        super.setupDefinition(widgetElement, uploadDefinition);

        setDisplayData(widgetElement, uploadDefinition);
        Iterator iter = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (iter.hasNext()) {
            uploadDefinition.addValueChangedListener((ValueChangedListener)iter.next());
        }

        
        if(widgetElement.hasAttribute("required"))
            uploadDefinition.setRequired(DomHelper.getAttributeAsBoolean(widgetElement, "required", false));
        
        uploadDefinition.addMimeTypes(mimeTypes);

        uploadDefinition.makeImmutable();
        return uploadDefinition;
    }
}
