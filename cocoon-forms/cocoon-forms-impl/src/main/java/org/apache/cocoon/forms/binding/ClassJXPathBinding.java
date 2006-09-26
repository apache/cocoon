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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * ClassJXPathBinding provides an implementation of a {@link Binding}
 * which allows the specification of a class of reusable bindings.
 * <p>NOTES:
 * <ol>
 * <li>This Binding uses the provided widget-id as the name for the class.</li>
 * </ol>
 *
 * @version $Id$
 */
public class ClassJXPathBinding extends ComposedJXPathBindingBase {

    private final String widgetId;

    /**
     * Constructs ClassJXPathBinding
     * @param commonAtts
     * @param widgetId
     * @param childBindings
     */
    public ClassJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                              String widgetId,
                              JXPathBindingBase[] childBindings) {
        super(commonAtts, childBindings);
        this.widgetId = widgetId;
    }

    /**
     * Returns binding definition id.
     */
    public String getId() {
        return widgetId;
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        // Do nothing
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        // Do nothing
    }

    public String toString() {
        return "ClassJXPathBinding [widget=" + this.widgetId + "]";
    }
}
