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

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.WidgetEvent;

/**
 * An Action widget. An Action widget can cause an {@link ActionEvent} to be triggered
 * on the server side, which will be handled by either the event handlers defined in the
 * form definition, and/or by the {@link org.apache.cocoon.forms.event.FormHandler FormHandler}
 * registered with the form, if any. An Action widget can e.g. be rendered as a button,
 * or as a hidden field which gets its value set by javascript. The Action widget will generate its associated
 * ActionEvent when a requestparameter is present with as name the id of this Action widget, and as
 * value a non-empty value.
 * 
 * @version $Id: Action.java,v 1.6 2004/04/22 14:26:48 mpo Exp $
 */
public class Action extends AbstractWidget {
    private final ActionDefinition definition;

    public Action(ActionDefinition definition) {
        this.definition = definition;
    }

    protected WidgetDefinition getDefinition() {
        return this.definition;
    }
    
    public void readFromRequest(final FormContext formContext) {
        Form form = getForm();
        
        // Set the submit widget if we can determine it from the request
        String fullId = getFullyQualifiedId();
        Request request = formContext.getRequest();
        
        String value = request.getParameter(fullId);
        if (value != null && value.length() > 0) {
            form.setSubmitWidget(this);
            
        } else {
            // Special workaround an IE bug for <input type="image" name="foo"> :
            // in that case, IE only sends "foo.x" and "foo.y" and not "foo" whereas
            // standards-compliant browsers such as Mozilla do send the "foo" parameter.
            //
            // Note that since actions are terminal widgets, there's no chance of conflict
            // with a child "x" or "y" widget.
            value = request.getParameter(fullId + ".x");
            if ((value != null) && value.length() > 0) {
                form.setSubmitWidget(this);
            }
        }
        
        if (form.getSubmitWidget() == this) {
            form.addWidgetEvent(new ActionEvent(this, definition.getActionCommand()));
            
            handleActivate();
        }
    }
    
    /**
     * Handle the fact that this action was activated. The default here is to end the
     * current form processing and redisplay the form, which means that actual behaviour
     * should be implemented in event listeners.
     */
    protected void handleActivate() {
        getForm().endProcessing(true);
    }

    /**
     * Always return <code>true</code> (an action has no validation)
     * 
     * @todo is there a use case for actions having validators?
     */
    public boolean validate(FormContext formContext) {
        return true;
    }

    private static final String ACTION_EL = "action";
    
    /**
     * @return "action"
     */
    public String getXMLElementName() {        
        return ACTION_EL;
    }  
    
    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireActionEvent((ActionEvent)event);
    }

}
