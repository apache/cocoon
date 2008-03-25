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

import org.apache.cocoon.forms.formmodel.AggregateField;
import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * AggregateJXPathBinding provides an implementation of a {@link Binding}
 * that narrows the context towards provided childbindings.
 * <p>NOTES:
 * <ol>
 * <li>This Binding assumes that the provided widget-id points to a widget
 * that contains other widgets.</li>
 * </ol>
 *
 * @version $Id$
 */
public class AggregateJXPathBinding extends ComposedJXPathBindingBase {

    private final String xpath;

    private final String widgetId;

    /**
     * Constructs AggregateJXPathBinding
     * @param widgetId
     * @param xpath
     * @param childBindings
     */
    public AggregateJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                                  String widgetId,
                                  String xpath,
                                  JXPathBindingBase[] childBindings) {
        super(commonAtts, childBindings);
        this.widgetId = widgetId;
        this.xpath = xpath;
    }

    public String getXPath() { return xpath; }
    public String getId() { return widgetId; }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
        AggregateField aggregate =
                (AggregateField) selectWidget(frmModel, this.widgetId);
        JXPathContext subContext =
                jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        super.doLoad(aggregate, subContext);
        aggregate.combineFields();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Done loading " + this);
        }
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
        AggregateField aggregate =
                (AggregateField) selectWidget(frmModel, this.widgetId);
        JXPathContext subContext =
                jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        super.doSave(aggregate, subContext);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Done saving " + this);
        }
    }

    public String toString() {
        return "AggregateJXPathBinding [widget=" + this.widgetId + ", xpath=" + this.xpath + "]";
    }
}
