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

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Interface to be implemented by Widgets. In CForms, a form consists of a number
 * of widgets. Each widget:
 *
 * <ul>
 *  <li>has an unique id within its parent context widget. See {@link #getId()}.</li>
 *  <li>can have a parent (see {@link #getParent()}.</li>
 *  <li>can hold a value (which can be any kind of object). See {@link #getValue()}.</li>
 *  <li>can read its value from a request object (and convert it from a string to its native type).
 *  See {@link #readFromRequest(FormContext)}.</li>
 *  <li>can validate itself. See {@link #validate()}.</li>
 *  <li>can generate an XML representation of itself.</li>
 * </ul>
 *
 * <p>When a request is submitted, first the {@link #readFromRequest(FormContext)} method of all widgets
 * will be called so that they can read their value(s). Next, the {@link #validate()} method will
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
 * @version $Id$
 */
public interface Widget extends Locatable {

    /**
     * Widget-Separator used in path-like notations
     * @see #lookupWidget(String)
     */
    char PATH_SEPARATOR = '/';

    /**
     * Called after widget's environment has been setup,
     * to allow for any contextual initalization such as
     * looking up case widgets for union widgets.
     */
    void initialize();

    /**
     * @return  the source location of this widget.
     */
    Location getLocation();

    /**
     * @return the name of this widget.  This should never be <code>null</code>
     * Top-level container widgets (like 'form') should return <code>""</code>
     */
    String getName();

    /**
     * @return the id of this widget.  This should never be <code>null</code>
     * Top-level container widgets (like 'form') should return <code>""</code>
     */
    String getId();

    /**
     * @return the parent of this widget. If this widget is the root widget,
     * this method returns null.
     */
    Widget getParent();

    /**
     * This method is called on a widget when it is added to a container.
     * You shouldn't call this method unless youre implementing a widget yourself (in
     * which case it should be called when a widget is added as child of your widget).
     */
    void setParent(Widget widget);

    /**
     * @return the {@link Form} to which this widget belongs. The form is the top-most ancestor
     * of the widget.
     */
    Form getForm();

    /**
     * Get this widget's definition.
     *
     * @return the widget's definition
     */
    WidgetDefinition getDefinition();
    
    /**
     * Get the widget's own state. Note that this state is <em>not</em> the one actually considered
     * for handling requests and producing output. For these matters, the combined state is used.
     *
     * @see #getCombinedState()
     * @return the widget's own state
     */
    WidgetState getState();

    /**
     * Set the widget's own state. This may change its combined state, and those of its
     * children, if any.
     *
     * @param state the new wiget state
     */
    void setState(WidgetState state);

    /**
     * Get the widget's combined state, which is the strictest of its own state and parent state.
     * This combined state is the one that will be used by the widget to know if request
     * parameters should be considered and if some output must be produced.
     *
     * @see WidgetState#strictest(WidgetState, WidgetState)
     * @return the combined state
     */
    WidgetState getCombinedState();

    /**
     * @return the name prefixed with the namespace, this name should be unique
     * accross all widgets on the form.
     */
    String getFullName();

    /**
     * @return the id prefixed with the namespace, this name should be unique
     * accross all widgets on the form.
     */
    String getRequestParameterName();

    /**
     * @deprecated getWidget got removed, use lookupWidget or getChild instead.
     * @throws UnsupportedOperationException indicating this method has been
     * deprecated from the API, and will be removed from future releases.
     */
    Widget getWidget(String id);

    /**
     * Finds a widget relative to this one based on a path-like
     * string (/-delimted) into the widget-tree structure.
     * This supports '../' and '/' to point to
     * @return the found widget or <code>null</code> if allong the traversal
     *   of the path an invalid section was encountered.
     */
    Widget lookupWidget(String path);

    /**
     * Lets this widget read its data from a request. At this point the Widget
     * may try to convert the request parameter to its native datatype (if it
     * is not a string), but it should not yet generate any validation errors.
     */
    void readFromRequest(FormContext formContext);

    /**
     * Validates this widget and returns the outcome. Possible error messages are
     * remembered by the widget itself and will be part of the XML produced by
     * this widget in its {@link #generateSaxFragment(ContentHandler, Locale)} method.
     *
     * @return <code>true</code> to indicate all validations were ok,
     *         <code>false</code> otherwise
     */
    boolean validate();

    void addValidator(WidgetValidator validator);

    boolean removeValidator(WidgetValidator validator);

    /**
     * Return the current validation state.
     * This method delivers the same result as the last call to {@link #validate()}.
     * The validation process is not started again. If the value of this widget has
     * changed since the latest call to {@link #validate()}, the result of this method
     * is out of date.
     * @return The result of the last call to {@link #validate()}.
     */
    boolean isValid();

    /**
     * Generates an XML representation of this widget. The startDocument and endDocument
     * SAX events will not be called. It is assumed that the prefix for the CForms namespace
     * mentioned in Constants.FI_PREFIX is already declared (by the caller or otherwise).
     */
    void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException;

    /**
     * Generates SAX events for the label of this widget. The label will not be wrapped
     * inside another element.
     */
    void generateLabel(ContentHandler contentHandler) throws SAXException;

    /**
     * Get the value of a widget.
     * <p>
     * Not all widgets do have a value (notably {@link ContainerWidget}s,
     * but this method is provided here as a convenience to ease writing and avoiding casts.
     *
     * @return the value of the widget.
     * @throws UnsupportedOperationException if this widget doesn't have a value.
     */
    Object getValue() throws UnsupportedOperationException;

    /**
     * Sets the value of this widget.
     * <p>
     * Not all widgets do have a value (notably {@link ContainerWidget}s,
     * but this method is provided here as a convenience to ease writing and avoiding casts.
     *
     * @param value the new widget's value.
     * @throws UnsupportedOperationException if this widget doesn't have a value.
     */
    void setValue(Object value) throws UnsupportedOperationException;

    /**
     * @return whether this widget is required to be filled in. As with {@link #getValue()},
     * for some widgets this may not make sense, those should return false here.
     */
    boolean isRequired();

    /**
     * Broadcast an event previously queued by this widget to its event listeners.
     */
    void broadcastEvent(WidgetEvent event);

    /**
     * Retrieves an attribute on this widget.
     *
     * @param name of the attribute to lookup
     * @return the found attribute or <code>null</code> if none was found with that name.
     */
    Object getAttribute(String name);

    /**
     * Sets an attribute on this widget. This can be used to store custom
     * data with each widget.
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the named attribute from this widget.
     *
     * @param name of the attribute
     */
    void removeAttribute(String name);
}
