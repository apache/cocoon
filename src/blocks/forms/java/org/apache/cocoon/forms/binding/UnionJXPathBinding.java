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

import org.apache.cocoon.forms.formmodel.Union;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * UnionJXPathBinding provides an implementation of a {@link Binding}
 * that narrows the context towards provided childbindings.
 * <p>
 * NOTES: <ol>
 * <li>This Binding assumes that the provided widget-id points to a
 * union widget.</li>
 * </ol>
 *
 * @author Timothy Larson
 * @version CVS $Id: UnionJXPathBinding.java,v 1.1 2004/03/09 10:33:55 reinhard Exp $
 */
public class UnionJXPathBinding extends ComposedJXPathBindingBase {

    private final String xpath;

    private final String widgetId;

    /**
     * Constructs UnionJXPathBinding
     * @param commonAtts
     * @param widgetId
     * @param xpath
     * @param childBindings
     */
    public UnionJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String widgetId, String xpath, JXPathBindingBase[] childBindings) {
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
        Widget widget = frmModel.getWidget(this.widgetId);
        JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        if (!(widget instanceof Union))
            throw new RuntimeException("Binding: Expected Union widget, but received class: \"" +
                    widget.getClass().getName() + "\".");
        Union unionWidget = (Union)widget;
        Binding[] subBindings = getChildBindings();
        if (subBindings != null) {
            int size = subBindings.length;
            for (int i = 0; i < size; i++) {
                subBindings[i].loadFormFromModel(unionWidget, subContext);
            }
        }
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
        Union unionWidget = (Union)frmModel.getWidget(this.widgetId);
        JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        Binding[] subBindings = getChildBindings();
        if (subBindings != null) {
            int size = subBindings.length;
            for (int i = 0; i < size; i++) {
                subBindings[i].saveFormToModel(unionWidget, subContext);
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving " + toString());
        }
    }

    public String toString() {
        return "UnionJXPathBinding [widget=" + this.widgetId + ", xpath=" + this.xpath + "]";
    }
}
