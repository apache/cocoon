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
package org.apache.cocoon.components.elementprocessor.types;

/**
 * Encapsulation of a single XML element attribute in a way that
 * shields the consumer from the data's XML origins.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Attribute.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class Attribute
{
    private String _name;
    private String _value;

    /**
     * Constructor
     *
     * @param name the name of the Attribute, the left hand side of
     *             the '=' of an XML element's attribute.
     * @param value the value of the Attribute, the right hand side of
     *              the '=' of an XML element's attribute.
     *
     * @exception IllegalArgumentException is thrown if name is null
     *            or empty, or if value is null.
     */

    public Attribute(final String name, final String value) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                "Attribute name is null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Attribute value is null");
        }
        _name  = name;
        _value = value;
    }

    /**
     * Get the name of the Attribute.
     *
     * @return the name of the Attribute
     */

    public String getName() {
        return _name;
    }

    /**
     *  Get the value of the Attribute as a String.
     *
     * @return the value of the Attribute as a String
     */

    public String getValue() {
        return _value;
    }

    /**
     * A convenience method to get the value of the Attribute as an
     * int.
     *
     * @return the value of the Attribute as an int
     *
     * @exception NumberFormatException if the value is not an int
     */

    public int getValueAsInt() {
        return Integer.parseInt(_value);
    }

    /**
     * A convenience method to get the value of the Attribute as a
     * short.
     *
     * @return the value of the Attribute as a short
     *
     * @exception NumberFormatException if the value is not a short
     */

    public short getValueAsShort() {
        return Short.parseShort(_value);
    }

    /**
     * A convenience method to get the value of the Attribute as a long.
     *
     * @return the value of the Attribute as a long
     *
     * @exception NumberFormatException if the value is not a long
     */

    public long getValueAsLong() {
        return Long.parseLong(_value);
    }

    /**
     * A convenience method to get the value of the attribute as a
     * boolean. Understands these value strings in a case-insensitive
     * fashion:
     * <ul>
     *     <li>t/f
     *     <li>true/false
     *     <li>y/n
     *     <li>yes/no
     * </ul>
     *
     * @return the value of the Attribute as a boolean
     *
     * @exception IllegalArgumentException if the value does not
     *            represent a boolean
     */

    public boolean getValueAsBoolean() {
        boolean rvalue = false;

        if (_value.equalsIgnoreCase("t") || _value.equalsIgnoreCase("y")
                || _value.equalsIgnoreCase("yes")
                || _value.equalsIgnoreCase("true")) {
            rvalue = true;
        } else if (_value.equalsIgnoreCase("f") || _value.equalsIgnoreCase("n")
                 || _value.equalsIgnoreCase("no")
                 || _value.equalsIgnoreCase("false")) {
            rvalue = false;
        } else {
            throw new IllegalArgumentException(
                "Value [" + _value + "] does not represent a boolean value");
        }
        return rvalue;
    }
}   // end public class Attribute
