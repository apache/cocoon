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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Helper class for the implementation of widgets containing other widgets.
 *
 * @author Timothy Larson
 * @version $Id: ContainerDelegate.java,v 1.4 2004/04/09 16:43:21 mpo Exp $
 */
public class ContainerDelegate {
//    private WidgetDefinition definition;
    private List widgets;
    private Map widgetsById;

    private static final String WIDGETS_EL = "widgets";

//    public ContainerDelegate(WidgetDefinition definition) {
    public ContainerDelegate() {
        widgets = new ArrayList();
        widgetsById = new HashMap();
//        this.definition = definition;
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
        widgetsById.put(widget.getId(), widget);
    }

    public void readFromRequest(FormContext formContext) {
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.readFromRequest(formContext);
        }
    }

    public boolean validate(FormContext formContext) {
        boolean valid = true;
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            valid = valid & widget.validate(formContext);
        }
        return valid;
    }

    public boolean hasWidget(String id) {
        return widgetsById.containsKey(id);
    }

    public Widget getWidget(String id) {
        return (Widget)widgetsById.get(id);
    }

    public Iterator iterator() {
        return widgets.iterator();
    }

    /**
     * Returns false if there is at least one field which has no value.
     */
    public boolean widgetsHaveValues() {
        Iterator widgetsIt = widgets.iterator();
        while(widgetsIt.hasNext()) {
            Widget widget = (Widget)widgetsIt.next();
            if (widget.getValue() == null)
                return false;
        }
        return true;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        contentHandler.startElement(Constants.INSTANCE_NS, WIDGETS_EL, Constants.INSTANCE_PREFIX_COLON + WIDGETS_EL, XMLUtils.EMPTY_ATTRIBUTES);
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.generateSaxFragment(contentHandler, locale);
        }
        contentHandler.endElement(Constants.INSTANCE_NS, WIDGETS_EL, Constants.INSTANCE_PREFIX_COLON + WIDGETS_EL);
    }
}

