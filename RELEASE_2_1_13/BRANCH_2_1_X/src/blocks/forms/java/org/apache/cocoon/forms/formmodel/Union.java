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

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormsRuntimeException;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;

import org.apache.commons.lang.ObjectUtils;

/**
 * A discriminated union that references a discriminant value in another
 * widget and contains one of several cases (widgets).  To have a case
 * hold more than one widget or to use a different id for the case than
 * for the widget id, just wrap the widget(s) in a container widget named
 * with the desired case id.
 *
 * @version $Id$
 */
public class Union extends AbstractContainerWidget {

    //Note: union instances behave like simple "field" instance with respect to
    //      XSLT post-processing, the choice of element-name reflects this.
    private static final String UNION_EL = "field";

    private Widget caseWidget;
    protected String caseValue;

    private final UnionDefinition definition;


    public Union(UnionDefinition definition) {
        super(definition);
        this.definition = definition;
        // TODO: Remove after moving logic to Field.
        //item.enteredValue = (String)definition.getDefaultValue();
    }

    public WidgetDefinition getDefinition() {
        return definition;
    }

    /**
     * Called after widget's environment has been setup,
     * to allow for any contextual initalization such as
     * looking up case widgets for union widgets.
     */
    public void initialize() {
        String caseWidgetId = definition.getCaseWidgetId();
        this.caseWidget = getParent().lookupWidget(caseWidgetId);
        if (this.caseWidget == null) {
            throw new FormsRuntimeException("Could not find case widget '" + caseWidgetId + "' for union '" + getId() + "'.",
                                            getLocation());
        }

        ((ValueChangedListenerEnabled) caseWidget).addValueChangedListener(
            new ValueChangedListener() {
                public void valueChanged(ValueChangedEvent event) {
                    String newValue = (String)event.getNewValue();
                    if (!ObjectUtils.equals(Union.this.caseValue, newValue)) {
                        Union.this.caseValue = newValue;
                        getForm().addWidgetUpdate(Union.this);
                    }
                }
            }
        );
    }

    /**
     * @return "field"
     */
    public String getXMLElementName() {
        return UNION_EL;
    }

    public Object getValue() {
        return this.caseWidget.getValue();
    }

    public void readFromRequest(FormContext formContext) {
        // Ensure the case widget has read its value
        this.caseWidget.readFromRequest(formContext);

        Widget widget;
        // Read current case from request
        String newValue = (String)getValue();
        if (newValue != null && !newValue.equals("")) {
            // We need to know if the case widget is the submit widget,
            // and since the submit widget is only determined after the readFromRequest,
            // we need to do it here prematurely (as happens in Action & Upload)
            String fullId = caseWidget.getRequestParameterName();
            if (fullId.equals(formContext.getRequest().getParameter(Form.SUBMIT_ID_PARAMETER))) {
               getForm().setSubmitWidget(this.caseWidget);
            }

            if (getForm().getSubmitWidget() == this.caseWidget && !newValue.equals(this.caseValue)) {
                // If submitted by the case widget and its value has changed, read the values
                // for the previous case value. This allows to keep any already entered values
                // despite the case change.
                widget = getChild(this.caseValue);
            } else {
                // Get the corresponding widget (will create it if needed)
                widget = getChild(newValue);
            }

            if (widget != null && getCombinedState().isAcceptingInputs()) {
                widget.readFromRequest(formContext);
            }
        }

        if (!ObjectUtils.equals(this.caseValue, newValue)) {
            this.caseValue = newValue;
            getForm().addWidgetUpdate(this);
        }
    }

    // TODO: Simplify this logic.
    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

        Widget widget;
        boolean valid = true;
        // Read current case from request
        String value = (String)getValue();
        if (value != null && !value.equals("")) {
            if ((widget = getChild(value)) != null) {
                valid = valid & widget.validate();
            }
        }
        this.wasValid = valid;
        return valid;
    }

    public Widget getChild(String id) {
        if (!widgets.hasWidget(id) && definition.hasWidget(id)) {
            definition.createWidget(this, id);
            Widget child = super.getChild(id);
            child.initialize();
            return child;
        }
        return super.getChild(id);
    }

    //TODO: check further: cause the claim in the accompanied comment doesn't seem
    // to be completely correct

    // This method is overridden to suppress output of sub-widget sax fragments.
//    public void generateItemsSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
//        // Do nothing
//    }

}
