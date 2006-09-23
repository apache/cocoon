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
package org.apache.cocoon.components.elementprocessor;

/**
 * Exception to be thrown when an ElementProcessor cannot be created.
 *
 * @version $Id$
 */
public class CannotCreateElementProcessorException
        extends Exception
{
    private String _element_name;
    private String _reason;

    /**
     * Constructor
     *
     * @param reason a simple explanation why the specified
     *               ElementProcessor could not be created.
     */

    public CannotCreateElementProcessorException(final String reason)
    {
        _element_name = null;
        _reason = (reason == null) ? "" : reason;
    }

    public void setElementName(final String name)
    {
        _element_name = name;
    }

    /**
     * override of Throwable's getMessage; allows us to format it
     * with the element name
     *
     * @return a succinct but useful message describing the
     *         problem and which element name we couldn't handle.
     */

    public String getMessage()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Could not create ElementProcessor for element ");
        buffer.append(_element_name);
        buffer.append(" ");
        if (_reason.length() != 0) {
            buffer.append("(").append(_reason).append(")");
        }
        return buffer.toString();
    }
}   // end public class CannotCreateElementProcessorException
