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
 * influence on the {@link #validate(Object, ExpressionContext)} method, which should in that case be passed
 * an array of objects. See also {@link #isArrayType()}.
 * 
 * @version $Id: Datatype.java,v 1.7 2004/03/05 13:02:28 bdelacretaz Exp $
 */
public interface Datatype {
    /**
     * Converts a string to an object of this datatype. Returns null if this
     * fails. This method uses the same {@link Convertor} as returned by the
     * {@link #getConvertor()} method.
     */
    Object convertFromString(String value, Locale locale);

    /**
     * Converts an object of this datatype to a string representation.
     * This method uses the same {@link Convertor} as returned by the
     * {@link #getConvertor()} method.
     */
    String convertToString(Object value, Locale locale);

    /**
     * Returns null if validation is successful, otherwise returns a
     * {@link ValidationError} instance.
     *
     * @param value  an Object of the correct type for this datatype (see
     *               {@link #getTypeClass()}, or if {@link #isArrayType()}
     *               returns true, an array of objects of that type.
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
