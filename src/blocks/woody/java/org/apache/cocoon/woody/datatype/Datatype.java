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
package org.apache.cocoon.woody.datatype;

import org.outerj.expression.ExpressionContext;
import org.apache.cocoon.woody.datatype.convertor.Convertor;

import java.util.Locale;

/**
 * A Datatype encapsulates the functionality for working with specific
 * kinds of data like integers, deciamals or dates.
 *
 * <p>I provides:
 * <ul>
 *  <li>Methods for converting between String and Object representation of the data
 *  <li>For validating the data (usually against a set of validation rules)
 *  <li>optionally a selection list
 * </ul>
 *
 * <p>Each datatype can be marked as an "arraytype". Currently, this only has an
 * influence on the {@link #validate} method, which should in that case be passed
 * an array of objects. See also {@link #isArrayType}.
 * 
 * @version $Id: Datatype.java,v 1.5 2004/02/11 09:53:43 antonio Exp $
 */
public interface Datatype {
    /**
     * Converts a string to an object of this datatype. Returns null if this
     * fails. This method uses the same {@link Convertor} as returned by the
     * {@link #getConvertor} method.
     */
    Object convertFromString(String value, Locale locale);

    /**
     * Converts an object of this datatype to a string representation.
     * This method uses the same {@link Convertor} as returned by the
     * {@link #getConvertor} method.
     */
    String convertToString(Object value, Locale locale);

    /**
     * Returns null if validation is successful, otherwise returns a
     * {@link ValidationError} instance.
     *
     * @param value an Object of the correct type for this datatype (see {@link #getTypeClass}, or
     * if {@link #isArrayType} returns true, an array of objects of that type.
     */
    ValidationError validate(Object value, ExpressionContext expressionContext);

    /**
     * Gets the class object for the type represented by this datatype. E.g. Long, String, ...
     * The objects returned from the convertFromString* methods are of this type, and the object
     * passed to the convertToString* or validate methods should be of this type.
     */
    Class getTypeClass();

    /**
     * Returns a descriptive name for the base type of this datatype,
     * i.e. something like 'string', 'long', 'decimal', ...
     */
    String getDescriptiveName();

    /**
     * Indicates wether this datatype represents an array type. This approach has been
     * chosen instead of creating a seperate ArrayDatatype interface (and corresponding
     * implementations), since almost all functionality is the same between the scalar
     * and array types. The main difference is that the validate() method will be passed
     * arrays instead of single values, and hence different validation rules will be
     * required.
     */
    boolean isArrayType();

    /**
     * Returns the convertor used by this datatype.
     */
    Convertor getConvertor();

    /**
     * Returns the "plain convertor". This is convertor that should have a locale-independent
     * string encoding, and guarantees perfect roundtripping. It is used if a value of this
     * datatype needs to be stored but not displayed to the user.
     */
    Convertor getPlainConvertor();

    /**
     * Returns the factory that built this datatype.
     */
    DatatypeBuilder getBuilder();
}
