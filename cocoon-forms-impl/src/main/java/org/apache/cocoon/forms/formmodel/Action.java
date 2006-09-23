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

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.*;

/**
 * An Action widget. An Action widget can cause an {@link ActionEvent} to be triggered
 * on the server side, which will be handled by either the event handlers defined in the
 * form definition, and/or by the {@link org.apache.cocoon.forms.event.FormHandler FormHandler}
 * registered with the form, if any. An Action widget can e.g. be rendered as a button,
 * or as a hidden field which gets its value set by javascript. The Action widget will generate its associated
 * ActionEvent when a requestparameter is present with as name the id of this Action widget, and as
 * value a non-empty value.
 *
 * @version $Id$
 */
public class Action extends AbstractWidget implements ActionListenerEnabled {

    private final ActionDefinition definition;
    /** Additional listeners to those defined as part of the widget definition (if any). */
    private ActionListener listener;


    public Action(ActionDefinition definition) {
        super(definition);
        this.definition = definition;
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void readFromRequest(final FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        Form form = getForm();

        // Set the submit widget if we can determine it from the request
        String fullId = getRequestParameterName();
        Request request = formContext.getRequest();

        if (request.getParameter(fullId) != null ||
                fullId.equals(request.getParameter(Form.SUBMIT_ID_PARAMETER))) {
           form.setSubmitWidget(this);

        } else {
            // Special workaround an IE bug for <input type="image" name="foo"> :
            // in that case, IE only sends "foo.x" and "foo.y" and not "foo" whereas
            // standards-compliant browsers such as Mozilla do send the "foo" parameter.
            //
            // Note that since actions are terminal widgets, there's no chance of conflict
            // with a child "x" or "y" widget.
            String value = request.getParameter(fullId + ".x");
            if ((value != null) && value.length() > 0) {
                form.setSubmitWidget(this);
            }
        }

        if (form.getSubmitWidget() == this) {
            handleActivate();
        }
    }
    
    /**
     * Performs all actions and calls on-action listeners attached to this object. Note that this
     * does not register this object as the submit widget and doesn't stop form processing.
     */
    public void performAction() {
        getForm().addWidgetEvent(new ActionEvent(this, definition.getActionCommand()));
    }

    /**
     * Handle the fact that this action was activated. The default here is to perform any
     * action associated to this object, end the current form processing and redisplay the
     * form.
     * 
     * @see #performAction()
     */
    protected void handleActivate() {
        performAction();
        getForm().endProcessing(true);
    }

    /**
     * Always return <code>true</code> (an action has no validation)
     *
     * <br>TODO is there a use case for actions having validators?
     */
    public boolean validate() {
        return true;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#isValid()
     */
    public boolean isValid() {
        return true;
    }

    private static final String ACTION_EL = "action";

    /**
     * @return "action"
     */
    public String getXMLElementName() {
        return ACTION_EL;
    }

    /**
     * Adds a ActionListener to this widget instance. Listeners defined
     * on the widget instance will be executed in addtion to any listeners
     * that might have been defined in the widget definition.
     */
    public void addActionListener(ActionListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeActionListener(ActionListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    private void fireActionEvent(ActionEvent event) {
        if (this.listener != null) {
            this.listener.actionPerformed(event);
        }
    }

    public void broadcastEvent(WidgetEvent event) {
        if (event instanceof ActionEvent) {
            this.definition.fireActionEvent((ActionEvent)event);
            fireActionEvent((ActionEvent)event);
        } else {
            // Other kinds of events
            super.broadcastEvent(event);
        }
    }
}
