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
package org.apache.cocoon.components.elementprocessor;

import org.apache.avalon.framework.component.Component;

/**
 * Create instances of specific ElementProcessor implementations to
 * handle specific XML elements and their content.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: ElementProcessorFactory.java,v 1.4 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public interface ElementProcessorFactory extends Component
{
    String ROLE = ElementProcessorFactory.class.getName();

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
        throws CannotCreateElementProcessorException;

}       // end public interface ElementProcessorFactory
