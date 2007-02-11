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
package org.apache.cocoon.components.elementprocessor.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.components.elementprocessor.CannotCreateElementProcessorException;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.ElementProcessorFactory;

/**
 * Create instances of specific ElementProcessor implementations to
 * handle specific XML elements and their content.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: AbstractElementProcessorFactory.java,v 1.4 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public abstract class AbstractElementProcessorFactory
     extends AbstractLogEnabled implements ElementProcessorFactory, Component
{

    // uses XML element names as keys and ElementProcessor progenitors
    // as values. An ElementProcessor progenitor is an Object that can
    // be used to create a new ElementProcessor instance that is
    // specific to a particular XML element. A progenitor may be a
    // Constructor or Class that can construct a new instance of an
    // appropriate ElementProcessor implementation, or any other
    // Object that an extension of AbstractElementProcessorFactory finds
    // useful to create new ElementProcessor instances.
    private Map _element_processor_map;

    /**
     * Protected default constructor
     */

    protected AbstractElementProcessorFactory() {
        _element_processor_map = new HashMap();
    }

    /**
     * Given an XML element name, create and return an appropriate
     * ElementProcessor.
     *
     * @param name element name
     *
     * @return the specified ElementProcessor
     *
     * @exception CannotCreateElementProcessorException if there is no
     *            ElementProcessor available for the specified name
     */

    public ElementProcessor createElementProcessor(final String name)
        throws CannotCreateElementProcessorException {
        Object progenitor = lookupElementProcessorProgenitor(name);

        if (progenitor == null) {
            CannotCreateElementProcessorException exception =
                new CannotCreateElementProcessorException(
                    "Cannot find progenitor for that name");
            exception.setElementName(name);
            throw exception;
        }
        ElementProcessor processor = null;

        try {
            processor = doCreateElementProcessor(progenitor);
        } catch (CannotCreateElementProcessorException e) {
            e.setElementName(name);
            throw e;
        }
        return processor;
    }

    /**
     * A method for extending classes to populate the map.
     *
     * @param name the element name for this progenitor; cannot be
     *             null ot empty
     * @param progenitor an object that can be used to generate an
     *                   appropriate ElementProcessor; cannot be nukk
     *
     * @exception IllegalArgumentException if name is already in the
     *            map or progenitor is null.
     */

    protected void addElementProcessorProgenitor(final String name,
            final Object progenitor)
    {
        if ((name == null) || (name.equals(""))) {
            throw new IllegalArgumentException(
                "Cannot use null or empty name as a key");
        }
        if (progenitor == null) {
            throw new IllegalArgumentException(
                "Cannot add null progenitor to the map");
        }
        if (_element_processor_map.put(name, progenitor) != null) {
            throw new IllegalArgumentException(
                name + " is already in use in the map");
        }
    }

    /**
     * A method to get the progenitor value associated with a
     * specified element name.
     *
     * @param name the element name
     *
     * @return the associated ElementProcessor progenitor; will be
     *         null if the element name has not yet been associated
     *         with a progenitor.
     */

    protected Object lookupElementProcessorProgenitor(final String name)
    {
        Object obj = _element_processor_map.get(name);
        if (obj == null && !name.equals("*")) {
            obj = lookupElementProcessorProgenitor("*");
      	}
        return obj;
    }

    /**
     * The method that a concrete extension of AbstractElementProcessorFactory
     * must implement. When this method is called, the element name
     * has already been looked up in the map and a progenitor Object
     * has been acquired. The progenitor is guaranteed not to be null.
     *
     * @param progenitor the object from which to create an
     *                   ElementProcessor
     *
     * @return freshly created ElementProcessor
     *
     * @exception CannotCreateElementProcessorException if the
     *            specified ElementProcessor cannot be created.
     */

    protected abstract ElementProcessor doCreateElementProcessor(
        final Object progenitor) throws CannotCreateElementProcessorException;

    /**
     * A reference implementation of doCreateElementProcessor that can
     * be used by an extending class whose progenitors are Class
     * objects for ElementProcessor implementations.
     *
     * @param progenitor a Class representing an ElementProcessor
     *
     * @return the new ElementProcessor instance
     *
     * @exception CannotCreateElementProcessorException if the
     *            ElementProcessor cannot be created.
     */

    protected ElementProcessor createNewElementProcessorInstance(
            final Class progenitor)
            throws CannotCreateElementProcessorException {
        ElementProcessor rval = null;

        try {
            rval = (ElementProcessor)progenitor.newInstance();
            if (rval instanceof AbstractLogEnabled) {
               ((AbstractLogEnabled)rval).enableLogging(getLogger());
            }
        } catch (ExceptionInInitializerError e) {
            throw new CannotCreateElementProcessorException(
                    "an exception (" + e + ") occurred in initializing the associated ElementProcessor class");
        } catch (SecurityException e) {
            throw new CannotCreateElementProcessorException(
                "a security exception was caught while creating the associated ElementProcessor");
        } catch (InstantiationException e) {
            throw new CannotCreateElementProcessorException(
                "associated ElementProcessor is an interface or abstract class or has no zero-parameter constructor");
        } catch (IllegalAccessException e) {
            throw new CannotCreateElementProcessorException(
                "cannot access ElementProcessor class or its zero-parameter constructor");
        } catch (ClassCastException e) {
            throw new CannotCreateElementProcessorException(
                "object created does not implement ElementProcessor");
        } catch (Exception e) {
            throw new CannotCreateElementProcessorException(
                "exception (" + e
                + ") occured while creating new instance of ElementProcessor");
        }
        if (rval == null) {
            throw new CannotCreateElementProcessorException(
                "somehow generated a null ElementProcessor");
        }
        return rval;
    }

    /**
     * A reference implementation of doCreateElementProcessor that can
     * be used by an extending class whose progenitors are Constructor
     * objects that can create new instances of ElementProcessor
     * implementations.
     *
     * @param progenitor a Constructor of an ElementProcessor
     *
     * @return the newly created ElementProcessor
     *
     * @exception CannotCreateElementProcessorException if the
     *            ElementProcessor cannot be created.
     */

    protected ElementProcessor constructElementProcessor(
            final Constructor progenitor)
            throws CannotCreateElementProcessorException {
        ElementProcessor rval = null;

        try {
            rval = (ElementProcessor) progenitor.newInstance(new Object[0]);
            if (rval instanceof AbstractLogEnabled) {
               ((AbstractLogEnabled)rval).enableLogging(getLogger());
            }
        } catch (ExceptionInInitializerError e) {
            throw new CannotCreateElementProcessorException(
                "an exception (" + e
                + ")occurred in initializing the associated ElementProcessor class");
        } catch (IllegalArgumentException e) {
            throw new CannotCreateElementProcessorException(
                "the ElementProcessor constructor apparently needs parameters");
        } catch (InstantiationException e) {
            throw new CannotCreateElementProcessorException(
                "associated ElementProcessor is an interface or abstract class");
        } catch (IllegalAccessException e) {
            throw new CannotCreateElementProcessorException(
                "cannot access ElementProcessor class or its zero-parameter constructor");
        } catch (InvocationTargetException e) {
            throw new CannotCreateElementProcessorException(
                "ElementProcessor constructor threw an exception ["
                + e.toString() + "]");
        } catch (ClassCastException e) {
            throw new CannotCreateElementProcessorException(
                "object created does not implement ElementProcessor");
        }
        if (rval == null) {
            throw new CannotCreateElementProcessorException(
                "somehow generated a null ElementProcessor");
        }
        return rval;
    }

}       // end public abstract class AbstractElementProcessorFactory
