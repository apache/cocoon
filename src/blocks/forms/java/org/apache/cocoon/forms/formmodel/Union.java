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

import java.util.Locale;

import org.apache.cocoon.forms.FormContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A discriminated union that references a discriminant value in another
 * widget and contains one of several cases (widgets).  To have a case
 * hold more than one widget or to use a different id for the case than
 * for the widget id, just wrap the widget(s) in a container widget named
 * with the desired case id.
 *
 * @author Timothy Larson
 * @version $Id: Union.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public class Union extends AbstractContainerWidget {
    private static final String ELEMENT = "field";
    private Widget caseWidget;

    public Union(UnionDefinition definition) {
        super(definition);
        setLocation(definition.getLocation());
        // TODO: Remove after moving logic to Field.
        //item.enteredValue = (String)definition.getDefaultValue();
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
        String caseWidgetId = ((UnionDefinition)definition).getCaseWidgetId();
        caseWidget = getParent().getWidget(caseWidgetId);
    }

    public String getElementName() {
        return ELEMENT;
    }

    public Object getOldValue() {
        return ((Field)caseWidget).getOldValue();
    }

    public Object getValue() {
        return caseWidget.getValue();
    }

    public void readFromRequest(FormContext formContext) {
        Widget widget;
        // Read current case from request
        String value = (String)getOldValue();
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
        String value = (String)getOldValue();
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

    // This method is overridden to suppress output of sub-widget sax fragments.
    public void generateItemsSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Do nothing
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        generateSaxFragment(contentHandler, locale, ELEMENT);
    }
}
