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
 * @version CVS $Id: AbstractElementProcessorFactory.java,v 1.3 2004/01/31 08:50:44 antonio Exp $
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
