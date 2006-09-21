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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import java.util.Iterator;
import java.util.Map;

/**
 * InputModule specifies an interface for components that provide
 * access to individual attributes e.g. request parameters, request
 * attributes, session attributes &c.
 *
 * @version $Id$
 */
public interface InputModule {

    String ROLE = InputModule.class.getName();


    /**
     * Standard access to an attribute's value. If more than one value
     * exists, the first is returned. If the value does not exist,
     * null is returned. To get all values, use
     * {@link #getAttributeValues(String, Configuration, Map)} or
     * {@link #getAttributeNames(Configuration, Map)} and
     * {@link #getAttribute(String, Configuration, Map)} to get them one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attribute. This is mainly a fallback if no
     * modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    Object getAttribute( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException;


    /**
     * Returns an Iterator of String objects containing the names
     * of the attributes available. If no attributes are available,
     * the method returns an empty Iterator.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    Iterator getAttributeNames( Configuration modeConf, Map objectModel ) throws ConfigurationException;


    /**
     * Returns an array of String objects containing all of the values
     * the given attribute has, or null if the attribute does not
     * exist. As an alternative,
     * {@link #getAttributeNames(Configuration, Map)} together with
     * {@link #getAttribute(String, Configuration, Map)} can be used to get the
     * values one by one.
     * @param name a String that specifies what the caller thinks
     * would identify an attributes. This is mainly a fallback
     * if no modeConf is present.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel
     */
    Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException;

}
