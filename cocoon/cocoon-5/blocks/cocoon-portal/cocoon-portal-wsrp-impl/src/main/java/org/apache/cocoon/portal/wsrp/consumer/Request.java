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
package org.apache.cocoon.portal.wsrp.consumer;

import oasis.names.tc.wsrp.v1.types.NamedString;

/**
 * Defines a request from the end-user to the swing consumer as result
 * of an interaction (e.g. click on hyperlink) of the end-user with the 
 * representation of a remote portlet.<br/> 
 *
 * Due to the two phase protocol of WSRP this request carries information
 * which need to be passed back to the producer in order to process the 
 * interaction at the producer-side.<br/>
 * 
 * @version $Id$
 */
public interface Request {

    /**
     * Set the interaction state of a portlet which should be passed 
     * to the producer.
     * 
     * @param state the interaction state of a portlet
     **/
    void setInteractionState(String state);

    /**
     * Get the interaction state of the portlet.
     * 
     * @return interaction state of a portlet carried in a request
     **/
    String getInteractionState();

    /**
     * Add any parameters to the request. These parameters should
     * be carried in the form parameters field of WSRP.
     * 
     * @param name The key which identifies the parameter
     * @param value The value of the parameter
     **/
    void addFormParameter(String name, String value);

    /**
     * Get all form parameters from the request. The returned
     * <code>NamedString</code> array contains all parameter key/value pairs
     * and can directly be passed to the form parameter field in WSRP.
     *
     * @return Array with all set parameters
     **/
    NamedString[] getFormParameters();
}
