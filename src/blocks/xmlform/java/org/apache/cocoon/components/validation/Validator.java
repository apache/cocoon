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
package org.apache.cocoon.components.validation;

import java.util.List;

/**
 *
 * Created on Sat, April 6, 2002
 *
 * @author  ivelin@apache.org
 * @version CVS $Id: Validator.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public interface Validator {

    /**
     * Validates an instance against a schema and returns a set of errors.
     *
     * Validator is not thread safe and is not re-entrant.
     *
     * @param instance The instance can be either a DOM node or a JavaBean.
     * @return SortedSet of ValidityViolation(s). The set is sorted by
     * ValidityViolation.getPath()
     *
     */
    List validate(Object instance);

    /**
     * This property can be used for partial document validation.
     * The concept is borrowed from the Schematron schema
     * Not all schemas support partial validation
     */
    String PROPERTY_PHASE = "http://xml.apache.org/cocoon/validator/phase";

    /**
     * @param property name
     * @param value property value
     * @throws IllegalArgumentException when the property is not supported
     */
    void setProperty(String property,
                     Object value) throws IllegalArgumentException;

    /**
     * @param property name
     * @return the property value
     * @throws IllegalArgumentException when the property is not supported
     */
    Object getProperty(String property) throws IllegalArgumentException;

}
