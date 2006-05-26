/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builder for {@link CalculatedField}s.
 * 
 * <p>A common calculated field definition is as follows :</p>
 * <p><code>
 *   &lt;fd:calculatedfield id="id" [state="{invisible|output|disabled|active}"]&gt;
 *     &lt;fd:datatype base="..."&gt;...&lt;/fd:datatype&gt;
 *     &lt;fd:label&gt;...&lt;/fd:label&gt;
 *     &lt;fd:value type="..."&gt;...&lt;/fd:algorithm&gt;
 *   &lt;/fd:calculatedfield&gt;
 * </code></p>
 * 
 * <p>Since it inherits from {@link org.apache.cocoon.forms.formmodel.Field}, 
 * other attributes and elements may be specified, like listeners (on-value-changed, on-create etc..) or
 * selection lists (which could make sense if the algorithm calculates one value between many possibilities).
 * </p>
 * 
 * <p> Note that the default state is active, althought typing in a calculated field is useless. The state invisible 
 * can be used to create fields which are used as temporary value placeholders in a chain of calculations. 
 * </p>
 * 
 * @version $Id$
 */
public class CalculatedFieldDefinitionBuilder extends FieldDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        CalculatedFieldDefinition definition = new CalculatedFieldDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
    }
        
    
    protected void setupDefinition(Element widgetElement, CalculatedFieldDefinition definition) throws Exception {
        super.setupDefinition(widgetElement, definition);

        Element algorithmElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "value");
        ServiceSelector builderSelector = (ServiceSelector)this.serviceManager.lookup(CalculatedFieldAlgorithmBuilder.ROLE + "Selector");
        CalculatedFieldAlgorithmBuilder builder = null;
        try {
            String algorithmType = algorithmElement.getAttribute("type");
            if (algorithmType.length() == 0) algorithmType = null;
            builder = (CalculatedFieldAlgorithmBuilder)builderSelector.select(algorithmType);
            definition.setAlgorithm(builder.build(algorithmElement));
        } finally {
            if (builder != null) {
                builderSelector.release(builder);
            }
            this.serviceManager.release(builderSelector);
        }
    }
    
    
}
