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
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for Widget implementations. Provides functionality
 * common to many widgets.
 * 
 * @version $Id: AbstractWidget.java,v 1.16 2004/05/01 00:05:44 joerg Exp $
 */
public abstract class AbstractWidget implements Widget {
    
    /** 
     * Containing parent-widget to this widget.
     * NOTE: avoid directly accessing this member since subclasses can mask this
     * property through own implemented getParent()
     */
    private Widget parent;
    
    /**
     * Lazy loaded reference to the top-level form.
     */
    private Form form;
        
    /**
     * Validation-rules local to the widget instance
     */
    private List validators;

    /**
     * Storage for the widget allocated attributes
     */
    private Map attributes;
 
    
    /**
     * Gets the id of this widget.
     */
    public String getId() {
        return getDefinition().getId();
    }
    
    /** 
     * Concrete subclasses should allow access to their underlaying Definition
     * through this method.
     *
     * If subclasses decide to return <code>null</code> they should also organize 
     * own implementations of {@link #getId()}, {@link #getLocation()},
     * {@link #validate()}, {@link #generateLabel(ContentHandler)} and
     * {@link #generateDisplayData(ContentHandler)} to avoid NPE's.
     * 
     * @return the widgetDefinition from which this widget was instantiated. 
     *        (@link WidgetDefinition#createInstance()}
     */
    protected abstract WidgetDefinition getDefinition();
    
    /**
     * @return the location-information (file, line and column) where this widget was 
     * configured.
     */
    public String getLocation() {
        return getDefinition().getLocation();
    }

    /** 
     * @return The parent-widget of this widget.
     */
    public Widget getParent() {
        return parent;
    }

    /** 
     * Sets the parent-widget of this widget.
     * This is a write-once property.
     * 
     * @param widget the parent-widget of this one.
     * @throws IllegalStateException when the parent had already been set.
     */    
    public void setParent(Widget widget) {
        if (this.parent != null) throw new IllegalStateException("The parent of widget " + getFullyQualifiedId() + " should only be set once.");
        this.parent = widget;
    }

    /**
     * @return the form where this widget belongs to.  
     */
    public Form getForm() {
        Widget myParent = getParent();
        if (this.form == null) {
            if (myParent == null) {
                this.form = (Form)this;
            } else {
                this.form = myParent.getForm();
            }
        }
        return this.form;
    }

    
    //TODO: check why this namespace property exists, it seems to be 
    // deceptively resemblant to the getFullyQualifiedId, 
    // looks like it can be removed, no?
//    public String getNamespace() {    
//        if (getParent() != null && getParent().getNamespace().length() > 0) {
//            return getParent().getNamespace() + "." + getId();
//        } else {
//            return getId();
//        }
//    }

