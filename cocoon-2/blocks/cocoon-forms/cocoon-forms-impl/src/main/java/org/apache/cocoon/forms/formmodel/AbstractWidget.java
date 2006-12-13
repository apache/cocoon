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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.CreateEvent;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.xml.AttributesImpl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for Widget implementations. Provides functionality
 * common to many widgets.
 *
 * @version $Id$
 */
public abstract class AbstractWidget implements Widget {

    /**
     * Containing parent-widget to this widget.
     * NOTE: avoid directly accessing this member since subclasses can mask this
     * property through own implemented getParent()
     */
    private Widget parent;

    /**
     * The widget's own state
     */
    private WidgetState state = WidgetState.ACTIVE;

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
     * The result of the last call to {@link #validate()}.
     */
    protected boolean wasValid = true;


    protected AbstractWidget(AbstractWidgetDefinition definition) {
        this.state = definition.getState();
    }

    /**
     * Called after widget's environment has been setup,
     * to allow for any contextual initalization, such as
     * looking up case widgets for union widgets.
     */
    public void initialize() {
        ((AbstractWidgetDefinition)getDefinition()).widgetCreated(this);
    }

    /**
     * Gets the id of this widget.
     */
    public String getId() {
        return getDefinition().getId();
    }

    public String getName() {
        return getId();
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
     *        (See {@link org.apache.cocoon.forms.formmodel.WidgetDefinition#createInstance()})
     */
    public abstract WidgetDefinition getDefinition();

    /**
     * @return the location-information (file, line and column) where this widget was
     * configured.
     */
    public Location getLocation() {
        return getDefinition().getLocation();
    }

    /**
     * @return The parent-widget of this widget.
     */
    // This method is final in order for other methods in this class to use this.parent
    public final Widget getParent() {
        return this.parent;
    }

    /**
     * Sets the parent-widget of this widget.
     * This is a write-once property.
     *
     * @param widget the parent-widget of this one.
     * @throws IllegalStateException when the parent had already been set.
     */
    public void setParent(Widget widget) {
        if (this.parent != null) {
            throw new IllegalStateException("The parent of widget " + getRequestParameterName() + " should only be set once.");
        }
        this.parent = widget;
    }

    /**
     * @return the form where this widget belongs to.
     */
    public Form getForm() {
        if (this.form == null) {
            Widget myParent = getParent();
            if (myParent == null) {
                this.form = (Form)this;
            } else {
                this.form = myParent.getForm();
            }
        }
        return this.form;
    }

    public WidgetState getState() {
        return this.state;
    }

    public void setState(WidgetState state) {
        if (state == null) {
            throw new IllegalArgumentException("A widget state cannot be set to null");
        }
        this.state = state;

        // Update the browser
        getForm().addWidgetUpdate(this);
    }

    public WidgetState getCombinedState() {
        if (this.parent == null) {
            return this.state;
        }
        return WidgetState.strictest(this.state, this.parent.getCombinedState());
    }

    // Cached param names, used to speed up execution of the method below while
    // still allowing ids to change (e.g. repeater rows when they are reordered).
    private String cachedParentParamName;
    private String cachedParamName;

    /**
     * Should be called when a widget's own name has changed, in order to clear
     * internal caches used to compute request parameters.
     */
    protected void widgetNameChanged() {
        this.cachedParentParamName = null;
        this.cachedParamName = null;
    }

    public String getFullName() {
        return getRequestParameterName();
    }

    public String getRequestParameterName() {

        if (this.parent == null) {
            return getId();
        }

        String parentParamName = parent.getRequestParameterName();
        if (parentParamName.equals(this.cachedParentParamName)) {
            // Parent name hasn't changed, so ours hasn't changed too
            return this.cachedParamName;
        }

        // Compute our name and cache it
        this.cachedParentParamName = parentParamName;
        if (this.cachedParentParamName.length() == 0) {
            // the top level form returns an id == ""
            this.cachedParamName = getId();
        } else {
            this.cachedParamName = this.cachedParentParamName + "." + getId();
        }

        return this.cachedParamName;
    }

    public Widget lookupWidget(String path) {

        if (path == null || path.length() == 0) {
            return this;
        }

        Widget relativeWidget;
        String relativePath;
        int sepPosition = path.indexOf("" + Widget.PATH_SEPARATOR);

        if (sepPosition < 0) {
            //last step
            if (path.startsWith("..")) return getParent();
            return getChild(path);
        } else if (sepPosition == 0) {
            //absolute path
            relativeWidget = getForm();
            relativePath = path.substring(1);
        } else {
            if (path.startsWith(".." + Widget.PATH_SEPARATOR))  {
                relativeWidget = getParent();
                relativePath = path.substring(3);
            } else {
                String childId = path.substring(0, sepPosition );
                relativeWidget = getChild(childId);
                relativePath = path.substring(sepPosition+1);
            }
        }

        if (relativeWidget == null) {
            return null;
        }

        return relativeWidget.lookupWidget(relativePath);
    }

    /**
     * Concrete widgets that contain actual child widgets should override to
     * return the actual child-widget.
     *
     * @param id of the child-widget
     * @return <code>null</code> if not overriden.
     */
    protected Widget getChild(String id) {
        return null;
    }

    public Widget getWidget(String id) {
        throw new UnsupportedOperationException("getWidget(id) got deprecated from the API. \n" +
                                                "Consider using getChild(id) or even lookupWidget(path) instead.");
    }

    public Object getValue() {
        throw new UnsupportedOperationException("Widget " + this + " has no value, at " + getLocation());
    }

    public void setValue(Object object) {
        throw new UnsupportedOperationException("Widget " + this + " has no value, at " + getLocation());
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
        if (event instanceof CreateEvent) {
            ((AbstractWidgetDefinition) getDefinition()).fireCreateEvent((CreateEvent) event);
        } else {
            throw new UnsupportedOperationException("Widget " + getRequestParameterName() + " doesn't handle events.");
        }
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
        }
        return false;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#validate()
     */
    public boolean validate() {
        // Consider widget valid if it is not validating values.
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

        // Test validators from the widget definition
        if (!getDefinition().validate(this)) {
            // Failed
            this.wasValid = false;
            return false;
        }
        // Definition successful, test local validators
        if (this.validators != null) {
            Iterator iter = this.validators.iterator();
            while(iter.hasNext()) {
                WidgetValidator validator = (WidgetValidator)iter.next();
                if (!validator.validate(this)) {
                    this.wasValid = false;
                    return false;
                }
            }
        }

        // Successful validation

        if (this instanceof ValidationErrorAware) {
            // Clear validation error if any
            ((ValidationErrorAware)this).setValidationError(null);
        }

        this.wasValid = true;
        return true;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#isValid()
     */
    public boolean isValid() {
        return this.wasValid;
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
        if (getCombinedState().isDisplayingValues()) {
            getDefinition().generateDisplayData("label", contentHandler);
        }
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
    protected void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Do nothing
    }

    /**
     * The XML element name used in {@link #generateSaxFragment(ContentHandler, Locale)}
     * to produce the wrapping element for all the XML-instance-content of this Widget.
     *
     * @return the main elementname for this widget's sax-fragment.
     */
    protected abstract String getXMLElementName();

    /**
     * The XML attributes used in {@link #generateSaxFragment(ContentHandler, Locale)}
     * to be placed on the wrapping element for all the XML-instance-content of this Widget.
     *
     * This automatically adds @id={@link #getRequestParameterName()} to that element.
     * Concrete subclasses should call super.getXMLElementAttributes and possibly
     * add additional attributes.
     *
     * Note: the @id is not added for those widgets who's getId() returns <code>null</code>
     * (e.g. top-level container widgets like 'form').  The contract of returning a non-null
     * {@link AttributesImpl} is however maintained.
     *
     * @return the attributes for the main element for this widget's sax-fragment.
     */
    protected AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = new AttributesImpl();
        // top-level widget-containers like forms might have their id set to ""
        // for those the @id should not be included.
        if (getId().length() != 0) {
            attrs.addCDATAAttribute("id", getRequestParameterName());
        }

        // Add the "state" attribute
        attrs.addCDATAAttribute("state", getCombinedState().getName());

        // Add the "listening" attribute is the value has change listeners
        if (this instanceof ValueChangedListenerEnabled &&
            ((ValueChangedListenerEnabled)this).hasValueChangedListeners()) {
            attrs.addCDATAAttribute("listening", "true");
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
    protected void generateDisplayData(ContentHandler contentHandler) throws SAXException {
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

        if (getCombinedState().isDisplayingValues()) {
            // FIXME: we may want to strip out completely widgets that aren't updated when in AJAX mode
            String element = this.getXMLElementName();
            AttributesImpl attrs = getXMLElementAttributes();
            contentHandler.startElement(FormsConstants.INSTANCE_NS, element, FormsConstants.INSTANCE_PREFIX_COLON + element, attrs);

            generateDisplayData(contentHandler);

            if (locale == null) {
                locale = getForm().getLocale();
            }

            generateItemSaxFragment(contentHandler, locale);

            contentHandler.endElement(FormsConstants.INSTANCE_NS, element, FormsConstants.INSTANCE_PREFIX_COLON + element);

        } else {
            // Generate a placeholder that can be used later by AJAX updates
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", getRequestParameterName());
            contentHandler.startElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder", attrs);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder");
        }
    }

    public Object getAttribute(String name) {
        Object result = null;

        // First check locally
        if (this.attributes != null) {
            result = this.attributes.get(name);
        }

        // Fall back to the definition's attributes
        if (result == null) {
            result = getDefinition().getAttribute(name);
        }

        return result;
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

    public String toString() {
        String className = this.getClass().getName();
        int last = className.lastIndexOf('.');
        if (last != -1) {
            className = className.substring(last+1);
        }

        String name = getRequestParameterName();
        return name.length() == 0 ? className : className + " '" + getRequestParameterName() + "'";
    }
}
