/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * An Action widget. An Action widget can cause an {@link ActionEvent} to be triggered
 * on the server side, which will be handled by either the event handlers defined in the
 * form definition, and/or by the {@link org.apache.cocoon.woody.event.FormHandler FormHandler}
 * registered with the form, if any. An Action widget can e.g. be rendered as a button,
 * or as a hidden field which gets its value set by javascript. The Action widget will generate its associated
 * ActionEvent when a requestparameter is present with as name the id of this Action widget, and as
 * value a non-empty value.
 */
public class Action extends AbstractWidget {
    protected ActionDefinition definition;

    public Action(ActionDefinition definition) {
        this.definition = definition;
        setLocation(definition.getLocation());
    }

    public String getId() {
        return definition.getId();
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

    public boolean validate(FormContext formContext) {
        return true;
    }

    private static final String ACTION_EL = "action";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl buttonAttrs = new AttributesImpl();
        buttonAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, ACTION_EL, Constants.WI_PREFIX_COLON + ACTION_EL, buttonAttrs);
        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);
        contentHandler.endElement(Constants.WI_NS, ACTION_EL, Constants.WI_PREFIX_COLON + ACTION_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
    
    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireActionEvent((ActionEvent)event);
    }
}
