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
import java.util.Iterator;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A general-purpose abstract Widget which can hold zero or more widgets.
 *
 * @author Timothy Larson
 * @version $Id: AbstractContainerWidget.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerWidget {
    protected ContainerDelegate widgets;

    public AbstractContainerWidget(AbstractWidgetDefinition definition) {
        setDefinition(definition);
        setLocation(definition.getLocation());
        widgets = new ContainerDelegate(definition);
    }

    public void addWidget(Widget widget) {
        widget.setParent(this);
        widgets.addWidget(widget);
    }

    public boolean hasWidget(String id) {
        return widgets.hasWidget(id);
    }

    public Widget getWidget(String id) {
    	return widgets.getWidget(id);
    }

    public Iterator getChildren() {
        return widgets.iterator();
    }

    public void readFromRequest(FormContext formContext) {
        widgets.readFromRequest(formContext);
    }

    public boolean validate(FormContext formContext) {
        // Validate self only if child widgets are valid
        if (widgets.validate(formContext)) {
            return super.validate(formContext);
        } else {
            return false;
        }
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale, String element) throws SAXException {
        if (getId() == null || getId().equals("")) {
            contentHandler.startElement(Constants.FI_NS, element, Constants.FI_PREFIX_COLON + element, Constants.EMPTY_ATTRS);
        } else {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", getFullyQualifiedId());
            contentHandler.startElement(Constants.FI_NS, element, Constants.FI_PREFIX_COLON + element, attrs);
        }
        if (definition != null)
            definition.generateDisplayData(contentHandler);
        // The child widgets
        widgets.generateSaxFragment(contentHandler, locale);
        contentHandler.endElement(Constants.FI_NS, element, Constants.FI_PREFIX_COLON + element);
    }
}
