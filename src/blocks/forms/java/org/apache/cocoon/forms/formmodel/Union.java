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

import org.apache.cocoon.forms.FormContext;

/**
 * A discriminated union that references a discriminant value in another
 * widget and contains one of several cases (widgets).  To have a case
 * hold more than one widget or to use a different id for the case than
 * for the widget id, just wrap the widget(s) in a container widget named
 * with the desired case id.
 *
 * @version $Id: Union.java,v 1.6 2004/04/20 22:19:27 mpo Exp $
 */
public class Union extends AbstractContainerWidget {
    
    //TODO: check if this element-name shouldn't have been 'union'?
    private static final String UNION_EL = "field";
    private Widget caseWidget;
    
    private final UnionDefinition definition;

    public Union(UnionDefinition definition) {
        this.definition = definition;
        // TODO: Remove after moving logic to Field.
        //item.enteredValue = (String)definition.getDefaultValue();
    }
    
    protected WidgetDefinition getDefinition() {
        return definition;
    }

    // TODO: This whole union mess is too dependent on undefined sequences of execution.
    // These need to be ordered into a contract of sequences.

    public void setParent(Widget widget) {
        super.setParent(widget);
        resolve();
    }

    // TODO: The "resolve" step currently expands each "New" into the list of widgets in the corresponding "Class".
    // "resolve" should be changed to "expand", and a new step, "resolve" should be introduced which patches up any
    // *widget* (not definition) references after the expansion has put all of the widgets in place.
    public void resolve() {
        String caseWidgetId = definition.getCaseWidgetId();
        caseWidget = getParent().getWidget(caseWidgetId);
    }

    /**
     * @return "field"
     */
    public String getXMLElementName() {
        return UNION_EL;
    }

    public Object getValue() {
        return caseWidget.getValue();
    }

    public void readFromRequest(FormContext formContext) {
        // Ensure the case widget has read its value
        caseWidget.readFromRequest(formContext);
        
        Widget widget;
        // Read current case from request
        String value = (String)getValue();
        if (value != null && !value.equals(""))
            if ((widget = getWidget(value)) != null)
                widget.readFromRequest(formContext);

        // Read union discriminant value from request
        //item.readFromRequest(formContext);
    }

    // TODO: Simplify this logic.
    public boolean validate(FormContext formContext) {
        Widget widget;
        boolean valid = true;
        // Read current case from request
        String value = (String)getValue();
        if (value != null && !value.equals(""))
            if ((widget = getWidget(value)) != null)
                valid = valid & widget.validate(formContext);
        return valid;
    }

    public Widget getWidget(String id) {
        if (!widgets.hasWidget(id) && ((ContainerDefinition)definition).hasWidget(id))
            ((ContainerDefinition)definition).createWidget(this, id);
        return super.getWidget(id);
    }

    
    //TODO: check further: cause the claim in the accompanied comment doesn't seem
    // to be completely correct
    
    // This method is overridden to suppress output of sub-widget sax fragments.
//    public void generateItemsSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
//        // Do nothing
//    }

}
