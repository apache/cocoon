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
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.formmodel.Struct;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * StructJXPathBinding provides an implementation of a {@link Binding}
 * that narrows the context towards provided childbindings.
 * <p>
 * NOTES: <ol>
 * <li>This Binding assumes that the provided widget-id points to a widget
 * that contains other widgets.</li>
 * </ol>
 *
 * @author Timothy Larson
 * @version CVS $Id: StructJXPathBinding.java,v 1.9 2004/03/09 13:54:09 reinhard Exp $
 */
public class StructJXPathBinding extends ComposedJXPathBindingBase {

    private final String xpath;

    private final String widgetId;

    /**
     * Constructs StructJXPathBinding
     * @param widgetId
     * @param xpath
     * @param childBindings
     */
    public StructJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String widgetId, String xpath, JXPathBindingBase[] childBindings) {
        super(commonAtts, childBindings);
        this.widgetId = widgetId;
        this.xpath = xpath;
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Struct structWidget = (Struct)getWidget(frmModel, this.widgetId);
        JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        super.doLoad(structWidget, subContext);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done loading " + toString());
        }
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Struct structWidget = (Struct)frmModel.getWidget(this.widgetId);
        JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        super.doSave(structWidget, subContext);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving " + toString());
        }
    }

    public String toString() {
        return "StructJXPathBinding [widget=" + this.widgetId + ", xpath=" + this.xpath + "]";
    }
}
