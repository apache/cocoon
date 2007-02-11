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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Group;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * GroupJXPathBinding provides an implementation of a {@link Binding}
 * that narrows the context towards provided childbindings.
 * <p>
 * NOTES: <ol>
 * <li>This Binding assumes that the provided widget-id points to a widget
 * that contains other widgets.</li>
 * </ol>
 *
 * @version $Id$
 */
public class GroupJXPathBinding extends ContextJXPathBinding {

    private final String widgetId;

    /**
     * Constructs GroupJXPathBinding
     * @param widgetId
     * @param xpath
     * @param childBindings
     */
    public GroupJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String widgetId, String xpath, JXPathBindingBase[] childBindings) {
        super(commonAtts, xpath, childBindings);
        this.widgetId = widgetId;
    }

    public String getId() { return widgetId; }
    
    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Group groupWidget = (Group)selectWidget(frmModel, this.widgetId);
        super.doLoad(groupWidget, jxpc);
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Group groupWidget = (Group)selectWidget(frmModel, this.widgetId);
        super.doSave(groupWidget, jxpc);
    }

    public String toString() {
        return "GroupJXPathBinding [widget=" + this.widgetId + ", xpath=" + getXPath() + "]";
    }
}
