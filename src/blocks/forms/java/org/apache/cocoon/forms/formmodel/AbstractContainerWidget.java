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

import java.util.Iterator;
import java.util.Locale;

import org.apache.cocoon.forms.FormContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A general-purpose abstract Widget which can hold zero or more widgets.
 *
 * @version $Id: AbstractContainerWidget.java,v 1.10 2004/04/30 08:37:45 bruno Exp $
 */
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerWidget {
    
    /**
     * List of contained widgets.
     */
    protected WidgetList widgets;

    /** 
     * Constructs AbstractContainerWidget
     */
    public AbstractContainerWidget() {
        widgets = new WidgetList();
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

    /**
     * Delegates the readFromRequest() down to the contained child-widgets.
     *  
     * When overriding one should call <code>super.readFromRequest()</code> 
     * to allow child-widgets to process the request.  
     * 
     * Overide only to add possible request-reading statements on the containment level.  
     * 
     * @param formContext to be passed to the {@link Widget#readFromRequest(FormContext)} 
     *                    of the contained widgets.
     */
    public void readFromRequest(FormContext formContext) {
        widgets.readFromRequest(formContext);
    }

    /**
     * Delegates the validate() down to the contained child-widgets,
     * and only validates the extra rules on this containment level if all
     * child-widgets are valid. 
     * 
     * When overriding one should call <code>super.validate()</code> as the first 
     * statement to keep in sync with this behaviour. 
     * 
     * @return <code>true</code> only if all contained widgets are valid and the 
     *         extra validation rules on this containment level are ok.
     */
    public boolean validate() {
        // Validate self only if child widgets are valid
        //TODO: check if we should not change this to still validating kids first 
        // BUT also validating the top level
        if (widgets.validate()) {
            return super.validate();
        } else {
            return false;
        }
    }


    /**
     * Subclass container widgets can call this super.generateItemSaxFragment(..) 
     * to just insert the child-widget content wrapped in a @lt;fi:widgets@gt; 
     * 
     * @param contentHandler where the SAX is sent to via {@link Widget#generateSaxFragment(ContentHandler, Locale)
     * @param locale
     * @throws SAXException
     */
    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        widgets.generateSaxFragment(contentHandler, locale);
    }
}
