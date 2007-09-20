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

import org.apache.commons.lang.enums.ValuedEnum;

/**
 * The state of a widget. States are ordered from the most featured ("active")
 * to the most constrained ("invisible"), so that state combinations can be
 * computed: a widget's combined state is the strictest between the widget's own
 * state and its parent state.
 * 
 * @version $Id$
 */
public class WidgetState extends ValuedEnum {

    private static final int ACTIVE_VALUE = 4;
    
    private static final int DISABLED_VALUE = 3;

    private static final int OUTPUT_VALUE = 2;

    private static final int INVISIBLE_VALUE = 1;

    /**
     * Active state. This is the default state, where widgets read their values
     * from the request and display them.
     */
    public static final WidgetState ACTIVE = new WidgetState("active", ACTIVE_VALUE);

    /**
     * Disabled state, value is displayed but user input is ignored. The widget should be
     * rendered in a manner that indicates that this widget could be active, but is currently not.
     */
    public static final WidgetState DISABLED = new WidgetState("disabled", DISABLED_VALUE);
    
    /**
     * Output state, value is displayed but user input is ignored. The widget should be rendered
     * as plain text, giving no indication that it could be input.
     */
    public static final WidgetState OUTPUT = new WidgetState("output", OUTPUT_VALUE);

    /**
     * Invisible state. Values are not displayed and user input is ignored.
     */
    public static final WidgetState INVISIBLE = new WidgetState("invisible", INVISIBLE_VALUE);

    /**
     * Private constructor
     */
    private WidgetState(String name, int value) {
        super(name, value);
    }

    /**
     * Get a state given its name. Valid names are "active", "disabled",
     * "invisible".
     * 
     * @param name the state name
     * @return the state, or <code>null</code> if <code>name</code> doesn't
     *         denote a known state name
     */
    public static WidgetState stateForName(String name) {
        return (WidgetState) getEnum(WidgetState.class, name);
    }

    /**
     * Determine the strictest of two states. "invisible" is stricter than
     * "disabled" which is stricter than "active"
     * 
     * @param one a state
     * @param two another state
     * @return the strictes of <code>one</code> and <code>two</code>
     */
    public static WidgetState strictest(WidgetState one, WidgetState two) {
        return (one.getValue() < two.getValue()) ? one : two;
    }

    /**
     * Test if the current state is stricter than another one.
     * 
     * @param other a state
     * @return <code>true</code> if <code>this</code> is stricter
     *         than <code>other</code>
     */
    public boolean stricterThan(WidgetState other) {
        return this.getValue() < other.getValue();
    }

    /**
     * Does this state accept user inputs?
     * 
     * @return <code>true</code> if this state accepts user inputs.
     */
    public boolean isAcceptingInputs() {
        return this.getValue() == ACTIVE_VALUE;
    }

    /**
     * Does this state display widget values?
     * 
     * @return <code>true</code> if this state displays widget values.
     */
    public boolean isDisplayingValues() {
        return this.getValue() > INVISIBLE_VALUE;
    }

    /**
     * Does this state validate widget values?
     * 
     * @return <code>true</code> if this state validates widget values.
     */
    public boolean isValidatingValues() {
        return this.getValue() == ACTIVE_VALUE;
    }

// Potential features provided by ValuedEnum that don't seem to be needed now
//
//    public static WidgetState stateForValue(int stateValue) {
//        return (WidgetState) getEnum(WidgetState.class, stateValue);
//    }
//
//    public static Map getEnumMap() {
//        return getEnumMap(WidgetState.class);
//    }
//
//    public static List getStateList() {
//        return getEnumList(WidgetState.class);
//    }
//
//    public static Iterator iterator() {
//        return iterator(WidgetState.class);
//    }

}
