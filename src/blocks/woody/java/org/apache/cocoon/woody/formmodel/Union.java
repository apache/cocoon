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

import java.util.Locale;

import org.apache.cocoon.woody.FormContext;
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
 * @version $Id: Union.java,v 1.3 2004/02/11 10:43:30 antonio Exp $
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
