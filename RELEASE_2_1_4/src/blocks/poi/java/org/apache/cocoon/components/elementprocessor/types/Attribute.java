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
package org.apache.cocoon.components.elementprocessor.types;

/**
 * Encapsulation of a single XML element attribute in a way that
 * shields the consumer from the data's XML origins.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Attribute.java,v 1.3 2004/01/31 08:50:43 antonio Exp $
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
