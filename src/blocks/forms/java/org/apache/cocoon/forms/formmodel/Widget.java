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

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Interface to be implemented by Widgets. In CForms, a form consists of a number
 * of widgets. Each widget:
 *
 * <ul>
 *  <li>has an id, unique within its parent context widget. See {@link #getId()}.</li>
 *  <li>can have children (see {@link #getWidget(String)}, and can have a parent (see {@link #getParent()}.</li>
 *  <li>can hold a value (which can be any kind of object). See {@link #getValue()}.</li>
 *  <li>can read its value from a request object (and convert it from a string to its native type).
 *  See {@link #readFromRequest(FormContext)}.</li>
 *  <li>can validate itself. See {@link #validate(FormContext)}.</li>
 *  <li>can generate an XML representation of itself.</li>
 * </ul>
 *
 * <p>Because widgets can have children, the widgets form a widget tree, with its root
 * being the {@link Form} widget.</p>
 *
 * <p>A widget can have only a value, or only child widgets, or can have both a value and child
 * widgets, or can have neither. This all depends on the widget implementation.</p>
 *
 * <p>When a request is submitted, first the {@link #readFromRequest(FormContext)} method of all widgets
 * will be called so that they can read their value(s). Next, the {@link #validate(FormContext)} method will
 * be called. Doing this in two steps allows the validation to compare values between widgets.
 * See also the method {@link Form#process(FormContext)}.</p>
 *
 * <p>A Widget is created by calling the createInstance method on the a
 * {@link WidgetDefinition}. A Widget holds all the data that is specific for
 * a certain use of the widget (its value, validationerrors, ...), while the
 * WidgetDefinition holds the data that is static accross all widgets. This
 * keeps the Widgets small and light to create. This mechanism is similar to
 * classes and objects in Java.
 * 
 * @version CVS $Id: Widget.java,v 1.4 2004/04/20 22:19:27 mpo Exp $
 */
public interface Widget {

    /**
     * Gets the source location of this widget.
     */
    public String getLocation();

    /**
     * Returns the id of this widget.
     */
    public String getId();

    /**
     * Gets the parent of this widget. If this widget is the root widget,
     * this method returns null.
     */
    public Widget getParent();

    /**
     * This method is called on a widget when it is added to a container.
     * You shouldn't call this method unless youre implementing a widget yourself (in
     * which case it should be called when a widget is added as child of your widget).
     */
    public void setParent(Widget widget);
    
    /**
     * Get the {@link Form} to which this widget belongs. The form is the top-most ancestor
     * of the widget.
     */
    public Form getForm();

    /**
     * Gets the namespace of this widget. The combination of a widget's namespace
     * with its id (see {@link #getId()} gives the widget a form-wide unique name.
     * In practice, the namespace consists of the id's of the widget's parent widgets,
     * separated by dots.
     */
    public String getNamespace();

    /**
     * Returns the id prefixed with the namespace, this name should be unique
     * accross all widgets on the form.
     */
    public String getFullyQualifiedId();

    /**
     * Lets this widget read its data from a request. At this point the Widget
     * may try to convert the request parameter to its native datatype (if it
     * is not a string), but it should not yet generate any validation errors.
     */
    public void readFromRequest(FormContext formContext);

    /**
     * Validates this widget and returns the outcome. Possible error messages are
     * remembered by the widget itself and will be part of the XML produced by
     * this widget in its {@link #generateSaxFragment(ContentHandler, Locale)} method.
     */
    public boolean validate(FormContext formContext);

    /**
     * Generates an XML representation of this widget. The startDocument and endDocument
     * SAX events will not be called. It is assumed that the prefix for the CForms namespace
     * mentioned in Constants.FI_PREFIX is already declared (by the caller or otherwise).
     */
    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException;

    /**
     * Generates SAX events for the label of this widget. The label will not be wrapped
     * inside another element.
     */
    public void generateLabel(ContentHandler contentHandler) throws SAXException;

    /**
     * Returns the value of the widget. For some widgets (notably ContainerWidgets)
     * this may not make sense, those should then simply return null here.
     */
    public Object getValue();
    
    /**
     * Sets the value of this widget to the given object. Some widgets may not support this
     * method, those should throw an runtime exception if you try to set their value anyway.
     */
    public void setValue(Object object);

    /**
     * Returns wether this widget is required to be filled in. As with {@link #getValue()}, for some
     * widgets this may not make sense, those should return false here.
     */
    public boolean isRequired();

    /**
     * Gets the child widget of this widget with the given id, or null if there isn't such a child.
     */
    //TODO: this is duplicated on the ContainerWidget interface
    // and in fact only seems to be reasonable in that scope
    // should we not remove this?
    public Widget getWidget(String id);
    
    /**
     * Broadcast an event previously queued by this widget to its event listeners.
     */
    public void broadcastEvent(WidgetEvent event);

    /**
     * Retrieves an attribute on this widget
     * 
     * @param name of the attribute to lookup
     * @return the found attribute or <code>null</code> if none was found with that name.
     */
    public Object getAttribute(String name);

    /**
     * Sets an attribute on this widget. This can be used to store custom
     * data with each widget.
     */
    public void setAttribute(String name, Object value);

    /**
     * Removes the named attribute from this widget.
     * 
     * @param name of the attribute
     */
    public void removeAttribute(String name);
}
