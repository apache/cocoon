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

import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Interface to be implemented by Widgets. In Woody, a form consists of a number
 * of widgets. Each widget:
 *
 * <ul>
 *  <li>has an id, unique within its parent context widget. See {@link #getId}.</li>
 *  <li>can have children (see {@link #getWidget}, and can have a parent (see {@link #getParent}.</li>
 *  <li>can hold a value (which can be any kind of object). See {@link #getValue}.</li>
 *  <li>can read its value from a request object (and convert it from a string to its native type).
 *  See {@link #readFromRequest}.</li>
 *  <li>can validate itself. See {@link #validate}.</li>
 *  <li>can generate an XML representation of itself.</li>
 * </ul>
 *
 * <p>Because widgets can have children, the widgets form a widget tree, with its root
 * being the {@link Form} widget.</p>
 *
 * <p>A widget can have only a value, or only child widgets, or can have both a value and child
 * widgets, or can have neither. This all depends on the widget implementation.</p>
 *
 * <p>When a request is submitted, first the {@link #readFromRequest} method of all widgets
 * will be called so that they can read their value(s). Next, the {@link #validate} method will
 * be called. Doing this in two steps allows the validation to compare values between widgets.
 * See also the method {@link Form#process}.</p>
 *
 * <p>A Widget is created by calling the createInstance method on the a
 * {@link WidgetDefinition}. A Widget holds all the data that is specific for
 * a certain use of the widget (its value, validationerrors, ...), while the
 * WidgetDefinition holds the data that is static accross all widgets. This
 * keeps the Widgets small and light to create. This mechanism is similar to
 * classes and objects in Java.
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
     * with its id (see {@link #getId} gives the widget a form-wide unique name.
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
     * this widget in its {@link #generateSaxFragment} method.
     */
    public boolean validate(FormContext formContext);

    /**
     * Generates an XML representation of this widget. The startDocument and endDocument
     * SAX events will not be called. It is assumed that the prefix for the Woody namespace
     * mentioned in Constants.WI_PREFIX is already declared (by the caller or otherwise).
     */
    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException;

    /**
     * Generates SAX events for the label of this widget. The label will not be wrapped
     * inside another element.
     */
    public void generateLabel(ContentHandler contentHandler) throws SAXException;

    /**
     * Returns the value of the widget. For some widgets (notably ContainerWidgets) this may not make sense, those
     * should then simply return null here.
     * <p>
     * Object having a value, but whose value is invalid, will return 
     * {@link org.apache.cocoon.woody.Constants#INVALID_VALUE}.
     */
    public Object getValue();
    
    /**
     * Sets the value of this widget to the given object. Some widgets may not support this
     * method, those should throw an runtime exception if you try to set their value anyway.
     */
    public void setValue(Object object);

    /**
     * Returns wether this widget is required to be filled in. As with {@link #getValue}, for some
     * widgets this may not make sense, those should return false here.
     */
    public boolean isRequired();

    /**
     * Gets the child widget of this widget with the given id, or null if there isn't such a child.
     */
    public Widget getWidget(String id);
    
    /**
     * Broadcast an event previously queued by this widget to its event listeners.
     */
    public void broadcastEvent(WidgetEvent event);
}
