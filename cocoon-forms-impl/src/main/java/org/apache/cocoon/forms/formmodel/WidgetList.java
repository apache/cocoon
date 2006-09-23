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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Helper class for the implementation of widgets containing other widgets.
 * This implements a type-aware List of Widgets that automatically can distribute
 * the common Widget operations over the contained Widgets.
 *
 * @version $Id$
 */
public class WidgetList {

    private static final String WIDGETS_EL = "widgets";
    
    /** 
     * List of the contained widgets.
     * This maintains the original order of the widgets to garantee order of
     * validation and generation of SAXFragments
     */
    private List widgets;
    
    /** 
     * Map of the contained widgets using its id as the lookup key.
     */
    private Map widgetsById;


    /**
     * Constructs ContainerDelegate to store and jointly manage a list of 
     * contained widgets.
     */
    public WidgetList() {
        widgets = new ArrayList();
        widgetsById = new HashMap();
    }

    /**
     * Get the (unmodifiable) list of widgets
     * 
     * @return the widget list
     */
    public List getWidgetList() {
        return Collections.unmodifiableList(this.widgets);
    }
    
    /**
     * Get the (unmodifiable) map of widgets
     * 
     * @return the widget map
     */
    public Map getWidgetMap() {
        return Collections.unmodifiableMap(this.widgetsById);
    }

    /** 
     * Adds a widget to the list of contained {@link Widget}'s
     * 
     * @param widget
     */
    public void addWidget(Widget widget) {
        widgets.add(widget);
        widgetsById.put(widget.getId(), widget);
    }
    

    /**
     * Performs the {@link Widget#readFromRequest(FormContext)} on all the 
     * contained widgets.
     * 
     * @param formContext to pass to the {@link Widget#readFromRequest(FormContext)}
     * 
     * @see Widget#readFromRequest(FormContext)
     */
    public void readFromRequest(FormContext formContext) {
        Iterator widgetIt = iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.readFromRequest(formContext);
        }
    }

    /** 
     * Validates all contained widgets and returns the combined result.
     * 
     * @return <code>false</code> if at least one of the contained widgets is not valid.
     * 
     * @see Widget#validate()
     */
    public boolean validate() {
        boolean valid = true;
        Iterator widgetIt = iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            valid = valid & widget.validate();
        }
        return valid;
    }

    /**
     * Checks if a widget with the provided id is contained in the list.
     *
     * @param id of the widget to look for.
     * @return true if the widget was found
     */
    public boolean hasWidget(String id) {
        return widgetsById.containsKey(id);
    }

    /** 
     * Looks for a widget in this list by using the provided id as a lookup key.
     * 
     * @param id of the widget to look for
     * @return the found widget or <code>null</code> if it could not be found.
     */
    public Widget getWidget(String id) {
        return (Widget)widgetsById.get(id);
    }

    /** 
     * @return an iterator over the contained {@link Widget}'s
     */
    public Iterator iterator() {
        return widgets.iterator();
    }

    /**
     * @return <code>false</code> if at least one of the contained widgets has no value.
     */
    public boolean widgetsHaveValues() {
        Iterator widgetsIt = iterator();
        while(widgetsIt.hasNext()) {
            Widget widget = (Widget)widgetsIt.next();
            if (widget.getValue() == null)
                return false;
        }
        return true;
    }

    /**
     * Generates the SAXfragments of the contained widgets
     * 
     * @param contentHandler
     * @param locale
     * @throws SAXException
     * 
     * @see Widget#generateSaxFragment(ContentHandler, Locale)
     */
    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        contentHandler.startElement(FormsConstants.INSTANCE_NS, WIDGETS_EL, FormsConstants.INSTANCE_PREFIX_COLON + WIDGETS_EL, XMLUtils.EMPTY_ATTRIBUTES);
        Iterator widgetIt = widgets.iterator();
        while (widgetIt.hasNext()) {
            Widget widget = (Widget)widgetIt.next();
            widget.generateSaxFragment(contentHandler, locale);
        }
        contentHandler.endElement(FormsConstants.INSTANCE_NS, WIDGETS_EL, FormsConstants.INSTANCE_PREFIX_COLON + WIDGETS_EL);
    }
}

