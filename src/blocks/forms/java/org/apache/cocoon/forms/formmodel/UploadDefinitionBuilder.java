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

import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link org.apache.cocoon.forms.formmodel.UploadDefinition}s.
 * 
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: UploadDefinitionBuilder.java,v 1.2 2004/03/09 11:31:12 joerg Exp $
 */
public class UploadDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        String mimeTypes = DomHelper.getAttribute(widgetElement, "mime-types", null);
        boolean required = DomHelper.getAttributeAsBoolean(widgetElement, "required", false);
        
        UploadDefinition uploadDefinition = new UploadDefinition(required, mimeTypes);
        setLocation(widgetElement, uploadDefinition);
        setId(widgetElement, uploadDefinition);

        setDisplayData(widgetElement, uploadDefinition);
        setValidators(widgetElement, uploadDefinition);

        return uploadDefinition;
    }
}
