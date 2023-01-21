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

import org.apache.cocoon.forms.formmodel.library.Library;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A WidgetDefinition holds all the static information about a Widget. It's
 * function is a lot like that of the class in Java. Users of the Cocoon Forms framework
 * usually won't have to bother with the WidgetDefinition's, but will rather use
 * the Widget's themselves.
 *
 * @version $Id$
 */
public interface WidgetDefinition extends Locatable {

    /**
     * Initializes this definition with values from the given other definition
     */
    void initializeFrom(WidgetDefinition definition) throws Exception;

    /**
     * Gets the {@link FormDefinition}.
     */
    FormDefinition getFormDefinition();

    /**
     * Gets the Library object containing this definition
     */
    Library getEnclosingLibrary();

    /**
     * Sets the Library object containing this definition (should only be used by Library itself!)
     */
    void setEnclosingLibrary(Library library);

    /**
     * Sets the parent of this definition.
     */
    void setParent(WidgetDefinition definition);

    /**
     * Gets source location of this widget definition.
     */
    Location getLocation();

    /**
     * Gets id of this widget definition.
     */
    String getId();

    /**
     * Gets an attribute that has been defined on the widget's definition.
     *
     * @param name the attribute name
     * @return the attribute value, or null if it doesn't exist
     */
    Object getAttribute(String name);

    /**
     * Validate a widget using the validators that were defined in its definition. If validation
     * fails, the validator has set a validation error on the widget or one of its children.
     *
     * @param widget the widget
     * @return <code>true</code> if validation was successful.
     */
    boolean validate(Widget widget);

    /**
     * Checks whether this definition is complete since we are allowed to have partial
     * definitions in libraries. Definitions need to be complete _before_ a call to
     * createInstance() though.
     */
    void checkCompleteness() throws IncompletenessException;

    /**
     * Creates and returns a widget based on this widget definition.
     */
    Widget createInstance();

    /**
     * Generates SAX events for named display data.
     */
    void generateDisplayData(String name, ContentHandler contentHandler) throws SAXException;

    /**
     * Generates SAX events for display data.
     */
    void generateDisplayData(ContentHandler contentHandler) throws SAXException;

    /**
     * Generates SAX events for the label of this widget.
     */
    void generateLabel(ContentHandler contentHandler) throws SAXException;
}
