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
package org.apache.cocoon.woody.formmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for Widget implementations. Provides functionality
 * common to many widgets.
 * 
 * @version $Id: AbstractWidget.java,v 1.13 2004/03/09 13:53:55 reinhard Exp $
 */
public abstract class AbstractWidget implements Widget {
    private String location;
    private Widget parent;
    private Form form;
    protected AbstractWidgetDefinition definition;
    
    private List validators;

    /**
     * Sets the definition of this widget.
     */
    protected void setDefinition(AbstractWidgetDefinition definition) {
        this.definition = definition;
    }

    /**
     * Gets the id of this widget.
     */
    public String getId() {
        return definition.getId();
    }

    /**
     * Sets the source location of this widget.
     */
    protected void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the source location of this widget.
     */
    public String getLocation() {
        return this.location;
    }

    public Widget getParent() {
        return parent;
    }

    public void setParent(Widget widget) {
        this.parent = widget;
    }

    public Form getForm() {
        if (this.form == null) {
            if (parent == null) {
                this.form = (Form)this;
            } else {
                this.form = parent.getForm();
            }
        }
        return this.form;
    }

    public String getNamespace() {
        if (getParent() != null && getParent().getNamespace().length() > 0) {
            return getParent().getNamespace() + "." + getId();
        } else {
            return getId();
        }
    }

    public String getFullyQualifiedId() {
        if (parent != null) {
            String namespace = parent.getNamespace();
            if (namespace.length() > 0) {
                return namespace + "." + getId();
            }
        }
        return getId();
    }

    public Object getValue() {
        return null;
    }

    public void setValue(Object object) {
        throw new RuntimeException("Cannot set the value of widget " + getFullyQualifiedId());
    }

    public boolean isRequired() {
        return false;
    }

    public Widget getWidget(String id) {
        return null;
    }
    
    public void broadcastEvent(WidgetEvent event) {
        throw new UnsupportedOperationException("Widget " + this.getFullyQualifiedId() + " doesn't handle events.");
    }
    
    /**
     * Add a validator to this widget instance.
     * 
     * @param validator
     */
    public void addValidator(WidgetValidator validator) {
        if (this.validators == null) {
            this.validators = new ArrayList();
        }
        
        this.validators.add(validator);
    }
    
    /**
     * Remove a validator from this widget instance
     * 
     * @param validator
     * @return <code>true</code> if the validator was found.
     */
    public boolean removeValidator(WidgetValidator validator) {
        return (this.validators == null)? false : this.validators.remove(validator);
    }
    
    public boolean validate(FormContext context) {
        // Test validators from the widget definition
        if (!this.definition.validate(this, context)) {
            // Failed
            return false;
        } else {
            // Definition sussessful, test local validators
            if (this.validators == null) {
                // No local validators
                return true;
            } else {
                // Iterate on local validators
                Iterator iter = this.validators.iterator();
                while(iter.hasNext()) {
                    WidgetValidator validator = (WidgetValidator)iter.next();
                    if (!validator.validate(this, context)) {
                        return false;
                    }
                }
                // All local iterators successful
                return true;
            }
        }
    }
    
    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        if (definition != null) {
            definition.generateDisplayData("label", contentHandler);
        }
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Do nothing
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale, String element, WidgetDefinition definition)
    throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element, attrs);
        generateItemSaxFragment(contentHandler, locale);
        contentHandler.endElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element);
    }
}