    public String getFullyQualifiedId() {
        Widget myParent = getParent();
        if (myParent != null) {
            String parentFullId = myParent.getFullyQualifiedId();
            // the top level form returns an id == ""
            if (parentFullId.length() > 0) {
                return parentFullId + "." + getId();
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

    /**
     * {@inheritDoc}
     * 
     * Abstract implementation throws a {@link UnsupportedOperationException}.
     * Concrete subclass widgets need to override when supporting event broadcasting.
     */
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
        if (this.validators != null) {
            return this.validators.remove(validator);
        } else {
            return false;
        }
    }
    
    
    public boolean validate() {
        // Test validators from the widget definition
        if (!getDefinition().validate(this)) {
            // Failed
            return false;
        } else {
            // Definition successful, test local validators
            if (this.validators == null) {
                // No local validators
                return true;
            } else {
                // Iterate on local validators
                Iterator iter = this.validators.iterator();
                while(iter.hasNext()) {
                    WidgetValidator validator = (WidgetValidator)iter.next();
                    if (!validator.validate(this)) {
                        return false;
                    }
                }
                // All local iterators successful
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the {@link #getDefinition()} to generate the 'label' part of
     * the display-data of this widget.  
     * 
     * Subclasses should override if the getDefinition can return <code>null</code>
     * to avoid NPE's
     * 
     * @param contentHandler
     * @throws SAXException
     */
    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        getDefinition().generateDisplayData("label", contentHandler);
    }

    
    /**
     * Generates nested additional content nested inside the main element for this 
     * widget which is generated by {@link #generateSaxFragment(ContentHandler, Locale)}
     * 
     * The implementation on the AbstractWidget level inserts no additional XML.
     * Subclasses need to override to insert widget specific content.
     * 
     * @param contentHandler to send the SAX events to
     * @param locale in which context potential content needs to be put.
     * @throws SAXException
     */
    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Do nothing
    }

    /** 
     * The XML element name used in {@link #generateSaxFragment(ContentHandler, Locale)}
     * to produce the wrapping element for all the XML-instance-content of this Widget.
     * 
     * @return the main elementname for this widget's sax-fragment. 
     */
    public abstract String getXMLElementName();

    /** 
     * The XML attributes used in {@link #generateSaxFragment(ContentHandler, Locale)}
     * to be placed on the wrapping element for all the XML-instance-content of this Widget.
     * 
     * This automatically adds @id={@link #getFullyQualifiedId()} to that element.
     * Concrete subclasses should call super.getXMLElementAttributes and possibly
     * add additional attributes.
     * 
     * Note: the @id is not added for those widgets who's getId() returns <code>null</code> 
     * (e.g. top-level container widgets like 'form').  The contract of returning a non-null
     * {@link AttributesImpl} is however maintained.
     * 
     * @return the attributes for the main element for this widget's sax-fragment.
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = new AttributesImpl();
        // top-level widget-containers like forms will have their id set to ""
        // for those the @id should not be included.
        if (getId().length() != 0) {
        	attrs.addCDATAAttribute("id", getFullyQualifiedId());
        }
        return attrs;
    }

    /**
     * Delegates to the {@link #getDefinition()} of this widget to generate a common
     * set of 'display' data. (i.e. help, label, hint,...)
     * 
     * Subclasses should override if the getDefinition can return <code>null</code>
     * to avoid NPE's.
     * 
     * @param contentHandler where to send the SAX events to.
     * @throws SAXException
     * 
     * @see WidgetDefinition#generateDisplayData(ContentHandler) 
     */
    public void generateDisplayData(ContentHandler contentHandler) throws SAXException {
        getDefinition().generateDisplayData(contentHandler);
    }

    /**
     * {@inheritDoc}
     * 
     * This will generate some standard XML consisting of a simple wrapper 
     * element (name provided by {@link #getXMLElementName()}) with attributes
     * (provided by {@link #getXMLElementAttributes()} around anything injected 
     * in by both {@link #generateDisplayData(ContentHandler)} and
     * {@link #generateItemSaxFragment(ContentHandler, Locale)}.
     * 
     * <pre>
     * &lt;fi:{@link #getXMLElementName()} {@link #getXMLElementAttributes()} &gt;
     *   {@link #generateDisplayData(ContentHandler)} (i.e. help, label, ...)
     * 
     *   {@link #generateItemSaxFragment(ContentHandler, Locale)}
     * &lt;/fi:{@link #getXMLElementName()} &gt; 
     * </pre>
     * 
     * @param contentHandler to send the SAX events to
     * @param locale in which context potential content needs to be put.
     * @throws SAXException 
     */
    public void generateSaxFragment(ContentHandler contentHandler, Locale locale)    
    throws SAXException {
        String element = this.getXMLElementName();       
        AttributesImpl attrs = getXMLElementAttributes();
        contentHandler.startElement(Constants.INSTANCE_NS, element, Constants.INSTANCE_PREFIX_COLON + element, attrs);

        generateDisplayData(contentHandler);
        
        generateItemSaxFragment(contentHandler, locale);
        
        contentHandler.endElement(Constants.INSTANCE_NS, element, Constants.INSTANCE_PREFIX_COLON + element);
    }

	public Object getAttribute(String name) {
        if (this.attributes != null){
            return this.attributes.get(name);
        } else{
            return null;
        }
    }

    public void setAttribute(String name, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        if (this.attributes != null) {
            this.attributes.remove(name);
        }
    }
}
