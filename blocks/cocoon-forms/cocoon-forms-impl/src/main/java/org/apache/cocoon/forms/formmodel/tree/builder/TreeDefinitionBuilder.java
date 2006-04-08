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
package org.apache.cocoon.forms.formmodel.tree.builder;

import java.util.Iterator;

import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.formmodel.AbstractWidgetDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.formmodel.tree.Tree;
import org.apache.cocoon.forms.formmodel.tree.TreeDefinition;
import org.apache.cocoon.forms.formmodel.tree.TreeSelectionListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds a {@link org.apache.cocoon.forms.formmodel.tree.Tree}.
 * 
 * @version $Id$
 */
public class TreeDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        TreeDefinition definition = new TreeDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
    }
    
    protected void setupDefinition(Element widgetElement, TreeDefinition definition) throws Exception {
        super.setupDefinition(widgetElement, definition);
        
        // Get the optional "root-visible" attribute
        definition.setRootVisible(DomHelper.getAttributeAsBoolean(widgetElement, "root-visible", true));
        
        // Get the optional "selection" attribute
        String selection = DomHelper.getAttribute(widgetElement, "selection", null);
        if (selection == null) {
            // Nothing
        } else if ("multiple".equals(selection)) {
            definition.setSelectionModel(Tree.MULTIPLE_SELECTION);
        } else if ("single".equals(selection)) {
            definition.setSelectionModel(Tree.SINGLE_SELECTION);
        } else {
            throw new Exception("Invalid value selection value '" + selection + "' at " +
                    DomHelper.getLocation(widgetElement));
        }
        
        // Get the model optional element
        Element modelElt = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "tree-model", false);
        if (modelElt != null) {
            String type = DomHelper.getAttribute(modelElt, "type");
            ServiceSelector selector =
                (ServiceSelector)this.serviceManager.lookup(TreeModelDefinitionBuilder.ROLE + "Selector");
            
            TreeModelDefinitionBuilder builder = (TreeModelDefinitionBuilder)selector.select(type);
            try {
                definition.setModelDefinition(builder.build(modelElt));
            } finally {
                selector.release(builder);
                serviceManager.release(selector);
            }
        }
        
        // parse "on-selection-changed"
        Iterator iter = buildEventListeners(widgetElement, "on-selection-changed", TreeSelectionListener.class).iterator();
        while (iter.hasNext()) {
            definition.addSelectionListener((TreeSelectionListener)iter.next());
        }
        //TODO: allow child widgets, that will be attached to each node of the tree
        //It may be useful to add TreeModel.getNodeType(Object) so that the container holding child
        //widgets can have a value used by a union widget.
    }
}
