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

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.FormHandler;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;

/**
 * A widget that serves as a container for other widgets, the top-level widget in
 * a form description file.
 */
public class Form extends AbstractWidget {
    private List widgets;
    private Map widgetsById;
    private FormDefinition definition;
    private FormHandler formHandler;

    public Form(FormDefinition definition) {
        widgets = new ArrayList();
        widgetsById = new HashMap();
        this.definition = definition;
    }

    protected void addWidget(Widget widget) {
        widgets.add(widget);
        widget.setParent(this);
        widgetsById.put(widget.getId(), widget);
    }

    public void setFormHandler(FormHandler formHandler) {
        this.formHandler = formHandler;
    }

    /**
     * Processes a form submit. This consists of multiple steps:
     * <ul>
     *  <li>all widgets read their value from the request (i.e. {@link #readFromRequest} is called recursively on
     *       the whole widget tree)
     *  <li>if there is an action event, execute it
     *  <li>perform validation, if {@link FormContext#doValidation} returns true (which is true by default,
     *      but false by default if there is an action event).
     * </ul>
     *
     * If the form is finished, i.e. validation was succesful and the form should not be redisplayed to the user,
     * then this method returns true, otherwise it returns false.
     */
    public boolean process(FormContext formContext) {
        readFromRequest(formContext);
        if (formContext.getActionEvent() != null && formHandler != null) {
            formHandler.handleActionEvent(formContext.getActionEvent());
        }
        if (formContext.doValidation())
            return validate(formContext);
        return false;
    }

    public void readFromRequest(FormContext formContext) {
        // let all individual widgets read their value from the request object
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.readFromRequest(formContext);
        }
    }

    public boolean validate(FormContext formContext) {
        boolean allValid = true;
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            allValid = allValid & widget.validate(formContext);
        }
        return allValid;
    }

    public Widget getWidget(String id) {
        return (Widget)widgetsById.get(id);
    }

    public String getId() {
        return definition.getId();
    }

    private static final String FORM_EL = "form";
    private static final String CHILDREN_EL = "children";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl formAttrs = new AttributesImpl();
        formAttrs.addCDATAAttribute("id", definition.getId());

        contentHandler.startElement(Constants.WI_NS, FORM_EL, Constants.WI_PREFIX_COLON + FORM_EL, Constants.EMPTY_ATTRS);
        definition.generateLabel(contentHandler);

        contentHandler.startElement(Constants.WI_NS, CHILDREN_EL, Constants.WI_PREFIX_COLON + CHILDREN_EL, Constants.EMPTY_ATTRS);
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.generateSaxFragment(contentHandler, locale);
        }
        contentHandler.endElement(Constants.WI_NS, CHILDREN_EL, Constants.WI_PREFIX_COLON + CHILDREN_EL);

        contentHandler.endElement(Constants.WI_NS, FORM_EL, Constants.WI_PREFIX_COLON + FORM_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
}
